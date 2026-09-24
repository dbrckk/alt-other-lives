package com.alt.otherlives.core.generation

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import com.alt.otherlives.core.io.BoundedStreamCopy
import com.alt.otherlives.core.io.BoundedTextRead
import com.alt.otherlives.core.media.ImageBoundsValidation
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import java.util.UUID
import android.webkit.MimeTypeMap
import android.graphics.BitmapFactory

class ComfyUiClient(
    private val context: Context,
    private val config: ComfyUiConfig
) {
    init { config.validate() }

    data class UploadedImage(val name: String, val subfolder: String, val type: String)
    data class OutputImage(
        val filename: String,
        val subfolder: String,
        val type: String,
        val nodeId: String
    )

    suspend fun testConnection(): Unit = withContext(Dispatchers.IO) {
        val connection = open(
            path = "/system_stats",
            method = "GET",
            connectTimeoutMs = 8_000,
            readTimeoutMs = 10_000
        )
        val body = readResponse(connection)
        require(body.isNotBlank()) { "ComfyUI returned an empty response" }
        val json = runCatching { JSONObject(body) }
            .getOrElse { error("ComfyUI returned an invalid health response") }
        require(json.has("system") || json.has("devices")) {
            "Endpoint responded, but it does not look like ComfyUI"
        }
    }

    suspend fun uploadImage(uri: Uri): UploadedImage = withContext(Dispatchers.IO) {
        val boundary = "ALT-" + UUID.randomUUID()
        val connection = open(
            path = "/upload/image",
            method = "POST",
            connectTimeoutMs = 15_000,
            readTimeoutMs = 60_000
        ).apply {
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            doOutput = true
        }

        try {
            val mimeType = context.contentResolver.getType(uri)
                ?.takeIf { it.startsWith("image/") }
                ?: "image/jpeg"
            val extension = MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(mimeType)
                ?.takeIf { it.isNotBlank() }
                ?: "jpg"
            val filename = "alt-source-" + UUID.randomUUID() + "." + extension
            connection.outputStream.buffered().use { output ->
                fun write(value: String) = output.write(value.toByteArray(Charsets.UTF_8))
                write("--$boundary\r\n")
                write("Content-Disposition: form-data; name=\"image\"; filename=\"$filename\"\r\n")
                write("Content-Type: " + mimeType + "\r\n\r\n")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    BoundedStreamCopy.copy(
                        input = input,
                        output = output,
                        maxBytes = MAX_UPLOAD_IMAGE_BYTES
                    )
                } ?: error("Unable to read selected image")
                write("\r\n--$boundary\r\n")
                write("Content-Disposition: form-data; name=\"overwrite\"\r\n\r\n")
                write("true\r\n")
                write("--$boundary--\r\n")
            }

            val body = readResponse(connection)
            val json = parseJsonObject(body, "upload")
            val name = json.optString("name").takeIf { it.isNotBlank() }
                ?: error("ComfyUI upload response is missing image name")
            UploadedImage(
                name = ComfyUiRemotePath.validateFilename(name),
                subfolder = ComfyUiRemotePath.validateSubfolder(
                    json.optString("subfolder", "")
                ),
                type = ComfyUiRemotePath.validateType(
                    json.optString("type", "input").ifBlank { "input" }
                )
            )
        } finally {
            connection.disconnect()
        }
    }

    suspend fun queuePrompt(workflow: JSONObject): String = withContext(Dispatchers.IO) {
        val connection = open(
            path = "/prompt",
            method = "POST",
            connectTimeoutMs = 10_000,
            readTimeoutMs = 30_000
        ).apply {
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
        }
        try {
            val payload = JSONObject()
                .put("prompt", workflow)
                .put("client_id", config.clientId)
                .toString()
            try {
                connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
                val body = readResponse(connection)
                val json = parseJsonObject(body, "queue")
                val promptId = json.optString("prompt_id").takeIf { it.isNotBlank() }
                    ?: error("ComfyUI queue response is missing prompt_id")
                ComfyUiPromptId.validate(promptId)
            } catch (error: IOException) {
                throw IllegalStateException(
                    "ComfyUI queue request failed after submission may have started; retry manually to avoid duplicate jobs",
                    error
                )
            }
        } finally {
            connection.disconnect()
        }
    }

    suspend fun awaitOutputs(promptId: String, timeoutMs: Long = 180_000L): List<OutputImage> {
        require(timeoutMs > 0L) { "ComfyUI generation timeout must be positive" }
        val startedNanos = System.nanoTime()

        while ((System.nanoTime() - startedNanos) / 1_000_000L < timeoutMs) {
            val snapshot = try {
                historySnapshot(promptId)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Throwable) {
                if (!ComfyUiRetryPolicy.shouldRetry(error)) {
                    throw error
                }
                delay(POLL_INTERVAL_MS)
                continue
            }

            if (snapshot.status == "error") {
                error(snapshot.errorMessage ?: "ComfyUI generation failed")
            }
            if (snapshot.outputs.isNotEmpty()) return snapshot.outputs
            if (snapshot.completed) {
                error("ComfyUI completed without image outputs")
            }
            delay(POLL_INTERVAL_MS)
        }
        error("ComfyUI generation timed out")
    }

    suspend fun download(output: OutputImage, index: Int): Uri = withContext(Dispatchers.IO) {
        val query = "?filename=" + encode(output.filename) +
            "&subfolder=" + encode(output.subfolder) +
            "&type=" + encode(output.type)
        val connection = open(
            path = "/view" + query,
            method = "GET",
            connectTimeoutMs = 15_000,
            readTimeoutMs = 60_000
        )

        val dir = File(context.cacheDir, "generation").apply { mkdirs() }
        cleanupGenerationCache(dir)
        var file: File? = null

        try {
            ensureSuccess(connection)
            val contentType = connection.contentType
                ?.substringBefore(";")
                ?.trim()
                ?.takeIf { it.startsWith("image/") }
            val extension = GeneratedImageExtension.normalize(
                mimeExtension = contentType
                    ?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) },
                filename = output.filename
            )
            file = File(
                dir,
                "scene-" + UUID.randomUUID() + "-" + index + "." + extension
            )
            connection.inputStream.buffered().use { input ->
                file.outputStream().use { outputStream ->
                    BoundedStreamCopy.copy(
                        input = input,
                        output = outputStream,
                        maxBytes = MAX_GENERATED_IMAGE_BYTES
                    )
                }
            }

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, bounds)
            if (!ImageBoundsValidation.isReasonable(bounds.outWidth, bounds.outHeight)) {
                error(
                    "ComfyUI returned an invalid or unreasonable image for chapter " +
                        (index + 1)
                )
            }

            FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        } catch (error: Throwable) {
            file?.delete()
            throw error
        } finally {
            connection.disconnect()
        }
    }

    private data class HistorySnapshot(
        val status: String?,
        val completed: Boolean,
        val errorMessage: String?,
        val outputs: List<OutputImage>
    )

    private suspend fun historySnapshot(promptId: String): HistorySnapshot = withContext(Dispatchers.IO) {
        val connection = open(
            path = "/history/" + ComfyUiPromptId.validate(promptId),
            method = "GET",
            connectTimeoutMs = 8_000,
            readTimeoutMs = 15_000
        )
        val json = parseJsonObject(readResponse(connection), "history")
        val prompt = json.optJSONObject(promptId)
            ?: return@withContext HistorySnapshot(null, false, null, emptyList())

        val statusObject = prompt.optJSONObject("status")
        val status = statusObject?.optString("status_str")?.takeIf { it.isNotBlank() }
        val completed = statusObject?.optBoolean("completed", false) ?: false
        val errorMessage = extractExecutionError(statusObject?.optJSONArray("messages"))
        val outputs = prompt.optJSONObject("outputs")
        val result = mutableListOf<OutputImage>()

        outputs?.keys()?.forEach { key ->
            val node = outputs.optJSONObject(key) ?: return@forEach
            val images = node.optJSONArray("images") ?: JSONArray()
            for (i in 0 until images.length()) {
                val image = images.optJSONObject(i) ?: continue
                result += OutputImage(
                    filename = ComfyUiRemotePath.validateFilename(
                        image.getString("filename")
                    ),
                    subfolder = ComfyUiRemotePath.validateSubfolder(
                        image.optString("subfolder", "")
                    ),
                    type = ComfyUiRemotePath.validateType(
                        image.optString("type", "output").ifBlank { "output" }
                    ),
                    nodeId = key
                )
            }
        }

        HistorySnapshot(
            status = status,
            completed = completed,
            errorMessage = errorMessage,
            outputs = result
        )
    }

    private fun parseJsonObject(body: String, operation: String): JSONObject {
        require(body.isNotBlank()) { "ComfyUI $operation response was empty" }
        return runCatching { JSONObject(body) }
            .getOrElse { error("ComfyUI $operation response was not valid JSON") }
    }

    private fun extractExecutionError(messages: JSONArray?): String? {
        if (messages == null) return null
        for (index in 0 until messages.length()) {
            val message = messages.optJSONArray(index) ?: continue
            if (message.optString(0) != "execution_error") continue
            val detail = message.optJSONObject(1)
            return detail?.optString("exception_message")?.takeIf { it.isNotBlank() }
                ?: detail?.optString("exception_type")?.takeIf { it.isNotBlank() }
                ?: "ComfyUI execution error"
        }
        return null
    }

    private fun open(
        path: String,
        method: String,
        connectTimeoutMs: Int = 15_000,
        readTimeoutMs: Int = 180_000
    ): HttpURLConnection =
        (URL(config.normalizedBaseUrl + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = connectTimeoutMs
            readTimeout = readTimeoutMs
            useCaches = false
        }

    private fun readResponse(connection: HttpURLConnection): String {
        return try {
            ensureSuccess(connection)
            connection.inputStream.bufferedReader().use {
                BoundedTextRead.read(it, MAX_RESPONSE_BODY_CHARS)
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun ensureSuccess(connection: HttpURLConnection) {
        val code = connection.responseCode
        if (code !in 200..299) {
            val body = runCatching {
                connection.errorStream?.bufferedReader()?.use {
                    BoundedTextRead.read(it, MAX_ERROR_BODY_READ_CHARS)
                }
            }.getOrNull().orEmpty()
            val safeBody = body
                .replace(Regex("\\s+"), " ")
                .trim()
                .take(MAX_ERROR_BODY_CHARS)
            val suffix = if (safeBody.isBlank()) "" else ": " + safeBody
            error("ComfyUI HTTP " + code + suffix)
        }
    }

    private fun cleanupGenerationCache(dir: File) {
        val cutoff = System.currentTimeMillis() - CACHE_MAX_AGE_MS
        dir.listFiles()?.forEach { file ->
            if (file.isFile && file.lastModified() < cutoff) {
                file.delete()
            }
        }
    }

    private fun encode(value: String): String =
        URLEncoder.encode(value, Charsets.UTF_8.name())

    private companion object {
        const val CACHE_MAX_AGE_MS = 24L * 60L * 60L * 1000L
        const val MAX_ERROR_BODY_CHARS = 500
        const val MAX_ERROR_BODY_READ_CHARS = 4_096
        const val MAX_RESPONSE_BODY_CHARS = 4_000_000
        const val POLL_INTERVAL_MS = 900L
        const val MAX_UPLOAD_IMAGE_BYTES = 50L * 1024L * 1024L
        const val MAX_GENERATED_IMAGE_BYTES = 100L * 1024L * 1024L
    }
}

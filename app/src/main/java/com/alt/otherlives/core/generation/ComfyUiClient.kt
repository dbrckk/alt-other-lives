package com.alt.otherlives.core.generation

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import java.util.UUID

class ComfyUiClient(
    private val context: Context,
    private val config: ComfyUiConfig
) {
    init { config.validate() }

    data class UploadedImage(val name: String, val subfolder: String, val type: String)
    data class OutputImage(val filename: String, val subfolder: String, val type: String)

    suspend fun uploadImage(uri: Uri): UploadedImage = withContext(Dispatchers.IO) {
        val boundary = "ALT-" + UUID.randomUUID()
        val connection = open("/upload/image", "POST").apply {
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            doOutput = true
        }

        val filename = "alt-source-" + System.currentTimeMillis() + ".jpg"
        connection.outputStream.buffered().use { output ->
            fun write(value: String) = output.write(value.toByteArray(Charsets.UTF_8))
            write("--$boundary\r\n")
            write("Content-Disposition: form-data; name=\"image\"; filename=\"$filename\"\r\n")
            write("Content-Type: image/jpeg\r\n\r\n")
            context.contentResolver.openInputStream(uri)?.use { input -> input.copyTo(output) }
                ?: error("Unable to read selected image")
            write("\r\n--$boundary\r\n")
            write("Content-Disposition: form-data; name=\"overwrite\"\r\n\r\n")
            write("true\r\n")
            write("--$boundary--\r\n")
        }

        val json = JSONObject(readResponse(connection))
        UploadedImage(
            name = json.getString("name"),
            subfolder = json.optString("subfolder", ""),
            type = json.optString("type", "input")
        )
    }

    suspend fun queuePrompt(workflow: JSONObject): String = withContext(Dispatchers.IO) {
        val connection = open("/prompt", "POST").apply {
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
        }
        val payload = JSONObject()
            .put("prompt", workflow)
            .put("client_id", config.clientId)
            .toString()
        connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
        JSONObject(readResponse(connection)).getString("prompt_id")
    }

    suspend fun awaitOutputs(promptId: String, timeoutMs: Long = 180_000L): List<OutputImage> {
        val started = System.currentTimeMillis()
        while (System.currentTimeMillis() - started < timeoutMs) {
            val outputs = historyOutputs(promptId)
            if (outputs.isNotEmpty()) return outputs
            delay(900)
        }
        error("ComfyUI generation timed out")
    }

    suspend fun download(output: OutputImage, index: Int): Uri = withContext(Dispatchers.IO) {
        val query = "?filename=" + encode(output.filename) +
            "&subfolder=" + encode(output.subfolder) +
            "&type=" + encode(output.type)
        val connection = open("/view" + query, "GET")
        ensureSuccess(connection)

        val dir = File(context.cacheDir, "generation").apply { mkdirs() }
        val file = File(dir, "scene-" + System.currentTimeMillis() + "-" + index + ".png")
        connection.inputStream.buffered().use { input ->
            file.outputStream().use { outputStream -> input.copyTo(outputStream) }
        }
        FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
    }

    private suspend fun historyOutputs(promptId: String): List<OutputImage> = withContext(Dispatchers.IO) {
        val connection = open("/history/" + promptId, "GET")
        val json = JSONObject(readResponse(connection))
        val prompt = json.optJSONObject(promptId) ?: return@withContext emptyList()
        val outputs = prompt.optJSONObject("outputs") ?: return@withContext emptyList()
        val result = mutableListOf<OutputImage>()

        outputs.keys().forEach { key ->
            val node = outputs.optJSONObject(key) ?: return@forEach
            val images = node.optJSONArray("images") ?: JSONArray()
            for (i in 0 until images.length()) {
                val image = images.optJSONObject(i) ?: continue
                result += OutputImage(
                    filename = image.getString("filename"),
                    subfolder = image.optString("subfolder", ""),
                    type = image.optString("type", "output")
                )
            }
        }
        result
    }

    private fun open(path: String, method: String): HttpURLConnection =
        (URL(config.normalizedBaseUrl + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15_000
            readTimeout = 180_000
            useCaches = false
        }

    private fun readResponse(connection: HttpURLConnection): String {
        ensureSuccess(connection)
        return connection.inputStream.bufferedReader().use { it.readText() }
    }

    private fun ensureSuccess(connection: HttpURLConnection) {
        val code = connection.responseCode
        if (code !in 200..299) {
            val body = runCatching {
                connection.errorStream?.bufferedReader()?.use { it.readText() }
            }.getOrNull().orEmpty()
            val suffix = if (body.isBlank()) "" else ": " + body
            error("ComfyUI HTTP " + code + suffix)
        }
    }

    private fun encode(value: String): String =
        URLEncoder.encode(value, Charsets.UTF_8.name())
}

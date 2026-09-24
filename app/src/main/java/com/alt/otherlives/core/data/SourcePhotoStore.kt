package com.alt.otherlives.core.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import android.webkit.MimeTypeMap
import android.graphics.BitmapFactory
import java.io.File
import java.util.UUID
import com.alt.otherlives.core.io.BoundedStreamCopy
import com.alt.otherlives.core.media.ImageBoundsValidation

data class StoredPhoto(
    val fileName: String,
    val uri: Uri
)

class SourcePhotoStore(private val context: Context) {
    fun import(uri: Uri): StoredPhoto {
        val dir = File(context.filesDir, "source-photos").apply { mkdirs() }
        val mimeType = context.contentResolver.getType(uri)
        require(mimeType?.startsWith("image/") == true) { "Selected file is not an image" }
        val extension = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(mimeType)
            ?.takeIf { it.isNotBlank() }
            ?: "jpg"
        cleanupInterruptedImports(dir)
        val id = UUID.randomUUID().toString()
        val fileName = "source-" + id + "." + extension
        val target = File(dir, fileName)
        val temporary = File(dir, ".source-" + id + ".tmp")

        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                temporary.outputStream().use { output ->
                    BoundedStreamCopy.copy(
                        input = input,
                        output = output,
                        maxBytes = MAX_SOURCE_PHOTO_BYTES
                    )
                }
            } ?: error("Unable to read selected photo")

            require(temporary.length() > 0L) { "Selected image copy is empty" }
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(temporary.absolutePath, bounds)
            require(
                ImageBoundsValidation.isReasonable(
                    bounds.outWidth,
                    bounds.outHeight
                )
            ) {
                "Selected image is invalid, unsupported, or too large"
            }

            if (!temporary.renameTo(target)) {
                error("Unable to finalize selected photo import")
            }
        } catch (error: Throwable) {
            temporary.delete()
            target.delete()
            throw error
        }

        return StoredPhoto(fileName, uriFor(fileName))
    }

    fun uriFor(fileName: String): Uri {
        val file = storedFile(fileName)
        require(file.exists()) { "Stored photo not found" }
        if (!isValidImage(file)) {
            file.delete()
            error("Stored photo is invalid or corrupted")
        }
        return FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            file
        )
    }

    fun isAvailable(fileName: String): Boolean {
        val file = runCatching { storedFile(fileName) }.getOrNull() ?: return false
        if (!isValidImage(file)) {
            file.delete()
            return false
        }
        return true
    }

    private fun storedFile(fileName: String): File {
        require(SourcePhotoFileName.isValid(fileName)) {
            "Invalid stored photo filename"
        }
        require(File(fileName).name == fileName) { "Invalid stored photo filename" }
        val dir = File(context.filesDir, "source-photos")
        val file = File(dir, fileName)
        require(file.canonicalFile.parentFile == dir.canonicalFile) {
            "Stored photo path escapes private storage"
        }
        return file
    }

    fun latestStoredPhoto(): StoredPhoto? {
        val dir = File(context.filesDir, "source-photos")
        cleanupInterruptedImports(dir)
        val files = dir.listFiles()
            ?.filter { it.isFile && SourcePhotoFileName.isValid(it.name) }
            ?.sortedByDescending { it.lastModified() }
            .orEmpty()

        files.forEach { file ->
            if (!isValidImage(file)) {
                file.delete()
            } else {
                return StoredPhoto(file.name, uriFor(file.name))
            }
        }
        return null
    }

    private fun isValidImage(file: File): Boolean {
        if (!file.exists() || file.length() <= 0L) return false
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        return ImageBoundsValidation.isReasonable(
            bounds.outWidth,
            bounds.outHeight
        )
    }

    private fun cleanupInterruptedImports(dir: File) {
        dir.listFiles()
            ?.filter { it.isFile && it.name.startsWith(".source-") && it.name.endsWith(".tmp") }
            ?.forEach { it.delete() }
    }

    fun deleteUnreferenced(keepFileNames: Set<String>) {
        val dir = File(context.filesDir, "source-photos")
        dir.listFiles()?.forEach { file ->
            if (file.isFile && file.name !in keepFileNames) {
                file.delete()
            }
        }
    }

    fun clearAll() {
        File(context.filesDir, "source-photos").deleteRecursively()
    }

    private companion object {
        const val MAX_SOURCE_PHOTO_BYTES = 50L * 1024L * 1024L
    }
}

package com.alt.otherlives.core.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import android.webkit.MimeTypeMap
import android.graphics.BitmapFactory
import java.io.File

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
        val fileName = "source-" + System.currentTimeMillis() + "." + extension
        val target = File(dir, fileName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        } ?: error("Unable to read selected photo")

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(target.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            target.delete()
            error("Selected image is invalid or unsupported")
        }

        return StoredPhoto(fileName, uriFor(fileName))
    }

    fun uriFor(fileName: String): Uri {
        val file = File(File(context.filesDir, "source-photos"), fileName)
        require(file.exists()) { "Stored photo not found" }
        return FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            file
        )
    }

    fun latestStoredPhoto(): StoredPhoto? {
        val dir = File(context.filesDir, "source-photos")
        val file = dir.listFiles()
            ?.filter { it.isFile }
            ?.maxByOrNull { it.lastModified() }
            ?: return null
        return runCatching {
            StoredPhoto(file.name, uriFor(file.name))
        }.getOrNull()
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
}

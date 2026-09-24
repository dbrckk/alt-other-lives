package com.alt.otherlives.core.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

data class StoredPhoto(
    val fileName: String,
    val uri: Uri
)

class SourcePhotoStore(private val context: Context) {
    fun import(uri: Uri): StoredPhoto {
        val dir = File(context.filesDir, "source-photos").apply { mkdirs() }
        val fileName = "source-" + System.currentTimeMillis() + ".jpg"
        val target = File(dir, fileName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        } ?: error("Unable to read selected photo")

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

    fun clearAll() {
        File(context.filesDir, "source-photos").deleteRecursively()
    }
}

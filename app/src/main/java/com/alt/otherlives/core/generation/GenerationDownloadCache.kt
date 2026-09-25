package com.alt.otherlives.core.generation

import android.content.Context
import android.net.Uri
import java.io.File

internal object GenerationDownloadCache {
    fun ownedFileName(pathSegments: List<String>): String? {
        if (pathSegments.size < 2) return null
        if (pathSegments.first() != "generated_downloads") return null

        val fileName = pathSegments.last()
        if (File(fileName).name != fileName) return null
        if (!fileName.startsWith("scene-")) return null
        return fileName
    }

    fun deleteIfOwned(context: Context, uri: Uri): Boolean {
        if (uri.scheme != "content") return false
        if (uri.authority != context.packageName + ".fileprovider") return false

        val fileName = ownedFileName(uri.pathSegments) ?: return false

        val root = File(context.cacheDir, "generation")
        val file = File(root, fileName)
        if (file.canonicalFile.parentFile != root.canonicalFile) return false

        return !file.exists() || file.delete()
    }
}

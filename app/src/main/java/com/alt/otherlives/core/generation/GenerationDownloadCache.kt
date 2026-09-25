package com.alt.otherlives.core.generation

import android.content.Context
import android.net.Uri
import java.io.File

internal object GenerationDownloadCache {
    fun deleteIfOwned(context: Context, uri: Uri): Boolean {
        if (uri.scheme != "content") return false
        if (uri.authority != context.packageName + ".fileprovider") return false

        val segments = uri.pathSegments
        if (segments.size < 2 || segments.first() != "generated_downloads") return false

        val fileName = segments.last()
        if (File(fileName).name != fileName) return false
        if (!fileName.startsWith("scene-")) return false

        val root = File(context.cacheDir, "generation")
        val file = File(root, fileName)
        if (file.canonicalFile.parentFile != root.canonicalFile) return false

        return !file.exists() || file.delete()
    }
}

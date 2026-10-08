package com.alt.otherlives.core.media

import android.content.Context
import java.io.File

internal object TransientMediaCache {
    internal const val MAX_AGE_MS = 24L * 60L * 60L * 1000L

    internal fun isExpired(
        lastModifiedMs: Long,
        nowMs: Long,
        maxAgeMs: Long = MAX_AGE_MS
    ): Boolean {
        if (maxAgeMs < 0L) return false
        if (lastModifiedMs <= 0L) return true
        if (lastModifiedMs > nowMs) return false
        return nowMs - lastModifiedMs > maxAgeMs
    }

    /**
     * Only delete expired generated downloads and interrupted temporary files.
     * Never remove unrelated cache files or recently downloaded scenes that
     * another coroutine might still be persisting.
     */
    internal fun cleanupGenerationFiles(
        directory: File,
        nowMs: Long = System.currentTimeMillis()
    ) {
        cleanupFiles(directory = directory, nowMs = nowMs) { file ->
            file.name.startsWith("scene-") || file.name.endsWith(".tmp")
        }
    }

    fun cleanup(
        context: Context,
        nowMs: Long = System.currentTimeMillis()
    ) {
        cleanupGenerationFiles(
            directory = File(context.cacheDir, "generation"),
            nowMs = nowMs
        )

        cleanupFiles(
            directory = File(context.cacheDir, "shares"),
            nowMs = nowMs
        ) { file ->
            file.extension.equals("jpg", ignoreCase = true) ||
                file.extension.equals("mp4", ignoreCase = true) ||
                file.name.endsWith(".tmp")
        }

        cleanupFiles(
            directory = File(context.cacheDir, "shares/scenes"),
            nowMs = nowMs
        ) { file ->
            file.extension.equals("jpg", ignoreCase = true) ||
                file.name.endsWith(".tmp")
        }
    }

    private fun cleanupFiles(
        directory: File,
        nowMs: Long,
        ownsFile: (File) -> Boolean
    ) {
        if (!directory.exists()) return

        directory.listFiles()?.forEach { file ->
            if (
                file.isFile &&
                ownsFile(file) &&
                isExpired(file.lastModified(), nowMs)
            ) {
                runCatching { file.delete() }
            }
        }
    }
}

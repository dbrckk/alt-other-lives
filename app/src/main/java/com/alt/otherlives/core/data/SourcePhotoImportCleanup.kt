package com.alt.otherlives.core.data

import java.io.File

/**
 * Imported photos are copied into a hidden temporary file before the final
 * rename. Startup recovery and another import must not remove the temporary
 * copy while the first import is still writing it.
 *
 * Only orphaned imports older than a day are eligible for deletion. The exact
 * filename pattern prevents this cleanup from touching other hidden files.
 */
internal object SourcePhotoImportCleanup {
    internal const val MAX_ORPHAN_AGE_MS = 24L * 60L * 60L * 1000L

    private val ownedName = Regex(
        "^\\.source-[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-" +
            "[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.tmp$"
    )

    fun cleanup(directory: File, nowMs: Long = System.currentTimeMillis()) {
        directory.listFiles()?.forEach { file ->
            if (!file.isFile || !ownedName.matches(file.name)) return@forEach
            val modified = file.lastModified()
            if (modified > nowMs) return@forEach
            if (modified > 0L && nowMs - modified <= MAX_ORPHAN_AGE_MS) return@forEach
            runCatching { file.delete() }
        }
    }
}

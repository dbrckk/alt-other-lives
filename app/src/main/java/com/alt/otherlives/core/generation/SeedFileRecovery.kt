package com.alt.otherlives.core.generation

import java.io.File

internal object SeedFileRecovery {
    private const val MAX_SEED_FILE_BYTES = 32L

    fun recover(root: File) {
        val backup = File(root, ".seed.bak")
        if (!backup.exists()) return

        val target = File(root, "seed.txt")
        if (readSeedOrNull(target) != null) {
            backup.delete()
            return
        }

        if (target.exists() && !target.delete()) {
            error("Unable to discard invalid timeline seed")
        }

        if (readSeedOrNull(backup) != null) {
            if (!backup.renameTo(target)) {
                error("Unable to recover interrupted timeline seed write")
            }
        } else if (!backup.delete()) {
            error("Unable to discard invalid timeline seed backup")
        }
    }

    fun readSeedOrNull(file: File): Long? {
        if (!file.isFile) return null
        val length = file.length()
        if (length <= 0L || length > MAX_SEED_FILE_BYTES) return null

        return runCatching {
            file.readText()
                .trim()
                .toLongOrNull()
                ?.takeIf { it > 0L }
        }.getOrNull()
    }
}

package com.alt.otherlives.core.generation

import java.io.File

internal object SeedFileRecovery {
    fun recover(root: File) {
        val backup = File(root, ".seed.bak")
        if (!backup.exists()) return

        val target = File(root, "seed.txt")
        if (isValidSeedFile(target)) {
            backup.delete()
            return
        }

        if (target.exists() && !target.delete()) {
            error("Unable to discard invalid timeline seed")
        }

        if (isValidSeedFile(backup)) {
            if (!backup.renameTo(target)) {
                error("Unable to recover interrupted timeline seed write")
            }
        } else if (!backup.delete()) {
            error("Unable to discard invalid timeline seed backup")
        }
    }

    internal fun isValidSeedFile(file: File): Boolean =
        file.takeIf { it.isFile }
            ?.runCatching {
                readText()
                    .trim()
                    .toLongOrNull()
                    ?.takeIf { it > 0L }
            }
            ?.getOrNull() != null
}

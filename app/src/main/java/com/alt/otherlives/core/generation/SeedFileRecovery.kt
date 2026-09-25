package com.alt.otherlives.core.generation

import java.io.File

internal object SeedFileRecovery {
    fun recover(root: File) {
        val backup = File(root, ".seed.bak")
        if (!backup.exists()) return

        val target = File(root, "seed.txt")
        if (target.exists()) {
            backup.delete()
            return
        }

        if (!backup.renameTo(target)) {
            error("Unable to recover interrupted timeline seed write")
        }
    }
}

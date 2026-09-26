package com.alt.otherlives.core.generation

internal enum class RecoveryBackupKind {
    SEED,
    SCENE,
    UNKNOWN;

    companion object {
        fun fromName(name: String): RecoveryBackupKind = when {
            name == "seed.txt" -> SEED
            GeneratedSceneFileName.chapterIndex(name) != null -> SCENE
            else -> UNKNOWN
        }
    }
}

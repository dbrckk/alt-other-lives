package com.alt.otherlives.core.generation

import org.junit.Assert.assertEquals
import org.junit.Test

class RecoveryBackupKindTest {
    @Test
    fun classifiesSeedBackup() {
        assertEquals(
            RecoveryBackupKind.SEED,
            RecoveryBackupKind.fromName("seed.txt")
        )
    }

    @Test
    fun classifiesSupportedSceneBackup() {
        assertEquals(
            RecoveryBackupKind.SCENE,
            RecoveryBackupKind.fromName("scene-4.webp")
        )
    }

    @Test
    fun rejectsOutOfRangeOrUnknownBackupNames() {
        assertEquals(
            RecoveryBackupKind.UNKNOWN,
            RecoveryBackupKind.fromName("scene-5.png")
        )
        assertEquals(
            RecoveryBackupKind.UNKNOWN,
            RecoveryBackupKind.fromName("notes.txt")
        )
    }
}

package com.alt.otherlives.core.generation

import org.junit.Assert.assertEquals
import org.junit.Test

class SceneBackupRecoveryTest {
    @Test
    fun keepsValidCurrentScene() {
        assertEquals(
            SceneBackupRecovery.Action.KEEP_CURRENT,
            SceneBackupRecovery.decide(
                hasValidCurrent = true,
                hasValidBackup = true
            )
        )
    }

    @Test
    fun restoresValidBackupWhenCurrentSceneIsInvalid() {
        assertEquals(
            SceneBackupRecovery.Action.RESTORE_BACKUP,
            SceneBackupRecovery.decide(
                hasValidCurrent = false,
                hasValidBackup = true
            )
        )
    }

    @Test
    fun discardsInvalidBackupWhenNoValidSceneExists() {
        assertEquals(
            SceneBackupRecovery.Action.DISCARD_BACKUP,
            SceneBackupRecovery.decide(
                hasValidCurrent = false,
                hasValidBackup = false
            )
        )
    }
}

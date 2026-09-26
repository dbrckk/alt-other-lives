package com.alt.otherlives.core.generation

import org.junit.Assert.assertEquals
import org.junit.Test

class BatchBackupRecoveryTest {
    @Test
    fun keepsValidTarget() {
        assertEquals(
            BatchBackupRecovery.Action.KEEP_TARGET,
            BatchBackupRecovery.decide(
                hasValidTarget = true,
                hasValidBackup = true
            )
        )
    }

    @Test
    fun restoresValidBackupOverInvalidTarget() {
        assertEquals(
            BatchBackupRecovery.Action.RESTORE_BACKUP,
            BatchBackupRecovery.decide(
                hasValidTarget = false,
                hasValidBackup = true
            )
        )
    }

    @Test
    fun preservesTransactionWhenNeitherCopyIsValid() {
        assertEquals(
            BatchBackupRecovery.Action.PRESERVE_RECOVERY_DATA,
            BatchBackupRecovery.decide(
                hasValidTarget = false,
                hasValidBackup = false
            )
        )
    }
}

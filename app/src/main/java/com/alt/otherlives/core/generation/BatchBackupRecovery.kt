package com.alt.otherlives.core.generation

internal object BatchBackupRecovery {
    enum class Action {
        KEEP_TARGET,
        RESTORE_BACKUP,
        PRESERVE_RECOVERY_DATA
    }

    fun decide(
        hasValidTarget: Boolean,
        hasValidBackup: Boolean
    ): Action = when {
        hasValidTarget -> Action.KEEP_TARGET
        hasValidBackup -> Action.RESTORE_BACKUP
        else -> Action.PRESERVE_RECOVERY_DATA
    }
}

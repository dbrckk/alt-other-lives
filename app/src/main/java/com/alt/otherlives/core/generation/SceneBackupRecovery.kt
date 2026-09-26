package com.alt.otherlives.core.generation

internal object SceneBackupRecovery {
    enum class Action {
        KEEP_CURRENT,
        RESTORE_BACKUP,
        DISCARD_BACKUP
    }

    fun decide(
        hasValidCurrent: Boolean,
        hasValidBackup: Boolean
    ): Action = when {
        hasValidCurrent -> Action.KEEP_CURRENT
        hasValidBackup -> Action.RESTORE_BACKUP
        else -> Action.DISCARD_BACKUP
    }
}

package com.alt.otherlives.app

internal enum class StartupRestoreDecision {
    USE_CURRENT_HISTORY_CONTEXT,
    RESTORE_HISTORY_PHOTO,
    KEEP_CURRENT_PHOTO
}

internal object StartupRestorePolicy {
    fun decide(
        currentPhotoFileName: String?,
        latestHistoryPhotoFileName: String?,
        hasLatestStoredPhoto: Boolean
    ): StartupRestoreDecision {
        if (
            latestHistoryPhotoFileName != null &&
            latestHistoryPhotoFileName == currentPhotoFileName
        ) {
            return StartupRestoreDecision.USE_CURRENT_HISTORY_CONTEXT
        }

        if (!hasLatestStoredPhoto && latestHistoryPhotoFileName != null) {
            return StartupRestoreDecision.RESTORE_HISTORY_PHOTO
        }

        return StartupRestoreDecision.KEEP_CURRENT_PHOTO
    }
}

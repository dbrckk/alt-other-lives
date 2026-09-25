package com.alt.otherlives.app

import org.junit.Assert.assertEquals
import org.junit.Test

class StartupRestorePolicyTest {
    @Test
    fun matchingCurrentPhotoRestoresHistoryContext() {
        assertEquals(
            StartupRestoreDecision.USE_CURRENT_HISTORY_CONTEXT,
            StartupRestorePolicy.decide(
                currentPhotoFileName = "source-a.jpg",
                latestHistoryPhotoFileName = "source-a.jpg",
                hasLatestStoredPhoto = true
            )
        )
    }

    @Test
    fun looseNewerPhotoDoesNotAdoptOldTimelineContext() {
        assertEquals(
            StartupRestoreDecision.KEEP_CURRENT_PHOTO,
            StartupRestorePolicy.decide(
                currentPhotoFileName = "source-new.jpg",
                latestHistoryPhotoFileName = "source-old.jpg",
                hasLatestStoredPhoto = true
            )
        )
    }

    @Test
    fun missingLoosePhotoFallsBackToHistoryPhoto() {
        assertEquals(
            StartupRestoreDecision.RESTORE_HISTORY_PHOTO,
            StartupRestorePolicy.decide(
                currentPhotoFileName = null,
                latestHistoryPhotoFileName = "source-old.jpg",
                hasLatestStoredPhoto = false
            )
        )
    }

    @Test
    fun historyWithoutPhotoDoesNotReplaceCurrentState() {
        assertEquals(
            StartupRestoreDecision.KEEP_CURRENT_PHOTO,
            StartupRestorePolicy.decide(
                currentPhotoFileName = null,
                latestHistoryPhotoFileName = null,
                hasLatestStoredPhoto = false
            )
        )
    }
}

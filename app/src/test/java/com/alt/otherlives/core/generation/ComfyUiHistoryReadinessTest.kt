package com.alt.otherlives.core.generation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ComfyUiHistoryReadinessTest {
    @Test
    fun intermediateImageMustNotFinishPendingGeneration() {
        assertEquals(
            ComfyUiHistoryReadiness.Decision.KEEP_POLLING,
            ComfyUiHistoryReadiness.decide(completed = false, imageCount = 1)
        )
    }

    @Test
    fun multipleIntermediateOutputsStillRequireCompletion() {
        assertEquals(
            ComfyUiHistoryReadiness.Decision.KEEP_POLLING,
            ComfyUiHistoryReadiness.decide(completed = false, imageCount = 4)
        )
    }

    @Test
    fun missingHistoryStillPolls() {
        assertEquals(
            ComfyUiHistoryReadiness.Decision.KEEP_POLLING,
            ComfyUiHistoryReadiness.decide(completed = false, imageCount = 0)
        )
    }

    @Test
    fun terminalHistoryWithOutputsBecomesReady() {
        assertEquals(
            ComfyUiHistoryReadiness.Decision.READY,
            ComfyUiHistoryReadiness.decide(completed = true, imageCount = 2)
        )
    }

    @Test
    fun completedWorkflowWithoutImagesFailsInsteadOfWaitingUntilTimeout() {
        assertEquals(
            ComfyUiHistoryReadiness.Decision.COMPLETED_WITHOUT_IMAGES,
            ComfyUiHistoryReadiness.decide(completed = true, imageCount = 0)
        )
    }

    @Test
    fun previewThenFinalSequenceNeverAcceptsPreviewPrematurely() {
        val snapshots = listOf(
            false to 0,
            false to 1,
            false to 3,
            true to 2
        )
        assertEquals(
            listOf(
                ComfyUiHistoryReadiness.Decision.KEEP_POLLING,
                ComfyUiHistoryReadiness.Decision.KEEP_POLLING,
                ComfyUiHistoryReadiness.Decision.KEEP_POLLING,
                ComfyUiHistoryReadiness.Decision.READY
            ),
            snapshots.map { (completed, count) ->
                ComfyUiHistoryReadiness.decide(completed, count)
            }
        )
    }

    @Test
    fun malformedNegativeImageCountIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            ComfyUiHistoryReadiness.decide(completed = true, imageCount = -1)
        }
    }
}

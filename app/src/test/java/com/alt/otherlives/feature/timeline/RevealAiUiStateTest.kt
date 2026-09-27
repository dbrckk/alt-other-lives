package com.alt.otherlives.feature.timeline

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RevealAiUiStateTest {
    @Test
    fun startResetsPreviousProgressAndFailures() {
        val state = RevealAiUiState(
            isGenerating = false,
            isCancelling = true,
            completed = 3,
            total = 5,
            chapterFailures = mapOf(2 to "failed")
        ).start(total = 4)

        assertTrue(state.isGenerating)
        assertFalse(state.isCancelling)
        assertEquals(0, state.completed)
        assertEquals(4, state.total)
        assertTrue(state.chapterFailures.isEmpty())
    }

    @Test
    fun failureAndRetentionKeepOnlyRelevantChapters() {
        val state = RevealAiUiState()
            .failure(1, "one")
            .failure(3, "three")
            .retainFailures(setOf(3))

        assertEquals(mapOf(3 to "three"), state.chapterFailures)
    }

    @Test
    fun finishClearsBusyFlagsButKeepsResultContext() {
        val state = RevealAiUiState(
            isGenerating = true,
            isCancelling = true,
            completed = 4,
            total = 5,
            chapterFailures = mapOf(4 to "failed")
        ).finish()

        assertFalse(state.isGenerating)
        assertFalse(state.isCancelling)
        assertEquals(4, state.completed)
        assertEquals(5, state.total)
        assertEquals(mapOf(4 to "failed"), state.chapterFailures)
    }
}

package com.alt.otherlives.core.generation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ComfyUiHistoryFailureTest {
    @Test
    fun executionMessageWinsEvenBeforeErrorStatus() {
        assertEquals(
            "CUDA out of memory",
            ComfyUiClient.generationFailureMessage(
                status = "running",
                errorMessage = "CUDA out of memory"
            )
        )
    }

    @Test
    fun errorStatusFallsBackToGenericMessage() {
        assertEquals(
            "ComfyUI generation failed",
            ComfyUiClient.generationFailureMessage(
                status = "ERROR",
                errorMessage = null
            )
        )
    }

    @Test
    fun runningWithoutExecutionErrorKeepsPolling() {
        assertNull(
            ComfyUiClient.generationFailureMessage(
                status = "running",
                errorMessage = null
            )
        )
    }
}

package com.alt.otherlives.core.generation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ComfyUiPollingTimingTest {
    @Test
    fun pollsWithinRemainingBudgetInsteadOfTheFixedInterval() {
        assertEquals(200L, ComfyUiPollingTiming.nextDelayMs(200L, 900L))
        assertEquals(900L, ComfyUiPollingTiming.nextDelayMs(1_800L, 900L))
        assertEquals(0L, ComfyUiPollingTiming.nextDelayMs(0L, 900L))
    }

    @Test
    fun clampsNetworkWaitsToRemainingBudget() {
        assertEquals(250, ComfyUiPollingTiming.networkTimeoutMs(250L, 8_000))
        assertEquals(250, ComfyUiPollingTiming.networkTimeoutMs(250L, 15_000))
        assertEquals(8_000, ComfyUiPollingTiming.networkTimeoutMs(180_000L, 8_000))
        assertEquals(15_000, ComfyUiPollingTiming.networkTimeoutMs(180_000L, 15_000))
    }

    @Test
    fun networkTimeoutRemainsPositiveAtBoundary() {
        assertEquals(1, ComfyUiPollingTiming.networkTimeoutMs(0L, 8_000))
        assertEquals(1, ComfyUiPollingTiming.networkTimeoutMs(-10L, 8_000))
    }

    @Test
    fun remainingBudgetStopsPollingExactlyAtExpiration() {
        assertEquals(180_000L, ComfyUiPollingTiming.remainingMs(180_000L, 0L))
        assertEquals(500L, ComfyUiPollingTiming.remainingMs(1_000L, 500L))
        assertEquals(0L, ComfyUiPollingTiming.remainingMs(1_000L, 1_000L))
        assertEquals(0L, ComfyUiPollingTiming.remainingMs(1_000L, 1_001L))
    }

    @Test
    fun elapsedClockAnomalyCannotIncreaseRemainingBudget() {
        assertEquals(1_000L, ComfyUiPollingTiming.remainingMs(1_000L, -500L))
    }

    @Test
    fun invalidTimingArgumentsAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            ComfyUiPollingTiming.remainingMs(0L, 100L)
        }
        assertThrows(IllegalArgumentException::class.java) {
            ComfyUiPollingTiming.nextDelayMs(100L, 0L)
        }
        assertThrows(IllegalArgumentException::class.java) {
            ComfyUiPollingTiming.networkTimeoutMs(100L, 0)
        }
    }
}

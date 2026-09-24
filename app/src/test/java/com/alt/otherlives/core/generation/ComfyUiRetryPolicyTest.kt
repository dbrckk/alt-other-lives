package com.alt.otherlives.core.generation

import java.io.IOException
import java.net.SocketTimeoutException
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ComfyUiRetryPolicyTest {
    @Test
    fun retriesTimeoutsAndIoFailures() {
        assertTrue(ComfyUiRetryPolicy.shouldRetry(SocketTimeoutException("timeout")))
        assertTrue(ComfyUiRetryPolicy.shouldRetry(IOException("connection reset")))
        assertFalse(ComfyUiRetryPolicy.shouldRetry(IllegalStateException("ComfyUI generation timed out")))
    }

    @Test
    fun retriesRateLimitsAndServerFailures() {
        assertTrue(ComfyUiRetryPolicy.shouldRetry(IllegalStateException("ComfyUI HTTP 429")))
        assertTrue(ComfyUiRetryPolicy.shouldRetry(IllegalStateException("ComfyUI HTTP 503")))
        assertTrue(ComfyUiRetryPolicy.shouldRetry(IllegalStateException("ComfyUI HTTP 408")))
    }

    @Test
    fun doesNotRetryPermanentClientOrWorkflowFailures() {
        assertFalse(ComfyUiRetryPolicy.shouldRetry(IllegalStateException("ComfyUI HTTP 400")))
        assertFalse(ComfyUiRetryPolicy.shouldRetry(IllegalStateException("Workflow JSON is malformed")))
        assertFalse(ComfyUiRetryPolicy.shouldRetry(IllegalStateException("ComfyUI completed without image outputs")))
    }
}

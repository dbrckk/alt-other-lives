package com.alt.otherlives.core.generation

import java.io.IOException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException
import org.junit.Assert.assertEquals
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
        for (code in listOf(408, 425, 429, 500, 502, 503, 599)) {
            assertTrue(ComfyUiRetryPolicy.shouldRetry(ComfyUiHttpException(code)))
        }
    }

    @Test
    fun doesNotRetryAmbiguousQueueIoWrapper() {
        assertFalse(
            ComfyUiRetryPolicy.shouldRetry(
                IllegalStateException(
                    "ComfyUI queue request failed after submission may have started",
                    IOException("connection reset")
                )
            )
        )
    }

    @Test
    fun typedGeneratedSceneQualityExceptionIsNeverRetryable() {
        assertFalse(
            ComfyUiRetryPolicy.shouldRetry(
                GeneratedSceneQualityException("quality rejected")
            )
        )
    }

    @Test
    fun doesNotRetryGeneratedSceneQualityFailures() {
        assertFalse(
            ComfyUiRetryPolicy.shouldRetry(
                IllegalStateException(
                    "ComfyUI output is below ALT's minimum scene quality for chapter 2"
                )
            )
        )
        assertFalse(
            ComfyUiRetryPolicy.shouldRetry(
                IllegalStateException(
                    "ComfyUI returned a visually empty or near-uniform image for chapter 3"
                )
            )
        )
    }

    @Test
    fun doesNotRetryPermanentClientOrWorkflowFailures() {
        for (code in listOf(301, 302, 400, 401, 403, 404, 413, 422)) {
            assertFalse(ComfyUiRetryPolicy.shouldRetry(ComfyUiHttpException(code)))
        }
        assertFalse(ComfyUiRetryPolicy.shouldRetry(IllegalStateException("Workflow JSON is malformed")))
        assertFalse(ComfyUiRetryPolicy.shouldRetry(IllegalStateException("ComfyUI completed without image outputs")))
    }
    @Test
    fun plainTextCannotImpersonateARetryableHttpStatus() {
        assertFalse(
            ComfyUiRetryPolicy.shouldRetry(
                IllegalStateException("Model execution failed: ComfyUI HTTP 503")
            )
        )
        assertFalse(
            ComfyUiRetryPolicy.shouldRetry(
                IllegalArgumentException("ComfyUI HTTP 429")
            )
        )
        assertTrue(
            ComfyUiRetryPolicy.shouldRetry(
                ComfyUiHttpException(429, "Proxy error: ComfyUI HTTP 400")
            )
        )
        assertFalse(
            ComfyUiRetryPolicy.shouldRetry(
                ComfyUiHttpException(404, "Proxy error: ComfyUI HTTP 503")
            )
        )
    }

    @Test
    fun tlsCertificateAndHostnameFailuresAreNotRetried() {
        assertFalse(ComfyUiRetryPolicy.shouldRetry(SSLHandshakeException("untrusted root")))
        assertFalse(ComfyUiRetryPolicy.shouldRetry(SSLPeerUnverifiedException("hostname mismatch")))
    }

    @Test
    fun typedHttpErrorRetainsDiagnosticStatusAndBoundedMessage() {
        val error = ComfyUiHttpException(429, "rate limit")
        assertEquals(429, error.statusCode)
        assertEquals("ComfyUI HTTP 429: rate limit", error.message)
        assertEquals("ComfyUI HTTP 503", ComfyUiHttpException(503).message)
    }

}

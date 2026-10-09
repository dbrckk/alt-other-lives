package com.alt.otherlives.core.generation

import java.io.IOException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException

internal object ComfyUiRetryPolicy {
    fun shouldRetry(error: Throwable): Boolean = when (error) {
        // A certificate, hostname, or TLS trust failure needs a configuration
        // change. Never retry it as a transient connectivity error.
        is SSLHandshakeException, is SSLPeerUnverifiedException -> false

        is ComfyUiHttpException -> error.statusCode == 408 ||
            error.statusCode == 425 ||
            error.statusCode == 429 ||
            error.statusCode in 500..599

        // Includes socket timeouts and temporary broken connections.
        is IOException -> true
        else -> false
    }
}

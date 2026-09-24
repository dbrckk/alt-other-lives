package com.alt.otherlives.core.generation

import java.io.IOException
import java.net.SocketTimeoutException

internal object ComfyUiRetryPolicy {
    private val httpCodePattern = Regex("""ComfyUI HTTP (\d{3})""")

    fun shouldRetry(error: Throwable): Boolean {
        if (error is SocketTimeoutException) return true
        if (error is IOException) return true

        val message = error.message.orEmpty()
        val httpCode = httpCodePattern.find(message)
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull()

        return httpCode == 408 ||
            httpCode == 425 ||
            httpCode == 429 ||
            httpCode in 500..599
    }
}

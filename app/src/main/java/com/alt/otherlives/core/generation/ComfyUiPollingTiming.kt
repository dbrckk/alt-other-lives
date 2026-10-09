package com.alt.otherlives.core.generation

/**
 * Bounds history polling and each network wait by the remaining generation
 * budget. HttpURLConnection timeouts are per blocking operation, rather than
 * a strict whole-call deadline; this prevents short requested budgets from
 * using the much longer default connect/read timeouts.
 */
internal object ComfyUiPollingTiming {
    fun remainingMs(timeoutMs: Long, elapsedMs: Long): Long {
        require(timeoutMs > 0L) { "ComfyUI generation timeout must be positive" }
        return (timeoutMs - elapsedMs.coerceAtLeast(0L)).coerceAtLeast(0L)
    }

    fun nextDelayMs(remainingMs: Long, pollIntervalMs: Long): Long {
        require(pollIntervalMs > 0L) { "Polling interval must be positive" }
        return remainingMs.coerceAtLeast(0L).coerceAtMost(pollIntervalMs)
    }

    fun networkTimeoutMs(remainingMs: Long, defaultMs: Int): Int {
        require(defaultMs > 0) { "Network timeout must be positive" }
        return remainingMs.coerceAtLeast(1L).coerceAtMost(defaultMs.toLong()).toInt()
    }
}

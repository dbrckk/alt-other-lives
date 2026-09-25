package com.alt.otherlives.core.generation

internal object GenerationDownloadLifecycle {
    inline fun <T> persistAndRelease(
        persist: () -> T,
        release: () -> Unit
    ): T = try {
        persist()
    } finally {
        release()
    }
}

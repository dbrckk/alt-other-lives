package com.alt.otherlives.core.generation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerationDownloadLifecycleTest {
    @Test
    fun releasesDownloadAfterSuccessfulPersistence() {
        var released = false

        val result = GenerationDownloadLifecycle.persistAndRelease(
            persist = { "persisted" },
            release = { released = true }
        )

        assertEquals("persisted", result)
        assertTrue(released)
    }

    @Test
    fun releasesDownloadWhenPersistenceFails() {
        var released = false

        val error = runCatching {
            GenerationDownloadLifecycle.persistAndRelease<Unit>(
                persist = { error("disk full") },
                release = { released = true }
            )
        }.exceptionOrNull()

        assertTrue(error is IllegalStateException)
        assertTrue(released)
    }
}

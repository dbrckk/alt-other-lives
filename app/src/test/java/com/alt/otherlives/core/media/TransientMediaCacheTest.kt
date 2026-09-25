package com.alt.otherlives.core.media

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransientMediaCacheTest {
    @Test
    fun expiresOnlyFilesOlderThanMaximumAge() {
        val now = 10L * TransientMediaCache.MAX_AGE_MS

        assertFalse(
            TransientMediaCache.isExpired(
                lastModifiedMs = now - TransientMediaCache.MAX_AGE_MS,
                nowMs = now
            )
        )
        assertTrue(
            TransientMediaCache.isExpired(
                lastModifiedMs = now - TransientMediaCache.MAX_AGE_MS - 1L,
                nowMs = now
            )
        )
    }

    @Test
    fun futureTimestampIsNotExpired() {
        val now = 5L * TransientMediaCache.MAX_AGE_MS

        assertFalse(
            TransientMediaCache.isExpired(
                lastModifiedMs = now + 1_000L,
                nowMs = now
            )
        )
    }

    @Test
    fun missingTimestampIsTreatedAsExpired() {
        assertTrue(
            TransientMediaCache.isExpired(
                lastModifiedMs = 0L,
                nowMs = TransientMediaCache.MAX_AGE_MS
            )
        )
    }
}

package com.alt.otherlives.core.data

import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryTimestampAllocatorTest {
    @Test
    fun keepsRequestedTimestampWhenUnused() {
        assertEquals(
            100L,
            HistoryTimestampAllocator.nextAvailable(
                scenarioId = "career",
                requestedCreatedAt = 100L,
                existingEntries = emptyList()
            )
        )
    }

    @Test
    fun incrementsUntilScenarioTimestampIsUnique() {
        val existing = listOf(
            HistoryEntry("career", 100L),
            HistoryEntry("career", 101L)
        )

        assertEquals(
            102L,
            HistoryTimestampAllocator.nextAvailable(
                scenarioId = "career",
                requestedCreatedAt = 100L,
                existingEntries = existing
            )
        )
    }

    @Test
    fun sameTimestampInDifferentScenarioDoesNotCollide() {
        assertEquals(
            100L,
            HistoryTimestampAllocator.nextAvailable(
                scenarioId = "travel",
                requestedCreatedAt = 100L,
                existingEntries = listOf(HistoryEntry("career", 100L))
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNonPositiveTimestamp() {
        HistoryTimestampAllocator.nextAvailable(
            scenarioId = "career",
            requestedCreatedAt = 0L,
            existingEntries = emptyList()
        )
    }
}

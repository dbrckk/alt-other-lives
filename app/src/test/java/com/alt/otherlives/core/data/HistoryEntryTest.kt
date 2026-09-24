package com.alt.otherlives.core.data

import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryEntryTest {
    @Test
    fun timelineKeyIsStableAndCanonical() {
        val entry = HistoryEntry(
            scenarioId = "japan",
            createdAt = 1_725_000_000_000L,
            photoFileName = "source-example.jpg"
        )

        assertEquals("japan-1725000000000", entry.timelineKey)
    }
}

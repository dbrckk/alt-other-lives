package com.alt.otherlives.core.data

import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryEntryListPolicyTest {
    @Test
    fun limitsRestoredHistoryToMaximumEntries() {
        val entries = (1L..75L).map { createdAt ->
            HistoryEntry("japan", createdAt)
        }

        val restored = HistoryEntryListPolicy.sanitize(entries.asSequence())

        assertEquals(HistoryEntryListPolicy.MAX_ENTRIES, restored.size)
        assertEquals(1L, restored.first().createdAt)
        assertEquals(50L, restored.last().createdAt)
    }

    @Test
    fun removesDuplicateTimelineKeysKeepingFirstEntry() {
        val restored = HistoryEntryListPolicy.sanitize(
            sequenceOf(
                HistoryEntry("japan", 100L, "source-first.jpg"),
                HistoryEntry("japan", 100L, "source-second.jpg"),
                HistoryEntry("japan", 101L, "source-third.jpg")
            )
        )

        assertEquals(2, restored.size)
        assertEquals("source-first.jpg", restored.first().photoFileName)
        assertEquals(101L, restored.last().createdAt)
    }
}

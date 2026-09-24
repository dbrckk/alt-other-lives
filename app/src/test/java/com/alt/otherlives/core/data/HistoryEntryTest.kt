package com.alt.otherlives.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
    @Test
    fun validationAcceptsKnownScenarioAndCanonicalPhoto() {
        assertTrue(
            HistoryEntryValidation.isValid(
                scenarioId = "japan",
                createdAt = 1_725_000_000_000L,
                photoFileName = "source-example.jpg"
            )
        )
    }

    @Test
    fun validationAcceptsLegacyEntryWithoutPhoto() {
        assertTrue(
            HistoryEntryValidation.isValid(
                scenarioId = "japan",
                createdAt = 1_725_000_000_000L,
                photoFileName = null
            )
        )
    }

    @Test
    fun validationRejectsUnknownScenario() {
        assertFalse(
            HistoryEntryValidation.isValid(
                scenarioId = "../unknown",
                createdAt = 1_725_000_000_000L,
                photoFileName = null
            )
        )
    }

    @Test
    fun validationRejectsInvalidTimestamp() {
        assertFalse(
            HistoryEntryValidation.isValid(
                scenarioId = "japan",
                createdAt = 0L,
                photoFileName = null
            )
        )
    }

    @Test
    fun validationRejectsPhotoPathTraversal() {
        assertFalse(
            HistoryEntryValidation.isValid(
                scenarioId = "japan",
                createdAt = 1_725_000_000_000L,
                photoFileName = "../source-example.jpg"
            )
        )
    }
}


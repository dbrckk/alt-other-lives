package com.alt.otherlives.core.data

import com.alt.otherlives.core.media.NormalizedCropRect
import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryEntryCodecTest {
    @Test
    fun legacyRowsRestoreWithFullCrop() {
        val decoded = HistoryEntryCodec.decode(
            "japan|1725000000000|source-example.jpg"
        )

        assertEquals(1, decoded.size)
        assertEquals(NormalizedCropRect.Full, decoded.single().sourceCrop)
    }

    @Test
    fun encodedRowsRoundTripTimelineCrop() {
        val crop = NormalizedCropRect(
            left = 0.15f,
            top = 0.2f,
            right = 0.8f,
            bottom = 0.9f
        )
        val entry = HistoryEntry(
            scenarioId = "japan",
            createdAt = 1_725_000_000_000L,
            photoFileName = "source-example.jpg",
            sourceCrop = crop
        )

        val decoded = HistoryEntryCodec.decode(
            HistoryEntryCodec.encode(listOf(entry))
        )

        assertEquals(listOf(entry), decoded)
        assertEquals(crop, decoded.single().sourceCrop)
    }

    @Test
    fun malformedCropRejectsRowInsteadOfUsingUnsafeGeometry() {
        val decoded = HistoryEntryCodec.decode(
            "japan|1725000000000|source-example.jpg|0.8|0.2|0.1|0.9"
        )

        assertEquals(emptyList<HistoryEntry>(), decoded)
    }
}

package com.alt.otherlives.core.generation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AiGenerationReportStoreTest {
    @Test
    fun reportRoundTripsWithoutMediaData() {
        val report = AiGenerationReport(
            timelineKey = "future-123",
            scenarioId = "future",
            reason = AiGenerationReportReason.OTHER_UNSAFE,
            createdAt = 123L
        )

        val encoded = AiGenerationReportCodec.encode(report)

        assertEquals(report, AiGenerationReportCodec.decode(encoded))
        assertEquals(4, encoded.split("|").size)
    }

    @Test
    fun malformedReportIsIgnored() {
        assertNull(AiGenerationReportCodec.decode("bad"))
    }

    @Test
    fun invalidTimelineKeyIsIgnored() {
        assertNull(
            AiGenerationReportCodec.decode(
                "../escape|future|OTHER_UNSAFE|123"
            )
        )
    }
}

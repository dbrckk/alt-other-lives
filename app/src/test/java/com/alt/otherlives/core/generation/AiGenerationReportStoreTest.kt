package com.alt.otherlives.core.generation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AiGenerationReportStoreTest {
    private val codec = UnsafeCodec()

    @Test
    fun reportRoundTripsWithoutMediaData() {
        val report = AiGenerationReport(
            timelineKey = "future-123",
            scenarioId = "future",
            reason = AiGenerationReportReason.OTHER_UNSAFE,
            createdAt = 123L
        )

        assertEquals(report, codec.decode(codec.encode(report)))
    }

    @Test
    fun malformedReportIsIgnored() {
        assertNull(codec.decode("bad"))
    }

    private class UnsafeCodec {
        private val separator = "|"

        fun encode(report: AiGenerationReport): String =
            listOf(
                report.timelineKey,
                report.scenarioId,
                report.reason.name,
                report.createdAt.toString()
            ).joinToString(separator)

        fun decode(value: String): AiGenerationReport? {
            val fields = value.split(separator, limit = 4)
            if (fields.size != 4) return null
            val reason = runCatching {
                AiGenerationReportReason.valueOf(fields[2])
            }.getOrNull() ?: return null
            val createdAt = fields[3].toLongOrNull()?.takeIf { it > 0L } ?: return null
            return runCatching {
                GeneratedTimelineKey.validate(fields[0])
                require(fields[1].isNotBlank())
                AiGenerationReport(fields[0], fields[1], reason, createdAt)
            }.getOrNull()
        }
    }
}

package com.alt.otherlives.core.generation

import android.content.Context
import java.io.File

enum class AiGenerationReportReason(val label: String) {
    SEXUAL_CONTENT("Sexual or nude content"),
    VIOLENCE("Violence or graphic content"),
    HATE_OR_HARASSMENT("Hate or harassment"),
    OTHER_UNSAFE("Other unsafe content")
}

data class AiGenerationReport(
    val timelineKey: String,
    val scenarioId: String,
    val reason: AiGenerationReportReason,
    val createdAt: Long
)

object AiGenerationReportCodec {
    private const val FIELD_SEPARATOR = "|"

    fun encode(report: AiGenerationReport): String =
        listOf(
            report.timelineKey,
            report.scenarioId,
            report.reason.name,
            report.createdAt.toString()
        ).joinToString(FIELD_SEPARATOR)

    fun decode(value: String): AiGenerationReport? {
        val fields = value.split(FIELD_SEPARATOR, limit = 4)
        if (fields.size != 4) return null
        val timelineKey = fields[0]
        val scenarioId = fields[1]
        val reason = runCatching { AiGenerationReportReason.valueOf(fields[2]) }.getOrNull()
            ?: return null
        val createdAt = fields[3].toLongOrNull()?.takeIf { it > 0L } ?: return null

        return runCatching {
            GeneratedTimelineKey.validate(timelineKey)
            require(scenarioId.isNotBlank())
            AiGenerationReport(
                timelineKey = timelineKey,
                scenarioId = scenarioId,
                reason = reason,
                createdAt = createdAt
            )
        }.getOrNull()
    }
}

class AiGenerationReportStore(context: Context) {
    private val file = File(context.filesDir, FILE_NAME)

    fun record(report: AiGenerationReport) {
        GeneratedTimelineKey.validate(report.timelineKey)
        require(report.scenarioId.isNotBlank()) { "Scenario ID is required" }
        require(report.createdAt > 0L) { "Report timestamp must be positive" }

        val existing = if (file.exists()) {
            file.readLines()
                .mapNotNull(AiGenerationReportCodec::decode)
                .takeLast(MAX_REPORTS - 1)
        } else {
            emptyList()
        }
        writeAtomically(existing + report)
    }

    fun pendingReports(): List<AiGenerationReport> =
        if (!file.exists()) emptyList()
        else file.readLines().mapNotNull(AiGenerationReportCodec::decode).takeLast(MAX_REPORTS)

    private fun writeAtomically(reports: List<AiGenerationReport>) {
        val temporary = File(file.parentFile, "." + file.name + ".tmp")
        try {
            temporary.bufferedWriter().use { writer ->
                reports.takeLast(MAX_REPORTS).forEach { report ->
                    writer.append(AiGenerationReportCodec.encode(report))
                    writer.newLine()
                }
            }
            if (!temporary.renameTo(file)) {
                error("Unable to persist AI generation report")
            }
        } finally {
            if (temporary.exists()) {
                temporary.delete()
            }
        }
    }

    private companion object {
        const val FILE_NAME = "ai_generation_reports.txt"
        const val MAX_REPORTS = 100
    }
}

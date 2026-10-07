package com.alt.otherlives.core.diagnostics

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticsRepositoryTest {
    @Test
    fun journalKeepsOnlyMostRecentFiftyEntries() {
        val directory = createTempDir(prefix = "alt-diagnostics-")
        var now = 0L
        try {
            val repository = DiagnosticsRepository.forTest(
                directory = directory,
                clock = { ++now }
            )

            repeat(55) {
                repository.record(DiagnosticEvent.AI_CHAPTER_FAILED)
            }

            val entries = repository.entriesForTest()
            assertEquals(50, entries.size)
            assertEquals(6L, entries.first().timestampMillis)
            assertEquals(55L, entries.last().timestampMillis)
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun exportNeverIncludesExceptionMessageOrExternalData() {
        val directory = createTempDir(prefix = "alt-diagnostics-")
        try {
            val repository = DiagnosticsRepository.forTest(
                directory = directory,
                clock = { 1234L }
            )
            val error = IllegalStateException(
                "https://secret.example content://private/path prompt=do-not-copy"
            ).apply {
                stackTrace = arrayOf(
                    StackTraceElement(
                        "com.alt.otherlives.feature.timeline.GenerationRunner",
                        "generate",
                        "/data/user/0/private/File.kt",
                        42
                    )
                )
            }

            repository.record(
                event = DiagnosticEvent.AI_GENERATION_FAILED,
                error = error
            )

            val exported = repository.exportText()
            assertTrue(exported.contains("AI_GENERATION_FAILED"))
            assertTrue(exported.contains("IllegalStateException"))
            assertTrue(exported.contains("feature.timeline.GenerationRunner#generate"))
            assertFalse(exported.contains("secret.example"))
            assertFalse(exported.contains("content://"))
            assertFalse(exported.contains("do-not-copy"))
            assertFalse(exported.contains("/data/user"))
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun clearRemovesAllRecordedEntries() {
        val directory = createTempDir(prefix = "alt-diagnostics-")
        try {
            val repository = DiagnosticsRepository.forTest(
                directory = directory,
                clock = { 1L }
            )
            repository.record(DiagnosticEvent.PHOTO_IMPORT_FAILED)

            repository.clear()

            assertTrue(repository.entriesForTest().isEmpty())
        } finally {
            directory.deleteRecursively()
        }
    }
}

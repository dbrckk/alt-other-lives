package com.alt.otherlives.core.generation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ComfyUiWorkflowTest {
    @Test
    fun promptIncludesScenarioAndChapterWithoutProviderMarkup() {
        val prompt = ComfyUiWorkflow.promptFor(
            scenarioId = "japan",
            scenarioTitle = "What if I moved to Japan?",
            chapterLabel = "Year one",
            chapterNarrative = "A new city starts to feel like home.",
            chapterIndex = 0
        )

        assertTrue(prompt.contains("Japan"))
        assertTrue(prompt.contains("Year one"))
        assertTrue(prompt.contains("same person and facial identity"))
        assertTrue(prompt.contains("vertical 9:16"))
        assertFalse(prompt.contains("__ALT_"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun configRejectsNonHttpEndpoint() {
        ComfyUiConfig("ftp://example.test").validate()
    }
}

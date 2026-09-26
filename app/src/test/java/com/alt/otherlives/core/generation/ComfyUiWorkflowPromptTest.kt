package com.alt.otherlives.core.generation

import org.junit.Assert.assertTrue
import org.junit.Test

class ComfyUiWorkflowPromptTest {
    @Test
    fun futureScenarioUsesDedicatedNearFutureStyle() {
        val prompt = ComfyUiWorkflow.promptFor(
            scenarioId = "future",
            scenarioTitle = "What if I lived in 2100?",
            chapterLabel = "2100",
            chapterNarrative = "The future feels ordinary when you live inside it.",
            chapterIndex = 4
        )

        assertTrue(prompt.contains("credible near-future design"))
        assertTrue(prompt.contains("grounded science-fiction realism"))
    }
}

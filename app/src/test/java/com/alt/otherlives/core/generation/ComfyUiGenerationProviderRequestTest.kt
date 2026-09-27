package com.alt.otherlives.core.generation

import org.junit.Assert.assertEquals
import com.alt.otherlives.core.data.ScenarioCatalog
import org.junit.Assert.assertTrue
import org.junit.Test

class ComfyUiGenerationProviderRequestTest {
    @Test
    fun localizedScenarioUsesCanonicalEnglishPromptSource() {
        val frenchScenario = ScenarioCatalog.forLanguage("fr")
            .first { it.id == "japan" }

        val canonical = ComfyUiGenerationProvider.canonicalPromptScenario(
            frenchScenario
        )

        assertEquals("japan", canonical.id)
        assertEquals("What if I moved to Japan?", canonical.title)
        assertTrue(canonical.chapters.first().narrative.startsWith("You arrive"))
    }

    @Test
    fun nullRequestSelectsEveryChapter() {
        assertEquals(
            setOf(0, 1, 2, 3, 4),
            ComfyUiGenerationProvider.validateRequestedChapterIndexes(
                requested = null,
                chapterCount = 5
            )
        )
    }

    @Test
    fun validTargetedRequestIsPreserved() {
        assertEquals(
            setOf(1, 3),
            ComfyUiGenerationProvider.validateRequestedChapterIndexes(
                requested = setOf(1, 3),
                chapterCount = 5
            )
        )
    }

    @Test
    fun mixedValidAndInvalidRequestIsRejected() {
        val error = runCatching {
            ComfyUiGenerationProvider.validateRequestedChapterIndexes(
                requested = setOf(0, 5),
                chapterCount = 5
            )
        }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
        assertTrue(error?.message.orEmpty().contains("5"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun emptyTargetedRequestIsRejected() {
        ComfyUiGenerationProvider.validateRequestedChapterIndexes(
            requested = emptySet(),
            chapterCount = 5
        )
    }
}

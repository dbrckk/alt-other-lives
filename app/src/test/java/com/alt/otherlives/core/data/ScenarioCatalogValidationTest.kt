package com.alt.otherlives.core.data

import com.alt.otherlives.core.model.Scenario
import com.alt.otherlives.core.model.TimelineChapter
import com.alt.otherlives.core.model.TimelineConstraints
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScenarioCatalogValidationTest {
    private fun scenario(
        id: String = "valid",
        chapterCount: Int = 1
    ) = Scenario(
        id = id,
        title = "Title",
        subtitle = "Subtitle",
        chapters = List(chapterCount) { index ->
            TimelineChapter("Chapter $index", "Narrative $index")
        }
    )

    @Test
    fun frenchCatalogPreservesIdsAndChapterCounts() {
        val english = ScenarioCatalog.forLanguage("en")
        val french = ScenarioCatalog.forLanguage("fr")

        assertEquals(english.map { it.id }, french.map { it.id })
        assertEquals(
            english.map { it.chapters.size },
            french.map { it.chapters.size }
        )
        assertTrue(
            french.zip(english).all { (fr, en) ->
                fr.title != en.title || fr.subtitle != en.subtitle
            }
        )
    }

    @Test
    fun unsupportedLanguageFallsBackToEnglishCatalog() {
        assertEquals(
            ScenarioCatalog.scenarios,
            ScenarioCatalog.forLanguage("de")
        )
    }

    @Test
    fun acceptsStorageSafeScenario() {
        ScenarioCatalogValidation.validate(listOf(scenario()))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsReservedHistorySeparatorInScenarioId() {
        ScenarioCatalogValidation.validate(
            listOf(scenario(id = "bad|id"))
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsTraversalLikeScenarioId() {
        ScenarioCatalogValidation.validate(
            listOf(scenario(id = "bad..id"))
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsScenarioBeyondSupportedChapterLimit() {
        ScenarioCatalogValidation.validate(
            listOf(
                scenario(
                    chapterCount = TimelineConstraints.MAX_CHAPTER_COUNT + 1
                )
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsDuplicateScenarioIds() {
        ScenarioCatalogValidation.validate(
            listOf(scenario(id = "same"), scenario(id = "same"))
        )
    }
}

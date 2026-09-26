package com.alt.otherlives.core.data

import com.alt.otherlives.core.model.Scenario
import com.alt.otherlives.core.model.TimelineChapter
import com.alt.otherlives.core.model.TimelineConstraints
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

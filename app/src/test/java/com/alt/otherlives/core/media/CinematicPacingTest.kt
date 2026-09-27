package com.alt.otherlives.core.media

import com.alt.otherlives.core.model.Scenario
import com.alt.otherlives.core.model.TimelineChapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CinematicPacingTest {
    private val scenario = Scenario(
        id = "test",
        title = "Test",
        subtitle = "Test subtitle",
        chapters = listOf(
            TimelineChapter("1", "Short chapter."),
            TimelineChapter(
                "2",
                "This chapter has significantly more words so the viewer receives a little more reading time without making the social video feel slow."
            )
        )
    )

    @Test
    fun introAndOutroStayShort() {
        assertEquals(
            CinematicPacing.INTRO_DURATION_MS,
            CinematicPacing.sceneDurationMs(0, 4, scenario)
        )
        assertEquals(
            CinematicPacing.OUTRO_DURATION_MS,
            CinematicPacing.sceneDurationMs(3, 4, scenario)
        )
    }

    @Test
    fun denserChapterGetsMoreTimeWithinBounds() {
        val short = CinematicPacing.sceneDurationMs(1, 4, scenario)
        val long = CinematicPacing.sceneDurationMs(2, 4, scenario)

        assertTrue(long > short)
        assertTrue(short >= CinematicPacing.MIN_CHAPTER_DURATION_MS)
        assertTrue(long <= CinematicPacing.MAX_CHAPTER_DURATION_MS)
    }

    @Test
    fun fiveChapterStoryRemainsShortForm() {
        val fiveChapterScenario = scenario.copy(
            chapters = List(5) {
                TimelineChapter(
                    label = it.toString(),
                    narrative = "A cinematic chapter with enough detail to read comfortably on screen."
                )
            }
        )

        val total = CinematicPacing.totalDurationMs(
            sceneCount = 7,
            scenario = fiveChapterScenario
        )

        assertTrue(total in 12_000L..16_000L)
    }
}

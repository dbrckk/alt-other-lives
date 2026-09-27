package com.alt.otherlives.core.media

import com.alt.otherlives.core.model.Scenario

internal object CinematicPacing {
    const val INTRO_DURATION_MS = 1_600L
    const val OUTRO_DURATION_MS = 1_400L
    const val MIN_CHAPTER_DURATION_MS = 2_000L
    const val MAX_CHAPTER_DURATION_MS = 2_550L

    fun sceneDurationMs(
        sceneIndex: Int,
        sceneCount: Int,
        scenario: Scenario
    ): Long {
        require(sceneCount >= 2) { "Cinematic export requires intro and outro scenes" }
        require(sceneIndex in 0 until sceneCount) { "Scene index is out of bounds" }

        if (sceneIndex == 0) return INTRO_DURATION_MS
        if (sceneIndex == sceneCount - 1) return OUTRO_DURATION_MS

        val chapterIndex = sceneIndex - 1
        val narrative = scenario.chapters.getOrNull(chapterIndex)?.narrative.orEmpty()
        val words = narrative
            .trim()
            .split(Regex("\\s+"))
            .count { it.isNotBlank() }

        return (1_850L + words * 35L)
            .coerceIn(MIN_CHAPTER_DURATION_MS, MAX_CHAPTER_DURATION_MS)
    }

    fun totalDurationMs(
        sceneCount: Int,
        scenario: Scenario
    ): Long = (0 until sceneCount).sumOf { index ->
        sceneDurationMs(index, sceneCount, scenario)
    }
}

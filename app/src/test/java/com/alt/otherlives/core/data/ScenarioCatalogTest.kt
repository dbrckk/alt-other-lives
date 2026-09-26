package com.alt.otherlives.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import com.alt.otherlives.core.model.TimelineConstraints

class ScenarioCatalogTest {
    @Test
    fun scenarioIdsAreUniqueAndTimelinesAreComplete() {
        val scenarios = ScenarioCatalog.scenarios
        assertTrue(scenarios.size >= 8)
        assertEquals(scenarios.size, scenarios.map { it.id }.distinct().size)
        assertTrue(scenarios.all { it.id.isNotBlank() && it.title.isNotBlank() })
        assertTrue(
            scenarios.all {
                it.chapters.size in 4..TimelineConstraints.MAX_CHAPTER_COUNT
            }
        )
        assertTrue(scenarios.flatMap { it.chapters }.all { it.label.isNotBlank() && it.narrative.isNotBlank() })
    }
}

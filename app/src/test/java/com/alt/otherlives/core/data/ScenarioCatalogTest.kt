package com.alt.otherlives.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScenarioCatalogTest {
    @Test
    fun scenarioIdsAreUniqueAndTimelinesAreComplete() {
        val scenarios = ScenarioCatalog.scenarios
        assertTrue(scenarios.size >= 8)
        assertEquals(scenarios.size, scenarios.map { it.id }.distinct().size)
        assertTrue(scenarios.all { it.id.isNotBlank() && it.title.isNotBlank() })
        assertTrue(scenarios.all { it.chapters.size in 4..6 })
        assertTrue(scenarios.flatMap { it.chapters }.all { it.label.isNotBlank() && it.narrative.isNotBlank() })
    }
}

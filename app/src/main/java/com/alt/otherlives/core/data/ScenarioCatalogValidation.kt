package com.alt.otherlives.core.data

import com.alt.otherlives.core.generation.GeneratedTimelineKey
import com.alt.otherlives.core.model.Scenario
import com.alt.otherlives.core.model.TimelineConstraints

internal object ScenarioCatalogValidation {
    fun validate(scenarios: List<Scenario>): List<Scenario> {
        require(scenarios.isNotEmpty()) { "Scenario catalog must not be empty" }
        require(scenarios.map { it.id }.distinct().size == scenarios.size) {
            "Scenario IDs must be unique"
        }

        scenarios.forEach { scenario ->
            require(scenario.id.isNotBlank()) { "Scenario ID is required" }
            require(!scenario.id.contains('|') && !scenario.id.contains('\n') && !scenario.id.contains('\r')) {
                "Scenario ID contains reserved history characters"
            }
            GeneratedTimelineKey.validate(scenario.id + "-1")
            require(scenario.title.isNotBlank()) { "Scenario title is required" }
            require(scenario.subtitle.isNotBlank()) { "Scenario subtitle is required" }
            require(scenario.chapters.isNotEmpty()) { "Scenario must contain chapters" }
            require(scenario.chapters.size <= TimelineConstraints.MAX_CHAPTER_COUNT) {
                "Scenario exceeds supported chapter count"
            }
            require(
                scenario.chapters.all {
                    it.label.isNotBlank() && it.narrative.isNotBlank()
                }
            ) {
                "Scenario chapters require labels and narratives"
            }
        }

        return scenarios
    }
}

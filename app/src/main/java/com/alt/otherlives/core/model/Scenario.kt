package com.alt.otherlives.core.model

data class TimelineChapter(
    val label: String,
    val narrative: String
)

data class Scenario(
    val id: String,
    val title: String,
    val subtitle: String,
    val chapters: List<TimelineChapter>
)

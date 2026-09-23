package com.alt.otherlives.core.data

import com.alt.otherlives.core.model.Scenario
import com.alt.otherlives.core.model.TimelineChapter

object ScenarioCatalog {
    val scenarios = listOf(
        Scenario(
            id = "wealth",
            title = "What if I became wealthy?",
            subtitle = "A decade where one decision compounds.",
            chapters = listOf(
                TimelineChapter("2026", "You stop optimizing for comfort and start optimizing for leverage."),
                TimelineChapter("2028", "A small project becomes your first serious source of freedom."),
                TimelineChapter("2031", "You build systems that keep working while you sleep."),
                TimelineChapter("2034", "Money stops being the destination and becomes infrastructure."),
                TimelineChapter("2036", "Your life looks expensive from the outside — calm from the inside.")
            )
        ),
        Scenario(
            id = "japan",
            title = "What if I moved to Japan?",
            subtitle = "A new city, language and version of you.",
            chapters = listOf(
                TimelineChapter("2026", "You arrive with two bags and no familiar routine."),
                TimelineChapter("2027", "The city stops feeling foreign and starts feeling precise."),
                TimelineChapter("2029", "Your work, friends and habits become part of the place."),
                TimelineChapter("2032", "You realize you no longer translate your life in your head."),
                TimelineChapter("2036", "Home is no longer where you started.")
            )
        ),
        Scenario(
            id = "future",
            title = "What if I lived in 2100?",
            subtitle = "Your alternate life in a world that moved on.",
            chapters = listOf(
                TimelineChapter("2068", "Your identity becomes portable across physical and digital spaces."),
                TimelineChapter("2077", "Work is mostly direction, taste and judgment."),
                TimelineChapter("2086", "Cities feel quieter because infrastructure has become invisible."),
                TimelineChapter("2094", "Your oldest memories exist in forms younger generations can walk through."),
                TimelineChapter("2100", "The future feels ordinary when you live inside it.")
            )
        ),
        Scenario(
            id = "disappear",
            title = "What if I disappeared for 5 years?",
            subtitle = "No audience. No updates. Just change.",
            chapters = listOf(
                TimelineChapter("Year 1", "You remove the noise before you know what will replace it."),
                TimelineChapter("Year 2", "The new routine stops feeling temporary."),
                TimelineChapter("Year 3", "Your skills become visible before you do."),
                TimelineChapter("Year 4", "You stop measuring progress through other people."),
                TimelineChapter("Year 5", "You return with a life that no longer needs explaining.")
            )
        )
    )
}

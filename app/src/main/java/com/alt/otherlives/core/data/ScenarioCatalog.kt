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
        ),
        Scenario(
            id = "famous",
            title = "What if I became famous?",
            subtitle = "The version where everyone learns your name.",
            chapters = listOf(
                TimelineChapter("2026", "One piece of your work escapes your usual circle."),
                TimelineChapter("2027", "Recognition arrives faster than your routines can adapt."),
                TimelineChapter("2029", "You learn the difference between being visible and being known."),
                TimelineChapter("2032", "You build boundaries around the parts of life that still belong only to you."),
                TimelineChapter("2036", "Fame becomes background noise; the work becomes the point again.")
            )
        ),
        Scenario(
            id = "mars",
            title = "What if I lived on Mars?",
            subtitle = "A life measured in launch windows and red horizons.",
            chapters = listOf(
                TimelineChapter("2037", "Earth becomes a blue memory framed by a small window."),
                TimelineChapter("2038", "Every ordinary habit becomes part of keeping a settlement alive."),
                TimelineChapter("2041", "You stop thinking of the habitat as temporary."),
                TimelineChapter("2045", "A generation arrives that has never felt rain."),
                TimelineChapter("2050", "You watch two worlds in the sky and call one of them home.")
            )
        ),
        Scenario(
            id = "artist",
            title = "What if I became an artist?",
            subtitle = "A life built around making things that did not exist before.",
            chapters = listOf(
                TimelineChapter("2026", "You make something every day before deciding whether it is good."),
                TimelineChapter("2028", "Your style appears slowly, mostly through the choices you keep repeating."),
                TimelineChapter("2030", "Someone recognizes your work before seeing your name."),
                TimelineChapter("2033", "You stop waiting for inspiration and build a practice instead."),
                TimelineChapter("2036", "Your archive becomes a map of who you were becoming.")
            )
        ),
        Scenario(
            id = "restart",
            title = "What if I started over?",
            subtitle = "Same memories. Different decisions.",
            chapters = listOf(
                TimelineChapter("Day 1", "You keep the lessons and remove the obligations that no longer fit."),
                TimelineChapter("Month 6", "The empty space begins filling with deliberate choices."),
                TimelineChapter("Year 2", "Your new life stops feeling like an escape."),
                TimelineChapter("Year 5", "The decisions that once felt radical now look obvious."),
                TimelineChapter("Year 10", "You barely recognize the path you once thought was fixed.")
            )
        )
    )
}

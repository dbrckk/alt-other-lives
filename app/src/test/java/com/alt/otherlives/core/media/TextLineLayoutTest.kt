package com.alt.otherlives.core.media

import com.alt.otherlives.core.data.ScenarioCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TextLineLayoutTest {
    private val monospaceMeasure: (String) -> Float = { it.length.toFloat() }

    @Test
    fun capsFrenchTextToRequestedLineBudget() {
        val lines = TextLineLayout.layout(
            text = "Une nouvelle ville, une nouvelle langue, une nouvelle version de toi.",
            maxWidth = 24f,
            maxLines = 2,
            measure = monospaceMeasure
        )

        assertEquals(2, lines.size)
        assertTrue(lines.last().endsWith("…"))
        assertTrue(lines.all { it.length <= 24 })
    }

    @Test
    fun ellipsizesSingleOversizedToken() {
        val lines = TextLineLayout.layout(
            text = "anticonstitutionnellementextraordinaire",
            maxWidth = 12f,
            maxLines = 1,
            measure = monospaceMeasure
        )

        assertEquals(1, lines.size)
        assertTrue(lines.single().endsWith("…"))
        assertTrue(lines.single().length <= 12)
    }

    @Test
    fun frenchCatalogFitsConfiguredSocialLineBudgets() {
        val french = ScenarioCatalog.forLanguage("fr")

        french.forEach { scenario ->
            val titleLines = TextLineLayout.layout(
                text = scenario.title,
                maxWidth = 28f,
                maxLines = 3,
                measure = monospaceMeasure
            )
            val subtitleLines = TextLineLayout.layout(
                text = scenario.subtitle,
                maxWidth = 38f,
                maxLines = 3,
                measure = monospaceMeasure
            )

            assertTrue(titleLines.size <= 3)
            assertTrue(subtitleLines.size <= 3)
            assertTrue(titleLines.all { it.length <= 28 })
            assertTrue(subtitleLines.all { it.length <= 38 })

            scenario.chapters.forEach { chapter ->
                val narrativeLines = TextLineLayout.layout(
                    text = chapter.narrative,
                    maxWidth = 42f,
                    maxLines = 7,
                    measure = monospaceMeasure
                )
                assertTrue(narrativeLines.size <= 7)
                assertTrue(narrativeLines.all { it.length <= 42 })
            }
        }
    }

    @Test
    fun preservesShortTextWithoutEllipsis() {
        val lines = TextLineLayout.layout(
            text = "ALT life",
            maxWidth = 20f,
            maxLines = 2,
            measure = monospaceMeasure
        )

        assertEquals(listOf("ALT life"), lines)
    }
}

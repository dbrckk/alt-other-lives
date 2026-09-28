package com.alt.otherlives.feature.timeline

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RevealResponsiveLayoutTest {
    @Test
    fun compactViewportUsesShorterHeroAndTitle() {
        val layout = RevealResponsiveLayout.resolve(
            screenHeightDp = 640,
            fontScale = 1.0f
        )

        assertTrue(layout.heroHeightDp < 620)
        assertTrue(layout.titleSizeSp < 42)
    }

    @Test
    fun tallViewportKeepsCinematicHero() {
        val layout = RevealResponsiveLayout.resolve(
            screenHeightDp = 900,
            fontScale = 1.0f
        )

        assertEquals(620, layout.heroHeightDp)
        assertEquals(42, layout.titleSizeSp)
    }

    @Test
    fun largeTextReducesHeroAndTitleToProtectContent() {
        val layout = RevealResponsiveLayout.resolve(
            screenHeightDp = 800,
            fontScale = 1.45f
        )

        assertTrue(layout.heroHeightDp <= 540)
        assertTrue(layout.titleSizeSp <= 36)
        assertTrue(layout.titleLineHeightSp >= layout.titleSizeSp)
    }
}

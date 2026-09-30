package com.alt.otherlives.feature.scenarios

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScenarioResponsiveLayoutTest {
    @Test
    fun standardScreenPreservesEditorialCatalog() {
        val layout = ScenarioResponsiveLayout.resolve(
            screenHeightDp = 800,
            fontScale = 1f
        )

        assertEquals(42, layout.topPaddingDp)
        assertEquals(34, layout.headingSizeSp)
        assertEquals(22, layout.cardPaddingDp)
        assertEquals(14, layout.cardSpacingDp)
    }

    @Test
    fun compactScreenReducesChromeNotStoryText() {
        val layout = ScenarioResponsiveLayout.resolve(
            screenHeightDp = 650,
            fontScale = 1f
        )

        assertTrue(layout.topPaddingDp < 42)
        assertTrue(layout.headingSizeSp < 34)
        assertTrue(layout.cardPaddingDp < 22)
        assertTrue(layout.cardSpacingDp < 14)
    }

    @Test
    fun largeFontScaleUsesDenserChrome() {
        val layout = ScenarioResponsiveLayout.resolve(
            screenHeightDp = 800,
            fontScale = 1.4f
        )

        assertTrue(layout.topPaddingDp <= 28)
        assertTrue(layout.headingSizeSp <= 30)
        assertTrue(layout.cardPaddingDp <= 20)
    }

    @Test
    fun compactLargeTextUsesMostConservativeLayout() {
        val layout = ScenarioResponsiveLayout.resolve(
            screenHeightDp = 620,
            fontScale = 1.5f
        )

        assertTrue(layout.topPaddingDp <= 20)
        assertTrue(layout.headingSizeSp <= 28)
        assertTrue(layout.cardPaddingDp <= 18)
        assertTrue(layout.cardSpacingDp <= 10)
    }
}

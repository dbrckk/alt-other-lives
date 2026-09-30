package com.alt.otherlives.feature.history

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryResponsiveLayoutTest {
    @Test
    fun standardScreenPreservesPremiumLibrary() {
        val layout = HistoryResponsiveLayout.resolve(
            screenHeightDp = 800,
            fontScale = 1f
        )

        assertEquals(42, layout.topPaddingDp)
        assertEquals(31, layout.titleSizeSp)
        assertEquals(210, layout.previewHeightDp)
        assertEquals(18, layout.cardPaddingDp)
        assertEquals(12, layout.cardSpacingDp)
    }

    @Test
    fun compactScreenShowsMoreSavedLivesWithoutShrinkingStoryText() {
        val layout = HistoryResponsiveLayout.resolve(
            screenHeightDp = 650,
            fontScale = 1f
        )

        assertTrue(layout.topPaddingDp < 42)
        assertTrue(layout.titleSizeSp < 31)
        assertTrue(layout.previewHeightDp < 210)
        assertTrue(layout.cardPaddingDp < 18)
    }

    @Test
    fun largeFontScaleUsesDenserLibraryChrome() {
        val layout = HistoryResponsiveLayout.resolve(
            screenHeightDp = 800,
            fontScale = 1.4f
        )

        assertTrue(layout.topPaddingDp <= 28)
        assertTrue(layout.titleSizeSp <= 29)
        assertTrue(layout.previewHeightDp <= 180)
        assertTrue(layout.cardPaddingDp <= 16)
    }

    @Test
    fun compactLargeTextUsesMostConservativeLibraryLayout() {
        val layout = HistoryResponsiveLayout.resolve(
            screenHeightDp = 620,
            fontScale = 1.5f
        )

        assertTrue(layout.topPaddingDp <= 20)
        assertTrue(layout.titleSizeSp <= 27)
        assertTrue(layout.previewHeightDp <= 160)
        assertTrue(layout.cardPaddingDp <= 14)
        assertTrue(layout.cardSpacingDp <= 10)
    }
}

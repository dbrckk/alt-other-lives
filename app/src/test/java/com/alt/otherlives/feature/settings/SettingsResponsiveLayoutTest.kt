package com.alt.otherlives.feature.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsResponsiveLayoutTest {
    @Test
    fun standardScreenUsesPremiumDeveloperLayout() {
        val layout = SettingsResponsiveLayout.resolve(
            screenHeightDp = 800,
            fontScale = 1f
        )

        assertEquals(24, layout.contentPaddingDp)
        assertEquals(28, layout.titleSizeSp)
        assertEquals(12, layout.workflowMinLines)
        assertEquals(20, layout.sectionSpacingDp)
    }

    @Test
    fun compactScreenReducesVerticalChrome() {
        val layout = SettingsResponsiveLayout.resolve(
            screenHeightDp = 650,
            fontScale = 1f
        )

        assertTrue(layout.contentPaddingDp < 24)
        assertTrue(layout.titleSizeSp < 28)
        assertTrue(layout.workflowMinLines < 12)
        assertTrue(layout.sectionSpacingDp < 20)
    }

    @Test
    fun largeFontScalePrioritizesReadableViewport() {
        val layout = SettingsResponsiveLayout.resolve(
            screenHeightDp = 800,
            fontScale = 1.4f
        )

        assertTrue(layout.contentPaddingDp <= 20)
        assertTrue(layout.titleSizeSp <= 26)
        assertTrue(layout.workflowMinLines <= 9)
        assertTrue(layout.sectionSpacingDp <= 16)
    }

    @Test
    fun compactLargeTextUsesMostConservativeFormDensity() {
        val layout = SettingsResponsiveLayout.resolve(
            screenHeightDp = 620,
            fontScale = 1.5f
        )

        assertTrue(layout.contentPaddingDp <= 16)
        assertTrue(layout.titleSizeSp <= 24)
        assertTrue(layout.workflowMinLines <= 7)
        assertTrue(layout.sectionSpacingDp <= 14)
    }
}

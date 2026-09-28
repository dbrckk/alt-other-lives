package com.alt.otherlives.feature.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeResponsiveLayoutTest {
    @Test
    fun standardScreenPreservesPremiumComposition() {
        val layout = HomeResponsiveLayout.resolve(
            screenHeightDp = 800,
            fontScale = 1f
        )

        assertEquals(350, layout.photoHeroHeightDp)
        assertEquals(44, layout.headlineSizeSp)
        assertEquals(45, layout.headlineLineHeightSp)
    }

    @Test
    fun compactScreenKeepsPhotoAndCtaCloserToViewport() {
        val layout = HomeResponsiveLayout.resolve(
            screenHeightDp = 650,
            fontScale = 1f
        )

        assertTrue(layout.photoHeroHeightDp < 350)
        assertTrue(layout.headlineSizeSp < 44)
        assertTrue(layout.headlineLineHeightSp < 45)
    }

    @Test
    fun largeFontScaleReducesHeroWithoutShrinkingBodyAccessibility() {
        val layout = HomeResponsiveLayout.resolve(
            screenHeightDp = 800,
            fontScale = 1.4f
        )

        assertTrue(layout.photoHeroHeightDp <= 300)
        assertTrue(layout.headlineSizeSp <= 38)
        assertTrue(layout.headlineLineHeightSp <= 41)
    }

    @Test
    fun compactLargeTextUsesMostConservativePremiumLayout() {
        val layout = HomeResponsiveLayout.resolve(
            screenHeightDp = 620,
            fontScale = 1.5f
        )

        assertTrue(layout.photoHeroHeightDp <= 280)
        assertTrue(layout.headlineSizeSp <= 36)
    }
}

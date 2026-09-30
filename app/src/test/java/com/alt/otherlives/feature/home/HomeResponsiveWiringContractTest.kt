package com.alt.otherlives.feature.home

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeResponsiveWiringContractTest {
    @Test
    fun homeScreenUsesResolvedResponsiveMetrics() {
        val source = File(
            "src/main/java/com/alt/otherlives/feature/home/HomeScreen.kt"
        ).readText()

        assertTrue(source.contains("LocalConfiguration.current"))
        assertTrue(source.contains("LocalDensity.current.fontScale"))
        assertTrue(source.contains("HomeResponsiveLayout.resolve("))
        assertTrue(source.contains("height(homeLayout.photoHeroHeightDp.dp)"))
        assertTrue(source.contains("fontSize = homeLayout.headlineSizeSp.sp"))
        assertTrue(source.contains("lineHeight = homeLayout.headlineLineHeightSp.sp"))

        assertFalse(source.contains("height(350.dp)"))
        assertFalse(source.contains("fontSize = 44.sp"))
        assertFalse(source.contains("lineHeight = 45.sp"))
    }
}

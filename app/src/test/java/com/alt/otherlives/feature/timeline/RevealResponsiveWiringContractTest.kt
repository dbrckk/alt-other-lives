package com.alt.otherlives.feature.timeline

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RevealResponsiveWiringContractTest {
    @Test
    fun revealScreenUsesResolvedResponsiveHeroMetrics() {
        val source = File(
            "src/main/java/com/alt/otherlives/feature/timeline/RevealScreen.kt"
        ).readText()

        assertTrue(source.contains("RevealResponsiveLayout.resolve("))
        assertTrue(source.contains("height(revealLayout.heroHeightDp.dp)"))
        assertTrue(source.contains("fontSize = revealLayout.titleSizeSp.sp"))
        assertTrue(source.contains("lineHeight = revealLayout.titleLineHeightSp.sp"))
        assertFalse(source.contains("height(620.dp)"))
        assertFalse(source.contains("fontSize = 42.sp"))
        assertFalse(source.contains("lineHeight = 44.sp"))
    }
}

package com.alt.otherlives.feature.scenarios

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScenarioResponsiveWiringContractTest {
    @Test
    fun scenarioScreenUsesResolvedResponsiveChrome() {
        val source = File(
            "src/main/java/com/alt/otherlives/feature/scenarios/ScenarioScreen.kt"
        ).readText()

        assertTrue(source.contains("LocalConfiguration.current"))
        assertTrue(source.contains("LocalDensity.current.fontScale"))
        assertTrue(source.contains("ScenarioResponsiveLayout.resolve("))
        assertTrue(source.contains("padding(top = scenarioLayout.topPaddingDp.dp)"))
        assertTrue(source.contains("fontSize = scenarioLayout.headingSizeSp.sp"))
        assertTrue(source.contains("Arrangement.spacedBy(scenarioLayout.cardSpacingDp.dp)"))
        assertTrue(source.contains("Column(Modifier.padding(scenarioLayout.cardPaddingDp.dp))"))

        assertFalse(source.contains("padding(top = 42.dp)"))
        assertFalse(source.contains("fontSize = 34.sp"))
        assertFalse(source.contains("Arrangement.spacedBy(14.dp)"))
        assertFalse(source.contains("Column(Modifier.padding(22.dp))"))
    }
}

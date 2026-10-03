package com.alt.otherlives.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressAccessibilityContractTest {
    @Test
    fun scenarioCreationProgressHasAccessibleState() {
        val source = File(
            "src/main/java/com/alt/otherlives/feature/scenarios/ScenarioScreen.kt"
        ).readText()

        assertTrue(source.contains("ProgressBarRangeInfo.Indeterminate"))
        assertTrue(source.contains("progressBarRangeInfo"))
        assertTrue(source.contains("stateDescription"))
    }

    @Test
    fun revealGenerationAndExportProgressHaveAccessibleState() {
        val source = File(
            "src/main/java/com/alt/otherlives/feature/timeline/RevealScreen.kt"
        ).readText()

        assertTrue(source.contains("ProgressBarRangeInfo"))
        assertTrue(source.contains("stateDescription"))
        assertTrue(source.contains("progressBarRangeInfo"))
        assertTrue(source.contains("0f..100f"))
    }
}

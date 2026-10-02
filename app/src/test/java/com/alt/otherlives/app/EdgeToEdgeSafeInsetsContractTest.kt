package com.alt.otherlives.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class EdgeToEdgeSafeInsetsContractTest {
    private val screens = listOf(
        "src/main/java/com/alt/otherlives/feature/home/HomeScreen.kt",
        "src/main/java/com/alt/otherlives/feature/scenarios/ScenarioScreen.kt",
        "src/main/java/com/alt/otherlives/feature/history/HistoryScreen.kt",
        "src/main/java/com/alt/otherlives/feature/timeline/RevealScreen.kt",
        "src/main/java/com/alt/otherlives/feature/settings/GenerationSettingsScreen.kt"
    )

    @Test
    fun everyTopLevelScreenRespectsSafeDrawingInsets() {
        screens.forEach { path ->
            val source = File(path).readText()
            assertTrue(
                "$path must apply safeDrawingPadding() when edge-to-edge is enabled",
                source.contains(".safeDrawingPadding()")
            )
        }
    }
}

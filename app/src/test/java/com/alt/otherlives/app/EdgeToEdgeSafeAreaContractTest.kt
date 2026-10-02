package com.alt.otherlives.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class EdgeToEdgeSafeAreaContractTest {
    @Test
    fun allPrimaryScreensRespectSafeDrawingInsets() {
        val screens = listOf(
            "src/main/java/com/alt/otherlives/feature/home/HomeScreen.kt",
            "src/main/java/com/alt/otherlives/feature/scenarios/ScenarioScreen.kt",
            "src/main/java/com/alt/otherlives/feature/history/HistoryScreen.kt",
            "src/main/java/com/alt/otherlives/feature/settings/GenerationSettingsScreen.kt",
            "src/main/java/com/alt/otherlives/feature/timeline/RevealScreen.kt"
        )

        screens.forEach { path ->
            val source = File(path).readText()
            assertTrue(
                "$path must apply safeDrawingPadding() for edge-to-edge",
                source.contains("safeDrawingPadding()")
            )
        }
    }
}

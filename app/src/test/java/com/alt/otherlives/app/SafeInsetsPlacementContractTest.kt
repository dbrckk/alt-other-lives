package com.alt.otherlives.app

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SafeInsetsPlacementContractTest {
    private val roots = mapOf(
        "src/main/java/com/alt/otherlives/feature/home/HomeScreen.kt" to ".fillMaxSize()\n            .safeDrawingPadding()",
        "src/main/java/com/alt/otherlives/feature/scenarios/ScenarioScreen.kt" to ".fillMaxSize()\n            .safeDrawingPadding()",
        "src/main/java/com/alt/otherlives/feature/history/HistoryScreen.kt" to ".fillMaxSize()\n            .safeDrawingPadding()",
        "src/main/java/com/alt/otherlives/feature/timeline/RevealScreen.kt" to ".fillMaxSize()\n            .safeDrawingPadding()",
        "src/main/java/com/alt/otherlives/feature/settings/GenerationSettingsScreen.kt" to ".fillMaxSize()\n            .safeDrawingPadding()"
    )

    @Test
    fun safeDrawingInsetsAreAppliedExactlyOnceAtEachScreenRoot() {
        roots.forEach { (path, rootPattern) ->
            val source = File(path).readText()
            assertTrue("$path must apply safeDrawingPadding at its root", source.contains(rootPattern))
            assertEquals(
                "$path must not apply safeDrawingPadding to internal overlays",
                1,
                source.windowed("safeDrawingPadding()".length)
                    .count { it == "safeDrawingPadding()" }
            )
        }
    }
}

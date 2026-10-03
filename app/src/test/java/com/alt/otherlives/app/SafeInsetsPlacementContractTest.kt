package com.alt.otherlives.app

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SafeInsetsPlacementContractTest {
    @Test
    fun safeDrawingInsetsAreAppliedExactlyOnceAtEachScreenRoot() {
        val rootPatterns = mapOf(
            "src/main/java/com/alt/otherlives/feature/home/HomeScreen.kt" to
                ".fillMaxSize()\n            .safeDrawingPadding()\n            .verticalScroll",
            "src/main/java/com/alt/otherlives/feature/scenarios/ScenarioScreen.kt" to
                ".fillMaxSize()\n            .safeDrawingPadding()\n            .padding(top = scenarioLayout.topPaddingDp.dp)",
            "src/main/java/com/alt/otherlives/feature/history/HistoryScreen.kt" to
                "Column(\n        modifier = Modifier\n            .fillMaxSize()\n            .safeDrawingPadding()\n            .padding(top = historyLayout.topPaddingDp.dp)",
            "src/main/java/com/alt/otherlives/feature/timeline/RevealScreen.kt" to
                "LazyColumn(\n        modifier = Modifier\n            .fillMaxSize()\n            .safeDrawingPadding(),",
            "src/main/java/com/alt/otherlives/feature/settings/GenerationSettingsScreen.kt" to
                ".fillMaxSize()\n            .safeDrawingPadding()\n            .imePadding()"
        )

        rootPatterns.forEach { (path, rootPattern) ->
            val source = File(path).readText()
            assertTrue("$path must apply safeDrawingPadding at its screen root", source.contains(rootPattern))
            assertEquals(
                "$path must not apply safeDrawingPadding to internal overlays",
                1,
                Regex("""\.safeDrawingPadding\(\)""").findAll(source).count()
            )
        }
    }
}

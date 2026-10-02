package com.alt.otherlives.feature.history

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistorySafeInsetWiringContractTest {
    @Test
    fun historyAppliesSafeDrawingInsetToRootNotMediaOverlay() {
        val source = File(
            "src/main/java/com/alt/otherlives/feature/history/HistoryScreen.kt"
        ).readText()

        assertTrue(
            source.contains(
                "Column(\n        Modifier\n            .fillMaxSize()\n            .safeDrawingPadding()"
            )
        )
        assertFalse(
            source.contains(
                ".fillMaxSize()\n            .safeDrawingPadding()\n                                                .background("
            )
        )
    }
}

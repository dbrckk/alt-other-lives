package com.alt.otherlives.feature.history

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryResponsiveWiringContractTest {
    @Test
    fun historyScreenUsesResolvedResponsiveLibraryMetrics() {
        val source = File(
            "src/main/java/com/alt/otherlives/feature/history/HistoryScreen.kt"
        ).readText()

        assertTrue(source.contains("LocalConfiguration.current"))
        assertTrue(source.contains("LocalDensity.current.fontScale"))
        assertTrue(source.contains("HistoryResponsiveLayout.resolve("))
        assertTrue(source.contains("padding(top = historyLayout.topPaddingDp.dp)"))
        assertTrue(source.contains("fontSize = historyLayout.titleSizeSp.sp"))
        assertTrue(source.contains("height(historyLayout.previewHeightDp.dp)"))
        assertTrue(source.contains("Arrangement.spacedBy(historyLayout.cardSpacingDp.dp)"))
        assertTrue(source.contains("Column(Modifier.padding(historyLayout.cardPaddingDp.dp))"))

        assertFalse(source.contains("padding(top = 42.dp)"))
        assertFalse(source.contains("fontSize = 31.sp"))
        assertFalse(source.contains("height(210.dp)"))
        assertFalse(source.contains("Arrangement.spacedBy(12.dp)"))
        assertFalse(source.contains("Column(Modifier.padding(18.dp))"))
    }
}

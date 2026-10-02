package com.alt.otherlives.feature.history

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class HistorySafeAreaContractTest {
    @Test
    fun historyContentRespectsSafeDrawingInsets() {
        val source = File(
            "src/main/java/com/alt/otherlives/feature/history/HistoryScreen.kt"
        ).readText()

        assertTrue(source.contains(".safeDrawingPadding()"))
        assertTrue(source.contains("Column("))
        assertTrue(source.contains(".fillMaxSize()"))
    }
}

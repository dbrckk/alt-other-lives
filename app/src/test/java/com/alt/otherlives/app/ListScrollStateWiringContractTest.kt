package com.alt.otherlives.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ListScrollStateWiringContractTest {
    @Test
    fun appHoistsScenarioAndHistoryListStates() {
        val appSource = File(
            "src/main/java/com/alt/otherlives/app/AltApp.kt"
        ).readText()
        val scenarioSource = File(
            "src/main/java/com/alt/otherlives/feature/scenarios/ScenarioScreen.kt"
        ).readText()
        val historySource = File(
            "src/main/java/com/alt/otherlives/feature/history/HistoryScreen.kt"
        ).readText()

        assertTrue(appSource.contains("rememberSaveable(saver = LazyListState.Saver)"))
        assertTrue(appSource.contains("scenarioListState"))
        assertTrue(appSource.contains("historyListState"))
        assertTrue(appSource.contains("listState = scenarioListState"))
        assertTrue(appSource.contains("listState = historyListState"))

        assertTrue(scenarioSource.contains("listState: LazyListState"))
        assertTrue(scenarioSource.contains("state = listState"))

        assertTrue(historySource.contains("listState: LazyListState"))
        assertTrue(historySource.contains("state = listState"))
    }
}

package com.alt.otherlives.feature.history

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.alt.otherlives.core.data.HistoryEntry
import com.alt.otherlives.core.data.ScenarioCatalog
import com.alt.otherlives.core.designsystem.AltTheme
import org.junit.Rule
import org.junit.Test

class HistoryScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun historyWithTwoLivesOffersComparisonMode() {
        val first = ScenarioCatalog.scenarios[0]
        val second = ScenarioCatalog.scenarios[1]

        composeRule.setContent {
            AltTheme {
                HistoryScreen(
                    entries = listOf(
                        HistoryEntry(first.id, 1L),
                        HistoryEntry(second.id, 2L)
                    ),
                    scenarios = ScenarioCatalog.scenarios,
                    onBack = {},
                    onOpen = { _, _ -> },
                    onDelete = {},
                    onClear = {}
                )
            }
        }

        composeRule.onNodeWithText("Compare two lives")
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithText("0/2 selected")
            .assertIsDisplayed()
    }

    @Test
    fun emptyHistoryShowsPrivateEmptyState() {
        composeRule.setContent {
            AltTheme {
                HistoryScreen(
                    entries = emptyList(),
                    scenarios = ScenarioCatalog.scenarios,
                    onBack = {},
                    onOpen = { _, _ -> },
                    onDelete = {},
                    onClear = {}
                )
            }
        }

        composeRule.onNodeWithText("Your other lives")
            .assertIsDisplayed()
        composeRule.onNodeWithText("No alternate lives yet.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Stored privately on this device")
            .assertIsDisplayed()
    }
}

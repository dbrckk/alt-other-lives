package com.alt.otherlives.feature.history

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.alt.otherlives.core.data.ScenarioCatalog
import com.alt.otherlives.core.designsystem.AltTheme
import org.junit.Rule
import org.junit.Test

class HistoryScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

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

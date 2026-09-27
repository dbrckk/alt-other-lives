package com.alt.otherlives.feature.scenarios

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.alt.otherlives.core.data.ScenarioCatalog
import com.alt.otherlives.core.designsystem.AltTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ScenarioScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun scenarioScreenShowsCatalogAndDispatchesSelection() {
        var selectedScenarioId: String? = null

        composeRule.setContent {
            AltTheme {
                ScenarioScreen(
                    scenarios = ScenarioCatalog.scenarios,
                    onBack = {},
                    onSelect = { selectedScenarioId = it.id },
                    isCreatingTimeline = false
                )
            }
        }

        composeRule.onNodeWithText("What if…")
            .assertIsDisplayed()
        composeRule.onNodeWithText("What if I became wealthy?")
            .assertIsDisplayed()
            .performClick()

        composeRule.runOnIdle {
            assertEquals("wealth", selectedScenarioId)
        }
    }
}

package com.alt.otherlives.feature.timeline

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.alt.otherlives.core.data.ScenarioCatalog
import com.alt.otherlives.core.designsystem.AltTheme
import com.alt.otherlives.core.generation.GenerationSettings
import org.junit.Rule
import org.junit.Test

class RevealScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun revealShowsEditorialTimelineSummary() {
        val scenario = ScenarioCatalog.scenarios.first()

        composeRule.setContent {
            AltTheme {
                RevealScreen(
                    photoUri = null,
                    scenario = scenario,
                    generationSettings = GenerationSettings(),
                    timelineKey = "instrumentation-reveal-summary",
                    onBack = {},
                    onAiSettings = {},
                    onCreateAnotherLife = {},
                    onRemixThisLife = {}
                )
            }
        }

        composeRule.onNodeWithText(scenario.title)
            .assertIsDisplayed()
        composeRule.onNodeWithText("5 CHAPTERS")
            .assertIsDisplayed()
        composeRule.onNodeWithText("0/5 AI SCENES")
            .assertIsDisplayed()
    }

}

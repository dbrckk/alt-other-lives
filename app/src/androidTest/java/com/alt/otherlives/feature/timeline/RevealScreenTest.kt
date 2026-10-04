package com.alt.otherlives.feature.timeline

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.alt.otherlives.core.data.ScenarioCatalog
import com.alt.otherlives.core.designsystem.AltTheme
import com.alt.otherlives.core.generation.GenerationSettings
import org.junit.Assert.assertTrue
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

    @Test
    fun revealWithoutConfiguredAiOffersSettingsAction() {
        val scenario = ScenarioCatalog.scenarios.first()
        var openedSettings = false

        composeRule.setContent {
            AltTheme {
                RevealScreen(
                    photoUri = null,
                    scenario = scenario,
                    generationSettings = GenerationSettings(),
                    timelineKey = "instrumentation-reveal-settings",
                    onBack = {},
                    onAiSettings = { openedSettings = true },
                    onCreateAnotherLife = {},
                    onRemixThisLife = {}
                )
            }
        }

        composeRule.onNodeWithText(
            "AI generation needs the original source photo. Saved generated scenes can still be viewed and exported."
        ).fetchSemanticsNode()

        composeRule.onNodeWithText("Set up AI generation")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(openedSettings)
        }
    }
}

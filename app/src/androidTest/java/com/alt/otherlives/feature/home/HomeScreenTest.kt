package com.alt.otherlives.feature.home

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import android.net.Uri
import com.alt.otherlives.core.designsystem.AltTheme
import com.alt.otherlives.core.media.SourcePhotoQualityIssue
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun homeShowsLowQualitySourceStatusWithoutBlockingFlow() {
        composeRule.setContent {
            AltTheme {
                HomeScreen(
                    photoUri = Uri.parse("content://alt/test-photo"),
                    photoIsLikelyPremiumSource = false,
                    isImportingPhoto = false,
                    onPhotoSelected = {},
                    onContinue = {},
                    onHistory = {}
                )
            }
        }

        composeRule.onNodeWithText("SOURCE COULD BE SHARPER")
            .assertIsDisplayed()
        composeRule.onNodeWithText(
            "For stronger identity continuity, use a portrait at least 720 px on the short edge."
        ).assertIsDisplayed()
        composeRule.onNodeWithText("Choose another life")
            .assertIsDisplayed()
    }

    @Test
    fun homeShowsFramingGuidanceForExtremeAspectRatio() {
        composeRule.setContent {
            AltTheme {
                HomeScreen(
                    photoUri = Uri.parse("content://alt/panoramic-photo"),
                    photoIsLikelyPremiumSource = false,
                    photoQualityIssues = setOf(SourcePhotoQualityIssue.EXTREME_ASPECT_RATIO),
                    isImportingPhoto = false,
                    onPhotoSelected = {},
                    onContinue = {},
                    onHistory = {}
                )
            }
        }

        composeRule.onNodeWithText(
            "For better face framing, use a less panoramic photo with your face closer to the center."
        ).assertIsDisplayed()
    }

    @Test
    fun homeShowsConsumerFlowWithoutDeveloperAiSettings() {
        composeRule.setContent {
            AltTheme {
                HomeScreen(
                    photoUri = null,
                    photoIsLikelyPremiumSource = null,
                    isImportingPhoto = false,
                    onPhotoSelected = {},
                    onContinue = {},
                    onHistory = {}
                )
            }
        }

        composeRule.onNodeWithText("Choose your photo")
            .assertIsDisplayed()
        composeRule.onNodeWithText("View my other lives")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("AI generation settings")
            .assertCountEquals(0)
    }
}

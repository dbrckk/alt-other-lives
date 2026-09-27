package com.alt.otherlives.feature.home

import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.alt.otherlives.core.designsystem.AltTheme
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun homeShowsConsumerFlowWithoutDeveloperAiSettings() {
        composeRule.setContent {
            AltTheme {
                HomeScreen(
                    photoUri = null,
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
        composeRule.onNodeWithText("AI generation settings")
            .assertDoesNotExist()
    }
}

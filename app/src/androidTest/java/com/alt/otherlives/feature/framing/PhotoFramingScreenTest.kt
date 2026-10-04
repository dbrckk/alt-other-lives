package com.alt.otherlives.feature.framing

import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.alt.otherlives.core.designsystem.AltTheme
import com.alt.otherlives.core.media.SourcePhotoQualityIssue
import com.alt.otherlives.core.media.NormalizedCropRect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PhotoFramingScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun resetFramingEmitsFullCrop() {
        var changedCrop: NormalizedCropRect? = null

        composeRule.setContent {
            AltTheme {
                PhotoFramingScreen(
                    photoUri = Uri.parse("content://alt/framing-photo"),
                    qualityIssues = emptySet(),
                    crop = NormalizedCropRect.centered(0.5f, 0.5f),
                    onCropChange = { changedCrop = it },
                    onBack = {},
                    onConfirm = {}
                )
            }
        }

        composeRule.onNodeWithText("Reset framing")
            .performClick()

        composeRule.runOnIdle {
            assertEquals(NormalizedCropRect.Full, changedCrop)
        }
    }

    @Test
    fun panoramaSourceShowsGuidanceAndConfirms() {
        var confirmed = false

        composeRule.setContent {
            AltTheme {
                PhotoFramingScreen(
                    photoUri = Uri.parse("content://alt/framing-photo"),
                    qualityIssues = setOf(SourcePhotoQualityIssue.EXTREME_ASPECT_RATIO),
                    onBack = {},
                    onConfirm = { confirmed = true }
                )
            }
        }

        composeRule.onNodeWithText("Frame your face")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Keep your face inside this guide")
            .assertIsDisplayed()
        composeRule.onNodeWithText(
            "This source is very wide. A closer portrait will usually preserve your identity more reliably."
        ).assertIsDisplayed()
        composeRule.onNodeWithText("Looks good")
            .assertIsDisplayed()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(confirmed)
        }
    }
}

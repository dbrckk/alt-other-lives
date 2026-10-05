package com.alt.otherlives.feature.framing

import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
    fun resetFramingEmitsPortraitBaseCrop() {
        var changedCrop: NormalizedCropRect? = null

        val baseCrop = NormalizedCropRect.centered(width = 0.4f, height = 1f)

        composeRule.setContent {
            AltTheme {
                PhotoFramingScreen(
                    photoUri = Uri.parse("content://alt/framing-photo"),
                    qualityIssues = emptySet(),
                    crop = NormalizedCropRect.centered(0.2f, 0.5f),
                    baseCrop = baseCrop,
                    onCropChange = { changedCrop = it },
                    onBack = {},
                    onConfirm = {}
                )
            }
        }

        composeRule.onNodeWithText("Reset framing")
            .performScrollTo()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(baseCrop, changedCrop)
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
        composeRule.onNodeWithText("Pinch to zoom • drag to reposition")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText(
            "This source is very wide. A closer portrait will usually preserve your identity more reliably."
        )
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("Looks good")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(confirmed)
        }
    }
}

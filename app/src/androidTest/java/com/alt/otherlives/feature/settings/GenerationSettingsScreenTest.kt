package com.alt.otherlives.feature.settings

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import com.alt.otherlives.core.designsystem.AltTheme
import com.alt.otherlives.core.generation.GenerationSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class GenerationSettingsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun configuredSettingsCanBeReviewedAndSaved() {
        val baseUrl = "https://example.com"
        val workflow =
            """{"1":{"inputs":{"image":"__ALT_SOURCE_IMAGE__","text":"__ALT_PROMPT__"}}}"""
        var saveCalled = false
        var savedBaseUrl: String? = null
        var savedWorkflow: String? = null
        var savedConsent: Boolean? = null

        composeRule.setContent {
            AltTheme {
                GenerationSettingsScreen(
                    settings = GenerationSettings(
                        comfyUiBaseUrl = baseUrl,
                        workflowJson = workflow,
                        isConfigured = true,
                        remotePhotoUploadConsent = false
                    ),
                    onBack = {},
                    onSave = { savedUrl, savedJson, consent ->
                        saveCalled = true
                        savedBaseUrl = savedUrl
                        savedWorkflow = savedJson
                        savedConsent = consent
                    },
                    onTestConnection = {},
                    onClear = {}
                )
            }
        }

        composeRule.onNodeWithText("Advanced AI setup")
            .assertIsDisplayed()
        composeRule.onNodeWithText(baseUrl)
            .assertIsDisplayed()
        composeRule.onNodeWithText("Save ComfyUI settings")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(saveCalled)
            assertEquals(baseUrl, savedBaseUrl)
            assertEquals(workflow, savedWorkflow)
            assertFalse(savedConsent ?: true)
        }
    }
    @Test
    fun changingTheComfyUiServerRequiresFreshUploadConsent() {
        var savedConsent: Boolean? = null
        composeRule.setContent {
            AltTheme {
                GenerationSettingsScreen(
                    settings = GenerationSettings(
                        comfyUiBaseUrl = "https://previous.example",
                        workflowJson = """{"1":{"inputs":{"image":"__ALT_SOURCE_IMAGE__"}}}""",
                        isConfigured = true,
                        remotePhotoUploadConsent = true
                    ),
                    onBack = {},
                    onSave = { _, _, consent -> savedConsent = consent },
                    onTestConnection = {},
                    onClear = {}
                )
            }
        }

        composeRule.onNodeWithText("https://previous.example")
            .performTextReplacement("https://next.example")
        composeRule.onNodeWithText(
            "The ComfyUI server has changed. Save this server first, then explicitly enable photo upload for it."
        ).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Save ComfyUI settings")
            .performScrollTo().performClick()

        composeRule.runOnIdle {
            assertEquals(false, savedConsent)
        }
    }

    @Test
    fun savedServerChangeResetsPreviouslyCheckedUnsavedConsent() {
        val persistedSettings = mutableStateOf(
            GenerationSettings(
                comfyUiBaseUrl = "https://old.example",
                workflowJson = """{"1":{"inputs":{"image":"__ALT_SOURCE_IMAGE__"}}}""",
                isConfigured = true,
                remotePhotoUploadConsent = false
            )
        )
        composeRule.setContent {
            AltTheme {
                GenerationSettingsScreen(
                    settings = persistedSettings.value,
                    onBack = {},
                    onSave = { url, workflow, consent ->
                        persistedSettings.value = persistedSettings.value.copy(
                            comfyUiBaseUrl = url,
                            workflowJson = workflow,
                            remotePhotoUploadConsent = consent
                        )
                    },
                    onTestConnection = {},
                    onClear = {}
                )
            }
        }

        composeRule.onNodeWithTag("remotePhotoUploadConsent")
            .performScrollTo().performClick()
        composeRule.onNodeWithTag("remotePhotoUploadConsent").assertIsOn()
        composeRule.onNodeWithText("https://old.example")
            .performTextReplacement("https://new.example")
        composeRule.onNodeWithTag("remotePhotoUploadConsent").assertIsOff()
        composeRule.onNodeWithText("Save ComfyUI settings")
            .performScrollTo().performClick()

        composeRule.onNodeWithTag("remotePhotoUploadConsent").assertIsOff()
        composeRule.runOnIdle {
            assertFalse(persistedSettings.value.remotePhotoUploadConsent)
            assertEquals("https://new.example", persistedSettings.value.comfyUiBaseUrl)
        }
    }

    @Test
    fun privacyPolicyCanBeOpenedFromSettings() {
        var opened = false

        composeRule.setContent {
            AltTheme {
                GenerationSettingsScreen(
                    settings = GenerationSettings(),
                    onBack = {},
                    onSave = { _, _, _ -> },
                    onTestConnection = {},
                    onClear = {},
                    onOpenPrivacyPolicy = { opened = true }
                )
            }
        }

        composeRule.onNodeWithText("Read privacy policy")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(opened)
        }
    }

    @Test
    fun diagnosticsControlsAreAccessible() {
        var copyCalled = false
        var clearCalled = false

        composeRule.setContent {
            AltTheme {
                GenerationSettingsScreen(
                    settings = GenerationSettings(),
                    onBack = {},
                    onSave = { _, _, _ -> },
                    onTestConnection = {},
                    onClear = {},
                    onCopyDiagnostics = { copyCalled = true },
                    onClearDiagnostics = { clearCalled = true }
                )
            }
        }

        composeRule.onNodeWithText("Copy diagnostics")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithText("Clear diagnostics")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(copyCalled)
            assertTrue(clearCalled)
        }
    }
}

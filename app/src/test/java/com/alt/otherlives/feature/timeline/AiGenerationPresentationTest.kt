package com.alt.otherlives.feature.timeline

import com.alt.otherlives.core.generation.GenerationSettings
import com.alt.otherlives.core.generation.GenerationSettingsValidationIssue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiGenerationPresentationTest {
    @Test
    fun failedFreshVariationKeepsPreviousTimelineState() {
        val state = aiGenerationCompletionState(
            resetSeed = true,
            completeFreshVariation = false,
            readySceneCount = 5,
            expectedSceneCount = 5,
            failedChapterIndexes = setOf(1, 3)
        )

        assertEquals(
            AiGenerationCompletionState.FreshVariationIncomplete(
                failedChapterNumbers = listOf(2, 4)
            ),
            state
        )
    }

    @Test
    fun partialGenerationExposesRetryableChapterNumbers() {
        val state = aiGenerationCompletionState(
            resetSeed = false,
            completeFreshVariation = true,
            readySceneCount = 3,
            expectedSceneCount = 5,
            failedChapterIndexes = setOf(1, 4)
        )

        assertEquals(
            AiGenerationCompletionState.Partial(
                readySceneCount = 3,
                expectedSceneCount = 5,
                retryChapterNumbers = listOf(2, 5)
            ),
            state
        )
    }

    @Test
    fun invalidSettingsExposeTypedValidationIssue() {
        val state = aiGenerationUnavailableState(
            hasSourcePhoto = true,
            settings = GenerationSettings(
                comfyUiBaseUrl = "http://invalid",
                workflowJson = "{invalid}",
                isConfigured = false,
                validationError = "Workflow JSON is malformed",
                validationIssue = GenerationSettingsValidationIssue.WORKFLOW_MALFORMED
            )
        )

        assertEquals(
            AiGenerationUnavailableState.InvalidSettings(
                GenerationSettingsValidationIssue.WORKFLOW_MALFORMED
            ),
            state
        )
    }

    @Test
    fun configuredGenerationStillExplainsMissingConsentAsState() {
        val state = aiGenerationUnavailableState(
            hasSourcePhoto = true,
            settings = GenerationSettings(
                comfyUiBaseUrl = "https://example.com",
                workflowJson = "{}",
                isConfigured = true,
                remotePhotoUploadConsent = false
            )
        )

        assertEquals(
            AiGenerationUnavailableState.MissingUploadConsent,
            state
        )
    }

    @Test
    fun buttonStateReflectsTimelineState() {
        val state = aiGenerationButtonState(
            isGenerating = false,
            completed = 0,
            total = 0,
            generatedSceneCount = 2,
            expectedSceneCount = 5
        )

        assertEquals(AiGenerationButtonState.GenerateMissing, state)
    }

    @Test
    fun generatingButtonStatePreservesProgress() {
        val state = aiGenerationButtonState(
            isGenerating = true,
            completed = 2,
            total = 5,
            generatedSceneCount = 2,
            expectedSceneCount = 5
        )

        assertTrue(state is AiGenerationButtonState.Generating)
        assertEquals(
            AiGenerationButtonState.Generating(completed = 2, total = 5),
            state
        )
    }
}

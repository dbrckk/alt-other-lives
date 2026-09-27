package com.alt.otherlives.feature.timeline

import com.alt.otherlives.core.generation.GenerationSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class AiGenerationPresentationTest {
    @Test
    fun failedFreshVariationKeepsPreviousTimelineMessage() {
        assertEquals(
            "New variation incomplete • previous timeline kept • failed chapters 2, 4",
            aiGenerationCompletionMessage(
                resetSeed = true,
                completeFreshVariation = false,
                readySceneCount = 5,
                expectedSceneCount = 5,
                failedChapterIndexes = setOf(1, 3)
            )
        )
    }

    @Test
    fun partialGenerationListsRetryableChapters() {
        assertEquals(
            "Partial result: 3/5 scenes ready • retry chapters 2, 5",
            aiGenerationCompletionMessage(
                resetSeed = false,
                completeFreshVariation = true,
                readySceneCount = 3,
                expectedSceneCount = 5,
                failedChapterIndexes = setOf(1, 4)
            )
        )
    }

    @Test
    fun configuredGenerationStillExplainsMissingConsent() {
        assertEquals(
            "Remote AI generation is configured, but photo upload consent is not enabled.",
            aiGenerationUnavailableMessage(
                hasSourcePhoto = true,
                settings = GenerationSettings(
                    comfyUiBaseUrl = "https://example.com",
                    workflowJson = "{}",
                    isConfigured = true,
                    remotePhotoUploadConsent = false
                )
            )
        )
    }

    @Test
    fun buttonLabelReflectsTimelineState() {
        assertEquals(
            "Generate missing AI scenes",
            aiGenerationButtonLabel(
                isGenerating = false,
                completed = 0,
                total = 0,
                generatedSceneCount = 2,
                expectedSceneCount = 5
            )
        )
    }
}

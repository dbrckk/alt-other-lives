package com.alt.otherlives.feature.timeline

import com.alt.otherlives.core.generation.GenerationChapterFailureKind
import com.alt.otherlives.core.generation.GenerationSettings
import com.alt.otherlives.core.generation.GenerationSettingsValidationIssue

sealed interface AiGenerationCompletionState {
    data class FreshVariationIncomplete(
        val failedChapterNumbers: List<Int>
    ) : AiGenerationCompletionState

    data object Ready : AiGenerationCompletionState

    data class Partial(
        val readySceneCount: Int,
        val expectedSceneCount: Int,
        val retryChapterNumbers: List<Int>,
        val newVariationChapterNumbers: List<Int>
    ) : AiGenerationCompletionState
}

sealed interface AiGenerationUnavailableState {
    data object MissingSourcePhoto : AiGenerationUnavailableState

    data class InvalidSettings(
        val validationIssue: GenerationSettingsValidationIssue?
    ) : AiGenerationUnavailableState

    data object MissingUploadConsent : AiGenerationUnavailableState
    data object NotConfigured : AiGenerationUnavailableState
}

sealed interface AiGenerationButtonState {
    data class Generating(
        val completed: Int,
        val total: Int
    ) : AiGenerationButtonState

    data object GenerateAll : AiGenerationButtonState
    data object GenerateMissing : AiGenerationButtonState
    data object FreshVariationRequired : AiGenerationButtonState
    data object RegenerateAll : AiGenerationButtonState
}

fun aiGenerationCompletionState(
    resetSeed: Boolean,
    completeFreshVariation: Boolean,
    readySceneCount: Int,
    expectedSceneCount: Int,
    failedChapterIndexes: Set<Int>,
    failureKinds: Map<Int, GenerationChapterFailureKind> = emptyMap()
): AiGenerationCompletionState {
    val failedChapterNumbers = failedChapterIndexes
        .sorted()
        .map { it + 1 }
    val qualityRejectedIndexes = failureKinds
        .filterValues {
            it == GenerationChapterFailureKind.QUALITY_REJECTED
        }
        .keys
        .intersect(failedChapterIndexes)
    val retryChapterNumbers = (failedChapterIndexes - qualityRejectedIndexes)
        .sorted()
        .map { it + 1 }
    val newVariationChapterNumbers = qualityRejectedIndexes
        .sorted()
        .map { it + 1 }

    return when {
        resetSeed && !completeFreshVariation ->
            AiGenerationCompletionState.FreshVariationIncomplete(
                failedChapterNumbers = failedChapterNumbers
            )

        readySceneCount == expectedSceneCount ->
            AiGenerationCompletionState.Ready

        else ->
            AiGenerationCompletionState.Partial(
                readySceneCount = readySceneCount,
                expectedSceneCount = expectedSceneCount,
                retryChapterNumbers = retryChapterNumbers,
                newVariationChapterNumbers = newVariationChapterNumbers
            )
    }
}

fun aiGenerationUnavailableState(
    hasSourcePhoto: Boolean,
    settings: GenerationSettings
): AiGenerationUnavailableState = when {
    !hasSourcePhoto ->
        AiGenerationUnavailableState.MissingSourcePhoto

    settings.hasPersistedValues && !settings.isConfigured ->
        AiGenerationUnavailableState.InvalidSettings(
            validationIssue = settings.validationIssue
        )

    settings.isConfigured && !settings.remotePhotoUploadConsent ->
        AiGenerationUnavailableState.MissingUploadConsent

    else ->
        AiGenerationUnavailableState.NotConfigured
}

fun aiGenerationButtonState(
    isGenerating: Boolean,
    completed: Int,
    total: Int,
    generatedSceneCount: Int,
    expectedSceneCount: Int,
    requiresFreshVariation: Boolean = false
): AiGenerationButtonState = when {
    isGenerating ->
        AiGenerationButtonState.Generating(
            completed = completed,
            total = total
        )

    requiresFreshVariation ->
        AiGenerationButtonState.FreshVariationRequired

    generatedSceneCount == 0 ->
        AiGenerationButtonState.GenerateAll

    generatedSceneCount < expectedSceneCount ->
        AiGenerationButtonState.GenerateMissing

    else ->
        AiGenerationButtonState.RegenerateAll
}

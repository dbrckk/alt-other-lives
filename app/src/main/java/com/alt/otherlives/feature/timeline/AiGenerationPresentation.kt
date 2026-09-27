package com.alt.otherlives.feature.timeline

import com.alt.otherlives.core.generation.GenerationSettings

fun aiGenerationCompletionMessage(
    resetSeed: Boolean,
    completeFreshVariation: Boolean,
    readySceneCount: Int,
    expectedSceneCount: Int,
    failedChapterIndexes: Set<Int>
): String {
    val failed = failedChapterIndexes
        .sorted()
        .joinToString(", ") { (it + 1).toString() }

    return when {
        resetSeed && !completeFreshVariation ->
            "New variation incomplete • previous timeline kept" +
                if (failed.isBlank()) "" else " • failed chapters $failed"

        readySceneCount == expectedSceneCount ->
            "AI scenes ready"

        else ->
            "Partial result: $readySceneCount/$expectedSceneCount scenes ready" +
                if (failed.isBlank()) "" else " • retry chapters $failed"
    }
}

fun aiGenerationUnavailableMessage(
    hasSourcePhoto: Boolean,
    settings: GenerationSettings
): String = when {
    !hasSourcePhoto ->
        "AI generation needs the original source photo. Saved generated scenes can still be viewed and exported."

    settings.hasPersistedValues && !settings.isConfigured ->
        "Saved AI settings need attention: " +
            (settings.validationError ?: "configuration is invalid")

    settings.isConfigured && !settings.remotePhotoUploadConsent ->
        "Remote AI generation is configured, but photo upload consent is not enabled."

    else ->
        "AI generation is not configured yet."
}

fun aiGenerationButtonLabel(
    isGenerating: Boolean,
    completed: Int,
    total: Int,
    generatedSceneCount: Int,
    expectedSceneCount: Int
): String = when {
    isGenerating ->
        "Generating AI scenes • $completed/$total"

    generatedSceneCount == 0 ->
        "Generate AI scenes"

    generatedSceneCount < expectedSceneCount ->
        "Generate missing AI scenes"

    else ->
        "Regenerate all AI scenes"
}

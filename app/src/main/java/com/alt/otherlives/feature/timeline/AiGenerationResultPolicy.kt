package com.alt.otherlives.feature.timeline

data class AiGenerationResultPolicy(
    val shouldCommitFreshVariation: Boolean,
    val shouldDiscardPendingFreshDownloads: Boolean
)

fun decideAiGenerationResultPolicy(
    resetSeed: Boolean,
    generatedSceneCount: Int,
    requestedSceneCount: Int
): AiGenerationResultPolicy {
    require(generatedSceneCount >= 0) { "Generated scene count must be non-negative" }
    require(requestedSceneCount >= 0) { "Requested scene count must be non-negative" }
    require(generatedSceneCount <= requestedSceneCount) {
        "Generated scene count cannot exceed requested scene count"
    }

    val completeFreshVariation =
        !resetSeed || generatedSceneCount == requestedSceneCount

    return AiGenerationResultPolicy(
        shouldCommitFreshVariation = completeFreshVariation,
        shouldDiscardPendingFreshDownloads = resetSeed && !completeFreshVariation
    )
}

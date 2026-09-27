package com.alt.otherlives.feature.timeline

data class AiGenerationPlan(
    val expectedIndexes: Set<Int>,
    val missingIndexes: Set<Int>,
    val sameSeedMissingIndexes: Set<Int>,
    val retryableFailedIndexes: Set<Int>,
    val nonRetryableFailedIndexes: Set<Int>,
    val requiresFullRegenerationConfirmation: Boolean,
    val requiresFreshVariation: Boolean
)

fun planAiGeneration(
    chapterCount: Int,
    generatedChapterIndexes: Set<Int>,
    failedChapterIndexes: Set<Int>,
    nonRetryableFailedIndexes: Set<Int> = emptySet()
): AiGenerationPlan {
    require(chapterCount >= 0) { "Chapter count must be non-negative" }

    val expectedIndexes = (0 until chapterCount).toSet()
    val validGenerated = generatedChapterIndexes.intersect(expectedIndexes)
    val missingIndexes = expectedIndexes - validGenerated
    val validNonRetryableFailures =
        nonRetryableFailedIndexes.intersect(expectedIndexes) - validGenerated
    val sameSeedMissingIndexes = missingIndexes - validNonRetryableFailures
    val retryableFailedIndexes =
        failedChapterIndexes.intersect(expectedIndexes) -
            validGenerated -
            validNonRetryableFailures
    val requiresFullRegenerationConfirmation =
        expectedIndexes.isNotEmpty() && missingIndexes.isEmpty()
    val requiresFreshVariation =
        validNonRetryableFailures.isNotEmpty() &&
            sameSeedMissingIndexes.isEmpty()

    return AiGenerationPlan(
        expectedIndexes = expectedIndexes,
        missingIndexes = missingIndexes,
        sameSeedMissingIndexes = sameSeedMissingIndexes,
        retryableFailedIndexes = retryableFailedIndexes,
        nonRetryableFailedIndexes = validNonRetryableFailures,
        requiresFullRegenerationConfirmation = requiresFullRegenerationConfirmation,
        requiresFreshVariation = requiresFreshVariation
    )
}

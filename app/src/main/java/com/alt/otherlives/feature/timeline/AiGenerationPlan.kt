package com.alt.otherlives.feature.timeline

data class AiGenerationPlan(
    val expectedIndexes: Set<Int>,
    val missingIndexes: Set<Int>,
    val retryableFailedIndexes: Set<Int>,
    val requiresFullRegenerationConfirmation: Boolean
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
    val retryableFailedIndexes =
        failedChapterIndexes.intersect(expectedIndexes) -
            validGenerated -
            nonRetryableFailedIndexes.intersect(expectedIndexes)

    return AiGenerationPlan(
        expectedIndexes = expectedIndexes,
        missingIndexes = missingIndexes,
        retryableFailedIndexes = retryableFailedIndexes,
        requiresFullRegenerationConfirmation =
            expectedIndexes.isNotEmpty() && missingIndexes.isEmpty()
    )
}

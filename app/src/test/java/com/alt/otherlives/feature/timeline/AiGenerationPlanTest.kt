package com.alt.otherlives.feature.timeline

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiGenerationPlanTest {
    @Test
    fun incompleteTimelineTargetsOnlyMissingChapters() {
        val plan = planAiGeneration(
            chapterCount = 5,
            generatedChapterIndexes = setOf(0, 2, 4),
            failedChapterIndexes = setOf(1, 2, 3)
        )

        assertEquals(setOf(1, 3), plan.missingIndexes)
        assertEquals(setOf(1, 3), plan.retryableFailedIndexes)
        assertFalse(plan.requiresFullRegenerationConfirmation)
    }

    @Test
    fun qualityRejectedFailuresAreNotSameSeedRetryTargets() {
        val plan = planAiGeneration(
            chapterCount = 5,
            generatedChapterIndexes = setOf(0, 2, 4),
            failedChapterIndexes = setOf(1, 3),
            nonRetryableFailedIndexes = setOf(3)
        )

        assertEquals(setOf(1), plan.retryableFailedIndexes)
        assertEquals(setOf(1, 3), plan.missingIndexes)
    }

    @Test
    fun onlyQualityRejectedMissingSceneRequiresFreshVariation() {
        val plan = planAiGeneration(
            chapterCount = 3,
            generatedChapterIndexes = setOf(0, 1),
            failedChapterIndexes = setOf(2),
            nonRetryableFailedIndexes = setOf(2)
        )

        assertEquals(setOf(2), plan.missingIndexes)
        assertTrue(plan.sameSeedMissingIndexes.isEmpty())
        assertEquals(setOf(2), plan.nonRetryableFailedIndexes)
        assertTrue(plan.requiresFreshVariation)
    }

    @Test
    fun mixedMissingScenesGenerateRetryableOnesBeforeFreshVariation() {
        val plan = planAiGeneration(
            chapterCount = 4,
            generatedChapterIndexes = setOf(0),
            failedChapterIndexes = setOf(1, 2),
            nonRetryableFailedIndexes = setOf(2)
        )

        assertEquals(setOf(1, 3), plan.sameSeedMissingIndexes)
        assertFalse(plan.requiresFreshVariation)
    }

    @Test
    fun completeTimelineRequiresConfirmationBeforeFullRegeneration() {
        val plan = planAiGeneration(
            chapterCount = 3,
            generatedChapterIndexes = setOf(0, 1, 2),
            failedChapterIndexes = emptySet()
        )

        assertTrue(plan.missingIndexes.isEmpty())
        assertTrue(plan.requiresFullRegenerationConfirmation)
    }

    @Test
    fun invalidIndexesAreIgnored() {
        val plan = planAiGeneration(
            chapterCount = 2,
            generatedChapterIndexes = setOf(-1, 0, 8),
            failedChapterIndexes = setOf(1, 9)
        )

        assertEquals(setOf(0, 1), plan.expectedIndexes)
        assertEquals(setOf(1), plan.missingIndexes)
        assertEquals(setOf(1), plan.retryableFailedIndexes)
    }
}

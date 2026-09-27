package com.alt.otherlives.feature.timeline

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiGenerationResultPolicyTest {
    @Test
    fun partialNormalGenerationCanCommitSuccessfulScenes() {
        val policy = decideAiGenerationResultPolicy(
            resetSeed = false,
            generatedSceneCount = 2,
            requestedSceneCount = 5
        )

        assertTrue(policy.shouldCommitFreshVariation)
        assertFalse(policy.shouldDiscardPendingFreshDownloads)
    }

    @Test
    fun incompleteFreshVariationKeepsPreviousTimeline() {
        val policy = decideAiGenerationResultPolicy(
            resetSeed = true,
            generatedSceneCount = 4,
            requestedSceneCount = 5
        )

        assertFalse(policy.shouldCommitFreshVariation)
        assertTrue(policy.shouldDiscardPendingFreshDownloads)
    }

    @Test
    fun completeFreshVariationCanReplaceTimeline() {
        val policy = decideAiGenerationResultPolicy(
            resetSeed = true,
            generatedSceneCount = 5,
            requestedSceneCount = 5
        )

        assertTrue(policy.shouldCommitFreshVariation)
        assertFalse(policy.shouldDiscardPendingFreshDownloads)
    }
}

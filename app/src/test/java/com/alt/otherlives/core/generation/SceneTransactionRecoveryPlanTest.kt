package com.alt.otherlives.core.generation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SceneTransactionRecoveryPlanTest {
    @Test
    fun preCommitBackupsRequireRestoration() {
        assertTrue(
            SceneTransactionRecoveryPlan.shouldRestoreBackupsBeforeCommit(
                commitStarted = false,
                commitCompleted = false,
                hasBackups = true
            )
        )
    }

    @Test
    fun preCommitWithoutBackupsNeedsNoRestore() {
        assertFalse(
            SceneTransactionRecoveryPlan.shouldRestoreBackupsBeforeCommit(
                commitStarted = false,
                commitCompleted = false,
                hasBackups = false
            )
        )
    }

    @Test
    fun startedCommitUsesNormalRollbackInstead() {
        assertFalse(
            SceneTransactionRecoveryPlan.shouldRestoreBackupsBeforeCommit(
                commitStarted = true,
                commitCompleted = false,
                hasBackups = true
            )
        )
    }

    @Test
    fun noRecoveryBeforeCommitStarts() {
        assertNull(
            SceneTransactionRecoveryPlan.build(
                commitStarted = false,
                affectedIndexesText = "0,1,2",
                seedIncluded = true
            )
        )
    }

    @Test
    fun parsesAffectedIndexesAndIgnoresInvalidValues() {
        assertEquals(
            setOf(0, 2, 4),
            SceneTransactionRecoveryPlan.parseAffectedIndexes("0, 2, x, 4, 2")
        )
    }

    @Test
    fun completedCommitDoesNotRequestRollback() {
        assertNull(
            SceneTransactionRecoveryPlan.build(
                commitStarted = true,
                commitCompleted = true,
                affectedIndexesText = "0,1,2",
                seedIncluded = true
            )
        )
    }

    @Test
    fun committedBatchRestoresAffectedScenesWithoutSeedDeletionWhenSeedWasNotIncluded() {
        val plan = requireNotNull(
            SceneTransactionRecoveryPlan.build(
                commitStarted = true,
                affectedIndexesText = "1,3",
                seedIncluded = false
            )
        )

        assertEquals(setOf(1, 3), plan.affectedChapterIndexes)
        assertFalse(plan.deleteCurrentSeed)
    }

    @Test
    fun committedBatchDeletesCurrentSeedWhenTransactionIncludedFreshSeed() {
        val plan = requireNotNull(
            SceneTransactionRecoveryPlan.build(
                commitStarted = true,
                affectedIndexesText = "0,1,2,3,4",
                seedIncluded = true
            )
        )

        assertTrue(plan.deleteCurrentSeed)
    }
}

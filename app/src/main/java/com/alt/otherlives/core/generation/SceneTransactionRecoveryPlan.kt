package com.alt.otherlives.core.generation

internal data class BatchRecoveryPlan(
    val affectedChapterIndexes: Set<Int>,
    val deleteCurrentSeed: Boolean
)

internal object SceneTransactionRecoveryPlan {
    fun shouldRestoreBackupsBeforeCommit(
        commitStarted: Boolean,
        commitCompleted: Boolean,
        hasBackups: Boolean
    ): Boolean = !commitStarted && !commitCompleted && hasBackups

    fun parseAffectedIndexes(value: String?): Set<Int> =
        value
            ?.split(",")
            ?.mapNotNull { it.trim().toIntOrNull() }
            ?.toSet()
            .orEmpty()

    fun build(
        commitStarted: Boolean,
        commitCompleted: Boolean = false,
        affectedIndexesText: String?,
        seedIncluded: Boolean
    ): BatchRecoveryPlan? {
        if (!commitStarted || commitCompleted) return null
        return BatchRecoveryPlan(
            affectedChapterIndexes = parseAffectedIndexes(affectedIndexesText),
            deleteCurrentSeed = seedIncluded
        )
    }
}

package com.alt.otherlives.core.generation

internal data class BatchRecoveryPlan(
    val affectedChapterIndexes: Set<Int>,
    val deleteCurrentSeed: Boolean
)

internal object SceneTransactionRecoveryPlan {
    fun parseAffectedIndexes(value: String?): Set<Int> =
        value
            ?.split(",")
            ?.mapNotNull { it.trim().toIntOrNull() }
            ?.toSet()
            .orEmpty()

    fun build(
        commitStarted: Boolean,
        affectedIndexesText: String?,
        seedIncluded: Boolean
    ): BatchRecoveryPlan? {
        if (!commitStarted) return null
        return BatchRecoveryPlan(
            affectedChapterIndexes = parseAffectedIndexes(affectedIndexesText),
            deleteCurrentSeed = seedIncluded
        )
    }
}

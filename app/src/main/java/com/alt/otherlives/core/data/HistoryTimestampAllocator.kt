package com.alt.otherlives.core.data

internal object HistoryTimestampAllocator {
    fun nextAvailable(
        scenarioId: String,
        requestedCreatedAt: Long,
        existingEntries: List<HistoryEntry>
    ): Long {
        require(requestedCreatedAt > 0L) { "History timestamp must be positive" }

        val used = existingEntries
            .asSequence()
            .filter { it.scenarioId == scenarioId }
            .map { it.createdAt }
            .toHashSet()

        var candidate = requestedCreatedAt
        while (candidate in used) {
            require(candidate < Long.MAX_VALUE) {
                "Unable to allocate unique history timestamp"
            }
            candidate += 1L
        }
        return candidate
    }
}

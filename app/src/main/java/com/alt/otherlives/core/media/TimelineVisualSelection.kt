package com.alt.otherlives.core.media

internal object TimelineVisualSelection {
    fun nearestChapterIndex(
        targetIndex: Int,
        availableIndexes: Set<Int>
    ): Int? = availableIndexes
        .filter { it >= 0 }
        .minWithOrNull(
            compareBy<Int> { kotlin.math.abs(it - targetIndex) }
                .thenBy { it }
        )
}

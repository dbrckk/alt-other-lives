package com.alt.otherlives.core.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TimelineVisualSelectionTest {
    @Test
    fun picksNearestAvailableChapter() {
        assertEquals(
            3,
            TimelineVisualSelection.nearestChapterIndex(
                targetIndex = 4,
                availableIndexes = setOf(0, 3)
            )
        )
    }

    @Test
    fun tiesPreferEarlierChapterForStableContinuity() {
        assertEquals(
            1,
            TimelineVisualSelection.nearestChapterIndex(
                targetIndex = 2,
                availableIndexes = setOf(1, 3)
            )
        )
    }

    @Test
    fun returnsNullWhenNoGeneratedChapterExists() {
        assertNull(
            TimelineVisualSelection.nearestChapterIndex(
                targetIndex = 2,
                availableIndexes = emptySet()
            )
        )
    }
}

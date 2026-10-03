package com.alt.otherlives.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ActiveTimelineRestoreStateTest {
    @Test
    fun restorePreservesActiveScenarioAndTimeline() {
        val restored = ActiveTimelineRestoreState.restore(
            scenarioId = "japan",
            timelineKey = "timeline-123",
            photoFileName = "source-123.jpg"
        )

        assertEquals("japan", restored.scenarioId)
        assertEquals("timeline-123", restored.timelineKey)
        assertEquals("source-123.jpg", restored.photoFileName)
    }

    @Test
    fun blankValuesRestoreAsMissing() {
        val restored = ActiveTimelineRestoreState.restore(
            scenarioId = " ",
            timelineKey = "",
            photoFileName = null
        )

        assertNull(restored.scenarioId)
        assertNull(restored.timelineKey)
        assertNull(restored.photoFileName)
    }
}

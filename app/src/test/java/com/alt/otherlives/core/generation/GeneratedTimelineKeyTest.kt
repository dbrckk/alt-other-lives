package com.alt.otherlives.core.generation

import org.junit.Assert.assertEquals
import org.junit.Test

class GeneratedTimelineKeyTest {
    @Test
    fun acceptsCanonicalTimelineKeys() {
        assertEquals(
            "japan-1790265600000",
            GeneratedTimelineKey.validate("japan-1790265600000")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsParentTraversal() {
        GeneratedTimelineKey.validate("../other")
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNestedPaths() {
        GeneratedTimelineKey.validate("scenario/123")
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsBackslashPaths() {
        GeneratedTimelineKey.validate("scenario\\123")
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsEmbeddedTraversalSegments() {
        GeneratedTimelineKey.validate("scenario..123")
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsBlankKeys() {
        GeneratedTimelineKey.validate("   ")
    }
}

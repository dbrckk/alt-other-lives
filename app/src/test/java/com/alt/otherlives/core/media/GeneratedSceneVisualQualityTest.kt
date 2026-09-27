package com.alt.otherlives.core.media

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratedSceneVisualQualityTest {
    @Test
    fun rejectsFlatBlackOrFlatColorFrames() {
        assertFalse(
            GeneratedSceneVisualQuality.hasSufficientVariation(
                IntArray(64) { 0xFF000000.toInt() }
            )
        )
        assertFalse(
            GeneratedSceneVisualQuality.hasSufficientVariation(
                IntArray(64) { 0xFF303030.toInt() }
            )
        )
    }

    @Test
    fun rejectsAlmostUniformFrames() {
        val pixels = IntArray(64) { index ->
            val value = 40 + index % 5
            (0xFF shl 24) or (value shl 16) or (value shl 8) or value
        }

        assertFalse(GeneratedSceneVisualQuality.hasSufficientVariation(pixels))
    }

    @Test
    fun acceptsOrdinaryVisualVariation() {
        val pixels = IntArray(64) { index ->
            val red = 30 + (index * 7 % 180)
            val green = 20 + (index * 5 % 160)
            val blue = 15 + (index * 11 % 200)
            (0xFF shl 24) or (red shl 16) or (green shl 8) or blue
        }

        assertTrue(GeneratedSceneVisualQuality.hasSufficientVariation(pixels))
    }
}

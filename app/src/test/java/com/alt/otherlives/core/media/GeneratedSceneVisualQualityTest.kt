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
    fun rejectsMostlyTransparentFramesEvenWithRgbVariation() {
        val pixels = IntArray(64) { index ->
            val alpha = if (index < 8) 255 else 0
            val red = 20 + (index * 9 % 200)
            val green = 30 + (index * 5 % 180)
            val blue = 40 + (index * 7 % 160)
            (alpha shl 24) or (red shl 16) or (green shl 8) or blue
        }

        assertFalse(GeneratedSceneVisualQuality.hasSufficientVariation(pixels))
    }

    @Test
    fun acceptsMostlyOpaqueFramesWithVisualVariation() {
        val pixels = IntArray(64) { index ->
            val alpha = if (index < 60) 255 else 0
            val red = 30 + (index * 7 % 180)
            val green = 20 + (index * 5 % 160)
            val blue = 15 + (index * 11 % 200)
            (alpha shl 24) or (red shl 16) or (green shl 8) or blue
        }

        assertTrue(GeneratedSceneVisualQuality.hasSufficientVariation(pixels))
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

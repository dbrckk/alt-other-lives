package com.alt.otherlives.core.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BitmapSamplingTest {
    @Test
    fun actualDecodedSizeMustStayWithinMemoryCap() {
        assertTrue(BitmapSampling.isDecodedSizeSafe(3000, 4000))
        assertTrue(!BitmapSampling.isDecodedSizeSafe(4000, 4000))
    }

    @Test
    fun keepsSmallImagesAtFullResolution() {
        assertEquals(
            1,
            BitmapSampling.calculateSampleSize(
                sourceWidth = 1080,
                sourceHeight = 1920,
                targetWidth = 1080,
                targetHeight = 1920
            )
        )
    }

    @Test
    fun downsamplesLargePortraitForTarget() {
        assertEquals(
            4,
            BitmapSampling.calculateSampleSize(
                sourceWidth = 4320,
                sourceHeight = 7680,
                targetWidth = 1080,
                targetHeight = 1920
            )
        )
    }

    @Test
    fun extremePanoramaIsFurtherReducedForMemory() {
        val sample = BitmapSampling.calculateSampleSize(
            sourceWidth = 32768,
            sourceHeight = 9155,
            targetWidth = 1080,
            targetHeight = 1920
        )
        val decodedWidth = 32768 / sample
        val decodedHeight = 9155 / sample
        assertTrue(
            decodedWidth.toLong() * decodedHeight.toLong() <=
                BitmapSampling.MAX_DECODE_PIXELS
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidTargetDimensions() {
        BitmapSampling.calculateSampleSize(
            sourceWidth = 4000,
            sourceHeight = 3000,
            targetWidth = 0,
            targetHeight = 1920
        )
    }
}

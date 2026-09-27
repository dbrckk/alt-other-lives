package com.alt.otherlives.core.media

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageBoundsValidationTest {
    @Test
    fun acceptsTypicalPhoneAndGeneratedImages() {
        assertTrue(ImageBoundsValidation.isReasonable(4032, 3024))
        assertTrue(ImageBoundsValidation.isReasonable(2160, 3840))
    }

    @Test
    fun rejectsZeroOrNegativeDimensions() {
        assertFalse(ImageBoundsValidation.isReasonable(0, 100))
        assertFalse(ImageBoundsValidation.isReasonable(100, -1))
    }

    @Test
    fun rejectsExcessiveSingleDimension() {
        assertFalse(
            ImageBoundsValidation.isReasonable(
                ImageBoundsValidation.MAX_DIMENSION + 1,
                100
            )
        )
    }

    @Test
    fun generatedScenesRequireEnoughResolution() {
        assertFalse(ImageBoundsValidation.isUsableGeneratedScene(384, 1024))
        assertFalse(ImageBoundsValidation.isUsableGeneratedScene(512, 768))
        assertTrue(ImageBoundsValidation.isUsableGeneratedScene(576, 1024))
        assertTrue(ImageBoundsValidation.isUsableGeneratedScene(768, 1344))
    }

    @Test
    fun premiumSourceThresholdIsStricterThanGeneratedSceneThreshold() {
        assertFalse(ImageBoundsValidation.isLikelyPremiumSource(576, 1024))
        assertTrue(ImageBoundsValidation.isUsableGeneratedScene(576, 1024))
    }

    @Test
    fun rejectsExcessivePixelCount() {
        assertFalse(ImageBoundsValidation.isReasonable(20_000, 20_000))
    }
}

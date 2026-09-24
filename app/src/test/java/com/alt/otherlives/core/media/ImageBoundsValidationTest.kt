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
    fun rejectsExcessivePixelCount() {
        assertFalse(ImageBoundsValidation.isReasonable(20_000, 20_000))
    }
}

package com.alt.otherlives.core.media

import org.junit.Assert.assertEquals
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
    fun generatedScenesRejectExtremeLandscapeOrPanoramaOutputs() {
        assertFalse(ImageBoundsValidation.isUsableGeneratedScene(1344, 768))
        assertFalse(ImageBoundsValidation.isUsableGeneratedScene(1600, 600))
        assertTrue(ImageBoundsValidation.isUsableGeneratedScene(1024, 1024))
        assertTrue(ImageBoundsValidation.isUsableGeneratedScene(768, 1344))
    }

    @Test
    fun premiumSourceThresholdIsStricterThanGeneratedSceneThreshold() {
        assertFalse(ImageBoundsValidation.isLikelyPremiumSource(576, 1024))
        assertTrue(ImageBoundsValidation.isUsableGeneratedScene(576, 1024))
    }

    @Test
    fun premiumSourceAssessmentAcceptsTypicalPortraitAndLandscapePhotos() {
        assertTrue(ImageBoundsValidation.assessSourcePhoto(2160, 3840).isPremiumReady)
        assertTrue(ImageBoundsValidation.assessSourcePhoto(4032, 3024).isPremiumReady)
    }

    @Test
    fun premiumSourceAssessmentFlagsSmallPhotos() {
        val assessment = ImageBoundsValidation.assessSourcePhoto(640, 960)

        assertFalse(assessment.isPremiumReady)
        assertEquals(
            setOf(SourcePhotoQualityIssue.TOO_SMALL),
            assessment.issues
        )
    }

    @Test
    fun premiumSourceAssessmentFlagsExtremeAspectRatios() {
        val assessment = ImageBoundsValidation.assessSourcePhoto(3000, 1000)

        assertFalse(assessment.isPremiumReady)
        assertEquals(
            setOf(SourcePhotoQualityIssue.EXTREME_ASPECT_RATIO),
            assessment.issues
        )
    }

    @Test
    fun rejectsExcessivePixelCount() {
        assertFalse(ImageBoundsValidation.isReasonable(20_000, 20_000))
    }
}

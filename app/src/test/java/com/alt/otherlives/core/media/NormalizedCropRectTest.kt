package com.alt.otherlives.core.media

import org.junit.Assert.assertEquals
import org.junit.Test

class NormalizedCropRectTest {
    @Test
    fun panoramaGetsCenteredFourByFiveCrop() {
        val crop = NormalizedCropRect.centeredAspect(
            sourceWidth = 2000,
            sourceHeight = 1000
        )

        assertEquals(0.4f, crop.width, 0.0001f)
        assertEquals(1f, crop.height, 0.0001f)
        assertEquals(0.3f, crop.left, 0.0001f)
        assertEquals(0.7f, crop.right, 0.0001f)
    }

    @Test
    fun tallSourceGetsCenteredFourByFiveCrop() {
        val crop = NormalizedCropRect.centeredAspect(
            sourceWidth = 500,
            sourceHeight = 1000
        )

        assertEquals(1f, crop.width, 0.0001f)
        assertEquals(0.625f, crop.height, 0.0001f)
        assertEquals(0.1875f, crop.top, 0.0001f)
        assertEquals(0.8125f, crop.bottom, 0.0001f)
    }

    @Test
    fun resizeKeepsPortraitRatioAroundCurrentCenter() {
        val base = NormalizedCropRect.centeredAspect(
            sourceWidth = 2000,
            sourceHeight = 1000
        ).movedBy(deltaX = 0.3f, deltaY = 0f)

        val resized = base.resizedAroundCenter(
            targetWidth = 0.2f,
            targetHeight = 0.5f
        )

        assertEquals(0.2f, resized.width, 0.0001f)
        assertEquals(0.5f, resized.height, 0.0001f)
        assertEquals(0.7f, resized.left, 0.0001f)
        assertEquals(0.9f, resized.right, 0.0001f)
    }

    @Test
    fun movementClampsInsideSourceBounds() {
        val crop = NormalizedCropRect.centered(width = 0.6f, height = 0.6f)

        val moved = crop.movedBy(deltaX = 0.5f, deltaY = -0.5f)

        assertEquals(0.4f, moved.left, 0.0001f)
        assertEquals(1f, moved.right, 0.0001f)
        assertEquals(0f, moved.top, 0.0001f)
        assertEquals(0.6f, moved.bottom, 0.0001f)
    }

    @Test
    fun zoomInShrinksCropAroundItsCenter() {
        val crop = NormalizedCropRect.centered(width = 0.8f, height = 0.8f)

        val zoomed = crop.scaledAroundCenter(factor = 2f)

        assertEquals(0.4f, zoomed.width, 0.0001f)
        assertEquals(0.4f, zoomed.height, 0.0001f)
        assertEquals(0.3f, zoomed.left, 0.0001f)
        assertEquals(0.7f, zoomed.right, 0.0001f)
    }

    @Test
    fun zoomOutNeverEscapesSourceBounds() {
        val crop = NormalizedCropRect.centered(width = 0.5f, height = 0.5f)
            .movedBy(deltaX = 0.2f, deltaY = 0.2f)

        val zoomedOut = crop.scaledAroundCenter(factor = 0.2f)

        assertEquals(NormalizedCropRect.Full, zoomedOut)
    }
}

package com.alt.otherlives.feature.framing

import com.alt.otherlives.core.media.NormalizedCropRect
import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoFramingGestureTest {
    private val baseCrop = NormalizedCropRect.centered(
        width = 0.4f,
        height = 1f
    )

    @Test
    fun pinchZoomShrinksCropWithinMaximumZoom() {
        val zoomed = PhotoFramingGesture.apply(
            crop = baseCrop,
            baseCrop = baseCrop,
            zoomChange = 2f,
            panX = 0f,
            panY = 0f,
            viewportWidth = 400f,
            viewportHeight = 500f
        )

        assertEquals(0.2f, zoomed.width, 0.0001f)
        assertEquals(0.5f, zoomed.height, 0.0001f)
    }

    @Test
    fun pinchZoomOutStopsAtPortraitBaseCrop() {
        val zoomedIn = baseCrop.resizedAroundCenter(
            targetWidth = 0.2f,
            targetHeight = 0.5f
        )

        val zoomedOut = PhotoFramingGesture.apply(
            crop = zoomedIn,
            baseCrop = baseCrop,
            zoomChange = 0.1f,
            panX = 0f,
            panY = 0f,
            viewportWidth = 400f,
            viewportHeight = 500f
        )

        assertEquals(baseCrop, zoomedOut)
    }

    @Test
    fun draggingImageRightMovesCropLeft() {
        val zoomedIn = baseCrop.resizedAroundCenter(
            targetWidth = 0.2f,
            targetHeight = 0.5f
        )

        val moved = PhotoFramingGesture.apply(
            crop = zoomedIn,
            baseCrop = baseCrop,
            zoomChange = 1f,
            panX = 40f,
            panY = 0f,
            viewportWidth = 400f,
            viewportHeight = 500f
        )

        assertEquals(0.38f, moved.left, 0.0001f)
        assertEquals(0.58f, moved.right, 0.0001f)
    }
}

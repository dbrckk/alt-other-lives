package com.alt.otherlives.core.media

import org.junit.Assert.assertEquals
import org.junit.Test

class NormalizedCropRectTest {
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

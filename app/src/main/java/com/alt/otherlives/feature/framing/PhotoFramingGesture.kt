package com.alt.otherlives.feature.framing

import com.alt.otherlives.core.media.NormalizedCropRect

internal object PhotoFramingGesture {
    const val MAX_ZOOM = 2.85f

    fun apply(
        crop: NormalizedCropRect,
        baseCrop: NormalizedCropRect,
        zoomChange: Float,
        panX: Float,
        panY: Float,
        viewportWidth: Float,
        viewportHeight: Float
    ): NormalizedCropRect {
        require(zoomChange > 0f)
        if (viewportWidth <= 0f || viewportHeight <= 0f) return crop

        val currentZoom = maxOf(
            baseCrop.width / crop.width,
            baseCrop.height / crop.height
        ).coerceIn(1f, MAX_ZOOM)
        val targetZoom = (currentZoom * zoomChange).coerceIn(1f, MAX_ZOOM)

        val resized = crop.resizedAroundCenter(
            targetWidth = baseCrop.width / targetZoom,
            targetHeight = baseCrop.height / targetZoom
        )
        val deltaX = -panX * baseCrop.width / (viewportWidth * targetZoom)
        val deltaY = -panY * baseCrop.height / (viewportHeight * targetZoom)
        return resized.movedBy(deltaX, deltaY)
    }
}

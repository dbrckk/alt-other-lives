package com.alt.otherlives.core.media

internal object ImageBoundsValidation {
    const val MAX_DIMENSION = 32_768
    const val MAX_PIXELS = 300_000_000L

    fun isReasonable(width: Int, height: Int): Boolean {
        if (width <= 0 || height <= 0) return false
        if (width > MAX_DIMENSION || height > MAX_DIMENSION) return false
        return width.toLong() * height.toLong() <= MAX_PIXELS
    }
}

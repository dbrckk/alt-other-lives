package com.alt.otherlives.core.media

internal object ImageBoundsValidation {
    const val MAX_DIMENSION = 32_768
    const val MAX_PIXELS = 300_000_000L
    const val PREMIUM_MIN_SHORT_EDGE = 720
    const val PREMIUM_MIN_PIXELS = 1_000_000L
    const val GENERATED_MIN_SHORT_EDGE = 512
    const val GENERATED_MIN_PIXELS = 500_000L
    const val GENERATED_MIN_ASPECT = 0.45f
    const val GENERATED_MAX_ASPECT = 1.25f

    fun isReasonable(width: Int, height: Int): Boolean {
        if (width <= 0 || height <= 0) return false
        if (width > MAX_DIMENSION || height > MAX_DIMENSION) return false
        return width.toLong() * height.toLong() <= MAX_PIXELS
    }

    fun isLikelyPremiumSource(width: Int, height: Int): Boolean {
        if (!isReasonable(width, height)) return false
        val shortEdge = minOf(width, height)
        val pixels = width.toLong() * height.toLong()
        return shortEdge >= PREMIUM_MIN_SHORT_EDGE && pixels >= PREMIUM_MIN_PIXELS
    }

    fun isUsableGeneratedScene(width: Int, height: Int): Boolean {
        if (!isReasonable(width, height)) return false
        val shortEdge = minOf(width, height)
        val pixels = width.toLong() * height.toLong()
        val aspect = width.toFloat() / height.toFloat()
        return shortEdge >= GENERATED_MIN_SHORT_EDGE &&
            pixels >= GENERATED_MIN_PIXELS &&
            aspect in GENERATED_MIN_ASPECT..GENERATED_MAX_ASPECT
    }
}

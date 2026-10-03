package com.alt.otherlives.core.media

enum class SourcePhotoQualityIssue {
    TOO_SMALL,
    EXTREME_ASPECT_RATIO
}

data class SourcePhotoQualityAssessment(
    val isPremiumReady: Boolean,
    val issues: Set<SourcePhotoQualityIssue>
)

internal object ImageBoundsValidation {
    const val MAX_DIMENSION = 32_768
    const val MAX_PIXELS = 300_000_000L
    const val PREMIUM_MIN_SHORT_EDGE = 720
    const val PREMIUM_MIN_PIXELS = 1_000_000L
    const val PREMIUM_MIN_ASPECT = 0.55f
    const val PREMIUM_MAX_ASPECT = 1.8f
    const val GENERATED_MIN_SHORT_EDGE = 512
    const val GENERATED_MIN_PIXELS = 500_000L
    const val GENERATED_MIN_ASPECT = 0.45f
    const val GENERATED_MAX_ASPECT = 1.25f

    fun isReasonable(width: Int, height: Int): Boolean {
        if (width <= 0 || height <= 0) return false
        if (width > MAX_DIMENSION || height > MAX_DIMENSION) return false
        return width.toLong() * height.toLong() <= MAX_PIXELS
    }

    fun assessSourcePhoto(width: Int, height: Int): SourcePhotoQualityAssessment {
        if (!isReasonable(width, height)) {
            return SourcePhotoQualityAssessment(
                isPremiumReady = false,
                issues = setOf(SourcePhotoQualityIssue.TOO_SMALL)
            )
        }
        val shortEdge = minOf(width, height)
        val pixels = width.toLong() * height.toLong()
        val aspect = width.toFloat() / height.toFloat()
        val issues = buildSet {
            if (shortEdge < PREMIUM_MIN_SHORT_EDGE || pixels < PREMIUM_MIN_PIXELS) {
                add(SourcePhotoQualityIssue.TOO_SMALL)
            }
            if (aspect !in PREMIUM_MIN_ASPECT..PREMIUM_MAX_ASPECT) {
                add(SourcePhotoQualityIssue.EXTREME_ASPECT_RATIO)
            }
        }
        return SourcePhotoQualityAssessment(
            isPremiumReady = issues.isEmpty(),
            issues = issues
        )
    }

    fun isLikelyPremiumSource(width: Int, height: Int): Boolean =
        assessSourcePhoto(width, height).isPremiumReady

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

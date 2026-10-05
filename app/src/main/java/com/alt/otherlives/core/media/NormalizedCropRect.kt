package com.alt.otherlives.core.media

data class NormalizedCropRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top

    init {
        require(left in 0f..1f)
        require(top in 0f..1f)
        require(right in 0f..1f)
        require(bottom in 0f..1f)
        require(right > left)
        require(bottom > top)
    }

    fun movedBy(deltaX: Float, deltaY: Float): NormalizedCropRect {
        val clampedDx = deltaX.coerceIn(-left, 1f - right)
        val clampedDy = deltaY.coerceIn(-top, 1f - bottom)
        return copy(
            left = left + clampedDx,
            top = top + clampedDy,
            right = right + clampedDx,
            bottom = bottom + clampedDy
        )
    }

    fun scaledAroundCenter(
        factor: Float,
        minSize: Float = 0.35f
    ): NormalizedCropRect {
        require(factor > 0f)
        val targetWidth = (width / factor).coerceIn(minSize, 1f)
        val targetHeight = (height / factor).coerceIn(minSize, 1f)
        return resizedAroundCenter(targetWidth, targetHeight)
    }

    fun resizedAroundCenter(
        targetWidth: Float,
        targetHeight: Float
    ): NormalizedCropRect {
        require(targetWidth > 0f && targetWidth <= 1f)
        require(targetHeight > 0f && targetHeight <= 1f)
        val centerX = (left + right) / 2f
        val centerY = (top + bottom) / 2f
        val halfW = targetWidth / 2f
        val halfH = targetHeight / 2f
        val shiftedCenterX = centerX.coerceIn(halfW, 1f - halfW)
        val shiftedCenterY = centerY.coerceIn(halfH, 1f - halfH)
        return NormalizedCropRect(
            left = shiftedCenterX - halfW,
            top = shiftedCenterY - halfH,
            right = shiftedCenterX + halfW,
            bottom = shiftedCenterY + halfH
        )
    }

    companion object {
        val Full = NormalizedCropRect(0f, 0f, 1f, 1f)

        fun centeredAspect(
            sourceWidth: Int,
            sourceHeight: Int,
            targetAspect: Float = 4f / 5f
        ): NormalizedCropRect {
            require(sourceWidth > 0)
            require(sourceHeight > 0)
            require(targetAspect > 0f)

            val sourceAspect = sourceWidth.toFloat() / sourceHeight.toFloat()
            return if (sourceAspect >= targetAspect) {
                centered(
                    width = targetAspect / sourceAspect,
                    height = 1f
                )
            } else {
                centered(
                    width = 1f,
                    height = sourceAspect / targetAspect
                )
            }
        }

        fun centered(
            width: Float,
            height: Float
        ): NormalizedCropRect {
            require(width in 0f..1f)
            require(height in 0f..1f)
            val halfW = width / 2f
            val halfH = height / 2f
            return NormalizedCropRect(
                left = 0.5f - halfW,
                top = 0.5f - halfH,
                right = 0.5f + halfW,
                bottom = 0.5f + halfH
            )
        }
    }
}

package com.alt.otherlives.core.media

internal object BitmapSampling {
    const val MAX_DECODE_PIXELS = 12_000_000L

    fun calculateSampleSize(
        sourceWidth: Int,
        sourceHeight: Int,
        targetWidth: Int,
        targetHeight: Int
    ): Int {
        require(sourceWidth > 0 && sourceHeight > 0) {
            "Source bitmap dimensions must be positive"
        }
        require(targetWidth > 0 && targetHeight > 0) {
            "Target bitmap dimensions must be positive"
        }

        var sampleSize = 1
        while (sampleSize <= Int.MAX_VALUE / 2) {
            val next = sampleSize * 2
            val nextWidth = (sourceWidth / next).coerceAtLeast(1)
            val nextHeight = (sourceHeight / next).coerceAtLeast(1)
            val currentWidth = (sourceWidth / sampleSize).coerceAtLeast(1)
            val currentHeight = (sourceHeight / sampleSize).coerceAtLeast(1)
            val currentPixels = currentWidth.toLong() * currentHeight.toLong()

            val canDownsampleForTarget =
                nextWidth >= targetWidth && nextHeight >= targetHeight
            val mustDownsampleForMemory = currentPixels > MAX_DECODE_PIXELS

            if (!canDownsampleForTarget && !mustDownsampleForMemory) {
                break
            }
            sampleSize = next
        }
        return sampleSize
    }
}

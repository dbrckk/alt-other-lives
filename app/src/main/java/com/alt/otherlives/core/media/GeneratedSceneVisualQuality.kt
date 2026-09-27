package com.alt.otherlives.core.media

internal object GeneratedSceneVisualQuality {
    private const val MIN_CHANNEL_RANGE = 12

    fun hasSufficientVariation(pixels: IntArray): Boolean {
        if (pixels.size < 16) return false

        var minRed = 255
        var maxRed = 0
        var minGreen = 255
        var maxGreen = 0
        var minBlue = 255
        var maxBlue = 0

        pixels.forEach { color ->
            val red = color ushr 16 and 0xFF
            val green = color ushr 8 and 0xFF
            val blue = color and 0xFF

            if (red < minRed) minRed = red
            if (red > maxRed) maxRed = red
            if (green < minGreen) minGreen = green
            if (green > maxGreen) maxGreen = green
            if (blue < minBlue) minBlue = blue
            if (blue > maxBlue) maxBlue = blue
        }

        val widestRange = maxOf(
            maxRed - minRed,
            maxGreen - minGreen,
            maxBlue - minBlue
        )
        return widestRange >= MIN_CHANNEL_RANGE
    }
}

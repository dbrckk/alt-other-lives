package com.alt.otherlives.feature.timeline

internal data class RevealResponsiveLayoutSpec(
    val heroHeightDp: Int,
    val titleSizeSp: Int,
    val titleLineHeightSp: Int
)

internal object RevealResponsiveLayout {
    fun resolve(
        screenHeightDp: Int,
        fontScale: Float
    ): RevealResponsiveLayoutSpec {
        val largeText = fontScale >= 1.3f
        val compactHeight = screenHeightDp < 720

        return when {
            largeText -> RevealResponsiveLayoutSpec(
                heroHeightDp = if (compactHeight) 460 else 520,
                titleSizeSp = 34,
                titleLineHeightSp = 38
            )
            compactHeight -> RevealResponsiveLayoutSpec(
                heroHeightDp = 520,
                titleSizeSp = 36,
                titleLineHeightSp = 40
            )
            else -> RevealResponsiveLayoutSpec(
                heroHeightDp = 620,
                titleSizeSp = 42,
                titleLineHeightSp = 44
            )
        }
    }
}

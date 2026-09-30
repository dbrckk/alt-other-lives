package com.alt.otherlives.feature.history

data class HistoryResponsiveLayout(
    val topPaddingDp: Int,
    val titleSizeSp: Int,
    val previewHeightDp: Int,
    val cardPaddingDp: Int,
    val cardSpacingDp: Int
) {
    companion object {
        fun resolve(
            screenHeightDp: Int,
            fontScale: Float
        ): HistoryResponsiveLayout {
            val compact = screenHeightDp < 700
            val largeText = fontScale >= 1.3f

            return when {
                compact && largeText -> HistoryResponsiveLayout(
                    topPaddingDp = 20,
                    titleSizeSp = 27,
                    previewHeightDp = 160,
                    cardPaddingDp = 14,
                    cardSpacingDp = 10
                )
                largeText -> HistoryResponsiveLayout(
                    topPaddingDp = 28,
                    titleSizeSp = 29,
                    previewHeightDp = 180,
                    cardPaddingDp = 16,
                    cardSpacingDp = 10
                )
                compact -> HistoryResponsiveLayout(
                    topPaddingDp = 28,
                    titleSizeSp = 29,
                    previewHeightDp = 180,
                    cardPaddingDp = 16,
                    cardSpacingDp = 10
                )
                else -> HistoryResponsiveLayout(
                    topPaddingDp = 42,
                    titleSizeSp = 31,
                    previewHeightDp = 210,
                    cardPaddingDp = 18,
                    cardSpacingDp = 12
                )
            }
        }
    }
}

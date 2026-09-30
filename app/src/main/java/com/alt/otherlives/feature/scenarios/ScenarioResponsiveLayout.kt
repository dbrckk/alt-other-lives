package com.alt.otherlives.feature.scenarios

data class ScenarioResponsiveLayout(
    val topPaddingDp: Int,
    val headingSizeSp: Int,
    val cardPaddingDp: Int,
    val cardSpacingDp: Int
) {
    companion object {
        fun resolve(
            screenHeightDp: Int,
            fontScale: Float
        ): ScenarioResponsiveLayout {
            val compact = screenHeightDp < 700
            val largeText = fontScale >= 1.3f

            return when {
                compact && largeText -> ScenarioResponsiveLayout(
                    topPaddingDp = 20,
                    headingSizeSp = 28,
                    cardPaddingDp = 18,
                    cardSpacingDp = 10
                )
                largeText -> ScenarioResponsiveLayout(
                    topPaddingDp = 28,
                    headingSizeSp = 30,
                    cardPaddingDp = 20,
                    cardSpacingDp = 12
                )
                compact -> ScenarioResponsiveLayout(
                    topPaddingDp = 28,
                    headingSizeSp = 30,
                    cardPaddingDp = 20,
                    cardSpacingDp = 12
                )
                else -> ScenarioResponsiveLayout(
                    topPaddingDp = 42,
                    headingSizeSp = 34,
                    cardPaddingDp = 22,
                    cardSpacingDp = 14
                )
            }
        }
    }
}

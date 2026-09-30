package com.alt.otherlives.feature.home

internal data class HomeResponsiveMetrics(
    val photoHeroHeightDp: Int,
    val headlineSizeSp: Int,
    val headlineLineHeightSp: Int
)

internal object HomeResponsiveLayout {
    fun resolve(
        screenHeightDp: Int,
        fontScale: Float
    ): HomeResponsiveMetrics {
        val compact = screenHeightDp < 700
        val largeText = fontScale >= 1.3f

        return when {
            compact && largeText -> HomeResponsiveMetrics(
                photoHeroHeightDp = 270,
                headlineSizeSp = 36,
                headlineLineHeightSp = 39
            )
            largeText -> HomeResponsiveMetrics(
                photoHeroHeightDp = 300,
                headlineSizeSp = 38,
                headlineLineHeightSp = 41
            )
            compact -> HomeResponsiveMetrics(
                photoHeroHeightDp = 310,
                headlineSizeSp = 40,
                headlineLineHeightSp = 42
            )
            else -> HomeResponsiveMetrics(
                photoHeroHeightDp = 350,
                headlineSizeSp = 44,
                headlineLineHeightSp = 45
            )
        }
    }
}

package com.alt.otherlives.feature.settings

data class SettingsResponsiveLayout(
    val contentPaddingDp: Int,
    val titleSizeSp: Int,
    val workflowMinLines: Int,
    val sectionSpacingDp: Int
) {
    companion object {
        fun resolve(
            screenHeightDp: Int,
            fontScale: Float
        ): SettingsResponsiveLayout {
            val compact = screenHeightDp < 700
            val largeText = fontScale >= 1.3f

            return when {
                compact && largeText -> SettingsResponsiveLayout(
                    contentPaddingDp = 16,
                    titleSizeSp = 24,
                    workflowMinLines = 7,
                    sectionSpacingDp = 14
                )
                largeText -> SettingsResponsiveLayout(
                    contentPaddingDp = 20,
                    titleSizeSp = 26,
                    workflowMinLines = 9,
                    sectionSpacingDp = 16
                )
                compact -> SettingsResponsiveLayout(
                    contentPaddingDp = 20,
                    titleSizeSp = 26,
                    workflowMinLines = 9,
                    sectionSpacingDp = 16
                )
                else -> SettingsResponsiveLayout(
                    contentPaddingDp = 24,
                    titleSizeSp = 28,
                    workflowMinLines = 12,
                    sectionSpacingDp = 20
                )
            }
        }
    }
}

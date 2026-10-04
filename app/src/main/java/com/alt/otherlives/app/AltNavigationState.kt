package com.alt.otherlives.app

enum class AltScreen {
    HOME,
    FRAMING,
    SCENARIOS,
    REVEAL,
    HISTORY,
    SETTINGS
}

data class AltNavigationState(
    val screen: AltScreen = AltScreen.HOME,
    val settingsReturnScreen: AltScreen = AltScreen.HOME
) {
    fun goTo(target: AltScreen): AltNavigationState =
        copy(screen = target)

    fun openSettings(): AltNavigationState =
        copy(
            screen = AltScreen.SETTINGS,
            settingsReturnScreen = screen
        )

    fun closeSettings(): AltNavigationState =
        copy(screen = settingsReturnScreen)

    companion object {
        fun restore(screenName: String?, settingsReturnScreenName: String?): AltNavigationState {
            val screen = screenName
                ?.let { runCatching { AltScreen.valueOf(it) }.getOrNull() }
                ?: AltScreen.HOME
            val settingsReturnScreen = settingsReturnScreenName
                ?.let { runCatching { AltScreen.valueOf(it) }.getOrNull() }
                ?: AltScreen.HOME
            return AltNavigationState(
                screen = screen,
                settingsReturnScreen = settingsReturnScreen
            )
        }
    }
}

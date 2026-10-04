package com.alt.otherlives.app

object SystemBackNavigationPolicy {
    fun destination(
        state: AltNavigationState
    ): AltNavigationState? = when (state.screen) {
        AltScreen.HOME -> null
        AltScreen.FRAMING -> state.copy(screen = AltScreen.HOME)
        AltScreen.SCENARIOS -> state.copy(screen = AltScreen.FRAMING)
        AltScreen.REVEAL -> state.copy(screen = AltScreen.SCENARIOS)
        AltScreen.HISTORY -> state.copy(screen = AltScreen.HOME)
        AltScreen.SETTINGS -> state.closeSettings()
    }
}

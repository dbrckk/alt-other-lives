package com.alt.otherlives.app

import org.junit.Assert.assertEquals
import org.junit.Test

class AltNavigationStateTest {
    @Test
    fun settingsReturnsToOriginScreen() {
        val fromReveal = AltNavigationState(screen = AltScreen.REVEAL)

        val settings = fromReveal.openSettings()
        val restored = settings.closeSettings()

        assertEquals(AltScreen.SETTINGS, settings.screen)
        assertEquals(AltScreen.REVEAL, settings.settingsReturnScreen)
        assertEquals(AltScreen.REVEAL, restored.screen)
    }

    @Test
    fun restoreFallsBackSafelyForUnknownValues() {
        val restored = AltNavigationState.restore(
            screenName = "NOT_A_SCREEN",
            settingsReturnScreenName = null
        )

        assertEquals(AltScreen.HOME, restored.screen)
        assertEquals(AltScreen.HOME, restored.settingsReturnScreen)
    }

    @Test
    fun restoreKeepsValidScreens() {
        val restored = AltNavigationState.restore(
            screenName = AltScreen.HISTORY.name,
            settingsReturnScreenName = AltScreen.REVEAL.name
        )

        assertEquals(AltScreen.HISTORY, restored.screen)
        assertEquals(AltScreen.REVEAL, restored.settingsReturnScreen)
    }
}

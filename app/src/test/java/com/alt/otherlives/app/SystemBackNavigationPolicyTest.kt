package com.alt.otherlives.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SystemBackNavigationPolicyTest {
    @Test
    fun homeDelegatesBackToAndroid() {
        assertNull(
            SystemBackNavigationPolicy.destination(
                AltNavigationState(screen = AltScreen.HOME)
            )
        )
    }

    @Test
    fun scenariosReturnsHome() {
        assertEquals(
            AltScreen.HOME,
            SystemBackNavigationPolicy.destination(
                AltNavigationState(screen = AltScreen.SCENARIOS)
            )?.screen
        )
    }

    @Test
    fun revealReturnsScenarios() {
        assertEquals(
            AltScreen.SCENARIOS,
            SystemBackNavigationPolicy.destination(
                AltNavigationState(screen = AltScreen.REVEAL)
            )?.screen
        )
    }

    @Test
    fun historyReturnsHome() {
        assertEquals(
            AltScreen.HOME,
            SystemBackNavigationPolicy.destination(
                AltNavigationState(screen = AltScreen.HISTORY)
            )?.screen
        )
    }

    @Test
    fun settingsReturnsToItsOrigin() {
        val settings = AltNavigationState(
            screen = AltScreen.SETTINGS,
            settingsReturnScreen = AltScreen.REVEAL
        )

        assertEquals(
            AltScreen.REVEAL,
            SystemBackNavigationPolicy.destination(settings)?.screen
        )
    }
}

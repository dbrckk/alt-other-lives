package com.alt.otherlives.app

import org.junit.Assert.assertEquals
import org.junit.Test

class NavigationTransitionPolicyTest {
    @Test
    fun creationFlowMovesForward() {
        assertEquals(
            NavigationTransitionDirection.FORWARD,
            NavigationTransitionPolicy.direction(AltScreen.HOME, AltScreen.FRAMING)
        )
        assertEquals(
            NavigationTransitionDirection.FORWARD,
            NavigationTransitionPolicy.direction(AltScreen.FRAMING, AltScreen.SCENARIOS)
        )
        assertEquals(
            NavigationTransitionDirection.FORWARD,
            NavigationTransitionPolicy.direction(AltScreen.SCENARIOS, AltScreen.REVEAL)
        )
    }

    @Test
    fun creationFlowBackNavigationMovesBackward() {
        assertEquals(
            NavigationTransitionDirection.BACKWARD,
            NavigationTransitionPolicy.direction(AltScreen.REVEAL, AltScreen.SCENARIOS)
        )
        assertEquals(
            NavigationTransitionDirection.BACKWARD,
            NavigationTransitionPolicy.direction(AltScreen.SCENARIOS, AltScreen.FRAMING)
        )
        assertEquals(
            NavigationTransitionDirection.BACKWARD,
            NavigationTransitionPolicy.direction(AltScreen.FRAMING, AltScreen.HOME)
        )
    }

    @Test
    fun settingsAndHistoryUseNeutralTransitions() {
        assertEquals(
            NavigationTransitionDirection.NEUTRAL,
            NavigationTransitionPolicy.direction(AltScreen.HOME, AltScreen.HISTORY)
        )
        assertEquals(
            NavigationTransitionDirection.NEUTRAL,
            NavigationTransitionPolicy.direction(AltScreen.HISTORY, AltScreen.HOME)
        )
        assertEquals(
            NavigationTransitionDirection.NEUTRAL,
            NavigationTransitionPolicy.direction(AltScreen.REVEAL, AltScreen.SETTINGS)
        )
        assertEquals(
            NavigationTransitionDirection.NEUTRAL,
            NavigationTransitionPolicy.direction(AltScreen.SETTINGS, AltScreen.REVEAL)
        )
    }

    @Test
    fun disabledSystemAnimationsRemoveNavigationMotion() {
        assertEquals(
            NavigationTransitionDirection.NONE,
            NavigationTransitionPolicy.direction(
                AltScreen.HOME,
                AltScreen.SCENARIOS,
                animationsEnabled = false
            )
        )
        assertEquals(
            NavigationTransitionDirection.NONE,
            NavigationTransitionPolicy.direction(
                AltScreen.REVEAL,
                AltScreen.HISTORY,
                animationsEnabled = false
            )
        )
    }

    @Test
    fun unexpectedCrossFlowNavigationStaysNeutral() {
        assertEquals(
            NavigationTransitionDirection.NEUTRAL,
            NavigationTransitionPolicy.direction(AltScreen.HOME, AltScreen.REVEAL)
        )
    }
}

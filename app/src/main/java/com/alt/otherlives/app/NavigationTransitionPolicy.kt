package com.alt.otherlives.app

enum class NavigationTransitionDirection {
    FORWARD,
    BACKWARD,
    NEUTRAL
}

object NavigationTransitionPolicy {
    fun direction(
        from: AltScreen,
        to: AltScreen
    ): NavigationTransitionDirection = when {
        from == AltScreen.HOME && to == AltScreen.SCENARIOS ->
            NavigationTransitionDirection.FORWARD
        from == AltScreen.SCENARIOS && to == AltScreen.REVEAL ->
            NavigationTransitionDirection.FORWARD
        from == AltScreen.REVEAL && to == AltScreen.SCENARIOS ->
            NavigationTransitionDirection.BACKWARD
        from == AltScreen.SCENARIOS && to == AltScreen.HOME ->
            NavigationTransitionDirection.BACKWARD
        else ->
            NavigationTransitionDirection.NEUTRAL
    }
}

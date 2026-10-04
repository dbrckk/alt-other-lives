package com.alt.otherlives.app

enum class NavigationTransitionDirection {
    FORWARD,
    BACKWARD,
    NEUTRAL,
    NONE
}

object NavigationTransitionPolicy {
    fun direction(
        from: AltScreen,
        to: AltScreen,
        animationsEnabled: Boolean = true
    ): NavigationTransitionDirection {
        if (!animationsEnabled) return NavigationTransitionDirection.NONE

        return when {
        from == AltScreen.HOME && to == AltScreen.FRAMING ->
            NavigationTransitionDirection.FORWARD
        from == AltScreen.FRAMING && to == AltScreen.SCENARIOS ->
            NavigationTransitionDirection.FORWARD
        from == AltScreen.SCENARIOS && to == AltScreen.REVEAL ->
            NavigationTransitionDirection.FORWARD
        from == AltScreen.REVEAL && to == AltScreen.SCENARIOS ->
            NavigationTransitionDirection.BACKWARD
        from == AltScreen.SCENARIOS && to == AltScreen.FRAMING ->
            NavigationTransitionDirection.BACKWARD
        from == AltScreen.FRAMING && to == AltScreen.HOME ->
            NavigationTransitionDirection.BACKWARD
        else ->
            NavigationTransitionDirection.NEUTRAL
        }
    }
}

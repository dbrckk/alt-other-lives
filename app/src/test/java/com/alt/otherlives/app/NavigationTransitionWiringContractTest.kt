package com.alt.otherlives.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationTransitionWiringContractTest {
    @Test
    fun altAppUsesDirectionPolicyForAnimatedContent() {
        val source = File(
            "src/main/java/com/alt/otherlives/app/AltApp.kt"
        ).readText()

        assertTrue(source.contains("NavigationTransitionPolicy.direction(initialState, targetState)"))
        assertTrue(source.contains("NavigationTransitionDirection.FORWARD"))
        assertTrue(source.contains("NavigationTransitionDirection.BACKWARD"))
        assertTrue(source.contains("NavigationTransitionDirection.NEUTRAL"))
        assertTrue(source.contains("slideInHorizontally"))
        assertTrue(source.contains("slideOutHorizontally"))
        assertTrue(source.contains("fadeIn("))
        assertTrue(source.contains("fadeOut("))
    }
}

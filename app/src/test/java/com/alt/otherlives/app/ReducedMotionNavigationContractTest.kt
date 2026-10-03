package com.alt.otherlives.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ReducedMotionNavigationContractTest {
    @Test
    fun appHonorsSystemAnimatorDisablement() {
        val source = File("src/main/java/com/alt/otherlives/app/AltApp.kt").readText()

        assertTrue(source.contains("ValueAnimator.areAnimatorsEnabled()"))
        assertTrue(source.contains("NavigationTransitionDirection.NONE"))
        assertTrue(source.contains("EnterTransition.None togetherWith ExitTransition.None"))
    }
}

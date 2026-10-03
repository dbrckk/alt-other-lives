package com.alt.otherlives.feature.timeline

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class RevealReducedMotionContractTest {
    @Test
    fun revealChapterExpansionHonorsSystemAnimatorSetting() {
        val source = File(
            "src/main/java/com/alt/otherlives/feature/timeline/RevealScreen.kt"
        ).readText()

        assertTrue(source.contains("ValueAnimator.areAnimatorsEnabled()"))
        assertTrue(source.contains("Modifier.animateContentSize"))
        assertTrue(source.contains("else {\n                            Modifier"))
    }
}

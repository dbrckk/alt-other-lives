package com.alt.otherlives.feature.timeline

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RevealGenerationProviderContractTest {
    @Test
    fun revealDependsOnProviderAbstractionInsteadOfComfyUiImplementation() {
        val source = File(
            "src/main/java/com/alt/otherlives/feature/timeline/RevealScreen.kt"
        ).readText()

        assertTrue(source.contains("GenerationProviderResolution.resolve"))
        assertTrue(source.contains("GenerationProviderFactory.create"))
        assertFalse(source.contains("ComfyUiGenerationProvider("))
        assertFalse(source.contains("ComfyUiConfig("))
    }
}

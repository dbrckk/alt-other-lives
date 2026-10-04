package com.alt.otherlives.feature.framing

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class InteractiveCropWiringContractTest {
    @Test
    fun cropFlowsFromFramingIntoGenerationUpload() {
        val app = File("src/main/java/com/alt/otherlives/app/AltApp.kt").readText()
        val reveal = File("src/main/java/com/alt/otherlives/feature/timeline/RevealScreen.kt").readText()
        val orchestrator = File(
            "src/main/java/com/alt/otherlives/feature/timeline/AiGenerationOrchestrator.kt"
        ).readText()
        val provider = File(
            "src/main/java/com/alt/otherlives/core/generation/ComfyUiGenerationProvider.kt"
        ).readText()

        assertTrue(app.contains("onCropChange = { sourceCrop = it }"))
        assertTrue(app.contains("sourceCrop = sourceCrop"))
        assertTrue(reveal.contains("sourceCrop = sourceCrop"))
        assertTrue(orchestrator.contains("sourceCrop = sourceCrop"))
        assertTrue(provider.contains("SourcePhotoCropRenderer.prepare"))
        assertTrue(provider.contains("preparedSource.cleanup()"))
    }
}

package com.alt.otherlives.core.generation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerationProviderResolutionTest {
    @Test
    fun unresolvedWhenRemoteGenerationIsNotReady() {
        assertNull(
            GenerationProviderResolution.resolve(
                GenerationSettings(
                    comfyUiBaseUrl = "https://example.com",
                    workflowJson = "{}",
                    isConfigured = true,
                    remotePhotoUploadConsent = false
                )
            )
        )
    }

    @Test
    fun resolvesConfiguredComfyUiProvider() {
        val resolved = GenerationProviderResolution.resolve(
            GenerationSettings(
                comfyUiBaseUrl = "https://example.com",
                workflowJson = """{"1":{"inputs":{"image":"__ALT_SOURCE_IMAGE__","text":"__ALT_PROMPT__"}}}""",
                isConfigured = true,
                remotePhotoUploadConsent = true
            )
        )

        assertTrue(resolved is GenerationProviderConfig.ComfyUi)
        val comfy = resolved as GenerationProviderConfig.ComfyUi
        assertEquals("https://example.com", comfy.baseUrl)
        assertTrue(comfy.workflowTemplateJson.contains("__ALT_SOURCE_IMAGE__"))
        assertTrue(comfy.workflowTemplateJson.contains("__ALT_PROMPT__"))
    }
}

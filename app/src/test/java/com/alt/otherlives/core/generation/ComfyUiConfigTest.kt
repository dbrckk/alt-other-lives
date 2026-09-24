package com.alt.otherlives.core.generation

import org.junit.Assert.assertEquals
import org.junit.Test

class ComfyUiConfigTest {
    @Test
    fun acceptsHttpsReverseProxyPath() {
        val config = ComfyUiConfig("https://example.com/comfy///")
        config.validate()

        assertEquals("https://example.com/comfy", config.normalizedBaseUrl)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsHttp() {
        ComfyUiConfig("http://example.com").validate()
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsEmbeddedCredentials() {
        ComfyUiConfig("https://user:pass@example.com").validate()
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsQueryString() {
        ComfyUiConfig("https://example.com?token=abc").validate()
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsFragment() {
        ComfyUiConfig("https://example.com/#section").validate()
    }
    @Test(expected = IllegalArgumentException::class)
    fun rejectsOversizedBaseUrl() {
        ComfyUiConfig(
            "https://" + "a".repeat(GenerationSettingsValidation.MAX_BASE_URL_CHARS)
        ).validate()
    }
}


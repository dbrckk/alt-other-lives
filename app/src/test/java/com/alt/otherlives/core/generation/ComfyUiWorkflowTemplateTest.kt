package com.alt.otherlives.core.generation

import org.junit.Assert.assertTrue
import org.junit.Test

class ComfyUiWorkflowTemplateTest {
    private val validTemplate = """
        {
          "1": {"inputs": {"image": "__ALT_SOURCE_IMAGE__"}},
          "2": {"inputs": {"text": "__ALT_PROMPT__"}}
        }
    """.trimIndent()

    @Test
    fun validateTemplateAcceptsRequiredPlaceholders() {
        ComfyUiWorkflowTemplate.validateTemplate(validTemplate)
    }

    @Test(expected = IllegalArgumentException::class)
    fun validateTemplateRejectsMissingImagePlaceholder() {
        ComfyUiWorkflowTemplate.validateTemplate(
            """{"1":{"inputs":{"text":"__ALT_PROMPT__"}}}"""
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun validateTemplateRejectsMissingPromptPlaceholder() {
        ComfyUiWorkflowTemplate.validateTemplate(
            """{"1":{"inputs":{"image":"__ALT_SOURCE_IMAGE__"}}}"""
        )
    }

    @Test
    fun prepareReplacesAllPlaceholders() {
        val prepared = ComfyUiWorkflowTemplate.prepare(
            templateJson = validTemplate,
            uploaded = ComfyUiClient.UploadedImage("source.jpg", "", "input"),
            prompt = "cinematic test"
        ).toString()

        assertTrue(prepared.contains("source.jpg"))
        assertTrue(prepared.contains("cinematic test"))
    }
}

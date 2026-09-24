package com.alt.otherlives.core.generation

import org.junit.Test

class GenerationSettingsValidationTest {
    @Test
    fun acceptsNormalSettingsLengths() {
        GenerationSettingsValidation.validateLengths(
            baseUrl = "https://example.com",
            workflowJson = """{"1":{"inputs":{}}}"""
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsOversizedBaseUrl() {
        GenerationSettingsValidation.validateLengths(
            baseUrl = "x".repeat(GenerationSettingsValidation.MAX_BASE_URL_CHARS + 1),
            workflowJson = "{}"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsOversizedWorkflow() {
        GenerationSettingsValidation.validateLengths(
            baseUrl = "https://example.com",
            workflowJson = "x".repeat(GenerationSettingsValidation.MAX_WORKFLOW_CHARS + 1)
        )
    }
}

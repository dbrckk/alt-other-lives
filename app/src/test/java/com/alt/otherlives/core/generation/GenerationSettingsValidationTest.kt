package com.alt.otherlives.core.generation

import org.junit.Assert.assertEquals
import org.junit.Test

class GenerationSettingsValidationTest {
    @Test
    fun classifiesCommonConfigurationFailures() {
        assertEquals(
            GenerationSettingsValidationIssue.BASE_URL_MUST_USE_HTTPS,
            GenerationSettingsValidation.classify(
                IllegalArgumentException("ComfyUI base URL must use HTTPS.")
            )
        )
        assertEquals(
            GenerationSettingsValidationIssue.WORKFLOW_MALFORMED,
            GenerationSettingsValidation.classify(
                IllegalArgumentException("Workflow JSON is malformed")
            )
        )
        assertEquals(
            GenerationSettingsValidationIssue.WORKFLOW_MISSING_SOURCE_IMAGE,
            GenerationSettingsValidation.classify(
                IllegalArgumentException(
                    "Workflow must contain __ALT_SOURCE_IMAGE__ as an exact JSON value"
                )
            )
        )
    }

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

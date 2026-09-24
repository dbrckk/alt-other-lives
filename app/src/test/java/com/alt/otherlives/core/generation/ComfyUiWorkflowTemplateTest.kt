package com.alt.otherlives.core.generation

import org.junit.Assert.assertEquals
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
    fun preferredOutputNodeIdFindsAltOutputMarker() {
        val workflow = org.json.JSONObject(
            """{
              "7":{"inputs":{},"_meta":{"title":"Preview"}},
              "12":{"inputs":{},"_meta":{"title":"ALT OUTPUT"}}
            }"""
        )

        assertEquals("12", ComfyUiWorkflowTemplate.preferredOutputNodeId(workflow))
    }

    @Test
    fun preferredOutputNodeIdIsCaseInsensitive() {
        val workflow = org.json.JSONObject(
            """{"9":{"inputs":{},"_meta":{"title":"alt output"}}}"""
        )

        assertEquals("9", ComfyUiWorkflowTemplate.preferredOutputNodeId(workflow))
    }

}

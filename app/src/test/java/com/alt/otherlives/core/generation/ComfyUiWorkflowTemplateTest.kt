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
    fun validateTemplateRejectsMalformedJson() {
        ComfyUiWorkflowTemplate.validateTemplate(
            """{"1":{"inputs":{"image":"__ALT_SOURCE_IMAGE__","text":"__ALT_PROMPT__"}}"""
        )
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

    @Test(expected = IllegalArgumentException::class)
    fun validateTemplateRejectsPlaceholderUsedOnlyAsJsonKey() {
        ComfyUiWorkflowTemplate.validateTemplate(
            """{
              "__ALT_SOURCE_IMAGE__":{"value":"source.png"},
              "2":{"inputs":{"text":"__ALT_PROMPT__"}}
            }"""
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun validateTemplateRejectsEmbeddedPromptPlaceholder() {
        ComfyUiWorkflowTemplate.validateTemplate(
            """{
              "1":{"inputs":{"image":"__ALT_SOURCE_IMAGE__"}},
              "2":{"inputs":{"text":"prefix __ALT_PROMPT__ suffix"}}
            }"""
        )
    }

    @Test
    fun validateTemplateAcceptsRequiredPlaceholdersInsideArrays() {
        ComfyUiWorkflowTemplate.validateTemplate(
            """{
              "1":{"inputs":{"images":["__ALT_SOURCE_IMAGE__"]}},
              "2":{"inputs":{"texts":["__ALT_PROMPT__"]}}
            }"""
        )
    }

    @Test
    fun premiumContinuityIssuesAreTypedAndOrdered() {
        val issues = ComfyUiWorkflowTemplate.premiumContinuityIssues(validTemplate)

        assertEquals(
            listOf(
                ComfyUiWorkflowTemplate.PremiumContinuityIssue.MISSING_SHARED_SEED,
                ComfyUiWorkflowTemplate.PremiumContinuityIssue.MISSING_NEGATIVE_PROMPT
            ),
            issues
        )
    }

    @Test
    fun premiumContinuityWarningsReportMissingSeedAndNegativePrompt() {
        val warnings = ComfyUiWorkflowTemplate.premiumContinuityWarnings(validTemplate)

        assertEquals(2, warnings.size)
        assertTrue(warnings.any { it.contains("shared seed") })
        assertTrue(warnings.any { it.contains("negative prompt") })
    }

    @Test
    fun premiumContinuityIssuesWarnOnAmbiguousImageOutputs() {
        val workflow = """
            {
              "1": {"class_type": "LoadImage", "inputs": {"image": "__ALT_SOURCE_IMAGE__"}},
              "2": {"class_type": "CLIPTextEncode", "inputs": {"text": "__ALT_PROMPT__"}},
              "3": {"class_type": "SaveImage", "inputs": {"images": ["8", 0]}},
              "4": {"class_type": "PreviewImage", "inputs": {"images": ["8", 0]}}
            }
        """.trimIndent()

        val issues = ComfyUiWorkflowTemplate.premiumContinuityIssues(workflow)

        assertTrue(
            ComfyUiWorkflowTemplate.PremiumContinuityIssue.AMBIGUOUS_IMAGE_OUTPUTS in issues
        )
    }

    @Test
    fun altOutputMarkerResolvesAmbiguousImageOutputs() {
        val workflow = """
            {
              "1": {"class_type": "LoadImage", "inputs": {"image": "__ALT_SOURCE_IMAGE__"}},
              "2": {"class_type": "CLIPTextEncode", "inputs": {"text": "__ALT_PROMPT__"}},
              "3": {
                "class_type": "SaveImage",
                "inputs": {"images": ["8", 0]},
                "_meta": {"title": "ALT OUTPUT"}
              },
              "4": {"class_type": "PreviewImage", "inputs": {"images": ["8", 0]}}
            }
        """.trimIndent()

        val issues = ComfyUiWorkflowTemplate.premiumContinuityIssues(workflow)

        assertTrue(
            ComfyUiWorkflowTemplate.PremiumContinuityIssue.AMBIGUOUS_IMAGE_OUTPUTS !in issues
        )
    }

    @Test
    fun premiumContinuityWarningsAreEmptyForFullyWiredWorkflow() {
        val workflow = """
            {
              "1": {"inputs": {"image": "__ALT_SOURCE_IMAGE__"}},
              "2": {"inputs": {"text": "__ALT_PROMPT__"}},
              "3": {"inputs": {"negative": "__ALT_NEGATIVE_PROMPT__"}},
              "4": {"inputs": {"seed": "__ALT_SEED__"}}
            }
        """.trimIndent()

        assertTrue(
            ComfyUiWorkflowTemplate.premiumContinuityWarnings(workflow).isEmpty()
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

    @Test
    fun prepareInjectsOptionalNegativePromptAndSeed() {
        val workflow = """
            {
              "1": {"inputs": {"image": "__ALT_SOURCE_IMAGE__"}},
              "2": {"inputs": {"text": "__ALT_PROMPT__"}},
              "3": {"inputs": {"negative": "__ALT_NEGATIVE_PROMPT__"}},
              "4": {"inputs": {"seed": "__ALT_SEED__"}}
            }
        """.trimIndent()

        val prepared = ComfyUiWorkflowTemplate.prepare(
            templateJson = workflow,
            uploaded = ComfyUiClient.UploadedImage(
                name = "source.png",
                subfolder = "",
                type = "input"
            ),
            prompt = "chapter prompt",
            seed = 1234L
        )

        assertEquals(
            "source.png",
            prepared.getJSONObject("1").getJSONObject("inputs").getString("image")
        )
        assertEquals(
            "chapter prompt",
            prepared.getJSONObject("2").getJSONObject("inputs").getString("text")
        )
        assertTrue(
            prepared.getJSONObject("3")
                .getJSONObject("inputs")
                .getString("negative")
                .contains("identity drift")
        )
        assertEquals(
            1234L,
            prepared.getJSONObject("4").getJSONObject("inputs").getLong("seed")
        )
    }

}

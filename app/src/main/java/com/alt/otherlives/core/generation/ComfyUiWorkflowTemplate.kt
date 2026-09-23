package com.alt.otherlives.core.generation

import org.json.JSONArray
import org.json.JSONObject

object ComfyUiWorkflowTemplate {
    fun validateTemplate(templateJson: String) {
        require(templateJson.isNotBlank()) { "Workflow JSON is required" }
        require(templateJson.trimStart().startsWith("{")) { "Workflow must be a JSON object" }
        require(templateJson.contains(ComfyUiWorkflow.PLACEHOLDER_SOURCE_IMAGE)) {
            "Workflow must contain " + ComfyUiWorkflow.PLACEHOLDER_SOURCE_IMAGE
        }
        require(templateJson.contains(ComfyUiWorkflow.PLACEHOLDER_PROMPT)) {
            "Workflow must contain " + ComfyUiWorkflow.PLACEHOLDER_PROMPT
        }
    }

    fun prepare(
        templateJson: String,
        uploaded: ComfyUiClient.UploadedImage,
        prompt: String,
        seed: Long
    ): JSONObject {
        validateTemplate(templateJson)
        val root = JSONObject(templateJson)
        val imageValue = if (uploaded.subfolder.isBlank()) {
            uploaded.name
        } else {
            uploaded.subfolder.trimEnd('/') + "/" + uploaded.name
        }
        replace(root, imageValue, prompt, seed)
        return root
    }

    private fun replace(value: Any?, imageValue: String, prompt: String, seed: Long) {
        when (value) {
            is JSONObject -> {
                val keys = value.keys().asSequence().toList()
                keys.forEach { key ->
                    when (val child = value.get(key)) {
                        ComfyUiWorkflow.PLACEHOLDER_SOURCE_IMAGE -> value.put(key, imageValue)
                        ComfyUiWorkflow.PLACEHOLDER_PROMPT -> value.put(key, prompt)
                        ComfyUiWorkflow.PLACEHOLDER_SEED -> value.put(key, seed)
                        else -> replace(child, imageValue, prompt, seed)
                    }
                }
            }
            is JSONArray -> {
                for (index in 0 until value.length()) {
                    when (val child = value.get(index)) {
                        ComfyUiWorkflow.PLACEHOLDER_SOURCE_IMAGE -> value.put(index, imageValue)
                        ComfyUiWorkflow.PLACEHOLDER_PROMPT -> value.put(index, prompt)
                        ComfyUiWorkflow.PLACEHOLDER_SEED -> value.put(index, seed)
                        else -> replace(child, imageValue, prompt, seed)
                    }
                }
            }
        }
    }
}

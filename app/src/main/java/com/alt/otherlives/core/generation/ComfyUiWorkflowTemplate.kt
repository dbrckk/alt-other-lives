package com.alt.otherlives.core.generation

import org.json.JSONArray
import org.json.JSONObject

object ComfyUiWorkflowTemplate {
    fun validateTemplate(templateJson: String) {
        val root = JSONObject(templateJson)
        val serialized = root.toString()
        require(serialized.contains(ComfyUiWorkflow.PLACEHOLDER_SOURCE_IMAGE)) {
            "Workflow must contain " + ComfyUiWorkflow.PLACEHOLDER_SOURCE_IMAGE
        }
        require(serialized.contains(ComfyUiWorkflow.PLACEHOLDER_PROMPT)) {
            "Workflow must contain " + ComfyUiWorkflow.PLACEHOLDER_PROMPT
        }
    }

    fun prepare(
        templateJson: String,
        uploaded: ComfyUiClient.UploadedImage,
        prompt: String
    ): JSONObject {
        validateTemplate(templateJson)
        val root = JSONObject(templateJson)
        val imageValue = if (uploaded.subfolder.isBlank()) {
            uploaded.name
        } else {
            uploaded.subfolder.trimEnd('/') + "/" + uploaded.name
        }
        replace(root, imageValue, prompt)
        return root
    }

    private fun replace(value: Any?, imageValue: String, prompt: String) {
        when (value) {
            is JSONObject -> {
                val keys = value.keys().asSequence().toList()
                keys.forEach { key ->
                    when (val child = value.get(key)) {
                        ComfyUiWorkflow.PLACEHOLDER_SOURCE_IMAGE -> value.put(key, imageValue)
                        ComfyUiWorkflow.PLACEHOLDER_PROMPT -> value.put(key, prompt)
                        else -> replace(child, imageValue, prompt)
                    }
                }
            }
            is JSONArray -> {
                for (index in 0 until value.length()) {
                    when (val child = value.get(index)) {
                        ComfyUiWorkflow.PLACEHOLDER_SOURCE_IMAGE -> value.put(index, imageValue)
                        ComfyUiWorkflow.PLACEHOLDER_PROMPT -> value.put(index, prompt)
                        else -> replace(child, imageValue, prompt)
                    }
                }
            }
        }
    }
}

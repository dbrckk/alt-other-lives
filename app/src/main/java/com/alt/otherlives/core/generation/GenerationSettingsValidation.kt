package com.alt.otherlives.core.generation

internal object GenerationSettingsValidation {
    const val MAX_WORKFLOW_CHARS = 1_000_000
    const val MAX_BASE_URL_CHARS = 2_048

    fun validateLengths(baseUrl: String, workflowJson: String) {
        require(baseUrl.length <= MAX_BASE_URL_CHARS) {
            "ComfyUI base URL is too long"
        }
        require(workflowJson.length <= MAX_WORKFLOW_CHARS) {
            "Workflow JSON is too large"
        }
    }
}

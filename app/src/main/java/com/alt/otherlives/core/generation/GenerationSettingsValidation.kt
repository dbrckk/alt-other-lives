package com.alt.otherlives.core.generation

internal enum class GenerationSettingsValidationIssue {
    BASE_URL_TOO_LONG,
    BASE_URL_INVALID,
    BASE_URL_MUST_USE_HTTPS,
    BASE_URL_MISSING_HOST,
    BASE_URL_HAS_CREDENTIALS,
    BASE_URL_HAS_QUERY,
    BASE_URL_HAS_FRAGMENT,
    WORKFLOW_REQUIRED,
    WORKFLOW_TOO_LARGE,
    WORKFLOW_MALFORMED,
    WORKFLOW_MISSING_SOURCE_IMAGE,
    WORKFLOW_MISSING_PROMPT,
    UNKNOWN
}

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

    fun classify(error: Throwable?): GenerationSettingsValidationIssue? {
        val message = error?.message.orEmpty()
        if (message.isBlank()) return null

        return when {
            message.contains("base URL is too long", ignoreCase = true) ->
                GenerationSettingsValidationIssue.BASE_URL_TOO_LONG
            message.contains("base URL is invalid", ignoreCase = true) ->
                GenerationSettingsValidationIssue.BASE_URL_INVALID
            message.contains("must use HTTPS", ignoreCase = true) ->
                GenerationSettingsValidationIssue.BASE_URL_MUST_USE_HTTPS
            message.contains("valid host", ignoreCase = true) ->
                GenerationSettingsValidationIssue.BASE_URL_MISSING_HOST
            message.contains("embedded credentials", ignoreCase = true) ->
                GenerationSettingsValidationIssue.BASE_URL_HAS_CREDENTIALS
            message.contains("query string", ignoreCase = true) ->
                GenerationSettingsValidationIssue.BASE_URL_HAS_QUERY
            message.contains("fragment", ignoreCase = true) ->
                GenerationSettingsValidationIssue.BASE_URL_HAS_FRAGMENT
            message.contains("Workflow JSON is required", ignoreCase = true) ->
                GenerationSettingsValidationIssue.WORKFLOW_REQUIRED
            message.contains("Workflow JSON is too large", ignoreCase = true) ->
                GenerationSettingsValidationIssue.WORKFLOW_TOO_LARGE
            message.contains("Workflow JSON is malformed", ignoreCase = true) ->
                GenerationSettingsValidationIssue.WORKFLOW_MALFORMED
            message.contains(ComfyUiWorkflow.PLACEHOLDER_SOURCE_IMAGE) ->
                GenerationSettingsValidationIssue.WORKFLOW_MISSING_SOURCE_IMAGE
            message.contains(ComfyUiWorkflow.PLACEHOLDER_PROMPT) ->
                GenerationSettingsValidationIssue.WORKFLOW_MISSING_PROMPT
            else ->
                GenerationSettingsValidationIssue.UNKNOWN
        }
    }
}

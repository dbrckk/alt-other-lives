package com.alt.otherlives.core.generation

import androidx.annotation.StringRes
import com.alt.otherlives.R

@StringRes
fun GenerationSettingsValidationIssue.messageRes(): Int = when (this) {
    GenerationSettingsValidationIssue.BASE_URL_TOO_LONG ->
        R.string.settings_validation_base_url_too_long
    GenerationSettingsValidationIssue.BASE_URL_INVALID ->
        R.string.settings_validation_base_url_invalid
    GenerationSettingsValidationIssue.BASE_URL_MUST_USE_HTTPS ->
        R.string.settings_validation_https_required
    GenerationSettingsValidationIssue.BASE_URL_MISSING_HOST ->
        R.string.settings_validation_missing_host
    GenerationSettingsValidationIssue.BASE_URL_HAS_CREDENTIALS ->
        R.string.settings_validation_credentials
    GenerationSettingsValidationIssue.BASE_URL_HAS_QUERY ->
        R.string.settings_validation_query
    GenerationSettingsValidationIssue.BASE_URL_HAS_FRAGMENT ->
        R.string.settings_validation_fragment
    GenerationSettingsValidationIssue.WORKFLOW_REQUIRED ->
        R.string.settings_validation_workflow_required
    GenerationSettingsValidationIssue.WORKFLOW_TOO_LARGE ->
        R.string.settings_validation_workflow_too_large
    GenerationSettingsValidationIssue.WORKFLOW_MALFORMED ->
        R.string.settings_validation_workflow_malformed
    GenerationSettingsValidationIssue.WORKFLOW_MISSING_SOURCE_IMAGE ->
        R.string.settings_validation_missing_source
    GenerationSettingsValidationIssue.WORKFLOW_MISSING_PROMPT ->
        R.string.settings_validation_missing_prompt
    GenerationSettingsValidationIssue.UNKNOWN ->
        R.string.settings_validation_unknown
}

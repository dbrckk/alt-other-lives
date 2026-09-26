package com.alt.otherlives.core.generation

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.generationDataStore by preferencesDataStore(name = "alt_generation")

data class GenerationSettings(
    val comfyUiBaseUrl: String = "",
    val workflowJson: String = "",
    val isConfigured: Boolean = false,
    val validationError: String? = null,
    val remotePhotoUploadConsent: Boolean = false
) {
    val hasPersistedValues: Boolean
        get() = comfyUiBaseUrl.isNotBlank() || workflowJson.isNotBlank()

    val isReadyForRemoteGeneration: Boolean
        get() = isConfigured && remotePhotoUploadConsent
}

class GenerationSettingsRepository(private val context: Context) {
    private val baseUrlKey = stringPreferencesKey("comfyui_base_url")
    private val workflowKey = stringPreferencesKey("comfyui_workflow_json")
    private val remotePhotoUploadConsentKey =
        booleanPreferencesKey("remote_photo_upload_consent")

    val settings: Flow<GenerationSettings> = context.generationDataStore.data
        .map { prefs ->
            val baseUrl = prefs[baseUrlKey].orEmpty()
            val workflowJson = prefs[workflowKey].orEmpty()
            val remotePhotoUploadConsent =
                prefs[remotePhotoUploadConsentKey] ?: false
            val validation = runCatching {
                GenerationSettingsValidation.validateLengths(baseUrl, workflowJson)
                ComfyUiConfig(baseUrl.trim()).validate()
                require(workflowJson.isNotBlank()) { "Workflow JSON is required" }
                ComfyUiWorkflowTemplate.validateTemplate(workflowJson)
            }
            val hasPersistedValues = baseUrl.isNotBlank() || workflowJson.isNotBlank()
            GenerationSettings(
                comfyUiBaseUrl = baseUrl,
                workflowJson = workflowJson,
                isConfigured = hasPersistedValues && validation.isSuccess,
                validationError = if (hasPersistedValues) {
                    validation.exceptionOrNull()?.message
                } else {
                    null
                },
                remotePhotoUploadConsent = remotePhotoUploadConsent
            )
        }
        .flowOn(Dispatchers.Default)

    suspend fun save(
        baseUrl: String,
        workflowJson: String,
        remotePhotoUploadConsent: Boolean
    ) {
        val validated = withContext(Dispatchers.Default) {
            val normalized = baseUrl.trim()
            val trimmedWorkflow = workflowJson.trim()
            GenerationSettingsValidation.validateLengths(normalized, trimmedWorkflow)
            ComfyUiConfig(normalized).validate()
            require(trimmedWorkflow.isNotBlank()) { "Workflow JSON is required" }
            ComfyUiWorkflowTemplate.validateTemplate(trimmedWorkflow)
            normalized to trimmedWorkflow
        }

        context.generationDataStore.edit { prefs ->
            prefs[baseUrlKey] = validated.first
            prefs[workflowKey] = validated.second
            prefs[remotePhotoUploadConsentKey] = remotePhotoUploadConsent
        }
    }

    suspend fun clear() {
        context.generationDataStore.edit { prefs ->
            prefs.remove(baseUrlKey)
            prefs.remove(workflowKey)
            prefs.remove(remotePhotoUploadConsentKey)
        }
    }
}

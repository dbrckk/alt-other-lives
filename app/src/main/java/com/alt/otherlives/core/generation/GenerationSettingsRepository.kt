package com.alt.otherlives.core.generation

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.generationDataStore by preferencesDataStore(name = "alt_generation")

data class GenerationSettings(
    val comfyUiBaseUrl: String = "",
    val workflowJson: String = ""
) {
    val isConfigured: Boolean
        get() = comfyUiBaseUrl.isNotBlank() && workflowJson.isNotBlank()
}

class GenerationSettingsRepository(private val context: Context) {
    private val baseUrlKey = stringPreferencesKey("comfyui_base_url")
    private val workflowKey = stringPreferencesKey("comfyui_workflow_json")

    val settings: Flow<GenerationSettings> = context.generationDataStore.data.map { prefs ->
        GenerationSettings(
            comfyUiBaseUrl = prefs[baseUrlKey].orEmpty(),
            workflowJson = prefs[workflowKey].orEmpty()
        )
    }

    suspend fun save(baseUrl: String, workflowJson: String) {
        val normalized = baseUrl.trim()
        val trimmedWorkflow = workflowJson.trim()
        GenerationSettingsValidation.validateLengths(normalized, trimmedWorkflow)
        ComfyUiConfig(normalized).validate()
        require(trimmedWorkflow.isNotBlank()) { "Workflow JSON is required" }
        ComfyUiWorkflowTemplate.validateTemplate(trimmedWorkflow)

        context.generationDataStore.edit { prefs ->
            prefs[baseUrlKey] = normalized
            prefs[workflowKey] = trimmedWorkflow
        }
    }

    suspend fun clear() {
        context.generationDataStore.edit { prefs ->
            prefs.remove(baseUrlKey)
            prefs.remove(workflowKey)
        }
    }
}

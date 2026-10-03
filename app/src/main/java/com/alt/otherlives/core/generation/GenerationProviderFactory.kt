package com.alt.otherlives.core.generation

import android.content.Context

sealed interface GenerationProviderConfig {
    data class ComfyUi(
        val baseUrl: String,
        val workflowTemplateJson: String
    ) : GenerationProviderConfig
}

object GenerationProviderResolution {
    fun resolve(settings: GenerationSettings): GenerationProviderConfig? {
        if (!settings.isReadyForRemoteGeneration) return null

        return GenerationProviderConfig.ComfyUi(
            baseUrl = settings.comfyUiBaseUrl,
            workflowTemplateJson = settings.workflowJson
        )
    }
}

object GenerationProviderFactory {
    fun create(
        context: Context,
        config: GenerationProviderConfig
    ): GenerationProvider =
        when (config) {
            is GenerationProviderConfig.ComfyUi -> ComfyUiGenerationProvider(
                context = context.applicationContext,
                config = ComfyUiConfig(config.baseUrl),
                workflowTemplateJson = config.workflowTemplateJson
            )
        }
}

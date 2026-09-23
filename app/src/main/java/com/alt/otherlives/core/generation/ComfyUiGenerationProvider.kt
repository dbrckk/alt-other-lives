package com.alt.otherlives.core.generation

import android.content.Context

class ComfyUiGenerationProvider(
    context: Context,
    config: ComfyUiConfig,
    private val workflowTemplateJson: String
) : GenerationProvider {
    override val id: String = "comfyui"
    override val displayName: String = "ComfyUI"

    private val client = ComfyUiClient(context.applicationContext, config)

    override suspend fun generate(
        request: GenerationRequest,
        onProgress: (completed: Int, total: Int) -> Unit
    ): List<GeneratedScene> {
        val chapters = request.scenario.chapters.take(5)
        require(chapters.isNotEmpty()) { "Scenario has no chapters" }

        val uploaded = client.uploadImage(request.sourcePhoto)
        val result = mutableListOf<GeneratedScene>()

        chapters.forEachIndexed { index, chapter ->
            val prompt = ComfyUiWorkflow.promptFor(
                scenarioTitle = request.scenario.title,
                chapterLabel = chapter.label,
                chapterNarrative = chapter.narrative
            )
            val workflow = ComfyUiWorkflowTemplate.prepare(
                templateJson = workflowTemplateJson,
                uploaded = uploaded,
                prompt = prompt
            )
            val promptId = client.queuePrompt(workflow)
            val outputs = client.awaitOutputs(promptId)
            val selected = outputs.firstOrNull { it.type == "output" && it.filename.isNotBlank() }
                ?: outputs.firstOrNull { it.filename.isNotBlank() }
                ?: error("ComfyUI returned no image for chapter " + (index + 1))
            val uri = client.download(selected, index)
            result += GeneratedScene(chapterIndex = index, imageUri = uri)
            onProgress(result.size, chapters.size)
        }

        return result
    }
}

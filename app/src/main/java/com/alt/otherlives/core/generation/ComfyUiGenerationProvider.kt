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
        val sessionSeed = (System.currentTimeMillis() and Long.MAX_VALUE).coerceAtLeast(1L)

        chapters.forEachIndexed { index, chapter ->
            val prompt = ComfyUiWorkflow.promptFor(
                scenarioId = request.scenario.id,
                scenarioTitle = request.scenario.title,
                chapterLabel = chapter.label,
                chapterNarrative = chapter.narrative,
                chapterIndex = index
            )
            val generated = generateChapterWithRetry(
                index = index,
                uploaded = uploaded,
                prompt = prompt,
                seed = sessionSeed
            )
            result += generated
            onProgress(result.size, chapters.size)
        }

        return result
    }

    private suspend fun generateChapterWithRetry(
        index: Int,
        uploaded: ComfyUiClient.UploadedImage,
        prompt: String,
        seed: Long
    ): GeneratedScene {
        var lastError: Throwable? = null

        repeat(MAX_CHAPTER_ATTEMPTS) {
            val attemptSeed = seed + index
            val result = runCatching {
                val workflow = ComfyUiWorkflowTemplate.prepare(
                    templateJson = workflowTemplateJson,
                    uploaded = uploaded,
                    prompt = prompt,
                    seed = attemptSeed
                )
                val promptId = client.queuePrompt(workflow)
                val outputs = client.awaitOutputs(promptId)
                val selected = outputs.firstOrNull { it.type == "output" && it.filename.isNotBlank() }
                    ?: outputs.firstOrNull { it.filename.isNotBlank() }
                    ?: error("ComfyUI returned no image for chapter " + (index + 1))
                val uri = client.download(selected, index)
                GeneratedScene(chapterIndex = index, imageUri = uri)
            }

            result.onSuccess { return it }
            lastError = result.exceptionOrNull()
        }

        throw IllegalStateException(
            "ComfyUI failed for chapter " + (index + 1) + " after " + MAX_CHAPTER_ATTEMPTS + " attempts",
            lastError
        )
    }

    private companion object {
        const val MAX_CHAPTER_ATTEMPTS = 2
    }
}

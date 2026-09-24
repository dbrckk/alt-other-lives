package com.alt.otherlives.core.generation

import android.content.Context
import kotlinx.coroutines.CancellationException

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
        onProgress: (completed: Int, total: Int) -> Unit,
        onSceneGenerated: (GeneratedScene) -> Unit
    ): List<GeneratedScene> {
        val chapters = request.scenario.chapters.take(5)
        require(chapters.isNotEmpty()) { "Scenario has no chapters" }

        val requestedIndexes = request.chapterIndexes
            ?.filter { it in chapters.indices }
            ?.toSet()
            ?: chapters.indices.toSet()
        require(requestedIndexes.isNotEmpty()) { "No chapters selected for generation" }

        val uploaded = client.uploadImage(request.sourcePhoto)
        val result = mutableListOf<GeneratedScene>()
        val sessionSeed = request.seed
            ?.takeIf { it > 0L }
            ?: (System.currentTimeMillis() and Long.MAX_VALUE).coerceAtLeast(1L)

        val failures = mutableListOf<String>()
        var processed = 0

        chapters.forEachIndexed { index, chapter ->
            if (index !in requestedIndexes) return@forEachIndexed
            val prompt = ComfyUiWorkflow.promptFor(
                scenarioId = request.scenario.id,
                scenarioTitle = request.scenario.title,
                chapterLabel = chapter.label,
                chapterNarrative = chapter.narrative,
                chapterIndex = index
            )
            try {
                val generated = generateChapterWithRetry(
                    index = index,
                    uploaded = uploaded,
                    prompt = prompt,
                    seed = sessionSeed
                )
                result += generated
                onSceneGenerated(generated)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                failures += "Chapter " + (index + 1) + ": " + (error.message ?: "unknown error")
            }
            processed += 1
            onProgress(processed, requestedIndexes.size)
        }

        if (result.isEmpty()) {
            error(
                "ComfyUI could not generate any chapter" +
                    if (failures.isEmpty()) "" else ": " + failures.joinToString(" | ")
            )
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
            val attemptSeed = seed
            try {
                val workflow = ComfyUiWorkflowTemplate.prepare(
                    templateJson = workflowTemplateJson,
                    uploaded = uploaded,
                    prompt = prompt,
                    seed = attemptSeed
                )
                val promptId = client.queuePrompt(workflow)
                val outputs = client.awaitOutputs(promptId)
                val selected = selectOutput(outputs)
                    ?: error("ComfyUI returned no image for chapter " + (index + 1))
                val uri = client.download(selected, index)
                return GeneratedScene(chapterIndex = index, imageUri = uri)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                lastError = error
            }
        }

        throw IllegalStateException(
            "ComfyUI failed for chapter " + (index + 1) + " after " + MAX_CHAPTER_ATTEMPTS + " attempts",
            lastError
        )
    }

    internal companion object {
        const val MAX_CHAPTER_ATTEMPTS = 2

        fun selectOutput(outputs: List<ComfyUiClient.OutputImage>): ComfyUiClient.OutputImage? =
            outputs
                .filter { it.filename.isNotBlank() }
                .maxWithOrNull(
                    compareBy<ComfyUiClient.OutputImage> {
                        if (it.type.equals("output", ignoreCase = true)) 1 else 0
                    }.thenBy {
                        it.nodeId.toIntOrNull() ?: Int.MIN_VALUE
                    }.thenBy {
                        it.filename
                    }
                )
    }
}

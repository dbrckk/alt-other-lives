package com.alt.otherlives.core.generation

import android.content.Context
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import java.security.SecureRandom

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
        onSceneGenerated: suspend (GeneratedScene) -> Unit,
        onChapterFailure: (chapterIndex: Int, message: String) -> Unit
    ): List<GeneratedScene> {
        val chapters = request.scenario.chapters.take(5)
        require(chapters.isNotEmpty()) { "Scenario has no chapters" }

        val requestedIndexes = validateRequestedChapterIndexes(
            requested = request.chapterIndexes,
            chapterCount = chapters.size
        )

        val sessionSeed = resolveSessionSeed(request.seed)
        val uploaded = client.uploadImage(request.sourcePhoto)
        val result = mutableListOf<GeneratedScene>()

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
                GeneratedSceneAcceptance.accept(
                    scene = generated,
                    onSceneGenerated = onSceneGenerated,
                    accepted = result
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                val message = error.message ?: "unknown error"
                failures += "Chapter " + (index + 1) + ": " + message
                onChapterFailure(index, message)
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

        repeat(MAX_CHAPTER_ATTEMPTS) { attempt ->
            val attemptSeed = seed
            try {
                val workflow = ComfyUiWorkflowTemplate.prepare(
                    templateJson = workflowTemplateJson,
                    uploaded = uploaded,
                    prompt = prompt,
                    seed = attemptSeed
                )
                val preferredOutputNodeId = ComfyUiWorkflowTemplate.preferredOutputNodeId(workflow)
                val promptId = client.queuePrompt(workflow)
                val outputs = client.awaitOutputs(promptId)
                val selected = selectOutput(outputs, preferredOutputNodeId)
                    ?: error("ComfyUI returned no image for chapter " + (index + 1))
                val uri = client.download(selected, index)
                return GeneratedScene(chapterIndex = index, imageUri = uri)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                lastError = error
                if (!ComfyUiRetryPolicy.shouldRetry(error)) {
                    throw error
                }
                if (attempt < MAX_CHAPTER_ATTEMPTS - 1) {
                    delay(RETRY_DELAY_MS)
                }
            }
        }

        throw IllegalStateException(
            "ComfyUI failed for chapter " + (index + 1) + " after " + MAX_CHAPTER_ATTEMPTS + " attempts",
            lastError
        )
    }

    internal companion object {
        const val MAX_CHAPTER_ATTEMPTS = 2
        const val RETRY_DELAY_MS = 750L
        private val seedRandom = SecureRandom()

        fun resolveSessionSeed(seed: Long?): Long {
            if (seed != null) {
                require(seed > 0L) { "Generation seed must be positive" }
                return seed
            }
            return (seedRandom.nextLong() and Long.MAX_VALUE).coerceAtLeast(1L)
        }

        fun validateRequestedChapterIndexes(
            requested: Set<Int>?,
            chapterCount: Int
        ): Set<Int> {
            require(chapterCount > 0) { "Scenario has no chapters" }
            if (requested == null) return (0 until chapterCount).toSet()

            require(requested.isNotEmpty()) { "No chapters selected for generation" }
            val invalid = requested.filterNot { it in 0 until chapterCount }.sorted()
            require(invalid.isEmpty()) {
                "Invalid chapter indexes: " + invalid.joinToString(", ")
            }
            return requested
        }

        fun selectOutput(
            outputs: List<ComfyUiClient.OutputImage>,
            preferredNodeId: String? = null
        ): ComfyUiClient.OutputImage? {
            val usable = outputs.filter { it.filename.isNotBlank() }
            preferredNodeId
                ?.let { preferred -> usable.filter { it.nodeId == preferred } }
                ?.maxWithOrNull(compareBy<ComfyUiClient.OutputImage> {
                    if (it.type.equals("output", ignoreCase = true)) 1 else 0
                }.thenBy { it.filename })
                ?.let { return it }

            return usable.maxWithOrNull(
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
}

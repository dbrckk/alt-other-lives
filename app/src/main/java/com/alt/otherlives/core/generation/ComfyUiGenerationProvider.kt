package com.alt.otherlives.core.generation

import android.content.Context
import kotlinx.coroutines.CancellationException
import java.security.SecureRandom
import com.alt.otherlives.core.data.ScenarioCatalog
import com.alt.otherlives.core.model.Scenario
import com.alt.otherlives.core.model.TimelineConstraints
import com.alt.otherlives.core.media.SourcePhotoCropRenderer

class ComfyUiGenerationProvider(
    context: Context,
    config: ComfyUiConfig,
    private val workflowTemplateJson: String
) : GenerationProvider {
    override val id: String = "comfyui"
    override val displayName: String = "ComfyUI"

    private val appContext = context.applicationContext
    private val client = ComfyUiClient(appContext, config)

    override suspend fun generate(
        request: GenerationRequest,
        onProgress: (completed: Int, total: Int) -> Unit,
        onSceneGenerated: suspend (GeneratedScene) -> Unit,
        onChapterFailure: (
            chapterIndex: Int,
            message: String,
            kind: GenerationChapterFailureKind
        ) -> Unit
    ): List<GeneratedScene> {
        require(request.scenario.chapters.size <= TimelineConstraints.MAX_CHAPTER_COUNT) {
            "Scenario exceeds the supported timeline chapter limit"
        }
        val chapters = request.scenario.chapters
        require(chapters.isNotEmpty()) { "Scenario has no chapters" }
        val promptScenario = canonicalPromptScenario(request.scenario)

        val requestedIndexes = validateRequestedChapterIndexes(
            requested = request.chapterIndexes,
            chapterCount = chapters.size
        )
        ComfyUiWorkflowTemplate.validateTemplate(workflowTemplateJson)

        val sessionSeed = resolveSessionSeed(request.seed)
        val uploaded = PreparedSourceUpload.execute(
            prepare = {
                SourcePhotoCropRenderer.prepare(
                    context = appContext,
                    sourceUri = request.sourcePhoto,
                    crop = request.sourceCrop
                )
            },
            upload = { prepared -> client.uploadImage(prepared.uri) },
            cleanup = { prepared -> prepared.cleanup() }
        )
        val result = mutableListOf<GeneratedScene>()

        val failures = mutableListOf<String>()
        var processed = 0

        chapters.forEachIndexed { index, chapter ->
            if (index !in requestedIndexes) return@forEachIndexed
            val promptChapter = promptScenario.chapters.getOrElse(index) { chapter }
            val prompt = ComfyUiWorkflow.promptFor(
                scenarioId = promptScenario.id,
                scenarioTitle = promptScenario.title,
                chapterLabel = promptChapter.label,
                chapterNarrative = promptChapter.narrative,
                chapterIndex = index
            )
            try {
                val generated = generateChapterOnce(
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
                val kind = if (error is GeneratedSceneQualityException) {
                    GenerationChapterFailureKind.QUALITY_REJECTED
                } else {
                    GenerationChapterFailureKind.OTHER
                }
                onChapterFailure(index, message, kind)
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

    private suspend fun generateChapterOnce(
        index: Int,
        uploaded: ComfyUiClient.UploadedImage,
        prompt: String,
        seed: Long
    ): GeneratedScene {
        val workflow = ComfyUiWorkflowTemplate.prepare(
            templateJson = workflowTemplateJson,
            uploaded = uploaded,
            prompt = prompt,
            seed = seed
        )
        val preferredOutputNodeId = ComfyUiWorkflowTemplate.preferredOutputNodeId(workflow)

        // Keep a successful prompt ID across history polling and download failures.
        // Retrying the whole operation could launch another expensive GPU job.
        return ComfyUiChapterExecution.execute(
            queue = { client.queuePrompt(workflow) },
            await = { promptId ->
                val outputs = client.awaitOutputs(promptId)
                selectOutput(outputs, preferredOutputNodeId)
                    ?: error("ComfyUI returned no image for chapter " + (index + 1))
            },
            download = { selected ->
                GeneratedScene(
                    chapterIndex = index,
                    imageUri = client.download(selected, index)
                )
            }
        )
    }

    internal companion object {
        private val seedRandom = SecureRandom()

        fun canonicalPromptScenario(scenario: Scenario): Scenario =
            ScenarioCatalog.scenarios
                .firstOrNull { it.id == scenario.id }
                ?: scenario

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

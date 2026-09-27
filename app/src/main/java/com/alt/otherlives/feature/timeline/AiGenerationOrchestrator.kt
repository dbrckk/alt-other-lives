package com.alt.otherlives.feature.timeline

import android.content.Context
import android.net.Uri
import com.alt.otherlives.core.generation.GeneratedScene
import com.alt.otherlives.core.generation.GeneratedSceneStore
import com.alt.otherlives.core.generation.GenerationDownloadCache
import com.alt.otherlives.core.generation.GenerationDownloadLifecycle
import com.alt.otherlives.core.generation.GenerationProvider
import com.alt.otherlives.core.generation.GenerationRequest
import com.alt.otherlives.core.model.Scenario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

data class AiGenerationOutcome(
    val scenes: List<GeneratedScene>,
    val failedChapterIndexes: Set<Int>,
    val freshVariationCommitted: Boolean
)

class AiGenerationOrchestrator(
    context: Context,
    private val sceneStore: GeneratedSceneStore
) {
    private val appContext = context.applicationContext

    suspend fun generate(
        provider: GenerationProvider,
        sourcePhoto: Uri,
        scenario: Scenario,
        timelineKey: String,
        targetIndexes: Set<Int>,
        resetSeed: Boolean,
        onProgress: (completed: Int, total: Int) -> Unit,
        onScenesChanged: (List<GeneratedScene>) -> Unit,
        onChapterFailure: (chapterIndex: Int, message: String) -> Unit
    ): AiGenerationOutcome {
        val pendingResetDownloads = mutableListOf<Uri>()
        val failedIndexes = linkedSetOf<Int>()

        try {
            val existingSeed = withContext(Dispatchers.IO) {
                sceneStore.getOrCreateSeed(timelineKey)
            }
            val generationSeed = if (resetSeed) {
                withContext(Dispatchers.IO) {
                    sceneStore.createSeed()
                }
            } else {
                existingSeed
            }

            val newScenes = provider.generate(
                request = GenerationRequest(
                    sourcePhoto = sourcePhoto,
                    scenario = scenario,
                    chapterIndexes = targetIndexes,
                    seed = generationSeed
                ),
                onProgress = onProgress,
                onSceneGenerated = { generated ->
                    failedIndexes.remove(generated.chapterIndex)
                    if (resetSeed) {
                        pendingResetDownloads += generated.imageUri
                    } else {
                        val currentScenes = withContext(Dispatchers.IO) {
                            GenerationDownloadLifecycle.persistAndRelease(
                                persist = {
                                    sceneStore.persist(
                                        timelineKey,
                                        listOf(generated)
                                    )
                                },
                                release = {
                                    GenerationDownloadCache.deleteIfOwned(
                                        appContext,
                                        generated.imageUri
                                    )
                                }
                            )
                            sceneStore.load(timelineKey)
                        }
                        onScenesChanged(currentScenes)
                    }
                },
                onChapterFailure = { chapterIndex, message ->
                    failedIndexes += chapterIndex
                    onChapterFailure(chapterIndex, message)
                }
            )

            val policy = decideAiGenerationResultPolicy(
                resetSeed = resetSeed,
                generatedSceneCount = newScenes.size,
                requestedSceneCount = targetIndexes.size
            )

            val finalScenes = if (policy.shouldCommitFreshVariation) {
                withContext(Dispatchers.IO) {
                    if (resetSeed) {
                        sceneStore.replaceBatchAtomically(
                            timelineKey = timelineKey,
                            scenes = newScenes,
                            seed = generationSeed
                        )
                        releasePendingDownloads(pendingResetDownloads)
                    }
                    sceneStore.load(timelineKey)
                }
            } else {
                withContext(Dispatchers.IO) {
                    if (policy.shouldDiscardPendingFreshDownloads) {
                        releasePendingDownloads(pendingResetDownloads)
                    }
                    sceneStore.load(timelineKey)
                }
            }

            onScenesChanged(finalScenes)

            return AiGenerationOutcome(
                scenes = finalScenes,
                failedChapterIndexes = failedIndexes,
                freshVariationCommitted = policy.shouldCommitFreshVariation
            )
        } catch (cancelled: CancellationException) {
            if (pendingResetDownloads.isNotEmpty()) {
                withContext(NonCancellable + Dispatchers.IO) {
                    releasePendingDownloads(pendingResetDownloads)
                }
            }
            throw cancelled
        } catch (error: Throwable) {
            if (pendingResetDownloads.isNotEmpty()) {
                withContext(Dispatchers.IO) {
                    releasePendingDownloads(pendingResetDownloads)
                }
            }
            throw error
        }
    }

    private fun releasePendingDownloads(uris: MutableList<Uri>) {
        uris.forEach { uri ->
            GenerationDownloadCache.deleteIfOwned(appContext, uri)
        }
        uris.clear()
    }
}

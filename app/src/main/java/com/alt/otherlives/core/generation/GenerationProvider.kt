package com.alt.otherlives.core.generation

import android.net.Uri
import com.alt.otherlives.core.model.Scenario
import com.alt.otherlives.core.media.NormalizedCropRect

data class GenerationRequest(
    val sourcePhoto: Uri,
    val scenario: Scenario,
    val chapterIndexes: Set<Int>? = null,
    val seed: Long? = null,
    val sourceCrop: NormalizedCropRect = NormalizedCropRect.Full
)

data class GeneratedScene(
    val chapterIndex: Int,
    val imageUri: Uri
)

enum class GenerationChapterFailureKind {
    QUALITY_REJECTED,
    OTHER
}

class GeneratedSceneQualityException(message: String) :
    IllegalStateException(message)

sealed interface GenerationState {
    data object Idle : GenerationState
    data class Running(val completed: Int, val total: Int) : GenerationState
    data class Success(val scenes: List<GeneratedScene>) : GenerationState
    data class Failure(val message: String) : GenerationState
}

interface GenerationProvider {
    val id: String
    val displayName: String

    suspend fun generate(
        request: GenerationRequest,
        onProgress: (completed: Int, total: Int) -> Unit = { _, _ -> },
        onSceneGenerated: suspend (GeneratedScene) -> Unit = {},
        onChapterFailure: (
            chapterIndex: Int,
            message: String,
            kind: GenerationChapterFailureKind
        ) -> Unit = { _, _, _ -> }
    ): List<GeneratedScene>
}

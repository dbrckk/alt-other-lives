package com.alt.otherlives.feature.timeline

import com.alt.otherlives.core.generation.GenerationChapterFailureKind

data class RevealAiChapterFailure(
    val message: String,
    val kind: GenerationChapterFailureKind
)

data class RevealAiUiState(
    val isGenerating: Boolean = false,
    val isCancelling: Boolean = false,
    val completed: Int = 0,
    val total: Int = 0,
    val chapterFailures: Map<Int, RevealAiChapterFailure> = emptyMap()
) {
    fun start(
        total: Int,
        targetIndexes: Set<Int>? = null
    ): RevealAiUiState {
        require(total >= 0) { "AI generation total must be non-negative" }
        val retainedFailures = if (targetIndexes == null) {
            emptyMap()
        } else {
            chapterFailures.filterKeys { it !in targetIndexes }
        }
        return copy(
            isGenerating = true,
            isCancelling = false,
            completed = 0,
            total = total,
            chapterFailures = retainedFailures
        )
    }

    fun progress(completed: Int, total: Int): RevealAiUiState {
        require(completed >= 0) { "AI generation progress must be non-negative" }
        require(total >= 0) { "AI generation total must be non-negative" }
        require(completed <= total) { "AI generation progress cannot exceed total" }
        return copy(completed = completed, total = total)
    }

    fun failure(
        chapterIndex: Int,
        message: String,
        kind: GenerationChapterFailureKind
    ): RevealAiUiState {
        require(chapterIndex >= 0) { "Chapter index must be non-negative" }
        require(message.isNotBlank()) { "Chapter failure message is required" }
        return copy(
            chapterFailures = chapterFailures + (
                chapterIndex to RevealAiChapterFailure(message, kind)
            )
        )
    }

    fun retainFailures(indexes: Set<Int>): RevealAiUiState =
        copy(chapterFailures = chapterFailures.filterKeys { it in indexes })

    fun requestCancellation(): RevealAiUiState =
        copy(isCancelling = true)

    fun finish(): RevealAiUiState =
        copy(isGenerating = false, isCancelling = false)
}

package com.alt.otherlives.feature.timeline

data class RevealAiUiState(
    val isGenerating: Boolean = false,
    val isCancelling: Boolean = false,
    val completed: Int = 0,
    val total: Int = 0,
    val chapterFailures: Map<Int, String> = emptyMap()
) {
    fun start(total: Int): RevealAiUiState {
        require(total >= 0) { "AI generation total must be non-negative" }
        return copy(
            isGenerating = true,
            isCancelling = false,
            completed = 0,
            total = total,
            chapterFailures = emptyMap()
        )
    }

    fun progress(completed: Int, total: Int): RevealAiUiState {
        require(completed >= 0) { "AI generation progress must be non-negative" }
        require(total >= 0) { "AI generation total must be non-negative" }
        require(completed <= total) { "AI generation progress cannot exceed total" }
        return copy(completed = completed, total = total)
    }

    fun failure(chapterIndex: Int, message: String): RevealAiUiState {
        require(chapterIndex >= 0) { "Chapter index must be non-negative" }
        require(message.isNotBlank()) { "Chapter failure message is required" }
        return copy(chapterFailures = chapterFailures + (chapterIndex to message))
    }

    fun retainFailures(indexes: Set<Int>): RevealAiUiState =
        copy(chapterFailures = chapterFailures.filterKeys { it in indexes })

    fun requestCancellation(): RevealAiUiState =
        copy(isCancelling = true)

    fun finish(): RevealAiUiState =
        copy(isGenerating = false, isCancelling = false)
}

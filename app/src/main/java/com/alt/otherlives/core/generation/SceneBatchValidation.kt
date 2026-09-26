package com.alt.otherlives.core.generation

internal object SceneBatchValidation {
    const val MAX_CHAPTER_COUNT = 5

    fun isSupportedChapterIndex(index: Int): Boolean =
        index in 0 until MAX_CHAPTER_COUNT

    fun validateChapterIndexes(indexes: List<Int>) {
        require(indexes.all(::isSupportedChapterIndex)) {
            "Generated scene chapter indexes must be between 0 and " +
                (MAX_CHAPTER_COUNT - 1)
        }
        require(indexes.distinct().size == indexes.size) {
            "Generated scene batch contains duplicate chapter indexes"
        }
    }
}

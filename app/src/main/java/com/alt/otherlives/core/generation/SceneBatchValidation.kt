package com.alt.otherlives.core.generation

import com.alt.otherlives.core.model.TimelineConstraints

internal object SceneBatchValidation {

    fun isSupportedChapterIndex(index: Int): Boolean =
        index in 0 until TimelineConstraints.MAX_CHAPTER_COUNT

    fun validateChapterIndexes(indexes: List<Int>) {
        require(indexes.all(::isSupportedChapterIndex)) {
            "Generated scene chapter indexes must be between 0 and " +
                (TimelineConstraints.MAX_CHAPTER_COUNT - 1)
        }
        require(indexes.distinct().size == indexes.size) {
            "Generated scene batch contains duplicate chapter indexes"
        }
    }
}

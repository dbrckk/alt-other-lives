package com.alt.otherlives.core.generation

internal object SceneBatchValidation {
    fun validateChapterIndexes(indexes: List<Int>) {
        require(indexes.all { it >= 0 }) {
            "Generated scene chapter indexes must be non-negative"
        }
        require(indexes.distinct().size == indexes.size) {
            "Generated scene batch contains duplicate chapter indexes"
        }
    }
}

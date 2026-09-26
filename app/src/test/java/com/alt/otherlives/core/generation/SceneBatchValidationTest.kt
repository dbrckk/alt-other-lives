package com.alt.otherlives.core.generation

import org.junit.Test

class SceneBatchValidationTest {
    @Test
    fun acceptsUniqueNonNegativeIndexes() {
        SceneBatchValidation.validateChapterIndexes(listOf(0, 1, 2, 3, 4))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNegativeIndexes() {
        SceneBatchValidation.validateChapterIndexes(listOf(0, -1, 2))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsIndexesBeyondSupportedTimeline() {
        SceneBatchValidation.validateChapterIndexes(listOf(0, 5))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsDuplicateIndexes() {
        SceneBatchValidation.validateChapterIndexes(listOf(0, 1, 1, 2))
    }
}

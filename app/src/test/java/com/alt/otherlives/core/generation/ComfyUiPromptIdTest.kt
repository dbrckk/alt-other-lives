package com.alt.otherlives.core.generation

import org.junit.Assert.assertEquals
import org.junit.Test

class ComfyUiPromptIdTest {
    @Test
    fun acceptsUuidStylePromptId() {
        assertEquals(
            "550e8400-e29b-41d4-a716-446655440000",
            ComfyUiPromptId.validate("550e8400-e29b-41d4-a716-446655440000")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsPathTraversal() {
        ComfyUiPromptId.validate("../history")
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsQueryCharacters() {
        ComfyUiPromptId.validate("abc?x=1")
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsOversizedIds() {
        ComfyUiPromptId.validate("a".repeat(129))
    }
}

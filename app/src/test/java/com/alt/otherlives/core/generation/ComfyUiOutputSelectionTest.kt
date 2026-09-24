package com.alt.otherlives.core.generation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ComfyUiOutputSelectionTest {
    @Test
    fun prefersOutputTypeOverPreview() {
        val selected = ComfyUiGenerationProvider.selectOutput(
            listOf(
                ComfyUiClient.OutputImage("preview.png", "", "temp", "99"),
                ComfyUiClient.OutputImage("final.png", "", "output", "12")
            )
        )

        assertEquals("final.png", selected?.filename)
    }

    @Test
    fun prefersHighestNumericOutputNode() {
        val selected = ComfyUiGenerationProvider.selectOutput(
            listOf(
                ComfyUiClient.OutputImage("older.png", "", "output", "7"),
                ComfyUiClient.OutputImage("newer.png", "", "output", "18")
            )
        )

        assertEquals("newer.png", selected?.filename)
    }

    @Test
    fun ignoresBlankFilenames() {
        val selected = ComfyUiGenerationProvider.selectOutput(
            listOf(
                ComfyUiClient.OutputImage("", "", "output", "20"),
                ComfyUiClient.OutputImage("usable.png", "", "output", "19")
            )
        )

        assertEquals("usable.png", selected?.filename)
    }

    @Test
    fun explicitPreferredNodeWinsOverHigherFallbackNode() {
        val selected = ComfyUiGenerationProvider.selectOutput(
            outputs = listOf(
                ComfyUiClient.OutputImage("marked.png", "", "output", "12"),
                ComfyUiClient.OutputImage("higher.png", "", "output", "99")
            ),
            preferredNodeId = "12"
        )

        assertEquals("marked.png", selected?.filename)
    }

    @Test
    fun returnsNullWhenNoUsableOutputExists() {
        val selected = ComfyUiGenerationProvider.selectOutput(
            listOf(
                ComfyUiClient.OutputImage("", "", "output", "1")
            )
        )

        assertNull(selected)
    }
}

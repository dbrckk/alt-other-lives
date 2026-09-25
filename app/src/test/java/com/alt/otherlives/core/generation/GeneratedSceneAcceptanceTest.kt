package com.alt.otherlives.core.generation

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratedSceneAcceptanceTest {
    @Test
    fun acceptedSceneIsRecordedAfterCallbackSucceeds() = runBlocking {
        val accepted = mutableListOf<String>()

        GeneratedSceneAcceptance.accept(
            scene = "scene-2",
            onSceneGenerated = {},
            accepted = accepted
        )

        assertEquals(listOf("scene-2"), accepted)
    }

    @Test
    fun failedCallbackDoesNotRecordSceneAsAccepted() = runBlocking {
        val accepted = mutableListOf<String>()

        val error = runCatching {
            GeneratedSceneAcceptance.accept(
                scene = "scene-1",
                onSceneGenerated = { error("persist failed") },
                accepted = accepted
            )
        }.exceptionOrNull()

        assertTrue(error is IllegalStateException)
        assertTrue(accepted.isEmpty())
    }
}

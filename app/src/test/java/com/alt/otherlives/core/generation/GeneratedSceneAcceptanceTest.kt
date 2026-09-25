package com.alt.otherlives.core.generation

import android.net.Uri
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratedSceneAcceptanceTest {
    @Test
    fun acceptedSceneIsRecordedAfterCallbackSucceeds() = runBlocking {
        val scene = GeneratedScene(2, Uri.parse("content://alt/scene"))
        val accepted = mutableListOf<GeneratedScene>()

        GeneratedSceneAcceptance.accept(
            scene = scene,
            onSceneGenerated = {},
            accepted = accepted
        )

        assertEquals(listOf(scene), accepted)
    }

    @Test
    fun failedCallbackDoesNotRecordSceneAsAccepted() = runBlocking {
        val scene = GeneratedScene(1, Uri.parse("content://alt/scene"))
        val accepted = mutableListOf<GeneratedScene>()

        val error = runCatching {
            GeneratedSceneAcceptance.accept(
                scene = scene,
                onSceneGenerated = { error("persist failed") },
                accepted = accepted
            )
        }.exceptionOrNull()

        assertTrue(error is IllegalStateException)
        assertTrue(accepted.isEmpty())
    }
}

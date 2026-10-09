package com.alt.otherlives.core.generation

import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ComfyUiChapterExecutionTest {
    @Test
    fun successfulChapterUsesOnePromptIdForPollingAndDownloading() = runBlocking {
        var queueCalls = 0
        var polledId: String? = null
        var downloadedOutput: String? = null

        val result = ComfyUiChapterExecution.execute(
            queue = {
                queueCalls++
                "prompt-123"
            },
            await = { id ->
                polledId = id
                "image-123"
            },
            download = { output ->
                downloadedOutput = output
                "saved-scene"
            }
        )

        assertEquals("saved-scene", result)
        assertEquals(1, queueCalls)
        assertEquals("prompt-123", polledId)
        assertEquals("image-123", downloadedOutput)
    }

    @Test
    fun failedHistoryLookupDoesNotQueueAnotherGpuJob() = runBlocking {
        var queueCalls = 0
        var downloadCalls = 0
        val failure = IOException("history connection dropped")

        val thrown = runCatching {
            ComfyUiChapterExecution.execute(
                queue = {
                    queueCalls++
                    "accepted-prompt"
                },
                await = { throw failure },
                download = { _: String ->
                    downloadCalls++
                    "unreachable"
                }
            )
        }.exceptionOrNull()

        assertSame(failure, thrown)
        assertEquals(1, queueCalls)
        assertEquals(0, downloadCalls)
    }

    @Test
    fun failedImageDownloadDoesNotQueueAnotherGpuJob() = runBlocking {
        var queueCalls = 0
        var awaitCalls = 0
        var downloadCalls = 0
        val failure = IOException("image stream dropped")

        val thrown = runCatching {
            ComfyUiChapterExecution.execute(
                queue = {
                    queueCalls++
                    "accepted-prompt"
                },
                await = {
                    awaitCalls++
                    "image"
                },
                download = { _: String ->
                    downloadCalls++
                    throw failure
                }
            )
        }.exceptionOrNull()

        assertSame(failure, thrown)
        assertEquals(1, queueCalls)
        assertEquals(1, awaitCalls)
        assertEquals(1, downloadCalls)
    }

    @Test
    fun ambiguousQueueFailureIsPropagatedWithoutResubmission() = runBlocking {
        var queueCalls = 0
        var awaitCalls = 0
        val failure = IllegalStateException("queue response lost")

        val thrown = runCatching {
            ComfyUiChapterExecution.execute(
                queue = {
                    queueCalls++
                    throw failure
                },
                await = { _: String ->
                    awaitCalls++
                    "unreachable"
                },
                download = { _: String -> "unreachable" }
            )
        }.exceptionOrNull()

        assertSame(failure, thrown)
        assertEquals(1, queueCalls)
        assertEquals(0, awaitCalls)
    }

    @Test
    fun cancellationDoesNotTriggerAnotherGeneration() = runBlocking {
        var queueCalls = 0

        val thrown = runCatching {
            ComfyUiChapterExecution.execute(
                queue = {
                    queueCalls++
                    "accepted-prompt"
                },
                await = { _: String -> throw CancellationException("user cancelled") },
                download = { _: String -> "unreachable" }
            )
        }.exceptionOrNull()

        assertTrue(thrown is CancellationException)
        assertEquals(1, queueCalls)
    }
}

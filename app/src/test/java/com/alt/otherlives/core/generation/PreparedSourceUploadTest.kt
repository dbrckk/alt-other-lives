package com.alt.otherlives.core.generation

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Test

class PreparedSourceUploadTest {
    @Test
    fun preparationAndUploadDoNotBlockCallingThread() = runBlocking {
        val callingThread = Thread.currentThread()
        var preparationThread: Thread? = null
        var uploadThread: Thread? = null
        var released: String? = null

        val result = PreparedSourceUpload.execute(
            prepare = {
                preparationThread = Thread.currentThread()
                "sanitized-photo"
            },
            upload = { source ->
                uploadThread = Thread.currentThread()
                "uploaded-$source"
            },
            cleanup = { source -> released = source }
        )

        assertEquals("uploaded-sanitized-photo", result)
        assertNotEquals(callingThread, preparationThread)
        assertNotEquals(callingThread, uploadThread)
        assertEquals("sanitized-photo", released)
    }

    @Test
    fun uploadFailureStillReleasesPreparedPhoto() = runBlocking {
        val failure = IllegalStateException("remote upload failed")
        var released = false
        val thrown = runCatching {
            PreparedSourceUpload.execute(
                prepare = { "sanitized-photo" },
                upload = { _: String -> throw failure },
                cleanup = { released = true }
            )
        }.exceptionOrNull()

        assertSame(failure, thrown)
        assertEquals(true, released)
    }

    @Test
    fun cancellationDuringUploadReleasesPreparedPhoto() = runBlocking {
        val started = CompletableDeferred<Unit>()
        var releases = 0

        val job = launch {
            PreparedSourceUpload.execute(
                prepare = { "sanitized-photo" },
                upload = { _: String ->
                    started.complete(Unit)
                    awaitCancellation()
                },
                cleanup = { releases++ }
            )
        }

        started.await()
        job.cancelAndJoin()
        assertEquals(1, releases)
    }

    @Test
    fun preparationFailureDoesNotAttemptUploadOrCleanup() = runBlocking {
        val failure = IllegalStateException("decode failed")
        var uploadCalls = 0
        var cleanupCalls = 0

        val thrown = runCatching {
            PreparedSourceUpload.execute(
                prepare = { throw failure },
                upload = { _: String ->
                    uploadCalls++
                    "unreachable"
                },
                cleanup = { cleanupCalls++ }
            )
        }.exceptionOrNull()

        assertSame(failure, thrown)
        assertEquals(0, uploadCalls)
        assertEquals(0, cleanupCalls)
    }
}

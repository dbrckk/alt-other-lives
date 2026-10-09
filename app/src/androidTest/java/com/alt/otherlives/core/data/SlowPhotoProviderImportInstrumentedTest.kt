package com.alt.otherlives.core.data

import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Integration tests: read from a real Android ContentProvider in the test APK,
 * exercising ContentResolver, pipes, cancellation and private-file cleanup.
 */
class SlowPhotoProviderImportInstrumentedTest {
    @Test
    fun cancellingAStalledContentProviderImportLeavesNoPrivatePhoto() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.filesDir, "source-photos").apply { mkdirs() }
        val before = fileNames(directory)
        val uri = Uri.parse("content://${SlowPhotoTestProvider.AUTHORITY}/slow-jpeg")

        val importJob = launch(Dispatchers.IO) {
            val importContext = currentCoroutineContext()
            SourcePhotoStore(context).import(
                uri = uri,
                checkCancelled = { importContext.ensureActive() }
            )
        }

        try {
            // The provider has yielded its first JPEG bytes; its pipe then
            // deliberately stalls for 1.4 seconds before sending the rest.
            withTimeout(8_000L) {
                while (
                    directory.listFiles().orEmpty().none {
                        it.isFile && it.name !in before &&
                            it.name.startsWith(".source-") && it.length() > 0L
                    }
                ) {
                    delay(10)
                }
            }
            importJob.cancel()
            withTimeout(8_000L) {
                importJob.join()
            }

            assertTrue(importJob.isCancelled)
            assertEquals(before, fileNames(directory))
        } finally {
            importJob.cancel()
            withTimeout(8_000L) { importJob.join() }
        }
    }

    @Test
    fun oversizedAndroidProviderStreamIsRejectedAndItsTemporaryCopyDeleted() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.filesDir, "source-photos").apply { mkdirs() }
        val before = fileNames(directory)
        val uri = Uri.parse("content://${SlowPhotoTestProvider.AUTHORITY}/oversized")

        val failure = withTimeout(45_000L) {
            runCatching {
                withContext(Dispatchers.IO) {
                    SourcePhotoStore(context).import(uri)
                }
            }.exceptionOrNull()
        }

        assertTrue("Expected bounded copy to reject 51 MiB", failure is IllegalArgumentException)
        assertTrue(
            "Expected byte-limit failure",
            failure?.message?.contains("maximum allowed size") == true
        )
        assertEquals(before, fileNames(directory))
    }

    private fun fileNames(directory: File): Set<String> =
        directory.listFiles().orEmpty().map { it.name }.toSet()
}

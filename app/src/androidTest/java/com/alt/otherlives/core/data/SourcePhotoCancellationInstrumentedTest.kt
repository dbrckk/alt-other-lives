package com.alt.otherlives.core.data

import android.graphics.Bitmap
import androidx.core.content.FileProvider
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * Real Android storage/FileProvider integration checks, not JVM unit tests.
 * A cancelled import must never retain its private staged or finalized file.
 */
class SourcePhotoCancellationInstrumentedTest {
    @Test
    fun cancellationDuringCopyRemovesTemporaryAndFinalFiles() {
        withFixture { store, uri, directory ->
            val filesBefore = directory.listFiles().orEmpty().map { it.name }.toSet()
            var checks = 0
            try {
                store.import(uri) {
                    checks++
                    if (checks == 3) throw CancellationException("cancel during input copy")
                }
                fail("Cancelled import unexpectedly succeeded")
            } catch (expected: CancellationException) {
                assertTrue(checks >= 3)
            }

            assertEquals(
                filesBefore,
                directory.listFiles().orEmpty().map { it.name }.toSet()
            )
        }
    }

    @Test
    fun cancellationImmediatelyAfterFinalRenameStillRemovesPrivatePhoto() {
        withFixture { store, uri, directory ->
            val filesBefore = directory.listFiles().orEmpty().map { it.name }.toSet()
            var sawFinalizedFile = false
            try {
                store.import(uri) {
                    sawFinalizedFile = directory.listFiles().orEmpty().any { file ->
                        file.name !in filesBefore && SourcePhotoFileName.isValid(file.name)
                    }
                    if (sawFinalizedFile) {
                        throw CancellationException("cancel after final rename")
                    }
                }
                fail("Cancelled import unexpectedly succeeded")
            } catch (expected: CancellationException) {
                assertTrue(sawFinalizedFile)
            }

            assertEquals(
                filesBefore,
                directory.listFiles().orEmpty().map { it.name }.toSet()
            )
        }
    }

    @Test
    fun explicitCancellationRollbackDeletesOnlyTheRequestedFinalizedPhoto() {
        withFixture { store, uri, directory ->
            val before = directory.listFiles().orEmpty().map { it.name }.toSet()
            val imported = store.import(uri)
            val ownFile = File(directory, imported.fileName)
            assertTrue(ownFile.isFile)

            store.discardCancelledImport(imported.fileName)

            assertFalse(ownFile.exists())
            assertEquals(
                before,
                directory.listFiles().orEmpty().map { it.name }.toSet()
            )
        }
    }

    private fun withFixture(
        block: (SourcePhotoStore, android.net.Uri, File) -> Unit
    ) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val sourceDir = File(context.cacheDir, "generation")
        assertTrue(sourceDir.isDirectory || sourceDir.mkdirs())
        val sourceFile = File(sourceDir, "import-cancellation-${UUID.randomUUID()}.jpg")
        val bitmap = Bitmap.createBitmap(420, 280, Bitmap.Config.ARGB_8888)
        try {
            sourceFile.outputStream().use { output ->
                assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output))
            }
            val sourceUri = FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                sourceFile
            )
            val storeDir = File(context.filesDir, "source-photos")
            assertTrue(storeDir.isDirectory || storeDir.mkdirs())
            block(SourcePhotoStore(context), sourceUri, storeDir)
        } finally {
            bitmap.recycle()
            sourceFile.delete()
        }
    }
}

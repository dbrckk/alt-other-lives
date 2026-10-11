package com.alt.otherlives.core.media

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Android integration checks using the real MediaStore provider and the
 * app's FileProvider. Failed or cancelled gallery copies must not leave
 * pending or publicly visible items in Android's media database.
 */
class GalleryMediaSaverInstrumentedTest {
    @Test
    fun cancellationDuringImageCopyDeletesPendingGalleryItem() {
        verifyCancelledCopy(
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            mimeType = "image/jpeg",
            relativePath = "Pictures/ALT",
            extension = "jpg"
        )
    }

    @Test
    fun cancellationDuringVideoCopyDeletesPendingGalleryItem() {
        verifyCancelledCopy(
            collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            mimeType = "video/mp4",
            relativePath = "Movies/ALT",
            extension = "mp4"
        )
    }

    @Test
    fun successfulImageCopyIsPublishedAndReadable() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val sourceDir = File(context.cacheDir, "shares").apply {
            assertTrue(isDirectory || mkdirs())
        }
        val sourceFile = File(sourceDir, "gallery-success-${UUID.randomUUID()}.jpg")
        val bitmap = Bitmap.createBitmap(640, 960, Bitmap.Config.ARGB_8888)
        val displayName = "ALT-gallery-success-${UUID.randomUUID()}.jpg"
        var savedUri: Uri? = null
        try {
            bitmap.eraseColor(0xFF483264.toInt())
            sourceFile.outputStream().use { output ->
                assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output))
            }
            val originalBytes = sourceFile.readBytes()
            savedUri = GalleryMediaSaver.save(
                context = context,
                sourceUri = sourceUri(context, sourceFile),
                collectionUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                displayName = displayName,
                mimeType = "image/jpeg",
                relativePath = "Pictures/ALT"
            )
            assertEquals(
                1,
                matchingRowCount(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, displayName)
            )
            val savedBytes = context.contentResolver.openInputStream(savedUri)!!.use {
                it.readBytes()
            }
            assertArrayEquals(originalBytes, savedBytes)
        } finally {
            savedUri?.let { context.contentResolver.delete(it, null, null) }
            bitmap.recycle()
            sourceFile.delete()
        }
    }

    private fun verifyCancelledCopy(
        collection: Uri,
        mimeType: String,
        relativePath: String,
        extension: String
    ) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val sourceDir = File(context.cacheDir, "shares").apply {
            assertTrue(isDirectory || mkdirs())
        }
        val sourceFile = File(sourceDir, "gallery-cancel-${UUID.randomUUID()}.$extension")
        val displayName = "ALT-gallery-cancel-${UUID.randomUUID()}.$extension"
        var cancellationAfterWrite = false
        var checkpoints = 0
        try {
            // Large enough to exercise real ContentResolver stream writes.
            // The payload is deliberately unpublished, so metadata decoding
            // is neither necessary nor relied upon for this cancellation test.
            sourceFile.writeBytes(ByteArray(256 * 1024) { index ->
                (index * 17 + 31).toByte()
            })
            val failure = runCatching {
                GalleryMediaSaver.save(
                    context = context,
                    sourceUri = sourceUri(context, sourceFile),
                    collectionUri = collection,
                    displayName = displayName,
                    mimeType = mimeType,
                    relativePath = relativePath,
                    checkCancelled = {
                        checkpoints++
                        if (checkpoints == 5) {
                            // After: pre-insert, post-insert, pre-read, post-read,
                            // first stream write. The cancellation is not
                            // simply rejected before a MediaStore row exists.
                            cancellationAfterWrite = true
                            throw CancellationException("cancel after gallery copy started")
                        }
                    }
                )
            }.exceptionOrNull()
            assertTrue("Expected cancellation after one media stream write", cancellationAfterWrite)
            assertTrue("Expected cooperative cancellation", failure is CancellationException)
            assertEquals(
                "Cancelled media must not remain in the gallery (even as pending)",
                0,
                matchingRowCount(collection, displayName)
            )
            assertTrue("Private source file must remain intact", sourceFile.isFile)
            assertEquals(256L * 1024L, sourceFile.length())
        } finally {
            sourceFile.delete()
        }
    }

    private fun sourceUri(context: android.content.Context, file: File): Uri =
        FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            file
        )

    private fun matchingRowCount(collection: Uri, displayName: String): Int {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Collection queries hide pending rows by default. Include them so
        // an orphaned IS_PENDING=1 item cannot make this test falsely pass.
        @Suppress("DEPRECATION")
        val includingPending = MediaStore.setIncludePending(collection)
        return context.contentResolver.query(
            includingPending,
            arrayOf(MediaStore.MediaColumns._ID),
            "${MediaStore.MediaColumns.DISPLAY_NAME}=?",
            arrayOf(displayName),
            null
        )?.use { cursor -> cursor.count } ?: 0
    }
}

package com.alt.otherlives.core.data

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.math.min

/**
 * Test-APK-only ContentProvider. Serves a valid JPEG through a deliberately
 * stalled Android pipe and a separate >50 MiB stream for import limit checks.
 *
 * No original user photos, networking, or production APK code are involved.
 */
class SlowPhotoTestProvider : ContentProvider() {
    override fun onCreate(): Boolean = true

    override fun getType(uri: Uri): String = "image/jpeg"

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        require(mode == "r") { "The test provider is read-only" }
        val fileMode = requireNotNull(uri.lastPathSegment) { "Missing test stream mode" }
        require(fileMode == "slow-jpeg" || fileMode == "oversized") {
            "Unknown test stream mode"
        }

        val pipe = ParcelFileDescriptor.createPipe()
        Thread({
            try {
                ParcelFileDescriptor.AutoCloseOutputStream(pipe[1]).use { output ->
                    when (fileMode) {
                        "slow-jpeg" -> {
                            val pixels = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888)
                            val jpeg = try {
                                ByteArrayOutputStream().use { buffer ->
                                    check(pixels.compress(Bitmap.CompressFormat.JPEG, 90, buffer))
                                    buffer.toByteArray()
                                }
                            } finally {
                                pixels.recycle()
                            }
                            val initial = min(1024, jpeg.size / 2)
                            output.write(jpeg, 0, initial)
                            output.flush()
                            // Keep the reader inside a genuine ContentResolver pipe read
                            // while the test requests cancellation from another coroutine.
                            Thread.sleep(1_400)
                            output.write(jpeg, initial, jpeg.size - initial)
                        }
                        "oversized" -> {
                            val block = ByteArray(64 * 1024) { 0x53.toByte() }
                            repeat(51 * 1024 * 1024 / block.size) {
                                output.write(block)
                            }
                        }
                    }
                }
            } catch (_: IOException) {
                // The importer intentionally closes the read end on cancellation,
                // or on reaching the maximum byte count; EPIPE is expected here.
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }, "alt-slow-photo-test-stream").apply {
            isDaemon = true
            start()
        }
        return pipe[0]
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    companion object {
        const val AUTHORITY = "com.alt.otherlives.test.slowphotos"
    }
}

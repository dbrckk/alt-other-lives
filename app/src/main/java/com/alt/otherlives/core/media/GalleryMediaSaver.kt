package com.alt.otherlives.core.media

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.alt.otherlives.core.io.BoundedStreamCopy

/**
 * Publishes a private ALT export to Android MediaStore only after its bytes have
 * been copied in full. A cancelled or failed copy must never expose a partial
 * image/video in the gallery.
 *
 * This is a blocking Android ContentResolver operation. Call from Dispatchers.IO
 * and pass the owning coroutine's ensureActive() as [checkCancelled].
 */
internal object GalleryMediaSaver {
    private const val PENDING_MEDIA_EXPIRY_SECONDS = 24L * 60L * 60L

    fun save(
        context: Context,
        sourceUri: Uri,
        collectionUri: Uri,
        displayName: String,
        mimeType: String,
        relativePath: String,
        checkCancelled: () -> Unit = {}
    ): Uri {
        require(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "Gallery saving requires Android 10 or newer"
        }

        checkCancelled()
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
            put(
                MediaStore.MediaColumns.DATE_EXPIRES,
                System.currentTimeMillis() / 1000L + PENDING_MEDIA_EXPIRY_SECONDS
            )
        }
        val resolver = context.contentResolver
        val target = requireNotNull(resolver.insert(collectionUri, values)) {
            "Unable to create gallery item"
        }

        try {
            checkCancelled()
            val source = resolver.openInputStream(sourceUri)
                ?: error("Unable to open exported media")
            val copiedBytes = source.use { input ->
                val destination = resolver.openOutputStream(target)
                    ?: error("Unable to open gallery output")
                destination.use { output ->
                    BoundedStreamCopy.copy(
                        input = input,
                        output = output,
                        maxBytes = Long.MAX_VALUE,
                        checkCancelled = checkCancelled
                    )
                }
            }
            require(copiedBytes > 0L) { "Exported gallery media is empty" }

            // This is the only publication boundary. A cancellation detected
            // before or just after it rolls back the newly inserted row.
            checkCancelled()
            val publish = ContentValues().apply {
                put(MediaStore.MediaColumns.IS_PENDING, 0)
                putNull(MediaStore.MediaColumns.DATE_EXPIRES)
            }
            check(resolver.update(target, publish, null, null) > 0) {
                "Unable to finalize gallery item"
            }
            checkCancelled()
            return target
        } catch (failure: Throwable) {
            try {
                if (resolver.delete(target, null, null) < 1) {
                    failure.addSuppressed(
                        IllegalStateException("Unable to remove incomplete gallery item")
                    )
                }
            } catch (cleanupFailure: Throwable) {
                failure.addSuppressed(cleanupFailure)
            }
            throw failure
        }
    }
}

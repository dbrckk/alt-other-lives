package com.alt.otherlives.core.generation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Prepare personal photos and upload them off the UI thread. The prepared
 * temporary copy is always released, even on an upload failure or cancellation.
 *
 * Keeping preparation and upload in the same IO context avoids losing the
 * prepared file if a coroutine is cancelled between two dispatches.
 */
internal object PreparedSourceUpload {
    suspend fun <Source, Uploaded> execute(
        prepare: () -> Source,
        upload: suspend (Source) -> Uploaded,
        cleanup: (Source) -> Unit
    ): Uploaded = withContext(Dispatchers.IO) {
        val source = prepare()
        try {
            upload(source)
        } finally {
            cleanup(source)
        }
    }
}

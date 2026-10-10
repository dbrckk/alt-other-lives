package com.alt.otherlives.core.generation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
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
        val preparationContext = currentCoroutineContext()
        preparationContext.ensureActive()
        val source = prepare()
        try {
            // If cancellation happened during synchronous image preparation,
            // release the prepared file without attempting any remote upload.
            preparationContext.ensureActive()
            upload(source)
        } finally {
            cleanup(source)
        }
    }
}

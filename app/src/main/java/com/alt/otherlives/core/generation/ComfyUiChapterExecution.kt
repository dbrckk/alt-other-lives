package com.alt.otherlives.core.generation

/**
 * One acknowledged ComfyUI prompt represents one remote GPU job.
 *
 * Once [queue] has been attempted, retrying the entire chapter could submit
 * duplicate jobs if the server accepted the request but the response or a
 * later history/image request failed. The client already retries transient
 * history polling and image downloads against their original identifiers.
 *
 * This operation deliberately never requeues on any failure. The user may
 * explicitly retry a failed chapter.
 */
internal object ComfyUiChapterExecution {
    suspend fun <Output, Result> execute(
        queue: suspend () -> String,
        await: suspend (promptId: String) -> Output,
        download: suspend (output: Output) -> Result
    ): Result {
        val promptId = queue()
        val output = await(promptId)
        return download(output)
    }
}

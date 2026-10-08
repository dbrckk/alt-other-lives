package com.alt.otherlives.core.generation

/**
 * Rejects obviously invalid or oversized ComfyUI responses before a download
 * touches disk. The streamed byte limit remains authoritative because HTTP
 * servers may omit or misreport Content-Length.
 */
internal object ComfyUiDownloadResponsePolicy {
    fun validate(
        contentLengthBytes: Long,
        contentType: String?,
        maxBytes: Long
    ) {
        require(maxBytes > 0L) { "Maximum generated image size must be positive" }

        if (contentLengthBytes == 0L) {
            throw GeneratedSceneQualityException("ComfyUI returned an empty image")
        }
        if (contentLengthBytes > maxBytes) {
            throw GeneratedSceneQualityException("ComfyUI image exceeds the maximum download size")
        }

        val mediaType = contentType
            ?.substringBefore(';')
            ?.trim()
            ?.lowercase()
            .orEmpty()
        if (mediaType.isNotEmpty() &&
            !mediaType.startsWith("image/") &&
            mediaType != "application/octet-stream" &&
            mediaType != "application/x-octet-stream" &&
            mediaType != "binary/octet-stream"
        ) {
            throw GeneratedSceneQualityException("ComfyUI returned a non-image response")
        }
    }
}

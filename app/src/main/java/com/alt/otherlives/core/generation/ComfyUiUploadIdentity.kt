package com.alt.otherlives.core.generation

import com.alt.otherlives.core.media.NormalizedCropRect

/**
 * Names a sanitized upload using the original app-private photo identity,
 * selected framing and configured server, never the random temporary JPEG URI.
 *
 * The returned key is hashed by ComfyUiClient.sourceUploadFileName before
 * reaching ComfyUI. Keeping the key stable lets repeated runs replace the
 * server-side input instead of accumulating a new file each time.
 */
internal object ComfyUiUploadIdentity {
    fun stableSourceKey(
        endpoint: String,
        sourcePhotoUri: String,
        crop: NormalizedCropRect
    ): String {
        require(endpoint.isNotBlank()) { "ComfyUI endpoint is required" }
        require(sourcePhotoUri.isNotBlank()) { "Source photo URI is required" }

        return buildString {
            append("sanitized-jpeg-v1")
            append('\u0000')
            append(endpoint.trim().trimEnd('/'))
            append('\u0000')
            append(sourcePhotoUri)
            for (coordinate in listOf(crop.left, crop.top, crop.right, crop.bottom)) {
                append('\u0000')
                append(coordinate.toBits())
            }
        }
    }
}

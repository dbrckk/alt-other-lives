package com.alt.otherlives.core.generation

import org.junit.Assert.assertThrows
import org.junit.Test

class ComfyUiDownloadResponsePolicyTest {
    private val maxBytes = 100L * 1024L * 1024L

    @Test
    fun acceptsNormalImageWithKnownSize() {
        ComfyUiDownloadResponsePolicy.validate(
            contentLengthBytes = 1_500_000L,
            contentType = "image/png; charset=binary",
            maxBytes = maxBytes
        )
    }

    @Test
    fun acceptsUnknownSizeAndContentTypeForProxyCompatibility() {
        ComfyUiDownloadResponsePolicy.validate(
            contentLengthBytes = -1L,
            contentType = null,
            maxBytes = maxBytes
        )
        ComfyUiDownloadResponsePolicy.validate(
            contentLengthBytes = -1L,
            contentType = "application/octet-stream",
            maxBytes = maxBytes
        )
    }

    @Test
    fun acceptsMaximumDeclaredImageSize() {
        ComfyUiDownloadResponsePolicy.validate(
            contentLengthBytes = maxBytes,
            contentType = "IMAGE/JPEG",
            maxBytes = maxBytes
        )
    }

    @Test
    fun rejectsOversizedResponseBeforeStreaming() {
        assertThrows(GeneratedSceneQualityException::class.java) {
            ComfyUiDownloadResponsePolicy.validate(
                contentLengthBytes = maxBytes + 1L,
                contentType = "image/png",
                maxBytes = maxBytes
            )
        }
    }

    @Test
    fun rejectsExplicitEmptyResponse() {
        assertThrows(GeneratedSceneQualityException::class.java) {
            ComfyUiDownloadResponsePolicy.validate(
                contentLengthBytes = 0L,
                contentType = "image/png",
                maxBytes = maxBytes
            )
        }
    }

    @Test
    fun rejectsHtmlErrorPageReturnedWithSuccessStatus() {
        assertThrows(GeneratedSceneQualityException::class.java) {
            ComfyUiDownloadResponsePolicy.validate(
                contentLengthBytes = 3_000L,
                contentType = "text/html; charset=utf-8",
                maxBytes = maxBytes
            )
        }
    }

    @Test
    fun rejectsJsonFailureReturnedWithSuccessStatus() {
        assertThrows(GeneratedSceneQualityException::class.java) {
            ComfyUiDownloadResponsePolicy.validate(
                contentLengthBytes = -1L,
                contentType = "application/json",
                maxBytes = maxBytes
            )
        }
    }

    @Test
    fun acceptsBinaryContentTypesUsedByImageProxies() {
        for (mimeType in listOf("binary/octet-stream", "application/x-octet-stream")) {
            ComfyUiDownloadResponsePolicy.validate(
                contentLengthBytes = 2_500L,
                contentType = mimeType,
                maxBytes = maxBytes
            )
        }
    }

    @Test
    fun rejectsNonPositiveMaximum() {
        assertThrows(IllegalArgumentException::class.java) {
            ComfyUiDownloadResponsePolicy.validate(
                contentLengthBytes = -1L,
                contentType = "image/png",
                maxBytes = 0L
            )
        }
    }
}

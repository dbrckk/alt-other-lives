package com.alt.otherlives.core.generation

import org.junit.Assert.assertEquals
import org.junit.Test

class ComfyUiSourceMimeTypeTest {
    @Test
    fun normalizesValidImageMimeType() {
        assertEquals(
            "image/jpeg",
            ComfyUiClient.requireImageMimeType(" Image/JPEG ; charset=binary ")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsMissingMimeType() {
        ComfyUiClient.requireImageMimeType(null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNonImageMimeType() {
        ComfyUiClient.requireImageMimeType("application/octet-stream")
    }
}

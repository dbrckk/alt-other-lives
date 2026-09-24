package com.alt.otherlives.core.generation

import org.junit.Assert.assertEquals
import org.junit.Test

class GeneratedImageExtensionTest {
    @Test
    fun prefersAllowedMimeExtension() {
        assertEquals(
            "webp",
            GeneratedImageExtension.normalize(
                mimeExtension = "WEBP",
                filename = "scene.png"
            )
        )
    }

    @Test
    fun fallsBackToAllowedFilenameExtension() {
        assertEquals(
            "jpg",
            GeneratedImageExtension.normalize(
                mimeExtension = null,
                filename = "scene.JPG"
            )
        )
    }

    @Test
    fun rejectsUnexpectedFilenameExtension() {
        assertEquals(
            "png",
            GeneratedImageExtension.normalize(
                mimeExtension = null,
                filename = "scene.png.exe"
            )
        )
    }

    @Test
    fun rejectsUnexpectedMimeExtension() {
        assertEquals(
            "png",
            GeneratedImageExtension.normalize(
                mimeExtension = "../tmp",
                filename = "scene"
            )
        )
    }
}

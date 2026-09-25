package com.alt.otherlives.core.generation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ComfyUiSourceUploadNameTest {
    @Test
    fun sameSourceReusesSameRemoteName() {
        val first = ComfyUiClient.sourceUploadFileName(
            sourceKey = "content://com.alt.otherlives.fileprovider/source-photos/source-abc.jpg",
            extension = "jpg"
        )
        val second = ComfyUiClient.sourceUploadFileName(
            sourceKey = "content://com.alt.otherlives.fileprovider/source-photos/source-abc.jpg",
            extension = "jpg"
        )

        assertEquals(first, second)
    }

    @Test
    fun differentSourcesDoNotShareRemoteName() {
        val first = ComfyUiClient.sourceUploadFileName("source-a", "png")
        val second = ComfyUiClient.sourceUploadFileName("source-b", "png")

        assertNotEquals(first, second)
    }

    @Test
    fun uploadNameDoesNotExposeSourcePath() {
        val name = ComfyUiClient.sourceUploadFileName(
            sourceKey = "content://provider/private/source-secret-person.jpg",
            extension = "jpeg"
        )

        assertTrue(name.startsWith("alt-source-"))
        assertTrue(name.endsWith(".jpeg"))
        assertTrue(!name.contains("secret-person"))
    }

    @Test
    fun unsafeExtensionFallsBackToJpeg() {
        val name = ComfyUiClient.sourceUploadFileName(
            sourceKey = "source-a",
            extension = "../png"
        )

        assertTrue(name.endsWith(".jpg"))
    }
}

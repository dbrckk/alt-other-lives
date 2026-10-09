package com.alt.otherlives.core.generation

import com.alt.otherlives.core.media.NormalizedCropRect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ComfyUiUploadIdentityTest {
    private val photoUri = "content://com.alt.otherlives.fileprovider/source-photos/private-uuid.jpg"
    private val server = "https://example.com/comfy"

    private fun filename(
        uri: String = photoUri,
        endpoint: String = server,
        crop: NormalizedCropRect = NormalizedCropRect.Full
    ): String = ComfyUiClient.sourceUploadFileName(
        sourceKey = ComfyUiUploadIdentity.stableSourceKey(
            endpoint = endpoint,
            sourcePhotoUri = uri,
            crop = crop
        ),
        extension = "jpg"
    )

    @Test
    fun repeatedGenerationsOfSamePhotoReuseRemoteFileName() {
        val first = filename()
        val second = filename()
        assertEquals(first, second)
        assertEquals(43, first.length)
    }

    @Test
    fun privatePhotoPathNeverAppearsInRemoteFilename() {
        val name = filename()
        assertTrue(name.startsWith("alt-source-"))
        assertFalse(name.contains("private-uuid"))
        assertFalse(name.contains("example.com"))
    }

    @Test
    fun changingPhotoOrCropDoesNotOverwriteAnotherInput() {
        val original = filename()
        assertNotEquals(original, filename(uri = photoUri.replace("uuid", "other")))
        assertNotEquals(
            original,
            filename(crop = NormalizedCropRect(0.1f, 0.1f, 0.9f, 0.9f))
        )
        assertNotEquals(
            filename(crop = NormalizedCropRect(0.1f, 0.1f, 0.9f, 0.9f)),
            filename(crop = NormalizedCropRect(0.1f, 0.11f, 0.9f, 0.9f))
        )
    }

    @Test
    fun changingComfyUiServerDoesNotReuseCrossServerIdentity() {
        assertNotEquals(
            filename(endpoint = "https://first.example.com"),
            filename(endpoint = "https://second.example.com")
        )
    }

    @Test
    fun trailingEndpointSlashDoesNotChangeName() {
        assertEquals(filename(), filename(endpoint = server + "/"))
    }

    @Test
    fun emptyIdentifiersAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            ComfyUiUploadIdentity.stableSourceKey("", photoUri, NormalizedCropRect.Full)
        }
        assertThrows(IllegalArgumentException::class.java) {
            ComfyUiUploadIdentity.stableSourceKey(server, "", NormalizedCropRect.Full)
        }
    }
}

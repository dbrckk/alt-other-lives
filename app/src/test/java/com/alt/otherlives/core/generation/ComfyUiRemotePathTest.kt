package com.alt.otherlives.core.generation

import org.junit.Assert.assertEquals
import org.junit.Test

class ComfyUiRemotePathTest {
    @Test
    fun acceptsCanonicalFilenameAndSubfolder() {
        assertEquals(
            "scene.png",
            ComfyUiRemotePath.validateFilename("scene.png")
        )
        assertEquals(
            "outputs/alt",
            ComfyUiRemotePath.validateSubfolder("outputs/alt")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsFilenameTraversal() {
        ComfyUiRemotePath.validateFilename("../scene.png")
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsSubfolderTraversal() {
        ComfyUiRemotePath.validateSubfolder("outputs/../private")
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsAbsoluteSubfolder() {
        ComfyUiRemotePath.validateSubfolder("/tmp")
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsOversizedFilename() {
        ComfyUiRemotePath.validateFilename("a".repeat(256))
    }
    @Test
    fun normalizesKnownImageTypes() {
        assertEquals("output", ComfyUiRemotePath.validateType(" OUTPUT "))
        assertEquals("temp", ComfyUiRemotePath.validateType("temp"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsUnknownImageType() {
        ComfyUiRemotePath.validateType("../../etc")
    }
}


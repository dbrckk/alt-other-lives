package com.alt.otherlives.core.generation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GenerationDownloadCacheTest {
    @Test
    fun acceptsGeneratedDownloadProviderPath() {
        assertEquals(
            "scene-abc-0.png",
            GenerationDownloadCache.ownedFileName(
                listOf("generated_downloads", "scene-abc-0.png")
            )
        )
    }

    @Test
    fun rejectsShareCachePath() {
        assertNull(
            GenerationDownloadCache.ownedFileName(
                listOf("shared_images", "scene-abc-0.png")
            )
        )
    }

    @Test
    fun rejectsNonSceneFile() {
        assertNull(
            GenerationDownloadCache.ownedFileName(
                listOf("generated_downloads", "other.png")
            )
        )
    }

    @Test
    fun rejectsMissingFilename() {
        assertNull(
            GenerationDownloadCache.ownedFileName(
                listOf("generated_downloads")
            )
        )
    }
}

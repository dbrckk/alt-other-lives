package com.alt.otherlives.core.generation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GeneratedSceneStoreIndexingTest {
    @Test
    fun filenameRoundTripsChapterIndex() {
        assertEquals("scene-4.png", GeneratedSceneFileName.forChapter(4))
        assertEquals(4, GeneratedSceneFileName.chapterIndex("scene-4.png"))
    }

    @Test
    fun supportsNonPngExtensions() {
        assertEquals("scene-2.webp", GeneratedSceneFileName.forChapter(2, "webp"))
        assertEquals(2, GeneratedSceneFileName.chapterIndex("scene-2.webp"))
    }

    @Test
    fun rejectsMalformedOrNegativeChapterNames() {
        assertNull(GeneratedSceneFileName.chapterIndex("scene-x.png"))
        assertNull(GeneratedSceneFileName.chapterIndex("scene--1.png"))
        assertNull(GeneratedSceneFileName.chapterIndex("not-a-scene-2.png"))
    }

    @Test
    fun rejectsOutOfRangeChapterNames() {
        assertNull(GeneratedSceneFileName.chapterIndex("scene-5.png"))
        assertNull(GeneratedSceneFileName.chapterIndex("scene-999.png"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun refusesOutOfRangeChapterIndexes() {
        GeneratedSceneFileName.forChapter(5)
    }

    @Test
    fun normalizesUppercaseSupportedExtension() {
        assertEquals(
            "scene-1.webp",
            GeneratedSceneFileName.forChapter(1, "WEBP")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsUnsupportedExtensions() {
        GeneratedSceneFileName.forChapter(1, "gif")
    }

    @Test(expected = IllegalArgumentException::class)
    fun refusesNegativeChapterIndexes() {
        GeneratedSceneFileName.forChapter(-1)
    }
}

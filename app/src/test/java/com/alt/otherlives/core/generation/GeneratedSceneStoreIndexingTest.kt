package com.alt.otherlives.core.generation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GeneratedSceneStoreIndexingTest {
    @Test
    fun filenameRoundTripsChapterIndex() {
        val store = object {
            fun filenameForChapter(chapterIndex: Int): String = "scene-" + chapterIndex + ".png"
            fun chapterIndexFromFilename(name: String): Int? = name
                .substringAfter("scene-")
                .substringBefore(".")
                .toIntOrNull()
        }

        assertEquals("scene-4.png", store.filenameForChapter(4))
        assertEquals(4, store.chapterIndexFromFilename("scene-4.png"))
        assertNull(store.chapterIndexFromFilename("scene-x.png"))
    }
}

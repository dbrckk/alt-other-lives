package com.alt.otherlives.core.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SourcePhotoFileNameTest {
    @Test
    fun acceptsUuidBackedSourcePhoto() {
        assertTrue(
            SourcePhotoFileName.isValid(
                "source-550e8400-e29b-41d4-a716-446655440000.webp"
            )
        )
    }

    @Test
    fun acceptsLegacyNumericSourcePhoto() {
        assertTrue(SourcePhotoFileName.isValid("source-1725000000000.jpg"))
    }

    @Test
    fun rejectsPathTraversal() {
        assertFalse(SourcePhotoFileName.isValid("../source-example.jpg"))
    }

    @Test
    fun rejectsDataStoreFieldSeparator() {
        assertFalse(SourcePhotoFileName.isValid("source-example|other.jpg"))
    }

    @Test
    fun rejectsWhitespaceAndMissingExtension() {
        assertFalse(SourcePhotoFileName.isValid("source example.jpg"))
        assertFalse(SourcePhotoFileName.isValid("source-example"))
    }
}

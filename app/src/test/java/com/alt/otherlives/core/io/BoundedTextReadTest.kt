package com.alt.otherlives.core.io

import java.io.StringReader
import org.junit.Assert.assertEquals
import org.junit.Test

class BoundedTextReadTest {
    @Test
    fun readsTextWithinLimit() {
        assertEquals(
            "hello",
            BoundedTextRead.read(StringReader("hello"), maxChars = 5)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsTextAboveLimit() {
        BoundedTextRead.read(
            StringReader("123456"),
            maxChars = 5,
            bufferSize = 2
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNonPositiveLimits() {
        BoundedTextRead.read(StringReader("x"), maxChars = 0)
    }
}

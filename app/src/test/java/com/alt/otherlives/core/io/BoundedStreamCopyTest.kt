package com.alt.otherlives.core.io

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class BoundedStreamCopyTest {
    @Test
    fun copiesWithinLimit() {
        val source = byteArrayOf(1, 2, 3, 4)
        val output = ByteArrayOutputStream()

        val copied = BoundedStreamCopy.copy(
            input = ByteArrayInputStream(source),
            output = output,
            maxBytes = 4
        )

        assertEquals(4L, copied)
        assertArrayEquals(source, output.toByteArray())
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsDataBeyondLimit() {
        BoundedStreamCopy.copy(
            input = ByteArrayInputStream(ByteArray(5)),
            output = ByteArrayOutputStream(),
            maxBytes = 4
        )
    }
}

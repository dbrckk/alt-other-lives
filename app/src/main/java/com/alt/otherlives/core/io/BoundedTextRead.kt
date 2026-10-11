package com.alt.otherlives.core.io

import java.io.Reader

internal object BoundedTextRead {
    fun read(
        reader: Reader,
        maxChars: Int,
        bufferSize: Int = DEFAULT_BUFFER_SIZE,
        checkCancelled: () -> Unit = {}
    ): String {
        require(maxChars > 0) { "Maximum text size must be positive" }
        require(bufferSize > 0) { "Buffer size must be positive" }

        val buffer = CharArray(bufferSize)
        val result = StringBuilder(minOf(maxChars, bufferSize))
        var total = 0

        while (true) {
            // A socket read may block until its read timeout, but cancellation
            // is observed before processing or reading the next response chunk.
            checkCancelled()
            val read = reader.read(buffer)
            checkCancelled()
            if (read < 0) break
            if (read == 0) continue
            total += read
            require(total <= maxChars) { "Text response exceeds maximum allowed size" }
            result.append(buffer, 0, read)
        }

        return result.toString()
    }
}

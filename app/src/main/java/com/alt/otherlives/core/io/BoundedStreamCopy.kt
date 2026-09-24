package com.alt.otherlives.core.io

import java.io.InputStream
import java.io.OutputStream

internal object BoundedStreamCopy {
    fun copy(
        input: InputStream,
        output: OutputStream,
        maxBytes: Long,
        bufferSize: Int = DEFAULT_BUFFER_SIZE
    ): Long {
        require(maxBytes > 0L) { "Maximum copy size must be positive" }
        require(bufferSize > 0) { "Buffer size must be positive" }

        val buffer = ByteArray(bufferSize)
        var total = 0L
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            if (read == 0) continue
            total += read
            require(total <= maxBytes) { "File exceeds maximum allowed size" }
            output.write(buffer, 0, read)
        }
        return total
    }
}

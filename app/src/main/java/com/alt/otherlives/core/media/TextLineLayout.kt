package com.alt.otherlives.core.media

internal object TextLineLayout {
    fun layout(
        text: String,
        maxWidth: Float,
        maxLines: Int,
        measure: (String) -> Float
    ): List<String> {
        require(maxWidth > 0f) { "maxWidth must be positive" }
        require(maxLines > 0) { "maxLines must be positive" }

        val words = text.trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
        if (words.isEmpty()) return emptyList()

        val lines = mutableListOf<String>()
        var line = ""
        var wordIndex = 0

        while (wordIndex < words.size && lines.size < maxLines) {
            val word = words[wordIndex]
            val candidate = if (line.isEmpty()) word else "$line $word"

            if (measure(candidate) <= maxWidth) {
                line = candidate
                wordIndex += 1
                continue
            }

            if (line.isEmpty()) {
                line = ellipsize(word, maxWidth, measure)
                wordIndex += 1
            } else {
                val isLastLine = lines.size == maxLines - 1
                lines += if (isLastLine) {
                    ellipsize("$line…", maxWidth, measure)
                } else {
                    line
                }
                line = ""
            }
        }

        if (line.isNotEmpty() && lines.size < maxLines) {
            val hasMore = wordIndex < words.size
            lines += if (hasMore) {
                ellipsize("$line…", maxWidth, measure)
            } else {
                line
            }
        }

        if (wordIndex < words.size && lines.isNotEmpty()) {
            val last = lines.last()
            if (!last.endsWith("…")) {
                lines[lines.lastIndex] = ellipsize("$last…", maxWidth, measure)
            }
        }

        return lines
    }

    fun ellipsize(
        text: String,
        maxWidth: Float,
        measure: (String) -> Float
    ): String {
        require(maxWidth > 0f) { "maxWidth must be positive" }
        if (measure(text) <= maxWidth) return text

        val suffix = "…"
        var value = text.removeSuffix(suffix)
        while (value.isNotEmpty() && measure(value + suffix) > maxWidth) {
            value = value.dropLast(1)
        }
        return if (value.isEmpty()) suffix else value + suffix
    }
}

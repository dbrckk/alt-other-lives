package com.alt.otherlives.core.generation

internal object GeneratedImageExtension {
    private val allowed = setOf("jpg", "jpeg", "png", "webp")

    fun normalize(
        mimeExtension: String?,
        filename: String?
    ): String {
        val fromMime = mimeExtension
            ?.trim()
            ?.lowercase()
            ?.takeIf { it in allowed }
        if (fromMime != null) return fromMime

        val fromName = filename
            ?.substringAfterLast(".", "")
            ?.trim()
            ?.lowercase()
            ?.takeIf { it in allowed }

        return fromName ?: "png"
    }
}

package com.alt.otherlives.core.generation

internal object GeneratedSceneFileName {
    fun forChapter(chapterIndex: Int, extension: String = "png"): String {
        require(chapterIndex >= 0) { "Chapter index must be non-negative" }
        val normalizedExtension = GeneratedImageExtension.normalize(
            mimeExtension = extension,
            filename = null
        )
        require(normalizedExtension == extension.trim().lowercase()) {
            "Unsupported generated scene extension"
        }
        return "scene-" + chapterIndex + "." + normalizedExtension
    }

    fun chapterIndex(name: String): Int? = name
        .takeIf { it.startsWith("scene-") }
        ?.substringAfter("scene-")
        ?.substringBefore(".")
        ?.toIntOrNull()
        ?.takeIf { it >= 0 }
}

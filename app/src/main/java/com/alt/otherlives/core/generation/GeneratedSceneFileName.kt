package com.alt.otherlives.core.generation

internal object GeneratedSceneFileName {
    fun forChapter(chapterIndex: Int, extension: String = "png"): String {
        require(chapterIndex >= 0) { "Chapter index must be non-negative" }
        require(extension.isNotBlank()) { "Extension is required" }
        return "scene-" + chapterIndex + "." + extension
    }

    fun chapterIndex(name: String): Int? = name
        .takeIf { it.startsWith("scene-") }
        ?.substringAfter("scene-")
        ?.substringBefore(".")
        ?.toIntOrNull()
        ?.takeIf { it >= 0 }
}

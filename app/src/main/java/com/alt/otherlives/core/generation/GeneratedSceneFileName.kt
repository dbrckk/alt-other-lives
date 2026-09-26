package com.alt.otherlives.core.generation

internal object GeneratedSceneFileName {
    fun forChapter(chapterIndex: Int, extension: String = "png"): String {
        require(SceneBatchValidation.isSupportedChapterIndex(chapterIndex)) {
            "Chapter index is outside the supported timeline range"
        }
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
        ?.takeIf(SceneBatchValidation::isSupportedChapterIndex)
}

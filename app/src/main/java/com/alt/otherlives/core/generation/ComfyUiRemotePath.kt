package com.alt.otherlives.core.generation

internal object ComfyUiRemotePath {
    private const val MAX_FILENAME_LENGTH = 255
    private const val MAX_SUBFOLDER_LENGTH = 512
    private val allowedTypes = setOf("input", "output", "temp")

    fun validateFilename(value: String): String {
        require(value.isNotBlank()) { "ComfyUI image filename is required" }
        require(value.length <= MAX_FILENAME_LENGTH) { "ComfyUI image filename is too long" }
        require(!value.contains('/') && !value.contains('\\')) {
            "ComfyUI image filename must not contain path separators"
        }
        require(value != "." && value != "..") {
            "ComfyUI image filename is invalid"
        }
        require(value.none { it.isISOControl() }) {
            "ComfyUI image filename contains control characters"
        }
        return value
    }

    fun validateType(value: String): String {
        val normalized = value.trim().lowercase()
        require(normalized in allowedTypes) {
            "ComfyUI image type is invalid"
        }
        return normalized
    }

    fun validateSubfolder(value: String): String {
        if (value.isBlank()) return ""
        require(value.length <= MAX_SUBFOLDER_LENGTH) {
            "ComfyUI image subfolder is too long"
        }
        require(!value.startsWith("/") && !value.startsWith("\\")) {
            "ComfyUI image subfolder must be relative"
        }
        require(value.none { it.isISOControl() }) {
            "ComfyUI image subfolder contains control characters"
        }
        val segments = value.replace('\\', '/').split('/')
        require(segments.none { it == "." || it == ".." }) {
            "ComfyUI image subfolder contains traversal segments"
        }
        return segments.filter { it.isNotBlank() }.joinToString("/")
    }
}

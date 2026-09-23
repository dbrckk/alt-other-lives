package com.alt.otherlives.core.generation

data class ComfyUiConfig(
    val baseUrl: String,
    val clientId: String = "alt-android"
) {
    val normalizedBaseUrl: String
        get() = baseUrl.trim().removeSuffix("/")

    fun validate() {
        require(normalizedBaseUrl.startsWith("http://") || normalizedBaseUrl.startsWith("https://")) {
            "ComfyUI base URL must use http or https"
        }
    }
}

object ComfyUiWorkflow {
    const val PLACEHOLDER_SOURCE_IMAGE = "__ALT_SOURCE_IMAGE__"
    const val PLACEHOLDER_PROMPT = "__ALT_PROMPT__"

    fun promptFor(scenarioTitle: String, chapterLabel: String, chapterNarrative: String): String =
        buildString {
            append("cinematic portrait photography, same person and facial identity, ")
            append("premium editorial lighting, realistic skin, coherent anatomy, ")
            append("vertical 9:16 composition, no text, no watermark, ")
            append("alternate life scenario: ")
            append(scenarioTitle)
            append(". chapter: ")
            append(chapterLabel)
            append(". ")
            append(chapterNarrative)
        }
}

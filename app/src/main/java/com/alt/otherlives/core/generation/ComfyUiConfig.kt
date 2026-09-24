package com.alt.otherlives.core.generation

data class ComfyUiConfig(
    val baseUrl: String,
    val clientId: String = "alt-android"
) {
    val normalizedBaseUrl: String
        get() = baseUrl.trim().removeSuffix("/")

    fun validate() {
        require(normalizedBaseUrl.startsWith("https://")) {
            "ComfyUI base URL must use HTTPS. For a local server, expose it through a secure HTTPS tunnel instead of plain HTTP."
        }
    }
}

object ComfyUiWorkflow {
    const val PLACEHOLDER_SOURCE_IMAGE = "__ALT_SOURCE_IMAGE__"
    const val PLACEHOLDER_PROMPT = "__ALT_PROMPT__"
    const val PLACEHOLDER_SEED = "__ALT_SEED__"

    fun promptFor(
        scenarioId: String,
        scenarioTitle: String,
        chapterLabel: String,
        chapterNarrative: String,
        chapterIndex: Int
    ): String =
        buildString {
            append("cinematic portrait photography, same person and facial identity, ")
            append("use the source photo as the immutable identity anchor for every chapter, ")
            append("preserve identity-defining facial geometry, eye color, skin tone, hairline and hair texture, ")
            append("preserve apparent gender presentation and distinctive facial features unless the narrative explicitly requires a change, ")
            append("no identity swap, no lookalike drift, no face replacement, ")
            append("age progression only when narratively justified and always recognizably the same person, ")
            append("premium editorial lighting, realistic skin, coherent anatomy, ")
            append("vertical 9:16 composition, no text, no watermark, ")
            append("cinematic continuity, consistent lens language and premium color grading across the sequence, ")
            append("natural depth of field, ")
            append(styleForScenario(scenarioId))
            append(", sequence chapter ")
            append(chapterIndex + 1)
            append(", alternate life scenario: ")
            append(scenarioTitle)
            append(". chapter: ")
            append(chapterLabel)
            append(". ")
            append(chapterNarrative)
        }

    private fun styleForScenario(scenarioId: String): String = when (scenarioId) {
        "wealth" -> "luxury editorial realism, restrained wealth, architectural interiors, quiet confidence"
        "japan" -> "contemporary Japan, authentic urban details, natural neon ambience, documentary realism"
        "2100" -> "credible near-future design, elegant advanced technology, grounded science-fiction realism"
        "disappear" -> "introspective travel cinema, remote landscapes, weathered realism, contemplative atmosphere"
        "famous" -> "celebrity editorial photography, red carpet and backstage realism, controlled flash lighting"
        "mars" -> "hard-science Mars habitat realism, cinematic EVA lighting, physically plausible environment"
        "artist" -> "high-end art editorial, studio textures, gallery atmosphere, expressive but realistic lighting"
        "restart" -> "minimalist reinvention, grounded lifestyle editorial, calm natural light, hopeful realism"
        else -> "premium cinematic editorial realism"
    }
}

package com.alt.otherlives.core.generation

internal object ComfyUiPromptId {
    private val allowed = Regex("^[A-Za-z0-9_-]{1,128}$")

    fun validate(value: String): String {
        require(allowed.matches(value)) { "ComfyUI returned an invalid prompt_id" }
        return value
    }
}

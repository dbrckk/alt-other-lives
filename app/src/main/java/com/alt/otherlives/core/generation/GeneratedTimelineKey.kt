package com.alt.otherlives.core.generation

internal object GeneratedTimelineKey {
    private val allowed = Regex("^[A-Za-z0-9._-]+$")

    fun validate(value: String): String {
        require(value.isNotBlank()) { "Timeline key is required" }
        require(allowed.matches(value)) { "Invalid timeline key" }
        require(value != "." && value != "..") { "Invalid timeline key" }
        require(!value.contains("..")) { "Timeline key cannot contain traversal segments" }
        return value
    }
}

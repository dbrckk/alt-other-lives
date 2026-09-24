package com.alt.otherlives.core.data

internal object SourcePhotoFileName {
    private val pattern = Regex("^source-[A-Za-z0-9-]{1,80}\\.[A-Za-z0-9]{1,10}$")

    fun isValid(value: String): Boolean =
        value.length <= 110 && pattern.matches(value)
}

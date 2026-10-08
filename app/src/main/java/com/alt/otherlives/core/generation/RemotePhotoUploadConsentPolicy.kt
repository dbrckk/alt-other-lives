package com.alt.otherlives.core.generation

/**
 * Upload consent belongs to a specific configured server, not to the app globally.
 * Changing an existing endpoint always requires a separate save and fresh consent.
 */
internal object RemotePhotoUploadConsentPolicy {
    fun endpointChanged(previousBaseUrl: String, nextBaseUrl: String): Boolean {
        val previous = ComfyUiConfig(previousBaseUrl).normalizedBaseUrl
        if (previous.isBlank()) return false

        return previous != ComfyUiConfig(nextBaseUrl).normalizedBaseUrl
    }

    fun consentForSave(
        previousBaseUrl: String,
        nextBaseUrl: String,
        requestedConsent: Boolean
    ): Boolean = requestedConsent && !endpointChanged(previousBaseUrl, nextBaseUrl)
}

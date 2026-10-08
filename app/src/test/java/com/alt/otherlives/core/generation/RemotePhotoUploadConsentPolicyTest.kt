package com.alt.otherlives.core.generation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RemotePhotoUploadConsentPolicyTest {
    @Test
    fun consentIsRetainedForAnUnchangedEndpoint() {
        assertTrue(
            RemotePhotoUploadConsentPolicy.consentForSave(
                previousBaseUrl = "https://comfy.example/proxy",
                nextBaseUrl = "https://comfy.example/proxy",
                requestedConsent = true
            )
        )
    }

    @Test
    fun harmlessWhitespaceAndTrailingSlashesDoNotResetConsent() {
        assertTrue(
            RemotePhotoUploadConsentPolicy.consentForSave(
                previousBaseUrl = "https://comfy.example/proxy/",
                nextBaseUrl = "  https://comfy.example/proxy///  ",
                requestedConsent = true
            )
        )
    }

    @Test
    fun switchingToAnotherServerClearsEvenRequestedConsent() {
        assertFalse(
            RemotePhotoUploadConsentPolicy.consentForSave(
                previousBaseUrl = "https://original.example",
                nextBaseUrl = "https://new.example",
                requestedConsent = true
            )
        )
    }

    @Test
    fun switchingToAnotherPathAlsoClearsConsent() {
        assertTrue(
            RemotePhotoUploadConsentPolicy.endpointChanged(
                previousBaseUrl = "https://comfy.example/private",
                nextBaseUrl = "https://comfy.example/other"
            )
        )
    }

    @Test
    fun firstTimeConfigurationCanBeConsentedToExplicitly() {
        assertTrue(
            RemotePhotoUploadConsentPolicy.consentForSave(
                previousBaseUrl = "",
                nextBaseUrl = "https://comfy.example",
                requestedConsent = true
            )
        )
    }

    @Test
    fun userWithdrawalRemainsEffectiveOnSameServer() {
        assertFalse(
            RemotePhotoUploadConsentPolicy.consentForSave(
                previousBaseUrl = "https://comfy.example",
                nextBaseUrl = "https://comfy.example",
                requestedConsent = false
            )
        )
    }
}

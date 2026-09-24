package com.alt.otherlives.core.generation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerationSettingsTest {
    @Test
    fun emptySettingsAreNotPersistedOrConfigured() {
        val settings = GenerationSettings()

        assertFalse(settings.hasPersistedValues)
        assertFalse(settings.isConfigured)
        assertNull(settings.validationError)
    }

    @Test
    fun invalidPersistedSettingsRemainEditableButBlocked() {
        val settings = GenerationSettings(
            comfyUiBaseUrl = "http://invalid",
            workflowJson = "{invalid}",
            isConfigured = false,
            validationError = "Workflow JSON is malformed"
        )

        assertTrue(settings.hasPersistedValues)
        assertFalse(settings.isConfigured)
        assertTrue(settings.validationError!!.isNotBlank())
    }
}

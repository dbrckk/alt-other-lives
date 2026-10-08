package com.alt.otherlives.feature.settings

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsDraftStateContractTest {
    @Test
    fun editableSettingsUseSaveableState() {
        val source = File(
            "src/main/java/com/alt/otherlives/feature/settings/GenerationSettingsScreen.kt"
        ).readText()

        assertTrue(source.contains("import androidx.compose.runtime.saveable.rememberSaveable"))
        assertTrue(source.contains("rememberSaveable(settings.comfyUiBaseUrl)"))
        assertTrue(source.contains("rememberSaveable(settings.workflowJson)"))
        assertTrue(
            Regex("""rememberSaveable\(\s*settings\.comfyUiBaseUrl\s*,\s*settings\.remotePhotoUploadConsent\s*\)""")
                .containsMatchIn(source)
        )

        assertFalse(source.contains("remember(settings.comfyUiBaseUrl)"))
        assertFalse(source.contains("remember(settings.workflowJson)"))
        assertFalse(source.contains("remember(settings.remotePhotoUploadConsent)"))
    }
}

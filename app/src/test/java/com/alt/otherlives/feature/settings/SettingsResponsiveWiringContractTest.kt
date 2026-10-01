package com.alt.otherlives.feature.settings

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsResponsiveWiringContractTest {
    @Test
    fun settingsScreenUsesResolvedResponsiveMetrics() {
        val source = File(
            "src/main/java/com/alt/otherlives/feature/settings/GenerationSettingsScreen.kt"
        ).readText()

        assertTrue(source.contains("LocalConfiguration.current"))
        assertTrue(source.contains("LocalDensity.current.fontScale"))
        assertTrue(source.contains("SettingsResponsiveLayout.resolve("))
        assertTrue(source.contains(".padding(settingsLayout.contentPaddingDp.dp)"))
        assertTrue(source.contains("fontSize = settingsLayout.titleSizeSp.sp"))
        assertTrue(source.contains("minLines = settingsLayout.workflowMinLines"))
        assertTrue(source.contains("Spacer(Modifier.height(settingsLayout.sectionSpacingDp.dp))"))

        assertFalse(source.contains(".padding(24.dp)"))
        assertFalse(source.contains("minLines = 12"))
        assertFalse(source.contains("Spacer(Modifier.height(20.dp))"))
    }
}

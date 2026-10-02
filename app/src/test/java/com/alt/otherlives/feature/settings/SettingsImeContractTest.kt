package com.alt.otherlives.feature.settings

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsImeContractTest {
    @Test
    fun settingsScreenKeepsActionsAboveKeyboard() {
        val source = File(
            "src/main/java/com/alt/otherlives/feature/settings/GenerationSettingsScreen.kt"
        ).readText()

        assertTrue(source.contains("imePadding()"))
    }
}

package com.alt.otherlives.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class SystemBackNavigationWiringContractTest {
    @Test
    fun altAppUsesSystemBackPolicyOutsideHome() {
        val source = File(
            "src/main/java/com/alt/otherlives/app/AltApp.kt"
        ).readText()

        assertTrue(source.contains("import androidx.activity.compose.BackHandler"))
        assertTrue(source.contains("BackHandler("))
        assertTrue(source.contains("enabled = navigation.screen != AltScreen.HOME"))
        assertTrue(source.contains("SystemBackNavigationPolicy.destination(navigation)"))
        assertTrue(source.contains("navigation = destination"))
    }
}

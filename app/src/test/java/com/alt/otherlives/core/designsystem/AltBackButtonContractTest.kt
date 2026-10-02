package com.alt.otherlives.core.designsystem

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AltBackButtonContractTest {
    @Test
    fun sharedBackButtonUsesLocalizedCopyAndSemantics() {
        val source = File(
            "src/main/java/com/alt/otherlives/core/designsystem/AltBackButton.kt"
        ).readText()

        assertTrue(source.contains("stringResource(R.string.common_back)"))
        assertFalse(source.contains("""Text("‹ Back")"""))
        assertFalse(source.contains("""contentDescription = "Back""""))
    }
}

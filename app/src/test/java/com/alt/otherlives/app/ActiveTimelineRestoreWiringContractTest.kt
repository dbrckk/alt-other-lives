package com.alt.otherlives.app

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActiveTimelineRestoreWiringContractTest {
    @Test
    fun appPersistsDurableTimelineIdentityAndRebuildsPhotoUri() {
        val source = File(
            "src/main/java/com/alt/otherlives/app/AltApp.kt"
        ).readText()

        assertTrue(source.contains("var photoFileName by rememberSaveable"))
        assertTrue(source.contains("var selectedScenarioId by rememberSaveable"))
        assertTrue(source.contains("var activeTimelineKey by rememberSaveable"))
        assertTrue(source.contains("sourcePhotoStore.uriFor(restoredIdentity.photoFileName)"))
        assertTrue(source.contains(".firstOrNull { it.id == selectedScenarioId }"))

        assertFalse(source.contains("var photoFileName by remember {"))
        assertFalse(source.contains("var activeTimelineKey by remember {"))
        assertFalse(source.contains("var selectedScenario by remember {"))
    }
}

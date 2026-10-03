package com.alt.otherlives.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseSigningWorkflowContractTest {
    @Test
    fun releaseArtifactsExposeSigningStateAndRejectPartialSecrets() {
        val source = File("../.github/workflows/android.yml").readText()

        assertTrue(source.contains("Validate release signing configuration"))
        assertTrue(source.contains("Release signing secrets are only partially configured"))
        assertTrue(source.contains("ALT_RELEASE_SIGNED=true"))
        assertTrue(source.contains("jarsigner -verify -strict"))
        assertTrue(source.contains("alt-release-aab-signed"))
        assertTrue(source.contains("alt-release-aab-unsigned"))
    }
}

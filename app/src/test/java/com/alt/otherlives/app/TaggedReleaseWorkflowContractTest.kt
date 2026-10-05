package com.alt.otherlives.app

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TaggedReleaseWorkflowContractTest {
    private val source: String by lazy {
        File("../.github/workflows/tagged-release.yml").readText()
    }

    @Test
    fun releaseIsRestrictedToSemanticVersionTags() {
        assertTrue(source.contains("- \"v*.*.*\""))
        assertTrue(source.contains("^v([0-9]+)\\.([0-9]+)\\.([0-9]+)$"))
        assertTrue(source.contains("expected_version_name=\"\${tag#v}\""))
        assertTrue(source.contains(
            "ALT_VERSION_NAME must exactly match the release tag without the v prefix."
        ))
    }

    @Test
    fun releaseRequiresExplicitVersionCodeAndAllSigningSecrets() {
        assertTrue(source.contains(
            "ALT_VERSION_CODE must be an explicit positive integer for a tagged release."
        ))
        assertTrue(source.contains("ALT_ANDROID_KEYSTORE_BASE64"))
        assertTrue(source.contains("ALT_KEYSTORE_PASSWORD"))
        assertTrue(source.contains("ALT_KEY_ALIAS"))
        assertTrue(source.contains("ALT_KEY_PASSWORD"))
        assertTrue(source.contains("is required for a tagged release."))
    }

    @Test
    fun releaseBuildMustBeSignedAndStrictlyVerified() {
        assertTrue(source.contains("gradle :app:bundleRelease --stacktrace"))
        assertTrue(source.contains("jarsigner -verify -strict"))
        assertTrue(source.contains("signed-aab"))
        assertTrue(source.contains("keystore_path=\"\$RUNNER_TEMP/alt-release.jks\""))
        assertFalse(source.contains("\${{ runner.temp }}"))
        assertFalse(source.contains("unsigned-aab"))
    }
}

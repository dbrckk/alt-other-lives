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
    fun releaseDerivesVersionMetadataFromTagAndGitHistory() {
        assertTrue(source.contains("- \"v*.*.*\""))
        assertTrue(source.contains("^v([0-9]+)\\.([0-9]+)\\.([0-9]+)$"))
        assertTrue(source.contains("fetch-depth: 0"))
        assertTrue(source.contains("git fetch --force origin main --tags"))
        assertTrue(source.contains("git merge-base --is-ancestor \"\$GITHUB_SHA\" origin/main"))
        assertTrue(source.contains("version_name=\"\${tag#v}\""))
        assertTrue(source.contains("version_code=\"\$(git rev-list --count \"\$GITHUB_SHA\")\""))
        assertTrue(source.contains("sort -V"))
        assertTrue(source.contains("ALT_VERSION_NAME=\$version_name"))
        assertTrue(source.contains("ALT_VERSION_CODE=\$version_code"))
        assertTrue(source.contains("Derived versionCode must increase beyond the previous release."))
        assertFalse(source.contains("ALT_VERSION_CODE: \${{ vars.ALT_VERSION_CODE }}"))
        assertFalse(source.contains("ALT_VERSION_NAME: \${{ vars.ALT_VERSION_NAME }}"))
    }

    @Test
    fun taggedReleaseRunsQualityChecksWithTheSameGradleVersionAsCi() {
        assertTrue(source.contains("gradle-version: \"8.11.1\""))
        val unitTests = source.indexOf("gradle :app:testDebugUnitTest --stacktrace")
        val releaseLint = source.indexOf("gradle :app:lintRelease --stacktrace")
        val signing = source.indexOf("- name: Prepare release keystore")
        val bundle = source.indexOf("gradle :app:bundleRelease --stacktrace")

        assertTrue(unitTests >= 0)
        assertTrue(releaseLint > unitTests)
        assertTrue(signing > releaseLint)
        assertTrue(bundle > signing)
    }

    @Test
    fun releaseRequiresAllSigningSecrets() {
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
        assertTrue(source.contains("github.ref_name"))
        assertTrue(source.contains("signed-aab"))
        assertTrue(source.contains("keystore_path=\"\$RUNNER_TEMP/alt-release.jks\""))
        assertFalse(source.contains("\${{ runner.temp }}"))
        assertFalse(source.contains("unsigned-aab"))
    }
}

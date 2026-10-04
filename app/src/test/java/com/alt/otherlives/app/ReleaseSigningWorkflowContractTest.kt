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
        assertTrue(source.contains("ALT_VERSION_CODE: \${{ vars.ALT_VERSION_CODE }}"))
        assertTrue(source.contains("ALT_VERSION_NAME: \${{ vars.ALT_VERSION_NAME }}"))
        assertTrue(source.contains("fetch-depth: 0"))
        assertTrue(source.contains("Resolve build version metadata"))
        assertTrue(source.contains("git rev-list --count HEAD"))
        assertTrue(source.contains("ALT_VERSION_CODE=\$commit_count"))
        assertTrue(source.contains("ALT_VERSION_NAME=0.1.0-ci.\$short_sha"))
        assertTrue(source.contains("Signed releases require ALT_VERSION_CODE to be a positive integer repository variable."))
        assertTrue(source.contains("Signed releases require a non-empty ALT_VERSION_NAME repository variable."))
        assertTrue(source.contains("jarsigner -verify -strict"))
        assertTrue(source.contains("alt-release-aab-signed"))
        assertTrue(source.contains("alt-release-aab-unsigned"))
    }
}

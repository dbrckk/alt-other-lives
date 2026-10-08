package com.alt.otherlives.app

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidCiSecretScopeContractTest {
    private val workflow: String by lazy {
        File("../.github/workflows/android.yml").readText()
    }

    @Test
    fun signingSecretsNeverReachUnitTestsLintOrEmulator() {
        val jobEnvironment = workflow.substringAfter("jobs:")
            .substringBefore("    steps:")
        assertFalse(jobEnvironment.contains("secrets."))

        val qualityChecks = workflow.substringAfter("- name: Unit tests")
            .substringBefore("- name: Validate release signing configuration")
        assertFalse(qualityChecks.contains("secrets."))
        assertTrue(qualityChecks.contains("- name: Run instrumentation tests"))

        val afterSigning = workflow.substringAfter("- name: Verify signed release AAB")
        assertFalse(afterSigning.contains("secrets."))
    }

    @Test
    fun secretAccessIsRestrictedToValidationAndMatchingSigningOperations() {
        val validate = workflow.substringAfter("- name: Validate release signing configuration")
            .substringBefore("- name: Prepare release keystore")
        assertTrue(validate.contains("ALT_ANDROID_KEYSTORE_BASE64:"))
        assertTrue(validate.contains("ALT_KEYSTORE_PASSWORD:"))
        assertTrue(validate.contains("ALT_KEY_ALIAS:"))
        assertTrue(validate.contains("ALT_KEY_PASSWORD:"))

        val prepare = workflow.substringAfter("- name: Prepare release keystore")
            .substringBefore("- name: Build release AAB")
        assertTrue(prepare.contains("ALT_ANDROID_KEYSTORE_BASE64:"))
        assertFalse(prepare.contains("ALT_KEYSTORE_PASSWORD:"))
        assertFalse(prepare.contains("ALT_KEY_ALIAS:"))
        assertFalse(prepare.contains("ALT_KEY_PASSWORD:"))

        val bundle = workflow.substringAfter("- name: Build release AAB")
            .substringBefore("- name: Verify signed release AAB")
        assertTrue(bundle.contains("ALT_KEYSTORE_PASSWORD:"))
        assertTrue(bundle.contains("ALT_KEY_ALIAS:"))
        assertTrue(bundle.contains("ALT_KEY_PASSWORD:"))
        assertFalse(bundle.contains("ALT_ANDROID_KEYSTORE_BASE64:"))
    }

    @Test
    fun workflowUsesReadOnlyGithubToken() {
        assertTrue(workflow.contains("permissions:\\n  contents: read"))
        assertFalse(workflow.contains("contents: write"))
    }

    @Test
    fun stalePullRequestChecksAreCancelledByNewCommits() {
        assertTrue(workflow.contains("concurrency:"))
        assertTrue(workflow.contains("cancel-in-progress: true"))
        assertTrue(workflow.contains("github.event.pull_request.number || github.ref"))
    }
}

package com.alt.otherlives.app

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkflowActionPinningContractTest {
    private val workflowSources: List<String> by lazy {
        listOf(
            File("../.github/workflows/android.yml").readText(),
            File("../.github/workflows/tagged-release.yml").readText()
        )
    }

    @Test
    fun thirdPartyActionsArePinnedToImmutableCommits() {
        workflowSources.forEach { source ->
            assertFalse(source.contains("actions/checkout@v5"))
            assertFalse(source.contains("actions/setup-java@v5"))
            assertFalse(source.contains("gradle/actions/setup-gradle@v4"))
            assertFalse(source.contains("actions/upload-artifact@v4"))

            assertTrue(
                source.contains(
                    "actions/checkout@fbc6f3992d24b796d5a048ff273f7fcc4a7b6c09"
                )
            )
            assertTrue(
                source.contains(
                    "actions/setup-java@b6effb05e454b25005698d916606bdc6ffcbf961"
                )
            )
            assertTrue(
                source.contains(
                    "gradle/actions/setup-gradle@ed408507eac070d1f99cc633dbcf757c94c7933a"
                )
            )
            assertTrue(
                source.contains(
                    "reactivecircus/android-emulator-runner@a421e43855164a8197daf9d8d40fe71c6996bb0d"
                )
            )
        }
    }

    @Test
    fun artifactUploadsUsePinnedCommitWherePresent() {
        workflowSources.forEach { source ->
            if (source.contains("actions/upload-artifact@")) {
                assertTrue(
                    source.contains(
                        "actions/upload-artifact@ea165f8d65b6e75b540449e92b4886f43607fa02"
                    )
                )
            }
        }
    }
}

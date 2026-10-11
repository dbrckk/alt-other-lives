package com.alt.otherlives.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class BaselineProfileContractTest {
    @Test
    fun buildWiresBaselineProfileProducerIntoReleaseApp() {
        val settings = File("../settings.gradle.kts").readText()
        val rootBuild = File("../build.gradle.kts").readText()
        val appBuild = File("build.gradle.kts").readText()
        val producerBuild = File("../baselineprofile/build.gradle.kts").readText()

        assertTrue(settings.contains("include(\":baselineprofile\")"))
        assertTrue(rootBuild.contains("id(\"com.android.test\") version \"8.10.1\" apply false"))
        assertTrue(rootBuild.contains("id(\"androidx.baselineprofile\") version \"1.5.0\" apply false"))
        assertTrue(appBuild.contains("id(\"androidx.baselineprofile\")"))
        assertTrue(appBuild.contains("androidx.profileinstaller:profileinstaller:1.4.1"))
        assertTrue(appBuild.contains("baselineProfile(project(\":baselineprofile\"))"))
        assertTrue(appBuild.contains("mergeIntoMain = true"))
        assertTrue(appBuild.contains("saveInSrc = true"))
        assertTrue(appBuild.contains("automaticGenerationDuringBuild = false"))
        assertTrue(producerBuild.contains("targetProjectPath = \":app\""))
        assertTrue(producerBuild.contains("benchmark-macro-junit4:1.5.0"))
        assertTrue(producerBuild.contains("useConnectedDevices = true"))
    }

    @Test
    fun generatorCoversColdStartupAndCiProducesProfileBeforeReleaseBundle() {
        val generator = File(
            "../baselineprofile/src/main/java/com/alt/otherlives/baselineprofile/BaselineProfileGenerator.kt"
        ).readText()
        val benchmark = File(
            "../baselineprofile/src/main/java/com/alt/otherlives/baselineprofile/StartupBenchmark.kt"
        ).readText()
        val instrumentationScript =
            File("../.github/scripts/run-instrumentation-tests.sh").readText()
        val androidWorkflow = File("../.github/workflows/android.yml").readText()
        val releaseWorkflow = File("../.github/workflows/tagged-release.yml").readText()

        assertTrue(generator.contains("BaselineProfileRule"))
        assertTrue(generator.contains("includeInStartupProfile = true"))
        assertTrue(generator.contains("startActivityAndWait()"))
        assertTrue(benchmark.contains("StartupTimingMetric()"))
        assertTrue(benchmark.contains("StartupMode.COLD"))
        assertTrue(instrumentationScript.contains(":app:generateBaselineProfile"))
        assertTrue(instrumentationScript.contains("androidx.benchmark.enabledRules=BaselineProfile"))
        assertTrue(androidWorkflow.contains("alt-baseline-profile"))
        // The tagged release now uses the exact same instrumentation script
        // as main CI. That script runs the Android suite first and generates
        // the baseline profile only when the integration gate passes.
        assertTrue(releaseWorkflow.contains("Verify Android integration and generate baseline profile"))
        assertTrue(releaseWorkflow.contains("script: bash .github/scripts/run-instrumentation-tests.sh"))
        assertTrue(
            releaseWorkflow.indexOf("script: bash .github/scripts/run-instrumentation-tests.sh") <
                releaseWorkflow.indexOf("- name: Prepare release keystore")
        )
        assertTrue(
            releaseWorkflow.contains(
                "reactivecircus/android-emulator-runner@a421e43855164a8197daf9d8d40fe71c6996bb0d"
            )
        )
    }
}

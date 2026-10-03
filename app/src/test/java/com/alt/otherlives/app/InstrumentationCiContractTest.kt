package com.alt.otherlives.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class InstrumentationCiContractTest {
    @Test
    fun ciExecutesInstrumentationTestsOnPinnedEmulatorRunner() {
        val source = File("../.github/workflows/android.yml").readText()

        assertTrue(source.contains("Run instrumentation tests"))
        assertTrue(
            source.contains(
                "reactivecircus/android-emulator-runner@" +
                    "a421e43855164a8197daf9d8d40fe71c6996bb0d"
            )
        )
        assertTrue(source.contains("api-level: 35"))
        assertTrue(source.contains("bash .github/scripts/run-instrumentation-tests.sh"))
        val script = File("../.github/scripts/run-instrumentation-tests.sh").readText()
        assertTrue(script.contains("gradle :app:connectedDebugAndroidTest --stacktrace --info"))
        assertTrue(script.contains("adb logcat -d > app/build/instrumentation-logcat.txt"))
        assertTrue(source.contains("Upload instrumentation diagnostics"))
        assertTrue(source.contains("if: always()"))
    }
}

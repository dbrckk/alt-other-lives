package com.alt.otherlives.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class InstrumentationRunnerContractTest {
    @Test
    fun appConfiguresAndroidJUnitRunnerForDeviceTests() {
        val source = File("build.gradle.kts").readText()

        assertTrue(
            source.contains(
                "testInstrumentationRunner = \"androidx.test.runner.AndroidJUnitRunner\""
            )
        )
        assertTrue(source.contains("androidx.test:runner:1.6.2"))
    }
}

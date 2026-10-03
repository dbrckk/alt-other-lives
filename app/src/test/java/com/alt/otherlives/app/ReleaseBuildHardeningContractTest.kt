package com.alt.otherlives.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseBuildHardeningContractTest {
    @Test
    fun releaseBuildUsesR8AndResourceShrinking() {
        val source = File("build.gradle.kts").readText()
        val rules = File("proguard-rules.pro")

        assertTrue(source.contains("isMinifyEnabled = true"))
        assertTrue(source.contains("isShrinkResources = true"))
        assertTrue(source.contains("getDefaultProguardFile(\"proguard-android-optimize.txt\")"))
        assertTrue(source.contains("\"proguard-rules.pro\""))
        assertTrue(rules.isFile)
        assertTrue(rules.readText().contains("-keepattributes"))
    }
}

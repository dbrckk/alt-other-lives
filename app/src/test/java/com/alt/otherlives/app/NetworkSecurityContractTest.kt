package com.alt.otherlives.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkSecurityContractTest {
    @Test
    fun appRejectsCleartextTrafficAtPlatformLevel() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        val config = File("src/main/res/xml/network_security_config.xml").readText()

        assertTrue(manifest.contains("android:usesCleartextTraffic=\"false\""))
        assertTrue(manifest.contains("android:networkSecurityConfig=\"@xml/network_security_config\""))
        assertTrue(config.contains("cleartextTrafficPermitted=\"false\""))
    }
}

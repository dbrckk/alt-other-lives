package com.alt.otherlives.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class NativeSplashThemeContractTest {
    @Test
    fun android12ThemeUsesAltSplashBranding() {
        val source = File(
            "src/main/res/values-v31/styles.xml"
        ).readText()

        assertTrue(source.contains("android:windowSplashScreenBackground"))
        assertTrue(source.contains("android:windowSplashScreenAnimatedIcon"))
        assertTrue(source.contains("@mipmap/ic_launcher"))
        assertTrue(source.contains("#08080A"))
    }
}

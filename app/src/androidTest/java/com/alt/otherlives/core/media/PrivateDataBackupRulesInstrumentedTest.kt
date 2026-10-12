package com.alt.otherlives.core.media

import android.content.pm.ApplicationInfo
import androidx.test.platform.app.InstrumentationRegistry
import com.alt.otherlives.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.xmlpull.v1.XmlPullParser

/**
 * Android resource/manifest integration guard for user-owned private photos,
 * generated chapters, history, credentials and diagnostics.
 *
 * This verifies compiled extraction rules, not OEM-specific backup transports.
 */
class PrivateDataBackupRulesInstrumentedTest {
    private val excludedDomains = setOf("root", "device_root", "external")

    @Test
    fun manifestDisablesCloudBackupAndCompiledAndroid12RulesExcludeDeviceTransfer() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals(
            "Android backup must remain disabled by default",
            0,
            context.applicationInfo.flags and ApplicationInfo.FLAG_ALLOW_BACKUP
        )
        val rules = readRules(R.xml.data_extraction_rules)
        assertEquals(
            "Both cloud and phone-to-phone migration need explicit deny rules",
            setOf("cloud-backup", "device-transfer"),
            rules.keys
        )
        assertEquals(excludedDomains, rules["cloud-backup"])
        assertEquals(excludedDomains, rules["device-transfer"])
    }

    @Test
    fun compiledLegacyBackupRulesExcludeAllAppOwnedStorageDomains() {
        val rules = readRules(R.xml.backup_rules)
        assertEquals(setOf("full-backup-content"), rules.keys)
        assertEquals(excludedDomains, rules["full-backup-content"])
    }

    private fun readRules(resourceId: Int): Map<String, Set<String>> {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val ruleModes = mutableMapOf<String, MutableSet<String>>()
        val parser = context.resources.getXml(resourceId)
        var currentMode: String? = null
        try {
            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "cloud-backup", "device-transfer", "full-backup-content" -> {
                                currentMode = parser.name
                                ruleModes.getOrPut(currentMode) { mutableSetOf() }
                            }
                            "include" -> {
                                throw AssertionError("Private ALT storage must never be included in backup")
                            }
                            "exclude" -> {
                                val domain = parser.getAttributeValue(null, "domain")
                                val path = parser.getAttributeValue(null, "path")
                                assertEquals("Only whole-domain exclusions are safe", ".", path)
                                ruleModes.getValue(requireNotNull(currentMode)).add(
                                    requireNotNull(domain)
                                )
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> if (parser.name == currentMode) {
                        currentMode = null
                    }
                }
                parser.next()
            }
        } finally {
            parser.close()
        }
        assertFalse("The resource must define at least one exclusion mode", ruleModes.isEmpty())
        return ruleModes
    }
}

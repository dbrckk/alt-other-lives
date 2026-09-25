package com.alt.otherlives.core.generation

import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SeedFileRecoveryTest {
    @Test
    fun restoresBackupWhenTargetIsMissing() {
        val root = Files.createTempDirectory("alt-seed-recovery").toFile()
        try {
            val backup = root.resolve(".seed.bak").apply { writeText("12345") }

            SeedFileRecovery.recover(root)

            val target = root.resolve("seed.txt")
            assertTrue(target.exists())
            assertEquals("12345", target.readText())
            assertFalse(backup.exists())
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun keepsCompletedTargetAndRemovesStaleBackup() {
        val root = Files.createTempDirectory("alt-seed-recovery").toFile()
        try {
            val target = root.resolve("seed.txt").apply { writeText("222") }
            val backup = root.resolve(".seed.bak").apply { writeText("111") }

            SeedFileRecovery.recover(root)

            assertEquals("222", target.readText())
            assertFalse(backup.exists())
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun restoresValidBackupWhenTargetIsCorrupt() {
        val root = Files.createTempDirectory("alt-seed-recovery").toFile()
        try {
            root.resolve("seed.txt").writeText("not-a-seed")
            root.resolve(".seed.bak").writeText("777")

            SeedFileRecovery.recover(root)

            assertEquals("777", root.resolve("seed.txt").readText())
            assertFalse(root.resolve(".seed.bak").exists())
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun removesCorruptTargetAndCorruptBackup() {
        val root = Files.createTempDirectory("alt-seed-recovery").toFile()
        try {
            root.resolve("seed.txt").writeText("-5")
            root.resolve(".seed.bak").writeText("broken")

            SeedFileRecovery.recover(root)

            assertFalse(root.resolve("seed.txt").exists())
            assertFalse(root.resolve(".seed.bak").exists())
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun boundedReaderAcceptsPositiveSeedAndRejectsOversizedContent() {
        val root = Files.createTempDirectory("alt-seed-recovery").toFile()
        try {
            val valid = root.resolve("valid.txt").apply { writeText(" 12345 ") }
            val oversized = root.resolve("oversized.txt").apply { writeText("9".repeat(64)) }

            assertEquals(12345L, SeedFileRecovery.readSeedOrNull(valid))
            assertEquals(null, SeedFileRecovery.readSeedOrNull(oversized))
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun missingBackupIsNoOp() {
        val root = Files.createTempDirectory("alt-seed-recovery").toFile()
        try {
            SeedFileRecovery.recover(root)
            assertFalse(root.resolve("seed.txt").exists())
        } finally {
            root.deleteRecursively()
        }
    }
}

package com.alt.otherlives.core.media

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.junit.Assert.assertTrue
import org.junit.Test

class TransientMediaCacheTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test
    fun expiresOnlyFilesOlderThanMaximumAge() {
        val now = 10L * TransientMediaCache.MAX_AGE_MS

        assertFalse(
            TransientMediaCache.isExpired(
                lastModifiedMs = now - TransientMediaCache.MAX_AGE_MS,
                nowMs = now
            )
        )
        assertTrue(
            TransientMediaCache.isExpired(
                lastModifiedMs = now - TransientMediaCache.MAX_AGE_MS - 1L,
                nowMs = now
            )
        )
    }

    @Test
    fun futureTimestampIsNotExpired() {
        val now = 5L * TransientMediaCache.MAX_AGE_MS

        assertFalse(
            TransientMediaCache.isExpired(
                lastModifiedMs = now + 1_000L,
                nowMs = now
            )
        )
    }

    @Test
    fun missingTimestampIsTreatedAsExpired() {
        assertTrue(
            TransientMediaCache.isExpired(
                lastModifiedMs = 0L,
                nowMs = TransientMediaCache.MAX_AGE_MS
            )
        )
    }

    @Test
    fun generationCleanupPrunesOnlyExpiredOwnedFiles() {
        val directory = temporary.newFolder("generation")
        val now = System.currentTimeMillis()
        val old = now - TransientMediaCache.MAX_AGE_MS - 5_000L
        val expiredScene = File(directory, "scene-expired-0.png").apply {
            writeText("generated")
            assertTrue(setLastModified(old))
        }
        val interruptedDownload = File(directory, "interrupted.tmp").apply {
            writeText("partial")
            assertTrue(setLastModified(old))
        }
        val unrelated = File(directory, "keep-user-data.bin").apply {
            writeText("untouched")
            assertTrue(setLastModified(old))
        }

        TransientMediaCache.cleanupGenerationFiles(directory, now)

        assertFalse(expiredScene.exists())
        assertFalse(interruptedDownload.exists())
        assertTrue(unrelated.exists())
    }

    @Test
    fun generationCleanupPreservesRecentAndFutureDatedDownloads() {
        val directory = temporary.newFolder("recent-generation")
        val now = System.currentTimeMillis()
        val fresh = File(directory, "scene-recent-1.jpg").apply {
            writeText("in-progress")
            assertTrue(setLastModified(now))
        }
        val atBoundary = File(directory, "scene-at-boundary-2.jpg").apply {
            writeText("boundary")
            assertTrue(setLastModified(now - TransientMediaCache.MAX_AGE_MS))
        }
        val future = File(directory, "scene-future-3.jpg").apply {
            writeText("future")
            assertTrue(setLastModified(now + 10_000L))
        }

        TransientMediaCache.cleanupGenerationFiles(directory, now)

        assertTrue(fresh.exists())
        assertTrue(atBoundary.exists())
        assertTrue(future.exists())
    }

    @Test
    fun generationCleanupNeverRecursesIntoNestedDirectories() {
        val directory = temporary.newFolder("nested-generation")
        val now = System.currentTimeMillis()
        val folder = File(directory, "scene-directory").apply {
            assertTrue(mkdirs())
            assertTrue(setLastModified(now - TransientMediaCache.MAX_AGE_MS - 5_000L))
        }
        val contents = File(folder, "example.jpg").apply { writeText("preserve") }

        TransientMediaCache.cleanupGenerationFiles(directory, now)

        assertTrue(folder.isDirectory)
        assertTrue(contents.exists())
    }
}

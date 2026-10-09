package com.alt.otherlives.core.data

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SourcePhotoImportCleanupTest {
    @get:Rule val temporary = TemporaryFolder()

    private val id = "123e4567-e89b-12d3-a456-426614174000"

    @Test
    fun expiredInterruptedImportIsDeletedButAnUnrelatedFileIsPreserved() {
        val dir = temporary.newFolder("source-photos")
        val now = System.currentTimeMillis()
        val expired = now - SourcePhotoImportCleanup.MAX_ORPHAN_AGE_MS - 5_000L

        val orphan = File(dir, ".source-$id.tmp").apply {
            writeText("partial import")
            assertTrue(setLastModified(expired))
        }
        val unrelated = File(dir, ".source-not-a-uuid.tmp").apply {
            writeText("private unrelated data")
            assertTrue(setLastModified(expired))
        }
        val saved = File(dir, "source-$id.jpg").apply {
            writeText("committed")
            assertTrue(setLastModified(expired))
        }

        SourcePhotoImportCleanup.cleanup(dir, now)

        assertFalse(orphan.exists())
        assertTrue(unrelated.exists())
        assertTrue(saved.exists())
    }

    @Test
    fun recentInProgressAndFutureDatedImportsAreNotDeleted() {
        val dir = temporary.newFolder("active-imports")
        val now = System.currentTimeMillis()
        val boundary = now - SourcePhotoImportCleanup.MAX_ORPHAN_AGE_MS

        val inProgress = File(dir, ".source-$id.tmp").apply {
            writeText("actively copying")
            assertTrue(setLastModified(now))
        }
        val atBoundary = File(dir, ".source-00000000-0000-0000-0000-000000000001.tmp").apply {
            writeText("exact cutoff")
            assertTrue(setLastModified(boundary))
        }
        val future = File(dir, ".source-00000000-0000-0000-0000-000000000002.tmp").apply {
            writeText("future timestamp")
            assertTrue(setLastModified(now + 1_000L))
        }

        SourcePhotoImportCleanup.cleanup(dir, now)

        assertTrue(inProgress.exists())
        assertTrue(atBoundary.exists())
        assertTrue(future.exists())
    }

    @Test
    fun cleaningDoesNotRecurseIntoNestedDirectories() {
        val dir = temporary.newFolder("nested")
        val now = System.currentTimeMillis()
        val nested = File(dir, ".source-$id.tmp").apply {
            assertTrue(mkdir())
            assertTrue(setLastModified(now - SourcePhotoImportCleanup.MAX_ORPHAN_AGE_MS - 5_000L))
        }
        val contents = File(nested, "preserve.jpg").apply { writeText("keep") }

        SourcePhotoImportCleanup.cleanup(dir, now)

        assertTrue(nested.isDirectory)
        assertTrue(contents.exists())
    }

    @Test
    fun cleaningNonexistentDirectoryDoesNothing() {
        SourcePhotoImportCleanup.cleanup(File(temporary.root, "does-not-exist"))
    }
}

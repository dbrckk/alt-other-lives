package com.alt.otherlives.core.data

import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.util.UUID
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Device-level check of the actual app-private source-photo directory.
 * Automatic timeline pruning must not delete a concurrent temporary import.
 */
class SourcePhotoPruneInstrumentedTest {
    @Test
    fun pruningOnlyDeletesUnreferencedFinalizedPhotosAndExpiredOwnedImports() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.filesDir, "source-photos")
        assertTrue(directory.isDirectory || directory.mkdirs())

        val retained = File(directory, "source-${UUID.randomUUID()}.jpg")
        val unreferenced = File(directory, "source-${UUID.randomUUID()}.jpg")
        val activeImport = File(directory, ".source-${UUID.randomUUID()}.tmp")
        val interruptedImport = File(directory, ".source-${UUID.randomUUID()}.tmp")
        val unrelated = File(directory, ".other-${UUID.randomUUID()}.tmp")

        val fixtures = listOf(retained, unreferenced, activeImport, interruptedImport, unrelated)
        try {
            fixtures.forEach { it.writeText("fixture") }
            val now = System.currentTimeMillis()
            assertTrue(
                interruptedImport.setLastModified(
                    now - SourcePhotoImportCleanup.MAX_ORPHAN_AGE_MS - 60_000L
                )
            )

            SourcePhotoStore(context).deleteUnreferenced(setOf(retained.name))

            assertTrue(retained.exists())
            assertFalse(unreferenced.exists())
            assertTrue(activeImport.exists())
            assertFalse(interruptedImport.exists())
            assertTrue(unrelated.exists())
        } finally {
            fixtures.forEach { it.delete() }
        }
    }
}

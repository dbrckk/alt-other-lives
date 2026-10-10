package com.alt.otherlives.core.generation

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Device integration tests against the real FileProvider and private scene store.
 * No remote generation backend or JVM-only filesystem substitute is involved.
 */
class GeneratedScenePersistenceCancellationInstrumentedTest {
    @Test
    fun cancelledChapterCopyKeepsPreviousSceneAndRemovesStagingFile() = withFixture {
        context, store, timelineKey, root, original, replacement ->
        val saved = store.persist(timelineKey, listOf(GeneratedScene(0, original)))
        val originalBytes = readScene(context, saved.single().imageUri)
        var cancelledAfterCopyStarted = false

        val failure = runCatching {
            store.persist(
                timelineKey = timelineKey,
                scenes = listOf(GeneratedScene(0, replacement)),
                checkCancelled = {
                    if (root.listFiles().orEmpty().any {
                            it.isFile && it.name.startsWith(".scene-") &&
                                it.name.endsWith(".tmp") && it.length() > 0L
                        }) {
                        cancelledAfterCopyStarted = true
                        throw CancellationException("cancel during private chapter copy")
                    }
                }
            )
        }.exceptionOrNull()

        assertTrue("Cancellation must occur during an actual file copy", cancelledAfterCopyStarted)
        assertTrue("Expected cooperative cancellation", failure is CancellationException)
        val remaining = store.load(timelineKey)
        assertEquals(1, remaining.size)
        assertEquals(0, remaining.single().chapterIndex)
        assertArrayEquals(originalBytes, readScene(context, remaining.single().imageUri))
        assertFalse(
            "Interrupted chapter must not leave staged files or backups",
            root.listFiles().orEmpty().any {
                it.name.startsWith(".scene-") && (
                    it.name.endsWith(".tmp") || it.name.endsWith(".bak")
                )
            }
        )
    }

    @Test
    fun cancelledFullRegenerationPreservesAllChaptersAndSeed() = withFixture {
        context, store, timelineKey, root, original, replacement ->
        val originalScenes = (0..4).map { GeneratedScene(it, original) }
        val replacementScenes = (0..4).map { GeneratedScene(it, replacement) }
        val baseline = store.persist(timelineKey, originalScenes)
        val baselineBytes = baseline.associate { it.chapterIndex to readScene(context, it.imageUri) }
        val initialSeed = 123456789L
        store.setSeed(timelineKey, initialSeed)
        var cancelledWithFiveStagedScenes = false

        val failure = runCatching {
            store.replaceBatchAtomically(
                timelineKey = timelineKey,
                scenes = replacementScenes,
                seed = 987654321L,
                checkCancelled = {
                    val transaction = root.listFiles().orEmpty().firstOrNull {
                        it.isDirectory && it.name.startsWith(".batch-")
                    }
                    val staged = transaction?.let { File(it, "staged").listFiles().orEmpty() }
                        .orEmpty()
                    if (staged.size == 5 && staged.all { it.isFile && it.length() > 0L }) {
                        cancelledWithFiveStagedScenes = true
                        throw CancellationException("cancel before full-regeneration commit")
                    }
                }
            )
        }.exceptionOrNull()

        assertTrue("Five replacement scenes must have started staging", cancelledWithFiveStagedScenes)
        assertTrue("Expected cooperative cancellation", failure is CancellationException)
        assertEquals(initialSeed, store.getOrCreateSeed(timelineKey))
        val restored = store.load(timelineKey)
        assertEquals(5, restored.size)
        restored.forEach { scene ->
            assertArrayEquals(
                "Previous chapter ${scene.chapterIndex} must remain intact",
                baselineBytes.getValue(scene.chapterIndex),
                readScene(context, scene.imageUri)
            )
        }
        assertFalse(
            "Aborted regeneration must not keep an unfinished batch directory",
            root.listFiles().orEmpty().any { it.name.startsWith(".batch-") }
        )
    }

    private fun withFixture(
        run: (Context, GeneratedSceneStore, String, File, Uri, Uri) -> Unit
    ) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val sourceDir = File(context.cacheDir, "generation").apply {
            assertTrue(isDirectory || mkdirs())
        }
        val timelineKey = "scene-cancel-${UUID.randomUUID()}"
        val root = File(context.filesDir, "generated/$timelineKey")
        val originalFile = File(sourceDir, "scene-integrity-original-${UUID.randomUUID()}.jpg")
        val replacementFile = File(sourceDir, "scene-integrity-replace-${UUID.randomUUID()}.jpg")
        val store = GeneratedSceneStore(context)
        try {
            writeJpeg(originalFile, 0xFF204060.toInt())
            writeJpeg(replacementFile, 0xFFB03020.toInt())
            val original = imageUri(context, originalFile)
            val replacement = imageUri(context, replacementFile)
            run(context, store, timelineKey, root, original, replacement)
        } finally {
            store.clear(timelineKey)
            originalFile.delete()
            replacementFile.delete()
        }
    }

    private fun writeJpeg(file: File, color: Int) {
        val bitmap = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888)
        try {
            bitmap.eraseColor(color)
            file.outputStream().use { output ->
                assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output))
            }
        } finally {
            bitmap.recycle()
        }
    }

    private fun imageUri(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)

    private fun readScene(context: Context, uri: Uri): ByteArray =
        context.contentResolver.openInputStream(uri)!!.use { it.readBytes() }
}

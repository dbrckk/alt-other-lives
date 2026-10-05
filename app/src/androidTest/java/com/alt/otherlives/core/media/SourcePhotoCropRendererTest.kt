package com.alt.otherlives.core.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.content.FileProvider
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SourcePhotoCropRendererTest {
    @Test
    fun croppedSourceProducesExpectedDimensionsAndCleansTemporaryFile() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val generationDir = File(context.cacheDir, "generation").apply { mkdirs() }
        val source = File(generationDir, "crop-test-source.jpg")
        val bitmap = Bitmap.createBitmap(400, 200, Bitmap.Config.ARGB_8888)
        try {
            source.outputStream().use { stream ->
                assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream))
            }
        } finally {
            bitmap.recycle()
        }

        val sourceUri = FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            source
        )
        val cropDir = File(context.cacheDir, "generation/crops")
        val existingCropNames = cropDir.listFiles()
            .orEmpty()
            .map { it.name }
            .toSet()

        val prepared = SourcePhotoCropRenderer.prepare(
            context = context,
            sourceUri = sourceUri,
            crop = NormalizedCropRect.centeredAspect(
                sourceWidth = 400,
                sourceHeight = 200
            )
        )

        assertNotEquals(sourceUri, prepared.uri)

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(prepared.uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, bounds)
        }
        assertEquals(160, bounds.outWidth)
        assertEquals(200, bounds.outHeight)

        val createdCropFiles = cropDir.listFiles()
            .orEmpty()
            .filter { it.name !in existingCropNames }
        assertEquals(1, createdCropFiles.size)

        prepared.cleanup()

        assertFalse(createdCropFiles.single().exists())
        source.delete()
    }
}

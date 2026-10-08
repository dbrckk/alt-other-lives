package com.alt.otherlives.core.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import androidx.core.content.FileProvider
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
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
    @Test
    fun fullFrameRemoteSourceIsReEncodedWithoutCameraOrGpsMetadata() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.cacheDir, "generation").apply { mkdirs() }
        val source = File(directory, "exif-test-source-" + System.nanoTime() + ".jpg")
        val bitmap = Bitmap.createBitmap(400, 200, Bitmap.Config.ARGB_8888)
        var prepared: PreparedSourcePhoto? = null

        try {
            source.outputStream().use { stream ->
                assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream))
            }

            ExifInterface(source.absolutePath).apply {
                setAttribute(ExifInterface.TAG_MAKE, "ALT_TEST_CAMERA")
                setLatLong(48.8566, 2.3522)
                saveAttributes()
            }

            val sourceExif = ExifInterface(source.absolutePath)
            assertEquals("ALT_TEST_CAMERA", sourceExif.getAttribute(ExifInterface.TAG_MAKE))
            assertTrue(sourceExif.getLatLong(FloatArray(2)))

            val sourceUri = FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                source
            )
            prepared = SourcePhotoCropRenderer.prepare(
                context = context,
                sourceUri = sourceUri,
                crop = NormalizedCropRect.Full
            )
            assertNotEquals(sourceUri, prepared.uri)

            context.contentResolver.openFileDescriptor(prepared.uri, "r")!!.use { descriptor ->
                val uploadedExif = ExifInterface(descriptor.fileDescriptor)
                assertNull(uploadedExif.getAttribute(ExifInterface.TAG_MAKE))
                assertFalse(uploadedExif.getLatLong(FloatArray(2)))
            }

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(prepared.uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, bounds)
            }
            assertEquals(400, bounds.outWidth)
            assertEquals(200, bounds.outHeight)
            assertTrue(source.exists())
        } finally {
            prepared?.cleanup()
            bitmap.recycle()
            source.delete()
        }
    }

}

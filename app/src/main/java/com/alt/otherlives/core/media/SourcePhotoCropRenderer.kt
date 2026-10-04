package com.alt.otherlives.core.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID
import kotlin.math.ceil
import kotlin.math.floor

data class PreparedSourcePhoto(
    val uri: Uri,
    private val temporaryFile: File? = null
) {
    fun cleanup() {
        temporaryFile?.delete()
    }
}

object SourcePhotoCropRenderer {
    private const val MAX_DECODE_EDGE = 2560
    private const val JPEG_QUALITY = 95

    fun prepare(
        context: Context,
        sourceUri: Uri,
        crop: NormalizedCropRect
    ): PreparedSourcePhoto {
        if (crop == NormalizedCropRect.Full) {
            return PreparedSourcePhoto(sourceUri)
        }

        val bitmap = decodeOriented(context, sourceUri)
        val left = floor(crop.left * bitmap.width).toInt().coerceIn(0, bitmap.width - 1)
        val top = floor(crop.top * bitmap.height).toInt().coerceIn(0, bitmap.height - 1)
        val right = ceil(crop.right * bitmap.width).toInt().coerceIn(left + 1, bitmap.width)
        val bottom = ceil(crop.bottom * bitmap.height).toInt().coerceIn(top + 1, bitmap.height)
        val cropped = Bitmap.createBitmap(
            bitmap,
            left,
            top,
            right - left,
            bottom - top
        )
        if (cropped !== bitmap) {
            bitmap.recycle()
        }

        val dir = File(context.cacheDir, "generation/crops").apply { mkdirs() }
        val output = File(dir, "crop-" + UUID.randomUUID() + ".jpg")
        try {
            output.outputStream().use { stream ->
                check(cropped.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)) {
                    "Unable to encode cropped source photo"
                }
            }
        } finally {
            cropped.recycle()
        }

        val uri = try {
            FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                output
            )
        } catch (error: Throwable) {
            output.delete()
            throw error
        }
        return PreparedSourcePhoto(uri = uri, temporaryFile = output)
    }

    private fun decodeOriented(context: Context, uri: Uri): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
            BitmapFactory.decodeFileDescriptor(descriptor.fileDescriptor, null, bounds)
        } ?: error("Unable to inspect source photo")

        require(bounds.outWidth > 0 && bounds.outHeight > 0) {
            "Unable to decode source photo"
        }

        var sampleSize = 1
        while (
            bounds.outWidth / sampleSize > MAX_DECODE_EDGE ||
            bounds.outHeight / sampleSize > MAX_DECODE_EDGE
        ) {
            sampleSize *= 2
        }

        val bitmap = context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
            BitmapFactory.decodeFileDescriptor(
                descriptor.fileDescriptor,
                null,
                BitmapFactory.Options().apply { inSampleSize = sampleSize }
            )
        } ?: error("Unable to decode source photo")

        val orientation = runCatching {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                ExifInterface(descriptor.fileDescriptor).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

        val matrix = orientationMatrix(orientation)
        if (matrix.isIdentity) return bitmap

        val oriented = Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
        if (oriented !== bitmap) {
            bitmap.recycle()
        }
        return oriented
    }

    internal fun orientationMatrix(orientation: Int): Matrix =
        Matrix().apply {
            when (orientation) {
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> setScale(-1f, 1f)
                ExifInterface.ORIENTATION_ROTATE_180 -> setRotate(180f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> setScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    setRotate(90f)
                    postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_ROTATE_90 -> setRotate(90f)
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    setRotate(-90f)
                    postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_ROTATE_270 -> setRotate(-90f)
            }
        }
}

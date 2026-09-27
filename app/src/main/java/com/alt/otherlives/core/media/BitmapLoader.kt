package com.alt.otherlives.core.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri

object BitmapLoader {
    fun decodeFirstAvailable(
        context: Context,
        uris: Iterable<Uri?>,
        targetWidth: Int,
        targetHeight: Int
    ): Bitmap? {
        val seen = mutableSetOf<String>()
        uris.forEach { uri ->
            if (uri == null || !seen.add(uri.toString())) return@forEach
            val decoded = runCatching {
                decodeSampled(
                    context = context,
                    uri = uri,
                    targetWidth = targetWidth,
                    targetHeight = targetHeight
                )
            }.getOrNull()
            if (decoded != null) return decoded
        }
        return null
    }

    fun decodeSampled(
        context: Context,
        uri: Uri,
        targetWidth: Int,
        targetHeight: Int
    ): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, bounds)
        }

        if (!ImageBoundsValidation.isReasonable(bounds.outWidth, bounds.outHeight)) {
            return null
        }

        val sampleSize = BitmapSampling.calculateSampleSize(
            sourceWidth = bounds.outWidth,
            sourceHeight = bounds.outHeight,
            targetWidth = targetWidth,
            targetHeight = targetHeight
        )

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        val decoded = context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, options)
        } ?: return null

        if (!BitmapSampling.isDecodedSizeSafe(decoded.width, decoded.height)) {
            decoded.recycle()
            return null
        }
        return decoded
    }
}

package com.alt.otherlives.core.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri

object BitmapLoader {
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

        require(targetWidth > 0 && targetHeight > 0) {
            "Target bitmap dimensions must be positive"
        }

        var sampleSize = 1
        while (
            sampleSize <= Int.MAX_VALUE / 2 &&
            bounds.outWidth / 2 / sampleSize >= targetWidth &&
            bounds.outHeight / 2 / sampleSize >= targetHeight
        ) {
            sampleSize *= 2
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        return context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, options)
        }
    }
}

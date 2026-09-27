package com.alt.otherlives.core.media

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.provider.MediaStore
import android.os.Build
import androidx.core.content.FileProvider
import com.alt.otherlives.core.model.Scenario
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ShareCardRenderer {
    private data class RenderedShareImage(val uri: Uri, val file: File)

    private const val WIDTH = 1080
    private const val HEIGHT = 1920
    private const val CACHE_MAX_AGE_MS = 24L * 60L * 60L * 1000L
    private const val PENDING_MEDIA_EXPIRY_SECONDS = 24L * 60L * 60L
    fun render(
        context: Context,
        photoUri: Uri?,
        scenario: Scenario,
        fallbackImageUri: Uri? = null
    ): Uri = renderArtifact(
        context = context,
        photoUri = photoUri,
        scenario = scenario,
        fallbackImageUri = fallbackImageUri
    ).uri

    private fun renderArtifact(
        context: Context,
        photoUri: Uri?,
        scenario: Scenario,
        fallbackImageUri: Uri? = null
    ): RenderedShareImage {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.rgb(8, 8, 10))

            (photoUri ?: fallbackImageUri)?.let { uri ->
                val source = BitmapLoader.decodeSampled(context, uri, WIDTH, HEIGHT)
                    ?: error("Unable to decode ALT share visual")
                try {
                    drawCover(canvas, source, Rect(0, 0, WIDTH, 1180))
                } finally {
                    source.recycle()
                }
            }

            val overlay = Paint().apply {
                shader = LinearGradient(
                    0f,
                    280f,
                    0f,
                    1380f,
                    intArrayOf(
                        Color.argb(10, 8, 8, 10),
                        Color.argb(70, 8, 8, 10),
                        Color.argb(225, 8, 8, 10),
                        Color.rgb(8, 8, 10)
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 240f, WIDTH.toFloat(), 1450f, overlay)

            val accent = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(183, 167, 255)
                textSize = 31f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                letterSpacing = 0.08f
            }
            canvas.drawText("ALT  •  YOUR OTHER LIFE", 72f, 850f, accent)

            val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 78f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            var y = drawWrappedText(
                canvas,
                scenario.title,
                title,
                72f,
                950f,
                936f,
                88f
            )

            val subtitle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(210, 207, 219)
                textSize = 34f
            }
            y += 18f
            y = drawWrappedText(
                canvas,
                scenario.subtitle,
                subtitle,
                72f,
                y,
                900f,
                44f
            )

            val timelineLabel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(130, 127, 140)
                textSize = 24f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            y += 44f
            canvas.drawText("YOUR TIMELINE", 72f, y, timelineLabel)
            y += 46f

            val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(183, 167, 255)
                textSize = 36f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val chapterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 34f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            scenario.chapters.take(3).forEachIndexed { index, chapter ->
                canvas.drawText(
                    (index + 1).toString().padStart(2, '0'),
                    72f,
                    y,
                    numberPaint
                )
                canvas.drawText(
                    chapter.label,
                    148f,
                    y,
                    chapterPaint
                )
                y += 62f
            }

            val footer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(150, 146, 162)
                textSize = 27f
            }
            canvas.drawText("One choice. Another life.", 72f, 1812f, footer)

            val brand = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(183, 167, 255)
                textSize = 34f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("ALT", 930f, 1812f, brand)

            val dir = File(context.cacheDir, "shares").apply { mkdirs() }
            cleanupOldShareImages(dir)
            val file = File(
                dir,
                "alt-" + scenario.id + "-" + UUID.randomUUID() + ".jpg"
            )
            val temporary = File(dir, "." + file.name + ".tmp")

            try {
                FileOutputStream(temporary).use { output ->
                    check(bitmap.compress(Bitmap.CompressFormat.JPEG, 94, output)) {
                        "Unable to encode ALT share image"
                    }
                }
                require(temporary.length() > 0L) { "ALT share image is empty" }
                if (!temporary.renameTo(file)) {
                    error("Unable to finalize ALT share image")
                }
                return RenderedShareImage(
                    uri = FileProvider.getUriForFile(
                        context,
                        context.packageName + ".fileprovider",
                        file
                    ),
                    file = file
                )
            } catch (error: Throwable) {
                temporary.delete()
                file.delete()
                throw error
            }
        } finally {
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
    }

    fun renderComparison(
        context: Context,
        firstVisualUri: Uri?,
        firstScenario: Scenario,
        secondVisualUri: Uri?,
        secondScenario: Scenario
    ): Uri {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.rgb(8, 8, 10))

            val accent = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(183, 167, 255)
                textSize = 30f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                letterSpacing = 0.08f
            }
            canvas.drawText("ALT  •  TWO LIVES", 72f, 92f, accent)

            drawComparisonHalf(
                context = context,
                canvas = canvas,
                visualUri = firstVisualUri,
                scenario = firstScenario,
                top = 140,
                bottom = 865,
                label = "LIFE 01"
            )
            drawComparisonHalf(
                context = context,
                canvas = canvas,
                visualUri = secondVisualUri,
                scenario = secondScenario,
                top = 955,
                bottom = 1680,
                label = "LIFE 02"
            )

            val vsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(183, 167, 255)
                textSize = 34f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("VS", WIDTH / 2f, 925f, vsPaint)

            val footer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(150, 146, 162)
                textSize = 27f
            }
            canvas.drawText("Which life would you choose?", 72f, 1812f, footer)

            val brand = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(183, 167, 255)
                textSize = 34f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("ALT", 930f, 1812f, brand)

            val dir = File(context.cacheDir, "shares").apply { mkdirs() }
            cleanupOldShareImages(dir)
            val file = File(dir, "alt-compare-" + UUID.randomUUID() + ".jpg")
            val temporary = File(dir, "." + file.name + ".tmp")
            try {
                FileOutputStream(temporary).use { output ->
                    check(bitmap.compress(Bitmap.CompressFormat.JPEG, 94, output)) {
                        "Unable to encode ALT comparison image"
                    }
                }
                require(temporary.length() > 0L) { "ALT comparison image is empty" }
                if (!temporary.renameTo(file)) {
                    error("Unable to finalize ALT comparison image")
                }
                return FileProvider.getUriForFile(
                    context,
                    context.packageName + ".fileprovider",
                    file
                )
            } catch (error: Throwable) {
                temporary.delete()
                file.delete()
                throw error
            }
        } finally {
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
    }

    private fun drawComparisonHalf(
        context: Context,
        canvas: Canvas,
        visualUri: Uri?,
        scenario: Scenario,
        top: Int,
        bottom: Int,
        label: String
    ) {
        canvas.drawRect(
            48f,
            top.toFloat(),
            (WIDTH - 48).toFloat(),
            bottom.toFloat(),
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(20, 18, 26) }
        )

        visualUri?.let { uri ->
            val source = BitmapLoader.decodeSampled(context, uri, WIDTH, bottom - top)
            if (source != null) {
                try {
                    drawCover(
                        canvas,
                        source,
                        Rect(48, top, WIDTH - 48, bottom)
                    )
                } finally {
                    source.recycle()
                }
            }
        }

        val overlay = Paint().apply {
            shader = LinearGradient(
                0f,
                (top + 180).toFloat(),
                0f,
                bottom.toFloat(),
                intArrayOf(
                    Color.argb(10, 8, 8, 10),
                    Color.argb(195, 8, 8, 10)
                ),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(48f, top.toFloat(), (WIDTH - 48).toFloat(), bottom.toFloat(), overlay)

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(183, 167, 255)
            textSize = 27f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(label, 82f, (bottom - 156).toFloat(), labelPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 48f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        drawWrappedText(
            canvas,
            scenario.title,
            titlePaint,
            82f,
            (bottom - 92).toFloat(),
            870f,
            56f
        )
    }

    fun shareComparison(context: Context, uri: Uri) {
        val length = context.contentResolver.openAssetFileDescriptor(uri, "r")
            ?.use { it.length }
            ?: error("Rendered comparison image is no longer available")
        require(length != 0L) { "Rendered comparison image is empty" }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(
                Intent.EXTRA_TEXT,
                "ALT — Two lives. One choice. Which would you choose?"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share your ALT comparison"))
    }

    private fun cleanupOldShareImages(dir: File) {
        val cutoff = System.currentTimeMillis() - CACHE_MAX_AGE_MS
        dir.listFiles()?.forEach { file ->
            if (
                file.isFile &&
                (
                    file.extension.equals("jpg", ignoreCase = true) ||
                        file.name.endsWith(".tmp")
                    ) &&
                file.lastModified() < cutoff
            ) {
                file.delete()
            }
        }
    }

    fun saveToGallery(
        context: Context,
        photoUri: Uri?,
        scenario: Scenario,
        fallbackImageUri: Uri? = null
    ): Uri {
        require(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "Direct gallery save requires Android 10 or newer"
        }
        val rendered = renderArtifact(context, photoUri, scenario, fallbackImageUri)
        val values = android.content.ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "ALT-" + scenario.id + "-" + System.currentTimeMillis() + ".jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ALT")
            put(MediaStore.Images.Media.IS_PENDING, 1)
            put(
                MediaStore.MediaColumns.DATE_EXPIRES,
                System.currentTimeMillis() / 1000L + PENDING_MEDIA_EXPIRY_SECONDS
            )
        }
        var target: Uri? = null
        try {
            target = requireNotNull(
                context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )
            ) { "Unable to create image gallery item" }

            val copiedBytes = context.contentResolver.openOutputStream(target)?.use { output ->
                val input = context.contentResolver.openInputStream(rendered.uri)
                    ?: error("Unable to open rendered share image")
                input.use { it.copyTo(output) }
            } ?: error("Unable to open gallery output")
            require(copiedBytes > 0L) { "Rendered share image copy was empty" }

            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            values.putNull(MediaStore.MediaColumns.DATE_EXPIRES)
            require(
                context.contentResolver.update(target, values, null, null) > 0
            ) {
                "Unable to finalize image gallery item"
            }
            return target
        } catch (error: Throwable) {
            target?.let { context.contentResolver.delete(it, null, null) }
            throw error
        } finally {
            rendered.file.delete()
        }
    }
    fun share(context: Context, uri: Uri, scenario: Scenario) {
        val length = context.contentResolver.openAssetFileDescriptor(uri, "r")
            ?.use { it.length }
            ?: error("Rendered share image is no longer available")
        require(length != 0L) { "Rendered share image is empty" }

        val intent=Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(
                Intent.EXTRA_TEXT,
                "ALT — ${scenario.title}\nOne choice. Another life."
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent,"Share your ALT life"))
    }
    private fun drawCover(canvas: Canvas, source: Bitmap, target: Rect) {
        val sr=source.width.toFloat()/source.height; val tr=target.width().toFloat()/target.height()
        val src=if(sr>tr){ val w=(source.height*tr).toInt(); val l=(source.width-w)/2; Rect(l,0,l+w,source.height)}
        else { val h=(source.width/tr).toInt(); val t=(source.height-h)/2; Rect(0,t,source.width,t+h)}
        canvas.drawBitmap(source,src,target,Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
    }
    private fun drawWrappedText(canvas: Canvas,text:String,paint:Paint,x:Float,startY:Float,maxWidth:Float,lineHeight:Float):Float {
        var y=startY; var line=""
        text.split(" ").forEach { word ->
            val candidate=if(line.isEmpty()) word else line+" "+word
            if(paint.measureText(candidate)>maxWidth && line.isNotEmpty()){canvas.drawText(line,x,y,paint);y+=lineHeight;line=word}else line=candidate
        }
        if(line.isNotEmpty()){canvas.drawText(line,x,y,paint);y+=lineHeight}; return y
    }
}

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
                    drawCover(canvas, source, Rect(0, 0, WIDTH, 900))
                } finally {
                    source.recycle()
                }
            }

            val overlay = Paint().apply {
                shader = LinearGradient(
                    0f,
                    280f,
                    0f,
                    1050f,
                    intArrayOf(
                        Color.TRANSPARENT,
                        Color.argb(180, 8, 8, 10),
                        Color.rgb(8, 8, 10)
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 250f, WIDTH.toFloat(), 1100f, overlay)

            val accent = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(183, 167, 255)
                textSize = 34f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("ALT  •  YOUR OTHER LIFE", 72f, 760f, accent)

            val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 70f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            var y = drawWrappedText(
                canvas,
                scenario.title,
                title,
                72f,
                845f,
                936f,
                82f
            )

            val body = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(232, 230, 238)
                textSize = 36f
            }
            val label = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(183, 167, 255)
                textSize = 27f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            y += 28f
            scenario.chapters.take(5).forEach { chapter ->
                canvas.drawText(chapter.label.uppercase(), 72f, y, label)
                y += 48f
                y = drawWrappedText(
                    canvas,
                    chapter.narrative,
                    body,
                    72f,
                    y,
                    936f,
                    47f
                )
                y += 34f
            }

            val footer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(130, 127, 140)
                textSize = 27f
            }
            canvas.drawText("See the lives you could have lived.", 72f, 1815f, footer)
            canvas.drawText("ALT", 930f, 1815f, accent)

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
            type="image/jpeg"; putExtra(Intent.EXTRA_STREAM,uri); putExtra(Intent.EXTRA_TEXT,scenario.title)
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

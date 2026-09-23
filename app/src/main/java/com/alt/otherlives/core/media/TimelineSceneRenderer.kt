package com.alt.otherlives.core.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import com.alt.otherlives.core.model.Scenario
import java.io.File
import java.io.FileOutputStream

object TimelineSceneRenderer {
    private const val WIDTH = 1080
    private const val HEIGHT = 1920

    fun render(context: Context, photoUri: Uri?, scenario: Scenario, chapterImages: Map<Int, Uri> = emptyMap()): List<Uri> {
        cleanupOldScenes(context)
        val chapterScenes = scenario.chapters.take(5).mapIndexed { index, chapter ->
            val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.rgb(8, 8, 10))

            (chapterImages[index] ?: photoUri)?.let { uri ->
                BitmapLoader.decodeSampled(context, uri, WIDTH, HEIGHT)?.let { source ->
                    drawCover(canvas, source, Rect(0, 0, WIDTH, 1180), index)
                    source.recycle()
                }
            }

            val overlay = Paint().apply {
                shader = LinearGradient(
                    0f, 460f, 0f, 1500f,
                    intArrayOf(
                        Color.argb(12, 8, 8, 10),
                        Color.argb(185, 8, 8, 10),
                        Color.rgb(8, 8, 10)
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 360f, WIDTH.toFloat(), HEIGHT.toFloat(), overlay)

            val eyebrow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(183, 167, 255)
                textSize = 28f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("ALT • ${index + 1}/${scenario.chapters.take(5).size}", 72f, 900f, eyebrow)

            val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 64f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            var y = drawWrappedText(canvas, chapter.label, title, 72f, 990f, 920f, 76f)

            val body = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(235, 232, 242)
                textSize = 38f
            }
            y += 28f
            drawWrappedText(canvas, chapter.narrative, body, 72f, y, 920f, 52f)

            val scenarioPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(142, 137, 154)
                textSize = 26f
            }
            canvas.drawText(scenario.title, 72f, 1810f, scenarioPaint)

            val dir = File(context.cacheDir, "shares/scenes").apply { mkdirs() }
            val file = File(dir, "alt-${scenario.id}-scene-$index.jpg")
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 94, it) }
            bitmap.recycle()

            FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                file
            )
        }

        return listOf(renderIntro(context, photoUri, scenario)) +
            chapterScenes +
            listOf(renderOutro(context, scenario))
    }

    private fun renderIntro(context: Context, photoUri: Uri?, scenario: Scenario): Uri {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.rgb(8, 8, 10))

        photoUri?.let { uri ->
            BitmapLoader.decodeSampled(context, uri, WIDTH, HEIGHT)?.let { source ->
                drawCover(canvas, source, Rect(0, 0, WIDTH, 1260), 0)
                source.recycle()
            }
        }

        val overlay = Paint().apply {
            shader = LinearGradient(
                0f, 360f, 0f, 1540f,
                intArrayOf(
                    Color.argb(20, 8, 8, 10),
                    Color.argb(185, 8, 8, 10),
                    Color.rgb(8, 8, 10)
                ),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 300f, WIDTH.toFloat(), HEIGHT.toFloat(), overlay)

        val accent = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(183, 167, 255)
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("ALT • SEE YOUR OTHER LIFE", 72f, 960f, accent)

        val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 72f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        drawWrappedText(canvas, scenario.title, title, 72f, 1065f, 920f, 86f)

        val dir = File(context.cacheDir, "shares/scenes").apply { mkdirs() }
        val file = File(dir, "alt-${scenario.id}-intro.jpg")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 94, it) }
        bitmap.recycle()
        return FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
    }

    private fun renderOutro(context: Context, scenario: Scenario): Uri {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.rgb(8, 8, 10))

        val glow = Paint().apply {
            shader = LinearGradient(
                0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat(),
                intArrayOf(
                    Color.rgb(31, 24, 48),
                    Color.rgb(8, 8, 10),
                    Color.rgb(19, 15, 30)
                ),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat(), glow)

        val brand = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(183, 167, 255)
            textSize = 96f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("ALT", 72f, 880f, brand)

        val body = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 48f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        drawWrappedText(canvas, "See the lives you could have lived.", body, 72f, 980f, 900f, 60f)

        val subtle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(142, 137, 154)
            textSize = 28f
        }
        canvas.drawText(scenario.title, 72f, 1650f, subtle)

        val dir = File(context.cacheDir, "shares/scenes").apply { mkdirs() }
        val file = File(dir, "alt-${scenario.id}-outro.jpg")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 94, it) }
        bitmap.recycle()
        return FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
    }

    private fun cleanupOldScenes(context: Context) {
        val dir = File(context.cacheDir, "shares/scenes")
        if (!dir.exists()) return
        val cutoff = System.currentTimeMillis() - 24L * 60L * 60L * 1000L
        dir.listFiles()?.forEach { file ->
            if (file.isFile && file.lastModified() < cutoff) {
                file.delete()
            }
        }
    }

    private fun drawCover(canvas: Canvas, source: Bitmap, target: Rect, index: Int) {
        val sourceRatio = source.width.toFloat() / source.height
        val targetRatio = target.width().toFloat() / target.height()
        val src = if (sourceRatio > targetRatio) {
            val width = (source.height * targetRatio).toInt()
            val maxLeft = (source.width - width).coerceAtLeast(0)
            val left = ((maxLeft * (0.35f + 0.15f * (index % 3))).toInt()).coerceIn(0, maxLeft)
            Rect(left, 0, left + width, source.height)
        } else {
            val height = (source.width / targetRatio).toInt()
            val maxTop = (source.height - height).coerceAtLeast(0)
            val top = ((maxTop * (0.25f + 0.12f * (index % 3))).toInt()).coerceIn(0, maxTop)
            Rect(0, top, source.width, top + height)
        }
        canvas.drawBitmap(
            source,
            src,
            target,
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        )
    }

    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        paint: Paint,
        x: Float,
        startY: Float,
        maxWidth: Float,
        lineHeight: Float
    ): Float {
        var y = startY
        var line = ""
        text.split(" ").forEach { word ->
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(candidate) > maxWidth && line.isNotEmpty()) {
                canvas.drawText(line, x, y, paint)
                y += lineHeight
                line = word
            } else {
                line = candidate
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line, x, y, paint)
            y += lineHeight
        }
        return y
    }
}

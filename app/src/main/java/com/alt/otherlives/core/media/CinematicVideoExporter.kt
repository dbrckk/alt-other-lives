package com.alt.otherlives.core.media

import android.content.Context
import android.content.Intent
import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.Effect
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.ProgressHolder
import androidx.media3.effect.MatrixTransformation
import com.alt.otherlives.core.model.Scenario
import java.io.File
import java.util.UUID
import java.util.WeakHashMap
import java.util.Collections

@UnstableApi
object CinematicVideoExporter {
    private val activeOutputs = Collections.synchronizedMap(
        WeakHashMap<Transformer, File>()
    )

    private const val INTRO_DURATION_MS = 1800L
    private const val CHAPTER_DURATION_MS = 2200L
    private const val OUTRO_DURATION_MS = 1600L
    private const val FRAME_RATE = 30

    fun export(
        context: Context,
        imageUris: List<Uri>,
        scenario: Scenario,
        onCompleted: (Uri) -> Unit,
        onError: (Throwable) -> Unit
    ): Transformer {
        require(imageUris.isNotEmpty()) { "At least one scene is required" }

        val outputDir = File(context.cacheDir, "shares").apply { mkdirs() }
        cleanupOldVideos(outputDir)
        val outputFile = File(
            outputDir,
            "alt-" + scenario.id + "-" + UUID.randomUUID() + ".mp4"
        )

        val editedScenes = imageUris.mapIndexed { index, imageUri ->
            val durationMs = when (index) {
                0 -> INTRO_DURATION_MS
                imageUris.lastIndex -> OUTRO_DURATION_MS
                else -> CHAPTER_DURATION_MS
            }

            val mediaItem = MediaItem.Builder()
                .setUri(imageUri)
                .setImageDurationMs(durationMs)
                .build()

            val motion = MatrixTransformation { presentationTimeUs ->
                val progress = (presentationTimeUs / (durationMs * 1000f)).coerceIn(0f, 1f)
                val eased = progress * progress * (3f - 2f * progress)
                val direction = if (index % 2 == 0) 1f else -1f
                val isEdgeScene = index == 0 || index == imageUris.lastIndex
                val scaleRange = if (isEdgeScene) 0.035f else 0.07f
                val scale = 1.015f + scaleRange * eased
                val panX = direction * if (isEdgeScene) 0.010f * eased else (-0.018f + 0.036f * eased)
                val panY = if (isEdgeScene) 0f else 0.012f - 0.024f * eased
                Matrix().apply {
                    postScale(scale, scale)
                    postTranslate(panX, panY)
                }
            }

            EditedMediaItem.Builder(mediaItem)
                .setFrameRate(FRAME_RATE)
                .setEffects(Effects(emptyList(), listOf<Effect>(motion)))
                .build()
        }

        val sequence = EditedMediaItemSequence.Builder(editedScenes).build()
        val composition = Composition.Builder(listOf(sequence)).build()

        val transformerHolder = arrayOfNulls<Transformer>(1)
        val transformer = Transformer.Builder(context)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, result: ExportResult) {
                    transformerHolder[0]?.let { activeOutputs.remove(it) }
                    val uri = FileProvider.getUriForFile(
                        context,
                        context.packageName + ".fileprovider",
                        outputFile
                    )
                    onCompleted(uri)
                }

                override fun onError(
                    composition: Composition,
                    result: ExportResult,
                    exception: ExportException
                ) {
                    transformerHolder[0]?.let { activeOutputs.remove(it) }
                    outputFile.delete()
                    onError(exception)
                }
            })
            .build()

        transformerHolder[0] = transformer
        activeOutputs[transformer] = outputFile
        try {
            transformer.start(composition, outputFile.absolutePath)
        } catch (error: Throwable) {
            activeOutputs.remove(transformer)
            outputFile.delete()
            throw error
        }
        return transformer
    }

    private fun cleanupOldVideos(outputDir: File) {
        val cutoff = System.currentTimeMillis() - 24L * 60L * 60L * 1000L
        outputDir.listFiles()?.forEach { file ->
            if (file.isFile && file.extension.equals("mp4", ignoreCase = true) && file.lastModified() < cutoff) {
                file.delete()
            }
        }
    }

    fun cancel(transformer: Transformer) {
        transformer.cancel()
        activeOutputs.remove(transformer)?.delete()
    }

    fun progress(transformer: Transformer): Int? {
        val holder = ProgressHolder()
        return when (transformer.getProgress(holder)) {
            Transformer.PROGRESS_STATE_AVAILABLE -> holder.progress
            else -> null
        }
    }

    fun saveToGallery(context: Context, uri: Uri, scenario: Scenario): Uri {
        require(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "Direct video gallery save requires Android 10 or newer"
        }

        val values = ContentValues().apply {
            put(
                MediaStore.Video.Media.DISPLAY_NAME,
                "ALT-" + scenario.id + "-" + System.currentTimeMillis() + ".mp4"
            )
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/ALT")
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val target = requireNotNull(
            resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
        )

        try {
            resolver.openOutputStream(target)?.use { output ->
                resolver.openInputStream(uri)?.use { input -> input.copyTo(output) }
            } ?: error("Unable to open video gallery output")

            values.clear()
            values.put(MediaStore.Video.Media.IS_PENDING, 0)
            resolver.update(target, values, null, null)
            return target
        } catch (error: Throwable) {
            resolver.delete(target, null, null)
            throw error
        }
    }

    fun share(context: Context, uri: Uri, scenario: Scenario) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, scenario.title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share your ALT video"))
    }
}

package com.alt.otherlives.core.media

import android.content.Context
import android.content.Intent
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
import com.alt.otherlives.R
import java.io.File
import java.util.UUID
import java.util.WeakHashMap
import java.util.Collections

@UnstableApi
object CinematicVideoExporter {
    private val activeOutputs = Collections.synchronizedMap(
        WeakHashMap<Transformer, File>()
    )
    private val cancelledExports = Collections.synchronizedMap(
        WeakHashMap<Transformer, Boolean>()
    )

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
            val durationMs = CinematicPacing.sceneDurationMs(
                sceneIndex = index,
                sceneCount = imageUris.size,
                scenario = scenario
            )

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
                    val wasCancelled = transformerHolder[0]?.let { transformer ->
                        activeOutputs.remove(transformer)
                        cancelledExports.remove(transformer) == true
                    } ?: false
                    if (wasCancelled) {
                        outputFile.delete()
                        return
                    }
                    if (!outputFile.exists() || outputFile.length() <= 0L) {
                        outputFile.delete()
                        onError(IllegalStateException("Video export completed without a valid MP4 file"))
                        return
                    }
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
                    val wasCancelled = transformerHolder[0]?.let { transformer ->
                        activeOutputs.remove(transformer)
                        cancelledExports.remove(transformer) == true
                    } ?: false
                    outputFile.delete()
                    if (!wasCancelled) {
                        onError(exception)
                    }
                }
            })
            .build()

        transformerHolder[0] = transformer
        activeOutputs[transformer] = outputFile
        try {
            transformer.start(composition, outputFile.absolutePath)
        } catch (error: Throwable) {
            activeOutputs.remove(transformer)
            cancelledExports.remove(transformer)
            outputFile.delete()
            throw error
        }
        return transformer
    }

    private fun cleanupOldVideos(outputDir: File) {
        val cutoff = System.currentTimeMillis() - 24L * 60L * 60L * 1000L
        val activeFiles = synchronized(activeOutputs) {
            activeOutputs.values.toSet()
        }
        outputDir.listFiles()?.forEach { file ->
            if (
                file.isFile &&
                file !in activeFiles &&
                file.extension.equals("mp4", ignoreCase = true) &&
                file.lastModified() < cutoff
            ) {
                file.delete()
            }
        }
    }

    fun cancel(transformer: Transformer) {
        cancelledExports[transformer] = true
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

    fun saveToGallery(
        context: Context,
        uri: Uri,
        scenario: Scenario,
        checkCancelled: () -> Unit = {}
    ): Uri {
        require(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "Direct video gallery save requires Android 10 or newer"
        }

        return GalleryMediaSaver.save(
            context = context,
            sourceUri = uri,
            collectionUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            displayName = "ALT-" + scenario.id + "-" + System.currentTimeMillis() + ".mp4",
            mimeType = "video/mp4",
            relativePath = "Movies/ALT",
            checkCancelled = checkCancelled
        )
    }

    fun share(context: Context, uri: Uri, scenario: Scenario) {
        val length = context.contentResolver.openAssetFileDescriptor(uri, "r")
            ?.use { it.length }
            ?: error("Exported video is no longer available")
        require(length != 0L) { "Exported video is empty" }

        val intent = ShareMediaIntent.create(
            context = context,
            uri = uri,
            mimeType = "video/mp4",
            message = context.getString(R.string.video_share_text, scenario.title)
        )
        context.startActivity(
            Intent.createChooser(
                intent,
                context.getString(R.string.video_share_chooser)
            )
        )
    }
}

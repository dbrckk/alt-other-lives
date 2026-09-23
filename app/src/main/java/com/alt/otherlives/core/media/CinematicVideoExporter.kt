package com.alt.otherlives.core.media

import android.content.Context
import android.content.Intent
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

@UnstableApi
object CinematicVideoExporter {
    private const val SCENE_DURATION_MS = 2400L
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
        val outputFile = File(outputDir, "alt-" + scenario.id + "-" + System.currentTimeMillis() + ".mp4")

        val editedScenes = imageUris.mapIndexed { index, imageUri ->
            val mediaItem = MediaItem.Builder()
                .setUri(imageUri)
                .setImageDurationMs(SCENE_DURATION_MS)
                .build()

            val motion = MatrixTransformation { presentationTimeUs ->
                val progress = (presentationTimeUs / (SCENE_DURATION_MS * 1000f)).coerceIn(0f, 1f)
                val eased = progress * progress * (3f - 2f * progress)
                val direction = if (index % 2 == 0) 1f else -1f
                val scale = 1.02f + 0.07f * eased
                val panX = direction * (-0.018f + 0.036f * eased)
                val panY = 0.012f - 0.024f * eased
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

        val sequence = EditedMediaItemSequence.withAudioAndVideoFrom(editedScenes)
        val composition = Composition.Builder(sequence).build()

        val transformer = Transformer.Builder(context)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, result: ExportResult) {
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
                    outputFile.delete()
                    onError(exception)
                }
            })
            .build()

        transformer.start(composition, outputFile.absolutePath)
        return transformer
    }

    fun progress(transformer: Transformer): Int? {
        val holder = ProgressHolder()
        return when (transformer.getProgress(holder)) {
            Transformer.PROGRESS_STATE_AVAILABLE -> holder.progress
            else -> null
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

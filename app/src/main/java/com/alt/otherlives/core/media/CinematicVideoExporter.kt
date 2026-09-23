package com.alt.otherlives.core.media

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.alt.otherlives.core.model.Scenario
import java.io.File

@UnstableApi
object CinematicVideoExporter {
    private const val DURATION_MS = 7000L
    private const val FRAME_RATE = 30

    fun export(
        context: Context,
        imageUri: Uri,
        scenario: Scenario,
        onCompleted: (Uri) -> Unit,
        onError: (Throwable) -> Unit
    ): Transformer {
        val outputDir = File(context.cacheDir, "shares").apply { mkdirs() }
        val outputFile = File(outputDir, "alt-" + scenario.id + "-" + System.currentTimeMillis() + ".mp4")

        val mediaItem = MediaItem.Builder()
            .setUri(imageUri)
            .setImageDurationMs(DURATION_MS)
            .build()

        val edited = EditedMediaItem.Builder(mediaItem)
            .setFrameRate(FRAME_RATE)
            .build()

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

        transformer.start(edited, outputFile.absolutePath)
        return transformer
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

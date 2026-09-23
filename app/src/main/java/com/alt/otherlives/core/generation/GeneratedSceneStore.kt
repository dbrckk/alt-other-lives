package com.alt.otherlives.core.generation

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

class GeneratedSceneStore(private val context: Context) {
    fun persist(scenarioId: String, scenes: List<GeneratedScene>): List<GeneratedScene> {
        val root = File(context.filesDir, "generated/$scenarioId").apply { mkdirs() }
        root.listFiles()?.forEach { it.delete() }

        return scenes.mapIndexed { index, scene ->
            val target = File(root, "scene-$index.png")
            context.contentResolver.openInputStream(scene.imageUri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: error("Unable to persist generated scene $index")

            val uri = FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                target
            )
            GeneratedScene(scene.chapterIndex, uri)
        }
    }

    fun load(scenarioId: String): List<GeneratedScene> {
        val root = File(context.filesDir, "generated/$scenarioId")
        if (!root.exists()) return emptyList()

        return root.listFiles()
            ?.filter { it.isFile && it.name.startsWith("scene-") }
            ?.sortedBy { it.name.substringAfter("scene-").substringBefore(".").toIntOrNull() ?: Int.MAX_VALUE }
            ?.mapIndexed { index, file ->
                GeneratedScene(
                    chapterIndex = index,
                    imageUri = FileProvider.getUriForFile(
                        context,
                        context.packageName + ".fileprovider",
                        file
                    )
                )
            }
            .orEmpty()
    }

    fun clear(scenarioId: String) {
        File(context.filesDir, "generated/$scenarioId").deleteRecursively()
    }
}

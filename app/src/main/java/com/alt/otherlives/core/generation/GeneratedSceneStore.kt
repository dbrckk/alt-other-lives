package com.alt.otherlives.core.generation

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

class GeneratedSceneStore(private val context: Context) {
    fun persist(scenarioId: String, scenes: List<GeneratedScene>): List<GeneratedScene> {
        val root = File(context.filesDir, "generated/$scenarioId").apply { mkdirs() }
        root.listFiles()?.forEach { it.delete() }

        return scenes.map { scene ->
            val target = File(root, "scene-" + scene.chapterIndex + ".png")
            context.contentResolver.openInputStream(scene.imageUri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: error("Unable to persist generated scene " + scene.chapterIndex)

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
            ?.map { file ->
                val chapterIndex = file.name
                    .substringAfter("scene-")
                    .substringBefore(".")
                    .toIntOrNull()
                    ?: return@map null
                GeneratedScene(
                    chapterIndex = chapterIndex,
                    imageUri = FileProvider.getUriForFile(
                        context,
                        context.packageName + ".fileprovider",
                        file
                    )
                )
            }
            ?.filterNotNull()
            .orEmpty()
    }

    fun clear(scenarioId: String) {
        File(context.filesDir, "generated/$scenarioId").deleteRecursively()
    }
}

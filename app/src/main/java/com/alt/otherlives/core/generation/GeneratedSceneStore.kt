package com.alt.otherlives.core.generation

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

class GeneratedSceneStore(private val context: Context) {
    internal fun filenameForChapter(chapterIndex: Int): String = "scene-" + chapterIndex + ".png"

    internal fun chapterIndexFromFilename(name: String): Int? = name
        .substringAfter("scene-")
        .substringBefore(".")
        .toIntOrNull()

    fun getOrCreateSeed(timelineKey: String): Long {
        val root = File(context.filesDir, "generated/$timelineKey").apply { mkdirs() }
        val seedFile = File(root, "seed.txt")
        seedFile.takeIf { it.exists() }
            ?.readText()
            ?.trim()
            ?.toLongOrNull()
            ?.takeIf { it > 0L }
            ?.let { return it }

        return resetSeed(timelineKey)
    }

    fun resetSeed(timelineKey: String): Long {
        val root = File(context.filesDir, "generated/$timelineKey").apply { mkdirs() }
        val seed = (System.nanoTime() and Long.MAX_VALUE).coerceAtLeast(1L)
        File(root, "seed.txt").writeText(seed.toString())
        return seed
    }

    fun persist(timelineKey: String, scenes: List<GeneratedScene>): List<GeneratedScene> {
        val root = File(context.filesDir, "generated/$timelineKey").apply { mkdirs() }

        return scenes.map { scene ->
            val target = File(root, filenameForChapter(scene.chapterIndex))
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

    fun load(timelineKey: String): List<GeneratedScene> {
        val root = File(context.filesDir, "generated/$timelineKey")
        if (!root.exists()) return emptyList()

        return root.listFiles()
            ?.filter { it.isFile && it.name.startsWith("scene-") }
            ?.sortedBy { chapterIndexFromFilename(it.name) ?: Int.MAX_VALUE }
            ?.map { file ->
                val chapterIndex = chapterIndexFromFilename(file.name) ?: return@map null
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

    fun clear(timelineKey: String) {
        File(context.filesDir, "generated/$timelineKey").deleteRecursively()
    }

    fun deleteUnreferenced(keepTimelineKeys: Set<String>) {
        val root = File(context.filesDir, "generated")
        root.listFiles()?.forEach { dir ->
            if (dir.isDirectory && dir.name !in keepTimelineKeys) {
                dir.deleteRecursively()
            }
        }
    }

    fun clearAll() {
        File(context.filesDir, "generated").deleteRecursively()
    }
}

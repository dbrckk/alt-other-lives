package com.alt.otherlives.core.generation

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import android.webkit.MimeTypeMap
import android.graphics.BitmapFactory
import java.io.File

class GeneratedSceneStore(private val context: Context) {
    internal fun filenameForChapter(chapterIndex: Int, extension: String = "png"): String =
        "scene-" + chapterIndex + "." + extension

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
        val seed = (System.nanoTime() and Long.MAX_VALUE).coerceAtLeast(1L)
        setSeed(timelineKey, seed)
        return seed
    }

    fun setSeed(timelineKey: String, seed: Long) {
        require(seed > 0L) { "Seed must be positive" }
        val root = File(context.filesDir, "generated/$timelineKey").apply { mkdirs() }
        File(root, "seed.txt").writeText(seed.toString())
    }

    fun persist(timelineKey: String, scenes: List<GeneratedScene>): List<GeneratedScene> {
        val root = File(context.filesDir, "generated/$timelineKey").apply { mkdirs() }

        return scenes.map { scene ->
            val mimeType = context.contentResolver.getType(scene.imageUri)
            val extension = mimeType
                ?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) }
                ?.takeIf { it.isNotBlank() }
                ?: scene.imageUri.lastPathSegment
                    ?.substringAfterLast(".", "")
                    ?.takeIf { it.isNotBlank() }
                ?: "png"
            val target = File(root, filenameForChapter(scene.chapterIndex, extension))
            val temporary = File(
                root,
                ".scene-" + scene.chapterIndex + "-" + System.nanoTime() + ".tmp"
            )
            val backup = File(root, "." + target.name + ".bak")
            val previousFiles = root.listFiles()
                ?.filter { it.isFile && chapterIndexFromFilename(it.name) == scene.chapterIndex }
                .orEmpty()

            try {
                context.contentResolver.openInputStream(scene.imageUri)?.use { input ->
                    temporary.outputStream().use { output -> input.copyTo(output) }
                } ?: error("Unable to persist generated scene " + scene.chapterIndex)
                require(temporary.length() > 0L) {
                    "Generated scene copy is empty for chapter " + (scene.chapterIndex + 1)
                }

                backup.delete()
                val currentTarget = previousFiles.firstOrNull { it.absolutePath == target.absolutePath }
                if (currentTarget != null && !currentTarget.renameTo(backup)) {
                    error("Unable to prepare generated scene replacement")
                }

                if (!temporary.renameTo(target)) {
                    if (backup.exists()) {
                        backup.renameTo(target)
                    }
                    error("Unable to finalize generated scene replacement")
                }

                previousFiles
                    .filter { it.absolutePath != target.absolutePath }
                    .forEach { it.delete() }
                backup.delete()
            } catch (error: Throwable) {
                temporary.delete()
                if (!target.exists() && backup.exists()) {
                    backup.renameTo(target)
                }
                throw error
            }

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
        recoverInterruptedWrites(root)

        return root.listFiles()
            ?.filter { it.isFile && it.name.startsWith("scene-") }
            ?.sortedBy { chapterIndexFromFilename(it.name) ?: Int.MAX_VALUE }
            ?.map { file ->
                val chapterIndex = chapterIndexFromFilename(file.name) ?: return@map null
                if (!isValidImage(file)) {
                    file.delete()
                    return@map null
                }
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

    private fun isValidImage(file: File): Boolean {
        if (!file.exists() || file.length() <= 0L) return false
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        return bounds.outWidth > 0 && bounds.outHeight > 0
    }

    private fun recoverInterruptedWrites(root: File) {
        root.listFiles()
            ?.filter { it.isFile && it.name.startsWith(".scene-") && it.name.endsWith(".tmp") }
            ?.forEach { it.delete() }

        root.listFiles()
            ?.filter { it.isFile && it.name.startsWith(".scene-") && it.name.endsWith(".bak") }
            ?.forEach { backup ->
                val originalName = backup.name
                    .removePrefix(".")
                    .removeSuffix(".bak")
                val chapterIndex = chapterIndexFromFilename(originalName)
                    ?: run {
                        backup.delete()
                        return@forEach
                    }
                val hasScene = root.listFiles()
                    ?.any { file ->
                        file.isFile &&
                            file.name.startsWith("scene-") &&
                            chapterIndexFromFilename(file.name) == chapterIndex
                    }
                    ?: false
                if (hasScene) {
                    backup.delete()
                } else {
                    val restored = File(root, originalName)
                    if (!backup.renameTo(restored)) {
                        backup.delete()
                    }
                }
            }
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

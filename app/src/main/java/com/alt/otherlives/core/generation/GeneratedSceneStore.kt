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

    fun createSeed(): Long =
        (System.nanoTime() and Long.MAX_VALUE).coerceAtLeast(1L)

    fun resetSeed(timelineKey: String): Long {
        val seed = createSeed()
        setSeed(timelineKey, seed)
        return seed
    }

    fun setSeed(timelineKey: String, seed: Long) {
        require(seed > 0L) { "Seed must be positive" }
        val root = File(context.filesDir, "generated/$timelineKey").apply { mkdirs() }
        val target = File(root, "seed.txt")
        val temporary = File(root, ".seed-" + System.nanoTime() + ".tmp")
        val backup = File(root, ".seed.bak")
        try {
            temporary.writeText(seed.toString())
            require(temporary.length() > 0L) { "Seed write failed" }

            backup.delete()
            if (target.exists() && !target.renameTo(backup)) {
                error("Unable to prepare timeline seed replacement")
            }
            if (!temporary.renameTo(target)) {
                if (backup.exists()) {
                    backup.renameTo(target)
                }
                error("Unable to finalize timeline seed")
            }
            backup.delete()
        } catch (error: Throwable) {
            temporary.delete()
            if (!target.exists() && backup.exists()) {
                backup.renameTo(target)
            }
            throw error
        }
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

    fun replaceBatchAtomically(
        timelineKey: String,
        scenes: List<GeneratedScene>,
        seed: Long? = null
    ): List<GeneratedScene> {
        if (scenes.isEmpty()) return emptyList()
        SceneBatchValidation.validateChapterIndexes(
            scenes.map { it.chapterIndex }
        )
        val root = File(context.filesDir, "generated/$timelineKey").apply { mkdirs() }
        val transaction = File(root, ".batch-" + System.nanoTime()).apply { mkdirs() }
        val stagedDir = File(transaction, "staged").apply { mkdirs() }
        val backupDir = File(transaction, "backup").apply { mkdirs() }

        val staged = mutableListOf<Pair<Int, File>>()
        try {
            scenes.forEach { scene ->
                val mimeType = context.contentResolver.getType(scene.imageUri)
                val extension = mimeType
                    ?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) }
                    ?.takeIf { it.isNotBlank() }
                    ?: scene.imageUri.lastPathSegment
                        ?.substringAfterLast(".", "")
                        ?.takeIf { it.isNotBlank() }
                    ?: "png"
                val stagedFile = File(stagedDir, filenameForChapter(scene.chapterIndex, extension))
                context.contentResolver.openInputStream(scene.imageUri)?.use { input ->
                    stagedFile.outputStream().use { output -> input.copyTo(output) }
                } ?: error("Unable to stage generated scene " + scene.chapterIndex)
                require(isValidImage(stagedFile)) {
                    "Generated scene is invalid for chapter " + (scene.chapterIndex + 1)
                }
                staged += scene.chapterIndex to stagedFile
            }

            val affectedIndexes = staged.map { it.first }.toSet()
            File(transaction, "affected.txt").writeText(
                affectedIndexes.sorted().joinToString(",")
            )
            if (seed != null) {
                require(seed > 0L) { "Seed must be positive" }
                File(transaction, "seed.included").writeText("1")
                File(transaction, "seed.pending").writeText(seed.toString())
            }

            val previousFiles = root.listFiles()
                ?.filter {
                    it.isFile &&
                        it.name.startsWith("scene-") &&
                        chapterIndexFromFilename(it.name) in affectedIndexes
                }
                .orEmpty()

            previousFiles.forEach { oldFile ->
                val backup = File(backupDir, oldFile.name)
                if (!oldFile.renameTo(backup)) {
                    error("Unable to prepare atomic scene replacement")
                }
            }
            val seedTarget = File(root, "seed.txt")
            if (seed != null && seedTarget.exists()) {
                if (!seedTarget.renameTo(File(backupDir, "seed.txt"))) {
                    error("Unable to prepare atomic seed replacement")
                }
            }
            File(transaction, "commit.started").writeText("1")

            val committed = mutableListOf<File>()
            try {
                staged.forEach { (_, stagedFile) ->
                    val target = File(root, stagedFile.name)
                    if (!stagedFile.renameTo(target)) {
                        error("Unable to commit atomic scene replacement")
                    }
                    committed += target
                }
                if (seed != null) {
                    val pendingSeed = File(transaction, "seed.pending")
                    if (!pendingSeed.renameTo(seedTarget)) {
                        error("Unable to commit atomic timeline seed")
                    }
                    committed += seedTarget
                }
            } catch (error: Throwable) {
                committed.forEach { it.delete() }
                backupDir.listFiles()?.forEach { backup ->
                    backup.renameTo(File(root, backup.name))
                }
                throw error
            }

            val result = staged.map { (chapterIndex, stagedFile) ->
                val target = File(root, stagedFile.name)
                GeneratedScene(
                    chapterIndex,
                    FileProvider.getUriForFile(
                        context,
                        context.packageName + ".fileprovider",
                        target
                    )
                )
            }

            File(transaction, "commit.completed").writeText("1")
            val completedTransaction = File(
                root,
                ".batch-completed-" + transaction.name.removePrefix(".batch-")
            )
            if (transaction.renameTo(completedTransaction)) {
                completedTransaction.deleteRecursively()
            }
            return result
        } catch (error: Throwable) {
            backupDir.listFiles()?.forEach { backup ->
                val target = File(root, backup.name)
                if (!target.exists()) backup.renameTo(target)
            }
            transaction.deleteRecursively()
            throw error
        }
    }

    fun load(timelineKey: String): List<GeneratedScene> {
        val root = File(context.filesDir, "generated/$timelineKey")
        if (!root.exists()) return emptyList()
        recoverInterruptedWrites(root)

        val canonicalFiles = root.listFiles()
            ?.filter { it.isFile && it.name.startsWith("scene-") }
            ?.mapNotNull { file ->
                val chapterIndex = chapterIndexFromFilename(file.name) ?: return@mapNotNull null
                if (!isValidImage(file)) {
                    file.delete()
                    return@mapNotNull null
                }
                chapterIndex to file
            }
            ?.groupBy({ it.first }, { it.second })
            ?.mapValues { (_, files) ->
                val canonical = files.maxWithOrNull(
                    compareBy<File> { it.lastModified() }.thenBy { it.name }
                )
                files.filter { it != canonical }.forEach { it.delete() }
                canonical
            }
            .orEmpty()

        return canonicalFiles
            .toSortedMap()
            .mapNotNull { (chapterIndex, file) ->
                file ?: return@mapNotNull null
                GeneratedScene(
                    chapterIndex = chapterIndex,
                    imageUri = FileProvider.getUriForFile(
                        context,
                        context.packageName + ".fileprovider",
                        file
                    )
                )
            }
    }

    private fun isValidImage(file: File): Boolean {
        if (!file.exists() || file.length() <= 0L) return false
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        return bounds.outWidth > 0 && bounds.outHeight > 0
    }

    private fun recoverInterruptedBatchWrites(root: File) {
        root.listFiles()
            ?.filter { it.isDirectory && it.name.startsWith(".batch-") }
            ?.forEach { transaction ->
                if (transaction.name.startsWith(".batch-completed-")) {
                    transaction.deleteRecursively()
                    return@forEach
                }

                val stagedDir = File(transaction, "staged")
                val backupDir = File(transaction, "backup")
                val commitStarted = File(transaction, "commit.started").exists()
                val commitCompleted = File(transaction, "commit.completed").exists()

                val recoveryPlan = SceneTransactionRecoveryPlan.build(
                    commitStarted = commitStarted,
                    commitCompleted = commitCompleted,
                    affectedIndexesText = File(transaction, "affected.txt")
                        .takeIf { it.exists() }
                        ?.readText(),
                    seedIncluded = File(transaction, "seed.included").exists() ||
                        File(transaction, "seed.pending").exists() ||
                        File(backupDir, "seed.txt").exists()
                )
                if (commitCompleted) {
                    transaction.deleteRecursively()
                    return@forEach
                }

                recoveryPlan?.let { plan ->
                    root.listFiles()
                        ?.filter {
                            it.isFile &&
                                it.name.startsWith("scene-") &&
                                chapterIndexFromFilename(it.name) in plan.affectedChapterIndexes
                        }
                        ?.forEach { it.delete() }

                    if (plan.deleteCurrentSeed) {
                        File(root, "seed.txt").delete()
                    }

                    backupDir.listFiles()?.forEach { backup ->
                        backup.renameTo(File(root, backup.name))
                    }
                }

                transaction.deleteRecursively()
            }
    }

    private fun recoverInterruptedWrites(root: File) {
        recoverInterruptedBatchWrites(root)

        root.listFiles()
            ?.filter { it.isFile && it.name.startsWith(".seed-") && it.name.endsWith(".tmp") }
            ?.forEach { it.delete() }

        val seedBackup = File(root, ".seed.bak")
        val seedTarget = File(root, "seed.txt")
        if (seedBackup.exists()) {
            if (seedTarget.exists()) {
                seedBackup.delete()
            } else {
                seedBackup.renameTo(seedTarget)
            }
        }

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

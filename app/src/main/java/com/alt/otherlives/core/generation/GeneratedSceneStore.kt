package com.alt.otherlives.core.generation

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import android.webkit.MimeTypeMap
import android.graphics.BitmapFactory
import java.io.File
import com.alt.otherlives.core.io.BoundedStreamCopy
import com.alt.otherlives.core.media.ImageBoundsValidation

class GeneratedSceneStore(private val context: Context) {
    internal fun filenameForChapter(chapterIndex: Int, extension: String = "png"): String =
        GeneratedSceneFileName.forChapter(chapterIndex, extension)

    internal fun chapterIndexFromFilename(name: String): Int? =
        GeneratedSceneFileName.chapterIndex(name)

    fun getOrCreateSeed(timelineKey: String): Long {
        val root = timelineRoot(timelineKey).apply { mkdirs() }
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
        val root = timelineRoot(timelineKey).apply { mkdirs() }
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
                restoreBackupOrThrow(
                    target = target,
                    backup = backup,
                    message = "Unable to restore previous timeline seed"
                )
                error("Unable to finalize timeline seed")
            }
            backup.delete()
        } catch (error: Throwable) {
            temporary.delete()
            runCatching {
                restoreBackupOrThrow(
                    target = target,
                    backup = backup,
                    message = "Unable to restore previous timeline seed"
                )
            }.onFailure { restoreError ->
                restoreError.addSuppressed(error)
                throw restoreError
            }
            throw error
        }
    }

    private fun restoreBackupOrThrow(
        target: File,
        backup: File,
        message: String
    ) {
        if (!target.exists() && backup.exists() && !backup.renameTo(target)) {
            error(message)
        }
    }

    fun persist(timelineKey: String, scenes: List<GeneratedScene>): List<GeneratedScene> {
        val root = timelineRoot(timelineKey).apply { mkdirs() }
        recoverInterruptedWrites(root)

        return scenes.map { scene ->
            val mimeType = context.contentResolver.getType(scene.imageUri)
            val extension = GeneratedImageExtension.normalize(
                mimeExtension = mimeType
                    ?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) },
                filename = scene.imageUri.lastPathSegment
            )
            val target = File(root, filenameForChapter(scene.chapterIndex, extension))
            val temporary = File(
                root,
                ".scene-" + scene.chapterIndex + "-" + System.nanoTime() + ".tmp"
            )
            val backup = File(root, "." + target.name + ".bak")
            val previousFiles = root.listFiles()
                ?.filter {
                    it.isFile &&
                        it.name.startsWith("scene-") &&
                        chapterIndexFromFilename(it.name) == scene.chapterIndex
                }
                .orEmpty()

            try {
                context.contentResolver.openInputStream(scene.imageUri)?.use { input ->
                    temporary.outputStream().use { output ->
                    BoundedStreamCopy.copy(
                        input = input,
                        output = output,
                        maxBytes = MAX_PERSISTED_SCENE_BYTES
                    )
                }
                } ?: error("Unable to persist generated scene " + scene.chapterIndex)
                require(temporary.length() > 0L) {
                    "Generated scene copy is empty for chapter " + (scene.chapterIndex + 1)
                }
                require(isValidImage(temporary)) {
                    "Generated scene is invalid for chapter " + (scene.chapterIndex + 1)
                }

                backup.delete()
                val currentTarget = previousFiles.firstOrNull { it.absolutePath == target.absolutePath }
                if (currentTarget != null && !currentTarget.renameTo(backup)) {
                    error("Unable to prepare generated scene replacement")
                }

                if (!temporary.renameTo(target)) {
                    restoreBackupOrThrow(
                        target = target,
                        backup = backup,
                        message = "Unable to restore previous generated scene"
                    )
                    error("Unable to finalize generated scene replacement")
                }

                previousFiles
                    .filter { it.absolutePath != target.absolutePath }
                    .forEach { it.delete() }
                backup.delete()
            } catch (error: Throwable) {
                temporary.delete()
                runCatching {
                    restoreBackupOrThrow(
                        target = target,
                        backup = backup,
                        message = "Unable to restore previous generated scene"
                    )
                }.onFailure { restoreError ->
                    restoreError.addSuppressed(error)
                    throw restoreError
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
        val root = timelineRoot(timelineKey).apply { mkdirs() }
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
                    stagedFile.outputStream().use { output ->
                        BoundedStreamCopy.copy(
                            input = input,
                            output = output,
                            maxBytes = MAX_PERSISTED_SCENE_BYTES
                        )
                    }
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

            staged.forEach { (_, stagedFile) ->
                val target = File(root, stagedFile.name)
                if (!stagedFile.renameTo(target)) {
                    error("Unable to commit atomic scene replacement")
                }
            }
            if (seed != null) {
                val pendingSeed = File(transaction, "seed.pending")
                if (!pendingSeed.renameTo(seedTarget)) {
                    error("Unable to commit atomic timeline seed")
                }
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
            recoverInterruptedBatchWrites(root)
            if (transaction.exists()) {
                val rollbackError = IllegalStateException(
                    "Atomic scene rollback is incomplete; recovery data was preserved"
                )
                rollbackError.addSuppressed(error)
                throw rollbackError
            }
            throw error
        }
    }

    fun load(timelineKey: String): List<GeneratedScene> {
        val root = timelineRoot(timelineKey)
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
        return ImageBoundsValidation.isReasonable(
            bounds.outWidth,
            bounds.outHeight
        )
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

                if (
                    SceneTransactionRecoveryPlan.shouldRestoreBackupsBeforeCommit(
                        commitStarted = commitStarted,
                        commitCompleted = commitCompleted,
                        hasBackups = backupDir.listFiles()?.isNotEmpty() == true
                    )
                ) {
                    val restoreSucceeded = backupDir.listFiles()
                        .orEmpty()
                        .all { backup ->
                            val target = File(root, backup.name)
                            if (target.exists()) {
                                true
                            } else {
                                runCatching {
                                    backup.inputStream().use { input ->
                                        target.outputStream().use { output ->
                                            input.copyTo(output)
                                        }
                                    }
                                    target.length() == backup.length() && target.length() > 0L
                                }.getOrDefault(false)
                            }
                        }

                    if (restoreSucceeded) {
                        transaction.deleteRecursively()
                    }
                    return@forEach
                }

                if (!commitStarted) {
                    transaction.deleteRecursively()
                    return@forEach
                }

                val affectedFile = File(transaction, "affected.txt")
                val fallbackAffectedIndexes = buildSet {
                    stagedDir.listFiles()?.forEach { file ->
                        chapterIndexFromFilename(file.name)?.let(::add)
                    }
                    backupDir.listFiles()?.forEach { file ->
                        chapterIndexFromFilename(file.name)?.let(::add)
                    }
                }
                val affectedIndexesText = runCatching {
                    affectedFile.takeIf { it.exists() }?.readText()
                }.getOrNull()
                    ?.takeIf { SceneTransactionRecoveryPlan.parseAffectedIndexes(it).isNotEmpty() }
                    ?: fallbackAffectedIndexes.sorted().joinToString(",")

                val recoveryPlan = SceneTransactionRecoveryPlan.build(
                    commitStarted = commitStarted,
                    commitCompleted = commitCompleted,
                    affectedIndexesText = affectedIndexesText,
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

                    val restoreSucceeded = backupDir.listFiles()
                        .orEmpty()
                        .all { backup ->
                            val target = File(root, backup.name)
                            runCatching {
                                backup.inputStream().use { input ->
                                    target.outputStream().use { output ->
                                        input.copyTo(output)
                                    }
                                }
                                target.length() == backup.length() && target.length() > 0L
                            }.getOrDefault(false)
                        }

                    if (!restoreSucceeded) {
                        return@forEach
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
                        return@forEach
                    }
                }
            }
    }

    fun clear(timelineKey: String) {
        val root = timelineRoot(timelineKey)
        if (root.exists()) {
            check(root.deleteRecursively()) {
                "Unable to clear generated timeline scenes"
            }
        }
    }

    fun deleteUnreferenced(keepTimelineKeys: Set<String>) {
        val root = File(context.filesDir, "generated")
        val canonicalRoot = root.canonicalFile
        val validatedKeep = keepTimelineKeys
            .map { GeneratedTimelineKey.validate(it) }
            .toSet()

        root.listFiles()?.forEach { dir ->
            if (!dir.isDirectory || dir.name in validatedKeep) return@forEach
            val canonicalDir = dir.canonicalFile
            require(canonicalDir.parentFile == canonicalRoot) {
                "Generated cleanup path escapes private storage"
            }
            check(dir.deleteRecursively()) {
                "Unable to delete unreferenced generated timeline"
            }
        }
    }

    fun clearAll() {
        val root = File(context.filesDir, "generated")
        if (root.exists()) {
            check(root.deleteRecursively()) {
                "Unable to clear generated timeline storage"
            }
        }
    }

    private fun timelineRoot(timelineKey: String): File {
        val validated = GeneratedTimelineKey.validate(timelineKey)
        val generatedRoot = File(context.filesDir, "generated").apply { mkdirs() }
        val timeline = File(generatedRoot, validated)
        require(timeline.canonicalFile.parentFile == generatedRoot.canonicalFile) {
            "Timeline storage path escapes private generated storage"
        }
        return timeline
    }

    private companion object {
        const val MAX_PERSISTED_SCENE_BYTES = 100L * 1024L * 1024L
    }
}

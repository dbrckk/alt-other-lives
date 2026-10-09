package com.alt.otherlives.core.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import android.webkit.MimeTypeMap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import java.io.File
import java.util.UUID
import com.alt.otherlives.core.io.BoundedStreamCopy
import com.alt.otherlives.core.diagnostics.DiagnosticEvent
import com.alt.otherlives.core.diagnostics.DiagnosticsRepository
import com.alt.otherlives.core.media.ImageBoundsValidation
import com.alt.otherlives.core.media.SourcePhotoQualityIssue

data class SourcePhotoDimensions(
    val width: Int,
    val height: Int
)

data class StoredPhoto(
    val fileName: String,
    val uri: Uri,
    val dimensions: SourcePhotoDimensions,
    val isLikelyPremiumSource: Boolean,
    val qualityIssues: Set<SourcePhotoQualityIssue> = emptySet()
)

class SourcePhotoStore(private val context: Context) {
    private val diagnostics = DiagnosticsRepository(context.applicationContext)
    fun import(uri: Uri): StoredPhoto {
        val dir = File(context.filesDir, "source-photos").apply { mkdirs() }
        val mimeType = context.contentResolver.getType(uri)
        require(mimeType?.startsWith("image/") == true) { "Selected file is not an image" }
        val extension = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(mimeType)
            ?.takeIf { it.isNotBlank() }
            ?: "jpg"
        SourcePhotoImportCleanup.cleanup(dir)
        val id = UUID.randomUUID().toString()
        val fileName = "source-" + id + "." + extension
        val target = File(dir, fileName)
        val temporary = File(dir, ".source-" + id + ".tmp")
        var dimensions: SourcePhotoDimensions? = null
        var isLikelyPremiumSource = false
        var qualityIssues: Set<SourcePhotoQualityIssue> = emptySet()

        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                temporary.outputStream().use { output ->
                    BoundedStreamCopy.copy(
                        input = input,
                        output = output,
                        maxBytes = MAX_SOURCE_PHOTO_BYTES
                    )
                }
            } ?: error("Unable to read selected photo")

            require(temporary.length() > 0L) { "Selected image copy is empty" }
            val importedDimensions = requireNotNull(readDimensions(temporary)) {
                "Selected image is invalid, unsupported, or too large"
            }
            dimensions = importedDimensions
            val qualityAssessment = ImageBoundsValidation.assessSourcePhoto(
                importedDimensions.width,
                importedDimensions.height
            )
            isLikelyPremiumSource = qualityAssessment.isPremiumReady
            qualityIssues = qualityAssessment.issues

            if (!temporary.renameTo(target)) {
                error("Unable to finalize selected photo import")
            }
        } catch (error: Throwable) {
            temporary.delete()
            target.delete()
            diagnostics.record(
                event = DiagnosticEvent.PHOTO_IMPORT_FAILED,
                error = error
            )
            throw error
        }

        return StoredPhoto(
            fileName = fileName,
            uri = uriFor(fileName),
            dimensions = requireNotNull(dimensions),
            isLikelyPremiumSource = isLikelyPremiumSource,
            qualityIssues = qualityIssues
        )
    }

    fun uriFor(fileName: String): Uri {
        val file = storedFile(fileName)
        require(file.exists()) { "Stored photo not found" }
        if (!isValidImage(file)) {
            file.delete()
            error("Stored photo is invalid or corrupted")
        }
        return FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            file
        )
    }

    fun isAvailable(fileName: String): Boolean {
        val file = runCatching { storedFile(fileName) }.getOrNull() ?: return false
        if (!isValidImage(file)) {
            file.delete()
            return false
        }
        return true
    }

    fun dimensions(fileName: String): SourcePhotoDimensions? {
        val file = runCatching { storedFile(fileName) }.getOrNull() ?: return null
        val dimensions = readDimensions(file)
        if (dimensions == null) {
            file.delete()
        }
        return dimensions
    }

    fun qualityIssues(fileName: String): Set<SourcePhotoQualityIssue>? {
        val dimensions = dimensions(fileName) ?: return null
        return ImageBoundsValidation.assessSourcePhoto(
            dimensions.width,
            dimensions.height
        ).issues
    }

    fun isLikelyPremiumSource(fileName: String): Boolean? {
        val dimensions = dimensions(fileName) ?: return null
        return ImageBoundsValidation.isLikelyPremiumSource(
            dimensions.width,
            dimensions.height
        )
    }

    private fun storedFile(fileName: String): File {
        require(SourcePhotoFileName.isValid(fileName)) {
            "Invalid stored photo filename"
        }
        require(File(fileName).name == fileName) { "Invalid stored photo filename" }
        val dir = File(context.filesDir, "source-photos")
        val file = File(dir, fileName)
        require(file.canonicalFile.parentFile == dir.canonicalFile) {
            "Stored photo path escapes private storage"
        }
        return file
    }

    fun latestStoredPhoto(): StoredPhoto? {
        val dir = File(context.filesDir, "source-photos")
        SourcePhotoImportCleanup.cleanup(dir)
        val files = dir.listFiles()
            ?.filter { it.isFile && SourcePhotoFileName.isValid(it.name) }
            ?.sortedByDescending { it.lastModified() }
            .orEmpty()

        files.forEach { file ->
            if (!isValidImage(file)) {
                file.delete()
            } else {
                val dimensions = requireNotNull(readDimensions(file))
                val qualityAssessment = ImageBoundsValidation.assessSourcePhoto(
                    dimensions.width,
                    dimensions.height
                )
                return StoredPhoto(
                    fileName = file.name,
                    uri = uriFor(file.name),
                    dimensions = dimensions,
                    isLikelyPremiumSource = qualityAssessment.isPremiumReady,
                    qualityIssues = qualityAssessment.issues
                )
            }
        }
        return null
    }

    private fun isValidImage(file: File): Boolean = readDimensions(file) != null

    private fun readDimensions(file: File): SourcePhotoDimensions? {
        if (!file.exists() || file.length() <= 0L) return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (!ImageBoundsValidation.isReasonable(bounds.outWidth, bounds.outHeight)) {
            return null
        }

        val orientation = runCatching {
            ExifInterface(file.absolutePath).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
        val swapsAxes = orientation == ExifInterface.ORIENTATION_TRANSPOSE ||
            orientation == ExifInterface.ORIENTATION_ROTATE_90 ||
            orientation == ExifInterface.ORIENTATION_TRANSVERSE ||
            orientation == ExifInterface.ORIENTATION_ROTATE_270
        return if (swapsAxes) {
            SourcePhotoDimensions(
                width = bounds.outHeight,
                height = bounds.outWidth
            )
        } else {
            SourcePhotoDimensions(
                width = bounds.outWidth,
                height = bounds.outHeight
            )
        }
    }

    fun deleteUnreferenced(keepFileNames: Set<String>) {
        val dir = File(context.filesDir, "source-photos")
        dir.listFiles()?.forEach { file ->
            if (file.isFile && file.name !in keepFileNames) {
                check(file.delete()) {
                    "Unable to delete unreferenced source photo"
                }
            }
        }
    }

    fun clearAll() {
        val root = File(context.filesDir, "source-photos")
        if (root.exists()) {
            check(root.deleteRecursively()) {
                "Unable to clear private source photos"
            }
        }
    }

    private companion object {
        const val MAX_SOURCE_PHOTO_BYTES = 50L * 1024L * 1024L
    }
}

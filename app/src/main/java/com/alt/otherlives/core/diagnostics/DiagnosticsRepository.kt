package com.alt.otherlives.core.diagnostics

import android.content.Context
import com.alt.otherlives.BuildConfig
import java.io.File

enum class DiagnosticEvent {
    APP_CRASH,
    PHOTO_IMPORT_FAILED,
    AI_GENERATION_FAILED,
    AI_CHAPTER_FAILED,
    AI_QUALITY_REJECTED,
    SETTINGS_SAVE_FAILED,
    SETTINGS_CONNECTION_FAILED,
    SETTINGS_CLEAR_FAILED
}

data class DiagnosticEntry(
    val timestampMillis: Long,
    val event: DiagnosticEvent,
    val errorType: String? = null,
    val appLocation: String? = null
)

class DiagnosticsRepository(
    context: Context,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    private val file = File(context.filesDir, "diagnostics/events.log")

    fun record(
        event: DiagnosticEvent,
        error: Throwable? = null
    ) {
        runCatching {
            synchronized(FILE_LOCK) {
                val entries = readEntriesUnsafe().toMutableList()
                entries += DiagnosticEntry(
                    timestampMillis = clock(),
                    event = event,
                    errorType = error?.javaClass?.simpleName?.sanitized(MAX_ERROR_TYPE_LENGTH),
                    appLocation = error?.firstAppLocation()
                )
                writeEntriesUnsafe(entries.takeLast(MAX_ENTRIES))
            }
        }
    }

    fun exportText(): String = synchronized(FILE_LOCK) {
        buildString {
            appendLine("ALT diagnostics")
            appendLine("version=${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            appendLine("privacy=no messages, URLs, prompts, URIs, file paths, or media content")
            readEntriesUnsafe().forEach { entry ->
                append(entry.timestampMillis)
                append(" | ")
                append(entry.event.name)
                entry.errorType?.let {
                    append(" | ")
                    append(it)
                }
                entry.appLocation?.let {
                    append(" | ")
                    append(it)
                }
                appendLine()
            }
        }.trimEnd()
    }

    fun clear() {
        runCatching {
            synchronized(FILE_LOCK) {
                if (file.exists()) {
                    file.delete()
                }
            }
        }
    }

    internal fun entriesForTest(): List<DiagnosticEntry> = synchronized(FILE_LOCK) {
        readEntriesUnsafe()
    }

    private fun readEntriesUnsafe(): List<DiagnosticEntry> {
        if (!file.exists()) return emptyList()
        return file.readLines()
            .mapNotNull(::decode)
            .takeLast(MAX_ENTRIES)
    }

    private fun writeEntriesUnsafe(entries: List<DiagnosticEntry>) {
        file.parentFile?.mkdirs()
        val temporary = File(file.parentFile, file.name + ".tmp")
        temporary.writeText(
            entries.joinToString(separator = "\n", postfix = if (entries.isEmpty()) "" else "\n") {
                encode(it)
            }
        )
        check(temporary.renameTo(file) || run {
            file.delete()
            temporary.renameTo(file)
        }) {
            "Unable to persist diagnostics"
        }
    }

    private fun encode(entry: DiagnosticEntry): String = listOf(
        entry.timestampMillis.toString(),
        entry.event.name,
        entry.errorType.orEmpty(),
        entry.appLocation.orEmpty()
    ).joinToString("|")

    private fun decode(line: String): DiagnosticEntry? {
        val parts = line.split('|')
        if (parts.size != 4) return null
        val timestamp = parts[0].toLongOrNull() ?: return null
        val event = runCatching { DiagnosticEvent.valueOf(parts[1]) }.getOrNull() ?: return null
        return DiagnosticEntry(
            timestampMillis = timestamp,
            event = event,
            errorType = parts[2].takeIf { it.isNotBlank() },
            appLocation = parts[3].takeIf { it.isNotBlank() }
        )
    }

    private fun Throwable.firstAppLocation(): String? =
        stackTrace
            .firstOrNull { frame ->
                frame.className.startsWith(APP_PACKAGE_PREFIX) &&
                    !frame.className.startsWith(DIAGNOSTICS_PACKAGE_PREFIX)
            }
            ?.let { frame ->
                (frame.className.removePrefix(APP_PACKAGE_PREFIX) + "#" + frame.methodName)
                    .sanitized(MAX_LOCATION_LENGTH)
            }

    private fun String.sanitized(maxLength: Int): String =
        filter { character ->
            character.isLetterOrDigit() || character in "._$#"
        }.take(maxLength)

    private companion object {
        const val MAX_ENTRIES = 50
        const val MAX_ERROR_TYPE_LENGTH = 80
        const val MAX_LOCATION_LENGTH = 160
        const val APP_PACKAGE_PREFIX = "com.alt.otherlives."
        const val DIAGNOSTICS_PACKAGE_PREFIX = "com.alt.otherlives.core.diagnostics."
        val FILE_LOCK = Any()
    }
}

object AppCrashDiagnostics {
    @Volatile
    private var installed = false

    fun install(context: Context) {
        if (installed) return
        synchronized(this) {
            if (installed) return
            val previous = Thread.getDefaultUncaughtExceptionHandler() ?: return
            val appContext = context.applicationContext
            Thread.setDefaultUncaughtExceptionHandler { thread, error ->
                runCatching {
                    DiagnosticsRepository(appContext).record(
                        event = DiagnosticEvent.APP_CRASH,
                        error = error
                    )
                }
                previous.uncaughtException(thread, error)
            }
            installed = true
        }
    }
}

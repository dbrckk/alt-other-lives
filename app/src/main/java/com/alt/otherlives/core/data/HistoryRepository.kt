package com.alt.otherlives.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.alt.otherlives.core.media.NormalizedCropRect

private val Context.altDataStore by preferencesDataStore(name = "alt_local")

data class HistoryEntry(
    val scenarioId: String,
    val createdAt: Long,
    val photoFileName: String? = null,
    val sourceCrop: NormalizedCropRect = NormalizedCropRect.Full
) {
    val timelineKey: String
        get() = scenarioId + "-" + createdAt
}

internal object HistoryEntryValidation {
    private val scenarioIds = ScenarioCatalog.scenarios.map { it.id }.toSet()

    fun isValid(
        scenarioId: String,
        createdAt: Long,
        photoFileName: String?
    ): Boolean {
        if (createdAt <= 0L) return false
        if (scenarioId !in scenarioIds) return false
        if (photoFileName == null) return true
        return SourcePhotoFileName.isValid(photoFileName)
    }
}

internal object HistoryEntryCodec {
    private const val SEPARATOR = "\n"
    private const val FIELD_SEPARATOR = "|"

    fun encode(entries: List<HistoryEntry>): String =
        entries.joinToString(SEPARATOR) {
            listOf(
                it.scenarioId,
                it.createdAt,
                it.photoFileName ?: "",
                it.sourceCrop.left,
                it.sourceCrop.top,
                it.sourceCrop.right,
                it.sourceCrop.bottom
            ).joinToString(FIELD_SEPARATOR)
        }

    fun decode(value: String): List<HistoryEntry> =
        HistoryEntryListPolicy.sanitize(
            value.lineSequence()
                .mapNotNull { row ->
                    val parts = row.split(FIELD_SEPARATOR, limit = 7)
                    val time = parts.getOrNull(1)?.toLongOrNull()
                    val id = parts.firstOrNull()?.takeIf { it.isNotBlank() }
                    val photoFileName = parts.getOrNull(2)?.takeIf { it.isNotBlank() }
                    val sourceCrop = if (parts.size >= 7) {
                        runCatching {
                            NormalizedCropRect(
                                left = requireNotNull(parts[3].toFloatOrNull()),
                                top = requireNotNull(parts[4].toFloatOrNull()),
                                right = requireNotNull(parts[5].toFloatOrNull()),
                                bottom = requireNotNull(parts[6].toFloatOrNull())
                            )
                        }.getOrNull() ?: return@mapNotNull null
                    } else {
                        NormalizedCropRect.Full
                    }
                    if (
                        id != null &&
                        time != null &&
                        HistoryEntryValidation.isValid(id, time, photoFileName)
                    ) {
                        HistoryEntry(
                            scenarioId = id,
                            createdAt = time,
                            photoFileName = photoFileName,
                            sourceCrop = sourceCrop
                        )
                    } else {
                        null
                    }
                }
        )
}

class HistoryRepository(private val context: Context) {
    private val historyKey = stringPreferencesKey("history")

    val history: Flow<List<HistoryEntry>> = context.altDataStore.data.map { prefs ->
        HistoryEntryCodec.decode(prefs[historyKey].orEmpty())
    }

    data class RecordResult(
        val photoFileNames: Set<String>,
        val timelineKeys: Set<String>,
        val recordedEntry: HistoryEntry? = null
    )

    suspend fun record(
        scenarioId: String,
        photoFileName: String? = null,
        sourceCrop: NormalizedCropRect = NormalizedCropRect.Full,
        createdAt: Long = System.currentTimeMillis()
    ): RecordResult {
        require(HistoryEntryValidation.isValid(scenarioId, createdAt, photoFileName)) {
            "Invalid history entry"
        }
        var referencedPhotoFileNames = emptySet<String>()
        var referencedTimelineKeys = emptySet<String>()
        var recordedEntry: HistoryEntry? = null
        context.altDataStore.edit { prefs ->
            val current = HistoryEntryCodec.decode(prefs[historyKey].orEmpty())
            val uniqueCreatedAt = HistoryTimestampAllocator.nextAvailable(
                scenarioId = scenarioId,
                requestedCreatedAt = createdAt,
                existingEntries = current
            )
            val entry = HistoryEntry(
                scenarioId = scenarioId,
                createdAt = uniqueCreatedAt,
                photoFileName = photoFileName,
                sourceCrop = sourceCrop
            )
            val updated = (listOf(entry) + current).take(HistoryEntryListPolicy.MAX_ENTRIES)
            recordedEntry = entry
            prefs[historyKey] = HistoryEntryCodec.encode(updated)
            referencedPhotoFileNames = updated.mapNotNull { it.photoFileName }.toSet()
            referencedTimelineKeys = updated.map { it.timelineKey }.toSet()
        }
        return RecordResult(
            photoFileNames = referencedPhotoFileNames,
            timelineKeys = referencedTimelineKeys,
            recordedEntry = recordedEntry
        )
    }

    suspend fun remove(entry: HistoryEntry): RecordResult {
        var referencedPhotoFileNames = emptySet<String>()
        var referencedTimelineKeys = emptySet<String>()
        context.altDataStore.edit { prefs ->
            val current = decode(prefs[historyKey].orEmpty())
            val updated = current.filterNot {
                it.scenarioId == entry.scenarioId &&
                    it.createdAt == entry.createdAt
            }
            if (updated.isEmpty()) {
                prefs.remove(historyKey)
            } else {
                prefs[historyKey] = HistoryEntryCodec.encode(updated)
            }
            referencedPhotoFileNames = updated.mapNotNull { it.photoFileName }.toSet()
            referencedTimelineKeys = updated.map { it.timelineKey }.toSet()
        }
        return RecordResult(
            photoFileNames = referencedPhotoFileNames,
            timelineKeys = referencedTimelineKeys
        )
    }

    suspend fun clear() {
        context.altDataStore.edit { it.remove(historyKey) }
    }

}

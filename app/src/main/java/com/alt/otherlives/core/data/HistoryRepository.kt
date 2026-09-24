package com.alt.otherlives.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.altDataStore by preferencesDataStore(name = "alt_local")

data class HistoryEntry(
    val scenarioId: String,
    val createdAt: Long,
    val photoFileName: String? = null
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

class HistoryRepository(private val context: Context) {
    private val historyKey = stringPreferencesKey("history")

    val history: Flow<List<HistoryEntry>> = context.altDataStore.data.map { prefs ->
        decode(prefs[historyKey].orEmpty())
    }

    data class RecordResult(
        val photoFileNames: Set<String>,
        val timelineKeys: Set<String>
    )

    suspend fun record(
        scenarioId: String,
        photoFileName: String? = null,
        createdAt: Long = System.currentTimeMillis()
    ): RecordResult {
        require(HistoryEntryValidation.isValid(scenarioId, createdAt, photoFileName)) {
            "Invalid history entry"
        }
        var referencedPhotoFileNames = emptySet<String>()
        var referencedTimelineKeys = emptySet<String>()
        context.altDataStore.edit { prefs ->
            val current = decode(prefs[historyKey].orEmpty())
            val updated = (listOf(HistoryEntry(scenarioId, createdAt, photoFileName)) + current).take(MAX_ENTRIES)
            prefs[historyKey] = encode(updated)
            referencedPhotoFileNames = updated.mapNotNull { it.photoFileName }.toSet()
            referencedTimelineKeys = updated.map { it.timelineKey }.toSet()
        }
        return RecordResult(
            photoFileNames = referencedPhotoFileNames,
            timelineKeys = referencedTimelineKeys
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
                prefs[historyKey] = encode(updated)
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

    private fun encode(entries: List<HistoryEntry>): String =
        entries.joinToString(SEPARATOR) {
            it.scenarioId + FIELD_SEPARATOR + it.createdAt + FIELD_SEPARATOR + (it.photoFileName ?: "")
        }

    private fun decode(value: String): List<HistoryEntry> =
        value.split(SEPARATOR)
            .mapNotNull { row ->
                val parts = row.split(FIELD_SEPARATOR)
                val time = parts.getOrNull(1)?.toLongOrNull()
                val id = parts.firstOrNull()?.takeIf { it.isNotBlank() }
                val photoFileName = parts.getOrNull(2)?.takeIf { it.isNotBlank() }
                if (
                    id != null &&
                    time != null &&
                    HistoryEntryValidation.isValid(id, time, photoFileName)
                ) {
                    HistoryEntry(id, time, photoFileName)
                } else {
                    null
                }
            }

    private companion object {
        const val MAX_ENTRIES = 50
        const val SEPARATOR = "\n"
        const val FIELD_SEPARATOR = "|"
    }
}

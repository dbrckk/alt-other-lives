package com.alt.otherlives.core.data

internal object HistoryEntryListPolicy {
    const val MAX_ENTRIES = 50

    fun sanitize(entries: Sequence<HistoryEntry>): List<HistoryEntry> =
        entries
            .distinctBy { it.timelineKey }
            .take(MAX_ENTRIES)
            .toList()
}

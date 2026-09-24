package com.alt.otherlives.feature.history

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.alt.otherlives.core.data.HistoryEntry
import com.alt.otherlives.core.designsystem.AltAccent
import com.alt.otherlives.core.designsystem.AltCard
import com.alt.otherlives.core.designsystem.AltMuted
import com.alt.otherlives.core.designsystem.AltSurface
import com.alt.otherlives.core.model.Scenario
import java.text.DateFormat
import java.util.Date

@Composable
fun HistoryScreen(
    entries: List<HistoryEntry>,
    scenarios: List<Scenario>,
    onBack: () -> Unit,
    onOpen: (Scenario, HistoryEntry) -> Unit,
    onDelete: (HistoryEntry) -> Unit,
    deletingEntryKey: String? = null,
    isClearingHistory: Boolean = false,
    onClear: () -> Unit,
    unavailablePhotoFileNames: Set<String> = emptySet(),
    photoUrisByFileName: Map<String, Uri> = emptyMap(),
    generatedPreviewUrisByTimelineKey: Map<String, Uri> = emptyMap()
) {
    var showClearConfirmation by remember { mutableStateOf(false) }
    var pendingDeleteEntry by remember { mutableStateOf<HistoryEntry?>(null) }

    Column(Modifier.fillMaxSize().padding(top = 42.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "‹",
                fontSize = 36.sp,
                modifier = Modifier.clickable(
                    enabled = !isClearingHistory,
                    onClick = onBack
                )
            )
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text("Your other lives", fontSize = 30.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    if (entries.isEmpty()) {
                        "Stored privately on this device"
                    } else {
                        "${entries.size} saved ${if (entries.size == 1) "life" else "lives"} • private to this device"
                    },
                    color = AltMuted,
                    fontSize = 13.sp
                )
            }
        }

        if (entries.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(AltCard),
                    contentAlignment = Alignment.Center
                ) {
                    Text("ALT", color = AltAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(20.dp))
                Text("No alternate lives yet.", fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text("Create one and it will appear here.", color = AltMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(entries, key = { it.scenarioId + ":" + it.createdAt }) { entry ->
                    val scenario = scenarios.firstOrNull { it.id == entry.scenarioId }
                    if (scenario != null) {
                        val timelineKey = entry.timelineKey
                        val hasAiPreview = timelineKey in generatedPreviewUrisByTimelineKey
                        val previewUri = entry.photoFileName
                            ?.let { photoUrisByFileName[it] }
                            ?: generatedPreviewUrisByTimelineKey[timelineKey]
                        val entryKey = entry.scenarioId + ":" + entry.createdAt
                        val isDeleting = deletingEntryKey == entryKey
                        val mediaStatus = when {
                            entry.photoFileName == null && hasAiPreview -> "AI preview"
                            entry.photoFileName == null -> "No original photo"
                            entry.photoFileName in unavailablePhotoFileNames && hasAiPreview -> "AI preview"
                            entry.photoFileName in unavailablePhotoFileNames -> "Photo unavailable"
                            else -> null
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    enabled = !isDeleting && !isClearingHistory,
                                    onClick = { onOpen(scenario, entry) }
                                ),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = AltCard)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(92.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(AltSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (previewUri != null) {
                                        AsyncImage(
                                            model = previewUri,
                                            contentDescription = "Preview for ${scenario.title}",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(
                                            scenario.title.take(1).uppercase(),
                                            color = AltAccent,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        scenario.title,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        DateFormat.getDateTimeInstance(
                                            DateFormat.MEDIUM,
                                            DateFormat.SHORT
                                        ).format(Date(entry.createdAt)),
                                        color = AltMuted,
                                        fontSize = 13.sp
                                    )
                                    if (mediaStatus != null) {
                                        Spacer(Modifier.height(9.dp))
                                        Surface(
                                            shape = RoundedCornerShape(999.dp),
                                            color = AltSurface
                                        ) {
                                            Text(
                                                mediaStatus,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                color = AltMuted,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("›", color = AltMuted, fontSize = 28.sp)
                                    TextButton(
                                        onClick = { pendingDeleteEntry = entry },
                                        enabled = !isDeleting && !isClearingHistory,
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                    ) {
                                        Text(
                                            if (isDeleting) "Deleting…" else "Delete",
                                            color = AltMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            TextButton(
                onClick = { showClearConfirmation = true },
                enabled = deletingEntryKey == null && !isClearingHistory,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(if (isClearingHistory) "Clearing…" else "Clear local history")
            }
        }
    }

    pendingDeleteEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { pendingDeleteEntry = null },
            title = { Text("Delete this ALT life?") },
            text = {
                Text("This removes this saved timeline and any private media no longer used by another timeline.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteEntry = null
                        onDelete(entry)
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteEntry = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("Clear local history?") },
            text = {
                Text("This removes your saved alternate lives from this device. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirmation = false
                        onClear()
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

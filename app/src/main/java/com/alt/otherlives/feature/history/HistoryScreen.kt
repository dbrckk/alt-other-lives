package com.alt.otherlives.feature.history

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alt.otherlives.core.data.HistoryEntry
import com.alt.otherlives.core.designsystem.AltCard
import com.alt.otherlives.core.designsystem.AltMuted
import com.alt.otherlives.core.model.Scenario
import java.text.DateFormat
import java.util.Date

@Composable
fun HistoryScreen(
    entries: List<HistoryEntry>,
    scenarios: List<Scenario>,
    onBack: () -> Unit,
    onOpen: (Scenario, HistoryEntry) -> Unit,
    onClear: () -> Unit,
    unavailablePhotoFileNames: Set<String> = emptySet(),
    photoUrisByFileName: Map<String, Uri> = emptyMap(),
    generatedPreviewUrisByTimelineKey: Map<String, Uri> = emptyMap()
) {
    Column(Modifier.fillMaxSize().padding(top = 42.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            Text("‹", fontSize = 36.sp, modifier = Modifier.clickable(onClick = onBack))
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text("Your other lives", fontSize = 30.sp, fontWeight = FontWeight.SemiBold)
                Text("Stored privately on this device", color = AltMuted, fontSize = 13.sp)
            }
        }
        if (entries.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
                Text("No alternate lives yet.", fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text("Create one and it will appear here.", color = AltMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(entries, key = { it.scenarioId + ":" + it.createdAt }) { entry ->
                    val scenario = scenarios.firstOrNull { it.id == entry.scenarioId }
                    if (scenario != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onOpen(scenario, entry) },
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = AltCard)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                val timelineKey = entry.scenarioId + "-" + entry.createdAt
                                val previewUri = entry.photoFileName
                                    ?.let { photoUrisByFileName[it] }
                                    ?: generatedPreviewUrisByTimelineKey[timelineKey]
                                previewUri?.let { imageUri ->
                                    AsyncImage(
                                        model = imageUri,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(88.dp)
                                            .clip(RoundedCornerShape(18.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(scenario.title, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        DateFormat.getDateTimeInstance(
                                            DateFormat.MEDIUM,
                                            DateFormat.SHORT
                                        ).format(Date(entry.createdAt)),
                                        color = AltMuted,
                                        fontSize = 13.sp
                                    )
                                    when {
                                        entry.photoFileName == null -> {
                                            Spacer(Modifier.height(6.dp))
                                            Text("Original photo not stored", color = AltMuted, fontSize = 12.sp)
                                        }
                                        entry.photoFileName in unavailablePhotoFileNames -> {
                                            Spacer(Modifier.height(6.dp))
                                            Text(
                                            if (timelineKey in generatedPreviewUrisByTimelineKey) {
                                                "Original photo unavailable • AI preview"
                                            } else {
                                                "Original photo unavailable"
                                            },
                                            color = AltMuted,
                                            fontSize = 12.sp
                                        )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            TextButton(onClick = onClear, modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                Text("Clear local history")
            }
        }
    }
}

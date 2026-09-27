package com.alt.otherlives.feature.history

import android.net.Uri
import android.widget.Toast
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.alt.otherlives.core.data.HistoryEntry
import com.alt.otherlives.core.designsystem.AltAccent
import com.alt.otherlives.core.designsystem.AltBackButton
import com.alt.otherlives.core.designsystem.AltCard
import com.alt.otherlives.core.designsystem.AltDimmed
import com.alt.otherlives.core.designsystem.AltMuted
import com.alt.otherlives.core.designsystem.AltSurface
import com.alt.otherlives.core.model.Scenario
import com.alt.otherlives.core.media.ShareCardRenderer
import com.alt.otherlives.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    var showClearConfirmation by remember { mutableStateOf(false) }
    var pendingDeleteEntry by remember { mutableStateOf<HistoryEntry?>(null) }
    var isCompareMode by remember { mutableStateOf(false) }
    var compareSelection by remember { mutableStateOf<List<String>>(emptyList()) }
    var showCompareDialog by remember { mutableStateOf(false) }
    var isSharingComparison by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(top = 42.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onBack,
                enabled = !isClearingHistory
            ) {
                Text(stringResource(R.string.common_back))
            }
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(
                    stringResource(R.string.history_library_label),
                    color = AltAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(stringResource(R.string.history_title), fontSize = 31.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    if (entries.isEmpty()) {
                        stringResource(R.string.history_private_device)
                    } else {
                        if (entries.size == 1) {
                            stringResource(R.string.history_saved_life, entries.size)
                        } else {
                            stringResource(R.string.history_saved_lives, entries.size)
                        }
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
                Text(stringResource(R.string.history_empty_title), fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.history_empty_body), color = AltMuted)
            }
        } else {
            if (entries.size >= 2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            isCompareMode = !isCompareMode
                            compareSelection = emptyList()
                            showCompareDialog = false
                        },
                        enabled = deletingEntryKey == null && !isClearingHistory
                    ) {
                        Text(
                            if (isCompareMode) {
                                stringResource(R.string.history_compare_cancel)
                            } else {
                                stringResource(R.string.history_compare_action)
                            },
                            color = AltAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (isCompareMode) {
                        Spacer(Modifier.weight(1f))
                        Text(
                            stringResource(
                                R.string.history_compare_progress,
                                compareSelection.size
                            ),
                            color = AltMuted,
                            fontSize = 12.sp
                        )
                    }
                }
                if (isCompareMode) {
                    Text(
                        stringResource(R.string.history_compare_hint),
                        color = AltMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
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
                        val previewUri = generatedPreviewUrisByTimelineKey[timelineKey]
                            ?: entry.photoFileName?.let { photoUrisByFileName[it] }
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
                                    role = Role.Button,
                                    onClick = {
                                        if (isCompareMode) {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            val key = entry.timelineKey
                                            val updated = if (key in compareSelection) {
                                                compareSelection.filterNot { it == key }
                                            } else if (compareSelection.size < 2) {
                                                compareSelection + key
                                            } else {
                                                listOf(compareSelection.last(), key)
                                            }
                                            compareSelection = updated
                                            if (updated.size == 2) {
                                                showCompareDialog = true
                                            }
                                        } else {
                                            onOpen(scenario, entry)
                                        }
                                    }
                                ),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = AltCard)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(210.dp)
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
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                                        listOf(
                                                            androidx.compose.ui.graphics.Color.Transparent,
                                                            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f)
                                                        )
                                                    )
                                                )
                                        )
                                    } else {
                                        Text(
                                            scenario.title.take(1).uppercase(),
                                            color = AltAccent,
                                            fontSize = 42.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (isCompareMode && timelineKey in compareSelection) {
                                        Surface(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(14.dp),
                                            shape = RoundedCornerShape(999.dp),
                                            color = AltAccent
                                        ) {
                                            Text(
                                                stringResource(
                                                    R.string.history_compare_selected,
                                                    compareSelection.indexOf(timelineKey) + 1
                                                ),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                color = androidx.compose.ui.graphics.Color(0xFF16111F),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(14.dp),
                                        shape = RoundedCornerShape(999.dp),
                                        color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.42f)
                                    ) {
                                        Text(
                                            if (hasAiPreview) stringResource(R.string.history_ai_life) else stringResource(R.string.history_alt_life),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            color = AltAccent,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Column(Modifier.padding(18.dp)) {
                                    Text(
                                        scenario.title,
                                        fontSize = 21.sp,
                                        lineHeight = 26.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        scenario.subtitle,
                                        color = AltMuted,
                                        fontSize = 13.sp,
                                        lineHeight = 19.sp
                                    )
                                    Spacer(Modifier.height(14.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            DateFormat.getDateTimeInstance(
                                                DateFormat.MEDIUM,
                                                DateFormat.SHORT
                                            ).format(Date(entry.createdAt)),
                                            color = AltDimmed,
                                            fontSize = 12.sp
                                        )
                                        Spacer(Modifier.weight(1f))
                                        Text(
                                            when {
                                                isDeleting -> stringResource(R.string.history_deleting)
                                                isCompareMode && timelineKey in compareSelection ->
                                                    stringResource(R.string.history_compare_selected_short)
                                                isCompareMode -> stringResource(R.string.history_compare_select)
                                                else -> stringResource(R.string.history_open)
                                            },
                                            color = AltAccent,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    if (mediaStatus != null) {
                                        Spacer(Modifier.height(10.dp))
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
                                    Spacer(Modifier.height(2.dp))
                                    TextButton(
                                        onClick = { pendingDeleteEntry = entry },
                                        enabled = !isCompareMode && !isDeleting && !isClearingHistory,
                                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                                    ) {
                                        Text(
                                            if (isDeleting) stringResource(R.string.history_deleting) else stringResource(R.string.history_delete_life),
                                            color = AltDimmed,
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
                Text(if (isClearingHistory) stringResource(R.string.history_clearing) else stringResource(R.string.history_clear_local))
            }
        }
    }

    if (showCompareDialog) {
        val comparedEntries = compareSelection.mapNotNull { key ->
            entries.firstOrNull { it.timelineKey == key }
        }
        if (comparedEntries.size == 2) {
            AlertDialog(
                onDismissRequest = {
                    showCompareDialog = false
                    compareSelection = emptyList()
                    isCompareMode = false
                },
                title = { Text(stringResource(R.string.history_compare_title)) },
                text = {
                    Column {
                        comparedEntries.forEachIndexed { index, entry ->
                            val scenario = scenarios.firstOrNull { it.id == entry.scenarioId }
                            if (scenario != null) {
                                val previewUri =
                                    generatedPreviewUrisByTimelineKey[entry.timelineKey]
                                        ?: entry.photoFileName
                                            ?.let { photoUrisByFileName[it] }
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(22.dp),
                                    color = AltCard
                                ) {
                                    Column {
                                        if (previewUri != null) {
                                            AsyncImage(
                                                model = previewUri,
                                                contentDescription = "Comparison preview for ${scenario.title}",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(150.dp),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        Column(Modifier.padding(16.dp)) {
                                            Text(
                                                stringResource(
                                                    R.string.history_compare_life_number,
                                                    index + 1
                                                ),
                                                color = AltAccent,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(Modifier.height(6.dp))
                                            Text(
                                                scenario.title,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                scenario.subtitle,
                                                color = AltMuted,
                                                fontSize = 12.sp,
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }
                                }
                                if (index == 0) {
                                    Spacer(Modifier.height(10.dp))
                                    Text(
                                        stringResource(R.string.history_compare_vs),
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        color = AltAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(10.dp))
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    val firstEntry = comparedEntries[0]
                    val secondEntry = comparedEntries[1]
                    val firstScenario = scenarios.firstOrNull { it.id == firstEntry.scenarioId }
                    val secondScenario = scenarios.firstOrNull { it.id == secondEntry.scenarioId }
                    TextButton(
                        enabled = !isSharingComparison &&
                            firstScenario != null &&
                            secondScenario != null,
                        onClick = {
                            if (
                                !isSharingComparison &&
                                firstScenario != null &&
                                secondScenario != null
                            ) {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                isSharingComparison = true
                                scope.launch {
                                    val firstVisual =
                                        generatedPreviewUrisByTimelineKey[firstEntry.timelineKey]
                                            ?: firstEntry.photoFileName
                                                ?.let { photoUrisByFileName[it] }
                                    val secondVisual =
                                        generatedPreviewUrisByTimelineKey[secondEntry.timelineKey]
                                            ?: secondEntry.photoFileName
                                                ?.let { photoUrisByFileName[it] }
                                    val rendered = runCatching {
                                        withContext(Dispatchers.Default) {
                                            ShareCardRenderer.renderComparison(
                                                context = context,
                                                firstVisualUri = firstVisual,
                                                firstScenario = firstScenario,
                                                secondVisualUri = secondVisual,
                                                secondScenario = secondScenario
                                            )
                                        }
                                    }
                                    rendered.onSuccess { uri ->
                                        runCatching {
                                            ShareCardRenderer.shareComparison(context, uri)
                                        }.onFailure {
                                            Toast.makeText(
                                                context,
                                                context.getString(
                                                    R.string.history_compare_share_failed
                                                ),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }.onFailure {
                                        Toast.makeText(
                                            context,
                                            context.getString(
                                                R.string.history_compare_share_failed
                                            ),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    isSharingComparison = false
                                }
                            }
                        }
                    ) {
                        Text(
                            if (isSharingComparison) {
                                stringResource(R.string.history_compare_preparing_share)
                            } else {
                                stringResource(R.string.history_compare_share)
                            }
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        enabled = !isSharingComparison,
                        onClick = {
                            showCompareDialog = false
                            compareSelection = emptyList()
                            isCompareMode = false
                        }
                    ) {
                        Text(stringResource(R.string.history_compare_done))
                    }
                }
            )
        }
    }

    pendingDeleteEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { pendingDeleteEntry = null },
            title = { Text(stringResource(R.string.history_delete_title)) },
            text = {
                Text(stringResource(R.string.history_delete_body))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteEntry = null
                        onDelete(entry)
                    }
                ) {
                    Text(stringResource(R.string.history_delete_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteEntry = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text(stringResource(R.string.history_clear_title)) },
            text = {
                Text(stringResource(R.string.history_clear_body))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirmation = false
                        onClear()
                    }
                ) {
                    Text(stringResource(R.string.history_clear_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

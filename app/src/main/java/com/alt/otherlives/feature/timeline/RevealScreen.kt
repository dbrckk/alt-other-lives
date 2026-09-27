package com.alt.otherlives.feature.timeline

import android.net.Uri
import android.os.Build
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.media3.common.util.UnstableApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.media3.transformer.Transformer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.alt.otherlives.core.designsystem.AltAccent
import com.alt.otherlives.core.designsystem.AltBackButton
import com.alt.otherlives.core.designsystem.AltBackground
import com.alt.otherlives.core.designsystem.AltCard
import com.alt.otherlives.core.designsystem.AltDimmed
import com.alt.otherlives.core.designsystem.AltMuted
import com.alt.otherlives.core.designsystem.AltPrimary
import com.alt.otherlives.core.model.Scenario
import com.alt.otherlives.core.media.ShareCardRenderer
import com.alt.otherlives.core.media.CinematicVideoExporter
import com.alt.otherlives.core.media.TimelineSceneRenderer
import com.alt.otherlives.core.media.RenderedTimelineScenes
import com.alt.otherlives.core.generation.ComfyUiConfig
import com.alt.otherlives.core.generation.ComfyUiGenerationProvider
import com.alt.otherlives.core.generation.GenerationSettings
import com.alt.otherlives.core.generation.GeneratedScene
import com.alt.otherlives.core.generation.GeneratedSceneStore
import com.alt.otherlives.core.generation.AiGenerationReport
import com.alt.otherlives.core.generation.AiGenerationReportReason
import com.alt.otherlives.core.generation.AiGenerationReportStore

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun RevealScreen(
    photoUri: Uri?,
    scenario: Scenario,
    generationSettings: GenerationSettings,
    timelineKey: String,
    onBack: () -> Unit,
    onAiSettings: () -> Unit,
    onCreateAnotherLife: () -> Unit
) {
    val context = LocalContext.current
    val sceneStore = remember(context) { GeneratedSceneStore(context.applicationContext) }
    val aiOrchestrator = remember(context, sceneStore) {
        AiGenerationOrchestrator(context.applicationContext, sceneStore)
    }
    val reportStore = remember(context) { AiGenerationReportStore(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var isExporting by remember(timelineKey) { mutableStateOf(false) }
    var exportProgress by remember(timelineKey) { mutableStateOf<Int?>(null) }
    var activeTransformer by remember(timelineKey) { mutableStateOf<Transformer?>(null) }
    var activeRenderedScenes by remember(timelineKey) { mutableStateOf<RenderedTimelineScenes?>(null) }
    var exportJob by remember(timelineKey) { mutableStateOf<Job?>(null) }
    var completedVideoUri by remember(timelineKey) { mutableStateOf<Uri?>(null) }
    var isRenderingShareImage by remember(timelineKey) { mutableStateOf(false) }
    var isSavingVideoToGallery by remember(timelineKey) { mutableStateOf(false) }
    var generatedScenes by remember(timelineKey) { mutableStateOf<List<GeneratedScene>>(emptyList()) }
    var isLoadingStoredScenes by remember(timelineKey) { mutableStateOf(true) }
    var aiUiState by remember(timelineKey) {
        mutableStateOf(RevealAiUiState())
    }
    var aiGenerationJob by remember(timelineKey) { mutableStateOf<Job?>(null) }
    var showRegenerateAllDialog by remember(timelineKey) { mutableStateOf(false) }
    var showClearAiDialog by remember(timelineKey) { mutableStateOf(false) }
    var isClearingAi by remember(timelineKey) { mutableStateOf(false) }
    var showAiReportDialog by remember(timelineKey) { mutableStateOf(false) }
    var selectedAiReportReason by remember(timelineKey) {
        mutableStateOf<AiGenerationReportReason?>(null)
    }
    var isSavingAiReport by remember(timelineKey) { mutableStateOf(false) }
    val primaryGeneratedSceneUri = generatedScenes
        .minByOrNull { it.chapterIndex }
        ?.imageUri
    val revealHeroUri = primaryGeneratedSceneUri ?: photoUri
    val hasVisualAsset = revealHeroUri != null

    LaunchedEffect(timelineKey) {
        isLoadingStoredScenes = true
        val restored = runCatching {
            withContext(Dispatchers.IO) {
                sceneStore.load(timelineKey)
            }
        }
        restored.onSuccess {
            generatedScenes = it
        }.onFailure {
            generatedScenes = emptyList()
            Toast.makeText(
                context,
                "Could not restore saved AI scenes: " + (it.message ?: "unknown error"),
                Toast.LENGTH_SHORT
            ).show()
        }
        isLoadingStoredScenes = false
    }

    LaunchedEffect(photoUri, generatedScenes) {
        completedVideoUri = null
    }

    LaunchedEffect(activeTransformer, isExporting) {
        while (isExporting) {
            activeTransformer?.let { exportProgress = CinematicVideoExporter.progress(it) }
            delay(200)
        }
    }

    DisposableEffect(timelineKey) {
        onDispose {
            aiGenerationJob?.cancel()
            exportJob?.cancel()
            activeTransformer?.let { CinematicVideoExporter.cancel(it) }
            activeRenderedScenes?.deleteCacheFiles()
        }
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 40.dp)) {
        item {
            Box(modifier = Modifier.fillMaxWidth().height(620.dp)) {
                AsyncImage(
                    model = revealHeroUri,
                    contentDescription = "Hero image for ${scenario.title}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.18f),
                                Color.Transparent,
                                AltBackground.copy(alpha = 0.38f),
                                AltBackground
                            ),
                            startY = 0f
                        )
                    )
                )
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.padding(start = 16.dp, top = 36.dp)
                ) {
                    Text("‹ Back")
                }
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (primaryGeneratedSceneUri != null) "ALT ORIGINAL • AI GENERATED" else "ALT ORIGINAL",
                            color = AltAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        scenario.title,
                        fontSize = 42.sp,
                        lineHeight = 44.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        scenario.subtitle,
                        color = AltMuted,
                        fontSize = 16.sp,
                        lineHeight = 22.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "One choice. Another life.",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        if (isLoadingStoredScenes) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 14.dp)
                ) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Restoring saved ALT scenes…",
                        color = AltDimmed,
                        fontSize = 12.sp
                    )
                }
            }
        }
        itemsIndexed(scenario.chapters) { index, chapter ->
            val generated = generatedScenes.firstOrNull { it.chapterIndex == index }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp)
                    .animateContentSize(animationSpec = tween(durationMillis = 420)),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = AltCard)
            ) {
                Column {
                    if (generated != null) {
                        AsyncImage(
                            model = generated.imageUri,
                            contentDescription = "Generated scene for ${chapter.label}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(340.dp)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 30.dp,
                                        topEnd = 30.dp
                                    )
                                ),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Column(Modifier.padding(22.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                (index + 1).toString().padStart(2, '0'),
                                color = AltAccent,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(
                                    "CHAPTER ${index + 1}",
                                    color = AltDimmed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    chapter.label,
                                    color = AltAccent,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            chapter.narrative,
                            fontSize = 22.sp,
                            lineHeight = 30.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (generated == null) {
                            Spacer(Modifier.height(14.dp))
                            Text(
                                "Generate this chapter to unlock its cinematic scene.",
                                color = AltDimmed,
                                fontSize = 12.sp
                            )
                        }
                        if (index in aiUiState.chapterFailures) {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                if (generated != null) {
                                    "New AI variation failed • existing scene kept"
                                } else {
                                    "AI scene failed • retry available"
                                },
                                color = AltDimmed,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
        if (generatedScenes.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp)
                ) {
                    TextButton(
                        onClick = {
                            selectedAiReportReason = null
                            showAiReportDialog = true
                        },
                        enabled = !isSavingAiReport &&
                            !aiUiState.isGenerating &&
                            !isExporting &&
                            !isRenderingShareImage,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Flag unsafe AI generation")
                    }
                    Text(
                        "Flags are stored locally for now and do not include your photo or generated images.",
                        color = AltDimmed,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (showAiReportDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            if (!isSavingAiReport) {
                                showAiReportDialog = false
                            }
                        },
                        title = { Text("Flag this AI generation") },
                        text = {
                            Column {
                                Text(
                                    "Choose the reason. ALT stores only the timeline ID, scenario, reason and time."
                                )
                                Spacer(Modifier.height(10.dp))
                                AiGenerationReportReason.entries.forEach { reason ->
                                    TextButton(
                                        onClick = { selectedAiReportReason = reason },
                                        enabled = !isSavingAiReport,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            if (selectedAiReportReason == reason) {
                                                "✓ " + reason.label
                                            } else {
                                                reason.label
                                            }
                                        )
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(
                                enabled = selectedAiReportReason != null && !isSavingAiReport,
                                onClick = {
                                    val reason = selectedAiReportReason ?: return@TextButton
                                    isSavingAiReport = true
                                    scope.launch {
                                        val saved = runCatching {
                                            withContext(Dispatchers.IO) {
                                                reportStore.record(
                                                    AiGenerationReport(
                                                        timelineKey = timelineKey,
                                                        scenarioId = scenario.id,
                                                        reason = reason,
                                                        createdAt = System.currentTimeMillis()
                                                    )
                                                )
                                            }
                                        }
                                        isSavingAiReport = false
                                        saved.onSuccess {
                                            showAiReportDialog = false
                                            selectedAiReportReason = null
                                            Toast.makeText(
                                                context,
                                                "Flag saved locally",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }.onFailure {
                                            Toast.makeText(
                                                context,
                                                "Could not save flag: " +
                                                    (it.message ?: "unknown error"),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            ) {
                                Text(if (isSavingAiReport) "Saving…" else "Save flag")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showAiReportDialog = false },
                                enabled = !isSavingAiReport
                            ) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
        item {
            Column(Modifier.padding(24.dp)) {
                if (generationSettings.isReadyForRemoteGeneration && photoUri != null) {
                    val startGeneration: (Set<Int>, Boolean) -> Unit = { targetIndexes, resetSeed ->
                        if (!aiUiState.isGenerating) {
                            completedVideoUri = null
                            aiUiState = aiUiState.start(targetIndexes.size)
                            aiGenerationJob = scope.launch {
                                try {
                                    val provider = ComfyUiGenerationProvider(
                                        context = context,
                                        config = ComfyUiConfig(generationSettings.comfyUiBaseUrl),
                                        workflowTemplateJson = generationSettings.workflowJson
                                    )
                                    val outcome = aiOrchestrator.generate(
                                        provider = provider,
                                        sourcePhoto = photoUri,
                                        scenario = scenario,
                                        timelineKey = timelineKey,
                                        targetIndexes = targetIndexes,
                                        resetSeed = resetSeed,
                                        onProgress = { completed, total ->
                                            aiUiState = aiUiState.progress(completed, total)
                                        },
                                        onScenesChanged = { scenes ->
                                            generatedScenes = scenes
                                        },
                                        onChapterFailure = { chapterIndex, message ->
                                            aiUiState = aiUiState.failure(chapterIndex, message)
                                        }
                                    )
                                    aiUiState = aiUiState.retainFailures(
                                        outcome.failedChapterIndexes
                                    )

                                    val expected = scenario.chapters.take(5).size
                                    val message = aiGenerationCompletionMessage(
                                        resetSeed = resetSeed,
                                        completeFreshVariation =
                                            outcome.freshVariationCommitted,
                                        readySceneCount = outcome.scenes.size,
                                        expectedSceneCount = expected,
                                        failedChapterIndexes =
                                            outcome.failedChapterIndexes
                                    )
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                } catch (cancelled: CancellationException) {
                                    throw cancelled
                                } catch (error: Throwable) {
                                    Toast.makeText(
                                        context,
                                        "AI generation failed: " +
                                            (error.message ?: "unknown error"),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } finally {
                                    aiUiState = aiUiState.finish()
                                    aiGenerationJob = null
                                }
                            }
                        }
                    }

                    val generationPlan = planAiGeneration(
                        chapterCount = scenario.chapters.take(5).size,
                        generatedChapterIndexes =
                            generatedScenes.map { it.chapterIndex }.toSet(),
                        failedChapterIndexes = aiUiState.chapterFailures.keys
                    )

                    Button(
                        onClick = {
                            if (!aiUiState.isGenerating) {
                                if (generationPlan.requiresFullRegenerationConfirmation) {
                                    showRegenerateAllDialog = true
                                } else {
                                    startGeneration(
                                        if (generationPlan.missingIndexes.isNotEmpty()) {
                                            generationPlan.missingIndexes
                                        } else {
                                            generationPlan.expectedIndexes
                                        },
                                        false
                                    )
                                }
                            }
                        },
                        enabled = !isLoadingStoredScenes && !isClearingAi && !isSavingVideoToGallery && !aiUiState.isGenerating && !isExporting && !isRenderingShareImage,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            aiGenerationButtonLabel(
                                isGenerating = aiUiState.isGenerating,
                                completed = aiUiState.completed,
                                total = aiUiState.total,
                                generatedSceneCount = generatedScenes.size,
                                expectedSceneCount = scenario.chapters.take(5).size
                            ),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (generationPlan.retryableFailedIndexes.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                startGeneration(
                                    generationPlan.retryableFailedIndexes,
                                    false
                                )
                            },
                            enabled = !isLoadingStoredScenes &&
                                !isClearingAi &&
                                !isSavingVideoToGallery &&
                                !aiUiState.isGenerating &&
                                !isExporting &&
                                !isRenderingShareImage,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Retry failed chapters " +
                                    generationPlan.retryableFailedIndexes
                                        .sorted()
                                        .joinToString(", ") { (it + 1).toString() }
                            )
                        }
                    }

                    if (generatedScenes.isNotEmpty()) {
                        TextButton(
                            onClick = { showClearAiDialog = true },
                            enabled = !isLoadingStoredScenes && !isClearingAi && !isSavingVideoToGallery && !aiUiState.isGenerating && !isExporting && !isRenderingShareImage,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Remove generated AI scenes") }
                    }

                    if (showClearAiDialog) {
                        AlertDialog(
                            onDismissRequest = { showClearAiDialog = false },
                            title = { Text("Remove AI scenes?") },
                            text = { Text("The generated chapter images for this timeline will be deleted from this device.") },
                            confirmButton = {
                                TextButton(
                                    enabled = !isClearingAi,
                                    onClick = {
                                        if (!isClearingAi) {
                                            showClearAiDialog = false
                                            isClearingAi = true
                                            scope.launch {
                                                runCatching {
                                                    withContext(Dispatchers.IO) {
                                                        sceneStore.clear(timelineKey)
                                                    }
                                                }.onSuccess {
                                                    generatedScenes = emptyList()
                                                    completedVideoUri = null
                                                    Toast.makeText(
                                                        context,
                                                        "AI scenes removed",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }.onFailure {
                                                    Toast.makeText(
                                                        context,
                                                        "Could not remove AI scenes: " +
                                                            (it.message ?: "unknown error"),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                isClearingAi = false
                                            }
                                        }
                                    }
                                ) { Text(if (isClearingAi) "Removing…" else "Remove") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showClearAiDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

                    if (showRegenerateAllDialog) {
                        AlertDialog(
                            onDismissRequest = { showRegenerateAllDialog = false },
                            title = { Text("Regenerate all AI scenes?") },
                            text = { Text("This creates a new visual variation for the full timeline. It replaces the current version only if every chapter succeeds.") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showRegenerateAllDialog = false
                                        startGeneration(
                                            scenario.chapters.take(5).indices.toSet(),
                                            true
                                        )
                                    }
                                ) { Text("Regenerate all") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showRegenerateAllDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                    if (aiUiState.isGenerating) {
                        Spacer(Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                if (!aiUiState.isCancelling) {
                                    aiUiState = aiUiState.requestCancellation()
                                    aiGenerationJob?.cancel()
                                }
                            },
                            enabled = !aiUiState.isCancelling,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (aiUiState.isCancelling) {
                                    "Cancelling AI generation…"
                                } else {
                                    "Cancel AI generation"
                                }
                            )
                        }
                        LinearProgressIndicator(
                            progress = {
                                if (aiUiState.total == 0) 0f else aiUiState.completed.toFloat() / aiUiState.total.toFloat()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                } else {
                    val aiUnavailableMessage = aiGenerationUnavailableMessage(
                        hasSourcePhoto = photoUri != null,
                        settings = generationSettings
                    )
                    Text(
                        aiUnavailableMessage,
                        color = AltDimmed,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    TextButton(
                        onClick = onAiSettings,
                        enabled = !aiUiState.isGenerating && !isExporting && !isRenderingShareImage,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Set up AI generation")
                    }
                    Spacer(Modifier.height(12.dp))
                }

                Button(
                    onClick = {
                        if (!isRenderingShareImage) {
                            isRenderingShareImage = true
                            scope.launch {
                                val rendered = runCatching {
                                    withContext(Dispatchers.Default) {
                                        ShareCardRenderer.render(
                                            context = context,
                                            photoUri = primaryGeneratedSceneUri ?: photoUri,
                                            scenario = scenario,
                                            fallbackImageUri = photoUri
                                        )
                                    }
                                }
                                isRenderingShareImage = false
                                rendered.onSuccess { shareUri ->
                                    runCatching {
                                        ShareCardRenderer.share(context, shareUri, scenario)
                                    }.onFailure {
                                        Toast.makeText(
                                            context,
                                            "Could not share image: " +
                                                (it.message ?: "unknown error"),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }.onFailure {
                                    Toast.makeText(
                                        context,
                                        "Could not create share image: " + (it.message ?: "unknown error"),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    },
                    enabled = !isLoadingStoredScenes && !isClearingAi && !isSavingVideoToGallery && !isRenderingShareImage && !aiUiState.isGenerating && !isExporting && hasVisualAsset,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AltPrimary, contentColor = Color(0xFF16111F))
                ) {
                    Text(
                        if (isRenderingShareImage) "Preparing share image…" else "Share your ALT life",
                        fontWeight = FontWeight.Bold
                    )
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (!isRenderingShareImage) {
                                isRenderingShareImage = true
                                scope.launch {
                                    val saved = runCatching {
                                        withContext(Dispatchers.IO) {
                                            ShareCardRenderer.saveToGallery(
                                                context = context,
                                                photoUri = primaryGeneratedSceneUri ?: photoUri,
                                                scenario = scenario,
                                                fallbackImageUri = photoUri
                                            )
                                        }
                                    }
                                    isRenderingShareImage = false
                                    saved.onSuccess {
                                        Toast.makeText(context, "Saved to Pictures/ALT", Toast.LENGTH_SHORT).show()
                                    }.onFailure {
                                        Toast.makeText(
                                            context,
                                            "Could not save image: " + (it.message ?: "unknown error"),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        },
                        enabled = !isLoadingStoredScenes && !isClearingAi && !isSavingVideoToGallery && !isRenderingShareImage && !aiUiState.isGenerating && !isExporting && hasVisualAsset,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) { Text("Save 9:16 image", fontWeight = FontWeight.Bold) }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (!isExporting && activeTransformer == null) {
                            isExporting = true
                            exportProgress = null
                            completedVideoUri = null
                            exportJob = scope.launch {
                                val prepared = runCatching {
                                    withContext(Dispatchers.Default) {
                                        TimelineSceneRenderer.render(
                                            context = context,
                                            photoUri = photoUri,
                                            scenario = scenario,
                                            chapterImages = generatedScenes.associate { it.chapterIndex to it.imageUri }
                                        )
                                    }
                                }

                                prepared.onSuccess { renderedScenes ->
                                    activeRenderedScenes = renderedScenes
                                    runCatching {
                                        CinematicVideoExporter.export(
                                            context = context,
                                            imageUris = renderedScenes.imageUris,
                                            scenario = scenario,
                                            onCompleted = { videoUri ->
                                                renderedScenes.deleteCacheFiles()
                                                activeRenderedScenes = null
                                                isExporting = false
                                                exportProgress = 100
                                                activeTransformer = null
                                                exportJob = null
                                                completedVideoUri = videoUri
                                                Toast.makeText(
                                                    context,
                                                    "Video ready",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            onError = {
                                                renderedScenes.deleteCacheFiles()
                                                activeRenderedScenes = null
                                                isExporting = false
                                                exportProgress = null
                                                activeTransformer = null
                                                exportJob = null
                                                completedVideoUri = null
                                                Toast.makeText(
                                                    context,
                                                    "Video export failed: " +
                                                        (it.message ?: "unknown error"),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        )
                                    }.onSuccess { transformer ->
                                        activeTransformer = transformer
                                    }.onFailure {
                                        renderedScenes.deleteCacheFiles()
                                        activeRenderedScenes = null
                                        isExporting = false
                                        exportProgress = null
                                        activeTransformer = null
                                        exportJob = null
                                        completedVideoUri = null
                                        if (it !is CancellationException) {
                                            Toast.makeText(
                                                context,
                                                "Could not start video export: " +
                                                    (it.message ?: "unknown error"),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }.onFailure {
                                    isExporting = false
                                    exportProgress = null
                                    activeTransformer = null
                                    activeRenderedScenes = null
                                    exportJob = null
                                    completedVideoUri = null
                                    if (it !is CancellationException) {
                                        Toast.makeText(
                                            context,
                                            "Could not prepare video: " +
                                                (it.message ?: "unknown error"),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isLoadingStoredScenes && !isClearingAi && !isSavingVideoToGallery && !isExporting && !aiUiState.isGenerating && !isRenderingShareImage && hasVisualAsset,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        if (isExporting) "Creating video" + (exportProgress?.let { " • $it%" } ?: "…")
                        else "Create cinematic MP4",
                        fontWeight = FontWeight.Bold
                    )
                }
                if (isExporting) {
                    Spacer(Modifier.height(10.dp))
                    if (exportProgress != null) {
                        LinearProgressIndicator(
                            progress = { exportProgress!!.coerceIn(0, 100) / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(Modifier.height(8.dp))
                    androidx.compose.material3.TextButton(
                        onClick = {
                            exportJob?.cancel()
                            exportJob = null
                            activeTransformer?.let { CinematicVideoExporter.cancel(it) }
                            activeTransformer = null
                            activeRenderedScenes?.deleteCacheFiles()
                            activeRenderedScenes = null
                            isExporting = false
                            exportProgress = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Cancel export") }
                }

                completedVideoUri?.let { videoUri ->
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            runCatching {
                                CinematicVideoExporter.share(context, videoUri, scenario)
                            }.onFailure {
                                completedVideoUri = null
                                Toast.makeText(
                                    context,
                                    "Could not share video: " +
                                        (it.message ?: "unknown error"),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        enabled = !isLoadingStoredScenes && !isClearingAi && !isSavingVideoToGallery && !aiUiState.isGenerating && !isExporting,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) { Text("Share MP4", fontWeight = FontWeight.Bold) }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        Spacer(Modifier.height(8.dp))
                        androidx.compose.material3.TextButton(
                            enabled = !isLoadingStoredScenes &&
                                !isClearingAi &&
                                !isSavingVideoToGallery &&
                                !aiUiState.isGenerating &&
                                !isExporting,
                            onClick = {
                                if (!isSavingVideoToGallery) {
                                    isSavingVideoToGallery = true
                                    scope.launch {
                                        runCatching {
                                            withContext(Dispatchers.IO) {
                                                CinematicVideoExporter.saveToGallery(
                                                    context,
                                                    videoUri,
                                                    scenario
                                                )
                                            }
                                        }.onSuccess {
                                            Toast.makeText(
                                                context,
                                                "Saved to Movies/ALT",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }.onFailure {
                                            Toast.makeText(
                                                context,
                                                "Could not save video: " +
                                                    (it.message ?: "unknown error"),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        isSavingVideoToGallery = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (isSavingVideoToGallery) "Saving MP4…" else "Save MP4 to gallery"
                            )
                        }
                    }
                }
                if (!hasVisualAsset) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No visual source is available for this timeline. Share and video export are disabled.",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = AltDimmed,
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.height(18.dp))
                TextButton(
                    onClick = onCreateAnotherLife,
                    enabled = !isLoadingStoredScenes &&
                        !isClearingAi &&
                        !isSavingVideoToGallery &&
                        !isRenderingShareImage &&
                        !aiUiState.isGenerating &&
                        !isExporting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Create another ALT life  →",
                        color = AltAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Made for Reels • Shorts • Stories • 9:16 cinematic export",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = AltDimmed,
                    fontSize = 12.sp
                )
            }
        }
    }
}

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
import androidx.compose.foundation.layout.safeDrawingPadding
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
import androidx.compose.material3.Surface
import androidx.media3.transformer.Transformer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
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
import com.alt.otherlives.core.generation.GenerationChapterFailureKind
import com.alt.otherlives.core.generation.AiGenerationReport
import com.alt.otherlives.core.generation.AiGenerationReportReason
import com.alt.otherlives.core.generation.AiGenerationReportStore
import com.alt.otherlives.core.generation.messageRes
import com.alt.otherlives.R

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun RevealScreen(
    photoUri: Uri?,
    scenario: Scenario,
    generationSettings: GenerationSettings,
    timelineKey: String,
    onBack: () -> Unit,
    onAiSettings: () -> Unit,
    onCreateAnotherLife: () -> Unit,
    onRemixThisLife: () -> Unit,
    isRemixingLife: Boolean = false
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val revealLayout = RevealResponsiveLayout.resolve(
        screenHeightDp = configuration.screenHeightDp,
        fontScale = density.fontScale
    )
    val haptics = LocalHapticFeedback.current
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
    val storyChapterCount = scenario.chapters.take(5).size
    val readyAiSceneCount = generatedScenes
        .map { it.chapterIndex }
        .distinct()
        .count { it in 0 until storyChapterCount }

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
                context.getString(
                    R.string.reveal_restore_failed,
                    it.message ?: context.getString(R.string.common_unknown_error)
                ),
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
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 40.dp)
    ) {
        item {
            Box(modifier = Modifier.fillMaxWidth().height(revealLayout.heroHeightDp.dp)) {
                if (revealHeroUri != null) {
                    AsyncImage(
                        model = revealHeroUri,
                        contentDescription = stringResource(
                            R.string.reveal_hero_content_description,
                            scenario.title
                        ),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        AltCard,
                                        AltBackground
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            scenario.title
                                .firstOrNull()
                                ?.uppercase()
                                ?: "ALT",
                            color = AltAccent.copy(alpha = 0.32f),
                            fontSize = 128.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
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
                    Text("‹ " + stringResource(R.string.common_back))
                }
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (primaryGeneratedSceneUri != null) stringResource(R.string.reveal_alt_original_ai) else stringResource(R.string.reveal_alt_original),
                            color = AltAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        scenario.title,
                        fontSize = revealLayout.titleSizeSp.sp,
                        lineHeight = revealLayout.titleLineHeightSp.sp,
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
                        stringResource(R.string.reveal_one_choice),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color.Black.copy(alpha = 0.36f)
                        ) {
                            Text(
                                stringResource(
                                    R.string.reveal_story_chapter_count,
                                    storyChapterCount
                                ),
                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 6.dp
                                ),
                                color = AltMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color.Black.copy(alpha = 0.36f)
                        ) {
                            Text(
                                stringResource(
                                    R.string.reveal_story_ai_progress,
                                    readyAiSceneCount,
                                    storyChapterCount
                                ),
                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 6.dp
                                ),
                                color = if (readyAiSceneCount == storyChapterCount) {
                                    AltAccent
                                } else {
                                    AltMuted
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
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
                        stringResource(R.string.reveal_restoring_scenes),
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(340.dp)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 30.dp,
                                        topEnd = 30.dp
                                    )
                                )
                        ) {
                            AsyncImage(
                                model = generated.imageUri,
                                contentDescription = stringResource(
                                    R.string.reveal_generated_scene_content_description,
                                    chapter.label
                                ),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(14.dp),
                                shape = RoundedCornerShape(999.dp),
                                color = Color.Black.copy(alpha = 0.48f)
                            ) {
                                Text(
                                    stringResource(R.string.reveal_ai_scene_badge),
                                    modifier = Modifier.padding(
                                        horizontal = 10.dp,
                                        vertical = 6.dp
                                    ),
                                    color = AltAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
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
                                    stringResource(R.string.reveal_chapter_number, index + 1),
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
                                stringResource(R.string.reveal_generate_chapter),
                                color = AltDimmed,
                                fontSize = 12.sp
                            )
                        }
                        aiUiState.chapterFailures[index]?.let { failure ->
                            Spacer(Modifier.height(10.dp))
                            Text(
                                when {
                                    generated != null ->
                                        stringResource(
                                            R.string.reveal_variation_failed_existing
                                        )
                                    failure.kind ==
                                        GenerationChapterFailureKind.QUALITY_REJECTED ->
                                        stringResource(
                                            R.string.reveal_scene_quality_rejected
                                        )
                                    else ->
                                        stringResource(
                                            R.string.reveal_scene_failed_retry
                                        )
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
                        Text(stringResource(R.string.reveal_flag_action))
                    }
                    Text(
                        stringResource(R.string.reveal_flag_note),
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
                        title = { Text(stringResource(R.string.reveal_flag_title)) },
                        text = {
                            Column {
                                Text(
                                    stringResource(R.string.reveal_flag_body)
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
                                                "✓ " + when (reason) {
                                                    AiGenerationReportReason.SEXUAL_CONTENT ->
                                                        stringResource(R.string.reveal_flag_reason_sexual)
                                                    AiGenerationReportReason.VIOLENCE ->
                                                        stringResource(R.string.reveal_flag_reason_violence)
                                                    AiGenerationReportReason.HATE_OR_HARASSMENT ->
                                                        stringResource(R.string.reveal_flag_reason_hate)
                                                    AiGenerationReportReason.OTHER_UNSAFE ->
                                                        stringResource(R.string.reveal_flag_reason_other)
                                                }
                                            } else {
                                                when (reason) {
                                                    AiGenerationReportReason.SEXUAL_CONTENT ->
                                                        stringResource(R.string.reveal_flag_reason_sexual)
                                                    AiGenerationReportReason.VIOLENCE ->
                                                        stringResource(R.string.reveal_flag_reason_violence)
                                                    AiGenerationReportReason.HATE_OR_HARASSMENT ->
                                                        stringResource(R.string.reveal_flag_reason_hate)
                                                    AiGenerationReportReason.OTHER_UNSAFE ->
                                                        stringResource(R.string.reveal_flag_reason_other)
                                                }
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
                                                context.getString(R.string.reveal_flag_saved),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }.onFailure {
                                            Toast.makeText(
                                                context,
                                                context.getString(
                                                    R.string.reveal_flag_save_failed,
                                                    it.message
                                                        ?: context.getString(
                                                            R.string.common_unknown_error
                                                        )
                                                ),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            ) {
                                Text(
                                    if (isSavingAiReport) {
                                        stringResource(R.string.reveal_flag_saving)
                                    } else {
                                        stringResource(R.string.reveal_flag_save)
                                    }
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showAiReportDialog = false },
                                enabled = !isSavingAiReport
                            ) {
                                Text(stringResource(R.string.common_cancel))
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
                            aiUiState = aiUiState.start(
                                total = targetIndexes.size,
                                targetIndexes = targetIndexes
                            )
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
                                        onChapterFailure = { chapterIndex, message, kind ->
                                            aiUiState = aiUiState.failure(
                                                chapterIndex,
                                                message,
                                                kind
                                            )
                                        }
                                    )
                                    val expected = scenario.chapters.take(5).size
                                    val completionState = aiGenerationCompletionState(
                                        resetSeed = resetSeed,
                                        completeFreshVariation =
                                            outcome.freshVariationCommitted,
                                        readySceneCount = outcome.scenes.size,
                                        expectedSceneCount = expected,
                                        failedChapterIndexes =
                                            outcome.failedChapterIndexes,
                                        failureKinds = outcome.failureKinds
                                    )
                                    val message = when (completionState) {
                                        is AiGenerationCompletionState.FreshVariationIncomplete -> {
                                            val failed = completionState.failedChapterNumbers
                                                .joinToString(", ")
                                            if (failed.isBlank()) {
                                                context.getString(
                                                    R.string.reveal_completion_fresh_incomplete
                                                )
                                            } else {
                                                context.getString(
                                                    R.string.reveal_completion_fresh_incomplete_failed,
                                                    failed
                                                )
                                            }
                                        }
                                        AiGenerationCompletionState.Ready ->
                                            context.getString(R.string.reveal_completion_ready)
                                        is AiGenerationCompletionState.Partial -> {
                                            val retry = completionState.retryChapterNumbers
                                                .joinToString(", ")
                                            val newVariation =
                                                completionState.newVariationChapterNumbers
                                                    .joinToString(", ")
                                            when {
                                                retry.isNotBlank() &&
                                                    newVariation.isNotBlank() ->
                                                    context.getString(
                                                        R.string.reveal_completion_partial_mixed,
                                                        completionState.readySceneCount,
                                                        completionState.expectedSceneCount,
                                                        retry,
                                                        newVariation
                                                    )
                                                newVariation.isNotBlank() ->
                                                    context.getString(
                                                        R.string.reveal_completion_partial_quality,
                                                        completionState.readySceneCount,
                                                        completionState.expectedSceneCount,
                                                        newVariation
                                                    )
                                                retry.isNotBlank() ->
                                                    context.getString(
                                                        R.string.reveal_completion_partial_retry,
                                                        completionState.readySceneCount,
                                                        completionState.expectedSceneCount,
                                                        retry
                                                    )
                                                else ->
                                                    context.getString(
                                                        R.string.reveal_completion_partial,
                                                        completionState.readySceneCount,
                                                        completionState.expectedSceneCount
                                                    )
                                            }
                                        }
                                    }
                                    if (
                                        outcome.failedChapterIndexes.isEmpty() &&
                                        outcome.scenes.size >= expected
                                    ) {
                                        haptics.performHapticFeedback(
                                            HapticFeedbackType.LongPress
                                        )
                                    }
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                } catch (cancelled: CancellationException) {
                                    throw cancelled
                                } catch (error: Throwable) {
                                    Toast.makeText(
                                        context,
                                        context.getString(
                                            R.string.reveal_ai_generation_failed,
                                            error.message
                                                ?: context.getString(
                                                    R.string.common_unknown_error
                                                )
                                        ),
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
                        failedChapterIndexes = aiUiState.chapterFailures.keys,
                        nonRetryableFailedIndexes = aiUiState.chapterFailures
                            .filterValues {
                                it.kind ==
                                    GenerationChapterFailureKind.QUALITY_REJECTED
                            }
                            .keys
                    )

                    Button(
                        onClick = {
                            if (!aiUiState.isGenerating) {
                                if (
                                    generationPlan.requiresFullRegenerationConfirmation ||
                                    generationPlan.requiresFreshVariation
                                ) {
                                    showRegenerateAllDialog = true
                                } else {
                                    startGeneration(
                                        if (
                                            generationPlan.sameSeedMissingIndexes.isNotEmpty()
                                        ) {
                                            generationPlan.sameSeedMissingIndexes
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
                            when (
                                val buttonState = aiGenerationButtonState(
                                    isGenerating = aiUiState.isGenerating,
                                    completed = aiUiState.completed,
                                    total = aiUiState.total,
                                    generatedSceneCount = generatedScenes.size,
                                    expectedSceneCount = scenario.chapters.take(5).size,
                                    requiresFreshVariation =
                                        generationPlan.requiresFreshVariation
                                )
                            ) {
                                is AiGenerationButtonState.Generating ->
                                    stringResource(
                                        R.string.reveal_button_generating,
                                        buttonState.completed,
                                        buttonState.total
                                    )
                                AiGenerationButtonState.GenerateAll ->
                                    stringResource(R.string.reveal_button_generate_all)
                                AiGenerationButtonState.GenerateMissing ->
                                    stringResource(R.string.reveal_button_generate_missing)
                                AiGenerationButtonState.FreshVariationRequired ->
                                    stringResource(R.string.reveal_button_new_variation)
                                AiGenerationButtonState.RegenerateAll ->
                                    stringResource(R.string.reveal_button_regenerate_all)
                            },
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
                                stringResource(
                                    R.string.reveal_retry_failed_chapters,
                                    generationPlan.retryableFailedIndexes
                                        .sorted()
                                        .joinToString(", ") { (it + 1).toString() }
                                )
                            )
                        }
                    }

                    if (generatedScenes.isNotEmpty()) {
                        TextButton(
                            onClick = { showClearAiDialog = true },
                            enabled = !isLoadingStoredScenes && !isClearingAi && !isSavingVideoToGallery && !aiUiState.isGenerating && !isExporting && !isRenderingShareImage,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(stringResource(R.string.reveal_remove_ai_scenes)) }
                    }

                    if (showClearAiDialog) {
                        AlertDialog(
                            onDismissRequest = { showClearAiDialog = false },
                            title = { Text(stringResource(R.string.reveal_remove_ai_title)) },
                            text = { Text(stringResource(R.string.reveal_remove_ai_body)) },
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
                                                        context.getString(R.string.reveal_ai_scenes_removed),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }.onFailure {
                                                    Toast.makeText(
                                                        context,
                                                        context.getString(
                                                            R.string.reveal_remove_ai_failed,
                                                            it.message
                                                                ?: context.getString(
                                                                    R.string.common_unknown_error
                                                                )
                                                        ),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                isClearingAi = false
                                            }
                                        }
                                    }
                                ) {
                                    Text(
                                        if (isClearingAi) {
                                            stringResource(R.string.reveal_removing)
                                        } else {
                                            stringResource(R.string.reveal_remove)
                                        }
                                    )
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showClearAiDialog = false }) {
                                    Text(stringResource(R.string.common_cancel))
                                }
                            }
                        )
                    }

                    if (showRegenerateAllDialog) {
                        AlertDialog(
                            onDismissRequest = { showRegenerateAllDialog = false },
                            title = { Text(stringResource(R.string.reveal_regenerate_title)) },
                            text = { Text(stringResource(R.string.reveal_regenerate_body)) },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showRegenerateAllDialog = false
                                        startGeneration(
                                            scenario.chapters.take(5).indices.toSet(),
                                            true
                                        )
                                    }
                                ) { Text(stringResource(R.string.reveal_regenerate_all)) }
                            },
                            dismissButton = {
                                TextButton(onClick = { showRegenerateAllDialog = false }) {
                                    Text(stringResource(R.string.common_cancel))
                                }
                            }
                        )
                    }
                    if (aiUiState.isGenerating) {
                        Spacer(Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = AltCard)
                        ) {
                            Column(Modifier.padding(18.dp)) {
                                Text(
                                    stringResource(R.string.reveal_generation_label),
                                    color = AltAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    stringResource(
                                        R.string.reveal_generation_progress,
                                        (aiUiState.completed + 1)
                                            .coerceAtMost(aiUiState.total.coerceAtLeast(1)),
                                        aiUiState.total.coerceAtLeast(1)
                                    ),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(10.dp))
                                LinearProgressIndicator(
                                    progress = {
                                        if (aiUiState.total == 0) {
                                            0f
                                        } else {
                                            aiUiState.completed.toFloat() /
                                                aiUiState.total.toFloat()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    stringResource(R.string.reveal_generation_identity_hint),
                                    color = AltMuted,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                                Spacer(Modifier.height(6.dp))
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
                                            stringResource(R.string.reveal_generation_cancelling)
                                        } else {
                                            stringResource(R.string.reveal_generation_cancel)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                } else {
                    val aiUnavailableState = aiGenerationUnavailableState(
                        hasSourcePhoto = photoUri != null,
                        settings = generationSettings
                    )
                    val aiUnavailableMessage = when (aiUnavailableState) {
                        AiGenerationUnavailableState.MissingSourcePhoto ->
                            stringResource(R.string.reveal_unavailable_missing_source)
                        is AiGenerationUnavailableState.InvalidSettings ->
                            aiUnavailableState.validationIssue?.let {
                                stringResource(
                                    R.string.reveal_unavailable_invalid_settings,
                                    stringResource(it.messageRes())
                                )
                            } ?: stringResource(
                                R.string.reveal_unavailable_invalid_settings_generic
                            )
                        AiGenerationUnavailableState.MissingUploadConsent ->
                            stringResource(R.string.reveal_unavailable_missing_consent)
                        AiGenerationUnavailableState.NotConfigured ->
                            stringResource(R.string.reveal_unavailable_not_configured)
                    }
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
                        Text(stringResource(R.string.reveal_setup_ai))
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
                                            context.getString(
                                                R.string.reveal_share_image_failed,
                                                it.message
                                                    ?: context.getString(
                                                        R.string.common_unknown_error
                                                    )
                                            ),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }.onFailure {
                                    Toast.makeText(
                                        context,
                                        context.getString(
                                            R.string.reveal_create_share_failed,
                                            it.message
                                                ?: context.getString(
                                                    R.string.common_unknown_error
                                                )
                                        ),
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
                        if (isRenderingShareImage) stringResource(R.string.reveal_preparing_share) else stringResource(R.string.reveal_share_life),
                        fontWeight = FontWeight.Bold
                    )
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Spacer(Modifier.height(12.dp))
                    TextButton(
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
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.reveal_image_saved),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }.onFailure {
                                        Toast.makeText(
                                            context,
                                            context.getString(
                                                R.string.reveal_image_save_failed,
                                                it.message
                                                    ?: context.getString(
                                                        R.string.common_unknown_error
                                                    )
                                            ),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        },
                        enabled = !isLoadingStoredScenes && !isClearingAi && !isSavingVideoToGallery && !isRenderingShareImage && !aiUiState.isGenerating && !isExporting && hasVisualAsset,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            stringResource(R.string.reveal_save_image),
                            color = AltMuted,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
                                                    context.getString(R.string.reveal_video_ready),
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
                                                    context.getString(
                                                        R.string.reveal_video_export_failed,
                                                        it.message
                                                            ?: context.getString(
                                                                R.string.common_unknown_error
                                                            )
                                                    ),
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
                                                context.getString(
                                                    R.string.reveal_video_start_failed,
                                                    it.message
                                                        ?: context.getString(
                                                            R.string.common_unknown_error
                                                        )
                                                ),
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
                                            context.getString(
                                                R.string.reveal_video_prepare_failed,
                                                it.message
                                                    ?: context.getString(
                                                        R.string.common_unknown_error
                                                    )
                                            ),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isLoadingStoredScenes && !isClearingAi && !isSavingVideoToGallery && !isExporting && !aiUiState.isGenerating && !isRenderingShareImage && hasVisualAsset,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AltCard,
                        contentColor = AltPrimary
                    )
                ) {
                    Text(
                        if (isExporting) {
                            exportProgress?.let {
                                stringResource(
                                    R.string.reveal_video_creating_progress,
                                    it
                                )
                            } ?: stringResource(R.string.reveal_video_creating)
                        } else {
                            stringResource(R.string.reveal_create_video)
                        },
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
                    ) { Text(stringResource(R.string.reveal_cancel_export)) }
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
                                    context.getString(
                                        R.string.reveal_video_share_failed,
                                        it.message
                                            ?: context.getString(
                                                R.string.common_unknown_error
                                            )
                                    ),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        enabled = !isLoadingStoredScenes && !isClearingAi && !isSavingVideoToGallery && !aiUiState.isGenerating && !isExporting,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AltPrimary,
                            contentColor = Color(0xFF16111F)
                        )
                    ) {
                        Text(
                            stringResource(R.string.reveal_share_video),
                            fontWeight = FontWeight.Bold
                        )
                    }

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
                                                context.getString(R.string.reveal_video_saved),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }.onFailure {
                                            Toast.makeText(
                                                context,
                                                context.getString(
                                                    R.string.reveal_video_save_failed,
                                                    it.message
                                                        ?: context.getString(
                                                            R.string.common_unknown_error
                                                        )
                                                ),
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
                                if (isSavingVideoToGallery) {
                                    stringResource(R.string.reveal_video_saving)
                                } else {
                                    stringResource(R.string.reveal_video_save_gallery)
                                }
                            )
                        }
                    }
                }
                if (!hasVisualAsset) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.reveal_no_visual),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = AltDimmed,
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRemixThisLife()
                    },
                    enabled = !isRemixingLife &&
                        !isLoadingStoredScenes &&
                        !isClearingAi &&
                        !isSavingVideoToGallery &&
                        !isRenderingShareImage &&
                        !aiUiState.isGenerating &&
                        !isExporting,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        if (isRemixingLife) {
                            stringResource(R.string.reveal_remixing)
                        } else {
                            stringResource(R.string.reveal_remix)
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.reveal_remix_hint),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = AltDimmed,
                    fontSize = 11.sp
                )
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCreateAnotherLife()
                    },
                    enabled = !isRemixingLife &&
                        !isLoadingStoredScenes &&
                        !isClearingAi &&
                        !isSavingVideoToGallery &&
                        !isRenderingShareImage &&
                        !aiUiState.isGenerating &&
                        !isExporting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.reveal_create_another),
                        color = AltAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.reveal_social_footer),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = AltDimmed,
                    fontSize = 12.sp
                )
            }
        }
    }
}

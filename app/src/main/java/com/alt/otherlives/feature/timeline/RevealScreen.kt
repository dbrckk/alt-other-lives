package com.alt.otherlives.feature.timeline

import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import com.alt.otherlives.core.designsystem.AltBackground
import com.alt.otherlives.core.designsystem.AltDimmed
import com.alt.otherlives.core.designsystem.AltPrimary
import com.alt.otherlives.core.model.Scenario
import com.alt.otherlives.core.media.ShareCardRenderer
import com.alt.otherlives.core.media.CinematicVideoExporter
import com.alt.otherlives.core.media.TimelineSceneRenderer
import com.alt.otherlives.core.generation.ComfyUiConfig
import com.alt.otherlives.core.generation.ComfyUiGenerationProvider
import com.alt.otherlives.core.generation.GenerationRequest
import com.alt.otherlives.core.generation.GenerationSettings
import com.alt.otherlives.core.generation.GeneratedScene
import com.alt.otherlives.core.generation.GeneratedSceneStore

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun RevealScreen(
    photoUri: Uri?,
    scenario: Scenario,
    generationSettings: GenerationSettings,
    timelineKey: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sceneStore = remember(context) { GeneratedSceneStore(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }
    var exportProgress by remember { mutableStateOf<Int?>(null) }
    var activeTransformer by remember { mutableStateOf<Transformer?>(null) }
    var exportJob by remember { mutableStateOf<Job?>(null) }
    var completedVideoUri by remember(timelineKey) { mutableStateOf<Uri?>(null) }
    var isRenderingShareImage by remember { mutableStateOf(false) }
    var generatedScenes by remember(timelineKey) { mutableStateOf(sceneStore.load(timelineKey)) }
    var isGeneratingAi by remember { mutableStateOf(false) }
    var aiGenerationJob by remember { mutableStateOf<Job?>(null) }
    var aiCompleted by remember { mutableStateOf(0) }
    var aiTotal by remember { mutableStateOf(0) }
    var showRegenerateAllDialog by remember { mutableStateOf(false) }
    var showClearAiDialog by remember { mutableStateOf(false) }
    val primaryGeneratedSceneUri = generatedScenes
        .minByOrNull { it.chapterIndex }
        ?.imageUri
    val revealHeroUri = primaryGeneratedSceneUri ?: photoUri
    val hasVisualAsset = revealHeroUri != null

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
            activeTransformer?.cancel()
        }
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 40.dp)) {
        item {
            Box(modifier = Modifier.fillMaxWidth().height(500.dp)) {
                AsyncImage(
                    model = revealHeroUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color.Transparent, AltBackground), startY = 120f)
                    )
                )
                Text("‹", fontSize = 38.sp, modifier = Modifier.padding(start = 24.dp, top = 44.dp).clickable(onClick = onBack))
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)) {
                    Text("YOUR ALT LIFE", color = AltAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(scenario.title, fontSize = 34.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        itemsIndexed(scenario.chapters) { index, chapter ->
            Column(Modifier.padding(horizontal = 24.dp, vertical = 18.dp)) {
                generatedScenes.firstOrNull { it.chapterIndex == index }?.let { generated ->
                    AsyncImage(
                        model = generated.imageUri,
                        contentDescription = "Generated alternate life scene",
                        modifier = Modifier.fillMaxWidth().height(240.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(12.dp))
                }
                Text(chapter.label, color = AltAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(chapter.narrative, fontSize = 23.sp, lineHeight = 30.sp, fontWeight = FontWeight.Medium)
            }
        }
        item {
            Column(Modifier.padding(24.dp)) {
                if (generationSettings.isConfigured && photoUri != null) {
                    val startGeneration: (Set<Int>, Boolean) -> Unit = { targetIndexes, resetSeed ->
                        if (!isGeneratingAi) {
                            isGeneratingAi = true
                            aiCompleted = 0
                            aiTotal = targetIndexes.size
                            aiGenerationJob = scope.launch {
                                val previousSeed = sceneStore.getOrCreateSeed(timelineKey)
                                runCatching {
                                    val provider = ComfyUiGenerationProvider(
                                        context = context,
                                        config = ComfyUiConfig(generationSettings.comfyUiBaseUrl),
                                        workflowTemplateJson = generationSettings.workflowJson
                                    )
                                    provider.generate(
                                        request = GenerationRequest(
                                            sourcePhoto = photoUri,
                                            scenario = scenario,
                                            chapterIndexes = targetIndexes,
                                            seed = if (resetSeed) {
                                                sceneStore.resetSeed(timelineKey)
                                            } else {
                                                previousSeed
                                            }
                                        ),
                                        onProgress = { completed, total ->
                                            aiCompleted = completed
                                            aiTotal = total
                                        },
                                        onSceneGenerated = { generated ->
                                            if (!resetSeed) {
                                                sceneStore.persist(timelineKey, listOf(generated))
                                                generatedScenes = sceneStore.load(timelineKey)
                                            }
                                        }
                                    )
                                }.onSuccess { newScenes ->
                                    val completeFreshVariation =
                                        !resetSeed || newScenes.size == targetIndexes.size
                                    if (completeFreshVariation) {
                                        sceneStore.persist(timelineKey, newScenes)
                                        generatedScenes = sceneStore.load(timelineKey)
                                    } else {
                                        sceneStore.setSeed(timelineKey, previousSeed)
                                    }
                                    isGeneratingAi = false
                                    aiGenerationJob = null
                                    val expected = scenario.chapters.take(5).size
                                    val message = when {
                                        resetSeed && !completeFreshVariation ->
                                            "New variation incomplete • previous timeline kept"
                                        generatedScenes.size == expected ->
                                            "AI scenes ready"
                                        else ->
                                            "Partial result: " + generatedScenes.size + "/" + expected + " scenes ready"
                                    }
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }.onFailure {
                                    if (resetSeed) {
                                        sceneStore.setSeed(timelineKey, previousSeed)
                                    }
                                    isGeneratingAi = false
                                    aiGenerationJob = null
                                    if (it !is kotlinx.coroutines.CancellationException) {
                                        Toast.makeText(
                                            context,
                                            "AI generation failed: " + (it.message ?: "unknown error"),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (!isGeneratingAi) {
                                val expectedIndexes = scenario.chapters.take(5).indices.toSet()
                                val existingIndexes = generatedScenes.map { it.chapterIndex }.toSet()
                                val missingIndexes = expectedIndexes - existingIndexes
                                if (generatedScenes.size >= expectedIndexes.size && expectedIndexes.isNotEmpty()) {
                                    showRegenerateAllDialog = true
                                } else {
                                    startGeneration(
                                        if (missingIndexes.isNotEmpty()) missingIndexes else expectedIndexes,
                                        false
                                    )
                                }
                            }
                        },
                        enabled = !isGeneratingAi && !isExporting && !isRenderingShareImage,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            if (isGeneratingAi) "Generating AI scenes • $aiCompleted/$aiTotal"
                            else if (generatedScenes.isEmpty()) "Generate AI scenes"
                            else if (generatedScenes.size < scenario.chapters.take(5).size) "Generate missing AI scenes"
                            else "Regenerate all AI scenes",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (generatedScenes.isNotEmpty()) {
                        TextButton(
                            onClick = { showClearAiDialog = true },
                            enabled = !isGeneratingAi && !isExporting && !isRenderingShareImage,
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
                                    onClick = {
                                        sceneStore.clear(timelineKey)
                                        generatedScenes = emptyList()
                                        completedVideoUri = null
                                        showClearAiDialog = false
                                        Toast.makeText(context, "AI scenes removed", Toast.LENGTH_SHORT).show()
                                    }
                                ) { Text("Remove") }
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
                    if (isGeneratingAi) {
                        Spacer(Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                aiGenerationJob?.cancel()
                                aiGenerationJob = null
                                isGeneratingAi = false
                                aiCompleted = 0
                                aiTotal = 0
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Cancel AI generation") }
                        LinearProgressIndicator(
                            progress = {
                                if (aiTotal == 0) 0f else aiCompleted.toFloat() / aiTotal.toFloat()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
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
                                    ShareCardRenderer.share(context, shareUri, scenario)
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
                    enabled = !isRenderingShareImage && !isGeneratingAi && !isExporting && hasVisualAsset,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AltPrimary, contentColor = Color(0xFF16111F))
                ) {
                    Text(
                        if (isRenderingShareImage) "Preparing share image…" else "Share this ALT life",
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
                        enabled = !isRenderingShareImage && !isGeneratingAi && !isExporting && hasVisualAsset,
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

                                prepared.onSuccess { sceneUris ->
                                    activeTransformer = CinematicVideoExporter.export(
                                        context = context,
                                        imageUris = sceneUris,
                                        scenario = scenario,
                                        onCompleted = { videoUri ->
                                            isExporting = false
                                            exportProgress = 100
                                            activeTransformer = null
                                            exportJob = null
                                            completedVideoUri = videoUri
                                            Toast.makeText(context, "Video ready", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = {
                                            isExporting = false
                                            exportProgress = null
                                            activeTransformer = null
                                            exportJob = null
                                            completedVideoUri = null
                                            Toast.makeText(
                                                context,
                                                "Video export failed: " + (it.message ?: "unknown error"),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }.onFailure {
                                    isExporting = false
                                    exportProgress = null
                                    activeTransformer = null
                                    exportJob = null
                                    completedVideoUri = null
                                    if (it !is kotlinx.coroutines.CancellationException) {
                                        Toast.makeText(
                                            context,
                                            "Could not prepare video: " + (it.message ?: "unknown error"),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isExporting && !isGeneratingAi && !isRenderingShareImage && hasVisualAsset,
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
                            activeTransformer?.cancel()
                            activeTransformer = null
                            isExporting = false
                            exportProgress = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Cancel export") }
                }

                completedVideoUri?.let { videoUri ->
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { CinematicVideoExporter.share(context, videoUri, scenario) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) { Text("Share MP4", fontWeight = FontWeight.Bold) }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        Spacer(Modifier.height(8.dp))
                        androidx.compose.material3.TextButton(
                            onClick = {
                                runCatching {
                                    CinematicVideoExporter.saveToGallery(context, videoUri, scenario)
                                }.onSuccess {
                                    Toast.makeText(context, "Saved to Movies/ALT", Toast.LENGTH_SHORT).show()
                                }.onFailure {
                                    Toast.makeText(context, "Could not save video", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Save MP4 to gallery") }
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
                Spacer(Modifier.height(12.dp))
                Text(
                    "Multi-scene 9:16 MP4 • animated chapter sequence",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = AltDimmed,
                    fontSize = 12.sp
                )
            }
        }
    }
}

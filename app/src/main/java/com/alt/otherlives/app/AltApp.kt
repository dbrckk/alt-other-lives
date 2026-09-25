package com.alt.otherlives.app

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import com.alt.otherlives.core.data.HistoryRepository
import com.alt.otherlives.core.data.SourcePhotoStore
import com.alt.otherlives.core.generation.GenerationSettingsRepository
import com.alt.otherlives.core.generation.GeneratedSceneStore
import com.alt.otherlives.core.generation.ComfyUiClient
import com.alt.otherlives.core.generation.ComfyUiConfig
import androidx.compose.ui.Modifier
import com.alt.otherlives.core.data.ScenarioCatalog
import com.alt.otherlives.core.designsystem.AltBackground
import com.alt.otherlives.core.designsystem.AltTheme
import com.alt.otherlives.feature.home.HomeScreen
import com.alt.otherlives.feature.history.HistoryScreen
import com.alt.otherlives.feature.scenarios.ScenarioScreen
import com.alt.otherlives.feature.settings.GenerationSettingsScreen
import com.alt.otherlives.feature.timeline.RevealScreen

private enum class Screen { HOME, SCENARIOS, REVEAL, HISTORY, SETTINGS }

private const val HISTORY_PREVIEW_LIMIT = 16

@Composable
fun AltApp() {
    var screen by remember { mutableStateOf(Screen.HOME) }
    var settingsReturnScreen by remember { mutableStateOf(Screen.HOME) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoFileName by remember { mutableStateOf<String?>(null) }
    var selectedScenario by remember { mutableStateOf(ScenarioCatalog.scenarios.first()) }
    var activeTimelineKey by remember { mutableStateOf<String?>(null) }
    var generationSettingsMessage by remember { mutableStateOf<String?>(null) }
    var isImportingPhoto by remember { mutableStateOf(false) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var isSavingGenerationSettings by remember { mutableStateOf(false) }
    var isClearingGenerationSettings by remember { mutableStateOf(false) }
    var connectionTestJob by remember { mutableStateOf<Job?>(null) }
    var unavailablePhotoFileNames by remember { mutableStateOf<Set<String>>(emptySet()) }
    var historyPhotoUris by remember { mutableStateOf<Map<String, Uri>>(emptyMap()) }
    var historyGeneratedPreviewUris by remember { mutableStateOf<Map<String, Uri>>(emptyMap()) }
    var deletingHistoryEntryKey by remember { mutableStateOf<String?>(null) }
    var isClearingHistory by remember { mutableStateOf(false) }
    var isCreatingTimeline by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val historyRepository = remember(context) { HistoryRepository(context.applicationContext) }
    val sourcePhotoStore = remember(context) { SourcePhotoStore(context.applicationContext) }
    val generationSettingsRepository = remember(context) {
        GenerationSettingsRepository(context.applicationContext)
    }
    val generatedSceneStore = remember(context) {
        GeneratedSceneStore(context.applicationContext)
    }
    val scope = rememberCoroutineScope()
    val history by historyRepository.history.collectAsState(initial = emptyList())
    val generationSettings by generationSettingsRepository.settings.collectAsState(
        initial = com.alt.otherlives.core.generation.GenerationSettings()
    )

    LaunchedEffect(Unit) {
        val latestStoredPhoto = if (photoUri == null && !isImportingPhoto) {
            runCatching {
                withContext(Dispatchers.IO) {
                    sourcePhotoStore.latestStoredPhoto()
                }
            }.getOrNull()
        } else {
            null
        }

        latestStoredPhoto?.let { stored ->
            photoUri = stored.uri
            photoFileName = stored.fileName
        }

        val startupHistory = runCatching {
            historyRepository.history.first()
        }.getOrDefault(emptyList())

        startupHistory.firstOrNull()?.let { latest ->
            when (
                StartupRestorePolicy.decide(
                    currentPhotoFileName = photoFileName,
                    latestHistoryPhotoFileName = latest.photoFileName,
                    hasLatestStoredPhoto = latestStoredPhoto != null
                )
            ) {
                StartupRestoreDecision.USE_CURRENT_HISTORY_CONTEXT -> {
                    ScenarioCatalog.scenarios
                        .firstOrNull { it.id == latest.scenarioId }
                        ?.let { scenario ->
                            selectedScenario = scenario
                            activeTimelineKey = latest.timelineKey
                        }
                }

                StartupRestoreDecision.RESTORE_HISTORY_PHOTO -> {
                    val restoredHistoryPhoto = runCatching {
                        withContext(Dispatchers.IO) {
                            sourcePhotoStore.uriFor(requireNotNull(latest.photoFileName))
                        }
                    }.getOrNull()

                    if (restoredHistoryPhoto != null) {
                        photoUri = restoredHistoryPhoto
                        photoFileName = latest.photoFileName
                        ScenarioCatalog.scenarios
                            .firstOrNull { it.id == latest.scenarioId }
                            ?.let { scenario ->
                                selectedScenario = scenario
                                activeTimelineKey = latest.timelineKey
                            }
                    }
                }

                StartupRestoreDecision.KEEP_CURRENT_PHOTO -> Unit
            }
        }
    }

    LaunchedEffect(history, screen) {
        if (screen != Screen.HISTORY) return@LaunchedEffect
        val photoFileNames = history.mapNotNull { it.photoFileName }.distinct()
        val preflight = withContext(Dispatchers.IO) {
            val recentEntries = history.take(HISTORY_PREVIEW_LIMIT)
            val availablePhotos = photoFileNames.mapNotNull { fileName ->
                runCatching { sourcePhotoStore.uriFor(fileName) }
                    .getOrNull()
                    ?.let { fileName to it }
            }.toMap()
            val generatedPreviews = recentEntries.mapNotNull { entry ->
                val timelineKey = entry.timelineKey
                runCatching {
                    generatedSceneStore.load(timelineKey)
                        .minByOrNull { it.chapterIndex }
                        ?.imageUri
                }.getOrNull()
                    ?.let { timelineKey to it }
            }.toMap()
            availablePhotos to generatedPreviews
        }
        historyPhotoUris = preflight.first
        historyGeneratedPreviewUris = preflight.second
        unavailablePhotoFileNames = photoFileNames
            .filterNot { it in preflight.first }
            .toSet()
    }


    AltTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = AltBackground) {
            AnimatedContent(
                targetState = screen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen"
            ) { current ->
                when (current) {
                    Screen.HOME -> HomeScreen(
                        photoUri = photoUri,
                        isImportingPhoto = isImportingPhoto,
                        onPhotoSelected = { selectedUri ->
                            if (!isImportingPhoto) {
                                isImportingPhoto = true
                                scope.launch {
                                    try {
                                        val imported = runCatching {
                                            withContext(Dispatchers.IO) {
                                                sourcePhotoStore.import(selectedUri)
                                            }
                                        }

                                        imported.onSuccess { stored ->
                                            photoUri = stored.uri
                                            photoFileName = stored.fileName
                                            activeTimelineKey = null

                                            val keep =
                                                history.mapNotNull { it.photoFileName }.toSet() +
                                                    stored.fileName
                                            runCatching {
                                                withContext(Dispatchers.IO) {
                                                    sourcePhotoStore.deleteUnreferenced(keep)
                                                }
                                            }.onFailure {
                                                Toast.makeText(
                                                    context,
                                                    "Photo imported, but some old local photos could not be cleaned up",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }.onFailure {
                                            Toast.makeText(
                                                context,
                                                "Could not import photo: " +
                                                    (it.message ?: "unknown error"),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } finally {
                                        isImportingPhoto = false
                                    }
                                }
                            }
                        },
                        onContinue = { screen = Screen.SCENARIOS },
                        onHistory = { screen = Screen.HISTORY },
                        onAiSettings = {
                            settingsReturnScreen = Screen.HOME
                            screen = Screen.SETTINGS
                        }
                    )
                    Screen.SCENARIOS -> ScenarioScreen(
                        scenarios = ScenarioCatalog.scenarios,
                        onBack = {
                            if (!isCreatingTimeline) {
                                screen = Screen.HOME
                            }
                        },
                        isCreatingTimeline = isCreatingTimeline,
                        onSelect = { scenario ->
                            if (!isCreatingTimeline) {
                                isCreatingTimeline = true
                                val createdAt = System.currentTimeMillis()
                                scope.launch {
                                    val recordResult = runCatching {
                                        historyRepository.record(
                                            scenarioId = scenario.id,
                                            photoFileName = photoFileName,
                                            createdAt = createdAt
                                        )
                                    }

                                    recordResult.onSuccess { keep ->
                                        selectedScenario = scenario
                                        activeTimelineKey = scenario.id + "-" + createdAt
                                        screen = Screen.REVEAL

                                        runCatching {
                                            withContext(Dispatchers.IO) {
                                                sourcePhotoStore.deleteUnreferenced(
                                                    keep.photoFileNames
                                                )
                                                generatedSceneStore.deleteUnreferenced(
                                                    keep.timelineKeys
                                                )
                                            }
                                        }.onFailure {
                                            Toast.makeText(
                                                context,
                                                "Timeline created, but some old local media could not be cleaned up",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }.onFailure {
                                        Toast.makeText(
                                            context,
                                            "Could not create timeline: " +
                                                (it.message ?: "unknown error"),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    isCreatingTimeline = false
                                }
                            }
                        }
                    )
                    Screen.REVEAL -> RevealScreen(
                        photoUri = photoUri,
                        scenario = selectedScenario,
                        generationSettings = generationSettings,
                        timelineKey = activeTimelineKey ?: selectedScenario.id,
                        onBack = { screen = Screen.SCENARIOS },
                        onAiSettings = {
                            settingsReturnScreen = Screen.REVEAL
                            screen = Screen.SETTINGS
                        }
                    )
                    Screen.SETTINGS -> GenerationSettingsScreen(
                        settings = generationSettings,
                        onBack = {
                            connectionTestJob?.cancel()
                            connectionTestJob = null
                            isTestingConnection = false
                            generationSettingsMessage = null
                            screen = settingsReturnScreen
                        },
                        onSave = { baseUrl, workflowJson ->
                            if (
                                !isTestingConnection &&
                                !isSavingGenerationSettings &&
                                !isClearingGenerationSettings
                            ) {
                                isSavingGenerationSettings = true
                                scope.launch {
                                    val result = runCatching {
                                        generationSettingsRepository.save(baseUrl, workflowJson)
                                    }
                                    if (screen == Screen.SETTINGS) {
                                        result.onSuccess {
                                            generationSettingsMessage = "ComfyUI settings saved"
                                        }.onFailure {
                                            generationSettingsMessage =
                                                it.message ?: "Could not save ComfyUI settings"
                                        }
                                    }
                                    isSavingGenerationSettings = false
                                }
                            }
                        },
                        onTestConnection = { baseUrl ->
                            if (
                                !isTestingConnection &&
                                !isSavingGenerationSettings &&
                                !isClearingGenerationSettings
                            ) {
                                isTestingConnection = true
                                connectionTestJob = scope.launch {
                                    generationSettingsMessage = "Testing ComfyUI connection…"
                                    runCatching {
                                        ComfyUiClient(
                                            context = context.applicationContext,
                                            config = ComfyUiConfig(baseUrl)
                                        ).testConnection()
                                    }.onSuccess {
                                        generationSettingsMessage = "ComfyUI connection successful"
                                    }.onFailure {
                                        generationSettingsMessage = it.message ?: "Could not connect to ComfyUI"
                                    }
                                    isTestingConnection = false
                                    connectionTestJob = null
                                }
                            }
                        },
                        onClear = {
                            if (
                                !isTestingConnection &&
                                !isSavingGenerationSettings &&
                                !isClearingGenerationSettings
                            ) {
                                isClearingGenerationSettings = true
                                scope.launch {
                                    val result = runCatching {
                                        generationSettingsRepository.clear()
                                    }
                                    if (screen == Screen.SETTINGS) {
                                        result.onSuccess {
                                            generationSettingsMessage = "AI settings cleared"
                                        }.onFailure {
                                            generationSettingsMessage =
                                                it.message ?: "Could not clear AI settings"
                                        }
                                    }
                                    isClearingGenerationSettings = false
                                }
                            }
                        },
                        statusMessage = generationSettingsMessage,
                        isTestingConnection = isTestingConnection,
                        isSavingSettings = isSavingGenerationSettings,
                        isClearingSettings = isClearingGenerationSettings
                    )
                    Screen.HISTORY -> HistoryScreen(
                        entries = history,
                        scenarios = ScenarioCatalog.scenarios,
                        onBack = { screen = Screen.HOME },
                        unavailablePhotoFileNames = unavailablePhotoFileNames,
                        photoUrisByFileName = historyPhotoUris,
                        generatedPreviewUrisByTimelineKey = historyGeneratedPreviewUris,
                        deletingEntryKey = deletingHistoryEntryKey,
                        isClearingHistory = isClearingHistory,
                        onOpen = { scenario, entry ->
                            selectedScenario = scenario
                            activeTimelineKey = scenario.id + "-" + entry.createdAt
                            val restored = entry.photoFileName?.let { fileName ->
                                runCatching { sourcePhotoStore.uriFor(fileName) }
                                    .getOrNull()
                                    ?.let { uri -> uri to fileName }
                            }
                            photoUri = restored?.first
                            photoFileName = restored?.second
                            if (entry.photoFileName != null && restored == null) {
                                val hasAiPreview = entry.timelineKey in historyGeneratedPreviewUris
                                Toast.makeText(
                                    context,
                                    if (hasAiPreview) {
                                        "Original photo unavailable • opening saved AI timeline"
                                    } else {
                                        "This timeline's source photo is no longer available"
                                    },
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            screen = Screen.REVEAL
                        },
                        onDelete = { entry ->
                            val entryKey = entry.scenarioId + ":" + entry.createdAt
                            if (deletingHistoryEntryKey == null) {
                                deletingHistoryEntryKey = entryKey
                                scope.launch {
                                    val removal = runCatching {
                                        historyRepository.remove(entry)
                                    }

                                    removal.onSuccess { keep ->
                                        val deletedTimelineKey =
                                            entry.timelineKey
                                        historyGeneratedPreviewUris =
                                            historyGeneratedPreviewUris - deletedTimelineKey
                                        entry.photoFileName?.let { deletedPhotoFileName ->
                                            if (deletedPhotoFileName !in keep.photoFileNames) {
                                                historyPhotoUris =
                                                    historyPhotoUris - deletedPhotoFileName
                                                unavailablePhotoFileNames =
                                                    unavailablePhotoFileNames - deletedPhotoFileName
                                                if (photoFileName == deletedPhotoFileName) {
                                                    photoUri = null
                                                    photoFileName = null
                                                }
                                            }
                                        }
                                        if (activeTimelineKey == deletedTimelineKey) {
                                            activeTimelineKey = null
                                        }

                                        runCatching {
                                            withContext(Dispatchers.IO) {
                                                sourcePhotoStore.deleteUnreferenced(
                                                    keep.photoFileNames
                                                )
                                                generatedSceneStore.deleteUnreferenced(
                                                    keep.timelineKeys
                                                )
                                            }
                                        }.onFailure {
                                            Toast.makeText(
                                                context,
                                                "Timeline deleted, but some local media could not be cleaned up",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }.onFailure {
                                        Toast.makeText(
                                            context,
                                            "Could not delete timeline: " +
                                                (it.message ?: "unknown error"),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    deletingHistoryEntryKey = null
                                }
                            }
                        },
                        onClear = {
                            if (!isClearingHistory && deletingHistoryEntryKey == null) {
                                isClearingHistory = true
                                scope.launch {
                                    val cleared = runCatching {
                                        historyRepository.clear()
                                    }

                                    cleared.onSuccess {
                                        photoUri = null
                                        photoFileName = null
                                        activeTimelineKey = null
                                        historyPhotoUris = emptyMap()
                                        historyGeneratedPreviewUris = emptyMap()
                                        unavailablePhotoFileNames = emptySet()
                                        deletingHistoryEntryKey = null

                                        runCatching {
                                            withContext(Dispatchers.IO) {
                                                sourcePhotoStore.clearAll()
                                                generatedSceneStore.clearAll()
                                            }
                                        }.onFailure {
                                            Toast.makeText(
                                                context,
                                                "History cleared, but some local media could not be removed",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }.onFailure {
                                        Toast.makeText(
                                            context,
                                            "Could not clear history: " +
                                                (it.message ?: "unknown error"),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    isClearingHistory = false
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

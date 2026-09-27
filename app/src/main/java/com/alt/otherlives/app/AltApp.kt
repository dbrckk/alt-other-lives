package com.alt.otherlives.app

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
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
import com.alt.otherlives.core.media.TransientMediaCache
import androidx.compose.ui.Modifier
import com.alt.otherlives.core.data.ScenarioCatalog
import com.alt.otherlives.core.designsystem.AltBackground
import com.alt.otherlives.core.designsystem.AltTheme
import com.alt.otherlives.feature.home.HomeScreen
import com.alt.otherlives.feature.history.HistoryScreen
import com.alt.otherlives.feature.scenarios.ScenarioScreen
import com.alt.otherlives.feature.settings.GenerationSettingsScreen
import com.alt.otherlives.feature.timeline.RevealScreen
import com.alt.otherlives.R

private const val HISTORY_PREVIEW_LIMIT = 16

@Composable
fun AltApp() {
    var navigation by rememberSaveable(
        stateSaver = Saver(
            save = { state ->
                listOf(
                    state.screen.name,
                    state.settingsReturnScreen.name
                )
            },
            restore = { saved ->
                AltNavigationState.restore(
                    screenName = saved.getOrNull(0),
                    settingsReturnScreenName = saved.getOrNull(1)
                )
            }
        )
    ) {
        mutableStateOf(AltNavigationState())
    }
    val configuration = LocalConfiguration.current
    val appLanguage = configuration.locales[0].language
    val scenarios = remember(appLanguage) {
        ScenarioCatalog.forLanguage(appLanguage)
    }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoFileName by remember { mutableStateOf<String?>(null) }
    var selectedScenario by remember {
        mutableStateOf(scenarios.first())
    }
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

    LaunchedEffect(scenarios) {
        selectedScenario = scenarios
            .firstOrNull { it.id == selectedScenario.id }
            ?: scenarios.first()
    }

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
        runCatching {
            withContext(Dispatchers.IO) {
                TransientMediaCache.cleanup(context.applicationContext)
            }
        }

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
                    scenarios
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
                        scenarios
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

    LaunchedEffect(history, navigation.screen) {
        if (navigation.screen != AltScreen.HISTORY) return@LaunchedEffect
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
                targetState = navigation.screen,
                transitionSpec = {
                    (
                        fadeIn(animationSpec = tween(280)) +
                            scaleIn(
                                initialScale = 0.985f,
                                animationSpec = tween(320)
                            )
                        ) togetherWith (
                        fadeOut(animationSpec = tween(180)) +
                            scaleOut(
                                targetScale = 1.01f,
                                animationSpec = tween(180)
                            )
                        )
                },
                label = "screen"
            ) { current ->
                when (current) {
                    AltScreen.HOME -> HomeScreen(
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

                                            if (!stored.isLikelyPremiumSource) {
                                                Toast.makeText(
                                                    context,
                                                    context.getString(R.string.home_low_quality_source_warning),
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }

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
                                                    context.getString(R.string.app_photo_cleanup_warning),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }.onFailure {
                                            Toast.makeText(
                                                context,
                                                context.getString(
                                                    R.string.app_photo_import_failed,
                                                    it.message
                                                        ?: context.getString(
                                                            R.string.common_unknown_error
                                                        )
                                                ),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } finally {
                                        isImportingPhoto = false
                                    }
                                }
                            }
                        },
                        onContinue = { navigation = navigation.goTo(AltScreen.SCENARIOS) },
                        onHistory = { navigation = navigation.goTo(AltScreen.HISTORY) }
                    )
                    AltScreen.SCENARIOS -> ScenarioScreen(
                        scenarios = scenarios,
                        onBack = {
                            if (!isCreatingTimeline) {
                                navigation = navigation.goTo(AltScreen.HOME)
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
                                        val recordedEntry = requireNotNull(keep.recordedEntry) {
                                            "Timeline record did not return its persisted entry"
                                        }
                                        selectedScenario = scenario
                                        activeTimelineKey = recordedEntry.timelineKey
                                        navigation = navigation.goTo(AltScreen.REVEAL)

                                        val cleanupFailures = withContext(Dispatchers.IO) {
                                            listOfNotNull(
                                                runCatching {
                                                    sourcePhotoStore.deleteUnreferenced(
                                                        keep.photoFileNames
                                                    )
                                                }.exceptionOrNull(),
                                                runCatching {
                                                    generatedSceneStore.deleteUnreferenced(
                                                        keep.timelineKeys
                                                    )
                                                }.exceptionOrNull()
                                            )
                                        }
                                        if (cleanupFailures.isNotEmpty()) {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.app_timeline_cleanup_warning),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }.onFailure {
                                        Toast.makeText(
                                            context,
                                            context.getString(
                                                R.string.app_timeline_create_failed,
                                                it.message
                                                    ?: context.getString(
                                                        R.string.common_unknown_error
                                                    )
                                            ),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    isCreatingTimeline = false
                                }
                            }
                        }
                    )
                    AltScreen.REVEAL -> RevealScreen(
                        photoUri = photoUri,
                        scenario = selectedScenario,
                        generationSettings = generationSettings,
                        timelineKey = activeTimelineKey ?: selectedScenario.id,
                        onBack = { navigation = navigation.goTo(AltScreen.SCENARIOS) },
                        onAiSettings = {
                            navigation = navigation.openSettings()
                        },
                        onCreateAnotherLife = {
                            navigation = navigation.goTo(AltScreen.SCENARIOS)
                        },
                        onRemixThisLife = {
                            if (!isCreatingTimeline) {
                                isCreatingTimeline = true
                                val createdAt = System.currentTimeMillis()
                                scope.launch {
                                    val recordResult = runCatching {
                                        historyRepository.record(
                                            scenarioId = selectedScenario.id,
                                            photoFileName = photoFileName,
                                            createdAt = createdAt
                                        )
                                    }
                                    recordResult.onSuccess { keep ->
                                        val recordedEntry = requireNotNull(keep.recordedEntry) {
                                            "Remix timeline record did not return its persisted entry"
                                        }
                                        activeTimelineKey = recordedEntry.timelineKey

                                        val cleanupFailures = withContext(Dispatchers.IO) {
                                            listOfNotNull(
                                                runCatching {
                                                    sourcePhotoStore.deleteUnreferenced(
                                                        keep.photoFileNames
                                                    )
                                                }.exceptionOrNull(),
                                                runCatching {
                                                    generatedSceneStore.deleteUnreferenced(
                                                        keep.timelineKeys
                                                    )
                                                }.exceptionOrNull()
                                            )
                                        }
                                        if (cleanupFailures.isNotEmpty()) {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.app_remix_cleanup_warning),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }.onFailure {
                                        Toast.makeText(
                                            context,
                                            context.getString(
                                                R.string.app_remix_failed,
                                                it.message
                                                    ?: context.getString(
                                                        R.string.common_unknown_error
                                                    )
                                            ),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    isCreatingTimeline = false
                                }
                            }
                        },
                        isRemixingLife = isCreatingTimeline
                    )
                    AltScreen.SETTINGS -> GenerationSettingsScreen(
                        settings = generationSettings,
                        onBack = {
                            connectionTestJob?.cancel()
                            connectionTestJob = null
                            isTestingConnection = false
                            generationSettingsMessage = null
                            navigation = navigation.closeSettings()
                        },
                        onSave = { baseUrl, workflowJson, remotePhotoUploadConsent ->
                            if (
                                !isTestingConnection &&
                                !isSavingGenerationSettings &&
                                !isClearingGenerationSettings
                            ) {
                                isSavingGenerationSettings = true
                                scope.launch {
                                    val result = runCatching {
                                        generationSettingsRepository.save(
                                            baseUrl,
                                            workflowJson,
                                            remotePhotoUploadConsent
                                        )
                                    }
                                    if (navigation.screen == AltScreen.SETTINGS) {
                                        result.onSuccess {
                                            generationSettingsMessage =
                                                context.getString(R.string.app_settings_saved)
                                        }.onFailure {
                                            generationSettingsMessage =
                                                it.message
                                                    ?: context.getString(
                                                        R.string.app_settings_save_failed
                                                    )
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
                                    generationSettingsMessage =
                                        context.getString(R.string.app_connection_testing)
                                    runCatching {
                                        ComfyUiClient(
                                            context = context.applicationContext,
                                            config = ComfyUiConfig(baseUrl)
                                        ).testConnection()
                                    }.onSuccess {
                                        generationSettingsMessage =
                                            context.getString(R.string.app_connection_success)
                                    }.onFailure {
                                        generationSettingsMessage = it.message
                                            ?: context.getString(R.string.app_connection_failed)
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
                                    if (navigation.screen == AltScreen.SETTINGS) {
                                        result.onSuccess {
                                            generationSettingsMessage =
                                            context.getString(R.string.app_settings_cleared)
                                        }.onFailure {
                                            generationSettingsMessage =
                                                it.message
                                                    ?: context.getString(
                                                        R.string.app_settings_clear_failed
                                                    )
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
                    AltScreen.HISTORY -> HistoryScreen(
                        entries = history,
                        scenarios = scenarios,
                        onBack = { navigation = navigation.goTo(AltScreen.HOME) },
                        unavailablePhotoFileNames = unavailablePhotoFileNames,
                        photoUrisByFileName = historyPhotoUris,
                        generatedPreviewUrisByTimelineKey = historyGeneratedPreviewUris,
                        deletingEntryKey = deletingHistoryEntryKey,
                        isClearingHistory = isClearingHistory,
                        onOpen = { scenario, entry ->
                            selectedScenario = scenario
                            activeTimelineKey = entry.timelineKey
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
                                        context.getString(R.string.app_original_photo_missing_ai)
                                    } else {
                                        context.getString(R.string.app_original_photo_missing)
                                    },
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            navigation = navigation.goTo(AltScreen.REVEAL)
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

                                        val cleanupFailures = withContext(Dispatchers.IO) {
                                            listOfNotNull(
                                                runCatching {
                                                    sourcePhotoStore.deleteUnreferenced(
                                                        keep.photoFileNames
                                                    )
                                                }.exceptionOrNull(),
                                                runCatching {
                                                    generatedSceneStore.deleteUnreferenced(
                                                        keep.timelineKeys
                                                    )
                                                }.exceptionOrNull()
                                            )
                                        }
                                        if (cleanupFailures.isNotEmpty()) {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.app_timeline_delete_cleanup_warning),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }.onFailure {
                                        Toast.makeText(
                                            context,
                                            context.getString(
                                                R.string.app_timeline_delete_failed,
                                                it.message
                                                    ?: context.getString(
                                                        R.string.common_unknown_error
                                                    )
                                            ),
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

                                        val cleanupFailures = withContext(Dispatchers.IO) {
                                            listOfNotNull(
                                                runCatching {
                                                    sourcePhotoStore.clearAll()
                                                }.exceptionOrNull(),
                                                runCatching {
                                                    generatedSceneStore.clearAll()
                                                }.exceptionOrNull()
                                            )
                                        }
                                        if (cleanupFailures.isNotEmpty()) {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.app_history_clear_cleanup_warning),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }.onFailure {
                                        Toast.makeText(
                                            context,
                                            context.getString(
                                                R.string.app_history_clear_failed,
                                                it.message
                                                    ?: context.getString(
                                                        R.string.common_unknown_error
                                                    )
                                            ),
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

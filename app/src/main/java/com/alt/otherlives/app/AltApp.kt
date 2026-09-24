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
import kotlinx.coroutines.withContext
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

@Composable
fun AltApp() {
    var screen by remember { mutableStateOf(Screen.HOME) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoFileName by remember { mutableStateOf<String?>(null) }
    var selectedScenario by remember { mutableStateOf(ScenarioCatalog.scenarios.first()) }
    var activeTimelineKey by remember { mutableStateOf<String?>(null) }
    var generationSettingsMessage by remember { mutableStateOf<String?>(null) }
    var isImportingPhoto by remember { mutableStateOf(false) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var hasRestoredStartupPhoto by remember { mutableStateOf(false) }
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
        if (photoUri == null && !isImportingPhoto) {
            withContext(Dispatchers.IO) {
                sourcePhotoStore.latestStoredPhoto()
            }?.let { stored ->
                photoUri = stored.uri
                photoFileName = stored.fileName
            }
        }
        hasRestoredStartupPhoto = true
    }

    LaunchedEffect(history, hasRestoredStartupPhoto) {
        if (hasRestoredStartupPhoto && activeTimelineKey == null && history.isNotEmpty()) {
            val latest = history.first()
            ScenarioCatalog.scenarios.firstOrNull { it.id == latest.scenarioId }?.let { scenario ->
                selectedScenario = scenario
                activeTimelineKey = scenario.id + "-" + latest.createdAt
                if (photoUri == null) {
                    latest.photoFileName?.let { fileName ->
                        runCatching { sourcePhotoStore.uriFor(fileName) }
                            .getOrNull()
                            ?.let { uri ->
                                photoUri = uri
                                photoFileName = fileName
                            }
                    }
                }
            }
        }
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
                                    runCatching {
                                        withContext(Dispatchers.IO) {
                                            sourcePhotoStore.import(selectedUri)
                                        }
                                    }.onSuccess { stored ->
                                        photoUri = stored.uri
                                        photoFileName = stored.fileName
                                        val keep = history.mapNotNull { it.photoFileName }.toSet() + stored.fileName
                                        withContext(Dispatchers.IO) {
                                            sourcePhotoStore.deleteUnreferenced(keep)
                                        }
                                    }.onFailure {
                                        Toast.makeText(
                                            context,
                                            "Could not import photo: " + (it.message ?: "unknown error"),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    isImportingPhoto = false
                                }
                            }
                        },
                        onContinue = { screen = Screen.SCENARIOS },
                        onHistory = { screen = Screen.HISTORY },
                        onAiSettings = { screen = Screen.SETTINGS }
                    )
                    Screen.SCENARIOS -> ScenarioScreen(
                        scenarios = ScenarioCatalog.scenarios,
                        onBack = { screen = Screen.HOME },
                        onSelect = {
                            selectedScenario = it
                            val createdAt = System.currentTimeMillis()
                            activeTimelineKey = it.id + "-" + createdAt
                            scope.launch {
                                val keep = historyRepository.record(
                                    scenarioId = it.id,
                                    photoFileName = photoFileName,
                                    createdAt = createdAt
                                )
                                withContext(Dispatchers.IO) {
                                    sourcePhotoStore.deleteUnreferenced(keep.photoFileNames)
                                    generatedSceneStore.deleteUnreferenced(keep.timelineKeys)
                                }
                            }
                            screen = Screen.REVEAL
                        }
                    )
                    Screen.REVEAL -> RevealScreen(
                        photoUri = photoUri,
                        scenario = selectedScenario,
                        generationSettings = generationSettings,
                        timelineKey = activeTimelineKey ?: selectedScenario.id,
                        onBack = { screen = Screen.SCENARIOS }
                    )
                    Screen.SETTINGS -> GenerationSettingsScreen(
                        settings = generationSettings,
                        onBack = {
                            generationSettingsMessage = null
                            screen = Screen.HOME
                        },
                        onSave = { baseUrl, workflowJson ->
                            scope.launch {
                                runCatching {
                                    generationSettingsRepository.save(baseUrl, workflowJson)
                                }.onSuccess {
                                    generationSettingsMessage = "ComfyUI settings saved"
                                }.onFailure {
                                    generationSettingsMessage = it.message ?: "Could not save ComfyUI settings"
                                }
                            }
                        },
                        onTestConnection = { baseUrl ->
                            if (!isTestingConnection) {
                                isTestingConnection = true
                                scope.launch {
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
                                }
                            }
                        },
                        onClear = {
                            scope.launch {
                                generationSettingsRepository.clear()
                                generationSettingsMessage = "AI settings cleared"
                            }
                        },
                        statusMessage = generationSettingsMessage,
                        isTestingConnection = isTestingConnection
                    )
                    Screen.HISTORY -> HistoryScreen(
                        entries = history,
                        scenarios = ScenarioCatalog.scenarios,
                        onBack = { screen = Screen.HOME },
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
                                Toast.makeText(
                                    context,
                                    "This timeline's source photo is no longer available",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            screen = Screen.REVEAL
                        },
                        onClear = {
                            scope.launch {
                                historyRepository.clear()
                                withContext(Dispatchers.IO) {
                                    sourcePhotoStore.clearAll()
                                    generatedSceneStore.clearAll()
                                }
                                photoUri = null
                                photoFileName = null
                            }
                        }
                    )
                }
            }
        }
    }
}

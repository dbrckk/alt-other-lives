package com.alt.otherlives.app

import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.alt.otherlives.core.data.HistoryRepository
import androidx.compose.ui.Modifier
import com.alt.otherlives.core.data.ScenarioCatalog
import com.alt.otherlives.core.designsystem.AltBackground
import com.alt.otherlives.core.designsystem.AltTheme
import com.alt.otherlives.feature.home.HomeScreen
import com.alt.otherlives.feature.history.HistoryScreen
import com.alt.otherlives.feature.scenarios.ScenarioScreen
import com.alt.otherlives.feature.timeline.RevealScreen

private enum class Screen { HOME, SCENARIOS, REVEAL, HISTORY }

@Composable
fun AltApp() {
    var screen by remember { mutableStateOf(Screen.HOME) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedScenario by remember { mutableStateOf(ScenarioCatalog.scenarios.first()) }
    val context = LocalContext.current
    val historyRepository = remember(context) { HistoryRepository(context.applicationContext) }
    val scope = rememberCoroutineScope()
    val history by historyRepository.history.collectAsState(initial = emptyList())

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
                        onPhotoSelected = { photoUri = it },
                        onContinue = { screen = Screen.SCENARIOS },
                        onHistory = { screen = Screen.HISTORY }
                    )
                    Screen.SCENARIOS -> ScenarioScreen(
                        scenarios = ScenarioCatalog.scenarios,
                        onBack = { screen = Screen.HOME },
                        onSelect = {
                            selectedScenario = it
                            scope.launch { historyRepository.record(it.id) }
                            screen = Screen.REVEAL
                        }
                    )
                    Screen.REVEAL -> RevealScreen(
                        photoUri = photoUri,
                        scenario = selectedScenario,
                        onBack = { screen = Screen.SCENARIOS }
                    )
                    Screen.HISTORY -> HistoryScreen(
                        entries = history,
                        scenarios = ScenarioCatalog.scenarios,
                        onBack = { screen = Screen.HOME },
                        onOpen = {
                            selectedScenario = it
                            screen = Screen.REVEAL
                        },
                        onClear = { scope.launch { historyRepository.clear() } }
                    )
                }
            }
        }
    }
}

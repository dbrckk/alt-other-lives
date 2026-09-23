package com.alt.otherlives

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AltApp() }
    }
}

private enum class Screen { HOME, SCENARIOS, REVEAL }

private data class Scenario(
    val id: String,
    val title: String,
    val subtitle: String,
    val chapters: List<Pair<String, String>>
)

private val scenarios = listOf(
    Scenario(
        "wealth",
        "What if I became wealthy?",
        "A decade where one decision compounds.",
        listOf(
            "2026" to "You stop optimizing for comfort and start optimizing for leverage.",
            "2028" to "A small project becomes your first serious source of freedom.",
            "2031" to "You build systems that keep working while you sleep.",
            "2034" to "Money stops being the destination and becomes infrastructure.",
            "2036" to "Your life looks expensive from the outside — calm from the inside."
        )
    ),
    Scenario(
        "japan",
        "What if I moved to Japan?",
        "A new city, language and version of you.",
        listOf(
            "2026" to "You arrive with two bags and no familiar routine.",
            "2027" to "The city stops feeling foreign and starts feeling precise.",
            "2029" to "Your work, friends and habits become part of the place.",
            "2032" to "You realize you no longer translate your life in your head.",
            "2036" to "Home is no longer where you started."
        )
    ),
    Scenario(
        "future",
        "What if I lived in 2100?",
        "Your alternate life in a world that moved on.",
        listOf(
            "2068" to "Your identity becomes portable across physical and digital spaces.",
            "2077" to "Work is mostly direction, taste and judgment.",
            "2086" to "Cities feel quieter because infrastructure has become invisible.",
            "2094" to "Your oldest memories exist in forms younger generations can walk through.",
            "2100" to "The future feels ordinary when you live inside it."
        )
    ),
    Scenario(
        "disappear",
        "What if I disappeared for 5 years?",
        "No audience. No updates. Just change.",
        listOf(
            "Year 1" to "You remove the noise before you know what will replace it.",
            "Year 2" to "The new routine stops feeling temporary.",
            "Year 3" to "Your skills become visible before you do.",
            "Year 4" to "You stop measuring progress through other people.",
            "Year 5" to "You return with a life that no longer needs explaining."
        )
    )
)

@Composable
private fun AltApp() {
    var screen by remember { mutableStateOf(Screen.HOME) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedScenario by remember { mutableStateOf(scenarios.first()) }

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            background = Color(0xFF08080A),
            surface = Color(0xFF111116),
            primary = Color(0xFFEDE7FF),
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF08080A)) {
            AnimatedContent(
                targetState = screen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen"
            ) { current ->
                when (current) {
                    Screen.HOME -> HomeScreen(
                        photoUri = photoUri,
                        onPhotoSelected = { photoUri = it },
                        onContinue = { screen = Screen.SCENARIOS }
                    )
                    Screen.SCENARIOS -> ScenarioScreen(
                        onBack = { screen = Screen.HOME },
                        onSelect = {
                            selectedScenario = it
                            screen = Screen.REVEAL
                        }
                    )
                    Screen.REVEAL -> RevealScreen(
                        photoUri = photoUri,
                        scenario = selectedScenario,
                        onBack = { screen = Screen.SCENARIOS }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(
    photoUri: Uri?,
    onPhotoSelected: (Uri) -> Unit,
    onContinue: () -> Unit
) {
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let(onPhotoSelected) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 42.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("ALT", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB7A7FF))
            Spacer(Modifier.height(14.dp))
            Text("See the lives\nyou could have lived.", fontSize = 42.sp, lineHeight = 44.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            Text("One photo. One choice. A completely different timeline.", color = Color(0xFFAAA8B3), fontSize = 17.sp)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF29213D), Color(0xFF111116))
                    )
                )
                .clickable { picker.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Selected photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.48f))
                        .padding(16.dp)
                ) {
                    Text("Tap to change photo", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("+", fontSize = 48.sp, color = Color(0xFFEDE7FF))
                    Text("Choose your photo", fontSize = 19.sp, fontWeight = FontWeight.Medium)
                    Text("Your original stays on this device for this prototype.", color = Color(0xFFAAA8B3), fontSize = 13.sp)
                }
            }
        }

        Button(
            onClick = onContinue,
            enabled = photoUri != null,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEDE7FF),
                contentColor = Color(0xFF16111F),
                disabledContainerColor = Color(0xFF25242B),
                disabledContentColor = Color(0xFF777680)
            )
        ) {
            Text("Choose another life", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ScenarioScreen(onBack: () -> Unit, onSelect: (Scenario) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(top = 42.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("‹", fontSize = 36.sp, modifier = Modifier.clickable(onClick = onBack))
            Text("What if…", fontSize = 32.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 12.dp))
        }
        Spacer(Modifier.height(20.dp))
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(scenarios) { scenario ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onSelect(scenario) },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF15141B))
                ) {
                    Column(Modifier.padding(22.dp)) {
                        Text(scenario.title, fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text(scenario.subtitle, color = Color(0xFF9997A2), fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun RevealScreen(photoUri: Uri?, scenario: Scenario, onBack: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 40.dp)
    ) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth().height(500.dp)
            ) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xFF08080A)),
                            startY = 120f
                        )
                    )
                )
                Text(
                    "‹",
                    fontSize = 38.sp,
                    modifier = Modifier.padding(start = 24.dp, top = 44.dp).clickable(onClick = onBack)
                )
                Column(
                    modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)
                ) {
                    Text("YOUR ALT LIFE", color = Color(0xFFB7A7FF), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(scenario.title, fontSize = 34.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        items(scenario.chapters) { chapter ->
            Column(Modifier.padding(horizontal = 24.dp, vertical = 18.dp)) {
                Text(chapter.first, color = Color(0xFFB7A7FF), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(chapter.second, fontSize = 23.sp, lineHeight = 30.sp, fontWeight = FontWeight.Medium)
            }
        }

        item {
            Column(Modifier.padding(24.dp)) {
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEDE7FF),
                        contentColor = Color(0xFF16111F)
                    )
                ) {
                    Text("Create share story", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Local timeline prototype • cinematic export comes next",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Color(0xFF777680),
                    fontSize = 12.sp
                )
            }
        }
    }
}

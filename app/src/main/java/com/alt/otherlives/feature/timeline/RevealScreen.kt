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
import androidx.compose.foundation.lazy.items
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
import androidx.media3.transformer.Transformer
import kotlinx.coroutines.delay
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

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun RevealScreen(photoUri: Uri?, scenario: Scenario, onBack: () -> Unit) {
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }
    var exportProgress by remember { mutableStateOf<Int?>(null) }
    var activeTransformer by remember { mutableStateOf<Transformer?>(null) }
    var completedVideoUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(activeTransformer, isExporting) {
        while (isExporting) {
            activeTransformer?.let { exportProgress = CinematicVideoExporter.progress(it) }
            delay(200)
        }
    }

    DisposableEffect(Unit) {
        onDispose { activeTransformer?.cancel() }
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 40.dp)) {
        item {
            Box(modifier = Modifier.fillMaxWidth().height(500.dp)) {
                AsyncImage(model = photoUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
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
        items(scenario.chapters) { chapter ->
            Column(Modifier.padding(horizontal = 24.dp, vertical = 18.dp)) {
                Text(chapter.label, color = AltAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(chapter.narrative, fontSize = 23.sp, lineHeight = 30.sp, fontWeight = FontWeight.Medium)
            }
        }
        item {
            Column(Modifier.padding(24.dp)) {
                Button(
                    onClick = {
                        val shareUri = ShareCardRenderer.render(context, photoUri, scenario)
                        ShareCardRenderer.share(context, shareUri, scenario)
                    },
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AltPrimary, contentColor = Color(0xFF16111F))
                ) { Text("Share this ALT life", fontWeight = FontWeight.Bold) }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        runCatching { ShareCardRenderer.saveToGallery(context, photoUri, scenario) }
                            .onSuccess { Toast.makeText(context, "Saved to Pictures/ALT", Toast.LENGTH_SHORT).show() }
                            .onFailure { Toast.makeText(context, "Could not save image", Toast.LENGTH_SHORT).show() }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(20.dp)
                ) { Text("Save 9:16 image", fontWeight = FontWeight.Bold) }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (!isExporting && activeTransformer == null) {
                            isExporting = true
                            exportProgress = null
                            completedVideoUri = null
                            runCatching { TimelineSceneRenderer.render(context, photoUri, scenario) }
                                .onSuccess { sceneUris ->
                                    activeTransformer = CinematicVideoExporter.export(
                                        context = context,
                                        imageUris = sceneUris,
                                        scenario = scenario,
                                        onCompleted = { videoUri ->
                                            isExporting = false
                                            exportProgress = 100
                                            activeTransformer = null
                                            completedVideoUri = videoUri
                                            Toast.makeText(context, "Video ready", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = {
                                            isExporting = false
                                            exportProgress = null
                                            activeTransformer = null
                                            completedVideoUri = null
                                            Toast.makeText(
                                                context,
                                                "Video export failed: " + (it.message ?: "unknown error"),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                                .onFailure {
                                    isExporting = false
                                    exportProgress = null
                                    activeTransformer = null
                                    completedVideoUri = null
                                    Toast.makeText(
                                        context,
                                        "Could not prepare video: " + (it.message ?: "unknown error"),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    },
                    enabled = !isExporting,
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

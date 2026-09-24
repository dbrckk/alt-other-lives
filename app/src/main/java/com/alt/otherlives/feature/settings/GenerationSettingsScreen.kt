package com.alt.otherlives.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alt.otherlives.core.designsystem.AltMuted
import com.alt.otherlives.core.generation.GenerationSettings

@Composable
fun GenerationSettingsScreen(
    settings: GenerationSettings,
    onBack: () -> Unit,
    onSave: (String, String) -> Unit,
    onTestConnection: (String) -> Unit,
    onClear: () -> Unit,
    statusMessage: String? = null,
    isTestingConnection: Boolean = false
) {
    var baseUrl by remember(settings.comfyUiBaseUrl) { mutableStateOf(settings.comfyUiBaseUrl) }
    var workflow by remember(settings.workflowJson) { mutableStateOf(settings.workflowJson) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        TextButton(onClick = onBack) { Text("‹ Back") }
        Text("AI generation", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            "Configure your own ComfyUI endpoint. Settings stay on this device.",
            color = AltMuted
        )
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = baseUrl,
            onValueChange = { baseUrl = it },
            label = { Text("ComfyUI base URL") },
            placeholder = { Text("https://your-comfyui.example") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = workflow,
            onValueChange = { workflow = it },
            label = { Text("Workflow API JSON") },
            supportingText = {
                Text("Required: __ALT_SOURCE_IMAGE__ and __ALT_PROMPT__. Optional: __ALT_SEED__ for consistent sequence seeds.")
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 12
        )
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = { onSave(baseUrl, workflow) },
            enabled = baseUrl.isNotBlank() && workflow.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Save ComfyUI settings") }

        Spacer(Modifier.height(10.dp))
        TextButton(
            onClick = { onTestConnection(baseUrl) },
            enabled = baseUrl.isNotBlank() && !isTestingConnection,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (isTestingConnection) "Testing connection…" else "Test connection") }

        statusMessage?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, color = AltMuted)
        }

        if (settings.isConfigured) {
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = onClear, modifier = Modifier.fillMaxWidth()) {
                Text("Clear AI settings")
            }
        }
    }
}

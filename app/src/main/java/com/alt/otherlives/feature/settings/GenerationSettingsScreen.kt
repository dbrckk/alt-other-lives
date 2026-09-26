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
import androidx.compose.material3.Checkbox
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
import com.alt.otherlives.core.designsystem.AltBackButton
import com.alt.otherlives.core.generation.GenerationSettings
import com.alt.otherlives.core.generation.GenerationSettingsValidation

@Composable
fun GenerationSettingsScreen(
    settings: GenerationSettings,
    onBack: () -> Unit,
    onSave: (String, String, Boolean) -> Unit,
    onTestConnection: (String) -> Unit,
    onClear: () -> Unit,
    statusMessage: String? = null,
    isTestingConnection: Boolean = false,
    isSavingSettings: Boolean = false,
    isClearingSettings: Boolean = false
) {
    var baseUrl by remember(settings.comfyUiBaseUrl) { mutableStateOf(settings.comfyUiBaseUrl) }
    var workflow by remember(settings.workflowJson) { mutableStateOf(settings.workflowJson) }
    var remotePhotoUploadConsent by remember(settings.remotePhotoUploadConsent) {
        mutableStateOf(settings.remotePhotoUploadConsent)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        AltBackButton(onClick = onBack)
        Text("Advanced AI setup", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            "Developer setup for a custom ComfyUI endpoint. Most users should not need to change this once ALT has a managed backend.",
            color = AltMuted
        )
        Spacer(Modifier.height(20.dp))

        if (settings.hasPersistedValues && !settings.isConfigured) {
            Text(
                "Saved AI settings need attention" +
                    (settings.validationError?.let { ": " + it } ?: ""),
                color = AltMuted
            )
            Spacer(Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = baseUrl,
            onValueChange = {
                if (it.length <= GenerationSettingsValidation.MAX_BASE_URL_CHARS) {
                    baseUrl = it
                }
            },
            label = { Text("ComfyUI base URL") },
            placeholder = { Text("https://your-comfyui.example") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = workflow,
            onValueChange = {
                if (it.length <= GenerationSettingsValidation.MAX_WORKFLOW_CHARS) {
                    workflow = it
                }
            },
            label = { Text("Workflow API JSON") },
            supportingText = {
                Text(
                    "Required: __ALT_SOURCE_IMAGE__ and __ALT_PROMPT__. Optional: __ALT_NEGATIVE_PROMPT__ and __ALT_SEED__. " +
                        "For workflows with multiple image outputs, name the preferred node ALT OUTPUT. " +
                        "${workflow.length}/${GenerationSettingsValidation.MAX_WORKFLOW_CHARS} chars"
                )
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 12
        )
        Spacer(Modifier.height(18.dp))
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Checkbox(
                checked = remotePhotoUploadConsent,
                onCheckedChange = { remotePhotoUploadConsent = it }
            )
            Text(
                "I understand that AI generation uploads my selected source photo to the configured remote ComfyUI server.",
                modifier = Modifier.padding(start = 8.dp),
                color = AltMuted
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "ALT keeps the original photo on-device unless you enable remote AI generation. The remote server is controlled by whoever operates the URL above.",
            color = AltMuted
        )
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = { onSave(baseUrl, workflow, remotePhotoUploadConsent) },
            enabled = baseUrl.isNotBlank() &&
                workflow.isNotBlank() &&
                !isTestingConnection &&
                !isSavingSettings &&
                !isClearingSettings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSavingSettings) "Saving settings…" else "Save ComfyUI settings")
        }

        Spacer(Modifier.height(10.dp))
        TextButton(
            onClick = { onTestConnection(baseUrl) },
            enabled = baseUrl.isNotBlank() &&
                !isTestingConnection &&
                !isSavingSettings &&
                !isClearingSettings,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (isTestingConnection) "Testing connection…" else "Test connection") }

        statusMessage?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, color = AltMuted)
        }

        if (settings.hasPersistedValues) {
            Spacer(Modifier.height(10.dp))
            TextButton(
                onClick = onClear,
                enabled = !isTestingConnection && !isSavingSettings && !isClearingSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isClearingSettings) "Clearing settings…" else "Clear AI settings")
            }
        }
    }
}

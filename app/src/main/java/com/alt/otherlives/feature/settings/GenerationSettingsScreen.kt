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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alt.otherlives.core.designsystem.AltMuted
import com.alt.otherlives.core.designsystem.AltBackButton
import com.alt.otherlives.core.generation.ComfyUiWorkflowTemplate
import com.alt.otherlives.core.generation.GenerationSettings
import com.alt.otherlives.core.generation.GenerationSettingsValidation
import com.alt.otherlives.core.generation.messageRes
import com.alt.otherlives.R

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

    val configuration = LocalConfiguration.current
    val fontScale = LocalDensity.current.fontScale
    val settingsLayout = SettingsResponsiveLayout.resolve(
        screenHeightDp = configuration.screenHeightDp,
        fontScale = fontScale
    )

    val continuityIssues = remember(workflow) {
        if (workflow.isBlank()) {
            emptyList()
        } else {
            runCatching {
                ComfyUiWorkflowTemplate.premiumContinuityIssues(workflow)
            }.getOrDefault(emptyList())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(settingsLayout.contentPaddingDp.dp)
    ) {
        AltBackButton(onClick = onBack)
        Text(
            stringResource(R.string.settings_title),
            fontSize = settingsLayout.titleSizeSp.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(R.string.settings_intro),
            color = AltMuted
        )
        Spacer(Modifier.height(settingsLayout.sectionSpacingDp.dp))

        if (settings.hasPersistedValues && !settings.isConfigured) {
            Text(
                stringResource(
                    R.string.settings_attention,
                    settings.validationIssue?.let {
                        ": " + stringResource(it.messageRes())
                    } ?: ""
                ),
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
            label = { Text(stringResource(R.string.settings_base_url)) },
            placeholder = { Text(stringResource(R.string.settings_base_url_placeholder)) },
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
            label = { Text(stringResource(R.string.settings_workflow_json)) },
            supportingText = {
                Text(
                    stringResource(
                        R.string.settings_workflow_help,
                        workflow.length,
                        GenerationSettingsValidation.MAX_WORKFLOW_CHARS
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = settingsLayout.workflowMinLines
        )
        if (continuityIssues.isNotEmpty()) {
            Text(
                stringResource(R.string.settings_continuity_check),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            continuityIssues.forEach { issue ->
                val message = when (issue) {
                    ComfyUiWorkflowTemplate.PremiumContinuityIssue.MISSING_SHARED_SEED ->
                        stringResource(R.string.settings_warning_seed)
                    ComfyUiWorkflowTemplate.PremiumContinuityIssue.MISSING_NEGATIVE_PROMPT ->
                        stringResource(R.string.settings_warning_negative)
                    ComfyUiWorkflowTemplate.PremiumContinuityIssue.AMBIGUOUS_IMAGE_OUTPUTS ->
                        stringResource(R.string.settings_warning_ambiguous_output)
                }
                Text(
                    "• $message",
                    color = AltMuted
                )
                Spacer(Modifier.height(4.dp))
            }
            Spacer(Modifier.height(10.dp))
        }

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
                stringResource(R.string.settings_upload_consent),
                modifier = Modifier.padding(start = 8.dp),
                color = AltMuted
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.settings_privacy_note),
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
            Text(
                if (isSavingSettings) {
                    stringResource(R.string.settings_saving)
                } else {
                    stringResource(R.string.settings_save)
                }
            )
        }

        Spacer(Modifier.height(10.dp))
        TextButton(
            onClick = { onTestConnection(baseUrl) },
            enabled = baseUrl.isNotBlank() &&
                !isTestingConnection &&
                !isSavingSettings &&
                !isClearingSettings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (isTestingConnection) {
                    stringResource(R.string.settings_testing)
                } else {
                    stringResource(R.string.settings_test)
                }
            )
        }

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
                Text(
                    if (isClearingSettings) {
                        stringResource(R.string.settings_clearing)
                    } else {
                        stringResource(R.string.settings_clear)
                    }
                )
            }
        }
    }
}

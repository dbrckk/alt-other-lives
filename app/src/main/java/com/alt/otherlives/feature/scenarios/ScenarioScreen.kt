package com.alt.otherlives.feature.scenarios

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alt.otherlives.core.designsystem.AltAccent
import com.alt.otherlives.core.designsystem.AltCard
import com.alt.otherlives.core.designsystem.AltBackButton
import com.alt.otherlives.core.designsystem.AltDimmed
import com.alt.otherlives.core.designsystem.AltMuted
import com.alt.otherlives.core.model.Scenario
import com.alt.otherlives.R

@Composable
fun ScenarioScreen(
    scenarios: List<Scenario>,
    onBack: () -> Unit,
    onSelect: (Scenario) -> Unit,
    isCreatingTimeline: Boolean = false
) {
    val configuration = LocalConfiguration.current
    val fontScale = LocalDensity.current.fontScale
    val scenarioLayout = ScenarioResponsiveLayout.resolve(
        screenHeightDp = configuration.screenHeightDp,
        fontScale = fontScale
    )
    val haptics = LocalHapticFeedback.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(top = scenarioLayout.topPaddingDp.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onBack,
                enabled = !isCreatingTimeline
            ) {
                Text(stringResource(R.string.common_back))
            }
            Column(Modifier.padding(start = 12.dp)) {
                Text(stringResource(R.string.scenario_choose_path), color = AltAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.scenario_what_if), fontSize = scenarioLayout.headingSizeSp.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        if (isCreatingTimeline) {
            Spacer(Modifier.height(10.dp))
            Column(
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    stringResource(R.string.scenario_creating_timeline),
                    color = AltMuted,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(R.string.scenario_pick_life),
            color = AltMuted,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(18.dp))
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(scenarioLayout.cardSpacingDp.dp)
        ) {
            itemsIndexed(
                items = scenarios,
                key = { _, scenario -> scenario.id }
            ) { scenarioIndex, scenario ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            enabled = !isCreatingTimeline,
                            role = Role.Button,
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSelect(scenario)
                            }
                        ),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = AltCard)
                ) {
                    Column(Modifier.padding(scenarioLayout.cardPaddingDp.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                (scenarioIndex + 1).toString().padStart(2, '0'),
                                color = AltAccent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                stringResource(R.string.scenario_chapter_story, scenario.chapters.size),
                                color = AltDimmed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(18.dp))
                        Text(
                            scenario.title,
                            fontSize = 23.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(9.dp))
                        Text(
                            scenario.subtitle,
                            color = AltMuted,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                        Spacer(Modifier.height(16.dp))
                        val firstChapterLabel = scenario.chapters.firstOrNull()?.label
                        val lastChapterLabel = scenario.chapters.lastOrNull()?.label
                        if (
                            firstChapterLabel != null &&
                            lastChapterLabel != null &&
                            firstChapterLabel != lastChapterLabel
                        ) {
                            Text(
                                "$firstChapterLabel  →  $lastChapterLabel",
                                color = AltDimmed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                        Text(
                            stringResource(R.string.scenario_explore_life),
                            color = AltAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

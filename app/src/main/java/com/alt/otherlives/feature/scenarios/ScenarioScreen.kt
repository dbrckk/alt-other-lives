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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alt.otherlives.core.designsystem.AltCard
import com.alt.otherlives.core.designsystem.AltBackButton
import com.alt.otherlives.core.designsystem.AltMuted
import com.alt.otherlives.core.model.Scenario

@Composable
fun ScenarioScreen(
    scenarios: List<Scenario>,
    onBack: () -> Unit,
    onSelect: (Scenario) -> Unit,
    isCreatingTimeline: Boolean = false
) {
    Column(modifier = Modifier.fillMaxSize().padding(top = 42.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onBack,
                enabled = !isCreatingTimeline
            ) {
                Text("Back")
            }
            Text("What if…", fontSize = 32.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 12.dp))
        }
        if (isCreatingTimeline) {
            Spacer(Modifier.height(10.dp))
            Text(
                "Creating timeline…",
                color = AltMuted,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(scenarios, key = { it.id }) { scenario ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            enabled = !isCreatingTimeline,
                            role = Role.Button,
                            onClick = { onSelect(scenario) }
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = AltCard)
                ) {
                    Column(Modifier.padding(22.dp)) {
                        Text(scenario.title, fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text(scenario.subtitle, color = AltMuted, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

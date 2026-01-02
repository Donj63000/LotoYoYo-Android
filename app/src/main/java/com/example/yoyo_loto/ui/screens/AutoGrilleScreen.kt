package com.example.yoyo_loto.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.yoyo_loto.model.AutoGrilleItemUiState
import com.example.yoyo_loto.model.AutoGrilleUiState
import com.example.yoyo_loto.ui.components.NeonButton
import com.example.yoyo_loto.ui.components.NeonCard
import com.example.yoyo_loto.ui.components.OutcomeToggle
import com.example.yoyo_loto.ui.components.StatsPanel
import com.example.yoyo_loto.ui.theme.NeonGreen
import com.example.yoyo_loto.ui.theme.NeonOrange
import com.example.yoyo_loto.ui.theme.NeonRed
import com.example.yoyo_loto.ui.theme.TextMuted

@Composable
fun AutoGrilleScreen(
    state: AutoGrilleUiState,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            NeonCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Auto-grilles",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        NeonButton(text = "Retour", onClick = onBack)
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Combos : ${state.combosLabel}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                    state.limitLabel?.let { limit ->
                        Text(
                            text = limit,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
        items(state.items, key = { it.id }) { item ->
            AutoGrilleCard(item = item)
        }
        item { Spacer(modifier = Modifier.size(24.dp)) }
    }
}

@Composable
private fun AutoGrilleCard(item: AutoGrilleItemUiState) {
    val expanded = rememberSaveable(item.id) { mutableStateOf(false) }
    NeonCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        glowColor = MaterialTheme.colorScheme.secondary
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Auto-grille #${item.id}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                NeonButton(
                    text = if (expanded.value) "Masquer" else "Details",
                    onClick = { expanded.value = !expanded.value },
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Cout : ${item.costLabel}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
                Text(
                    text = "Proba : ${item.probabilityLabel}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            ScenarioPreview(item.scenario)
            AnimatedVisibility(visible = expanded.value) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    ScenarioDetails(item.scenario)
                    Spacer(modifier = Modifier.size(8.dp))
                    StatsPanel(stats = item.stats)
                }
            }
        }
    }
}

@Composable
private fun ScenarioPreview(scenario: List<Int>) {
    val previewCount = minOf(12, scenario.size)
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        for (i in 0 until previewCount) {
            val outcome = scenario[i]
            OutcomeToggle(
                label = outcomeLabel(outcome),
                selected = true,
                accent = outcomeColor(outcome),
                onToggle = {},
                enabled = false
            )
        }
        if (scenario.size > previewCount) {
            Text(
                text = "+${scenario.size - previewCount}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}

@Composable
private fun ScenarioDetails(scenario: List<Int>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        scenario.forEachIndexed { idx, outcome ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "M${idx + 1}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    modifier = Modifier.width(40.dp)
                )
                OutcomeToggle(
                    label = outcomeLabel(outcome),
                    selected = true,
                    accent = outcomeColor(outcome),
                    onToggle = {},
                    enabled = false
                )
            }
        }
    }
}

private fun outcomeLabel(outcome: Int): String {
    return when (outcome) {
        0 -> "1"
        1 -> "N"
        else -> "2"
    }
}

private fun outcomeColor(outcome: Int): androidx.compose.ui.graphics.Color {
    return when (outcome) {
        0 -> NeonGreen
        1 -> NeonOrange
        else -> NeonRed
    }
}

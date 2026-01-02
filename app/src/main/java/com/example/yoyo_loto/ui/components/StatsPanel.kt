package com.example.yoyo_loto.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.yoyo_loto.model.GridStatsUiState
import com.example.yoyo_loto.ui.theme.TextMuted

@Composable
fun StatsPanel(stats: GridStatsUiState) {
    Column {
        StatRow(label = "Tickets", value = stats.tickets)
        StatRow(label = "Budget", value = stats.budget)
        StatRow(label = "Couverture", value = stats.coverage)
        StatRow(label = "Couvrir tous", value = stats.coverAll)
        Divider(color = TextMuted.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "Distribution",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )
        if (stats.distribution.isEmpty()) {
            Text(
                text = "Appuyez sur Calculer pour remplir les stats.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        } else {
            stats.distribution.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Divider(color = TextMuted.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))
        StatRow(label = "Au moins 1", value = stats.atLeast1)
        StatRow(label = "Au moins moitie", value = stats.atLeastHalf)
        StatRow(label = "100 %", value = stats.atLeastAll)
        Divider(color = TextMuted.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))
        val worst = if (stats.isApproxWorstCase) "~${stats.worstCase}" else stats.worstCase
        StatRow(label = "Scenarios manquants", value = stats.scenariosMissing)
        StatRow(label = "Pire cas", value = worst)
        StatRow(label = "Efficacite", value = stats.efficiency)
        StatRow(label = "Moyenne bons", value = stats.meanHits)
        StatRow(label = "Ecart-type", value = stats.stdHits)
        StatRow(label = "Configs", value = stats.configs)
        StatRow(label = "Forces", value = stats.forced)
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
}

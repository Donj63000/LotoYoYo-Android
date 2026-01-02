package com.example.yoyo_loto.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.yoyo_loto.model.AppUiState
import com.example.yoyo_loto.model.GridUiState
import com.example.yoyo_loto.ui.components.NeonButton
import com.example.yoyo_loto.ui.components.NeonCard
import com.example.yoyo_loto.ui.components.OutcomeToggle
import com.example.yoyo_loto.ui.components.StatsPanel
import com.example.yoyo_loto.ui.theme.NeonGreen
import com.example.yoyo_loto.ui.theme.NeonOrange
import com.example.yoyo_loto.ui.theme.NeonRed
import com.example.yoyo_loto.ui.theme.TextMuted
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun MainScreen(
    state: AppUiState,
    onMatchCountChange: (String) -> Unit,
    onAddGrid: () -> Unit,
    onResetAll: () -> Unit,
    onRemoveGrid: (String) -> Unit,
    onToggleSelection: (String, Int, Int) -> Unit,
    onOddsInputChange: (String, Int, Int, String) -> Unit,
    onSetUseOdds: (String, Boolean) -> Unit,
    onApplyOdds: (String) -> Unit,
    onCalculate: (String) -> Unit,
    onAutoGrille: (String) -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset des grilles") },
            text = { Text("Tu veux supprimer toutes les grilles ?") },
            confirmButton = {
                NeonButton(
                    text = "Oui",
                    onClick = {
                        showResetDialog = false
                        onResetAll()
                    },
                    color = NeonRed
                )
            },
            dismissButton = {
                NeonButton(
                    text = "Non",
                    onClick = { showResetDialog = false }
                )
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            MatchInputRow(
                matchCount = state.matchCountInput,
                onMatchCountChange = onMatchCountChange,
                onAddGrid = onAddGrid,
                onReset = { showResetDialog = true }
            )
        }
        if (state.grids.isEmpty()) {
            item {
                NeonCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Aucune grille pour l'instant.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Ajoutez une grille pour commencer.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
        items(state.grids, key = { it.id }) { grid ->
            GridCard(
                grid = grid,
                onRemove = { onRemoveGrid(grid.id) },
                onToggleSelection = { matchIndex, outcomeIndex ->
                    onToggleSelection(grid.id, matchIndex, outcomeIndex)
                },
                onOddsInputChange = { matchIndex, outcomeIndex, value ->
                    onOddsInputChange(grid.id, matchIndex, outcomeIndex, value)
                },
                onUseOddsChange = { enabled -> onSetUseOdds(grid.id, enabled) },
                onApplyOdds = { onApplyOdds(grid.id) },
                onCalculate = { onCalculate(grid.id) },
                onAutoGrille = { onAutoGrille(grid.id) }
            )
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MatchInputRow(
    matchCount: String,
    onMatchCountChange: (String) -> Unit,
    onAddGrid: () -> Unit,
    onReset: () -> Unit
) {
    NeonCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Creation de grille",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val fieldSurface = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                OutlinedTextField(
                    value = matchCount,
                    onValueChange = onMatchCountChange,
                    label = { Text("Matchs") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = TextMuted.copy(alpha = 0.5f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = TextMuted,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = fieldSurface,
                        unfocusedContainerColor = fieldSurface
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    NeonButton(text = "Reset", onClick = onReset, color = NeonRed)
                    NeonButton(text = "Ajouter", onClick = onAddGrid)
                }
            }
        }
    }
}

@Composable
private fun GridCard(
    grid: GridUiState,
    onRemove: () -> Unit,
    onToggleSelection: (Int, Int) -> Unit,
    onOddsInputChange: (Int, Int, String) -> Unit,
    onUseOddsChange: (Boolean) -> Unit,
    onApplyOdds: () -> Unit,
    onCalculate: () -> Unit,
    onAutoGrille: () -> Unit
) {
    NeonCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Grille #${grid.displayIndex} - ${grid.matchCount} match(s)",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                NeonButton(text = "Supprimer", onClick = onRemove, color = NeonRed)
            }
            Spacer(modifier = Modifier.height(12.dp))
            SectionTitle("Selections")
            SelectionHeaderRow()
            grid.matches.forEachIndexed { index, match ->
                SelectionRow(
                    matchIndex = index,
                    selections = match.selections,
                    onToggle = onToggleSelection
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = grid.useOdds,
                    onCheckedChange = onUseOddsChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = TextMuted,
                        checkmarkColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                Text(
                    text = "Cotes ?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (grid.useOdds) {
                Spacer(modifier = Modifier.height(8.dp))
                SectionTitle("Cotes (1 / N / 2)")
                OddsHeaderRow()
                grid.matches.forEachIndexed { index, match ->
                    OddsRow(
                        matchIndex = index,
                        oddsInput = match.oddsInput,
                        onOddsInputChange = onOddsInputChange
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    NeonButton(text = "Appliquer cotes", onClick = onApplyOdds)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("Statistiques")
            if (grid.isCalculating) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Calcul en cours...", color = TextMuted)
                }
            } else {
                StatsPanel(stats = grid.stats)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
            ) {
                NeonButton(text = "Calculer", onClick = onCalculate)
                NeonButton(text = "Auto-grille", onClick = onAutoGrille, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
private fun SelectionRow(
    matchIndex: Int,
    selections: List<Boolean>,
    onToggle: (Int, Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = "M${matchIndex + 1}",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            modifier = Modifier.width(40.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutcomeToggle(label = "1", selected = selections[0], accent = NeonGreen) {
                onToggle(matchIndex, 0)
            }
            OutcomeToggle(label = "N", selected = selections[1], accent = NeonOrange) {
                onToggle(matchIndex, 1)
            }
            OutcomeToggle(label = "2", selected = selections[2], accent = NeonRed) {
                onToggle(matchIndex, 2)
            }
        }
    }
}

@Composable
private fun SelectionHeaderRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Spacer(modifier = Modifier.width(40.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "1", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
            Text(text = "N", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
            Text(text = "2", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
        }
    }
}

@Composable
private fun OddsRow(
    matchIndex: Int,
    oddsInput: List<String>,
    onOddsInputChange: (Int, Int, String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = "M${matchIndex + 1}",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            modifier = Modifier.width(40.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val fieldSurface = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            repeat(3) { idx ->
                OutlinedTextField(
                    value = oddsInput.getOrElse(idx) { "" },
                    onValueChange = { onOddsInputChange(matchIndex, idx, it) },
                    singleLine = true,
                    modifier = Modifier.width(90.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = TextMuted.copy(alpha = 0.4f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = TextMuted,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = fieldSurface,
                        unfocusedContainerColor = fieldSurface
                    )
                )
            }
        }
    }
}

@Composable
private fun OddsHeaderRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Spacer(modifier = Modifier.width(40.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "1", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
            Text(text = "N", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
            Text(text = "2", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
        }
    }
}


package com.example.yoyo_loto.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.yoyo_loto.ui.components.NeonButton
import com.example.yoyo_loto.ui.components.NeonCard
import com.example.yoyo_loto.ui.theme.TextMuted

@Composable
fun HelpScreen(onBack: () -> Unit) {
    val sections = helpSections()
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            NeonCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                glowColor = MaterialTheme.colorScheme.tertiary
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Aide",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )
                        NeonButton(text = "Retour", onClick = onBack, color = MaterialTheme.colorScheme.tertiary)
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "LotoYoYo applique les regles FDJ Loto Foot (point de vente).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "Cette aide resume les regles prises en compte.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }
        }
        items(sections) { section ->
            NeonCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                glowColor = MaterialTheme.colorScheme.tertiary
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    section.lines.forEach { line ->
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.size(24.dp)) }
    }
}

private data class HelpSection(val title: String, val lines: List<String>)

private fun helpSections(): List<HelpSection> {
    return listOf(
        HelpSection(
            title = "Formules FDJ",
            lines = listOf(
                "Loto Foot propose 4 formules : 7, 8, 12, 15.",
                "Chaque grille est creee sur une formule fixe."
            )
        ),
        HelpSection(
            title = "Rencontres reelles",
            lines = listOf(
                "Si la liste est courte, les dernieres lignes sont neutralisees.",
                "LF7 peut avoir 6, LF8 peut avoir 7, LF12 peut avoir 11/10/9, LF15 peut avoir 14/13/12.",
                "Les lignes neutralisees ne comptent pas dans la mise ni dans les stats."
            )
        ),
        HelpSection(
            title = "Doubles et triples",
            lines = listOf(
                "Les doubles/triples sont limites par le tableau FDJ.",
                "Une combinaison non autorisee serait rejetee au terminal."
            )
        ),
        HelpSection(
            title = "Annulation avant prise de jeu",
            lines = listOf(
                "Une rencontre annulee avant la prise de jeu est ignoree.",
                "Pas besoin de cocher, elle est reputee gagnante."
            )
        ),
        HelpSection(
            title = "Resultat retenu",
            lines = listOf(
                "Le resultat pris en compte est celui du temps reglementaire.",
                "Prolongations et tirs au but ne comptent pas."
            )
        ),
        HelpSection(
            title = "Avertissement",
            lines = listOf(
                "Application non officielle FDJ.",
                "Ne calcule pas les gains (partage, rangs, etc.).",
                "Outil d aide a la preparation."
            )
        )
    )
}

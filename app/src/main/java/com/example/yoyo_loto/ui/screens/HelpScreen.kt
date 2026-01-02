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
                        text = "LotoYoYo est un outil pour creer des grilles optimisees.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "Logiciel dedie a Mr.MARION.",
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
            title = "Introduction",
            lines = listOf(
                "Chaque match a trois issues : 1 (domicile), N (nul), 2 (exterieur).",
                "Cochez une ou plusieurs issues pour couvrir plus de scenarios.",
                "Plus de selections = plus de tickets et plus de budget."
            )
        ),
        HelpSection(
            title = "Creer une grille",
            lines = listOf(
                "Indiquez le nombre de matchs puis tapez Ajouter.",
                "Choisissez les issues avec les boutons 1 / N / 2.",
                "Saisissez les cotes et lancez Appliquer cotes."
            )
        ),
        HelpSection(
            title = "Statistiques",
            lines = listOf(
                "Tickets : total des combinaisons issues de vos choix.",
                "Couverture : part des 3^M scenarios couverts.",
                "Couvrir tous : chance que le vrai scenario soit couvert.",
                "Distribution : proba de 0..M bons pronostics."
            )
        ),
        HelpSection(
            title = "Auto-grille",
            lines = listOf(
                "Auto-grille genere les meilleurs scenarios dans vos choix.",
                "Si les combinaisons sont enormes, la liste est limitee."
            )
        ),
        HelpSection(
            title = "Conseils",
            lines = listOf(
                "Gardez les multi-choix la ou c est utile.",
                "Verifiez les cotes pour des probabilites realistes.",
                "Utilisez les stats pour equilibrer budget et couverture."
            )
        ),
        HelpSection(
            title = "Limites",
            lines = listOf(
                "Beaucoup de matchs font exploser les scenarios (3^M).",
                "Le pire cas est exact seulement sur de petites grilles.",
                "Le nombre de scenarios grimpe vite (3^M).",
                "Les grilles tres grandes sont calculees avec des limites."
            )
        ),
        HelpSection(
            title = "A quoi ca sert",
            lines = listOf(
                "Composer des grilles Loto Foot rapidement.",
                "Comparer budget, couverture et probabilites.",
                "Generer des auto-grilles a partir des choix."
            )
        ),
        HelpSection(
            title = "Pour qui",
            lines = listOf(
                "Joueurs qui veulent structurer leurs pronostics.",
                "Groupes qui mutualisent un budget.",
                "Curieux qui veulent analyser leurs selections."
            )
        ),
        HelpSection(
            title = "Fonctions cle",
            lines = listOf(
                "Selections 1 / N / 2 par match.",
                "Cotes et probabilites integrees.",
                "Auto-grilles et statistiques detaillees."
            )
        )
    )
}

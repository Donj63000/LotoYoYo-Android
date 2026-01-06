package com.example.yoyo_loto.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import com.example.yoyo_loto.model.AppScreen
import com.example.yoyo_loto.ui.components.NeonBackdrop
import com.example.yoyo_loto.ui.components.NeonButton
import com.example.yoyo_loto.ui.screens.AutoGrilleScreen
import com.example.yoyo_loto.ui.screens.HelpScreen
import com.example.yoyo_loto.ui.screens.MainScreen
import com.example.yoyo_loto.ui.theme.YoYoLotoTheme
import com.example.yoyo_loto.ui.theme.NeonBlue
import com.example.yoyo_loto.ui.theme.NeonGreen
import com.example.yoyo_loto.ui.theme.NeonMagenta
import com.example.yoyo_loto.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YoYoLotoApp(viewModel: AppViewModel) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSaveDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.snackbarMessage) {
        val msg = state.snackbarMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    YoYoLotoTheme {
        NeonBackdrop {
            Scaffold(
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            AnimatedTitle()
                        },
                        navigationIcon = {
                            TextButton(onClick = viewModel::goHome) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Retour",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Retour",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        actions = {
                            SaveActionButton(onClick = { showSaveDialog = true })
                            TopBarAction(text = "Aide", onClick = viewModel::openHelp)
                        }
                    )
                }
            ) { padding ->
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                ) {
                    when (state.screen) {
                        AppScreen.Main -> MainScreen(
                            state = state,
                            onFormatChange = viewModel::setSelectedFormat,
                            onRealMatchCountChange = viewModel::setSelectedRealMatchCount,
                            onAddGrid = viewModel::addGrid,
                            onResetAll = viewModel::resetAllGrids,
                            onRemoveGrid = viewModel::removeGrid,
                            onToggleSelection = viewModel::toggleSelection,
                            onOddsInputChange = viewModel::updateOddsInput,
                            onSetUseOdds = viewModel::setUseOdds,
                            onApplyOdds = viewModel::applyOdds,
                            onCalculate = viewModel::calculateGrid,
                            onAutoGrille = viewModel::openAutoGrille
                        )
                        AppScreen.Help -> HelpScreen(onBack = viewModel::closeHelp)
                        AppScreen.AutoGrille -> {
                            state.autoGrilleState?.let { auto ->
                                AutoGrilleScreen(state = auto, onBack = viewModel::closeAutoGrille)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Sauvegarde") },
            text = { Text("Veux-tu sauvegarder l'etat des grilles ?") },
            confirmButton = {
                NeonButton(
                    text = "OUI",
                    onClick = {
                        showSaveDialog = false
                        viewModel.saveNow()
                    }
                )
            },
            dismissButton = {
                NeonButton(
                    text = "NON",
                    onClick = { showSaveDialog = false }
                )
            }
        )
    }
}

@Composable
private fun AnimatedTitle() {
    val transition = rememberInfiniteTransition(label = "title")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 6000
                0f at 0
                0.33f at 2000
                0.66f at 4000
                1f at 6000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "title-phase"
    )
    val colors = listOf(NeonBlue, NeonMagenta, NeonGreen, NeonBlue)
    val segment = when {
        phase < 0.33f -> 0
        phase < 0.66f -> 1
        else -> 2
    }
    val localT = when (segment) {
        0 -> phase / 0.33f
        1 -> (phase - 0.33f) / 0.33f
        else -> (phase - 0.66f) / 0.34f
    }.coerceIn(0f, 1f)
    val color = lerp(colors[segment], colors[segment + 1], localT)

    Text(
        text = "LotoYoYo",
        style = TextStyle(
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.4.sp,
            shadow = Shadow(
                color = color.copy(alpha = 0.7f),
                blurRadius = 20f
            )
        ),
        color = color
    )
}

@Composable
private fun TopBarAction(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SaveActionButton(onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
        modifier = Modifier.padding(end = 6.dp)
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Filled.Save,
                contentDescription = "Sauvegarder",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

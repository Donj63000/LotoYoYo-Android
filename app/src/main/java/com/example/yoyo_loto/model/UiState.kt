package com.example.yoyo_loto.model

import androidx.compose.runtime.Immutable
import com.example.yoyo_loto.core.GridFdjValidationResult
import com.example.yoyo_loto.core.LotoFootFormat
import com.example.yoyo_loto.core.MatchStatus

@Immutable
sealed class AppScreen {
    data object Main : AppScreen()
    data object Help : AppScreen()
    data object AutoGrille : AppScreen()
}

@Immutable
data class AppUiState(
    val screen: AppScreen = AppScreen.Main,
    val selectedFormat: LotoFootFormat = LotoFootFormat.LF7,
    val selectedRealMatchCount: Int = LotoFootFormat.LF7.nominalMatches,
    val grids: List<GridUiState> = emptyList(),
    val autoGrilleState: AutoGrilleUiState? = null,
    val snackbarMessage: String? = null
)

@Immutable
data class GridUiState(
    val id: String,
    val displayIndex: Int,
    val format: LotoFootFormat,
    val nominalMatchCount: Int,
    val realMatchCount: Int,
    val matches: List<MatchUiState>,
    val useOdds: Boolean = true,
    val stats: GridStatsUiState = GridStatsUiState.empty(),
    val isCalculating: Boolean = false,
    val fdjValidation: GridFdjValidationResult
)

@Immutable
data class MatchUiState(
    val selections: List<Boolean>,
    val oddsInput: List<String>,
    val oddsApplied: List<Double>,
    val probabilities: List<Double>,
    val status: MatchStatus = MatchStatus.ACTIVE
)

@Immutable
data class GridStatsUiState(
    val tickets: String,
    val budget: String,
    val coverage: String,
    val coverAll: String,
    val distribution: List<String>,
    val atLeast1: String,
    val atLeastHalf: String,
    val atLeastAll: String,
    val scenariosMissing: String,
    val worstCase: String,
    val efficiency: String,
    val meanHits: String,
    val stdHits: String,
    val configs: String,
    val forced: String,
    val isApproxWorstCase: Boolean
) {
    companion object {
        fun empty(): GridStatsUiState = GridStatsUiState(
            tickets = "--",
            budget = "--",
            coverage = "--",
            coverAll = "--",
            distribution = emptyList(),
            atLeast1 = "--",
            atLeastHalf = "--",
            atLeastAll = "--",
            scenariosMissing = "--",
            worstCase = "--",
            efficiency = "--",
            meanHits = "--",
            stdHits = "--",
            configs = "--",
            forced = "--",
            isApproxWorstCase = false
        )
    }
}

@Immutable
data class AutoGrilleUiState(
    val gridId: String,
    val matchCount: Int,
    val combosLabel: String,
    val limitLabel: String?,
    val items: List<AutoGrilleItemUiState>
)

@Immutable
data class AutoGrilleItemUiState(
    val id: Int,
    val scenario: List<Int>,
    val costLabel: String,
    val probabilityLabel: String,
    val stats: GridStatsUiState
)

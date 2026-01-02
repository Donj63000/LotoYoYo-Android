package com.example.yoyo_loto.model

data class SavedAppState(
    val matchCountInput: String,
    val nextGridIndex: Int,
    val grids: List<SavedGridState>
)

data class SavedGridState(
    val id: String,
    val displayIndex: Int,
    val matchCount: Int,
    val matches: List<SavedMatchState>,
    val useOdds: Boolean
)

data class SavedMatchState(
    val selections: List<Boolean>,
    val oddsInput: List<String>,
    val oddsApplied: List<Double>
)

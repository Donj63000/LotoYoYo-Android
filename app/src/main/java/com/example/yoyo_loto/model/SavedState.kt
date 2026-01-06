package com.example.yoyo_loto.model

data class SavedAppState(
    val selectedFormat: String,
    val selectedRealMatchCount: Int,
    val nextGridIndex: Int,
    val grids: List<SavedGridState>
)

data class SavedGridState(
    val id: String,
    val displayIndex: Int,
    val format: String,
    val realMatchCount: Int,
    val matches: List<SavedMatchState>,
    val useOdds: Boolean
)

data class SavedMatchState(
    val selections: List<Boolean>,
    val oddsInput: List<String>,
    val oddsApplied: List<Double>,
    val status: String
)

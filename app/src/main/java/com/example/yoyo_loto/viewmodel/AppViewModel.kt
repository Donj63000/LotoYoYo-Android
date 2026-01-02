package com.example.yoyo_loto.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.yoyo_loto.core.DEFAULT_MATCH_COUNT
import com.example.yoyo_loto.core.DEFAULT_ODDS
import com.example.yoyo_loto.core.MAX_AUTO_GRILLES
import com.example.yoyo_loto.core.MAX_EXACT_WORST_CASE_MATCHES
import com.example.yoyo_loto.core.MAX_MATCHES
import com.example.yoyo_loto.core.MAX_SCENARIO_SAMPLES
import com.example.yoyo_loto.core.MAX_TICKETS_ENUMERATION
import com.example.yoyo_loto.core.buildDistribution
import com.example.yoyo_loto.core.combBig
import com.example.yoyo_loto.core.intToScen
import com.example.yoyo_loto.core.kBestClosestScenarios
import com.example.yoyo_loto.core.logPow
import com.example.yoyo_loto.core.oddsToProb
import com.example.yoyo_loto.core.powBig
import com.example.yoyo_loto.core.powInt
import com.example.yoyo_loto.core.scenarioProbability
import com.example.yoyo_loto.core.computeStats
import com.example.yoyo_loto.data.AppStateRepository
import com.example.yoyo_loto.model.AppScreen
import com.example.yoyo_loto.model.AppUiState
import com.example.yoyo_loto.model.AutoGrilleItemUiState
import com.example.yoyo_loto.model.AutoGrilleUiState
import com.example.yoyo_loto.model.GridStatsUiState
import com.example.yoyo_loto.model.GridUiState
import com.example.yoyo_loto.model.MatchUiState
import com.example.yoyo_loto.model.SavedAppState
import com.example.yoyo_loto.model.SavedGridState
import com.example.yoyo_loto.model.SavedMatchState
import java.math.BigInteger
import java.util.Locale
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.min
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppStateRepository(application)

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private var nextGridIndex = 1
    private var saveJob: Job? = null


    init {
        viewModelScope.launch(Dispatchers.IO) {
            val saved = repository.loadState()
            if (saved != null) {
                val maxIndex = saved.grids.maxOfOrNull { it.displayIndex } ?: 0
                nextGridIndex = maxOf(1, maxOf(saved.nextGridIndex, maxIndex + 1))
                _uiState.update { it.copy(matchCountInput = saved.matchCountInput, grids = saved.grids.map(::toGridUiState)) }
            }
        }
    }

    fun updateMatchCountInput(value: String) {
        val filtered = value.filter { it.isDigit() }
        _uiState.update { it.copy(matchCountInput = filtered) }
        scheduleSave()
    }

    fun addGrid() {
        val raw = _uiState.value.matchCountInput.trim()
        val parsed = raw.toIntOrNull() ?: DEFAULT_MATCH_COUNT
        if (parsed <= 0) {
            showMessage("Le nombre de matchs doit etre > 0.")
            return
        }
        val clamped = min(parsed, MAX_MATCHES)
        if (clamped != parsed) {
            showMessage("Nombre de matchs limite a $MAX_MATCHES.")
        }
        val grid = GridUiState(
            id = "grid_${System.currentTimeMillis()}_${nextGridIndex}",
            displayIndex = nextGridIndex,
            matchCount = clamped,
            matches = List(clamped) { defaultMatch() },
            useOdds = true
        )
        nextGridIndex += 1
        _uiState.update { it.copy(grids = it.grids + grid) }
        scheduleSave()
    }

    fun removeGrid(gridId: String) {
        _uiState.update { it.copy(grids = it.grids.filterNot { grid -> grid.id == gridId }) }
        scheduleSave()
    }

    fun toggleSelection(gridId: String, matchIndex: Int, outcomeIndex: Int) {
        _uiState.update { state ->
            val updated = state.grids.map { grid ->
                if (grid.id != gridId) grid else updateMatch(grid, matchIndex) { match ->
                    val selections = match.selections.mapIndexed { idx, value ->
                        if (idx == outcomeIndex) !value else value
                    }
                    match.copy(selections = selections)
                }
            }
            state.copy(grids = updated)
        }
        scheduleSave()
    }

    fun updateOddsInput(gridId: String, matchIndex: Int, outcomeIndex: Int, value: String) {
        val sanitized = value.replace(',', '.').filter { it.isDigit() || it == '.' }
        _uiState.update { state ->
            val updated = state.grids.map { grid ->
                if (grid.id != gridId) grid else updateMatch(grid, matchIndex) { match ->
                    val odds = match.oddsInput.mapIndexed { idx, v ->
                        if (idx == outcomeIndex) sanitized else v
                    }
                    match.copy(oddsInput = odds)
                }
            }
            state.copy(grids = updated)
        }
        scheduleSave()
    }

    fun applyOdds(gridId: String) {
        val grid = _uiState.value.grids.firstOrNull { it.id == gridId } ?: return
        val updatedMatches = mutableListOf<MatchUiState>()
        for (match in grid.matches) {
            val parsed = match.oddsInput.map { parseOdd(it) }
            if (parsed.any { it == null }) {
                showMessage("Cote invalide.")
                return
            }
            val odds = parsed.map { it ?: 1.0 }
            if (odds.any { it <= 0.0 }) {
                showMessage("Les cotes doivent etre > 0.")
                return
            }
            val probs = oddsToProb(odds[0], odds[1], odds[2]).toList()
            updatedMatches.add(match.copy(oddsApplied = odds, probabilities = probs))
        }
        _uiState.update { state ->
            val updated = state.grids.map { g ->
                if (g.id != gridId) g else g.copy(matches = updatedMatches)
            }
            state.copy(grids = updated)
        }
        showMessage("Cotes appliquees.")
        scheduleSave()
    }

    fun calculateGrid(gridId: String) {
        setGridLoading(gridId, true)
        viewModelScope.launch(Dispatchers.Default) {
            val grid = _uiState.value.grids.firstOrNull { it.id == gridId } ?: return@launch
            val stats = computeStatsForGrid(grid)
            _uiState.update { state ->
                val updated = state.grids.map { g ->
                    if (g.id != gridId) g else g.copy(stats = stats, isCalculating = false)
                }
                state.copy(grids = updated)
            }
            scheduleSave()
        }
    }

    fun openAutoGrille(gridId: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val grid = _uiState.value.grids.firstOrNull { it.id == gridId } ?: return@launch
            val matchCount = grid.matchCount
            val allowedChoices = grid.matches.map { match ->
                match.selections.mapIndexedNotNull { idx, selected -> if (selected) idx else null }
            }
            if (allowedChoices.any { it.isEmpty() }) {
                showMessage("Selectionnez au moins une issue par match.")
                return@launch
            }
            val combosBig = countCombos(allowedChoices)
            val combosLabel = formatBig(combosBig)
            val maxK = MAX_AUTO_GRILLES
            val combosInt = bigIntToIntOrNull(combosBig)
            val k = if (combosInt == null) maxK else min(combosInt, maxK)
            val limited = combosInt == null || combosInt > maxK

            val probList = probabilitiesForGrid(grid).map { it.toDoubleArray() }.toTypedArray()
            val scenarios = kBestClosestScenarios(probList, allowedChoices, k)

            val items = scenarios.mapIndexed { index, sc ->
                val stats = computeStatsForScenario(matchCount, probList, sc.scenario)
                AutoGrilleItemUiState(
                    id = index + 1,
                    scenario = sc.scenario.toList(),
                    costLabel = format4(sc.cost),
                    probabilityLabel = formatPercent(exp(-sc.cost)),
                    stats = stats
                )
            }
            val limitLabel = if (limited) "Limite a $maxK" else null
            _uiState.update { state ->
                state.copy(
                    screen = AppScreen.AutoGrille,
                    autoGrilleState = AutoGrilleUiState(
                        gridId = gridId,
                        matchCount = matchCount,
                        combosLabel = combosLabel,
                        limitLabel = limitLabel,
                        items = items
                    )
                )
            }
        }
    }

    fun closeAutoGrille() {
        _uiState.update { it.copy(screen = AppScreen.Main, autoGrilleState = null) }
    }

    fun openHelp() {
        _uiState.update { it.copy(screen = AppScreen.Help) }
    }

    fun closeHelp() {
        _uiState.update { it.copy(screen = AppScreen.Main) }
    }

    fun goHome() {
        _uiState.update { it.copy(screen = AppScreen.Main, autoGrilleState = null) }
    }

    fun resetAllGrids() {
        nextGridIndex = 1
        _uiState.update { it.copy(screen = AppScreen.Main, autoGrilleState = null, grids = emptyList()) }
        scheduleSave()
    }

    fun setUseOdds(gridId: String, enabled: Boolean) {
        _uiState.update { state ->
            val updated = state.grids.map { grid ->
                if (grid.id != gridId) grid else grid.copy(useOdds = enabled)
            }
            state.copy(grids = updated)
        }
        scheduleSave()
    }

    fun clearMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun saveNow() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveState(toSavedState())
        }
        showMessage("Sauvegarde OK.")
    }

    private fun showMessage(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    private fun setGridLoading(gridId: String, loading: Boolean) {
        _uiState.update { state ->
            val updated = state.grids.map { g ->
                if (g.id != gridId) g else g.copy(isCalculating = loading)
            }
            state.copy(grids = updated)
        }
    }

    private fun defaultMatch(): MatchUiState {
        val odds = DEFAULT_ODDS.toList()
        val probs = oddsToProb(odds[0], odds[1], odds[2]).toList()
        return MatchUiState(
            selections = listOf(false, false, false),
            oddsInput = odds.map { format2(it) },
            oddsApplied = odds,
            probabilities = probs
        )
    }

    private fun updateMatch(grid: GridUiState, matchIndex: Int, transform: (MatchUiState) -> MatchUiState): GridUiState {
        val updatedMatches = grid.matches.mapIndexed { idx, match ->
            if (idx == matchIndex) transform(match) else match
        }
        return grid.copy(matches = updatedMatches)
    }

    private fun parseOdd(text: String): Double? {
        return text.replace(',', '.').toDoubleOrNull()
    }

    private fun computeStatsForGrid(grid: GridUiState): GridStatsUiState {
        val matchCount = grid.matchCount
        val selections = grid.matches.map { it.selections }
        val probList = probabilitiesForGrid(grid)

        var combos = BigInteger.ONE
        var logCombos = 0.0
        var forced = 0
        var hasZero = false

        for (sel in selections) {
            val count = sel.count { it }
            forced += count
            if (count == 0) {
                hasZero = true
                combos = BigInteger.ZERO
            } else if (!hasZero) {
                combos = combos.multiply(BigInteger.valueOf(count.toLong()))
                logCombos += ln(count.toDouble())
            }
        }

        val totalScenarios = powBig(3, matchCount)
        val coverageFraction = if (hasZero) 0.0 else exp(logCombos - logPow(3.0, matchCount))
        val efficiency = if (hasZero) 0.0 else exp(-logPow(3.0, matchCount))

        val pMatch = DoubleArray(matchCount)
        var pAll = 1.0
        for (i in 0 until matchCount) {
            var pSel = 0.0
            val probs = probList[i]
            if (selections[i][0]) pSel += probs[0]
            if (selections[i][1]) pSel += probs[1]
            if (selections[i][2]) pSel += probs[2]
            pMatch[i] = pSel
            pAll *= pSel
        }

        val dist = buildDistribution(pMatch)
        val distLines = dist.mapIndexed { idx, v -> "${idx} bon(s) : ${formatPercent(v)}" }

        val pAtLeast1 = 1.0 - dist[0]
        val half = (matchCount + 1) / 2
        var pAtLeastHalf = 0.0
        for (k in half..matchCount) {
            pAtLeastHalf += dist[k]
        }

        val stats = computeStats(dist)
        val scenariosMissing = if (combos > totalScenarios) BigInteger.ZERO else totalScenarios.subtract(combos)

        val totalCases = 3 * matchCount
        val freeCases = totalCases - forced
        val configs = powBig(2, freeCases)
        val forcedComb = combBig(totalCases, forced)

        val worstCaseResult = computeWorstCase(selections, matchCount)
        val worstLabel = worstCaseResult?.first?.toString() ?: "--"

        return GridStatsUiState(
            tickets = formatBig(combos),
            budget = "${formatBig(combos)} EUR",
            coverage = formatPercent(coverageFraction),
            coverAll = formatPercent(pAll),
            distribution = distLines,
            atLeast1 = formatPercent(pAtLeast1),
            atLeastHalf = formatPercent(pAtLeastHalf),
            atLeastAll = formatPercent(dist[matchCount]),
            scenariosMissing = formatBig(scenariosMissing),
            worstCase = worstLabel,
            efficiency = format4(efficiency),
            meanHits = format2(stats[0]),
            stdHits = format2(stats[1]),
            configs = formatBig(configs),
            forced = formatBig(forcedComb),
            isApproxWorstCase = worstCaseResult?.second == true
        )
    }

    private fun computeStatsForScenario(
        matchCount: Int,
        probList: Array<DoubleArray>,
        scenario: IntArray
    ): GridStatsUiState {
        val totalScenarios = powBig(3, matchCount)
        val coverageFraction = exp(-logPow(3.0, matchCount))
        val scenarioProb = scenarioProbability(probList, scenario)
        val pMatch = DoubleArray(matchCount) { idx -> probList[idx][scenario[idx]] }
        val dist = buildDistribution(pMatch)
        val distLines = dist.mapIndexed { idx, v -> "${idx} bon(s) : ${formatPercent(v)}" }
        val pAtLeast1 = 1.0 - dist[0]
        val half = (matchCount + 1) / 2
        var pAtLeastHalf = 0.0
        for (k in half..matchCount) {
            pAtLeastHalf += dist[k]
        }
        val stats = computeStats(dist)
        val totalCases = 3 * matchCount
        val configs = BigInteger.ONE
        val forcedComb = combBig(totalCases, matchCount)

        return GridStatsUiState(
            tickets = "1",
            budget = "1 EUR",
            coverage = formatPercent(coverageFraction),
            coverAll = formatPercent(scenarioProb),
            distribution = distLines,
            atLeast1 = formatPercent(pAtLeast1),
            atLeastHalf = formatPercent(pAtLeastHalf),
            atLeastAll = formatPercent(dist[matchCount]),
            scenariosMissing = formatBig(totalScenarios.subtract(BigInteger.ONE)),
            worstCase = "0",
            efficiency = format4(coverageFraction),
            meanHits = format2(stats[0]),
            stdHits = format2(stats[1]),
            configs = formatBig(configs),
            forced = formatBig(forcedComb),
            isApproxWorstCase = false
        )
    }

    private fun computeWorstCase(
        selections: List<List<Boolean>>,
        matchCount: Int
    ): Pair<Int, Boolean>? {
        if (matchCount > MAX_EXACT_WORST_CASE_MATCHES) return null
        val choices = selections.map { sel ->
            sel.mapIndexedNotNull { idx, picked -> if (picked) idx else null }.toIntArray()
        }
        if (choices.any { it.isEmpty() }) return 0 to false
        var combos = 1L
        for (arr in choices) {
            combos *= arr.size
            if (combos > MAX_TICKETS_ENUMERATION) return null
        }
        val combosInt = combos.toInt()
        val tickets = ArrayList<IntArray>(combosInt)
        val current = IntArray(matchCount)
        enumerateTickets(choices, 0, current, tickets)

        val totalScenarios = powInt(3, matchCount) ?: return null
        val shouldExact = combosInt <= 8000 && totalScenarios <= 100_000
        return if (shouldExact) {
            exactWorstCase(tickets, matchCount, totalScenarios) to false
        } else {
            approximateWorstCase(tickets, matchCount, totalScenarios) to true
        }
    }

    private fun exactWorstCase(tickets: List<IntArray>, matchCount: Int, totalScenarios: Int): Int {
        var worst = matchCount
        for (code in 0 until totalScenarios) {
            val scen = intToScen(code, matchCount)
            var bestLocal = 0
            for (ticket in tickets) {
                var hits = 0
                for (i in 0 until matchCount) {
                    if (ticket[i] == scen[i]) hits++
                }
                if (hits > bestLocal) bestLocal = hits
                if (bestLocal == matchCount) break
            }
            if (bestLocal < worst) worst = bestLocal
            if (worst == 0) break
        }
        return worst
    }

    private fun approximateWorstCase(tickets: List<IntArray>, matchCount: Int, totalScenarios: Int): Int {
        var worst = matchCount
        val samples = min(MAX_SCENARIO_SAMPLES, totalScenarios)
        repeat(samples) {
            val code = Random.nextInt(totalScenarios)
            val scen = intToScen(code, matchCount)
            var bestLocal = 0
            for (ticket in tickets) {
                var hits = 0
                for (i in 0 until matchCount) {
                    if (ticket[i] == scen[i]) hits++
                }
                if (hits > bestLocal) bestLocal = hits
                if (bestLocal == matchCount) break
            }
            if (bestLocal < worst) worst = bestLocal
            if (worst == 0) return@repeat
        }
        return worst
    }

    private fun enumerateTickets(
        choices: List<IntArray>,
        idx: Int,
        current: IntArray,
        out: MutableList<IntArray>
    ) {
        if (idx >= choices.size) {
            out.add(current.clone())
            return
        }
        for (value in choices[idx]) {
            current[idx] = value
            enumerateTickets(choices, idx + 1, current, out)
        }
    }

    private fun formatPercent(value: Double): String {
        if (value.isNaN() || value.isInfinite()) {
            return "0.00 %"
        }
        return "${format2(value * 100)} %"
    }

    private fun format2(value: Double): String {
        return String.format(Locale.US, "%.2f", value)
    }

    private fun format4(value: Double): String {
        return String.format(Locale.US, "%.4f", value)
    }

    private fun formatBig(value: BigInteger): String {
        val digits = value.toString().length
        return if (digits <= 9) value.toString() else com.example.yoyo_loto.core.bigIntToSci(value, 6)
    }

    private fun countCombos(allowedChoices: List<List<Int>>): BigInteger {
        var combos = BigInteger.ONE
        for (choices in allowedChoices) {
            combos = combos.multiply(BigInteger.valueOf(choices.size.toLong()))
        }
        return combos
    }

    private fun toGridUiState(saved: SavedGridState): GridUiState {
        val matches = saved.matches.map { match ->
            val oddsApplied = if (match.oddsApplied.size == 3) match.oddsApplied else DEFAULT_ODDS.toList()
            val probs = oddsToProb(oddsApplied[0], oddsApplied[1], oddsApplied[2]).toList()
            val selections = when (match.selections.size) {
                3 -> match.selections
                else -> listOf(false, false, false)
            }
            val oddsInput = if (match.oddsInput.size == 3) match.oddsInput else oddsApplied.map { format2(it) }
            MatchUiState(
                selections = selections,
                oddsInput = oddsInput,
                oddsApplied = oddsApplied,
                probabilities = probs
            )
        }
        return GridUiState(
            id = saved.id,
            displayIndex = saved.displayIndex,
            matchCount = saved.matchCount,
            matches = matches,
            useOdds = saved.useOdds
        )
    }

    private fun toSavedState(): SavedAppState {
        val state = _uiState.value
        val grids = state.grids.map { grid ->
            SavedGridState(
                id = grid.id,
                displayIndex = grid.displayIndex,
                matchCount = grid.matchCount,
                matches = grid.matches.map { match ->
                    SavedMatchState(
                        selections = match.selections,
                        oddsInput = match.oddsInput,
                        oddsApplied = match.oddsApplied
                    )
                },
                useOdds = grid.useOdds
            )
        }
        return SavedAppState(
            matchCountInput = state.matchCountInput,
            nextGridIndex = nextGridIndex,
            grids = grids
        )
    }

    private fun probabilitiesForGrid(grid: GridUiState): List<List<Double>> {
        if (grid.useOdds) return grid.matches.map { it.probabilities }
        val uniform = listOf(1.0 / 3.0, 1.0 / 3.0, 1.0 / 3.0)
        return List(grid.matches.size) { uniform }
    }

    private fun scheduleSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch(Dispatchers.IO) {
            delay(300)
            repository.saveState(toSavedState())
        }
    }

    private fun bigIntToIntOrNull(value: BigInteger): Int? {
        return if (value > BigInteger.valueOf(Int.MAX_VALUE.toLong())) null else value.toInt()
    }
}

package com.example.yoyo_loto.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.yoyo_loto.core.DEFAULT_MATCH_COUNT
import com.example.yoyo_loto.core.DEFAULT_ODDS
import com.example.yoyo_loto.core.LotoFootFormat
import com.example.yoyo_loto.core.MatchStatus
import com.example.yoyo_loto.model.SavedAppState
import com.example.yoyo_loto.model.SavedGridState
import com.example.yoyo_loto.model.SavedMatchState
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "yoyo_loto_state")

class AppStateRepository(private val context: Context) {
    private val stateKey = stringPreferencesKey("state_json")

    suspend fun loadState(): SavedAppState? {
        val prefs = context.dataStore.data.first()
        val json = prefs[stateKey] ?: return null
        return parseState(json)
    }

    suspend fun saveState(state: SavedAppState) {
        val json = encodeState(state)
        context.dataStore.edit { prefs ->
            prefs[stateKey] = json
        }
    }

    private fun encodeState(state: SavedAppState): String {
        val root = JSONObject()
        root.put("selectedFormat", state.selectedFormat)
        root.put("selectedRealMatchCount", state.selectedRealMatchCount)
        root.put("nextGridIndex", state.nextGridIndex)
        val gridsArray = JSONArray()
        for (grid in state.grids) {
            val gridObj = JSONObject()
            gridObj.put("id", grid.id)
            gridObj.put("displayIndex", grid.displayIndex)
            gridObj.put("format", grid.format)
            gridObj.put("realMatchCount", grid.realMatchCount)
            gridObj.put("useOdds", grid.useOdds)
            val matchesArray = JSONArray()
            for (match in grid.matches) {
                val matchObj = JSONObject()
                matchObj.put("selections", JSONArray(match.selections))
                matchObj.put("oddsInput", JSONArray(match.oddsInput))
                matchObj.put("oddsApplied", JSONArray(match.oddsApplied))
                matchObj.put("status", match.status)
                matchesArray.put(matchObj)
            }
            gridObj.put("matches", matchesArray)
            gridsArray.put(gridObj)
        }
        root.put("grids", gridsArray)
        return root.toString()
    }

    private fun parseState(json: String): SavedAppState? {
        return try {
            val root = JSONObject(json)
            val legacyMatchCountInput = root.optString("matchCountInput", DEFAULT_MATCH_COUNT.toString())
            val legacyMatchCount = legacyMatchCountInput.toIntOrNull() ?: DEFAULT_MATCH_COUNT
            val selectedFormat = LotoFootFormat.fromSerialized(root.optString("selectedFormat", ""))
                ?: LotoFootFormat.fromMatchCount(legacyMatchCount)
                ?: LotoFootFormat.LF7
            val selectedRealMatchCount = selectedFormat.normalizeRealMatches(
                root.optInt("selectedRealMatchCount", selectedFormat.nominalMatches)
            )
            val nextGridIndex = root.optInt("nextGridIndex", 1)
            val gridsArray = root.optJSONArray("grids") ?: JSONArray()
            val grids = mutableListOf<SavedGridState>()
            for (i in 0 until gridsArray.length()) {
                val gridObj = gridsArray.optJSONObject(i) ?: continue
                val id = gridObj.optString("id", "grid_$i")
                val displayIndex = gridObj.optInt("displayIndex", i + 1)
                val format = LotoFootFormat.fromSerialized(gridObj.optString("format", ""))
                    ?: LotoFootFormat.fromMatchCount(gridObj.optInt("matchCount", DEFAULT_MATCH_COUNT))
                    ?: LotoFootFormat.LF7
                val realMatchCount = format.normalizeRealMatches(
                    gridObj.optInt("realMatchCount", format.nominalMatches)
                )
                val useOdds = gridObj.optBoolean("useOdds", true)
                val matchesArray = gridObj.optJSONArray("matches") ?: JSONArray()
                val matches = mutableListOf<SavedMatchState>()
                for (m in 0 until matchesArray.length()) {
                    val matchObj = matchesArray.optJSONObject(m) ?: continue
                    val selections = toBooleanList(matchObj.optJSONArray("selections"), 3, false)
                    val oddsInput = toStringList(matchObj.optJSONArray("oddsInput"), DEFAULT_ODDS.map { it.toString() })
                    val oddsApplied = toDoubleList(matchObj.optJSONArray("oddsApplied"), DEFAULT_ODDS.toList())
                    val status = parseStatus(matchObj.optString("status", MatchStatus.ACTIVE.name))
                    matches.add(SavedMatchState(selections, oddsInput, oddsApplied, status.name))
                }
                val normalizedMatches = normalizeMatches(format, realMatchCount, matches)
                grids.add(
                    SavedGridState(
                        id = id,
                        displayIndex = displayIndex,
                        format = format.name,
                        realMatchCount = realMatchCount,
                        matches = normalizedMatches,
                        useOdds = useOdds
                    )
                )
            }
            SavedAppState(selectedFormat.name, selectedRealMatchCount, nextGridIndex, grids)
        } catch (_: Exception) {
            null
        }
    }

    private fun normalizeMatches(
        format: LotoFootFormat,
        realMatchCount: Int,
        matches: List<SavedMatchState>
    ): List<SavedMatchState> {
        val matchCount = format.nominalMatches
        val normalized = matches.toMutableList()
        while (normalized.size < matchCount) {
            normalized.add(
                SavedMatchState(
                    selections = listOf(false, false, false),
                    oddsInput = DEFAULT_ODDS.map { it.toString() },
                    oddsApplied = DEFAULT_ODDS.toList(),
                    status = MatchStatus.ACTIVE.name
                )
            )
        }
        return normalized.take(matchCount).mapIndexed { index, match ->
            val originalStatus = parseStatus(match.status)
            val adjustedStatus = when {
                index >= realMatchCount -> MatchStatus.NEUTRALIZED
                originalStatus == MatchStatus.NEUTRALIZED -> MatchStatus.ACTIVE
                else -> originalStatus
            }
            match.copy(status = adjustedStatus.name)
        }
    }

    private fun toBooleanList(array: JSONArray?, size: Int, default: Boolean): List<Boolean> {
        if (array == null) return List(size) { default }
        return List(size) { idx -> array.optBoolean(idx, default) }
    }

    private fun toStringList(array: JSONArray?, fallback: List<String>): List<String> {
        if (array == null) return fallback
        val out = mutableListOf<String>()
        for (i in 0 until array.length()) {
            out.add(array.optString(i, fallback.getOrElse(i) { "" }))
        }
        return if (out.isEmpty()) fallback else out
    }

    private fun toDoubleList(array: JSONArray?, fallback: List<Double>): List<Double> {
        if (array == null) return fallback
        val out = mutableListOf<Double>()
        for (i in 0 until array.length()) {
            out.add(array.optDouble(i, fallback.getOrElse(i) { 1.0 }))
        }
        return if (out.isEmpty()) fallback else out
    }

    private fun parseStatus(value: String?): MatchStatus {
        if (value.isNullOrBlank()) return MatchStatus.ACTIVE
        return MatchStatus.entries.firstOrNull { it.name == value } ?: MatchStatus.ACTIVE
    }
}

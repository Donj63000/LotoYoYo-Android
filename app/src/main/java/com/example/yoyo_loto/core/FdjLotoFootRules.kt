package com.example.yoyo_loto.core

import java.math.BigInteger

enum class MatchStatus {
    ACTIVE,
    NEUTRALIZED,
    CANCELLED_BEFORE_BET
}

enum class LotoFootFormat(
    val label: String,
    val nominalMatches: Int,
    val allowedRealMatches: List<Int>,
    private val maxTriplesByDoubles: List<Int>
) {
    LF7(
        label = "Loto Foot 7",
        nominalMatches = 7,
        allowedRealMatches = listOf(7, 6),
        maxTriplesByDoubles = listOf(3, 2, 1, 1, 0)
    ),
    LF8(
        label = "Loto Foot 8",
        nominalMatches = 8,
        allowedRealMatches = listOf(8, 7),
        maxTriplesByDoubles = listOf(3, 2, 2, 1, 0, 0)
    ),
    LF12(
        label = "Loto Foot 12",
        nominalMatches = 12,
        allowedRealMatches = listOf(12, 11, 10, 9),
        maxTriplesByDoubles = listOf(5, 4, 4, 3, 2, 2, 1, 0, 0)
    ),
    LF15(
        label = "Loto Foot 15",
        nominalMatches = 15,
        allowedRealMatches = listOf(15, 14, 13, 12),
        maxTriplesByDoubles = listOf(6, 5, 4, 4, 3, 2, 2, 1, 1, 0)
    );

    fun isAllowedCombo(doubles: Int, triples: Int): Boolean {
        if (doubles < 0 || triples < 0) return false
        if (doubles >= maxTriplesByDoubles.size) return false
        return triples <= maxTriplesByDoubles[doubles]
    }

    fun normalizeRealMatches(realMatches: Int): Int {
        return if (allowedRealMatches.contains(realMatches)) realMatches else nominalMatches
    }

    companion object {
        fun fromSerialized(value: String?): LotoFootFormat? {
            if (value.isNullOrBlank()) return null
            return entries.firstOrNull { it.name == value }
        }

        fun fromMatchCount(matchCount: Int): LotoFootFormat? {
            return entries.firstOrNull { it.nominalMatches == matchCount }
        }
    }
}

data class GridFdjValidationResult(
    val isOk: Boolean,
    val errors: List<String>,
    val format: LotoFootFormat,
    val nominalMatches: Int,
    val realMatches: Int,
    val neutralizedCount: Int,
    val doubles: Int,
    val triples: Int,
    val stake: BigInteger,
    val stakeReason: String? = null
)

data class FdjMatchSelection(
    val selections: List<Boolean>,
    val status: MatchStatus
)

fun validateFdjGrid(
    format: LotoFootFormat,
    realMatches: Int,
    matches: List<FdjMatchSelection>
): GridFdjValidationResult {
    val errors = mutableListOf<String>()
    val nominal = format.nominalMatches
    val normalizedReal = format.normalizeRealMatches(realMatches)

    if (realMatches != normalizedReal) {
        errors.add("Rencontres reelles non autorisees pour ${format.label}.")
    }
    if (matches.size != nominal) {
        errors.add("Nombre de lignes invalide (attendu $nominal).")
    }

    val neutralizedCount = matches.count { it.status == MatchStatus.NEUTRALIZED }
    val activeMatches = matches.filter { it.status == MatchStatus.ACTIVE }
    if (activeMatches.isEmpty()) {
        errors.add("Aucune rencontre active.")
    }

    var doubles = 0
    var triples = 0
    var hasEmptySelection = false
    for (match in activeMatches) {
        val count = match.selections.count { it }
        when (count) {
            0 -> hasEmptySelection = true
            2 -> doubles += 1
            3 -> triples += 1
        }
    }
    if (hasEmptySelection) {
        errors.add("Au moins une issue par match actif.")
    }
    if (!format.isAllowedCombo(doubles, triples)) {
        errors.add("Combinaison doubles/triples non autorisee.")
    }

    val isOk = errors.isEmpty()
    val stake = if (isOk) {
        powBig(2, doubles).multiply(powBig(3, triples))
    } else {
        BigInteger.ZERO
    }

    return GridFdjValidationResult(
        isOk = isOk,
        errors = errors,
        format = format,
        nominalMatches = nominal,
        realMatches = normalizedReal,
        neutralizedCount = neutralizedCount,
        doubles = doubles,
        triples = triples,
        stake = stake,
        stakeReason = errors.firstOrNull()
    )
}

package com.example.yoyo_loto.core

import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FdjLotoFootRulesTest {
    @Test
    fun lf7_allows_and_rejects_expected_combos() {
        val okMatches = buildMatches(LotoFootFormat.LF7, doubles = 0, triples = 3)
        val ok = validateFdjGrid(LotoFootFormat.LF7, 7, okMatches)
        assertTrue(ok.isOk)
        assertEquals(BigInteger.valueOf(27L), ok.stake)

        val badMatches = buildMatches(LotoFootFormat.LF7, doubles = 1, triples = 3)
        val bad = validateFdjGrid(LotoFootFormat.LF7, 7, badMatches)
        assertFalse(bad.isOk)
    }

    @Test
    fun lf8_allows_and_rejects_expected_combos() {
        val okMatches = buildMatches(LotoFootFormat.LF8, doubles = 2, triples = 2)
        val ok = validateFdjGrid(LotoFootFormat.LF8, 8, okMatches)
        assertTrue(ok.isOk)
        assertEquals(BigInteger.valueOf(36L), ok.stake)

        val badMatches = buildMatches(LotoFootFormat.LF8, doubles = 2, triples = 3)
        val bad = validateFdjGrid(LotoFootFormat.LF8, 8, badMatches)
        assertFalse(bad.isOk)
    }

    @Test
    fun lf12_allows_and_rejects_expected_combos() {
        val okMatches = buildMatches(LotoFootFormat.LF12, doubles = 8, triples = 0)
        val ok = validateFdjGrid(LotoFootFormat.LF12, 12, okMatches)
        assertTrue(ok.isOk)
        assertEquals(BigInteger.valueOf(256L), ok.stake)

        val badMatches = buildMatches(LotoFootFormat.LF12, doubles = 6, triples = 2)
        val bad = validateFdjGrid(LotoFootFormat.LF12, 12, badMatches)
        assertFalse(bad.isOk)
    }

    @Test
    fun lf15_allows_and_rejects_expected_combos() {
        val okMatches = buildMatches(LotoFootFormat.LF15, doubles = 8, triples = 1)
        val ok = validateFdjGrid(LotoFootFormat.LF15, 15, okMatches)
        assertTrue(ok.isOk)
        assertEquals(BigInteger.valueOf(768L), ok.stake)

        val badMatches = buildMatches(LotoFootFormat.LF15, doubles = 8, triples = 2)
        val bad = validateFdjGrid(LotoFootFormat.LF15, 15, badMatches)
        assertFalse(bad.isOk)
    }

    @Test
    fun neutralized_lines_do_not_affect_stake_or_validation() {
        val format = LotoFootFormat.LF12
        val matches = buildNeutralizedMatches(format, realMatches = 10)
        val result = validateFdjGrid(format, 10, matches)
        assertTrue(result.isOk)
        assertEquals(2, result.neutralizedCount)
        assertEquals(BigInteger.ONE, result.stake)
    }

    @Test
    fun cancelled_before_bet_does_not_require_selection() {
        val format = LotoFootFormat.LF7
        val matches = mutableListOf<FdjMatchSelection>()
        repeat(6) { matches.add(singleActive()) }
        matches.add(FdjMatchSelection(listOf(false, false, false), MatchStatus.CANCELLED_BEFORE_BET))

        val result = validateFdjGrid(format, 7, matches)
        assertTrue(result.isOk)
        assertEquals(BigInteger.ONE, result.stake)
    }

    private fun buildMatches(
        format: LotoFootFormat,
        doubles: Int,
        triples: Int
    ): List<FdjMatchSelection> {
        val total = format.nominalMatches
        val singles = total - doubles - triples
        require(singles >= 0) { "Invalid test setup: singles < 0" }
        val matches = mutableListOf<FdjMatchSelection>()
        repeat(triples) { matches.add(tripleActive()) }
        repeat(doubles) { matches.add(doubleActive()) }
        repeat(singles) { matches.add(singleActive()) }
        return matches
    }

    private fun buildNeutralizedMatches(
        format: LotoFootFormat,
        realMatches: Int
    ): List<FdjMatchSelection> {
        val matches = mutableListOf<FdjMatchSelection>()
        repeat(realMatches) { matches.add(singleActive()) }
        repeat(format.nominalMatches - realMatches) {
            matches.add(FdjMatchSelection(listOf(false, false, false), MatchStatus.NEUTRALIZED))
        }
        return matches
    }

    private fun singleActive(): FdjMatchSelection {
        return FdjMatchSelection(listOf(true, false, false), MatchStatus.ACTIVE)
    }

    private fun doubleActive(): FdjMatchSelection {
        return FdjMatchSelection(listOf(true, true, false), MatchStatus.ACTIVE)
    }

    private fun tripleActive(): FdjMatchSelection {
        return FdjMatchSelection(listOf(true, true, true), MatchStatus.ACTIVE)
    }
}

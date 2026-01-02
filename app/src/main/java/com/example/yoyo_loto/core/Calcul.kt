package com.example.yoyo_loto.core

import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt

const val MAX_EXHAUSTIVE_MATCHES = 15
const val DEFAULT_SAMPLE_SIZE = 200_000

fun scenToInt(scen: IntArray): Int {
    var v = 0
    for (x in scen) {
        v = 3 * v + x
    }
    return v
}

fun intToScen(code: Int, m: Int): IntArray {
    var c = code
    val out = IntArray(m)
    for (i in m - 1 downTo 0) {
        val r = c % 3
        c /= 3
        out[i] = r
    }
    return out
}

fun ticketToInt(ticket: IntArray): Int = scenToInt(ticket)

fun oddsToProb(c1: Double, cN: Double, c2: Double): DoubleArray {
    val inv1 = 1.0 / c1
    val invN = 1.0 / cN
    val inv2 = 1.0 / c2
    val s = inv1 + invN + inv2
    return doubleArrayOf(inv1 / s, invN / s, inv2 / s)
}

fun buildDistribution(pMatch: DoubleArray): DoubleArray {
    val m = pMatch.size
    var dist = DoubleArray(m + 1)
    dist[0] = 1.0
    for (i in 0 until m) {
        val pm = pMatch[i]
        val newDist = DoubleArray(m + 1)
        for (k in 0..m) {
            val v = dist[k]
            if (v > 0.0) {
                newDist[k] += v * (1.0 - pm)
                if (k + 1 <= m) {
                    newDist[k + 1] += v * pm
                }
            }
        }
        dist = newDist
    }
    return dist
}

fun computeStats(dist: DoubleArray): DoubleArray {
    val m = dist.size - 1
    var e = 0.0
    var e2 = 0.0
    for (k in 0..m) {
        e += k * dist[k]
        e2 += (k * k) * dist[k]
    }
    var variance = e2 - e * e
    if (variance < 0.0) variance = 0.0
    val std = sqrt(variance)
    return doubleArrayOf(e, std)
}

fun scenarioLogProbability(probList: Array<DoubleArray>, scenario: IntArray): Double {
    var logP = 0.0
    for (i in scenario.indices) {
        logP += ln(probList[i][scenario[i]])
    }
    return logP
}

fun scenarioProbability(probList: Array<DoubleArray>, scenario: IntArray): Double {
    return exp(scenarioLogProbability(probList, scenario))
}

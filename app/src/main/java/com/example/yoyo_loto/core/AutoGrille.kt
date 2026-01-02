package com.example.yoyo_loto.core

import java.util.PriorityQueue
import kotlin.math.ln

data class ScenarioCost(val scenario: IntArray, val cost: Double)

private data class IssueProb(val issue: Int, val prob: Double)

private data class HeapItem(val cost: Double, val idx: Int, val partial: IntArray) : Comparable<HeapItem> {
    override fun compareTo(other: HeapItem): Int = cost.compareTo(other.cost)
}

private data class HeapItemClosest(
    val dist: Int,
    val cost: Double,
    val idx: Int,
    val partial: IntArray
) : Comparable<HeapItemClosest> {
    override fun compareTo(other: HeapItemClosest): Int {
        val cmp = dist.compareTo(other.dist)
        if (cmp != 0) return cmp
        return cost.compareTo(other.cost)
    }
}

fun kBestScenariosWithCost(probList: Array<DoubleArray>, k: Int): List<ScenarioCost> {
    if (k <= 0) return emptyList()
    val m = probList.size

    val order = ArrayList<List<IssueProb>>(m)
    val costs = ArrayList<DoubleArray>(m)
    val bestSuffixCost = DoubleArray(m + 1)

    for (p in probList) {
        val row = ArrayList<IssueProb>(3)
        for (issue in 0..2) {
            row.add(IssueProb(issue, p[issue]))
        }
        row.sortByDescending { it.prob }
        order.add(row)

        val costRow = DoubleArray(3)
        for (i in 0..2) {
            costRow[i] = -ln(row[i].prob)
        }
        costs.add(costRow)
    }

    for (i in m - 1 downTo 0) {
        bestSuffixCost[i] = bestSuffixCost[i + 1] + costs[i][0]
    }

    val heap = PriorityQueue<HeapItem>()
    heap.add(HeapItem(0.0, 0, IntArray(0)))

    val results = ArrayList<ScenarioCost>(k)
    var worstKeptCost = Double.POSITIVE_INFINITY

    while (heap.isNotEmpty() && results.size < k) {
        val top = heap.poll()
        val currentCost = top.cost
        val idx = top.idx
        val partial = top.partial

        if (currentCost + bestSuffixCost[idx] > worstKeptCost) {
            break
        }
        if (idx == m) {
            results.add(ScenarioCost(partial, currentCost))
            if (results.size == k) {
                worstKeptCost = currentCost
            }
            continue
        }

        val costRow = costs[idx]
        val row = order[idx]
        for (rank in 0..2) {
            val newCost = currentCost + costRow[rank]
            val bound = newCost + bestSuffixCost[idx + 1]
            if (bound > worstKeptCost) continue

            val newIssue = row[rank].issue
            val newPartial = partial.copyOf(partial.size + 1)
            newPartial[newPartial.size - 1] = newIssue
            heap.add(HeapItem(newCost, idx + 1, newPartial))
        }
    }
    return results
}

fun kBestClosestScenarios(
    probList: Array<DoubleArray>,
    allowedChoices: List<List<Int>>,
    k: Int
): List<ScenarioCost> {
    if (k <= 0) return emptyList()
    val m = probList.size
    require(m == allowedChoices.size) { "allowedChoices must match probList size" }

    val order = ArrayList<List<IssueProb>>(m)
    val costs = ArrayList<DoubleArray>(m)
    val bestSuffixCost = DoubleArray(m + 1)
    val minSuffixDist = IntArray(m + 1)

    for (i in 0 until m) {
        val p = probList[i]
        val row = ArrayList<IssueProb>(3)
        for (issue in 0..2) {
            row.add(IssueProb(issue, p[issue]))
        }
        row.sortByDescending { it.prob }
        order.add(row)

        val costRow = DoubleArray(3)
        for (r in 0..2) {
            costRow[r] = -ln(row[r].prob)
        }
        costs.add(costRow)
    }

    for (i in m - 1 downTo 0) {
        bestSuffixCost[i] = bestSuffixCost[i + 1] + costs[i][0]
        val bestIssue = order[i][0]
        val plus = if (allowedChoices[i].contains(bestIssue.issue)) 0 else 1
        minSuffixDist[i] = minSuffixDist[i + 1] + plus
    }

    val heap = PriorityQueue<HeapItemClosest>()
    heap.add(HeapItemClosest(0, 0.0, 0, IntArray(0)))

    val results = ArrayList<ScenarioCost>(k)
    var worstKeepDist = Int.MAX_VALUE
    var worstKeepCost = Double.POSITIVE_INFINITY

    while (heap.isNotEmpty() && results.size < k) {
        val top = heap.poll()
        val distCur = top.dist
        val costCur = top.cost
        val idx = top.idx
        val partial = top.partial

        val boundDist = distCur + minSuffixDist[idx]
        val boundCost = costCur + bestSuffixCost[idx]
        if (boundDist > worstKeepDist || (boundDist == worstKeepDist && boundCost > worstKeepCost)) {
            continue
        }

        if (idx == m) {
            results.add(ScenarioCost(partial, costCur))
            if (results.size == k) {
                worstKeepDist = distCur
                worstKeepCost = costCur
            }
            continue
        }

        val costRow = costs[idx]
        val row = order[idx]
        for (rank in 0..2) {
            val ip = row[rank]
            val newDist = distCur + if (allowedChoices[idx].contains(ip.issue)) 0 else 1
            val newCost = costCur + costRow[rank]

            val bDist = newDist + minSuffixDist[idx + 1]
            val bCost = newCost + bestSuffixCost[idx + 1]
            if (bDist > worstKeepDist || (bDist == worstKeepDist && bCost > worstKeepCost)) {
                continue
            }
            val newPartial = partial.copyOf(partial.size + 1)
            newPartial[newPartial.size - 1] = ip.issue
            heap.add(HeapItemClosest(newDist, newCost, idx + 1, newPartial))
        }
    }

    return results
}

package com.example.engine

import com.example.model.BoardHole
import com.example.model.Bolt
import com.example.model.HintMove
import com.example.model.Plank
import com.example.model.PlankStatus
import java.util.PriorityQueue
import kotlin.math.roundToInt

object PuzzleSolver {

    data class SolveResult(
        val moves: List<HintMove>,
        val exploredNodes: Int
    )

    private data class SearchNode(
        val boltHoles: IntArray,
        val planks: List<Plank>,
        val firstMove: HintMove?,
        val parent: SearchNode?,
        val lastMove: HintMove?,
        val g: Int,
        val f: Int
    ) : Comparable<SearchNode> {
        override fun compareTo(other: SearchNode): Int = f.compareTo(other.f)
    }

    /**
     * Finds a valid move sequence clearing all active planks from the given state.
     * Used both by the unit test suite (for all 100 levels) and in-game for real-time Hints.
     */
    fun solve(
        holes: List<BoardHole>,
        bolts: List<Bolt>,
        planks: List<Plank>,
        maxNodes: Int = 15_000
    ): SolveResult? {
        val holeById = holes.associateBy { it.id }
        val holeIndexById = holes.withIndex().associate { it.value.id to it.index }

        val activeInitial = planks.map { p ->
            if (p.status == PlankStatus.FALLING || p.status == PlankStatus.CLEARED) {
                p.copy(status = PlankStatus.CLEARED)
            } else {
                p
            }
        }

        // First normalize any 0-bolt or 1-bolt plank in the starting snapshot
        val normalizedInitial = settleAllPlanks(activeInitial, bolts, holeById)
        if (normalizedInitial.all { it.status == PlankStatus.CLEARED }) {
            return SolveResult(emptyList(), 0)
        }

        val initialBoltHoles = IntArray(bolts.size) { i -> bolts[i].holeId }
        val openSet = PriorityQueue<SearchNode>()
        val visited = HashSet<StateKey>(2048)

        val startKey = makeKey(initialBoltHoles, normalizedInitial)
        visited.add(startKey)
        openSet.add(
            SearchNode(
                boltHoles = initialBoltHoles,
                planks = normalizedInitial,
                firstMove = null,
                parent = null,
                lastMove = null,
                g = 0,
                f = heuristic(normalizedInitial, bolts, holeById)
            )
        )

        var explored = 0
        while (openSet.isNotEmpty() && explored < maxNodes) {
            val current = openSet.poll() ?: break
            explored++

            val currentBolts = List(bolts.size) { idx ->
                Bolt(id = bolts[idx].id, holeId = current.boltHoles[idx])
            }

            // Compute currently occupied hole IDs
            val occupiedHoleIds = HashSet<Int>(current.boltHoles.size * 2)
            for (hid in current.boltHoles) occupiedHoleIds.add(hid)

            // Find all currently available empty holes
            val availableHoles = ArrayList<BoardHole>(holes.size)
            for (h in holes) {
                if (h.id !in occupiedHoleIds && current.planks.none { BoardGeometry.doesPlankBlockHole(it, h) }) {
                    availableHoles.add(h)
                }
            }
            if (availableHoles.isEmpty()) continue

            // Try moving each bolt to each available hole
            for (bIdx in bolts.indices) {
                val bolt = currentBolts[bIdx]
                // Skip bolts that aren't touching any active plank unless we have no better move?
                // Actually checking all bolts is fine, heuristic ranks useful moves first!
                for (targetHole in availableHoles) {
                    val nextBoltHoles = current.boltHoles.copyOf()
                    nextBoltHoles[bIdx] = targetHole.id

                    val nextBolts = List(bolts.size) { idx ->
                        Bolt(id = bolts[idx].id, holeId = nextBoltHoles[idx])
                    }

                    val nextPlanks = settleAllPlanks(current.planks, nextBolts, holeById)
                    val move = HintMove(boltId = bolt.id, targetHoleId = targetHole.id)

                    if (nextPlanks.all { it.status == PlankStatus.CLEARED }) {
                        val path = reconstructPath(current, move)
                        return SolveResult(path, explored)
                    }

                    val key = makeKey(nextBoltHoles, nextPlanks)
                    if (visited.add(key)) {
                        val gNext = current.g + 1
                        val hNext = heuristic(nextPlanks, nextBolts, holeById)
                        openSet.add(
                            SearchNode(
                                boltHoles = nextBoltHoles,
                                planks = nextPlanks,
                                firstMove = current.firstMove ?: move,
                                parent = current,
                                lastMove = move,
                                g = gNext,
                                f = gNext + hNext
                            )
                        )
                    }
                }
            }
        }
        return null
    }

    private fun settleAllPlanks(
        planks: List<Plank>,
        bolts: List<Bolt>,
        holeById: Map<Int, BoardHole>
    ): List<Plank> {
        // Compute bolt counts & settle 1-bolt planks / clear 0-bolt planks
        return planks.map { plank ->
            if (plank.status == PlankStatus.CLEARED || plank.status == PlankStatus.FALLING) {
                plank.copy(status = PlankStatus.CLEARED)
            } else {
                val pins = BoardGeometry.getPinningBolts(plank, bolts, holeById)
                when (pins.size) {
                    0 -> plank.copy(status = PlankStatus.CLEARED)
                    1 -> {
                        val (bolt, localPivot) = pins[0]
                        val pivotHole = holeById[bolt.holeId]
                        if (pivotHole != null) {
                            BoardGeometry.settleSingleBoltPlank(plank, pivotHole, localPivot, planks)
                        } else {
                            plank
                        }
                    }
                    else -> plank.copy(status = PlankStatus.PINNED)
                }
            }
        }
    }

    private fun heuristic(
        planks: List<Plank>,
        bolts: List<Bolt>,
        holeById: Map<Int, BoardHole>
    ): Int {
        var remaining = 0
        var totalPins = 0
        for (p in planks) {
            if (p.status != PlankStatus.CLEARED && p.status != PlankStatus.FALLING) {
                remaining++
                totalPins += BoardGeometry.getPinningBolts(p, bolts, holeById).size
            }
        }
        return remaining * 3 + totalPins
    }

    private fun reconstructPath(lastNode: SearchNode, finalMove: HintMove): List<HintMove> {
        val list = ArrayList<HintMove>()
        list.add(finalMove)
        var cur: SearchNode? = lastNode
        while (cur != null) {
            val m = cur.lastMove
            if (m != null) list.add(m)
            cur = cur.parent
        }
        list.reverse()
        return list
    }

    private class StateKey(private val data: IntArray) {
        private val hash = data.contentHashCode()
        override fun hashCode(): Int = hash
        override fun equals(other: Any?): Boolean {
            return other is StateKey && data.contentEquals(other.data)
        }
    }

    private fun makeKey(boltHoles: IntArray, planks: List<Plank>): StateKey {
        // Sort bolt holes so bolt identity permutation doesn't explode state space!
        val sortedBolts = boltHoles.copyOf()
        sortedBolts.sort()
        val arr = IntArray(sortedBolts.size + planks.size * 4)
        for (i in sortedBolts.indices) {
            arr[i] = sortedBolts[i]
        }
        var offset = sortedBolts.size
        for (p in planks) {
            if (p.status == PlankStatus.CLEARED || p.status == PlankStatus.FALLING) {
                arr[offset++] = 0
                arr[offset++] = 0
                arr[offset++] = 0
                arr[offset++] = 0
            } else {
                arr[offset++] = 1
                arr[offset++] = (p.centerX * 2f).roundToInt()
                arr[offset++] = (p.centerY * 2f).roundToInt()
                val deg = (((p.angleRad * 180f / Math.PI.toFloat()).roundToInt() % 360) + 360) % 360
                arr[offset++] = deg
            }
        }
        return StateKey(arr)
    }
}

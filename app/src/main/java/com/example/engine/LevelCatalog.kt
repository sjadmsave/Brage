package com.example.engine

import com.example.model.BoardHole
import com.example.model.Bolt
import com.example.model.DifficultyTier
import com.example.model.LevelSpec
import com.example.model.LocalPoint
import com.example.model.Plank
import com.example.model.PlankStatus
import com.example.model.WoodStyle
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

object LevelCatalog {
    const val TOTAL_LEVELS = 100

    /**
     * Pre-verified seed offsets for levels 11..100 (verified solvable by LevelCatalogVerificationTest).
     */
    private val VERIFIED_SEED_OFFSETS = intArrayOf(
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 7, 0, 0, 0, 0,
        0, 0, 0, 0, 3, 0, 0, 2, 0, 0,
        2, 0, 0, 4, 0, 0, 0, 0, 0, 16,
        0, 0, 11, 0, 0, 0, 0, 0, 1, 0
    )

    private val VERIFIED_PAR_MOVES = intArrayOf(
        4, 4, 5, 6, 4, 6, 8, 6, 5, 12,
        8, 9, 9, 8, 11, 9, 10, 10, 9, 9,
        7, 8, 7, 15, 15, 12, 10, 11, 10, 9,
        10, 9, 9, 10, 9, 11, 11, 16, 8, 11,
        10, 14, 16, 15, 12, 13, 13, 14, 12, 12,
        19, 12, 16, 15, 11, 13, 12, 15, 12, 8,
        19, 19, 15, 19, 18, 17, 19, 14, 17, 11,
        12, 14, 15, 16, 24, 21, 17, 22, 16, 14,
        18, 17, 13, 20, 18, 18, 22, 18, 19, 22,
        23, 21, 22, 23, 21, 27, 17, 24, 29, 22
    )

    fun getLevel(levelNumber: Int): LevelSpec {
        val clamped = levelNumber.coerceIn(1, TOTAL_LEVELS)
        val spec = if (clamped <= 10) {
            buildHandcraftedLevel(clamped)
        } else {
            val offset = VERIFIED_SEED_OFFSETS[clamped - 11]
            buildProceduralLevel(clamped, offset)
        }
        return spec.copy(parMoves = VERIFIED_PAR_MOVES[clamped - 1])
    }

    /**
     * Generates a candidate level for [levelNumber] with [attemptOffset] so tests or
     * level builders can deterministically pick verified solvable layouts.
     */
    fun buildLevelWithAttempt(levelNumber: Int, attemptOffset: Int): LevelSpec {
        return if (levelNumber <= 10) {
            buildHandcraftedLevel(levelNumber)
        } else {
            buildProceduralLevel(levelNumber, attemptOffset)
        }
    }

    private fun createPlankFromHoles(
        id: Int,
        layer: Int,
        style: WoodStyle,
        holes: List<BoardHole>,
        overhang: Float = 24f,
        thickness: Float = 34f
    ): Plank {
        require(holes.size >= 2) { "Plank must span at least 2 holes" }
        // Sort holes along the primary axis from first to last
        val first = holes.first()
        val last = holes.last()
        val dx = last.x - first.x
        val dy = last.y - first.y
        val span = hypot(dx, dy).coerceAtLeast(1f)
        val ux = dx / span
        val uy = dy / span
        val angleRad = atan2(uy, ux)

        // Compute projections of all holes onto axis u from first
        val projections = holes.map { h ->
            val vx = h.x - first.x
            val vy = h.y - first.y
            vx * ux + vy * uy
        }
        val minProj = projections.minOrNull() ?: 0f
        val maxProj = projections.maxOrNull() ?: span
        val midProj = (minProj + maxProj) * 0.5f

        val centerX = first.x + ux * midProj
        val centerY = first.y + uy * midProj
        val width = (maxProj - minProj) + overhang * 2f

        val localHoles = projections.map { p ->
            LocalPoint(p - midProj, 0f)
        }

        return Plank(
            id = id,
            layer = layer,
            woodStyle = style,
            centerX = centerX,
            centerY = centerY,
            width = width,
            height = thickness,
            angleRad = angleRad,
            localHoles = localHoles,
            status = PlankStatus.PINNED
        )
    }

    private fun buildHandcraftedLevel(level: Int): LevelSpec {
        val tier = DifficultyTier.forLevel(level)
        return when (level) {
            1 -> {
                // Tutorial 1: Two simple horizontal planks + 3 open holes
                val h = listOf(
                    BoardHole(0, 100f, 170f),
                    BoardHole(1, 260f, 170f),
                    BoardHole(2, 100f, 290f),
                    BoardHole(3, 260f, 290f),
                    BoardHole(4, 130f, 400f),
                    BoardHole(5, 230f, 400f)
                )
                val planks = listOf(
                    createPlankFromHoles(0, 0, WoodStyle.MAPLE, listOf(h[0], h[1])),
                    createPlankFromHoles(1, 1, WoodStyle.OAK, listOf(h[2], h[3]))
                )
                val bolts = listOf(Bolt(0, 0), Bolt(1, 1), Bolt(2, 2), Bolt(3, 3))
                LevelSpec(1, tier, h, bolts, planks, parMoves = 2)
            }
            2 -> {
                // Tutorial 2: Swinging plank covering a spare hole!
                val h = listOf(
                    BoardHole(0, 110f, 150f),
                    BoardHole(1, 250f, 210f),
                    BoardHole(2, 110f, 330f),
                    BoardHole(3, 250f, 330f),
                    BoardHole(4, 180f, 180f), // Blocked initially by plank 0!
                    BoardHole(5, 180f, 410f)  // Open spare hole
                )
                val planks = listOf(
                    createPlankFromHoles(0, 1, WoodStyle.CHERRY, listOf(h[0], h[1])),
                    createPlankFromHoles(1, 0, WoodStyle.BIRCH, listOf(h[2], h[3]))
                )
                val bolts = listOf(Bolt(0, 0), Bolt(1, 1), Bolt(2, 2), Bolt(3, 3))
                LevelSpec(2, tier, h, bolts, planks, parMoves = 3)
            }
            3 -> {
                // Triangle 'A' Frame with shared apex bolt
                val h = listOf(
                    BoardHole(0, 180f, 130f),
                    BoardHole(1, 95f, 330f),
                    BoardHole(2, 265f, 330f),
                    BoardHole(3, 138f, 230f),
                    BoardHole(4, 222f, 230f),
                    BoardHole(5, 180f, 420f),
                    BoardHole(6, 90f, 420f)
                )
                val planks = listOf(
                    createPlankFromHoles(0, 0, WoodStyle.OAK, listOf(h[0], h[1])),
                    createPlankFromHoles(1, 1, WoodStyle.TEAK, listOf(h[0], h[2])),
                    createPlankFromHoles(2, 2, WoodStyle.CHERRY, listOf(h[3], h[4]))
                )
                val bolts = listOf(Bolt(0, 0), Bolt(1, 1), Bolt(2, 2), Bolt(3, 3), Bolt(4, 4))
                LevelSpec(3, tier, h, bolts, planks, parMoves = 4)
            }
            4 -> {
                // Classic '+' Cross + lower lock bar
                val h = listOf(
                    BoardHole(0, 180f, 140f),
                    BoardHole(1, 180f, 320f),
                    BoardHole(2, 90f, 230f),
                    BoardHole(3, 270f, 230f),
                    BoardHole(4, 110f, 390f),
                    BoardHole(5, 250f, 390f),
                    BoardHole(6, 180f, 230f), // Blocked center under cross!
                    BoardHole(7, 90f, 140f),
                    BoardHole(8, 270f, 140f)
                )
                val planks = listOf(
                    createPlankFromHoles(0, 0, WoodStyle.WALNUT, listOf(h[0], h[1])),
                    createPlankFromHoles(1, 1, WoodStyle.MAPLE, listOf(h[2], h[3])),
                    createPlankFromHoles(2, 0, WoodStyle.TEAK, listOf(h[4], h[5]))
                )
                val bolts = listOf(Bolt(0, 0), Bolt(1, 1), Bolt(2, 2), Bolt(3, 3), Bolt(4, 4), Bolt(5, 5))
                LevelSpec(4, tier, h, bolts, planks, parMoves = 4)
            }
            5 -> {
                // Overlapping Twin Gates (4 planks)
                val h = listOf(
                    BoardHole(0, 105f, 140f),
                    BoardHole(1, 255f, 140f),
                    BoardHole(2, 105f, 320f),
                    BoardHole(3, 255f, 320f),
                    BoardHole(4, 180f, 230f),
                    BoardHole(5, 105f, 410f),
                    BoardHole(6, 255f, 410f)
                )
                val planks = listOf(
                    createPlankFromHoles(0, 0, WoodStyle.OAK, listOf(h[0], h[2])),
                    createPlankFromHoles(1, 0, WoodStyle.OAK, listOf(h[1], h[3])),
                    createPlankFromHoles(2, 1, WoodStyle.CHERRY, listOf(h[0], h[3])),
                    createPlankFromHoles(3, 2, WoodStyle.BIRCH, listOf(h[1], h[2]))
                )
                val bolts = listOf(Bolt(0, 0), Bolt(1, 1), Bolt(2, 2), Bolt(3, 3))
                LevelSpec(5, tier, h, bolts, planks, parMoves = 4)
            }
            6 -> {
                // Diamond Frame + Center Bar (5 planks)
                val h = listOf(
                    BoardHole(0, 180f, 110f),
                    BoardHole(1, 80f, 230f),
                    BoardHole(2, 280f, 230f),
                    BoardHole(3, 180f, 350f),
                    BoardHole(4, 180f, 230f),
                    BoardHole(5, 100f, 430f),
                    BoardHole(6, 260f, 430f)
                )
                val planks = listOf(
                    createPlankFromHoles(0, 0, WoodStyle.MAPLE, listOf(h[0], h[1])),
                    createPlankFromHoles(1, 1, WoodStyle.TEAK, listOf(h[0], h[2])),
                    createPlankFromHoles(2, 1, WoodStyle.OLIVE, listOf(h[1], h[3])),
                    createPlankFromHoles(3, 0, WoodStyle.ROSEWOOD, listOf(h[2], h[3])),
                    createPlankFromHoles(4, 2, WoodStyle.WALNUT, listOf(h[1], h[4], h[2]))
                )
                val bolts = listOf(Bolt(0, 0), Bolt(1, 1), Bolt(2, 2), Bolt(3, 3), Bolt(4, 4))
                LevelSpec(6, tier, h, bolts, planks, parMoves = 5)
            }
            7 -> {
                // Stepping Stairs (5 planks)
                val h = listOf(
                    BoardHole(0, 75f, 120f),
                    BoardHole(1, 185f, 120f),
                    BoardHole(2, 115f, 200f),
                    BoardHole(3, 235f, 200f),
                    BoardHole(4, 155f, 280f),
                    BoardHole(5, 285f, 280f),
                    BoardHole(6, 85f, 360f),
                    BoardHole(7, 225f, 360f),
                    BoardHole(8, 120f, 440f),
                    BoardHole(9, 240f, 440f)
                )
                val planks = listOf(
                    createPlankFromHoles(0, 0, WoodStyle.BIRCH, listOf(h[0], h[1])),
                    createPlankFromHoles(1, 1, WoodStyle.OAK, listOf(h[2], h[3])),
                    createPlankFromHoles(2, 2, WoodStyle.CHERRY, listOf(h[4], h[5])),
                    createPlankFromHoles(3, 0, WoodStyle.TEAK, listOf(h[6], h[7])),
                    createPlankFromHoles(4, 3, WoodStyle.WALNUT, listOf(h[1], h[3], h[5]))
                )
                val bolts = (0..7).map { Bolt(it, it) }
                LevelSpec(7, tier, h, bolts, planks, parMoves = 6)
            }
            8 -> {
                // Double Cross Lattice (6 planks)
                val h = listOf(
                    BoardHole(0, 90f, 130f),
                    BoardHole(1, 270f, 130f),
                    BoardHole(2, 90f, 260f),
                    BoardHole(3, 270f, 260f),
                    BoardHole(4, 90f, 390f),
                    BoardHole(5, 270f, 390f),
                    BoardHole(6, 180f, 195f), // Blocked by diagonal!
                    BoardHole(7, 180f, 325f), // Blocked by diagonal!
                    BoardHole(8, 180f, 455f),
                    BoardHole(9, 90f, 455f)
                )
                val planks = listOf(
                    createPlankFromHoles(0, 0, WoodStyle.OAK, listOf(h[0], h[1])),
                    createPlankFromHoles(1, 0, WoodStyle.OAK, listOf(h[2], h[3])),
                    createPlankFromHoles(2, 0, WoodStyle.OAK, listOf(h[4], h[5])),
                    createPlankFromHoles(3, 1, WoodStyle.CHERRY, listOf(h[0], h[3])),
                    createPlankFromHoles(4, 2, WoodStyle.MAPLE, listOf(h[1], h[2])),
                    createPlankFromHoles(5, 1, WoodStyle.ROSEWOOD, listOf(h[2], h[5]))
                )
                val bolts = (0..5).map { Bolt(it, it) }
                LevelSpec(8, tier, h, bolts, planks, parMoves = 6)
            }
            9 -> {
                // Triangular Bridge (6 planks)
                val h = listOf(
                    BoardHole(0, 180f, 105f),
                    BoardHole(1, 80f, 215f),
                    BoardHole(2, 280f, 215f),
                    BoardHole(3, 80f, 345f),
                    BoardHole(4, 280f, 345f),
                    BoardHole(5, 180f, 280f),
                    BoardHole(6, 180f, 425f),
                    BoardHole(7, 90f, 435f),
                    BoardHole(8, 270f, 435f)
                )
                val planks = listOf(
                    createPlankFromHoles(0, 0, WoodStyle.TEAK, listOf(h[0], h[1])),
                    createPlankFromHoles(1, 1, WoodStyle.TEAK, listOf(h[0], h[2])),
                    createPlankFromHoles(2, 2, WoodStyle.WALNUT, listOf(h[1], h[2])),
                    createPlankFromHoles(3, 0, WoodStyle.OLIVE, listOf(h[1], h[3])),
                    createPlankFromHoles(4, 0, WoodStyle.OLIVE, listOf(h[2], h[4])),
                    createPlankFromHoles(5, 3, WoodStyle.CHERRY, listOf(h[3], h[4]))
                )
                val bolts = (0..4).map { Bolt(it, it) }
                LevelSpec(9, tier, h, bolts, planks, parMoves = 5)
            }
            else -> {
                // Level 10: Chapter 1 Finale (7 planks, 2 spare holes, 1 blocked)
                val h = listOf(
                    BoardHole(0, 85f, 120f),
                    BoardHole(1, 180f, 120f),
                    BoardHole(2, 275f, 120f),
                    BoardHole(3, 85f, 245f),
                    BoardHole(4, 180f, 245f),
                    BoardHole(5, 275f, 245f),
                    BoardHole(6, 85f, 370f),
                    BoardHole(7, 180f, 370f),
                    BoardHole(8, 275f, 370f),
                    BoardHole(9, 132f, 182f),  // Blocked initially!
                    BoardHole(10, 180f, 450f), // Open spare hole
                    BoardHole(11, 275f, 450f)  // Open spare hole
                )
                val planks = listOf(
                    createPlankFromHoles(0, 0, WoodStyle.MAPLE, listOf(h[0], h[1], h[2])),
                    createPlankFromHoles(1, 1, WoodStyle.OAK, listOf(h[3], h[4], h[5])),
                    createPlankFromHoles(2, 0, WoodStyle.TEAK, listOf(h[6], h[7], h[8])),
                    createPlankFromHoles(3, 2, WoodStyle.CHERRY, listOf(h[0], h[4])),
                    createPlankFromHoles(4, 3, WoodStyle.WALNUT, listOf(h[2], h[4])),
                    createPlankFromHoles(5, 2, WoodStyle.ROSEWOOD, listOf(h[3], h[6])),
                    createPlankFromHoles(6, 2, WoodStyle.OLIVE, listOf(h[5], h[8]))
                )
                val bolts = (0..8).map { Bolt(it, it) }
                LevelSpec(10, tier, h, bolts, planks, parMoves = 8)
            }
        }
    }

    /**
     * Deterministic procedural level builder for Levels 11..100 with escalating complexity:
     * - Plank count scales 4 -> 11
     * - Spare unblocked holes shrink from 3 -> 1..2
     * - Extra blocked holes underneath plank bodies force unlocking sequence
     * - Multi-plank shared intersections increase with level
     */
    private fun buildProceduralLevel(level: Int, attemptOffset: Int): LevelSpec {
        val tier = DifficultyTier.forLevel(level)
        val rng = Random(level * 10_007 + attemptOffset * 7919)

        val progress = (level - 11).toFloat() / 89f // 0.0 -> 1.0
        val targetPlanks = (4 + (progress * 7.2f).toInt()).coerceIn(4, 11)
        val openSpareCount = when {
            level <= 28 -> 2
            level <= 65 -> 2
            else -> if (level % 3 == 0) 1 else 2
        }
        val blockedSpareCount = when {
            level <= 25 -> 1
            level <= 60 -> 2
            else -> 2
        }

        // Build clean anchor grid (4 cols x 5 rows inside safe board bounds)
        val cols = 4
        val rows = 5
        val xStart = 66f
        val xStep = 76f
        val yStart = 96f
        val yStep = 78f

        val gridPoints = ArrayList<Pair<Float, Float>>()
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                gridPoints.add((xStart + c * xStep) to (yStart + r * yStep))
            }
        }

        // Generate candidate plank segments connecting 2 or 3 collinear/near-collinear grid nodes
        data class Segment(val indices: List<Int>)
        val candidates = ArrayList<Segment>()

        fun addIfValid(idxs: List<Int>) {
            val pts = idxs.map { gridPoints[it] }
            val len = hypot(pts.last().first - pts.first().first, pts.last().second - pts.first().second)
            if (len in 70f..255f) {
                candidates.add(Segment(idxs))
            }
        }

        // Horizontal segments (length 1 or 2 grid steps)
        for (r in 0 until rows) {
            for (c in 0 until cols - 1) {
                addIfValid(listOf(r * cols + c, r * cols + c + 1))
                if (c + 2 < cols) {
                    addIfValid(listOf(r * cols + c, r * cols + c + 1, r * cols + c + 2))
                }
            }
        }
        // Vertical segments
        for (c in 0 until cols) {
            for (r in 0 until rows - 1) {
                addIfValid(listOf(r * cols + c, (r + 1) * cols + c))
                if (r + 2 < rows) {
                    addIfValid(listOf(r * cols + c, (r + 1) * cols + c, (r + 2) * cols + c))
                }
            }
        }
        // Diagonal segments
        for (r in 0 until rows - 1) {
            for (c in 0 until cols - 1) {
                addIfValid(listOf(r * cols + c, (r + 1) * cols + (c + 1)))
                addIfValid(listOf(r * cols + (c + 1), (r + 1) * cols + c))
                if (r + 2 < rows && c + 2 < cols) {
                    addIfValid(listOf(r * cols + c, (r + 1) * cols + (c + 1), (r + 2) * cols + (c + 2)))
                    addIfValid(listOf(r * cols + (c + 2), (r + 1) * cols + (c + 1), (r + 2) * cols + c))
                }
            }
        }

        candidates.shuffle(rng)

        // Pick connected/intersecting segments so bolts are shared across planks!
        val chosen = ArrayList<Segment>()
        val usedNodeCounts = HashMap<Int, Int>()

        // Iterative greedy selection favoring intersections
        val pool = candidates.toMutableList()
        while (chosen.size < targetPlanks && pool.isNotEmpty()) {
            // Re-rank top slice to favor touching already-chosen nodes (creates interlocking puzzles)
            var bestIdx = 0
            var bestScore = Int.MIN_VALUE
            val checkLimit = min(pool.size, 30)
            for (i in 0 until checkLimit) {
                val seg = pool[i]
                // Avoid duplicate exact 2-endpoint pair
                val endpoints = setOf(seg.indices.first(), seg.indices.last())
                val duplicate = chosen.any { setOf(it.indices.first(), it.indices.last()) == endpoints }
                if (duplicate) continue
                // Avoid collinear complete overlap with existing plank
                val shareTwo = chosen.any { existing -> existing.indices.count { it in seg.indices } >= 2 }
                if (shareTwo) continue

                val intersections = seg.indices.count { (usedNodeCounts[it] ?: 0) > 0 }
                val overCrowded = seg.indices.count { (usedNodeCounts[it] ?: 0) >= 3 }
                val score = if (chosen.isEmpty()) {
                    seg.indices.size * 3
                } else {
                    intersections * 5 - overCrowded * 8 + seg.indices.size * 2
                }
                if (score > bestScore) {
                    bestScore = score
                    bestIdx = i
                }
            }
            val picked = pool.removeAt(bestIdx)
            val endpoints = setOf(picked.indices.first(), picked.indices.last())
            val duplicate = chosen.any { setOf(it.indices.first(), it.indices.last()) == endpoints }
            val shareTwo = chosen.any { existing -> existing.indices.count { it in picked.indices } >= 2 }
            if (!duplicate && !shareTwo) {
                chosen.add(picked)
                for (n in picked.indices) {
                    usedNodeCounts[n] = (usedNodeCounts[n] ?: 0) + 1
                }
            }
        }

        // Create BoardHoles for every node used by chosen planks
        val usedNodesSorted = usedNodeCounts.keys.sorted()
        val nodeToHole = HashMap<Int, BoardHole>()
        val holes = ArrayList<BoardHole>()
        for (nodeIdx in usedNodesSorted) {
            val (px, py) = gridPoints[nodeIdx]
            val hole = BoardHole(id = holes.size, x = px, y = py)
            holes.add(hole)
            nodeToHole[nodeIdx] = hole
        }

        // Build Planks with graph-colored layers so overlapping planks sit cleanly on distinct Z-layers
        val woodStyles = WoodStyle.entries
        val rawPlanks = ArrayList<Plank>()
        for ((idx, seg) in chosen.withIndex()) {
            val plankHoles = seg.indices.map { nodeToHole.getValue(it) }
            val style = woodStyles[(level + idx * 3) % woodStyles.size]
            val thickness = if (seg.indices.size == 3) 34f else 32f
            val basePlank = createPlankFromHoles(
                id = idx,
                layer = 0,
                style = style,
                holes = plankHoles,
                overhang = 22f,
                thickness = thickness
            )
            // Assign lowest layer that doesn't overlap any plank already in that layer
            var assignedLayer = 0
            while (assignedLayer < 5) {
                val clashes = rawPlanks.any { existing ->
                    existing.layer == assignedLayer && BoardGeometry.obbsOverlap(basePlank, existing, padding = -2f)
                }
                if (!clashes) break
                assignedLayer++
            }
            rawPlanks.add(basePlank.copy(layer = assignedLayer.coerceAtMost(4)))
        }

        // Place initial bolts on all plank holes
        val bolts = holes.mapIndexed { idx, hole -> Bolt(id = idx, holeId = hole.id) }

        // Add blocked spare holes (underneath planks, away from existing holes)
        var addedBlocked = 0
        val candidatePositions = ArrayList<Pair<Float, Float>>()
        for (p in rawPlanks) {
            // Midpoint between consecutive holes on the plank is guaranteed under the plank body!
            for (i in 0 until p.localHoles.size - 1) {
                val midLocal = LocalPoint(
                    (p.localHoles[i].x + p.localHoles[i + 1].x) * 0.5f,
                    0f
                )
                val (wx, wy) = BoardGeometry.localToWorld(p, midLocal)
                candidatePositions.add(wx to wy)
            }
        }
        candidatePositions.shuffle(rng)
        for ((wx, wy) in candidatePositions) {
            if (addedBlocked >= blockedSpareCount) break
            val farEnough = holes.all { hypot(it.x - wx, it.y - wy) >= 34f }
            if (farEnough) {
                val candidateHole = BoardHole(id = holes.size, x = wx, y = wy)
                if (rawPlanks.any { BoardGeometry.doesPlankBlockHole(it, candidateHole) }) {
                    holes.add(candidateHole)
                    addedBlocked++
                }
            }
        }

        // Add open (unblocked) spare holes at clean empty spots on the board
        var addedOpen = 0
        val openGridCandidates = ArrayList<Pair<Float, Float>>()
        for (r in 0..5) {
            for (c in 0..4) {
                val gx = 56f + c * 62f
                val gy = 78f + r * 76f
                openGridCandidates.add(gx to gy)
            }
        }
        // Also try bottom parking rail
        openGridCandidates.add(95f to 468f)
        openGridCandidates.add(180f to 468f)
        openGridCandidates.add(265f to 468f)
        openGridCandidates.shuffle(rng)

        for ((wx, wy) in openGridCandidates) {
            if (addedOpen >= openSpareCount) break
            val farEnough = holes.all { hypot(it.x - wx, it.y - wy) >= 38f }
            if (farEnough) {
                val candidateHole = BoardHole(id = holes.size, x = wx, y = wy)
                if (rawPlanks.none { BoardGeometry.doesPlankBlockHole(it, candidateHole) }) {
                    holes.add(candidateHole)
                    addedOpen++
                }
            }
        }

        // Guarantee at least 1 open spare hole if grid was super dense
        if (addedOpen == 0) {
            for (x in listOf(70f, 180f, 290f, 125f, 235f)) {
                val fallback = BoardHole(id = holes.size, x = x, y = 478f)
                if (holes.all { hypot(it.x - x, it.y - 478f) >= 32f } &&
                    rawPlanks.none { BoardGeometry.doesPlankBlockHole(it, fallback) }
                ) {
                    holes.add(fallback)
                    break
                }
            }
        }

        val estimatedPar = max(targetPlanks, (bolts.size * 0.85f).toInt())
        return LevelSpec(
            levelNumber = level,
            tier = tier,
            holes = holes,
            initialBolts = bolts,
            initialPlanks = rawPlanks,
            parMoves = estimatedPar
        )
    }
}

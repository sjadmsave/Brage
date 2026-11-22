package com.example.engine

import com.example.model.BoardHole
import com.example.model.Bolt
import com.example.model.DifficultyTier
import com.example.model.LevelSpec
import com.example.model.LocalPoint
import com.example.model.Plank
import com.example.model.PlankStatus
import com.example.model.WoodStyle
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object LevelCatalog {
    const val TOTAL_LEVELS = 300

    /**
     * Pre-verified seed offsets for levels 11..300 (290 levels).
     * Verified by LevelCatalogVerificationTest to guarantee 100% solvability.
     */
    private val VERIFIED_SEED_OFFSETS = IntArray(290) { 0 }.apply {
        val baked11To200 = intArrayOf(
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 7, 0, 0, 0, 0,
        0, 0, 0, 0, 3, 0, 0, 2, 0, 0,
        2, 0, 0, 4, 0, 0, 0, 0, 0, 17,
        0, 0, 11, 0, 0, 0, 0, 0, 0, 0,
        0, 3, 0, 0, 0, 0, 0, 1, 0, 0,
        6, 0, 0, 1, 0, 0, 9, 0, 0, 2,
        0, 0, 1, 0, 0, 6, 0, 0, 11, 0,
        0, 4, 0, 0, 10, 0, 0, 5, 0, 0,
        4, 0, 0, 6, 0, 0, 0, 0, 0, 4,
        0, 0, 6, 0, 0, 1, 0, 0, 10, 0,
        0, 1, 0, 0, 4, 0, 0, 4, 0, 0,
        8, 0, 0, 0, 0, 0, 2, 0, 0, 2,
        0, 0, 1, 0, 0, 5, 0, 0, 13, 0,
            0, 1, 0, 0, 10, 0, 0, 4, 0, 0
        )
        for (i in baked11To200.indices) this[i] = baked11To200[i]
        val baked201To300 = intArrayOf(
            7, 0, 0, 9, 0, 0, 2, 0, 0, 0,
            0, 0, 2, 0, 0, 1, 0, 0, 5, 0,
            0, 31, 0, 0, 4, 0, 0, 6, 0, 0,
            4, 0, 0, 5, 0, 0, 17, 0, 0, 0,
            0, 0, 1, 0, 0, 3, 0, 0, 8, 0,
            0, 1, 0, 0, 4, 0, 0, 5, 0, 0,
            0, 0, 0, 0, 0, 0, 2, 0, 0, 5,
            0, 0, 8, 0, 0, 0, 0, 0, 7, 0,
            0, 6, 0, 0, 16, 0, 0, 5, 0, 0,
            2, 0, 0, 0, 0, 0, 1, 0, 0, 4
        )
        for (i in baked201To300.indices) this[190 + i] = baked201To300[i]
    }

    private val VERIFIED_PAR_MOVES = IntArray(300) { 26 }.apply {
        val baked1To200 = intArrayOf(
        4, 4, 5, 6, 4, 6, 8, 6, 5, 12,
        8, 8, 9, 8, 11, 9, 8, 10, 9, 9,
        7, 8, 7, 12, 15, 12, 11, 11, 10, 10,
        10, 9, 10, 8, 9, 12, 11, 16, 9, 16,
        10, 15, 16, 15, 13, 14, 13, 15, 12, 12,
        20, 16, 16, 14, 11, 13, 13, 15, 12, 9,
        13, 19, 20, 19, 18, 20, 19, 14, 18, 13,
        13, 16, 18, 18, 25, 23, 18, 24, 17, 16,
        19, 19, 14, 22, 19, 24, 23, 20, 24, 22,
        24, 23, 23, 25, 22, 29, 22, 26, 26, 24,
        17, 21, 20, 20, 20, 25, 22, 22, 19, 19,
        21, 19, 18, 20, 21, 22, 17, 19, 17, 24,
        21, 20, 16, 26, 24, 25, 24, 22, 19, 21,
        17, 21, 18, 28, 21, 24, 23, 20, 24, 21,
        26, 25, 24, 24, 24, 26, 29, 20, 26, 19,
        27, 25, 30, 25, 25, 27, 30, 30, 29, 20,
        26, 28, 25, 22, 29, 20, 28, 23, 30, 28,
        29, 22, 27, 25, 23, 25, 32, 25, 27, 32,
        29, 24, 24, 25, 22, 27, 32, 29, 33, 32,
            31, 29, 29, 37, 34, 22, 34, 28, 34, 30
        )
        for (i in baked1To200.indices) this[i] = baked1To200[i]
        val baked201To300 = intArrayOf(
            27, 27, 23, 22, 25, 25, 27, 23, 20, 20,
            22, 27, 28, 21, 26, 26, 25, 22, 19, 22,
            25, 22, 22, 22, 26, 18, 30, 26, 30, 23,
            30, 23, 25, 26, 30, 29, 26, 27, 22, 28,
            23, 17, 25, 25, 27, 21, 30, 28, 31, 27,
            29, 25, 25, 25, 29, 30, 24, 22, 32, 24,
            26, 26, 33, 31, 32, 24, 32, 29, 36, 28,
            24, 32, 34, 31, 28, 30, 33, 22, 32, 28,
            31, 26, 26, 26, 33, 23, 29, 26, 30, 33,
            32, 30, 26, 33, 25, 32, 36, 34, 33, 30
        )
        for (i in baked201To300.indices) this[200 + i] = baked201To300[i]
    }

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

    fun buildLevelWithAttempt(levelNumber: Int, attemptOffset: Int): LevelSpec {
        return if (levelNumber <= 10) {
            buildHandcraftedLevel(levelNumber)
        } else {
            buildProceduralLevel(levelNumber, attemptOffset)
        }
    }

    /**
     * Generates a guaranteed-solvable Daily Challenge puzzle deterministically from [dateKey] (YYYYMMDD).
     */
    fun getDailyLevel(dateKey: Int): LevelSpec {
        val virtualLevel = 55 + (dateKey % 70)
        for (attempt in 0..40) {
            val candidate = buildProceduralLevel(virtualLevel, attemptOffset = (dateKey % 997) + attempt)
            val holeById = candidate.holes.associateBy { it.id }
            val allPinned = candidate.initialPlanks.all {
                BoardGeometry.getPinningBolts(it, candidate.initialBolts, holeById).size >= 2
            }
            val hasOpen = candidate.holes.any {
                BoardGeometry.isHoleAvailableForBolt(it, candidate.initialBolts, candidate.initialPlanks)
            }
            if (allPinned && hasOpen) {
                val solved = PuzzleSolver.solve(candidate.holes, candidate.initialBolts, candidate.initialPlanks, maxNodes = 14_000)
                if (solved != null && solved.moves.size >= 8) {
                    val shieldMoves = candidate.initialBolts.count { it.reinforced }
                    return candidate.copy(levelNumber = 1, parMoves = solved.moves.size + shieldMoves)
                }
            }
        }
        return getLevel(75).copy(levelNumber = 1)
    }

    /**
     * Generates a guaranteed-solvable level for Endless / Time Attack / Precision arcade rounds.
     */
    fun getArcadeRoundLevel(round: Int, modeSalt: Int): LevelSpec {
        val mappedLevel = (12 + (round - 1) * 5).coerceIn(12, TOTAL_LEVELS)
        if (modeSalt == 0 && mappedLevel <= TOTAL_LEVELS) {
            return getLevel(mappedLevel).copy(levelNumber = round)
        }
        for (attempt in 0..40) {
            val candidate = buildProceduralLevel(mappedLevel, attemptOffset = modeSalt * 31 + round * 7 + attempt)
            val holeById = candidate.holes.associateBy { it.id }
            val allPinned = candidate.initialPlanks.all {
                BoardGeometry.getPinningBolts(it, candidate.initialBolts, holeById).size >= 2
            }
            val hasOpen = candidate.holes.any {
                BoardGeometry.isHoleAvailableForBolt(it, candidate.initialBolts, candidate.initialPlanks)
            }
            if (allPinned && hasOpen) {
                val solved = PuzzleSolver.solve(candidate.holes, candidate.initialBolts, candidate.initialPlanks, maxNodes = 12_000)
                if (solved != null) {
                    val shieldMoves = candidate.initialBolts.count { it.reinforced }
                    return candidate.copy(levelNumber = round, parMoves = solved.moves.size + shieldMoves)
                }
            }
        }
        return getLevel(mappedLevel).copy(levelNumber = round)
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
        val first = holes.first()
        val last = holes.last()
        val dx = last.x - first.x
        val dy = last.y - first.y
        val span = hypot(dx, dy).coerceAtLeast(1f)
        val ux = dx / span
        val uy = dy / span
        val angleRad = atan2(uy, ux)

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

        val localHoles = projections.map { p -> LocalPoint(p - midProj, 0f) }

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
                LevelSpec(1, tier, h, bolts, planks, parMoves = 4)
            }
            2 -> {
                val h = listOf(
                    BoardHole(0, 110f, 150f),
                    BoardHole(1, 250f, 210f),
                    BoardHole(2, 110f, 330f),
                    BoardHole(3, 250f, 330f),
                    BoardHole(4, 180f, 180f),
                    BoardHole(5, 180f, 410f)
                )
                val planks = listOf(
                    createPlankFromHoles(0, 1, WoodStyle.CHERRY, listOf(h[0], h[1])),
                    createPlankFromHoles(1, 0, WoodStyle.BIRCH, listOf(h[2], h[3]))
                )
                val bolts = listOf(Bolt(0, 0), Bolt(1, 1), Bolt(2, 2), Bolt(3, 3))
                LevelSpec(2, tier, h, bolts, planks, parMoves = 4)
            }
            3 -> {
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
                LevelSpec(3, tier, h, bolts, planks, parMoves = 5)
            }
            4 -> {
                val h = listOf(
                    BoardHole(0, 180f, 140f),
                    BoardHole(1, 180f, 320f),
                    BoardHole(2, 90f, 230f),
                    BoardHole(3, 270f, 230f),
                    BoardHole(4, 110f, 390f),
                    BoardHole(5, 250f, 390f),
                    BoardHole(6, 180f, 230f),
                    BoardHole(7, 90f, 140f),
                    BoardHole(8, 270f, 140f)
                )
                val planks = listOf(
                    createPlankFromHoles(0, 0, WoodStyle.WALNUT, listOf(h[0], h[1])),
                    createPlankFromHoles(1, 1, WoodStyle.MAPLE, listOf(h[2], h[3])),
                    createPlankFromHoles(2, 0, WoodStyle.TEAK, listOf(h[4], h[5]))
                )
                val bolts = (0..5).map { Bolt(it, it) }
                LevelSpec(4, tier, h, bolts, planks, parMoves = 6)
            }
            5 -> {
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
                val bolts = (0..3).map { Bolt(it, it) }
                LevelSpec(5, tier, h, bolts, planks, parMoves = 4)
            }
            6 -> {
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
                val bolts = (0..4).map { Bolt(it, it) }
                LevelSpec(6, tier, h, bolts, planks, parMoves = 6)
            }
            7 -> {
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
                LevelSpec(7, tier, h, bolts, planks, parMoves = 8)
            }
            8 -> {
                val h = listOf(
                    BoardHole(0, 90f, 130f),
                    BoardHole(1, 270f, 130f),
                    BoardHole(2, 90f, 260f),
                    BoardHole(3, 270f, 260f),
                    BoardHole(4, 90f, 390f),
                    BoardHole(5, 270f, 390f),
                    BoardHole(6, 180f, 195f),
                    BoardHole(7, 180f, 325f),
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
                    BoardHole(9, 132f, 182f),
                    BoardHole(10, 180f, 450f),
                    BoardHole(11, 275f, 450f)
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
                LevelSpec(10, tier, h, bolts, planks, parMoves = 12)
            }
        }
    }

    private fun buildProceduralLevel(level: Int, attemptOffset: Int): LevelSpec {
        val tier = DifficultyTier.forLevel(level)
        val rng = Random(level * 10_007 + attemptOffset * 7919)

        val isUltraGrid = level > 200
        val isMegaGrid = level > 100
        val cols = if (isMegaGrid) 5 else 4
        val rows = if (isUltraGrid) 6 else 5
        val xStart = if (isMegaGrid) 54f else 66f
        val xStep = if (isMegaGrid) 63f else 76f
        val yStart = when {
            isUltraGrid -> 76f
            isMegaGrid -> 88f
            else -> 96f
        }
        val yStep = when {
            isUltraGrid -> 67f
            isMegaGrid -> 75f
            else -> 78f
        }

        val targetPlanks = when {
            !isMegaGrid -> (4 + ((level - 11).toFloat() / 89f * 7.2f).toInt()).coerceIn(4, 11)
            !isUltraGrid -> (9 + ((level - 101).toFloat() / 99f * 4.5f).toInt()).coerceIn(9, 13)
            else -> (10 + ((level - 201).toFloat() / 99f * 4.2f).toInt()).coerceIn(10, 14)
        }

        val openSpareCount = when {
            level <= 28 -> 2
            level <= 65 -> 2
            else -> if (level % 3 == 0) 1 else 2
        }
        val blockedSpareCount = when {
            level <= 25 -> 1
            level <= 100 -> 2
            else -> 3
        }

        val gridPoints = ArrayList<Pair<Float, Float>>()
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                gridPoints.add((xStart + c * xStep) to (yStart + r * yStep))
            }
        }

        data class Segment(val indices: List<Int>)
        val candidates = ArrayList<Segment>()

        fun addIfValid(idxs: List<Int>) {
            val pts = idxs.map { gridPoints[it] }
            val len = hypot(pts.last().first - pts.first().first, pts.last().second - pts.first().second)
            if (len in 58f..265f) {
                candidates.add(Segment(idxs))
            }
        }

        for (r in 0 until rows) {
            for (c in 0 until cols - 1) {
                addIfValid(listOf(r * cols + c, r * cols + c + 1))
                if (c + 2 < cols) addIfValid(listOf(r * cols + c, r * cols + c + 1, r * cols + c + 2))
            }
        }
        for (c in 0 until cols) {
            for (r in 0 until rows - 1) {
                addIfValid(listOf(r * cols + c, (r + 1) * cols + c))
                if (r + 2 < rows) addIfValid(listOf(r * cols + c, (r + 1) * cols + c, (r + 2) * cols + c))
            }
        }
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

        val chosen = ArrayList<Segment>()
        val usedNodeCounts = HashMap<Int, Int>()
        val pool = candidates.toMutableList()

        while (chosen.size < targetPlanks && pool.isNotEmpty()) {
            var bestIdx = 0
            var bestScore = Int.MIN_VALUE
            val checkLimit = min(pool.size, 32)
            for (i in 0 until checkLimit) {
                val seg = pool[i]
                val endpoints = setOf(seg.indices.first(), seg.indices.last())
                val duplicate = chosen.any { setOf(it.indices.first(), it.indices.last()) == endpoints }
                if (duplicate) continue
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

        val usedNodesSorted = usedNodeCounts.keys.sorted()
        val nodeToHole = HashMap<Int, BoardHole>()
        val holes = ArrayList<BoardHole>()
        for (nodeIdx in usedNodesSorted) {
            val (px, py) = gridPoints[nodeIdx]
            val hole = BoardHole(id = holes.size, x = px, y = py)
            holes.add(hole)
            nodeToHole[nodeIdx] = hole
        }

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
                overhang = if (isMegaGrid) 20f else 22f,
                thickness = thickness
            )
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

        // Reinforced Shielded Bolts appear selectively on levels 26+
        val shieldCount = when {
            level < 26 -> 0
            level < 70 -> if (level % 3 == 0) 1 else 0
            level < 130 -> if (level % 2 == 0) 2 else 1
            else -> 2 + (level % 2)
        }
        val bolts = holes.mapIndexed { idx, hole ->
            val isShielded = shieldCount > 0 && idx < shieldCount
            Bolt(id = idx, holeId = hole.id, reinforced = isShielded)
        }

        var addedBlocked = 0
        val candidatePositions = ArrayList<Pair<Float, Float>>()
        for (p in rawPlanks) {
            for (i in 0 until p.localHoles.size - 1) {
                val midLocal = LocalPoint((p.localHoles[i].x + p.localHoles[i + 1].x) * 0.5f, 0f)
                val (wx, wy) = BoardGeometry.localToWorld(p, midLocal)
                candidatePositions.add(wx to wy)
            }
        }
        candidatePositions.shuffle(rng)
        for ((wx, wy) in candidatePositions) {
            if (addedBlocked >= blockedSpareCount) break
            val farEnough = holes.all { hypot(it.x - wx, it.y - wy) >= 32f }
            if (farEnough) {
                val candidateHole = BoardHole(id = holes.size, x = wx, y = wy)
                if (rawPlanks.any { BoardGeometry.doesPlankBlockHole(it, candidateHole) }) {
                    holes.add(candidateHole)
                    addedBlocked++
                }
            }
        }

        var addedOpen = 0
        val openGridCandidates = ArrayList<Pair<Float, Float>>()
        for (r in 0..5) {
            for (c in 0..4) {
                openGridCandidates.add((56f + c * 62f) to (78f + r * 76f))
            }
        }
        openGridCandidates.add(95f to 468f)
        openGridCandidates.add(180f to 468f)
        openGridCandidates.add(265f to 468f)
        openGridCandidates.shuffle(rng)

        for ((wx, wy) in openGridCandidates) {
            if (addedOpen >= openSpareCount) break
            val farEnough = holes.all { hypot(it.x - wx, it.y - wy) >= 36f }
            if (farEnough) {
                val candidateHole = BoardHole(id = holes.size, x = wx, y = wy)
                if (rawPlanks.none { BoardGeometry.doesPlankBlockHole(it, candidateHole) }) {
                    holes.add(candidateHole)
                    addedOpen++
                }
            }
        }

        if (addedOpen == 0) {
            for (x in listOf(70f, 180f, 290f, 125f, 235f)) {
                val fallback = BoardHole(id = holes.size, x = x, y = 478f)
                if (holes.all { hypot(it.x - x, it.y - 478f) >= 30f } &&
                    rawPlanks.none { BoardGeometry.doesPlankBlockHole(it, fallback) }
                ) {
                    holes.add(fallback)
                    break
                }
            }
        }

        val estimatedPar = max(targetPlanks, (bolts.size * 0.85f).toInt()) + shieldCount
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

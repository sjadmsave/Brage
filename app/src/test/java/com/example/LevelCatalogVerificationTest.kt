package com.example

import com.example.engine.BoardGeometry
import com.example.engine.LevelCatalog
import com.example.engine.PuzzleSolver
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.hypot

class LevelCatalogVerificationTest {

    @Test
    fun all100LevelsAreValidSolvableAndProgressivelyHarder() {
        var totalBeginnerPar = 0
        var totalMasterPar = 0

        for (level in 1..LevelCatalog.TOTAL_LEVELS) {
            val spec = LevelCatalog.getLevel(level)
            val holeById = spec.holes.associateBy { it.id }

            // 1. All holes inside board boundaries and spaced apart
            for (i in spec.holes.indices) {
                val h = spec.holes[i]
                assertTrue("Level $level hole $i X out of bounds: ${h.x}", h.x in 30f..(BoardGeometry.BOARD_WIDTH - 30f))
                assertTrue("Level $level hole $i Y out of bounds: ${h.y}", h.y in 40f..(BoardGeometry.BOARD_HEIGHT - 30f))
                for (j in i + 1 until spec.holes.size) {
                    val other = spec.holes[j]
                    val dist = hypot(h.x - other.x, h.y - other.y)
                    assertTrue("Level $level holes $i and $j overlap: dist=$dist", dist >= 28f)
                }
            }

            // 2. Every plank starts pinned by >= 2 bolts
            for (plank in spec.initialPlanks) {
                val pins = BoardGeometry.getPinningBolts(plank, spec.initialBolts, holeById)
                assertTrue(
                    "Level $level plank ${plank.id} starts with ${pins.size} bolts (expected >= 2)",
                    pins.size >= 2
                )
            }

            // 3. At least 1 open unblocked hole initially
            val hasOpenHole = spec.holes.any {
                BoardGeometry.isHoleAvailableForBolt(it, spec.initialBolts, spec.initialPlanks)
            }
            assertTrue("Level $level must have at least 1 unblocked initial hole", hasOpenHole)

            // 4. Verified solvable by PuzzleSolver
            val solveResult = PuzzleSolver.solve(
                holes = spec.holes,
                bolts = spec.initialBolts,
                planks = spec.initialPlanks,
                maxNodes = 20_000
            )
            assertNotNull("Level $level must be solvable!", solveResult)
            assertTrue("Level $level solution must not be empty", solveResult!!.moves.isNotEmpty())

            if (level in 1..20) totalBeginnerPar += spec.parMoves
            if (level in 81..100) totalMasterPar += spec.parMoves
        }

        // 5. Master tier must be significantly more complex than Beginner tier
        assertTrue(
            "Master levels par ($totalMasterPar) must exceed Beginner levels par ($totalBeginnerPar)",
            totalMasterPar > totalBeginnerPar * 2
        )
    }
}

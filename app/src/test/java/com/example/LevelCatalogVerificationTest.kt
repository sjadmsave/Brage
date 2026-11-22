package com.example

import com.example.engine.BoardGeometry
import com.example.engine.LevelCatalog
import com.example.engine.PuzzleSolver
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelCatalogVerificationTest {

    @Test
    fun all300LevelsAreValidAndSolvable() {
        for (level in 1..LevelCatalog.TOTAL_LEVELS) {
            val spec = LevelCatalog.getLevel(level)
            val holeById = spec.holes.associateBy { it.id }

            val allPinned = spec.initialPlanks.all { plank ->
                BoardGeometry.getPinningBolts(plank, spec.initialBolts, holeById).size >= 2
            }
            assertTrue("Level $level has a plank pinned by fewer than 2 bolts!", allPinned)

            val hasOpenHole = spec.holes.any { hole ->
                BoardGeometry.isHoleAvailableForBolt(hole, spec.initialBolts, spec.initialPlanks)
            }
            assertTrue("Level $level has no open starting hole!", hasOpenHole)

            val solution = PuzzleSolver.solve(
                holes = spec.holes,
                bolts = spec.initialBolts,
                planks = spec.initialPlanks,
                maxNodes = 25_000
            )
            assertTrue("Level $level could not be solved!", solution != null && solution.moves.isNotEmpty())
        }
    }

    @Test
    fun dailyAndArcadeLevelsAreSolvable() {
        val daily = LevelCatalog.getDailyLevel(20261122)
        val dailySol = PuzzleSolver.solve(daily.holes, daily.initialBolts, daily.initialPlanks, maxNodes = 20_000)
        assertTrue("Daily level is not solvable!", dailySol != null)

        for (round in 1..10) {
            val arcade = LevelCatalog.getArcadeRoundLevel(round, modeSalt = 2)
            val sol = PuzzleSolver.solve(arcade.holes, arcade.initialBolts, arcade.initialPlanks, maxNodes = 20_000)
            assertTrue("Arcade round $round is not solvable!", sol != null)
        }
    }
}

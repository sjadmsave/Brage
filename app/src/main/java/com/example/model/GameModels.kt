package com.example.model

import androidx.compose.ui.graphics.Color

enum class WoodStyle(
    val lightColor: Color,
    val darkColor: Color,
    val grainColor: Color,
    val highlightColor: Color
) {
    // Sleek futuristic tech-alloy & circuit-etched composite panel styles
    MAPLE(
        lightColor = Color(0xFF1F4E78),
        darkColor = Color(0xFF112C47),
        grainColor = Color(0xFF00D4FF),
        highlightColor = Color(0xFF66EAFF)
    ),
    OAK(
        lightColor = Color(0xFF284166),
        darkColor = Color(0xFF14243D),
        grainColor = Color(0xFF4BA3FF),
        highlightColor = Color(0xFF8CC8FF)
    ),
    CHERRY(
        lightColor = Color(0xFF5A254B),
        darkColor = Color(0xFF33112B),
        grainColor = Color(0xFFFF4D8D),
        highlightColor = Color(0xFFFF8CB5)
    ),
    WALNUT(
        lightColor = Color(0xFF242D42),
        darkColor = Color(0xFF131926),
        grainColor = Color(0xFF7B93C4),
        highlightColor = Color(0xFFAABDE6)
    ),
    TEAK(
        lightColor = Color(0xFF5E4518),
        darkColor = Color(0xFF36260B),
        grainColor = Color(0xFFFFB020),
        highlightColor = Color(0xFFFFD166)
    ),
    OLIVE(
        lightColor = Color(0xFF145247),
        darkColor = Color(0xFF0A2E27),
        grainColor = Color(0xFF00E699),
        highlightColor = Color(0xFF66FFD1)
    ),
    BIRCH(
        lightColor = Color(0xFF3C4C6E),
        darkColor = Color(0xFF212C42),
        grainColor = Color(0xFF38BDF8),
        highlightColor = Color(0xFF93E2FF)
    ),
    ROSEWOOD(
        lightColor = Color(0xFF3F2666),
        darkColor = Color(0xFF23133D),
        grainColor = Color(0xFFA855F7),
        highlightColor = Color(0xFFD09CFF)
    )
}

enum class DifficultyTier(
    val titleAr: String,
    val range: IntRange
) {
    BEGINNER("مبتدئ", 1..20),
    MEDIUM("متوسط", 21..40),
    HARD("صعب", 41..60),
    EXPERT("خبير", 61..80),
    MASTER("أسطوري", 81..100);

    companion object {
        fun forLevel(level: Int): DifficultyTier {
            return entries.firstOrNull { level in it.range } ?: MASTER
        }
    }
}

data class LocalPoint(
    val x: Float,
    val y: Float
)

data class BoardHole(
    val id: Int,
    val x: Float,
    val y: Float,
    val radius: Float = 14f
)

data class Bolt(
    val id: Int,
    val holeId: Int
)

enum class PlankStatus {
    PINNED,
    SWINGING,
    FALLING,
    CLEARED
}

data class Plank(
    val id: Int,
    val layer: Int,
    val woodStyle: WoodStyle,
    val centerX: Float,
    val centerY: Float,
    val width: Float,
    val height: Float,
    val angleRad: Float,
    val localHoles: List<LocalPoint>,
    val status: PlankStatus = PlankStatus.PINNED,
    val angularVelocity: Float = 0f,
    val velocityY: Float = 0f,
    val velocityX: Float = 0f,
    val alpha: Float = 1f
)

data class LevelSpec(
    val levelNumber: Int,
    val tier: DifficultyTier,
    val holes: List<BoardHole>,
    val initialBolts: List<Bolt>,
    val initialPlanks: List<Plank>,
    val parMoves: Int
)

data class HintMove(
    val boltId: Int,
    val targetHoleId: Int
)

data class BoardSnapshot(
    val bolts: List<Bolt>,
    val planks: List<Plank>,
    val moves: Int
)

data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val size: Float,
    val color: Color,
    val life: Float,
    val maxLife: Float,
    val rotation: Float
)

data class LevelProgress(
    val levelNumber: Int,
    val stars: Int = 0,
    val bestMoves: Int? = null
)

data class UserProgressState(
    val highestUnlockedLevel: Int = 1,
    val levelStars: Map<Int, Int> = emptyMap(),
    val levelBestMoves: Map<Int, Int> = emptyMap(),
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val unlockAllMode: Boolean = false
) {
    val totalStars: Int get() = levelStars.values.sum()
    val completedCount: Int get() = levelStars.count { it.value > 0 }

    fun isLevelUnlocked(level: Int): Boolean {
        return unlockAllMode || level <= highestUnlockedLevel
    }
}

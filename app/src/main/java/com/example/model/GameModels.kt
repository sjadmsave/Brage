package com.example.model

import androidx.compose.ui.graphics.Color

enum class WoodStyle(
    val lightColor: Color,
    val darkColor: Color,
    val grainColor: Color,
    val highlightColor: Color
) {
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

enum class GameMode(
    val titleAr: String,
    val subtitleAr: String
) {
    CLASSIC("الطور الكلاسيكي", "200 مستوى متدرج الصعوبة"),
    TIME_ATTACK("سباق الزمن", "أنهِ اللوحة قبل نفاد الوقت (+4ث لكل لوح)"),
    PRECISION("تحدي الدقة", "حل اللغز ضمن سقف الحركات المسموح"),
    ENDLESS("التحدي اللانهائي", "جولات متجددة تتصاعد صعوبتها بلا حدود"),
    DAILY("التحدي اليومي", "لغز يومي خاص يتجدد كل يوم")
}

enum class BoardTheme(
    val titleAr: String,
    val unlockCost: Int,
    val accentColor: Color,
    val deckTopColor: Color,
    val deckBottomColor: Color,
    val frameTopColor: Color,
    val frameBottomColor: Color
) {
    CYBER_NAVY(
        titleAr = "سايبر أزرق",
        unlockCost = 0,
        accentColor = Color(0xFF00D4FF),
        deckTopColor = Color(0xFF080E1C),
        deckBottomColor = Color(0xFF070C18),
        frameTopColor = Color(0xFF263B63),
        frameBottomColor = Color(0xFF101B33)
    ),
    MATRIX_GREEN(
        titleAr = "مصفوفة الزمرد",
        unlockCost = 50,
        accentColor = Color(0xFF00E699),
        deckTopColor = Color(0xFF061612),
        deckBottomColor = Color(0xFF040F0C),
        frameTopColor = Color(0xFF134E3E),
        frameBottomColor = Color(0xFF0B2921)
    ),
    SOLAR_AMBER(
        titleAr = "ذهب المفاعل",
        unlockCost = 100,
        accentColor = Color(0xFFFFB020),
        deckTopColor = Color(0xFF181106),
        deckBottomColor = Color(0xFF0F0B04),
        frameTopColor = Color(0xFF5C4115),
        frameBottomColor = Color(0xFF2B1E0A)
    ),
    PLASMA_VIOLET(
        titleAr = "بلازما كمومية",
        unlockCost = 180,
        accentColor = Color(0xFFD946EF),
        deckTopColor = Color(0xFF14071F),
        deckBottomColor = Color(0xFF0B0412),
        frameTopColor = Color(0xFF4A1B6D),
        frameBottomColor = Color(0xFF230C38)
    )
}

enum class DifficultyTier(
    val titleAr: String,
    val range: IntRange
) {
    BEGINNER("مبتدئ", 1..25),
    MEDIUM("متوسط", 26..50),
    HARD("صعب", 51..90),
    EXPERT("خبير", 91..140),
    MASTER("أسطوري", 141..195),
    QUANTUM("كوانتم", 196..250),
    OMEGA("أوميغا", 251..300);

    companion object {
        fun forLevel(level: Int): DifficultyTier {
            return entries.firstOrNull { level in it.range } ?: OMEGA
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
    val holeId: Int,
    val reinforced: Boolean = false
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
    val holes: List<BoardHole>,
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

data class UserProgressState(
    val highestUnlockedLevel: Int = 1,
    val levelStars: Map<Int, Int> = emptyMap(),
    val levelBestMoves: Map<Int, Int> = emptyMap(),
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val unlockAllMode: Boolean = false,
    val timeAttackBestStreak: Int = 0,
    val precisionClears: Int = 0,
    val endlessBestRound: Int = 0,
    val dailyCompletedKey: Int = 0,
    val techCredits: Int = 60,
    val unlockedThemes: Set<String> = setOf(BoardTheme.CYBER_NAVY.name),
    val selectedTheme: BoardTheme = BoardTheme.CYBER_NAVY
) {
    val totalStars: Int get() = levelStars.values.sum()
    val completedCount: Int get() = levelStars.count { it.value > 0 }

    fun isLevelUnlocked(level: Int): Boolean {
        return unlockAllMode || level <= highestUnlockedLevel
    }

    fun isThemeUnlocked(theme: BoardTheme): Boolean {
        return theme == BoardTheme.CYBER_NAVY || theme.name in unlockedThemes
    }
}

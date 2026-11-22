package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.engine.LevelCatalog
import com.example.model.BoardTheme
import com.example.model.UserProgressState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "wood_bolts_progress")

class ProgressRepository(private val context: Context) {

    private object Keys {
        val HIGHEST_UNLOCKED = intPreferencesKey("highest_unlocked_level")
        val LEVEL_STARS = stringPreferencesKey("level_stars_map")
        val LEVEL_BEST_MOVES = stringPreferencesKey("level_best_moves_map")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val UNLOCK_ALL_MODE = booleanPreferencesKey("unlock_all_mode")
        val TIME_ATTACK_BEST_STREAK = intPreferencesKey("time_attack_best_streak")
        val PRECISION_CLEARS = intPreferencesKey("precision_clears")
        val ENDLESS_BEST_ROUND = intPreferencesKey("endless_best_round")
        val DAILY_COMPLETED_KEY = intPreferencesKey("daily_completed_key")
        val TECH_CREDITS = intPreferencesKey("tech_credits")
        val UNLOCKED_THEMES = stringPreferencesKey("unlocked_themes")
        val SELECTED_THEME = stringPreferencesKey("selected_theme")
    }

    val progressFlow: Flow<UserProgressState> = context.dataStore.data.map { prefs ->
        val unlockedThemesRaw = prefs[Keys.UNLOCKED_THEMES]
        val unlockedSet = if (unlockedThemesRaw.isNullOrBlank()) {
            setOf(BoardTheme.CYBER_NAVY.name)
        } else {
            unlockedThemesRaw.split(",").filter { it.isNotBlank() }.toSet() + BoardTheme.CYBER_NAVY.name
        }
        val selectedThemeName = prefs[Keys.SELECTED_THEME] ?: BoardTheme.CYBER_NAVY.name
        val selectedTheme = BoardTheme.entries.firstOrNull { it.name == selectedThemeName }
            ?: BoardTheme.CYBER_NAVY

        UserProgressState(
            highestUnlockedLevel = (prefs[Keys.HIGHEST_UNLOCKED] ?: 1).coerceIn(1, LevelCatalog.TOTAL_LEVELS),
            levelStars = parseIntMap(prefs[Keys.LEVEL_STARS]),
            levelBestMoves = parseIntMap(prefs[Keys.LEVEL_BEST_MOVES]),
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
            unlockAllMode = prefs[Keys.UNLOCK_ALL_MODE] ?: false,
            timeAttackBestStreak = prefs[Keys.TIME_ATTACK_BEST_STREAK] ?: 0,
            precisionClears = prefs[Keys.PRECISION_CLEARS] ?: 0,
            endlessBestRound = prefs[Keys.ENDLESS_BEST_ROUND] ?: 0,
            dailyCompletedKey = prefs[Keys.DAILY_COMPLETED_KEY] ?: 0,
            techCredits = prefs[Keys.TECH_CREDITS] ?: 60,
            unlockedThemes = unlockedSet,
            selectedTheme = selectedTheme
        )
    }

    suspend fun recordLevelComplete(levelNumber: Int, starsEarned: Int, movesUsed: Int, creditsReward: Int) {
        context.dataStore.edit { prefs ->
            val currentUnlocked = prefs[Keys.HIGHEST_UNLOCKED] ?: 1
            val nextUnlocked = maxOf(currentUnlocked, (levelNumber + 1).coerceAtMost(LevelCatalog.TOTAL_LEVELS))
            prefs[Keys.HIGHEST_UNLOCKED] = nextUnlocked

            val starsMap = parseIntMap(prefs[Keys.LEVEL_STARS]).toMutableMap()
            val prevStars = starsMap[levelNumber] ?: 0
            if (starsEarned > prevStars) {
                starsMap[levelNumber] = starsEarned.coerceIn(1, 3)
                prefs[Keys.LEVEL_STARS] = serializeIntMap(starsMap)
            }

            val movesMap = parseIntMap(prefs[Keys.LEVEL_BEST_MOVES]).toMutableMap()
            val prevBest = movesMap[levelNumber]
            if (prevBest == null || movesUsed < prevBest) {
                movesMap[levelNumber] = movesUsed
                prefs[Keys.LEVEL_BEST_MOVES] = serializeIntMap(movesMap)
            }

            val currentCredits = prefs[Keys.TECH_CREDITS] ?: 60
            prefs[Keys.TECH_CREDITS] = currentCredits + creditsReward.coerceAtLeast(0)
        }
    }

    suspend fun recordTimeAttackStreak(streak: Int, creditsReward: Int) {
        context.dataStore.edit { prefs ->
            val prev = prefs[Keys.TIME_ATTACK_BEST_STREAK] ?: 0
            if (streak > prev) prefs[Keys.TIME_ATTACK_BEST_STREAK] = streak
            val currentCredits = prefs[Keys.TECH_CREDITS] ?: 60
            prefs[Keys.TECH_CREDITS] = currentCredits + creditsReward.coerceAtLeast(0)
        }
    }

    suspend fun recordPrecisionClear(creditsReward: Int) {
        context.dataStore.edit { prefs ->
            val prev = prefs[Keys.PRECISION_CLEARS] ?: 0
            prefs[Keys.PRECISION_CLEARS] = prev + 1
            val currentCredits = prefs[Keys.TECH_CREDITS] ?: 60
            prefs[Keys.TECH_CREDITS] = currentCredits + creditsReward.coerceAtLeast(0)
        }
    }

    suspend fun recordEndlessRound(round: Int, creditsReward: Int) {
        context.dataStore.edit { prefs ->
            val prev = prefs[Keys.ENDLESS_BEST_ROUND] ?: 0
            if (round > prev) prefs[Keys.ENDLESS_BEST_ROUND] = round
            val currentCredits = prefs[Keys.TECH_CREDITS] ?: 60
            prefs[Keys.TECH_CREDITS] = currentCredits + creditsReward.coerceAtLeast(0)
        }
    }

    suspend fun recordDailyCompleted(dateKey: Int, creditsReward: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DAILY_COMPLETED_KEY] = dateKey
            val currentCredits = prefs[Keys.TECH_CREDITS] ?: 60
            prefs[Keys.TECH_CREDITS] = currentCredits + creditsReward.coerceAtLeast(0)
        }
    }

    suspend fun spendCredits(cost: Int): Boolean {
        var success = false
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.TECH_CREDITS] ?: 60
            if (current >= cost) {
                prefs[Keys.TECH_CREDITS] = current - cost
                success = true
            }
        }
        return success
    }

    suspend fun unlockAndSelectTheme(theme: BoardTheme): Boolean {
        var unlocked = false
        context.dataStore.edit { prefs ->
            val raw = prefs[Keys.UNLOCKED_THEMES] ?: BoardTheme.CYBER_NAVY.name
            val existing = raw.split(",").filter { it.isNotBlank() }.toMutableSet()
            existing.add(BoardTheme.CYBER_NAVY.name)
            if (theme.name in existing) {
                prefs[Keys.SELECTED_THEME] = theme.name
                unlocked = true
            } else {
                val currentCredits = prefs[Keys.TECH_CREDITS] ?: 60
                if (currentCredits >= theme.unlockCost) {
                    prefs[Keys.TECH_CREDITS] = currentCredits - theme.unlockCost
                    existing.add(theme.name)
                    prefs[Keys.UNLOCKED_THEMES] = existing.joinToString(",")
                    prefs[Keys.SELECTED_THEME] = theme.name
                    unlocked = true
                }
            }
        }
        return unlocked
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
    }

    suspend fun setUnlockAllMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.UNLOCK_ALL_MODE] = enabled }
    }

    suspend fun resetAllProgress() {
        context.dataStore.edit { prefs ->
            prefs[Keys.HIGHEST_UNLOCKED] = 1
            prefs.remove(Keys.LEVEL_STARS)
            prefs.remove(Keys.LEVEL_BEST_MOVES)
            prefs.remove(Keys.TIME_ATTACK_BEST_STREAK)
            prefs.remove(Keys.PRECISION_CLEARS)
            prefs.remove(Keys.ENDLESS_BEST_ROUND)
            prefs.remove(Keys.DAILY_COMPLETED_KEY)
            prefs[Keys.TECH_CREDITS] = 60
            prefs.remove(Keys.UNLOCKED_THEMES)
            prefs[Keys.SELECTED_THEME] = BoardTheme.CYBER_NAVY.name
        }
    }

    private fun parseIntMap(raw: String?): Map<Int, Int> {
        if (raw.isNullOrBlank()) return emptyMap()
        val result = LinkedHashMap<Int, Int>()
        for (entry in raw.split(",")) {
            val parts = entry.split(":")
            if (parts.size == 2) {
                val k = parts[0].toIntOrNull()
                val v = parts[1].toIntOrNull()
                if (k != null && v != null) {
                    result[k] = v
                }
            }
        }
        return result
    }

    private fun serializeIntMap(map: Map<Int, Int>): String {
        return map.entries.sortedBy { it.key }.joinToString(",") { "${it.key}:${it.value}" }
    }
}

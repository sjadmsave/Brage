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
    }

    val progressFlow: Flow<UserProgressState> = context.dataStore.data.map { prefs ->
        UserProgressState(
            highestUnlockedLevel = (prefs[Keys.HIGHEST_UNLOCKED] ?: 1).coerceIn(1, LevelCatalog.TOTAL_LEVELS),
            levelStars = parseIntMap(prefs[Keys.LEVEL_STARS]),
            levelBestMoves = parseIntMap(prefs[Keys.LEVEL_BEST_MOVES]),
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
            unlockAllMode = prefs[Keys.UNLOCK_ALL_MODE] ?: false
        )
    }

    suspend fun recordLevelComplete(levelNumber: Int, starsEarned: Int, movesUsed: Int) {
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
        }
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

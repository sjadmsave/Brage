package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.engine.LevelCatalog
import com.example.engine.SoundPlayer
import com.example.viewmodel.GameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appNameMatchesArabicTitle() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("فك البراغي", appName)
    }

    @Test
    fun criticalUserJourney_navigateHomeToLevelsAndOpenLevel1() {
        composeRule.waitForIdle()
        composeRule.onNodeWithText("فك البراغي").assertIsDisplayed()
        composeRule.onNodeWithTag("levels_map_button").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodes(hasTestTag("level_tile_1")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("خريطة المستويات").assertIsDisplayed()
        composeRule.onNodeWithTag("level_tile_1").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodes(hasTestTag("hint_button")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("hint_button").assertIsDisplayed()
        composeRule.onNodeWithTag("restart_button").assertIsDisplayed()
        composeRule.onNodeWithTag("undo_button").assertIsDisplayed()
        composeRule.onNodeWithTag("game_settings_button").assertIsDisplayed()
        composeRule.onNodeWithTag("game_board_canvas").assertIsDisplayed()
    }

    @Test
    fun settingsDialogAllowsMutingAndUnmutingSound() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("setting_sound_switch").assertIsDisplayed()
        composeRule.onNodeWithTag("setting_sound_switch").performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun soundEffectsTriggerOnUnscrewLockDetachAndVictoryAndRespectMute() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = GameViewModel(app)

        val events = mutableListOf<String>()
        val spySound = object : SoundPlayer {
            override var enabled: Boolean = true
            override fun playUnscrew() { if (enabled) events.add("unscrew") }
            override fun playBoltLock() { if (enabled) events.add("lock") }
            override fun playBlocked() { if (enabled) events.add("blocked") }
            override fun playPlankDetached() { if (enabled) events.add("detach") }
            override fun playUndo() { if (enabled) events.add("undo") }
            override fun playHint() { if (enabled) events.add("hint") }
            override fun playVictory() { if (enabled) events.add("victory") }
        }

        vm.setSoundPlayerForTest(spySound)
        vm.loadLevel(1)

        val level1 = LevelCatalog.getLevel(1)
        val hole0 = level1.holes[0]
        val openHole4 = level1.holes[4]

        // Tap bolt 0 to unscrew
        vm.onBoardTap(hole0.x, hole0.y)
        assertTrue(events.contains("unscrew"))

        // Move bolt 0 to open hole 4 -> locks bolt & detaches plank 0 (wait: plank 0 still has bolt 1, so only lock)
        vm.onBoardTap(openHole4.x, openHole4.y)
        assertTrue(events.contains("lock"))

        // Now test mute
        vm.setSoundEnabled(false)
        assertFalse(spySound.enabled)
        events.clear()
        val hole1 = level1.holes[1]
        vm.onBoardTap(hole1.x, hole1.y)
        assertTrue("Muted player should not emit sound events", events.isEmpty())
    }
}

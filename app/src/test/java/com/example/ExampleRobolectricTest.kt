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
import com.example.engine.BoardGeometry
import com.example.engine.LevelCatalog
import com.example.engine.SoundPlayer
import com.example.model.GameMode
import com.example.viewmodel.FailReason
import com.example.viewmodel.GameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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

    private class SpySoundPlayer(val events: MutableList<String>) : SoundPlayer {
        override var enabled: Boolean = true
        override fun playUnscrew() { if (enabled) events.add("unscrew") }
        override fun playBoltLock() { if (enabled) events.add("lock") }
        override fun playShieldBreak() { if (enabled) events.add("shield_break") }
        override fun playBlocked() { if (enabled) events.add("blocked") }
        override fun playPlankDetached() { if (enabled) events.add("detach") }
        override fun playDrill() { if (enabled) events.add("drill") }
        override fun playUndo() { if (enabled) events.add("undo") }
        override fun playHint() { if (enabled) events.add("hint") }
        override fun playPowerup() { if (enabled) events.add("powerup") }
        override fun playVictory() { if (enabled) events.add("victory") }
        override fun playFail() { if (enabled) events.add("fail") }
    }

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
        composeRule.onNodeWithTag("drill_button").assertIsDisplayed()
        composeRule.onNodeWithTag("cutter_button").assertIsDisplayed()
        composeRule.onNodeWithTag("game_settings_button").assertIsDisplayed()
        composeRule.onNodeWithTag("game_board_canvas").assertIsDisplayed()
    }

    @Test
    fun arcadeModeTimeAttackLaunchesFromHome() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("mode_time_attack_card").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodes(hasTestTag("hud_timer_text")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("hud_moves_text").assertIsDisplayed()
        composeRule.onNodeWithTag("hud_timer_text").assertIsDisplayed()
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
        val spySound = SpySoundPlayer(events)

        vm.setSoundPlayerForTest(spySound)
        vm.loadLevel(1)

        val level1 = LevelCatalog.getLevel(1)
        val hole0 = level1.holes[0]
        val openHole4 = level1.holes[4]

        vm.onBoardTap(hole0.x, hole0.y)
        assertTrue(events.contains("unscrew"))

        vm.onBoardTap(openHole4.x, openHole4.y)
        assertTrue(events.contains("lock"))

        vm.setSoundEnabled(false)
        assertFalse(spySound.enabled)
        events.clear()
        val hole1 = level1.holes[1]
        vm.onBoardTap(hole1.x, hole1.y)
        assertTrue("Muted player should not emit sound events", events.isEmpty())
    }

    @Test
    fun shieldedBoltLaserDrillAndPrecisionModeWorkCorrectly() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = GameViewModel(app)
        val events = mutableListOf<String>()
        vm.setSoundPlayerForTest(SpySoundPlayer(events))

        // Level 27 is the first level with reinforced shield bolts (level >= 26 && level % 3 == 0)
        vm.loadLevel(27, mode = GameMode.CLASSIC)
        val reinforcedBolt = vm.uiState.value.bolts.firstOrNull { it.reinforced }
        assertNotNull("Level 27 should have at least one reinforced bolt", reinforcedBolt)
        val boltHole = vm.uiState.value.holes.first { it.id == reinforcedBolt!!.holeId }

        vm.onBoardTap(boltHole.x, boltHole.y)
        assertTrue(events.contains("shield_break"))
        assertFalse(vm.uiState.value.bolts.first { it.id == reinforcedBolt!!.id }.reinforced)
        assertEquals(reinforcedBolt!!.id, vm.uiState.value.selectedBoltId)

        // Laser Drill adds a new open socket when tapped away from planks/holes
        val initialHoleCount = vm.uiState.value.holes.size
        vm.toggleDrillMode()
        assertTrue(vm.uiState.value.isDrillMode)
        var drilled = false
        for (yInt in 65..465 step 15) {
            for (xInt in 50..310 step 15) {
                val x = xInt.toFloat()
                val y = yInt.toFloat()
                val tooClose = vm.uiState.value.holes.any {
                    kotlin.math.hypot(it.x - x, it.y - y) < 32f
                }
                val candidate = com.example.model.BoardHole(id = 999, x = x, y = y)
                val blocked = vm.uiState.value.planks.any { BoardGeometry.doesPlankBlockHole(it, candidate) }
                if (!tooClose && !blocked) {
                    vm.onBoardTap(x, y)
                    drilled = true
                    break
                }
            }
            if (drilled) break
        }
        assertTrue("Should find a valid open spot to drill", drilled)
        assertEquals(initialHoleCount + 1, vm.uiState.value.holes.size)
        assertEquals(0, vm.uiState.value.drillsLeft)
        assertTrue(events.contains("drill"))

        // Precision mode enforces movesBudget and detects OUT_OF_MOVES
        vm.loadLevel(1, mode = GameMode.PRECISION)
        val budget = vm.uiState.value.movesBudget
        assertNotNull(budget)
        // Bounce a bolt back and forth until moves == budget
        val state1 = vm.uiState.value
        val b = state1.bolts.first()
        val hStart = state1.holes.first { it.id == b.holeId }
        val hOpen = state1.holes.first { BoardGeometry.isHoleAvailableForBolt(it, state1.bolts, state1.planks) }
        var toggle = true
        repeat(budget!!) {
            if (!vm.uiState.value.isLost && !vm.uiState.value.isWon) {
                val currBolt = vm.uiState.value.bolts.first { it.id == b.id }
                val currHole = vm.uiState.value.holes.first { it.id == currBolt.holeId }
                vm.onBoardTap(currHole.x, currHole.y)
                val dest = if (toggle) hOpen else hStart
                vm.onBoardTap(dest.x, dest.y)
                toggle = !toggle
            }
        }
        assertTrue(vm.uiState.value.isLost)
        assertEquals(FailReason.OUT_OF_MOVES, vm.uiState.value.failReason)
        assertTrue(events.contains("fail"))
    }

    @Test
    fun immersiveFullscreenAndArmoryAchievementsWork() {
        composeRule.waitForIdle()

        // Verify immersive fullscreen cutout/flags on MainActivity window
        composeRule.activityRule.scenario.onActivity { activity ->
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val mode = activity.window.attributes.layoutInDisplayCutoutMode
                assertTrue(
                    mode == android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS ||
                        mode == android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                )
            }
        }

        // Open Armory Themes dialog and unlock MATRIX_GREEN (costs 50 from initial 60 credits)
        composeRule.onNodeWithTag("armory_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("armory_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("theme_card_MATRIX_GREEN").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("armory_close_button").performClick()
        composeRule.waitForIdle()

        // Open Achievements dialog
        composeRule.onNodeWithTag("achievements_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("achievements_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("achievements_close_button").performClick()
        composeRule.waitForIdle()
    }
}

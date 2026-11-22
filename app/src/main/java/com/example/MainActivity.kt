package com.example

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.model.GameMode
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LevelSelectScreen
import com.example.ui.theme.WoodBoltsTheme
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.HomeViewModel

object AppRoutes {
    const val HOME = "home"
    const val LEVEL_SELECT = "level_select"
    const val GAME = "game/{mode}/{levelNumber}"

    fun gameRoute(levelNumber: Int, mode: GameMode = GameMode.CLASSIC): String =
        "game/${mode.name}/$levelNumber"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFullImmersiveMode()
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                WoodBoltsTheme {
                    val navController = rememberNavController()
                    val homeViewModel: HomeViewModel = viewModel()
                    val progress by homeViewModel.progressState.collectAsStateWithLifecycle()

                    NavHost(
                        navController = navController,
                        startDestination = AppRoutes.HOME
                    ) {
                        composable(AppRoutes.HOME) {
                            HomeScreen(
                                progress = progress,
                                onPlayLevel = { level ->
                                    navController.navigate(AppRoutes.gameRoute(level, GameMode.CLASSIC))
                                },
                                onStartMode = { mode, level ->
                                    navController.navigate(AppRoutes.gameRoute(level, mode))
                                },
                                onOpenLevelSelect = {
                                    navController.navigate(AppRoutes.LEVEL_SELECT)
                                },
                                onUnlockOrSelectTheme = homeViewModel::unlockOrSelectTheme,
                                onToggleSound = homeViewModel::setSoundEnabled,
                                onToggleHaptics = homeViewModel::setHapticsEnabled,
                                onToggleUnlockAll = homeViewModel::setUnlockAllMode,
                                onResetProgress = homeViewModel::resetProgress
                            )
                        }

                        composable(AppRoutes.LEVEL_SELECT) {
                            LevelSelectScreen(
                                progress = progress,
                                onSelectLevel = { level ->
                                    navController.navigate(AppRoutes.gameRoute(level, GameMode.CLASSIC))
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = AppRoutes.GAME,
                            arguments = listOf(
                                navArgument("mode") { type = NavType.StringType },
                                navArgument("levelNumber") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val modeName = backStackEntry.arguments?.getString("mode") ?: GameMode.CLASSIC.name
                            val mode = GameMode.entries.firstOrNull { it.name == modeName } ?: GameMode.CLASSIC
                            val levelNumber = backStackEntry.arguments?.getInt("levelNumber") ?: 1
                            val gameViewModel: GameViewModel = viewModel()
                            GameScreen(
                                mode = mode,
                                levelNumber = levelNumber,
                                viewModel = gameViewModel,
                                progress = progress,
                                onToggleSound = homeViewModel::setSoundEnabled,
                                onToggleHaptics = homeViewModel::setHapticsEnabled,
                                onToggleUnlockAll = homeViewModel::setUnlockAllMode,
                                onResetProgress = homeViewModel::resetProgress,
                                onBack = { navController.popBackStack() },
                                onOpenLevels = {
                                    navController.navigate(AppRoutes.LEVEL_SELECT) {
                                        popUpTo(AppRoutes.HOME)
                                    }
                                },
                                onPlayLevel = { nextLevel ->
                                    navController.navigate(AppRoutes.gameRoute(nextLevel, mode)) {
                                        popUpTo(AppRoutes.GAME) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBarsSticky()
        }
    }

    private fun applyFullImmersiveMode() {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                } else {
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }
        }
        hideSystemBarsSticky()
    }

    private fun hideSystemBarsSticky() {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }
}

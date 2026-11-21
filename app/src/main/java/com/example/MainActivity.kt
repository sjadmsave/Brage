package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LevelSelectScreen
import com.example.ui.theme.WoodBoltsTheme
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.HomeViewModel

object AppRoutes {
    const val HOME = "home"
    const val LEVEL_SELECT = "level_select"
    const val GAME = "game/{levelNumber}"

    fun gameRoute(levelNumber: Int): String = "game/$levelNumber"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
                                    navController.navigate(AppRoutes.gameRoute(level))
                                },
                                onOpenLevelSelect = {
                                    navController.navigate(AppRoutes.LEVEL_SELECT)
                                },
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
                                    navController.navigate(AppRoutes.gameRoute(level))
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = AppRoutes.GAME,
                            arguments = listOf(navArgument("levelNumber") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val levelNumber = backStackEntry.arguments?.getInt("levelNumber") ?: 1
                            val gameViewModel: GameViewModel = viewModel()
                            GameScreen(
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
                                    navController.navigate(AppRoutes.gameRoute(nextLevel)) {
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
}

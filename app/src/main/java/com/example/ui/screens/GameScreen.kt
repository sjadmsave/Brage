package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.engine.LevelCatalog
import com.example.model.GameMode
import com.example.model.UserProgressState
import com.example.ui.components.FailDialog
import com.example.ui.components.LevelCompleteDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TierBadge
import com.example.ui.components.WoodBoardCanvas
import com.example.ui.components.WorkshopActionChip
import com.example.ui.components.formatTime
import com.example.ui.theme.BrassBright
import com.example.ui.theme.ChakraPetchFontFamily
import com.example.ui.theme.CopperAccent
import com.example.ui.theme.CrimsonBlocked
import com.example.ui.theme.EmeraldValid
import com.example.ui.theme.SandText
import com.example.ui.theme.SkyHint
import com.example.ui.theme.WalnutCard
import com.example.ui.theme.WalnutDark
import com.example.ui.theme.WalnutMedium
import com.example.ui.theme.WalnutSurface
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.HapticEvent

@Composable
fun GameScreen(
    mode: GameMode = GameMode.CLASSIC,
    levelNumber: Int,
    viewModel: GameViewModel,
    progress: UserProgressState,
    onToggleSound: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onToggleUnlockAll: (Boolean) -> Unit,
    onResetProgress: () -> Unit,
    onBack: () -> Unit,
    onOpenLevels: () -> Unit,
    onPlayLevel: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    var showSettings by remember { mutableStateOf(false) }

    LaunchedEffect(mode, levelNumber) {
        viewModel.loadLevel(levelNumber, mode = mode, keepStreak = false)
    }

    LaunchedEffect(Unit) {
        viewModel.hapticEvents.collect { event ->
            when (event) {
                HapticEvent.LIGHT_TAP -> haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                HapticEvent.SUCCESS -> haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                HapticEvent.BLOCKED, HapticEvent.PLANK_DROP, HapticEvent.VICTORY ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    }

    val headerTitle = when (uiState.mode) {
        GameMode.CLASSIC -> "المستوى ${uiState.levelNumber}"
        GameMode.TIME_ATTACK -> "سباق الزمن • ${uiState.levelNumber}"
        GameMode.PRECISION -> "تحدي الدقة • ${uiState.levelNumber}"
        GameMode.ENDLESS -> "اللانهائي • جولة ${uiState.levelNumber}"
        GameMode.DAILY -> "التحدي اليومي"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.img_tech_workshop_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            WalnutDark.copy(alpha = 0.72f),
                            WalnutMedium.copy(alpha = 0.60f),
                            WalnutDark.copy(alpha = 0.90f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top HUD
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(WalnutCard)
                        .testTag("game_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "رجوع",
                        tint = BrassBright
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = headerTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = BrassBright
                        )
                        if (uiState.mode == GameMode.TIME_ATTACK && uiState.sessionStreak > 0) {
                            Surface(
                                color = CopperAccent.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, CopperAccent.copy(alpha = 0.75f))
                            ) {
                                Text(
                                    text = "🔥 ${uiState.sessionStreak}",
                                    color = CopperAccent,
                                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = ChakraPetchFontFamily),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        } else {
                            TierBadge(tier = uiState.tier)
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val budget = uiState.movesBudget
                        val movesText = if (budget != null) {
                            "الحركات: ${uiState.moves}/$budget"
                        } else {
                            "الحركات: ${uiState.moves}"
                        }
                        val movesColor = if (budget != null && budget - uiState.moves <= 2) {
                            CrimsonBlocked
                        } else {
                            SandText
                        }

                        Text(
                            text = movesText,
                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = ChakraPetchFontFamily),
                            color = movesColor,
                            modifier = Modifier.testTag("hud_moves_text")
                        )
                        Text(
                            text = "الألواح: ${uiState.activePlanksCount}/${uiState.totalPlanksCount}",
                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = ChakraPetchFontFamily),
                            color = SandText
                        )

                        val rem = uiState.remainingSeconds
                        if (rem != null) {
                            val timeColor = if (rem <= 10) CrimsonBlocked else CopperAccent
                            Text(
                                text = "⏱ ${formatTime(rem)}",
                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = ChakraPetchFontFamily),
                                color = timeColor,
                                modifier = Modifier.testTag("hud_timer_text")
                            )
                        } else {
                            Text(
                                text = formatTime(uiState.elapsedSeconds),
                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = ChakraPetchFontFamily),
                                color = SandText,
                                modifier = Modifier.testTag("hud_timer_text")
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = viewModel::toggleSound,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(WalnutCard)
                            .testTag("game_sound_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.soundEnabled) {
                                Icons.AutoMirrored.Rounded.VolumeUp
                            } else {
                                Icons.AutoMirrored.Rounded.VolumeOff
                            },
                            contentDescription = "تبديل الصوت",
                            tint = if (uiState.soundEnabled) BrassBright else SandText
                        )
                    }
                    IconButton(
                        onClick = { showSettings = true },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(WalnutCard)
                            .testTag("game_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "الإعدادات",
                            tint = BrassBright
                        )
                    }
                }
            }

            // Contextual status / combo / tutorial / hint banner
            val showTutorial = uiState.mode == GameMode.CLASSIC && uiState.levelNumber == 1 && uiState.moves == 0
            val showBanner = uiState.statusMessage != null ||
                uiState.hintMove != null ||
                uiState.hintUnavailable ||
                showTutorial

            AnimatedVisibility(visible = showBanner) {
                val (bannerText, bannerColor) = when {
                    uiState.statusMessage != null -> (uiState.statusMessage ?: "") to EmeraldValid
                    uiState.hintMove != null -> "اتبع المسار المضيء: انقل البرغي إلى المنفذ المحدد" to SkyHint
                    uiState.hintUnavailable -> "جرّب التراجع خطوة أو إعادة المحاولة لحل أسرع" to CrimsonBlocked
                    else -> "اضغط على أي برغي لفكّه، ثم اضغط على منفذ متاح" to BrassBright
                }
                Surface(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .widthIn(max = 560.dp),
                    color = WalnutSurface.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, bannerColor.copy(alpha = 0.65f))
                ) {
                    Text(
                        text = bannerText,
                        color = bannerColor,
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Cyber Chassis Board Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                contentAlignment = Alignment.Center
            ) {
                WoodBoardCanvas(
                    uiState = uiState,
                    onBoardTap = viewModel::onBoardTap,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Action Bar (Undo, Restart, Hint, Laser Drill, Pulse Cutter)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                WorkshopActionChip(
                    icon = Icons.AutoMirrored.Rounded.Undo,
                    label = "تراجع",
                    enabled = uiState.canUndo && !uiState.isWon && !uiState.isLost,
                    onClick = viewModel::undoMove,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("undo_button")
                )
                WorkshopActionChip(
                    icon = Icons.Rounded.Refresh,
                    label = "إعادة",
                    enabled = !uiState.isWon,
                    onClick = viewModel::restartLevel,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("restart_button")
                )
                WorkshopActionChip(
                    icon = Icons.Rounded.Lightbulb,
                    label = if (uiState.isComputingHint) "تحليل" else "تلميح",
                    enabled = !uiState.isWon && !uiState.isLost && !uiState.isComputingHint,
                    isActive = uiState.hintMove != null,
                    onClick = viewModel::requestHint,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("hint_button")
                )
                val canRefillDrill = uiState.drillsLeft == 0 &&
                    uiState.mode != GameMode.PRECISION &&
                    uiState.techCredits >= com.example.viewmodel.GameUiState.DRILL_REFILL_COST
                WorkshopActionChip(
                    icon = Icons.Rounded.Build,
                    label = when {
                        uiState.drillsLeft > 0 -> "مثقاب (${uiState.drillsLeft})"
                        uiState.mode != GameMode.PRECISION -> "مثقاب 50⚡"
                        else -> "مثقاب"
                    },
                    enabled = !uiState.isWon && !uiState.isLost && (uiState.drillsLeft > 0 || canRefillDrill),
                    isActive = uiState.isDrillMode,
                    onClick = viewModel::toggleDrillMode,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("drill_button")
                )
                val canRefillCutter = uiState.cuttersLeft == 0 &&
                    (uiState.mode == GameMode.CLASSIC || uiState.mode == GameMode.ENDLESS || uiState.mode == GameMode.DAILY) &&
                    uiState.techCredits >= com.example.viewmodel.GameUiState.CUTTER_REFILL_COST
                WorkshopActionChip(
                    icon = Icons.Rounded.ContentCut,
                    label = when {
                        uiState.cuttersLeft > 0 -> "قاطع (${uiState.cuttersLeft})"
                        uiState.mode == GameMode.CLASSIC || uiState.mode == GameMode.ENDLESS || uiState.mode == GameMode.DAILY -> "قاطع 75⚡"
                        else -> "قاطع"
                    },
                    enabled = !uiState.isWon && !uiState.isLost && (uiState.cuttersLeft > 0 || canRefillCutter),
                    isActive = uiState.isCutterMode,
                    onClick = viewModel::toggleCutterMode,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("cutter_button")
                )
            }
        }

        if (uiState.isWon) {
            val isFinal = uiState.mode == GameMode.CLASSIC && uiState.levelNumber >= LevelCatalog.TOTAL_LEVELS
            LevelCompleteDialog(
                mode = uiState.mode,
                levelNumber = uiState.levelNumber,
                starsEarned = uiState.starsEarned,
                moves = uiState.moves,
                elapsedSeconds = uiState.elapsedSeconds,
                sessionStreak = uiState.sessionStreak,
                creditsEarned = uiState.lastCreditsEarned,
                isFinalLevel = isFinal,
                onNextLevel = {
                    when (uiState.mode) {
                        GameMode.CLASSIC -> {
                            val next = (uiState.levelNumber + 1).coerceAtMost(LevelCatalog.TOTAL_LEVELS)
                            onPlayLevel(next)
                        }
                        GameMode.TIME_ATTACK, GameMode.PRECISION, GameMode.ENDLESS -> {
                            viewModel.loadLevel(
                                levelNumber = uiState.levelNumber + 1,
                                mode = uiState.mode,
                                keepStreak = (uiState.mode == GameMode.TIME_ATTACK)
                            )
                        }
                        GameMode.DAILY -> onBack()
                    }
                },
                onReplay = viewModel::restartLevel,
                onLevelsMap = {
                    if (uiState.mode == GameMode.CLASSIC) {
                        onOpenLevels()
                    } else {
                        onBack()
                    }
                }
            )
        }

        if (uiState.isLost) {
            FailDialog(
                failReason = uiState.failReason,
                mode = uiState.mode,
                levelNumber = uiState.levelNumber,
                moves = uiState.moves,
                onRetry = viewModel::restartLevel,
                onHome = onBack
            )
        }

        if (showSettings) {
            SettingsDialog(
                soundEnabled = progress.soundEnabled,
                hapticsEnabled = progress.hapticsEnabled,
                unlockAllMode = progress.unlockAllMode,
                onToggleSound = onToggleSound,
                onToggleHaptics = onToggleHaptics,
                onToggleUnlockAll = onToggleUnlockAll,
                onResetProgress = onResetProgress,
                onDismiss = { showSettings = false }
            )
        }
    }
}

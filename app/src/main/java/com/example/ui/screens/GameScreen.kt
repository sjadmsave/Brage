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
import androidx.compose.material.icons.rounded.GridView
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
import com.example.model.UserProgressState
import com.example.ui.components.LevelCompleteDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TierBadge
import com.example.ui.components.WoodBoardCanvas
import com.example.ui.components.WorkshopActionChip
import com.example.ui.components.formatTime
import com.example.ui.theme.BrassBright
import com.example.ui.theme.ChakraPetchFontFamily
import com.example.ui.theme.CrimsonBlocked
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

    LaunchedEffect(levelNumber) {
        viewModel.loadLevel(levelNumber)
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
                            text = "المستوى ${uiState.levelNumber}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = BrassBright
                        )
                        TierBadge(tier = uiState.tier)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الحركات: ${uiState.moves}",
                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = ChakraPetchFontFamily),
                            color = SandText
                        )
                        Text(
                            text = "الألواح: ${uiState.activePlanksCount}/${uiState.totalPlanksCount}",
                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = ChakraPetchFontFamily),
                            color = SandText
                        )
                        Text(
                            text = formatTime(uiState.elapsedSeconds),
                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = ChakraPetchFontFamily),
                            color = SandText
                        )
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

            // Contextual helper banner
            AnimatedVisibility(
                visible = uiState.hintMove != null || uiState.hintUnavailable || (uiState.levelNumber == 1 && uiState.moves == 0)
            ) {
                val (bannerText, bannerColor) = when {
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

            // Bottom Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WorkshopActionChip(
                    icon = Icons.AutoMirrored.Rounded.Undo,
                    label = "تراجع",
                    enabled = uiState.canUndo && !uiState.isWon,
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
                    label = if (uiState.isComputingHint) "تحليل..." else "تلميح",
                    enabled = !uiState.isWon && !uiState.isComputingHint,
                    isActive = uiState.hintMove != null,
                    onClick = viewModel::requestHint,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("hint_button")
                )
                WorkshopActionChip(
                    icon = Icons.Rounded.GridView,
                    label = "المستويات",
                    onClick = onOpenLevels,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("game_levels_button")
                )
            }
        }

        if (uiState.isWon) {
            val isFinal = uiState.levelNumber >= LevelCatalog.TOTAL_LEVELS
            LevelCompleteDialog(
                levelNumber = uiState.levelNumber,
                starsEarned = uiState.starsEarned,
                moves = uiState.moves,
                elapsedSeconds = uiState.elapsedSeconds,
                isFinalLevel = isFinal,
                onNextLevel = {
                    val next = (uiState.levelNumber + 1).coerceAtMost(LevelCatalog.TOTAL_LEVELS)
                    onPlayLevel(next)
                },
                onReplay = viewModel::restartLevel,
                onLevelsMap = onOpenLevels
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

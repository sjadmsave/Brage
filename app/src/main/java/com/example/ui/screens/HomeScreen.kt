package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.engine.LevelCatalog
import com.example.model.DifficultyTier
import com.example.model.UserProgressState
import com.example.ui.components.BrassPrimaryButton
import com.example.ui.components.HowToPlayDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TierBadge
import com.example.ui.theme.BrassBright
import com.example.ui.theme.BrassGold
import com.example.ui.theme.ChakraPetchFontFamily
import com.example.ui.theme.CopperAccent
import com.example.ui.theme.CreamText
import com.example.ui.theme.EmeraldValid
import com.example.ui.theme.OakBorder
import com.example.ui.theme.SandText
import com.example.ui.theme.WalnutCard
import com.example.ui.theme.WalnutDark
import com.example.ui.theme.WalnutMedium
import com.example.ui.theme.WalnutSurface

@Composable
fun HomeScreen(
    progress: UserProgressState,
    onPlayLevel: (Int) -> Unit,
    onOpenLevelSelect: () -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onToggleUnlockAll: (Boolean) -> Unit,
    onResetProgress: () -> Unit
) {
    var showHowToPlay by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    val currentLevel = progress.highestUnlockedLevel.coerceIn(1, LevelCatalog.TOTAL_LEVELS)
    val currentTier = DifficultyTier.forLevel(currentLevel)

    Box(modifier = Modifier.fillMaxSize()) {
        // Cyber-tech circuit background
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
                            WalnutDark.copy(alpha = 0.65f),
                            WalnutMedium.copy(alpha = 0.55f),
                            WalnutDark.copy(alpha = 0.88f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar: Offline badge + Help & Settings buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = WalnutSurface.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, EmeraldValid.copy(alpha = 0.55f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WifiOff,
                            contentDescription = null,
                            tint = EmeraldValid,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "يعمل بدون إنترنت",
                            color = EmeraldValid,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { showHowToPlay = true },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(WalnutCard.copy(alpha = 0.9f))
                            .testTag("how_to_play_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                            contentDescription = "طريقة اللعب",
                            tint = BrassBright
                        )
                    }
                    IconButton(
                        onClick = { showSettings = true },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(WalnutCard.copy(alpha = 0.9f))
                            .testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "الإعدادات",
                            tint = BrassBright
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Hero Emblem & Game Title
            Column(
                modifier = Modifier.widthIn(max = 560.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(88.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = WalnutSurface,
                    border = BorderStroke(2.dp, BrassGold),
                    shadowElevation = 12.dp
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_tech_bolt),
                        contentDescription = "شعار لعبة فك البراغي",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "فك البراغي",
                    style = MaterialTheme.typography.displayLarge,
                    color = BrassBright,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "الألغاز الرقمية للألواح • 100 مستوى تقني",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SandText,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cyber Progress Telemetry Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp),
                color = WalnutMedium.copy(alpha = 0.92f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.2.dp, OakBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "حالة التقدم",
                                style = MaterialTheme.typography.titleMedium,
                                color = CreamText
                            )
                            Text(
                                text = "المستوى الحالي: $currentLevel / 100",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SandText
                            )
                        }
                        TierBadge(tier = currentTier)
                    }

                    LinearProgressIndicator(
                        progress = { progress.completedCount / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                        color = BrassGold,
                        trackColor = WalnutDark
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TechStatPill(
                            icon = Icons.Rounded.Star,
                            iconTint = CopperAccent,
                            value = "${progress.totalStars} / 300",
                            label = "النجوم",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        TechStatPill(
                            icon = Icons.Rounded.Memory,
                            iconTint = BrassBright,
                            value = "${progress.completedCount} / 100",
                            label = "مكتمل",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Primary Play CTA & Level Map Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BrassPrimaryButton(
                    text = if (currentLevel == 1 && progress.completedCount == 0) {
                        "ابدأ اللعب — المستوى 1"
                    } else {
                        "متابعة اللعب — المستوى $currentLevel"
                    },
                    icon = Icons.Rounded.PlayArrow,
                    onClick = { onPlayLevel(currentLevel) },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "play_continue_button"
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onOpenLevelSelect)
                        .testTag("levels_map_button"),
                    color = WalnutCard.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.2.dp, BrassGold.copy(alpha = 0.5f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.GridView,
                            contentDescription = null,
                            tint = BrassBright,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "خريطة المستويات (1 - 100)",
                            color = CreamText,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (showHowToPlay) {
        HowToPlayDialog(onDismiss = { showHowToPlay = false })
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

@Composable
private fun TechStatPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = WalnutSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, OakBorder.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
            Column {
                Text(
                    text = value,
                    color = CreamText,
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = ChakraPetchFontFamily)
                )
                Text(
                    text = label,
                    color = SandText,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

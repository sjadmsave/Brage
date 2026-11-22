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
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Timer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.engine.LevelCatalog
import com.example.model.BoardTheme
import com.example.model.DifficultyTier
import com.example.model.GameMode
import com.example.model.UserProgressState
import com.example.ui.components.AchievementsDialog
import com.example.ui.components.ArmoryThemesDialog
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
import com.example.ui.theme.TierExpertColor
import com.example.ui.theme.TierHardColor
import com.example.ui.theme.TierMasterColor
import com.example.ui.theme.WalnutCard
import com.example.ui.theme.WalnutDark
import com.example.ui.theme.WalnutMedium
import com.example.ui.theme.WalnutSurface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    progress: UserProgressState,
    onPlayLevel: (Int) -> Unit,
    onStartMode: (GameMode, Int) -> Unit,
    onOpenLevelSelect: () -> Unit,
    onUnlockOrSelectTheme: (BoardTheme) -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onToggleUnlockAll: (Boolean) -> Unit,
    onResetProgress: () -> Unit
) {
    var showHowToPlay by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showArmory by remember { mutableStateOf(false) }
    var showAchievements by remember { mutableStateOf(false) }

    val currentLevel = progress.highestUnlockedLevel.coerceIn(1, LevelCatalog.TOTAL_LEVELS)
    val currentTier = DifficultyTier.forLevel(currentLevel)
    val maxStars = LevelCatalog.TOTAL_LEVELS * 3
    val todayKey = remember {
        SimpleDateFormat("yyyyMMdd", Locale.US).format(Date()).toIntOrNull() ?: 20261121
    }
    val isDailyDone = progress.dailyCompletedKey == todayKey

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
                            WalnutDark.copy(alpha = 0.62f),
                            WalnutMedium.copy(alpha = 0.54f),
                            WalnutDark.copy(alpha = 0.90f)
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
            // Top bar: Offline & Credits badges + Help & Settings buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = WalnutSurface.copy(alpha = 0.88f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, EmeraldValid.copy(alpha = 0.55f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WifiOff,
                                contentDescription = null,
                                tint = EmeraldValid,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "أوفلاين",
                                color = EmeraldValid,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    Surface(
                        color = WalnutSurface.copy(alpha = 0.88f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CopperAccent.copy(alpha = 0.65f))
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("home_credits_badge"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Bolt,
                                contentDescription = null,
                                tint = CopperAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${progress.techCredits}",
                                color = CopperAccent,
                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = ChakraPetchFontFamily)
                            )
                        }
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

            Spacer(modifier = Modifier.height(10.dp))

            // Hero Emblem & Game Title
            Column(
                modifier = Modifier.widthIn(max = 560.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(78.dp),
                    shape = RoundedCornerShape(20.dp),
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

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "فك البراغي",
                    style = MaterialTheme.typography.displayLarge,
                    color = BrassBright,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "الألغاز الرقمية للألواح • ${LevelCatalog.TOTAL_LEVELS} مستوى و5 أنماط لعب",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SandText,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

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
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "الطور الكلاسيكي",
                                style = MaterialTheme.typography.titleMedium,
                                color = CreamText
                            )
                            Text(
                                text = "المستوى الحالي: $currentLevel / ${LevelCatalog.TOTAL_LEVELS}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SandText
                            )
                        }
                        TierBadge(tier = currentTier)
                    }

                    LinearProgressIndicator(
                        progress = { progress.completedCount / LevelCatalog.TOTAL_LEVELS.toFloat() },
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
                            value = "${progress.totalStars} / $maxStars",
                            label = "النجوم",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        TechStatPill(
                            icon = Icons.Rounded.Memory,
                            iconTint = BrassBright,
                            value = "${progress.completedCount} / ${LevelCatalog.TOTAL_LEVELS}",
                            label = "مكتمل",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Play CTA & Level Map + Armory/Achievements Row
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        .height(52.dp)
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
                            text = "خريطة المستويات (1 - ${LevelCatalog.TOTAL_LEVELS})",
                            color = CreamText,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                // Armory Themes & Achievements Quick Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showArmory = true }
                            .testTag("armory_button"),
                        color = WalnutSurface.copy(alpha = 0.94f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, progress.selectedTheme.accentColor.copy(alpha = 0.6f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Palette,
                                contentDescription = null,
                                tint = progress.selectedTheme.accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "سمات اللوحة",
                                color = CreamText,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showAchievements = true }
                            .testTag("achievements_button"),
                        color = WalnutSurface.copy(alpha = 0.94f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, CopperAccent.copy(alpha = 0.6f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.EmojiEvents,
                                contentDescription = null,
                                tint = CopperAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "الأوسمة",
                                color = CreamText,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Daily Challenge Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onStartMode(GameMode.DAILY, 1) }
                    .testTag("daily_challenge_card"),
                color = WalnutSurface.copy(alpha = 0.95f),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(
                    1.4.dp,
                    if (isDailyDone) EmeraldValid.copy(alpha = 0.8f) else BrassGold.copy(alpha = 0.75f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isDailyDone) EmeraldValid.copy(alpha = 0.16f)
                                    else BrassGold.copy(alpha = 0.16f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isDailyDone) Icons.Rounded.CheckCircle else Icons.Rounded.CalendarToday,
                                contentDescription = null,
                                tint = if (isDailyDone) EmeraldValid else BrassBright,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = GameMode.DAILY.titleAr,
                                style = MaterialTheme.typography.titleMedium,
                                color = CreamText
                            )
                            Text(
                                text = if (isDailyDone) "تم إنجاز تحدي اليوم بنجاح ✓" else GameMode.DAILY.subtitleAr,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDailyDone) EmeraldValid else SandText
                            )
                        }
                    }

                    Surface(
                        color = if (isDailyDone) EmeraldValid.copy(alpha = 0.15f) else BrassGold.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (!isDailyDone) {
                                Icon(
                                    imageVector = Icons.Rounded.Bolt,
                                    contentDescription = null,
                                    tint = BrassBright,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = if (isDailyDone) "إعادة" else "العب الآن",
                                color = if (isDailyDone) EmeraldValid else BrassBright,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Arcade Gameplay Modes Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "طرائق اللعب الإضافية",
                    style = MaterialTheme.typography.titleMedium,
                    color = BrassBright,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                ArcadeModeCard(
                    mode = GameMode.TIME_ATTACK,
                    icon = Icons.Rounded.Timer,
                    accent = TierHardColor,
                    statBadge = "أفضل سلسلة: ${progress.timeAttackBestStreak}",
                    testTag = "mode_time_attack_card",
                    onClick = { onStartMode(GameMode.TIME_ATTACK, 1) }
                )

                ArcadeModeCard(
                    mode = GameMode.PRECISION,
                    icon = Icons.Rounded.GpsFixed,
                    accent = TierExpertColor,
                    statBadge = "انتصارات الدقة: ${progress.precisionClears}",
                    testTag = "mode_precision_card",
                    onClick = { onStartMode(GameMode.PRECISION, 1) }
                )

                ArcadeModeCard(
                    mode = GameMode.ENDLESS,
                    icon = Icons.Rounded.AllInclusive,
                    accent = TierMasterColor,
                    statBadge = "أعلى جولة: ${progress.endlessBestRound}",
                    testTag = "mode_endless_card",
                    onClick = { onStartMode(GameMode.ENDLESS, 1) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
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
    if (showArmory) {
        ArmoryThemesDialog(
            progress = progress,
            onUnlockOrSelectTheme = onUnlockOrSelectTheme,
            onDismiss = { showArmory = false }
        )
    }
    if (showAchievements) {
        AchievementsDialog(
            progress = progress,
            onDismiss = { showAchievements = false }
        )
    }
}

@Composable
private fun ArcadeModeCard(
    mode: GameMode,
    icon: ImageVector,
    accent: Color,
    statBadge: String,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        color = WalnutMedium.copy(alpha = 0.92f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, accent.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mode.titleAr,
                    style = MaterialTheme.typography.titleMedium,
                    color = CreamText
                )
                Text(
                    text = mode.subtitleAr,
                    style = MaterialTheme.typography.bodySmall,
                    color = SandText
                )
            }

            Surface(
                color = WalnutSurface,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.45f))
            ) {
                Text(
                    text = statBadge,
                    color = accent,
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = ChakraPetchFontFamily),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun TechStatPill(
    icon: ImageVector,
    iconTint: Color,
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

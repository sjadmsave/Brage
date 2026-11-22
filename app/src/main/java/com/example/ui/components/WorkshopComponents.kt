package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Palette
import com.example.model.BoardTheme
import com.example.model.DifficultyTier
import com.example.model.GameMode
import com.example.model.UserProgressState
import com.example.ui.theme.BrassBright
import com.example.ui.theme.BrassDark
import com.example.ui.theme.BrassGold
import com.example.ui.theme.ChakraPetchFontFamily
import com.example.ui.theme.CopperAccent
import com.example.ui.theme.CreamText
import com.example.ui.theme.CrimsonBlocked
import com.example.ui.theme.EmeraldValid
import com.example.ui.theme.OakBorder
import com.example.ui.theme.SandText
import com.example.ui.theme.TierBeginnerColor
import com.example.ui.theme.TierExpertColor
import com.example.ui.theme.TierHardColor
import com.example.ui.theme.TierMasterColor
import com.example.ui.theme.TierMediumColor
import com.example.ui.theme.TierOmegaColor
import com.example.ui.theme.TierQuantumColor
import com.example.ui.theme.WalnutCard
import com.example.ui.theme.WalnutDark
import com.example.ui.theme.WalnutMedium
import com.example.ui.theme.WalnutSurface
import com.example.viewmodel.FailReason

fun DifficultyTier.accentColor(): Color = when (this) {
    DifficultyTier.BEGINNER -> TierBeginnerColor
    DifficultyTier.MEDIUM -> TierMediumColor
    DifficultyTier.HARD -> TierHardColor
    DifficultyTier.EXPERT -> TierExpertColor
    DifficultyTier.MASTER -> TierMasterColor
    DifficultyTier.QUANTUM -> TierQuantumColor
    DifficultyTier.OMEGA -> TierOmegaColor
}

@Composable
fun TierBadge(
    tier: DifficultyTier,
    modifier: Modifier = Modifier
) {
    val accent = tier.accentColor()
    Surface(
        modifier = modifier,
        color = accent.copy(alpha = 0.16f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.7f))
    ) {
        Text(
            text = tier.titleAr,
            color = accent,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun BrassPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    testTag: String? = null
) {
    val tagMod = if (testTag != null) modifier.testTag(testTag) else modifier
    Box(
        modifier = tagMod
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(BrassDark, BrassGold, BrassBright)
                )
            )
            .border(1.5.dp, Color(0xFFB3F5FF).copy(alpha = 0.75f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = WalnutDark,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = text,
                color = WalnutDark,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun WorkshopActionChip(
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    isActive: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        !enabled -> WalnutSurface.copy(alpha = 0.45f)
        isActive -> BrassGold.copy(alpha = 0.22f)
        else -> WalnutCard
    }
    val borderColor = when {
        !enabled -> OakBorder.copy(alpha = 0.3f)
        isActive -> BrassGold
        else -> OakBorder
    }
    val contentColor = when {
        !enabled -> SandText.copy(alpha = 0.35f)
        isActive -> BrassBright
        else -> CreamText
    }

    Surface(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick),
        color = bgColor,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.2.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1
            )
        }
    }
}

@Composable
fun StarRow(
    stars: Int,
    starSize: Dp = 20.dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..3) {
            Icon(
                imageVector = if (i <= stars) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                contentDescription = null,
                tint = if (i <= stars) CopperAccent else SandText.copy(alpha = 0.32f),
                modifier = Modifier.size(starSize)
            )
        }
    }
}

@Composable
fun HowToPlayDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = WalnutMedium,
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.5.dp, BrassGold.copy(alpha = 0.55f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "بروتوكول اللعب",
                    style = MaterialTheme.typography.headlineMedium,
                    color = BrassBright
                )
                RuleStepItem(
                    step = "01",
                    title = "فك البراغي والدرع الواقي",
                    description = "اضغط على أي برغي لفكّه ونقله لمنفذ أخضر متاح. البراغي المحاطة بدرع أحمر تحتاج ضغطة أولى لكسر الدرع."
                )
                RuleStepItem(
                    step = "02",
                    title = "أدوات الليزر التكتيكية",
                    description = "استخدم المثقاب لحفر منفذ إضافي في أي مساحة فارغة، أو القاطع لتفكيك لوح عنيد فوراً."
                )
                RuleStepItem(
                    step = "03",
                    title = "5 أنماط لعب و300 مستوى",
                    description = "استمتع بـ 300 مستوى في الطور الكلاسيكي أو نافس في سباق الزمن، الدقة، اللانهائي، والتحدي اليومي واجمع الطاقة لتخصيص اللوحة."
                )
                Spacer(modifier = Modifier.height(4.dp))
                BrassPrimaryButton(
                    text = "بدء التشغيل",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "how_to_play_close_button"
                )
            }
        }
    }
}

@Composable
private fun RuleStepItem(step: String, title: String, description: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BrassGold.copy(alpha = 0.18f))
                .border(1.dp, BrassGold, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step,
                color = BrassBright,
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = ChakraPetchFontFamily)
            )
        }
        Column {
            Text(
                text = title,
                color = CreamText,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = description,
                color = SandText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun SettingsDialog(
    soundEnabled: Boolean,
    hapticsEnabled: Boolean,
    unlockAllMode: Boolean,
    onToggleSound: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onToggleUnlockAll: (Boolean) -> Unit,
    onResetProgress: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = WalnutMedium,
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.5.dp, BrassGold.copy(alpha = 0.55f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "إعدادات النظام",
                    style = MaterialTheme.typography.headlineMedium,
                    color = BrassBright
                )
                SettingToggleRow(
                    label = "المؤثرات الصوتية",
                    checked = soundEnabled,
                    onCheckedChange = onToggleSound,
                    testTag = "setting_sound_switch"
                )
                SettingToggleRow(
                    label = "الاهتزاز اللمسي",
                    checked = hapticsEnabled,
                    onCheckedChange = onToggleHaptics,
                    testTag = "setting_haptics_switch"
                )
                SettingToggleRow(
                    label = "وضع حر (فتح الـ 300 مستوى)",
                    checked = unlockAllMode,
                    onCheckedChange = onToggleUnlockAll,
                    testTag = "setting_unlock_all_switch"
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onResetProgress,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "إعادة تعيين التقدم",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrassGold,
                            contentColor = WalnutDark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تم", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(WalnutSurface)
            .border(1.dp, OakBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = CreamText,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = WalnutDark,
                checkedTrackColor = EmeraldValid,
                uncheckedThumbColor = SandText,
                uncheckedTrackColor = WalnutCard
            )
        )
    }
}

@Composable
fun LevelCompleteDialog(
    mode: GameMode,
    levelNumber: Int,
    starsEarned: Int,
    moves: Int,
    elapsedSeconds: Int,
    sessionStreak: Int = 0,
    creditsEarned: Int = 0,
    isFinalLevel: Boolean,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onLevelsMap: () -> Unit
) {
    val headerText = when {
        mode == GameMode.CLASSIC && isFinalLevel -> "النظام مكتمل 100%!"
        mode == GameMode.CLASSIC -> "تمت المهمة • المستوى $levelNumber"
        mode == GameMode.DAILY -> "أتممت التحدي اليومي!"
        mode == GameMode.TIME_ATTACK -> "انتصار سباق الزمن • جولة $levelNumber"
        mode == GameMode.PRECISION -> "دقة متناهية • جولة $levelNumber"
        else -> "تجاوزت الجولة $levelNumber!"
    }
    val nextButtonText = when (mode) {
        GameMode.CLASSIC -> "المستوى التالي (${levelNumber + 1})"
        GameMode.TIME_ATTACK, GameMode.PRECISION, GameMode.ENDLESS -> "الجولة التالية (${levelNumber + 1})"
        GameMode.DAILY -> ""
    }
    val canAdvance = when (mode) {
        GameMode.CLASSIC -> !isFinalLevel
        GameMode.DAILY -> false
        GameMode.TIME_ATTACK, GameMode.PRECISION, GameMode.ENDLESS -> true
    }

    Dialog(onDismissRequest = {}) {
        Surface(
            color = WalnutMedium,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(2.dp, BrassGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = headerText,
                    style = MaterialTheme.typography.headlineMedium,
                    color = BrassBright,
                    textAlign = TextAlign.Center
                )
                if (mode == GameMode.CLASSIC && isFinalLevel) {
                    Text(
                        text = "لقد أتممت جميع المستويات الـ 300 بنجاح باهر!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = EmeraldValid,
                        textAlign = TextAlign.Center
                    )
                } else if (mode == GameMode.TIME_ATTACK && sessionStreak > 0) {
                    Text(
                        text = "سلسلة الانتصارات المتتالية: $sessionStreak 🔥",
                        style = MaterialTheme.typography.titleMedium,
                        color = CopperAccent,
                        textAlign = TextAlign.Center
                    )
                } else if (mode == GameMode.DAILY) {
                    Text(
                        text = "تم تسجيل إنجاز اليوم بنجاح!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = EmeraldValid,
                        textAlign = TextAlign.Center
                    )
                }

                StarRow(stars = starsEarned, starSize = 42.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(WalnutSurface)
                        .border(1.dp, OakBorder.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                        .padding(vertical = 12.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBadge(label = "الحركات", value = moves.toString())
                    StatBadge(label = "الزمن", value = formatTime(elapsedSeconds))
                    if (creditsEarned > 0) {
                        StatBadge(label = "طاقة", value = "+$creditsEarned⚡")
                    }
                }

                if (canAdvance) {
                    BrassPrimaryButton(
                        text = nextButtonText,
                        onClick = onNextLevel,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "next_level_button"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onReplay)
                            .testTag("victory_replay_button"),
                        color = WalnutCard,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, OakBorder)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("إعادة", color = CreamText, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onLevelsMap)
                            .testTag("victory_levels_button"),
                        color = WalnutCard,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, OakBorder)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (mode == GameMode.CLASSIC) "المستويات" else "الرئيسية",
                                color = CreamText,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FailDialog(
    failReason: FailReason?,
    mode: GameMode,
    levelNumber: Int,
    moves: Int,
    onRetry: () -> Unit,
    onHome: () -> Unit
) {
    val title = when (failReason) {
        FailReason.TIME_UP -> "انتهى الوقت!"
        FailReason.OUT_OF_MOVES -> "نفدت الحركات!"
        null -> "انتهت المحاولة"
    }
    val subtitle = when (failReason) {
        FailReason.TIME_UP -> "توقفت الجولة $levelNumber في ${mode.titleAr} بعد نفاد العداد."
        FailReason.OUT_OF_MOVES -> "استنفدت حد الحركات المسموح ($moves) في تحدي الدقة."
        null -> "أعد المحاولة بخطة أسرع!"
    }

    Dialog(onDismissRequest = {}) {
        Surface(
            color = WalnutMedium,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(2.dp, CrimsonBlocked)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = CrimsonBlocked,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = SandText,
                    textAlign = TextAlign.Center
                )

                BrassPrimaryButton(
                    text = "إعادة المحاولة",
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "fail_retry_button"
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onHome)
                        .testTag("fail_home_button"),
                    color = WalnutCard,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, OakBorder)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("العودة للرئيسية", color = CreamText, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBadge(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = BrassBright,
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = ChakraPetchFontFamily)
        )
        Text(text = label, color = SandText, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun ArmoryThemesDialog(
    progress: UserProgressState,
    onUnlockOrSelectTheme: (BoardTheme) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.testTag("armory_dialog"),
            color = WalnutMedium,
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.5.dp, BrassGold.copy(alpha = 0.65f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Palette,
                            contentDescription = null,
                            tint = BrassBright
                        )
                        Text(
                            text = "سمات اللوحة",
                            style = MaterialTheme.typography.headlineMedium,
                            color = BrassBright
                        )
                    }
                    Surface(
                        color = WalnutSurface,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CopperAccent.copy(alpha = 0.65f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Bolt,
                                contentDescription = null,
                                tint = CopperAccent,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "${progress.techCredits}",
                                color = CopperAccent,
                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = ChakraPetchFontFamily)
                            )
                        }
                    }
                }

                for (theme in BoardTheme.entries) {
                    val isUnlocked = progress.isThemeUnlocked(theme)
                    val isSelected = progress.selectedTheme == theme
                    val canAfford = progress.techCredits >= theme.unlockCost

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(enabled = isUnlocked || canAfford) {
                                onUnlockOrSelectTheme(theme)
                            }
                            .testTag("theme_card_${theme.name}"),
                        color = if (isSelected) theme.accentColor.copy(alpha = 0.16f) else WalnutSurface,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            if (isSelected) 1.8.dp else 1.dp,
                            if (isSelected) theme.accentColor else OakBorder.copy(alpha = 0.6f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(theme.frameTopColor)
                                        .border(2.dp, theme.accentColor, CircleShape)
                                )
                                Text(
                                    text = theme.titleAr,
                                    color = CreamText,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                            when {
                                isSelected -> Text(
                                    text = "مفعّل ✓",
                                    color = theme.accentColor,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                isUnlocked -> Text(
                                    text = "تفعيل",
                                    color = BrassBright,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                canAfford -> Text(
                                    text = "فتح ${theme.unlockCost}⚡",
                                    color = CopperAccent,
                                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = ChakraPetchFontFamily)
                                )
                                else -> Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lock,
                                        contentDescription = null,
                                        tint = SandText.copy(alpha = 0.45f),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "${theme.unlockCost}⚡",
                                        color = SandText.copy(alpha = 0.55f),
                                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = ChakraPetchFontFamily)
                                    )
                                }
                            }
                        }
                    }
                }

                BrassPrimaryButton(
                    text = "إغلاق",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "armory_close_button"
                )
            }
        }
    }
}

private data class AchievementItem(
    val title: String,
    val description: String,
    val unlocked: Boolean
)

@Composable
fun AchievementsDialog(
    progress: UserProgressState,
    onDismiss: () -> Unit
) {
    val achievements = listOf(
        AchievementItem("البداية الرقمية", "إكمال المستوى الأول", progress.completedCount >= 1),
        AchievementItem("مهندس صاعد", "إكمال 25 مستوى", progress.completedCount >= 25),
        AchievementItem("خبير البراغي", "إكمال 100 مستوى", progress.completedCount >= 100),
        AchievementItem("سيد الكوانتم", "إكمال 200 مستوى", progress.completedCount >= 200),
        AchievementItem("أسطورة أوميغا", "إكمال جميع الـ 300 مستوى", progress.completedCount >= 300),
        AchievementItem("جامع النجوم", "جمع 150 نجمة ذهبية", progress.totalStars >= 150),
        AchievementItem("برق الزمن", "تحقيق سلسلة 3 في سباق الزمن", progress.timeAttackBestStreak >= 3),
        AchievementItem("دقة جراحية", "الفوز 5 مرات في تحدي الدقة", progress.precisionClears >= 5),
        AchievementItem("بطل اليوم", "إتمام لغز التحدي اليومي", progress.dailyCompletedKey > 0),
        AchievementItem("صائد اللانهائي", "الوصول للجولة 5 في اللانهائي", progress.endlessBestRound >= 5)
    )
    val unlockedCount = achievements.count { it.unlocked }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.testTag("achievements_dialog"),
            color = WalnutMedium,
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.5.dp, CopperAccent.copy(alpha = 0.65f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.EmojiEvents,
                            contentDescription = null,
                            tint = CopperAccent
                        )
                        Text(
                            text = "الأوسمة والإنجازات",
                            style = MaterialTheme.typography.titleLarge,
                            color = BrassBright
                        )
                    }
                    Text(
                        text = "$unlockedCount / ${achievements.size}",
                        color = CopperAccent,
                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = ChakraPetchFontFamily)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(310.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (item in achievements) {
                        Surface(
                            color = if (item.unlocked) EmeraldValid.copy(alpha = 0.12f) else WalnutSurface,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                if (item.unlocked) EmeraldValid.copy(alpha = 0.65f) else OakBorder.copy(alpha = 0.45f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (item.unlocked) Icons.Rounded.CheckCircle else Icons.Rounded.Lock,
                                    contentDescription = null,
                                    tint = if (item.unlocked) EmeraldValid else SandText.copy(alpha = 0.4f),
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = item.title,
                                        color = if (item.unlocked) CreamText else SandText,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = item.description,
                                        color = SandText.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }

                BrassPrimaryButton(
                    text = "إغلاق",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "achievements_close_button"
                )
            }
        }
    }
}

fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}

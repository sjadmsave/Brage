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
import com.example.model.DifficultyTier
import com.example.ui.theme.BrassBright
import com.example.ui.theme.BrassDark
import com.example.ui.theme.BrassGold
import com.example.ui.theme.ChakraPetchFontFamily
import com.example.ui.theme.CopperAccent
import com.example.ui.theme.CreamText
import com.example.ui.theme.EmeraldValid
import com.example.ui.theme.OakBorder
import com.example.ui.theme.SandText
import com.example.ui.theme.TierBeginnerColor
import com.example.ui.theme.TierExpertColor
import com.example.ui.theme.TierHardColor
import com.example.ui.theme.TierMasterColor
import com.example.ui.theme.TierMediumColor
import com.example.ui.theme.WalnutCard
import com.example.ui.theme.WalnutDark
import com.example.ui.theme.WalnutMedium
import com.example.ui.theme.WalnutSurface

fun DifficultyTier.accentColor(): Color = when (this) {
    DifficultyTier.BEGINNER -> TierBeginnerColor
    DifficultyTier.MEDIUM -> TierMediumColor
    DifficultyTier.HARD -> TierHardColor
    DifficultyTier.EXPERT -> TierExpertColor
    DifficultyTier.MASTER -> TierMasterColor
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
                    title = "فك البرغي",
                    description = "اضغط على أي برغي لفكّه وتجهيزه للنقل مع تأثير صوتي رقمي."
                )
                RuleStepItem(
                    step = "02",
                    title = "التثبيت في منفذ متاح",
                    description = "اضغط على أي منفذ متوهج باللون الأخضر غير محجوب بالألواح."
                )
                RuleStepItem(
                    step = "03",
                    title = "تحرير الألواح الرقمية",
                    description = "اللوح المثبّت ببرغي واحد يتأرجح بفعل الجاذبية، وبفك آخر برغي ينفصل اللوح فوراً!"
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
                    label = "وضع حر (فتح الـ 100 مستوى)",
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
    levelNumber: Int,
    starsEarned: Int,
    moves: Int,
    elapsedSeconds: Int,
    isFinalLevel: Boolean,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onLevelsMap: () -> Unit
) {
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
                    text = if (isFinalLevel) "النظام مكتمل 100%!" else "تمت المهمة • المستوى $levelNumber",
                    style = MaterialTheme.typography.headlineMedium,
                    color = BrassBright,
                    textAlign = TextAlign.Center
                )
                if (isFinalLevel) {
                    Text(
                        text = "لقد أتممت جميع المستويات المئة بنجاح باهر!",
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
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBadge(label = "الحركات", value = moves.toString())
                    StatBadge(label = "الزمن", value = formatTime(elapsedSeconds))
                }

                if (!isFinalLevel) {
                    BrassPrimaryButton(
                        text = "المستوى التالي (${levelNumber + 1})",
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
                            Text("المستويات", color = CreamText, style = MaterialTheme.typography.labelLarge)
                        }
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

fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}

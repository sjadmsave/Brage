package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.engine.LevelCatalog
import com.example.model.DifficultyTier
import com.example.model.UserProgressState
import com.example.ui.components.StarRow
import com.example.ui.components.accentColor
import com.example.ui.theme.BrassBright
import com.example.ui.theme.BrassGold
import com.example.ui.theme.ChakraPetchFontFamily
import com.example.ui.theme.CopperAccent
import com.example.ui.theme.CreamText
import com.example.ui.theme.OakBorder
import com.example.ui.theme.SandText
import com.example.ui.theme.WalnutCard
import com.example.ui.theme.WalnutDark
import com.example.ui.theme.WalnutMedium
import com.example.ui.theme.WalnutSurface

@Composable
fun LevelSelectScreen(
    progress: UserProgressState,
    onSelectLevel: (Int) -> Unit,
    onBack: () -> Unit
) {
    var selectedTier by remember { mutableStateOf<DifficultyTier?>(null) }

    val levelsToShow = remember(selectedTier) {
        val range = selectedTier?.range ?: (1..LevelCatalog.TOTAL_LEVELS)
        range.toList()
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
                            WalnutDark.copy(alpha = 0.78f),
                            WalnutMedium.copy(alpha = 0.70f),
                            WalnutDark.copy(alpha = 0.92f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(WalnutCard)
                            .testTag("levels_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "رجوع",
                            tint = BrassBright
                        )
                    }
                    Column {
                        Text(
                            text = "خريطة المستويات",
                            style = MaterialTheme.typography.headlineMedium,
                            color = BrassBright
                        )
                        Text(
                            text = "${progress.completedCount} / 100 مستوى مكتمل",
                            style = MaterialTheme.typography.labelMedium,
                            color = SandText
                        )
                    }
                }

                Surface(
                    color = WalnutCard,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CopperAccent.copy(alpha = 0.65f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = CopperAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${progress.totalStars}",
                            color = CreamText,
                            style = MaterialTheme.typography.labelLarge.copy(fontFamily = ChakraPetchFontFamily)
                        )
                    }
                }
            }

            // Tier Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTier == null,
                    onClick = { selectedTier = null },
                    label = { Text("الكل (100)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrassGold,
                        selectedLabelColor = WalnutDark,
                        containerColor = WalnutSurface,
                        labelColor = CreamText
                    )
                )
                for (tier in DifficultyTier.entries) {
                    val isSelected = selectedTier == tier
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTier = if (isSelected) null else tier },
                        label = {
                            Text("${tier.titleAr} (${tier.range.first}-${tier.range.last})")
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = tier.accentColor(),
                            selectedLabelColor = WalnutDark,
                            containerColor = WalnutSurface,
                            labelColor = CreamText
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 100-Level Adaptive Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 74.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(levelsToShow, key = { it }) { levelNum ->
                    val unlocked = progress.isLevelUnlocked(levelNum)
                    val stars = progress.levelStars[levelNum] ?: 0
                    val isCurrent = levelNum == progress.highestUnlockedLevel
                    val tier = DifficultyTier.forLevel(levelNum)

                    LevelGridTile(
                        levelNumber = levelNum,
                        tier = tier,
                        unlocked = unlocked,
                        isCurrent = isCurrent,
                        stars = stars,
                        onClick = { if (unlocked) onSelectLevel(levelNum) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelGridTile(
    levelNumber: Int,
    tier: DifficultyTier,
    unlocked: Boolean,
    isCurrent: Boolean,
    stars: Int,
    onClick: () -> Unit
) {
    val accent = tier.accentColor()
    val bgColor = when {
        !unlocked -> WalnutDark.copy(alpha = 0.78f)
        isCurrent -> WalnutCard
        stars > 0 -> WalnutSurface
        else -> WalnutCard.copy(alpha = 0.88f)
    }
    val borderColor = when {
        !unlocked -> OakBorder.copy(alpha = 0.3f)
        isCurrent -> BrassBright
        stars == 3 -> CopperAccent.copy(alpha = 0.75f)
        else -> accent.copy(alpha = 0.5f)
    }

    Surface(
        modifier = Modifier
            .aspectRatio(0.95f)
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = unlocked, onClick = onClick)
            .testTag("level_tile_$levelNumber"),
        color = bgColor,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(if (isCurrent) 2.dp else 1.2.dp, borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(if (unlocked) accent else accent.copy(alpha = 0.25f))
            )

            if (unlocked) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = levelNumber.toString(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isCurrent) BrassBright else CreamText
                    )
                    StarRow(stars = stars, starSize = 13.dp)
                }
            } else {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = "مقفل",
                        tint = SandText.copy(alpha = 0.32f),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = levelNumber.toString(),
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = ChakraPetchFontFamily),
                        color = SandText.copy(alpha = 0.32f)
                    )
                }
            }
        }
    }
}

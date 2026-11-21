package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val CyberTechColorScheme = darkColorScheme(
    primary = BrassGold,
    onPrimary = WalnutDark,
    primaryContainer = BrassDark,
    onPrimaryContainer = CreamText,
    secondary = CopperAccent,
    onSecondary = WalnutDark,
    secondaryContainer = WalnutCard,
    onSecondaryContainer = SandText,
    tertiary = EmeraldValid,
    onTertiary = WalnutDark,
    background = WalnutDark,
    onBackground = CreamText,
    surface = WalnutMedium,
    onSurface = CreamText,
    surfaceVariant = WalnutSurface,
    onSurfaceVariant = SandText,
    outline = OakBorder,
    error = CrimsonBlocked,
    onError = CreamText
)

private val CyberTechShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(26.dp)
)

@Composable
fun WoodBoltsTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberTechColorScheme,
        typography = WoodBoltsTypography,
        shapes = CyberTechShapes,
        content = content
    )
}

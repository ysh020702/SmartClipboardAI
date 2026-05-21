package com.samsung.smartclipboard.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = SamsungBlueDarkMode,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0D3B66),
    onPrimaryContainer = Color(0xFFEAF4FF),
    secondary = OneUiDarkTextSecondary,
    onSecondary = OneUiDarkBackground,
    secondaryContainer = OneUiDarkSurfaceRaised,
    onSecondaryContainer = OneUiDarkText,
    tertiary = OneUiPositive,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF163D2A),
    onTertiaryContainer = Color(0xFFE6F6EC),
    background = OneUiDarkBackground,
    onBackground = OneUiDarkText,
    surface = OneUiDarkSurface,
    onSurface = OneUiDarkText,
    surfaceVariant = OneUiDarkSurfaceRaised,
    onSurfaceVariant = OneUiDarkTextSecondary,
    outline = OneUiDarkTextSecondary,
    outlineVariant = OneUiDarkOutline,
    error = Color(0xFFFF6A5F),
    onError = Color.White,
    errorContainer = Color(0xFF4D1511),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = SamsungBlue,
    onPrimary = Color.White,
    primaryContainer = SamsungBlueSoft,
    onPrimaryContainer = SamsungBlueDark,
    secondary = OneUiTextSecondary,
    onSecondary = Color.White,
    secondaryContainer = OneUiSurfaceRaised,
    onSecondaryContainer = OneUiText,
    tertiary = OneUiPositive,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEAF7EF),
    onTertiaryContainer = Color(0xFF0B5F32),
    background = OneUiBackground,
    onBackground = OneUiText,
    surface = OneUiSurface,
    onSurface = OneUiText,
    surfaceVariant = OneUiSurfaceRaised,
    onSurfaceVariant = OneUiTextSecondary,
    outline = OneUiTextSecondary,
    outlineVariant = OneUiOutline,
    error = OneUiDanger,
    onError = Color.White,
    errorContainer = Color(0xFFFFEDEA),
    onErrorContainer = Color(0xFFBA1A1A)
)

private val OneUiShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun SmartClipboardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = OneUiShapes,
        content = content
    )
}

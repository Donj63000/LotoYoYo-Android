package com.example.yoyo_loto.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NeonColorScheme = darkColorScheme(
    primary = NeonBlue,
    onPrimary = DarkBg,
    secondary = NeonMagenta,
    onSecondary = DarkBg,
    tertiary = NeonGreen,
    onTertiary = DarkBg,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceAlt,
    outline = DarkOutline,
    error = NeonRed,
    onError = DarkBg
)

@Composable
fun YoYoLotoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NeonColorScheme,
        typography = Typography,
        content = content
    )
}

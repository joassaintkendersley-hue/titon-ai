package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TitonColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.Black,
    primaryContainer = NeonViolet,
    onPrimaryContainer = Color.White,
    secondary = NeonPurple,
    onSecondary = Color.Black,
    secondaryContainer = TitonSurfaceVariant,
    onSecondaryContainer = ElectricBlue,
    tertiary = NeonMagenta,
    onTertiary = Color.Black,
    background = TitonDarkVoid,
    onBackground = TextPrimary,
    surface = TitonDarkPurple,
    onSurface = TextPrimary,
    surfaceVariant = TitonSurface,
    onSurfaceVariant = TextSecondary,
    outline = TitonBorder,
    outlineVariant = TitonSurfaceVariant,
    error = StatusError,
    onError = Color.White
)

@Composable
fun TitonAiTheme(
    content: @Composable () -> Unit
) {
    // TITON AI is exclusively styled with a luxury futuristic dark palette
    MaterialTheme(
        colorScheme = TitonColorScheme,
        typography = Typography,
        content = content
    )
}

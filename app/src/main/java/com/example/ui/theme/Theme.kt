package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MirrorChessDarkScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = BackgroundDark,
    primaryContainer = SurfaceHighlight,
    onPrimaryContainer = TextPrimary,
    secondary = AmberAccent,
    onSecondary = BackgroundDark,
    secondaryContainer = SurfaceElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = EmeraldAccent,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceHighlight,
    error = CrimsonAccent
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Mirror Chess is designed with a premium dark theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MirrorChessDarkScheme,
        typography = Typography,
        content = content
    )
}

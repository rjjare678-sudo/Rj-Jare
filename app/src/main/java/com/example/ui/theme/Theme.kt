package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = IndigoAccent,
    secondary = VioletAccent,
    tertiary = HotPink,
    background = ObsidianBg,
    surface = GlassBgSoft,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = SpaceWhite,
    onSurface = SpaceWhite,
    surfaceVariant = GlassBgMedium,
    onSurfaceVariant = SlateTextMuted,
    outline = GlassBorderSoft
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

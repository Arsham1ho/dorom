package com.arsham.dorom.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DoromColorScheme = darkColorScheme(
    primary = Terracotta,
    onPrimary = InkBackground,
    secondary = Sage,
    onSecondary = InkBackground,
    background = InkBackground,
    onBackground = TextPrimary,
    surface = InkSurface,
    onSurface = TextPrimary,
    surfaceVariant = InkSurfaceRaised,
    onSurfaceVariant = TextSecondary,
    outline = InkOutline,
    error = DangerRed,
    onError = TextPrimary,
)

@Composable
fun DoromTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DoromColorScheme,
        typography = DoromTypography,
        shapes = DoromShapes,
        content = content,
    )
}

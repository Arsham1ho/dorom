package com.arsham.dorom.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

enum class ThemeMode { SYSTEM, LIGHT, DARK }

private val DoromDarkScheme = darkColorScheme(
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

private val DoromLightScheme = lightColorScheme(
    primary = TerracottaOnLight,
    onPrimary = ParchmentSurface,
    secondary = SageOnLight,
    onSecondary = ParchmentSurface,
    background = ParchmentBackground,
    onBackground = TextPrimaryLight,
    surface = ParchmentSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = ParchmentSurfaceRaised,
    onSurfaceVariant = TextSecondaryLight,
    outline = ParchmentOutline,
    error = DangerRedOnLight,
    onError = ParchmentSurface,
)

@Composable
fun DoromTheme(themeMode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val useDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    MaterialTheme(
        colorScheme = if (useDark) DoromDarkScheme else DoromLightScheme,
        typography = DoromTypography,
        shapes = DoromShapes,
        content = content,
    )
}

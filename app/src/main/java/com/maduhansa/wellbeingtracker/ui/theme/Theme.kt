package com.maduhansa.wellbeingtracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = TealPrimaryLight,
    onPrimary = TealOnPrimaryLight,
    primaryContainer = TealContainerLight,
    onPrimaryContainer = TealOnContainerLight,
    secondary = SecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    background = BackgroundLight,
    surface = BackgroundLight
)

private val DarkColors = darkColorScheme(
    primary = TealPrimaryDark,
    onPrimary = TealOnPrimaryDark,
    primaryContainer = TealContainerDark,
    onPrimaryContainer = TealOnContainerDark,
    secondary = SecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    background = BackgroundDark,
    surface = BackgroundDark
)

// The app's theme: fixed teal colours (no wallpaper-based dynamic colour),
// following the phone's light/dark mode setting.
@Composable
fun WellbeingTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
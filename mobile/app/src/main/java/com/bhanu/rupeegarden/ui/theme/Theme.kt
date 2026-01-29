package com.bhanu.rupeegarden.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimary,
    onPrimary = TextOnPrimary,
    primaryContainer = GreenPrimaryDark,
    onPrimaryContainer = TextOnPrimary,
    secondary = XpGold,
    onSecondary = TextPrimary,
    secondaryContainer = XpGoldDark,
    onSecondaryContainer = TextOnPrimary,
    tertiary = SpentRed,
    onTertiary = TextOnPrimary,
    tertiaryContainer = SpentRedDark,
    onTertiaryContainer = TextOnPrimary,
    background = BackgroundDark,
    onBackground = Color.White,
    surface = SurfaceDark,
    onSurface = Color.White,
    surfaceVariant = CardDark,
    onSurfaceVariant = Color.White.copy(alpha = 0.8f),
    error = SpentRed,
    onError = TextOnPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = TextOnPrimary,
    primaryContainer = GreenLight,
    onPrimaryContainer = TextPrimary,
    secondary = XpGold,
    onSecondary = TextPrimary,
    secondaryContainer = XpGoldLight,
    onSecondaryContainer = TextPrimary,
    tertiary = SpentRed,
    onTertiary = TextOnPrimary,
    tertiaryContainer = SpentRedLight,
    onTertiaryContainer = TextPrimary,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = CardLight,
    onSurfaceVariant = TextSecondary,
    error = SpentRed,
    onError = TextOnPrimary
)

@Composable
fun RupeeGardenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

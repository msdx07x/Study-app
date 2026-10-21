package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AuraDarkColorScheme = darkColorScheme(
    primary = AuraCyan,
    onPrimary = AuraBackground,
    primaryContainer = AuraIndigo,
    onPrimaryContainer = TextPrimary,
    secondary = AuraPurple,
    onSecondary = TextPrimary,
    secondaryContainer = AuraSurfaceVariant,
    onSecondaryContainer = AuraCyan,
    tertiary = AuraAmber,
    onTertiary = AuraBackground,
    background = AuraBackground,
    onBackground = TextPrimary,
    surface = AuraSurface,
    onSurface = TextPrimary,
    surfaceVariant = AuraSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = AuraCardBorder,
    outlineVariant = AuraGlowBorder
)

private val AuraLightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = TextPrimary,
    primaryContainer = LightSecondary,
    onPrimaryContainer = TextPrimary,
    secondary = LightSecondary,
    onSecondary = TextPrimary,
    background = LightBackground,
    onBackground = AuraBackground,
    surface = LightSurface,
    onSurface = AuraBackground,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = AuraSurface
)

@Composable
fun StudyOSTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AuraDarkColorScheme else AuraLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    StudyOSTheme(darkTheme = darkTheme, content = content)
}

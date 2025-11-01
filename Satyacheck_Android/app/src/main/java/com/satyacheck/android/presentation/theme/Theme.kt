package com.satyacheck.android.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = TextPrimaryLight,
    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = SecondaryLight,
    onSecondaryContainer = TextPrimaryLight,
    tertiary = VerdictCredible,
    onTertiary = Color.White,
    tertiaryContainer = VerdictCredibleBackground,
    onTertiaryContainer = TextPrimaryLight,
    error = Error,
    onError = Color.White,
    errorContainer = ErrorLight,
    onErrorContainer = TextPrimaryLight,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = TextTertiaryLight
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Color.Black,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = Secondary,
    onSecondary = Color.Black,
    secondaryContainer = SecondaryDark,
    onSecondaryContainer = Color.White,
    tertiary = VerdictCredible,
    onTertiary = Color.Black,
    tertiaryContainer = VerdictCredibleBackgroundDark,
    onTertiaryContainer = Color.White,
    error = ErrorDark,
    onError = Color.Black,
    errorContainer = Error,
    onErrorContainer = Color.White,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = TextTertiaryDark
)

/**
 * Theme enumeration that represents the available theme modes
 */
enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

@Composable
fun SatyaCheckTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    // Determine if we should use dark theme based on the selected theme mode
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    
    // Use dynamic color on Android 12+
    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val context = LocalContext.current
    
    val colorScheme = when {
        dynamicColor && darkTheme -> dynamicDarkColorScheme(context)
        dynamicColor && !darkTheme -> dynamicLightColorScheme(context)
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set status bar to transparent
            window.statusBarColor = Color.Transparent.toArgb()
            // Use light status bar icons if the theme is light
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            // Enable edge-to-edge display
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

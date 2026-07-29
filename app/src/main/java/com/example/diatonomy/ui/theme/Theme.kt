package com.example.diatonomy.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color

private val MidnightColorScheme = darkColorScheme(
    primary = LavenderPrimary,
    onPrimary = MidnightBackground,
    secondary = LavenderPrimaryVariant,
    onSecondary = MidnightBackground,
    background = MidnightBackground,
    onBackground = OnSurfaceLight,
    surface = MidnightSurface,
    onSurface = OnSurfaceLight,
    surfaceVariant = MidnightSurfaceVariant,
    onSurfaceVariant = MutedText,
    error = Color(0xFFEF9A9A)
)

@Composable
fun DiaTonomyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = MidnightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
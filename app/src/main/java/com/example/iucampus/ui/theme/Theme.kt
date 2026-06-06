package com.example.iucampus.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.compose.runtime.SideEffect

private val DarkColorScheme = darkColorScheme(
    primary = IUPrimary,
    secondary = IUSecondary,
    tertiary = IUTertiary
)

private val LightColorScheme = lightColorScheme(
    primary = IUPrimary,
    secondary = IUSecondary,
    tertiary = IUTertiary,
    background = IUBackground,
    surface = IUSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    primaryContainer = IUPrimary,
    onPrimaryContainer = Color.White,
    secondaryContainer = IUSecondary,
    onSecondaryContainer = Color.White,
    surfaceVariant = Color.White,
    onSurfaceVariant = IUPrimary
)

@Composable
fun IUCampusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disable dynamic color by default to ensure brand colors are used
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
package com.bitchat.android.ui.theme

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

// Ocean Blue Palette (Professional & Trusted)
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF64B5F6),        // Light Blue 300
    onPrimary = Color(0xFF0D47A1),        // Dark Blue 900
    secondary = Color(0xFF90CAF9),      // Blue 200
    onSecondary = Color(0xFF0D47A1),
    background = Color(0xFF0A1929),      // Deep Navy
    onBackground = Color(0xFFE3F2FD),    // Light Blue text
    surface = Color(0xFF132F4C),         // Navy Blue Surface
    onSurface = Color(0xFFE3F2FD),      // Light Blue text
    error = Color(0xFFF44336),          // Standard Red
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),        // Blue 700
    onPrimary = Color.White,
    secondary = Color(0xFF42A5F5),      // Blue 400
    onSecondary = Color.White,
    background = Color(0xFFF0F7FF),      // Very light blue tint
    onBackground = Color(0xFF0D47A1),    // Dark Blue text
    surface = Color.White,
    onSurface = Color(0xFF0D47A1),      // Dark Blue text
    error = Color(0xFFD32F2F),          // Dark Red
    onError = Color.White
)

@Composable
fun BitchatTheme(
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit
) {
    // App-level override from ThemePreferenceManager
    val themePref by ThemePreferenceManager.themeFlow.collectAsState(initial = ThemePreference.System)
    val shouldUseDark = when (darkTheme) {
        true -> true
        false -> false
        null -> when (themePref) {
            ThemePreference.Dark -> true
            ThemePreference.Light -> false
            ThemePreference.System -> isSystemInDarkTheme()
        }
    }

    val colorScheme = if (shouldUseDark) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    SideEffect {
        (view.context as? Activity)?.window?.let { window ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.setSystemBarsAppearance(
                    if (!shouldUseDark) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = if (!shouldUseDark) {
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else 0
            }
            window.navigationBarColor = colorScheme.background.toArgb()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

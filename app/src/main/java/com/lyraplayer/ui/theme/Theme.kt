package com.lyraplayer.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Dark Color Scheme ─────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary          = Amber400,
    onPrimary        = Color(0xFF1A1200),
    primaryContainer = Amber800,
    onPrimaryContainer = Color(0xFFFFECC2),

    secondary        = Amber300,
    onSecondary      = Color(0xFF1A1200),

    background       = Dark900,
    onBackground     = White90,

    surface          = Dark800,
    onSurface        = White90,

    surfaceVariant   = Dark700,
    onSurfaceVariant = White60,

    outline          = White30,
    outlineVariant   = Dark600,
)

// ── Light Color Scheme ────────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary          = Amber800,
    onPrimary        = Color(0xFFFFFFFF),
    primaryContainer = LightVariant,
    onPrimaryContainer = Color(0xFF2A1600),

    secondary        = Color(0xFFBF7B00),
    onSecondary      = Color(0xFFFFFFFF),

    background       = LightBg,
    onBackground     = Color(0xFF1A1200),

    surface          = LightSurface,
    onSurface        = Color(0xFF1A1200),

    surfaceVariant   = LightVariant,
    onSurfaceVariant = Color(0xFF4A3600),

    outline          = Color(0xFFBFA070),
    outlineVariant   = Color(0xFFE8D0A0),
)

// ── Theme ─────────────────────────────────────────────────────────────────────
@Composable
fun LyraPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // matikan dynamic color
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
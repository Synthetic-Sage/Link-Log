package com.synthetic.linklog.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─── Dark Color Scheme ───────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary             = DarkAccent,
    onPrimary           = OnDarkAccent,
    primaryContainer    = DarkAccentVar,
    onPrimaryContainer  = OnDarkAccent,

    secondary           = DarkAccentVar,
    onSecondary         = OnDarkAccent,

    background          = DarkBase,
    onBackground        = OnDarkBase,

    surface             = DarkSurface,
    onSurface           = OnDarkBase,

    surfaceVariant      = DarkSurfaceVar,
    onSurfaceVariant    = OnDarkMuted,

    error               = ErrorRed,
    onError             = OnError,

    outline             = OnDarkMuted,
    outlineVariant      = DarkSurfaceVar,
)

// ─── Light Color Scheme ──────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary             = LightAccent,
    onPrimary           = OnLightAccent,
    primaryContainer    = LightAccentVar,
    onPrimaryContainer  = OnLightAccent,

    secondary           = LightAccentVar,
    onSecondary         = OnLightAccent,

    background          = LightBase,
    onBackground        = OnLightBase,

    surface             = LightSurface,
    onSurface           = OnLightBase,

    surfaceVariant      = LightSurfaceVar,
    onSurfaceVariant    = OnLightMuted,

    error               = ErrorRed,
    onError             = OnError,

    outline             = OnLightMuted,
    outlineVariant      = LightSurfaceVar,
)

// ─── Theme Composition Local for manual dark-mode toggle ─────────────────────
// MainScaffold can expose a lambda to flip this.
val LocalDarkTheme = compositionLocalOf { true }

@Composable
fun LinkLogTheme(
    darkTheme: Boolean = true,
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

    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography   = LinkLogTypography,
            shapes       = LinkLogShapes,
            content      = content
        )
    }
}
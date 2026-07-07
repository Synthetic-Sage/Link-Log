package com.synthetic.linklog.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Dark Mode Palette ──────────────────────────────────────────────────────
// Base backgrounds
val DarkBase        = Color(0xFF1A1B2E)   // deepest background
val DarkSurface     = Color(0xFF22243A)   // card / surface layer
val DarkSurfaceVar  = Color(0xFF2C2E4A)   // elevated card / sheet

// Accent (deep indigo violet)
val DarkAccent      = Color(0xFF4C3AA0)
val DarkAccentVar   = Color(0xFF6A52C4)   // lighter tint for hover / selected

// On-colors for dark
val OnDarkBase      = Color(0xFFEAEAFF)   // primary text
val OnDarkMuted     = Color(0xFF9A9CBF)   // secondary / description text
val OnDarkAccent    = Color(0xFFFFFFFF)   // text on accent color

// ─── Light Mode Palette ─────────────────────────────────────────────────────
// Base backgrounds
val LightBase       = Color(0xFFFFF8ED)   // warm amber base
val LightSurface    = Color(0xFFFFFFFF)   // card / surface
val LightSurfaceVar = Color(0xFFF2E8D8)   // elevated / sheet

// Accent (warm amber)
val LightAccent     = Color(0xFFE8890C)
val LightAccentVar  = Color(0xFFFFA726)

// On-colors for light
val OnLightBase     = Color(0xFF1A1B2E)   // primary text
val OnLightMuted    = Color(0xFF6B6880)   // secondary / description text
val OnLightAccent   = Color(0xFFFFFFFF)   // text on accent

// ─── Semantic / Shared ───────────────────────────────────────────────────────
// Rank badge colours
val RankGold        = Color(0xFFFFD700)
val RankSilver      = Color(0xFFC0C0C0)
val RankBronze      = Color(0xFFCD7F32)
val RankDefault     = Color(0xFF1A1B2E)   // rank 4+ — black-ish in dark; same token

// Destructive
val ErrorRed        = Color(0xFFCF6679)
val OnError         = Color(0xFFFFFFFF)

// Success (for save-tick)
val SuccessGreen    = Color(0xFF4CAF50)

// Snackbar / undo highlight
val SnackbarBg      = Color(0xFF2E3050)
val SnackbarText    = Color(0xFFEAEAFF)
val UndoText        = Color(0xFF7C6FE0)   // accent in snackbar

// Rank-editing highlight (keyboard focus)
val RankEditBlue    = Color(0xFF2196F3)
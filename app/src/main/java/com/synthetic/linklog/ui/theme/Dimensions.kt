package com.synthetic.linklog.ui.theme

import androidx.compose.ui.unit.dp

// ═══════════════════════════════════════════════════════════════════════════════
//  SPACING  — 8dp grid
//  Use these instead of raw Dp literals anywhere in the app.
// ═══════════════════════════════════════════════════════════════════════════════

object Spacing {
    /** 4dp — tight inter-label / icon padding */
    val xs  = 4.dp
    /** 8dp — gap between elements */
    val sm  = 8.dp
    /** 12dp — internal card padding */
    val md  = 12.dp
    /** 16dp — screen edge padding */
    val lg  = 16.dp
    /** 24dp — section separation */
    val xl  = 24.dp
    /** 32dp — large section gap */
    val xxl = 32.dp
}

// ═══════════════════════════════════════════════════════════════════════════════
//  ELEVATION  — card shadow tokens
// ═══════════════════════════════════════════════════════════════════════════════

object Elevation {
    /** No elevation — flat backgrounds */
    val none   = 0.dp
    /** 2dp — link cards (subtle shadow per spec) */
    val card   = 2.dp
    /** 4dp — dropdowns / menus */
    val menu   = 4.dp
    /** 8dp — bottom sheets / dialogs */
    val sheet  = 8.dp
    /** 16dp — FAB */
    val fab    = 16.dp
}

// ═══════════════════════════════════════════════════════════════════════════════
//  ICON / IMAGE SIZES
// ═══════════════════════════════════════════════════════════════════════════════

object IconSize {
    val sm   = 16.dp   // inline glyph
    val md   = 24.dp   // standard icon
    val lg   = 32.dp   // action icon in detail screen
    val xl   = 48.dp   // empty-state illustration glyph
}

object ImageSize {
    /** Link card thumbnail — spec: 80×80dp */
    val cardThumbnail = 80.dp
    /** Rank badge circle diameter */
    val rankBadge     = 28.dp
    /** Detail screen hero thumbnail height */
    val heroHeight    = 240.dp
}

// ═══════════════════════════════════════════════════════════════════════════════
//  CARD DIMENSIONS
// ═══════════════════════════════════════════════════════════════════════════════

object CardDim {
    /** Link card height */
    val linkCardHeight   = 100.dp
    /** Folder grid card height */
    val folderCardHeight = 120.dp
}

// ═══════════════════════════════════════════════════════════════════════════════
//  SNACKBAR
// ═══════════════════════════════════════════════════════════════════════════════

object SnackbarDuration {
    /** Undo snackbar visible for 4 seconds per spec */
    const val undoMs = 4_000L
}

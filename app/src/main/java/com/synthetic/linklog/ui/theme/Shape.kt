package com.synthetic.linklog.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ─── Shape Scale (matches Material3 shape system) ────────────────────────────
val LinkLogShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // chips, tiny badges
    small      = RoundedCornerShape(8.dp),   // text fields, small surfaces
    medium     = RoundedCornerShape(12.dp),  // dialogs, sheets
    large      = RoundedCornerShape(16.dp),  // link cards  ← spec calls for 16dp
    extraLarge = RoundedCornerShape(28.dp),  // FAB, pill dropdowns
)

// Specific token for pill/cylinder dropdown
val ShapePill = RoundedCornerShape(50)       // fully rounded ends

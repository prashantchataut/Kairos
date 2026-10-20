package com.kairos.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** One radius family: small chips, controls, cards, sheets. Pills use CircleShape. */
object Radii {
    val Small = RoundedCornerShape(14.dp)
    val Control = RoundedCornerShape(22.dp)
    val Card = RoundedCornerShape(32.dp)
    val Sheet = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
}

/** 4dp grid. */
object Space {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val huge = 48.dp
    /** Horizontal gutter for every screen. */
    val gutter = 22.dp
}

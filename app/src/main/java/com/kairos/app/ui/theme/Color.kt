package com.kairos.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Paint washes — used for backgrounds, illustrations and Pip, never for body text. */
@Immutable
data class Washes(
    val sky: Color,
    val mint: Color,
    val lilac: Color,
    val peach: Color,
    val butter: Color,
    val rose: Color,
) {
    val all: List<Color> get() = listOf(sky, mint, lilac, peach, butter, rose)
}

@Immutable
data class KairosColors(
    val paper: Color,
    val paperDeep: Color,
    val ink: Color,
    val inkSoft: Color,
    val inkFaint: Color,
    val lagoon: Color,
    val onLagoon: Color,
    val glassTint: Color,
    val glassRim: Color,
    val acrylic: Color,
    val shadow: Color,
    val danger: Color,
    val washes: Washes,
    val isDark: Boolean,
)

val LightKairos = KairosColors(
    paper = Color(0xFFF3F7FB),
    paperDeep = Color(0xFFE6EEF7),
    ink = Color(0xFF1D2640),
    inkSoft = Color(0xFF55617E),
    inkFaint = Color(0xFF8C97B2),
    lagoon = Color(0xFF3E8EDE),
    onLagoon = Color.White,
    glassTint = Color.White.copy(alpha = 0.42f),
    glassRim = Color.White.copy(alpha = 0.9f),
    acrylic = Color.White.copy(alpha = 0.62f),
    shadow = Color(0xFF1D2640).copy(alpha = 0.10f),
    danger = Color(0xFFE0566E),
    washes = Washes(
        sky = Color(0xFF8FC1F7),
        mint = Color(0xFF8EDDC0),
        lilac = Color(0xFFBBA8F6),
        peach = Color(0xFFFFB59A),
        butter = Color(0xFFFFE07A),
        rose = Color(0xFFFF97AB),
    ),
    isDark = false,
)

val DarkKairos = KairosColors(
    paper = Color(0xFF0C1322),
    paperDeep = Color(0xFF131C30),
    ink = Color(0xFFEEF3FF),
    inkSoft = Color(0xFFA6B1CE),
    inkFaint = Color(0xFF6E7A99),
    lagoon = Color(0xFF6FB0F5),
    onLagoon = Color(0xFF0C1322),
    glassTint = Color(0xFF1B2640).copy(alpha = 0.50f),
    glassRim = Color.White.copy(alpha = 0.22f),
    acrylic = Color(0xFF1A2440).copy(alpha = 0.66f),
    shadow = Color.Black.copy(alpha = 0.35f),
    danger = Color(0xFFFF7A90),
    washes = Washes(
        sky = Color(0xFF3F7BC8),
        mint = Color(0xFF2E9C7C),
        lilac = Color(0xFF6E58C2),
        peach = Color(0xFFC9705A),
        butter = Color(0xFFC7A43A),
        rose = Color(0xFFC25472),
    ),
    isDark = true,
)

val LocalKairosColors = staticCompositionLocalOf { LightKairos }

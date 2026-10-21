package com.kairos.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * "Wish & bloom" (see .claude/skills/kairos-taste). Sky and aura for hero moments, warm paper
 * for reading, ultramarine for action, pastels and saturated primaries as content colour.
 */
@Immutable
data class Palette(
    val isDark: Boolean,
    val skyTop: Color,
    val skyMid: Color,
    val skyLow: Color,
    /** Slow-drifting aura blobs over the sky (#606-02/34). */
    val auras: List<Color>,
    val onSky: Color,
    val onSkySoft: Color,
    val paper: Color,
    val surface: Color,
    val surfaceSoft: Color,
    val line: Color,
    val ink: Color,
    val inkSoft: Color,
    val inkFaint: Color,
    val accent: Color,
    val onAccent: Color,
    val positive: Color,
    val warning: Color,
    val negative: Color,
    val shadow: Color,
    val glass: Color,
    val glassRim: Color,
    /** Pastel tiles (#606-05), in [Tint] order, each with a deep ink for text. */
    val tints: List<Color>,
    val tintInk: List<Color>,
)

enum class Tint { Sky, Mint, Lilac, Peach, Butter, Blush }

/** The cast's saturated primaries (#606-03). The same in light and dark. */
object Cast {
    val Blue = Color(0xFF2F6BFF)
    val Coral = Color(0xFFFF5A4E)
    val Pink = Color(0xFFFF4FD8)
    val Green = Color(0xFF22C27A)
    val Orange = Color(0xFFFFB020)
    val Sun = Color(0xFFFFD84A)
    val all = listOf(Blue, Coral, Pink, Green, Orange, Sun)
}

/** Petal glazes for blooms (#606-29). */
object Glaze {
    val Coral = Color(0xFFFF7A6B)
    val Butter = Color(0xFFFFD66B)
    val Cyan = Color(0xFF6FD3FF)
    val Violet = Color(0xFF9B7BFF)
    val Rose = Color(0xFFFF8FC8)
    val Mint = Color(0xFF7FE3B8)
}

val LightPalette = Palette(
    isDark = false,
    skyTop = Color(0xFF3E7BFA),
    skyMid = Color(0xFF8DB9FF),
    skyLow = Color(0xFFE6F0FF),
    auras = listOf(Color(0xFFB9A4FF), Color(0xFFFFB8D9), Color(0xFF7FE0FF)),
    onSky = Color.White,
    onSkySoft = Color.White.copy(alpha = 0.82f),
    paper = Color(0xFFF7F5F0),
    surface = Color.White,
    surfaceSoft = Color(0xFFEFECE5),
    line = Color(0xFFE5E1D8),
    ink = Color(0xFF101828),
    inkSoft = Color(0xFF4F5868),
    inkFaint = Color(0xFF8C93A1),
    accent = Color(0xFF2340E8),
    onAccent = Color.White,
    positive = Color(0xFF14A06B),
    warning = Color(0xFFD9861C),
    negative = Color(0xFFE5484D),
    shadow = Color(0xFF1B2440).copy(alpha = 0.14f),
    glass = Color.White.copy(alpha = 0.26f),
    glassRim = Color.White.copy(alpha = 0.6f),
    tints = listOf(Color(0xFFD6E7FF), Color(0xFFCDEFDF), Color(0xFFE4DAFF), Color(0xFFFFDCC7), Color(0xFFFFF0A6), Color(0xFFFFD5E4)),
    tintInk = listOf(Color(0xFF1A3F9E), Color(0xFF0F5C40), Color(0xFF45309A), Color(0xFF8E3A12), Color(0xFF6B5300), Color(0xFF8E2252)),
)

val DarkPalette = Palette(
    isDark = true,
    skyTop = Color(0xFF2A3FD0),
    skyMid = Color(0xFF1B2470),
    skyLow = Color(0xFF111735),
    auras = listOf(Color(0xFF6B4BFF), Color(0xFFB04BA8), Color(0xFF1FA8C9)),
    onSky = Color.White,
    onSkySoft = Color.White.copy(alpha = 0.76f),
    paper = Color(0xFF0B1026),
    surface = Color(0xFF161C38),
    surfaceSoft = Color(0xFF1E2546),
    line = Color(0xFF283058),
    ink = Color(0xFFF1F3FA),
    inkSoft = Color(0xFFB0B7CF),
    inkFaint = Color(0xFF737C9C),
    accent = Color(0xFF8EA2FF),
    onAccent = Color(0xFF0B1026),
    positive = Color(0xFF3CCB97),
    warning = Color(0xFFF0B45A),
    negative = Color(0xFFFF6B70),
    shadow = Color.Black.copy(alpha = 0.45f),
    glass = Color.White.copy(alpha = 0.10f),
    glassRim = Color.White.copy(alpha = 0.28f),
    tints = listOf(Color(0xFF1C2D5C), Color(0xFF15403A), Color(0xFF2C2560), Color(0xFF45291F), Color(0xFF3C3417), Color(0xFF45213A)),
    tintInk = listOf(Color(0xFFB3CBFF), Color(0xFF93E3C4), Color(0xFFCBBBFF), Color(0xFFFFC2A2), Color(0xFFF2DD86), Color(0xFFFFB7D3)),
)

val LocalPalette = staticCompositionLocalOf { LightPalette }

fun Palette.tint(t: Tint) = tints[t.ordinal]
fun Palette.tintInk(t: Tint) = tintInk[t.ordinal]

object Radius {
    val chip = RoundedCornerShape(16.dp)
    val tile = RoundedCornerShape(24.dp)
    val card = RoundedCornerShape(30.dp)
    val sheet = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp)
}

object Gap {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val huge = 48.dp
    val gutter = 20.dp
}

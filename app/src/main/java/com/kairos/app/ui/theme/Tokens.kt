package com.kairos.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * "Playful sky": a bright sky that melts into a cool white canvas, crisp glass, one cobalt action
 * colour, and a candy set (sun, coral, bubblegum, mint, lilac, sky) for characters, tiles, petals
 * and celebrations. Candy never carries body text.
 */
@Immutable
data class Palette(
    val isDark: Boolean,
    // Sky (hero areas)
    val skyTop: Color,
    val skyMid: Color,
    val skyFade: Color,
    val onSky: Color,
    val onSkySoft: Color,
    val glassOnSky: Color,
    val glassOnSkyRim: Color,
    // Canvas
    val canvas: Color,
    val surface: Color,
    val surfaceSoft: Color,
    val line: Color,
    val ink: Color,
    val inkSoft: Color,
    val inkFaint: Color,
    // Accent and feedback
    val accent: Color,
    val accentSoft: Color,
    val onAccent: Color,
    val positive: Color,
    val warning: Color,
    val negative: Color,
    val shadow: Color,
    /** Pale tile tints, in [Tint] order. */
    val tints: List<Color>,
    /** Deep text colour for each tint. */
    val tintInk: List<Color>,
    /** The vivid candy colour behind each tint: characters, petals, stickers. */
    val candy: List<Color>,
    /** Night sky for the time capsule, top to bottom. */
    val night: List<Color>,
)

/**
 * Named colours so content can choose by meaning (category, theme, mood, envelope). The names are
 * stored with capsules, so they stay stable even though the colours are now candy.
 */
enum class Tint { Sky, Mint, Lavender, Peach, Butter, Rose }

object Candy {
    val Sun = Color(0xFFFFC83D)
    val Coral = Color(0xFFFF6A4D)
    val Bubblegum = Color(0xFFFF79B5)
    val Mint = Color(0xFF2ECF8B)
    val Lilac = Color(0xFFA98CFF)
    val Sky = Color(0xFF4DA3FF)
    val Cobalt = Color(0xFF2F5BFF)
    val Navy = Color(0xFF0F1A3A)
    val Cream = Color(0xFFFFF6E6)
}

val LightPalette = Palette(
    isDark = false,
    skyTop = Color(0xFF3D7BFA),
    skyMid = Color(0xFF8EB9FF),
    skyFade = Color(0xFFE9F2FF),
    onSky = Color.White,
    onSkySoft = Color.White.copy(alpha = 0.8f),
    glassOnSky = Color.White.copy(alpha = 0.24f),
    glassOnSkyRim = Color.White.copy(alpha = 0.55f),
    canvas = Color(0xFFF6F8FE),
    surface = Color.White,
    surfaceSoft = Color(0xFFEEF2FB),
    line = Color(0xFFE2E8F3),
    ink = Candy.Navy,
    inkSoft = Color(0xFF55607A),
    inkFaint = Color(0xFF9AA3B8),
    accent = Candy.Cobalt,
    accentSoft = Color(0xFFE5ECFF),
    onAccent = Color.White,
    positive = Color(0xFF14B87A),
    warning = Color(0xFFF2A21F),
    negative = Color(0xFFF0454B),
    shadow = Color(0xFF1B2A6B).copy(alpha = 0.12f),
    tints = listOf(Color(0xFFDDEBFF), Color(0xFFD6F5E7), Color(0xFFEAE3FF), Color(0xFFFFE3DB), Color(0xFFFFF1C9), Color(0xFFFFE0EE)),
    tintInk = listOf(Color(0xFF1A47B8), Color(0xFF0E6B47), Color(0xFF4B33B0), Color(0xFFB03A1E), Color(0xFF8A6200), Color(0xFFB0306B)),
    candy = listOf(Candy.Sky, Candy.Mint, Candy.Lilac, Candy.Coral, Candy.Sun, Candy.Bubblegum),
    night = listOf(Color(0xFF2353E8), Color(0xFF0E1A5C), Color(0xFF060A22)),
)

val DarkPalette = Palette(
    isDark = true,
    skyTop = Color(0xFF2148C9),
    skyMid = Color(0xFF1B347E),
    skyFade = Color(0xFF0F1833),
    onSky = Color.White,
    onSkySoft = Color.White.copy(alpha = 0.74f),
    glassOnSky = Color.White.copy(alpha = 0.12f),
    glassOnSkyRim = Color.White.copy(alpha = 0.28f),
    canvas = Color(0xFF0A0F1F),
    surface = Color(0xFF141C31),
    surfaceSoft = Color(0xFF1B2440),
    line = Color(0xFF26314D),
    ink = Color(0xFFEFF3FC),
    inkSoft = Color(0xFFA8B2C8),
    inkFaint = Color(0xFF6A7590),
    accent = Color(0xFF7898FF),
    accentSoft = Color(0xFF1E2A55),
    onAccent = Color(0xFF07102A),
    positive = Color(0xFF3CD69F),
    warning = Color(0xFFF5B850),
    negative = Color(0xFFFF6B70),
    shadow = Color.Black.copy(alpha = 0.45f),
    tints = listOf(Color(0xFF1B2D57), Color(0xFF143A31), Color(0xFF2A2458), Color(0xFF3D2420), Color(0xFF3A3018), Color(0xFF3D2033)),
    tintInk = listOf(Color(0xFFA9C8FF), Color(0xFF8BE8C2), Color(0xFFC8B9FF), Color(0xFFFFB9A6), Color(0xFFFFDF8A), Color(0xFFFFB0D3)),
    candy = listOf(Candy.Sky, Candy.Mint, Candy.Lilac, Candy.Coral, Candy.Sun, Candy.Bubblegum),
    night = listOf(Color(0xFF1C3FB8), Color(0xFF0B1448), Color(0xFF04061A)),
)

val LocalPalette = staticCompositionLocalOf { LightPalette }

fun Palette.tint(t: Tint) = tints[t.ordinal]
fun Palette.tintInk(t: Tint) = tintInk[t.ordinal]
fun Palette.candy(t: Tint) = candy[t.ordinal]

/** One radius family, sized by role. */
object Radius {
    val chip = RoundedCornerShape(14.dp)
    val tile = RoundedCornerShape(22.dp)
    val card = RoundedCornerShape(28.dp)
    val deck = RoundedCornerShape(32.dp)
    val sheet = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
}

/** 4dp grid. */
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

package com.kairos.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * "Airy sky glass": a sky-blue hero that fades into a quiet white canvas, crisp frosted glass,
 * and one cobalt accent. Pastels appear only as tile tints in the library, never as decoration.
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
    /** Tile tints for the library board, in [Tint] order. */
    val tints: List<Color>,
    val tintInk: List<Color>,
)

/** Named tints so content can choose a colour by meaning (category, theme, mood, envelope). */
enum class Tint { Sky, Mint, Lavender, Peach, Butter, Rose }

val LightPalette = Palette(
    isDark = false,
    skyTop = Color(0xFF3B82F0),
    skyMid = Color(0xFF86B6F6),
    skyFade = Color(0xFFE7F0FC),
    onSky = Color.White,
    onSkySoft = Color.White.copy(alpha = 0.76f),
    glassOnSky = Color.White.copy(alpha = 0.22f),
    glassOnSkyRim = Color.White.copy(alpha = 0.45f),
    canvas = Color(0xFFF6F8FB),
    surface = Color.White,
    surfaceSoft = Color(0xFFEEF2F7),
    line = Color(0xFFE4E9F0),
    ink = Color(0xFF0E1726),
    inkSoft = Color(0xFF5A6577),
    inkFaint = Color(0xFF9AA3B2),
    accent = Color(0xFF2D6BFF),
    accentSoft = Color(0xFFE6EEFF),
    onAccent = Color.White,
    positive = Color(0xFF12A877),
    warning = Color(0xFFE39A2D),
    negative = Color(0xFFE5484D),
    shadow = Color(0xFF1B2A4A).copy(alpha = 0.10f),
    tints = listOf(Color(0xFFDCEAFD), Color(0xFFD5F0E6), Color(0xFFE7E0FB), Color(0xFFFCE5D8), Color(0xFFF8F0CC), Color(0xFFFADFE8)),
    tintInk = listOf(Color(0xFF1E4FB8), Color(0xFF136B50), Color(0xFF5038A8), Color(0xFFA2481C), Color(0xFF7F6410), Color(0xFFA2305A)),
)

val DarkPalette = Palette(
    isDark = true,
    skyTop = Color(0xFF1F4FBF),
    skyMid = Color(0xFF1A3478),
    skyFade = Color(0xFF111A33),
    onSky = Color.White,
    onSkySoft = Color.White.copy(alpha = 0.72f),
    glassOnSky = Color.White.copy(alpha = 0.12f),
    glassOnSkyRim = Color.White.copy(alpha = 0.25f),
    canvas = Color(0xFF0B1120),
    surface = Color(0xFF151D30),
    surfaceSoft = Color(0xFF1C2539),
    line = Color(0xFF243049),
    ink = Color(0xFFEFF3FA),
    inkSoft = Color(0xFFA7B1C4),
    inkFaint = Color(0xFF6B7690),
    accent = Color(0xFF6F9BFF),
    accentSoft = Color(0xFF1E2C52),
    onAccent = Color(0xFF07102A),
    positive = Color(0xFF3CCB97),
    warning = Color(0xFFF0B45A),
    negative = Color(0xFFFF6B70),
    shadow = Color.Black.copy(alpha = 0.4f),
    tints = listOf(Color(0xFF1C2E52), Color(0xFF173A35), Color(0xFF2A2551), Color(0xFF3A2A25), Color(0xFF363020), Color(0xFF3A2433)),
    tintInk = listOf(Color(0xFFA9C6FF), Color(0xFF8FE0C3), Color(0xFFC6B8FF), Color(0xFFFFC2A2), Color(0xFFF0DC8C), Color(0xFFFFB3CC)),
)

val LocalPalette = staticCompositionLocalOf { LightPalette }

fun Palette.tint(t: Tint) = tints[t.ordinal]
fun Palette.tintInk(t: Tint) = tintInk[t.ordinal]

/** One radius family, sized by role. */
object Radius {
    val chip = RoundedCornerShape(14.dp)
    val tile = RoundedCornerShape(22.dp)
    val card = RoundedCornerShape(28.dp)
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

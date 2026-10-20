package com.kairos.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Kairos colour tokens.
 *
 * Primitives are named after the watercolour pigments they imitate. Screens never use
 * primitives directly: they read the semantic [KairosPalette] through [Kairos.colors],
 * so "Daybreak" (light) and "Dusk" (dark) are just two palettes.
 */
object Pigment {
    // Paper and ink
    val SkyPaper = Color(0xFFF3F7FD)
    val SkyPaperDeep = Color(0xFFE3ECFA)
    val Ink = Color(0xFF1E2B4A)
    val InkSoft = Color(0xFF55627F)
    val InkFaint = Color(0xFF8D98B2)

    // The one accent
    val Cobalt = Color(0xFF3461E8)
    val CobaltDeep = Color(0xFF2347B8)

    // Washes (decorative only — never used for text)
    val Cerulean = Color(0xFF8EC5FF)
    val Lilac = Color(0xFFCDB8FF)
    val Peach = Color(0xFFFFC4A3)
    val Mint = Color(0xFFA4E7CD)
    val Butter = Color(0xFFFFE59C)
    val Rose = Color(0xFFFFB4C8)

    // Dusk
    val NightPaper = Color(0xFF0E1536)
    val NightPaperDeep = Color(0xFF080C26)
    val Moonlight = Color(0xFFEEF2FF)
    val MoonlightSoft = Color(0xFFADB7DA)
    val MoonlightFaint = Color(0xFF7581A8)
    val Periwinkle = Color(0xFF93ACFF)

    // Feedback
    val Sprout = Color(0xFF2C9C77)
    val Marigold = Color(0xFFE19A2E)
    val Berry = Color(0xFFDD4F6A)
}

@Immutable
data class KairosPalette(
    val isDark: Boolean,
    val paper: Color,
    val paperDeep: Color,
    val ink: Color,
    val inkSoft: Color,
    val inkFaint: Color,
    val accent: Color,
    val accentDeep: Color,
    val onAccent: Color,
    /** Watercolour washes, in the order the background painter uses them. */
    val washes: List<Color>,
    val glassFill: Color,
    val glassFillStrong: Color,
    val glassRimTop: Color,
    val glassRimBottom: Color,
    val glassShadow: Color,
    val card: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
)

val DaybreakPalette = KairosPalette(
    isDark = false,
    paper = Pigment.SkyPaper,
    paperDeep = Pigment.SkyPaperDeep,
    ink = Pigment.Ink,
    inkSoft = Pigment.InkSoft,
    inkFaint = Pigment.InkFaint,
    accent = Pigment.Cobalt,
    accentDeep = Pigment.CobaltDeep,
    onAccent = Color.White,
    washes = listOf(Pigment.Cerulean, Pigment.Lilac, Pigment.Peach, Pigment.Mint, Pigment.Butter, Pigment.Rose),
    glassFill = Color.White.copy(alpha = 0.46f),
    glassFillStrong = Color.White.copy(alpha = 0.72f),
    glassRimTop = Color.White.copy(alpha = 0.95f),
    glassRimBottom = Color.White.copy(alpha = 0.25f),
    glassShadow = Color(0xFF2A3D7A).copy(alpha = 0.16f),
    card = Color.White.copy(alpha = 0.86f),
    success = Pigment.Sprout,
    warning = Pigment.Marigold,
    danger = Pigment.Berry,
)

val DuskPalette = KairosPalette(
    isDark = true,
    paper = Pigment.NightPaper,
    paperDeep = Pigment.NightPaperDeep,
    ink = Pigment.Moonlight,
    inkSoft = Pigment.MoonlightSoft,
    inkFaint = Pigment.MoonlightFaint,
    accent = Pigment.Periwinkle,
    accentDeep = Color(0xFF6E8BF0),
    onAccent = Pigment.NightPaperDeep,
    washes = listOf(
        Color(0xFF2B4FC4), Color(0xFF6142B5), Color(0xFFB25B78),
        Color(0xFF1C7D6C), Color(0xFF9C7B2C), Color(0xFF8A3E8E),
    ),
    glassFill = Color.White.copy(alpha = 0.09f),
    glassFillStrong = Color.White.copy(alpha = 0.16f),
    glassRimTop = Color.White.copy(alpha = 0.42f),
    glassRimBottom = Color.White.copy(alpha = 0.06f),
    glassShadow = Color.Black.copy(alpha = 0.35f),
    card = Color(0xFF1A2350).copy(alpha = 0.88f),
    success = Color(0xFF6BD7B0),
    warning = Color(0xFFF3C06A),
    danger = Color(0xFFFF8BA0),
)

val LocalKairosPalette = staticCompositionLocalOf { DaybreakPalette }

/** Named washes for content that wants a specific hue (moods, share cards, capsules). */
enum class Wash(val light: Color, val dark: Color) {
    Cerulean(Pigment.Cerulean, Color(0xFF2B4FC4)),
    Lilac(Pigment.Lilac, Color(0xFF6142B5)),
    Peach(Pigment.Peach, Color(0xFFB25B78)),
    Mint(Pigment.Mint, Color(0xFF1C7D6C)),
    Butter(Pigment.Butter, Color(0xFF9C7B2C)),
    Rose(Pigment.Rose, Color(0xFF8A3E8E));

    fun color(dark: Boolean) = if (dark) this.dark else light
}

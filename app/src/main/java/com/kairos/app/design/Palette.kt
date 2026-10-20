package com.kairos.app.design

import androidx.compose.ui.graphics.Color

/**
 * Aquarelle palette.
 *
 * Two grounds only — warm paper and night pond — each carrying five pigment
 * washes. The user picks one *mood* (sky, peach, mint, lilac); everything
 * accent-shaped follows it. No screen invents a colour of its own.
 */
enum class Mood(val label: String) {
    Sky("Sky"),
    Peach("Peach"),
    Mint("Mint"),
    Lilac("Lilac");

    companion object {
        fun from(name: String?): Mood = entries.firstOrNull { it.name == name } ?: Sky
    }
}

data class KairosColors(
    val dark: Boolean,
    val mood: Mood,
    // ground
    val canvas: Color,
    val canvasTint: Color,
    val surface: Color,
    // ink
    val ink: Color,
    val inkSoft: Color,
    val inkFaint: Color,
    val hairline: Color,
    // pigment washes (full strength / pale)
    val sky: Color,
    val skyWash: Color,
    val peach: Color,
    val peachWash: Color,
    val mint: Color,
    val mintWash: Color,
    val lilac: Color,
    val lilacWash: Color,
    val butter: Color,
    val butterWash: Color,
    val rose: Color,
    val roseWash: Color,
    // glass
    val glassFill: Color,
    val glassFillStrong: Color,
    val glassEdge: Color,
    val glassShade: Color,
    val scrim: Color,
) {
    /** The accent that follows the chosen mood. */
    val accent: Color get() = when (mood) {
        Mood.Sky -> sky
        Mood.Peach -> peach
        Mood.Mint -> mint
        Mood.Lilac -> lilac
    }

    val accentWash: Color get() = when (mood) {
        Mood.Sky -> skyWash
        Mood.Peach -> peachWash
        Mood.Mint -> mintWash
        Mood.Lilac -> lilacWash
    }

    /** A second pigment for gradients, chosen so it never muddies the accent. */
    val accentPartner: Color get() = when (mood) {
        Mood.Sky -> lilac
        Mood.Peach -> butter
        Mood.Mint -> sky
        Mood.Lilac -> peach
    }

    val onAccent: Color get() = if (dark) Color(0xFF0B1020) else Color.White

    val isLight: Boolean get() = !dark
}

val PaperColors = KairosColors(
    dark = false,
    mood = Mood.Sky,
    canvas = Color(0xFFFBF6EE),
    canvasTint = Color(0xFFF4ECDF),
    surface = Color(0xFFFFFCF6),
    ink = Color(0xFF241F1B),
    inkSoft = Color(0xFF6A6158),
    inkFaint = Color(0xFF9B9187),
    hairline = Color(0xFFE9DFD1),
    sky = Color(0xFF3D7BF0),
    skyWash = Color(0xFFD8E6FF),
    peach = Color(0xFFFF8E6E),
    peachWash = Color(0xFFFFE2D6),
    mint = Color(0xFF2FAE8F),
    mintWash = Color(0xFFD3F0E7),
    lilac = Color(0xFF8E7BF0),
    lilacWash = Color(0xFFE6DFFF),
    butter = Color(0xFFF2B33D),
    butterWash = Color(0xFFFFEFC8),
    rose = Color(0xFFDE5B52),
    roseWash = Color(0xFFFFDFDB),
    glassFill = Color(0x99FFFFFF),
    glassFillStrong = Color(0xE6FFFFFF),
    glassEdge = Color(0xE6FFFFFF),
    glassShade = Color(0x14000000),
    scrim = Color(0x66241F1B),
)

val NightColors = KairosColors(
    dark = true,
    mood = Mood.Sky,
    canvas = Color(0xFF12141C),
    canvasTint = Color(0xFF181B26),
    surface = Color(0xFF1B1F2B),
    ink = Color(0xFFF4EFE7),
    inkSoft = Color(0xFFB3ACA2),
    inkFaint = Color(0xFF7E786F),
    hairline = Color(0xFF2C3142),
    sky = Color(0xFF8FB4FF),
    skyWash = Color(0xFF22345C),
    peach = Color(0xFFFFB49B),
    peachWash = Color(0xFF4A2C25),
    mint = Color(0xFF7BDCC0),
    mintWash = Color(0xFF1E3B34),
    lilac = Color(0xFFC0AEFF),
    lilacWash = Color(0xFF2C2650),
    butter = Color(0xFFFFD479),
    butterWash = Color(0xFF3D3118),
    rose = Color(0xFFFF8A80),
    roseWash = Color(0xFF4A2320),
    glassFill = Color(0x14FFFFFF),
    glassFillStrong = Color(0x26FFFFFF),
    glassEdge = Color(0x33FFFFFF),
    glassShade = Color(0x40000000),
    scrim = Color(0x99000000),
)

fun kairosColors(dark: Boolean, mood: Mood): KairosColors =
    (if (dark) NightColors else PaperColors).copy(mood = mood)

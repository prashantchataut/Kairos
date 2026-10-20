package com.kairos.app.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kairos.app.R

/**
 * "Soft Focus" — Kairos' visual language.
 *
 * Two materials carry the whole app:
 *  - **watercolour**: pigment suspended in paper. Broad, uneven washes that drift behind content.
 *  - **liquid glass**: a thin, bright pane laid on top. It catches light at the top edge and
 *    lets colour bleed through, so every surface belongs to the wash underneath it.
 *
 * All colour resolves through [KairosColors]; no screen owns a hex value.
 */

@Immutable
data class AccentSet(
    val name: String,
    val blossom: Color,
    val apricot: Color,
    val periwinkle: Color,
    val mint: Color,
    val butter: Color,
    val rose: Color,
)

@Immutable
data class KairosColors(
    val dark: Boolean,
    val paper: Color,
    val paperDeep: Color,
    val ink: Color,
    val inkSoft: Color,
    val inkFaint: Color,
    val hairline: Color,
    val glassFill: Color,
    val glassFillStrong: Color,
    val glassStroke: Color,
    val glassHighlight: Color,
    val accent: AccentSet,
    val scrim: Color,
) {
    val blossom get() = accent.blossom
    val apricot get() = accent.apricot
    val periwinkle get() = accent.periwinkle
    val mint get() = accent.mint
    val butter get() = accent.butter
    val rose get() = accent.rose
}

object KairosAccents {
    private val light = mapOf(
        "Blossom" to AccentSet("Blossom", Color(0xFFF07C9B), Color(0xFFFFA36C), Color(0xFF8E97F6), Color(0xFF5FC8B0), Color(0xFFF5C15C), Color(0xFFFFC2CF)),
        "Tide" to AccentSet("Tide", Color(0xFF44B79C), Color(0xFF6FA8FF), Color(0xFF7C8CFF), Color(0xFF8FE3C8), Color(0xFFFFD37E), Color(0xFFFFB7C5)),
        "Dusk" to AccentSet("Dusk", Color(0xFF8B7BF0), Color(0xFFFF9E7A), Color(0xFF6FA8FF), Color(0xFF7FD6C0), Color(0xFFF5C15C), Color(0xFFFFB3C7)),
        "Butter" to AccentSet("Butter", Color(0xFFE9A23C), Color(0xFFFF9E7A), Color(0xFF9BB0FF), Color(0xFF8FD8BF), Color(0xFFFFD37E), Color(0xFFFFC2CF)),
    )

    private val dark = mapOf(
        "Blossom" to AccentSet("Blossom", Color(0xFFFF9BB4), Color(0xFFFFB98C), Color(0xFFA8AFFF), Color(0xFF7EE0C8), Color(0xFFFFD47E), Color(0xFFFFC9D5)),
        "Tide" to AccentSet("Tide", Color(0xFF6FDCC0), Color(0xFF9BC4FF), Color(0xFFA8B4FF), Color(0xFF9BEBD6), Color(0xFFFFDE9B), Color(0xFFFFC9D5)),
        "Dusk" to AccentSet("Dusk", Color(0xFFB0A2FF), Color(0xFFFFB98C), Color(0xFF9BC4FF), Color(0xFF8FE3CC), Color(0xFFFFD47E), Color(0xFFFFC9D5)),
        "Butter" to AccentSet("Butter", Color(0xFFFFC46B), Color(0xFFFFB98C), Color(0xFFB4C1FF), Color(0xFF9BEBD6), Color(0xFFFFDE9B), Color(0xFFFFC9D5)),
    )

    val names = listOf("Blossom", "Tide", "Dusk", "Butter")

    fun of(name: String, darkTheme: Boolean): AccentSet =
        (if (darkTheme) dark else light)[name] ?: (if (darkTheme) dark else light).getValue("Blossom")
}

private val LightColors = KairosColors(
    dark = false,
    paper = Color(0xFFFBF7F1),
    paperDeep = Color(0xFFF1E9DE),
    ink = Color(0xFF211E2B),
    inkSoft = Color(0xFF5E586F),
    inkFaint = Color(0xFF938C9F),
    hairline = Color(0xFFE3DACD),
    glassFill = Color(0xB8FFFDF8),
    glassFillStrong = Color(0xE9FFFCF5),
    glassStroke = Color(0xCCFFFFFF),
    glassHighlight = Color(0xFFFFFFFF),
    accent = AccentSet(
        name = "Blossom",
        blossom = Color(0xFFF07C9B),
        apricot = Color(0xFFFFA36C),
        periwinkle = Color(0xFF8E97F6),
        mint = Color(0xFF5FC8B0),
        butter = Color(0xFFF5C15C),
        rose = Color(0xFFFFC2CF),
    ),
    scrim = Color(0x6614101C),
)

private val DarkColors = KairosColors(
    dark = true,
    paper = Color(0xFF12101A),
    paperDeep = Color(0xFF0A0910),
    ink = Color(0xFFF7F1E8),
    inkSoft = Color(0xFFB7AFC3),
    inkFaint = Color(0xFF837B90),
    hairline = Color(0xFF2C2739),
    glassFill = Color(0x14FFFFFF),
    glassFillStrong = Color(0x24FFFFFF),
    glassStroke = Color(0x2EFFFFFF),
    glassHighlight = Color(0x59FFFFFF),
    accent = AccentSet(
        name = "Blossom",
        blossom = Color(0xFFFF9BB4),
        apricot = Color(0xFFFFB98C),
        periwinkle = Color(0xFFA8AFFF),
        mint = Color(0xFF7EE0C8),
        butter = Color(0xFFFFD47E),
        rose = Color(0xFFFFC9D5),
    ),
    scrim = Color(0x99100C18),
)

val InstrumentSerif = FontFamily(
    Font(R.font.instrument_serif, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.instrument_serif_italic, FontWeight.Normal, FontStyle.Italic),
)

val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
)

val Caveat = FontFamily(Font(R.font.patrick_hand, FontWeight.Normal))

/** Editorial serif for the things worth pausing on. */
val KairosDisplay = InstrumentSerif

/** Geometric, friendly sans for the interface itself. */
val KairosSans = Poppins

/** A handwriting voice for marginal notes — never for body copy. */
val KairosHand = Caveat

private val KairosTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = InstrumentSerif, fontWeight = FontWeight.Normal,
        fontSize = 64.sp, lineHeight = 64.sp, letterSpacing = (-0.5).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = InstrumentSerif, fontWeight = FontWeight.Normal,
        fontSize = 46.sp, lineHeight = 48.sp, letterSpacing = (-0.3).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = InstrumentSerif, fontWeight = FontWeight.Normal,
        fontSize = 36.sp, lineHeight = 40.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = InstrumentSerif, fontWeight = FontWeight.Normal,
        fontSize = 32.sp, lineHeight = 36.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = InstrumentSerif, fontWeight = FontWeight.Normal,
        fontSize = 27.sp, lineHeight = 32.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = InstrumentSerif, fontWeight = FontWeight.Normal,
        fontSize = 23.sp, lineHeight = 28.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = (-0.2).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 22.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 25.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 22.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 18.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.3.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Medium,
        fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 0.6.sp,
    ),
)

/** Extra styles that live outside the Material scale. */
object KairosType {
    val quote = TextStyle(
        fontFamily = InstrumentSerif, fontWeight = FontWeight.Normal,
        fontSize = 30.sp, lineHeight = 38.sp,
    )
    val quoteLarge = TextStyle(
        fontFamily = InstrumentSerif, fontWeight = FontWeight.Normal,
        fontSize = 38.sp, lineHeight = 44.sp,
    )
    val word = TextStyle(
        fontFamily = InstrumentSerif, fontWeight = FontWeight.Normal,
        fontSize = 52.sp, lineHeight = 56.sp,
    )
    val hand = TextStyle(
        fontFamily = Caveat, fontWeight = FontWeight.Normal,
        fontSize = 22.sp, lineHeight = 28.sp,
    )
    val handSmall = TextStyle(
        fontFamily = Caveat, fontWeight = FontWeight.Normal,
        fontSize = 18.sp, lineHeight = 24.sp,
    )
    val eyebrow = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 1.6.sp,
    )
}

/** Shape vocabulary: everything is a river stone, nothing is a rectangle. */
object KairosShape {
    val small = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
    val medium = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
    val card = androidx.compose.foundation.shape.RoundedCornerShape(26.dp)
    val large = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
    val sheet = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp)
    val pill = androidx.compose.foundation.shape.RoundedCornerShape(100.dp)
}

private val LocalKairosColors = staticCompositionLocalOf { LightColors }
private val LocalReduceMotion = staticCompositionLocalOf { false }

object KairosTheme {
    val colors: KairosColors
        @Composable @ReadOnlyComposable get() = LocalKairosColors.current

    val reduceMotion: Boolean
        @Composable @ReadOnlyComposable get() = LocalReduceMotion.current
}

@Composable
fun KairosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    reduceMotion: Boolean = false,
    accentName: String = "Blossom",
    content: @Composable () -> Unit,
) {
    val base = if (darkTheme) DarkColors else LightColors
    val colors = base.copy(accent = KairosAccents.of(accentName, darkTheme))
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = colors.ink,
            onPrimary = colors.paper,
            secondary = colors.periwinkle,
            onSecondary = colors.ink,
            background = colors.paper,
            onBackground = colors.ink,
            surface = colors.paperDeep,
            onSurface = colors.ink,
            surfaceVariant = colors.glassFill,
            onSurfaceVariant = colors.inkSoft,
            outline = colors.hairline,
            error = Color(0xFFFF8A80),
        )
    } else {
        lightColorScheme(
            primary = colors.ink,
            onPrimary = colors.paper,
            secondary = colors.periwinkle,
            onSecondary = Color.White,
            background = colors.paper,
            onBackground = colors.ink,
            surface = colors.paperDeep,
            onSurface = colors.ink,
            surfaceVariant = colors.glassFill,
            onSurfaceVariant = colors.inkSoft,
            outline = colors.hairline,
            error = Color(0xFFC2413B),
        )
    }

    CompositionLocalProvider(
        LocalKairosColors provides colors,
        LocalReduceMotion provides reduceMotion,
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = KairosTypography,
            content = content,
        )
    }
}

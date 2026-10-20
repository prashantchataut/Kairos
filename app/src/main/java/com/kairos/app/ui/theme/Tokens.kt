package com.kairos.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.kairos.app.R

/**
 * Kairos design tokens.
 *
 * Direction: a watercolor pond under liquid glass. Paper and ink neutrals tinted toward blue,
 * one accent ("pond"), and a set of pigment washes that only ever appear as soft paint —
 * never as flat UI chrome.
 */
@Immutable
data class KairosColors(
    val paper: Color,
    val paperDeep: Color,
    val ink: Color,
    val inkSoft: Color,
    val inkFaint: Color,
    val pond: Color,
    val pondDeep: Color,
    val onPond: Color,
    val glass: Color,
    val glassStrong: Color,
    val rimTop: Color,
    val rimBottom: Color,
    val shadow: Color,
    val sky: Color,
    val peach: Color,
    val mint: Color,
    val lilac: Color,
    val butter: Color,
    val rose: Color,
    val success: Color,
    val warn: Color,
    val danger: Color,
    val isDark: Boolean,
) {
    val washes: List<Color> get() = listOf(sky, peach, mint, lilac, butter, rose)
}

val LightColors = KairosColors(
    paper = Color(0xFFFBF6EE),
    paperDeep = Color(0xFFF2EADD),
    ink = Color(0xFF1B2340),
    inkSoft = Color(0xFF545D80),
    inkFaint = Color(0xFF8C93AD),
    pond = Color(0xFF3B6FEA),
    pondDeep = Color(0xFF2550C4),
    onPond = Color(0xFFFFFFFF),
    glass = Color(0x8CFFFFFF),
    glassStrong = Color(0xCCFFFFFF),
    rimTop = Color(0xE6FFFFFF),
    rimBottom = Color(0x33FFFFFF),
    shadow = Color(0x1F1B2340),
    sky = Color(0xFFBCD6FF),
    peach = Color(0xFFFFD2B0),
    mint = Color(0xFFB5EDD6),
    lilac = Color(0xFFDCCBFF),
    butter = Color(0xFFFFE7A3),
    rose = Color(0xFFFFC8D5),
    success = Color(0xFF23915E),
    warn = Color(0xFFC98510),
    danger = Color(0xFFD9475E),
    isDark = false,
)

val DarkColors = KairosColors(
    paper = Color(0xFF0E1424),
    paperDeep = Color(0xFF090D18),
    ink = Color(0xFFEEF1FA),
    inkSoft = Color(0xFFA9B2CE),
    inkFaint = Color(0xFF6F7896),
    pond = Color(0xFF86A9FF),
    pondDeep = Color(0xFF5C86F0),
    onPond = Color(0xFF0B1020),
    glass = Color(0x1FFFFFFF),
    glassStrong = Color(0x33FFFFFF),
    rimTop = Color(0x59FFFFFF),
    rimBottom = Color(0x0DFFFFFF),
    shadow = Color(0x66000000),
    sky = Color(0xFF24427E),
    peach = Color(0xFF6E4038),
    mint = Color(0xFF1E5A4D),
    lilac = Color(0xFF41347A),
    butter = Color(0xFF5F5326),
    rose = Color(0xFF632F47),
    success = Color(0xFF5AD39A),
    warn = Color(0xFFF2C14E),
    danger = Color(0xFFFF7A8E),
    isDark = true,
)

val LocalKairosColors = staticCompositionLocalOf { LightColors }

/* ---------- Type ---------- */

val Fraunces = FontFamily(
    Font(R.font.fraunces_regular, FontWeight.Normal),
    Font(R.font.fraunces_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.fraunces_semibold, FontWeight.SemiBold),
    Font(R.font.fraunces_bold, FontWeight.Bold),
)

val Nunito = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold),
)

/** Seven sizes, two families. Fraunces speaks (words, quotes, headlines); Nunito works. */
@Immutable
data class KairosType(
    val hero: TextStyle = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 56.sp, lineHeight = 58.sp, letterSpacing = (-0.02).em),
    val display: TextStyle = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = (-0.015).em),
    val title: TextStyle = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 33.sp, letterSpacing = (-0.01).em),
    val quote: TextStyle = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.Normal, fontSize = 22.sp, lineHeight = 31.sp),
    val heading: TextStyle = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, lineHeight = 26.sp),
    val body: TextStyle = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
    val bodyRegular: TextStyle = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    val label: TextStyle = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 18.sp),
    val caption: TextStyle = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.04.em),
)

val LocalKairosType = staticCompositionLocalOf { KairosType() }

/* ---------- Shape & space ---------- */

object Radii {
    val sm = 14.dp
    val md = 22.dp
    val lg = 32.dp
    val small = RoundedCornerShape(sm)
    val card = RoundedCornerShape(md)
    val sheet = RoundedCornerShape(lg)
    val pill = RoundedCornerShape(percent = 50)
}

object Space {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val huge = 48.dp
    val screen = 20.dp
}

/* ---------- Motion ---------- */

object Motion {
    val Enter = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
    val Move = CubicBezierEasing(0.25f, 1f, 0.5f, 1f)
    val Drawer = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)
    const val FAST = 140
    const val BASE = 240
    const val SLOW = 420

    fun <T> snappy() = spring<T>(dampingRatio = 0.82f, stiffness = 620f)
    fun <T> gentle() = spring<T>(dampingRatio = 0.86f, stiffness = Spring.StiffnessLow)
    /** Reserved for rare, delightful moments (hatching, sealing, celebrating). */
    fun <T> playful() = spring<T>(dampingRatio = 0.55f, stiffness = 380f)
}

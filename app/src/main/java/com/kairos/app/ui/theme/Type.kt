package com.kairos.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.kairos.app.R

/*
 * Two families. Bricolage Grotesque carries the interface and the big playful headlines (its
 * ExtraBold at a large optical size is the r03 "Abode" energy). Fraunces carries the words, quotes
 * and letters, with its SOFT and WONK axes turned up so the serif feels warm rather than solemn.
 */

private fun brico(w: Int, opsz: Float = 16f, wdth: Float = 100f) = Font(
    R.font.bricolage, FontWeight(w),
    variationSettings = FontVariation.Settings(FontVariation.weight(w), FontVariation.Setting("opsz", opsz), FontVariation.Setting("wdth", wdth)),
)

private fun fraunces(w: Int, opsz: Float, italic: Boolean = false, soft: Float = 100f, wonk: Float = 1f) = Font(
    if (italic) R.font.fraunces_italic else R.font.fraunces, FontWeight(w), if (italic) FontStyle.Italic else FontStyle.Normal,
    variationSettings = FontVariation.Settings(FontVariation.weight(w), FontVariation.Setting("opsz", opsz), FontVariation.Setting("SOFT", soft), FontVariation.Setting("WONK", wonk)),
)

/** Interface text. */
val Sans = FontFamily(brico(400), brico(500), brico(600), brico(700))

/** Big headlines: ExtraBold, display optical size, a touch condensed. */
val Display = FontFamily(brico(800, opsz = 96f, wdth = 88f), brico(700, opsz = 72f, wdth = 92f))

/** The word of the day, at display optical size. */
val Serif = FontFamily(
    fraunces(400, 144f), fraunces(600, 144f),
    fraunces(400, 72f, italic = true), fraunces(600, 72f, italic = true),
)

/** Reading serif for quotes in cards and letters: text optical size so it stays sturdy small. */
val SerifText = FontFamily(
    fraunces(400, 24f, soft = 50f, wonk = 0f), fraunces(600, 24f, soft = 50f, wonk = 0f),
    fraunces(400, 24f, italic = true, soft = 50f, wonk = 0f),
)

@Immutable
data class Typography(
    /** 76 Fraunces: the word of the day. */
    val hero: TextStyle,
    /** 40 Fraunces: word titles, big dates. */
    val serifLarge: TextStyle,
    /** 26 Fraunces italic: quotes. */
    val quote: TextStyle,
    /** 44 Bricolage ExtraBold: playful headlines (onboarding, big moments). */
    val shout: TextStyle,
    /** 30 Bricolage Bold: screen titles. */
    val display: TextStyle,
    /** 19 sans: card titles. */
    val title: TextStyle,
    /** 16 sans: body. */
    val body: TextStyle,
    /** 14 sans: labels, buttons. */
    val label: TextStyle,
    /** 12 sans: captions. */
    val caption: TextStyle,
)

val DefaultTypography = Typography(
    hero = TextStyle(fontFamily = Serif, fontSize = 76.sp, lineHeight = 78.sp, letterSpacing = (-0.03).em),
    serifLarge = TextStyle(fontFamily = Serif, fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = (-0.02).em),
    quote = TextStyle(fontFamily = Serif, fontStyle = FontStyle.Italic, fontSize = 26.sp, lineHeight = 33.sp, letterSpacing = (-0.01).em),
    shout = TextStyle(fontFamily = Display, fontWeight = FontWeight.ExtraBold, fontSize = 44.sp, lineHeight = 44.sp, letterSpacing = (-0.03).em),
    display = TextStyle(fontFamily = Display, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 34.sp, letterSpacing = (-0.025).em),
    title = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, lineHeight = 25.sp, letterSpacing = (-0.01).em),
    body = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    label = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    caption = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
)

val LocalTypography = staticCompositionLocalOf { DefaultTypography }

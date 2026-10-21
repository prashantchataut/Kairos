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
 * Two families (kairos-taste §4):
 *  - Bricolage Grotesque: the interface, and chunky condensed display for playful headlines (#606-03).
 *  - Instrument Serif: every word, quote, letter and big date (#606-08/13/30).
 */

private fun bricolage(weight: Int, width: Float = 100f, opsz: Float = 14f) = Font(
    R.font.bricolage,
    FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight), FontVariation.width(width), FontVariation.Setting("opsz", opsz)),
)

/** Text cut: open, friendly, for body and controls. */
val Sans = FontFamily(bricolage(400), bricolage(500), bricolage(600), bricolage(700))

/** Display cut: heavy and condensed, optical size 96. */
val Display = FontFamily(bricolage(700, 88f, 96f), bricolage(800, 85f, 96f))

val Serif = FontFamily(
    Font(R.font.instrument_serif, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.instrument_serif_italic, FontWeight.Normal, FontStyle.Italic),
)

@Immutable
data class Typography(
    /** 80 serif: the word of the day. */
    val hero: TextStyle,
    /** 44 serif: word titles, big dates. */
    val serifLarge: TextStyle,
    /** 28 serif italic: quotes. */
    val quote: TextStyle,
    /** 40 condensed heavy: playful headlines. */
    val display: TextStyle,
    /** 20 semibold: card titles. */
    val title: TextStyle,
    /** 16 regular: body. */
    val body: TextStyle,
    /** 14 semibold: labels and buttons. */
    val label: TextStyle,
    /** 12 medium: captions. */
    val caption: TextStyle,
)

val DefaultTypography = Typography(
    hero = TextStyle(fontFamily = Serif, fontSize = 80.sp, lineHeight = 80.sp, letterSpacing = (-0.025).em),
    serifLarge = TextStyle(fontFamily = Serif, fontSize = 44.sp, lineHeight = 46.sp, letterSpacing = (-0.015).em),
    quote = TextStyle(fontFamily = Serif, fontStyle = FontStyle.Italic, fontSize = 28.sp, lineHeight = 34.sp),
    display = TextStyle(fontFamily = Display, fontWeight = FontWeight(800), fontSize = 40.sp, lineHeight = 38.sp, letterSpacing = (-0.03).em),
    title = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 25.sp, letterSpacing = (-0.01).em),
    body = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    label = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    caption = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
)

val LocalTypography = staticCompositionLocalOf { DefaultTypography }

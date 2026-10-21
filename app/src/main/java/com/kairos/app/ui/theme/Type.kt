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

/* Two families: Instrument Sans carries the interface; Instrument Serif carries the words and quotes. */

private fun sans(w: Int) = Font(R.font.instrument_sans, FontWeight(w), variationSettings = FontVariation.Settings(FontVariation.weight(w)))

val Sans = FontFamily(sans(400), sans(500), sans(600), sans(700))
val Serif = FontFamily(
    Font(R.font.instrument_serif, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.instrument_serif_italic, FontWeight.Normal, FontStyle.Italic),
)

@Immutable
data class Typography(
    /** 72 serif — the word of the day on the sky. */
    val hero: TextStyle,
    /** 40 serif — word titles, big dates. */
    val serifLarge: TextStyle,
    /** 26 serif italic — quotes. */
    val quote: TextStyle,
    /** 30 sans — screen titles. */
    val display: TextStyle,
    /** 19 sans — card titles. */
    val title: TextStyle,
    /** 16 sans — body. */
    val body: TextStyle,
    /** 14 sans — labels, buttons. */
    val label: TextStyle,
    /** 12 sans — captions. */
    val caption: TextStyle,
)

val DefaultTypography = Typography(
    hero = TextStyle(fontFamily = Serif, fontSize = 72.sp, lineHeight = 74.sp, letterSpacing = (-0.02).em),
    serifLarge = TextStyle(fontFamily = Serif, fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = (-0.01).em),
    quote = TextStyle(fontFamily = Serif, fontStyle = FontStyle.Italic, fontSize = 26.sp, lineHeight = 33.sp),
    display = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = (-0.02).em),
    title = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, lineHeight = 25.sp, letterSpacing = (-0.01).em),
    body = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    label = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    caption = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
)

val LocalTypography = staticCompositionLocalOf { DefaultTypography }

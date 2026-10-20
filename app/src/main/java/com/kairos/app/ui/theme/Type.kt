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
 * Three families, each with one job:
 *  - Fraunces (soft, slightly wonky optical serif): words, quotes, big dates.
 *  - Nunito (rounded sans): every piece of interface text.
 *  - Caveat (handwriting): letters sealed in the time capsule.
 */

private fun fraunces(weight: Int, italic: Boolean = false, opsz: Float = 72f) = Font(
    resId = if (italic) R.font.fraunces_italic else R.font.fraunces,
    weight = FontWeight(weight),
    style = if (italic) FontStyle.Italic else FontStyle.Normal,
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight),
        FontVariation.Setting("opsz", opsz),
        FontVariation.Setting("SOFT", 100f),
        FontVariation.Setting("WONK", 1f),
    ),
)

private fun nunito(weight: Int) = Font(
    resId = R.font.nunito,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

private fun caveat(weight: Int) = Font(
    resId = R.font.caveat,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val FrauncesFamily = FontFamily(
    fraunces(300), fraunces(400), fraunces(500), fraunces(600), fraunces(700),
    fraunces(300, italic = true), fraunces(400, italic = true), fraunces(600, italic = true),
)
val NunitoFamily = FontFamily(nunito(400), nunito(500), nunito(600), nunito(700), nunito(800), nunito(900))
val CaveatFamily = FontFamily(caveat(400), caveat(600), caveat(700))

/** The whole type scale: eight sizes, nothing else. */
@Immutable
data class KairosType(
    /** 56 — hero word, big dates. */
    val hero: TextStyle,
    /** 34 — screen titles. */
    val display: TextStyle,
    /** 24 — quotes and card headlines. */
    val quote: TextStyle,
    /** 20 — section and card titles. */
    val title: TextStyle,
    /** 16 — body copy. */
    val body: TextStyle,
    /** 14 — secondary copy, buttons. */
    val label: TextStyle,
    /** 12 — captions, meta. */
    val caption: TextStyle,
    /** 26 — handwritten capsule letters. */
    val hand: TextStyle,
)

val DefaultKairosType = KairosType(
    hero = TextStyle(fontFamily = FrauncesFamily, fontWeight = FontWeight(600), fontSize = 56.sp, lineHeight = 60.sp, letterSpacing = (-0.02).em),
    display = TextStyle(fontFamily = FrauncesFamily, fontWeight = FontWeight(600), fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.01).em),
    quote = TextStyle(fontFamily = FrauncesFamily, fontWeight = FontWeight(400), fontStyle = FontStyle.Italic, fontSize = 24.sp, lineHeight = 33.sp),
    title = TextStyle(fontFamily = NunitoFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, lineHeight = 26.sp),
    body = TextStyle(fontFamily = NunitoFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 25.sp),
    label = TextStyle(fontFamily = NunitoFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp),
    caption = TextStyle(fontFamily = NunitoFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp),
    hand = TextStyle(fontFamily = CaveatFamily, fontWeight = FontWeight(500), fontSize = 26.sp, lineHeight = 32.sp),
)

val LocalKairosType = staticCompositionLocalOf { DefaultKairosType }

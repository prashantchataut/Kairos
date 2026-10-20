package com.kairos.app.design

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.kairos.app.R

/**
 * Two voices, no more.
 *
 * Poppins carries the interface: geometric, round terminals, friendly without
 * being childish. Lora italic carries anything the app says *to* you — quotes,
 * prompts, capsule letters. The contrast between the two is the whole
 * typographic idea.
 */
val Poppins = FontFamily(
    Font(R.font.poppins_light, FontWeight.Light),
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
    Font(R.font.poppins_extrabold, FontWeight.ExtraBold),
)

val Lora = FontFamily(
    Font(R.font.lora_regular, FontWeight.Normal),
    Font(R.font.lora_medium, FontWeight.Medium),
    Font(R.font.lora_semibold, FontWeight.SemiBold),
    Font(R.font.lora_bold, FontWeight.Bold),
    Font(R.font.lora_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.lora_bold_italic, FontWeight.Bold, FontStyle.Italic),
)

private val trim = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

data class KairosType(
    val display: TextStyle = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.ExtraBold, fontSize = 40.sp,
        lineHeight = 44.sp, letterSpacing = (-1.4).sp, lineHeightStyle = trim,
    ),
    val hero: TextStyle = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 32.sp,
        lineHeight = 37.sp, letterSpacing = (-0.9).sp, lineHeightStyle = trim,
    ),
    val title: TextStyle = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 22.sp,
        lineHeight = 28.sp, letterSpacing = (-0.4).sp, lineHeightStyle = trim,
    ),
    val headline: TextStyle = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 17.sp,
        lineHeight = 23.sp, letterSpacing = (-0.2).sp,
    ),
    val body: TextStyle = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Normal, fontSize = 15.sp,
        lineHeight = 23.sp, letterSpacing = 0.sp,
    ),
    val bodyStrong: TextStyle = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 15.sp,
        lineHeight = 23.sp,
    ),
    val label: TextStyle = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 13.sp,
        lineHeight = 17.sp, letterSpacing = 0.1.sp,
    ),
    val caption: TextStyle = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Normal, fontSize = 12.sp,
        lineHeight = 16.sp, letterSpacing = 0.1.sp,
    ),
    val micro: TextStyle = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 10.5.sp,
        lineHeight = 13.sp, letterSpacing = 0.8.sp,
    ),
    val quote: TextStyle = TextStyle(
        fontFamily = Lora, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Normal,
        fontSize = 25.sp, lineHeight = 36.sp, letterSpacing = (-0.2).sp,
    ),
    val quoteSmall: TextStyle = TextStyle(
        fontFamily = Lora, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Normal,
        fontSize = 18.sp, lineHeight = 27.sp,
    ),
    val letter: TextStyle = TextStyle(
        fontFamily = Lora, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 27.sp,
    ),
    val term: TextStyle = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 30.sp,
        lineHeight = 34.sp, letterSpacing = (-0.8).sp,
    ),
)

/** Material needs a Typography for snackbars, sheets and text fields. */
internal fun materialType(t: KairosType) = Typography(
    displayLarge = t.display,
    headlineLarge = t.hero,
    headlineMedium = t.title,
    titleLarge = t.title,
    titleMedium = t.headline,
    bodyLarge = t.body,
    bodyMedium = t.body,
    bodySmall = t.caption,
    labelLarge = t.label,
    labelMedium = t.label,
    labelSmall = t.micro,
)

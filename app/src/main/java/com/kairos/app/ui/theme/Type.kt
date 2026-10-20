package com.kairos.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.kairos.app.R

val Fredoka = FontFamily(
    Font(R.font.fredoka_medium, FontWeight.Medium),
    Font(R.font.fredoka_semibold, FontWeight.SemiBold),
    Font(R.font.fredoka_bold, FontWeight.Bold),
)

val Nunito = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.nunito_medium, FontWeight.Medium),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold),
)

/** Handwriting — reserved for capsule letters. */
val Caveat = FontFamily(
    Font(R.font.caveat_regular, FontWeight.Normal),
    Font(R.font.caveat_semibold, FontWeight.SemiBold),
)

private fun display(size: Int, line: Int, weight: FontWeight = FontWeight.SemiBold) = TextStyle(
    fontFamily = Fredoka, fontWeight = weight, fontSize = size.sp, lineHeight = line.sp,
    letterSpacing = (-0.02).em,
)

private fun body(size: Int, line: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = Nunito, fontWeight = weight, fontSize = size.sp, lineHeight = line.sp,
)

val KairosTypography = Typography(
    displayLarge = display(56, 60, FontWeight.Bold),
    displayMedium = display(44, 48, FontWeight.Bold),
    displaySmall = display(36, 40),
    headlineLarge = display(32, 38),
    headlineMedium = display(28, 34),
    headlineSmall = display(24, 30),
    titleLarge = display(21, 26),
    titleMedium = body(17, 24, FontWeight.Bold),
    titleSmall = body(15, 20, FontWeight.Bold),
    bodyLarge = body(17, 26),
    bodyMedium = body(15, 22),
    bodySmall = body(13, 18),
    labelLarge = body(15, 20, FontWeight.Bold),
    labelMedium = body(13, 16, FontWeight.Bold),
    labelSmall = body(11, 14, FontWeight.Bold).copy(letterSpacing = 0.02.em),
)

val LetterStyle = TextStyle(fontFamily = Caveat, fontSize = 24.sp, lineHeight = 30.sp)

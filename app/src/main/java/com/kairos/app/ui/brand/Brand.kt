package com.kairos.app.ui.brand

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import com.kairos.app.R
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Pigment
import kotlin.math.PI

/**
 * The Kairos mark: a single drop caught at the instant before it falls — kairos, the
 * opportune moment — painted cobalt-to-lilac with a glass highlight, a sparkle for the good
 * thing that moment holds, and a ripple below for what it sets moving.
 */
fun DrawScope.drawKairosMark(origin: Offset, s: Float, ripple: Boolean = true) = translate(origin.x, origin.y) {
    val u = s / 100f
    if (ripple) {
        drawOval(
            Pigment.Cerulean.copy(alpha = 0.55f), topLeft = Offset(18f * u, 86f * u), size = Size(64f * u, 12f * u),
            style = Stroke(width = 3f * u),
        )
    }
    val drop = Path().apply {
        moveTo(52f * u, 4f * u)
        cubicTo(62f * u, 20f * u, 80f * u, 38f * u, 80f * u, 58f * u)
        cubicTo(80f * u, 76f * u, 67f * u, 88f * u, 50f * u, 88f * u)
        cubicTo(33f * u, 88f * u, 20f * u, 76f * u, 20f * u, 58f * u)
        cubicTo(20f * u, 38f * u, 40f * u, 22f * u, 52f * u, 4f * u)
        close()
    }
    drawPath(
        drop,
        Brush.linearGradient(
            listOf(Pigment.Cerulean, Pigment.Cobalt, Color(0xFF7A5CE0)),
            start = Offset(30f * u, 10f * u), end = Offset(72f * u, 90f * u),
        ),
    )
    drawPath(drop, Brush.radialGradient(listOf(Color.White.copy(alpha = 0.45f), Color.Transparent), Offset(38f * u, 46f * u), 30f * u))
    rotate(-26f, pivot = Offset(36f * u, 52f * u)) {
        drawOval(Color.White.copy(alpha = 0.85f), Offset(31f * u, 40f * u), Size(8f * u, 18f * u))
    }
    drawPath(drop, Color.White.copy(alpha = 0.35f), style = Stroke(width = 1.6f * u))
    drawSparkle(Offset(80f * u, 18f * u), 13f * u, Pigment.Butter)
    drawSparkle(Offset(90f * u, 36f * u), 6f * u, Color.White)
}

/** Four-point sparkle centred at [c]. */
fun DrawScope.drawSparkle(c: Offset, r: Float, color: Color) {
    val p = Path().apply {
        moveTo(c.x, c.y - r)
        quadraticTo(c.x + r * 0.16f, c.y - r * 0.16f, c.x + r, c.y)
        quadraticTo(c.x + r * 0.16f, c.y + r * 0.16f, c.x, c.y + r)
        quadraticTo(c.x - r * 0.16f, c.y + r * 0.16f, c.x - r, c.y)
        quadraticTo(c.x - r * 0.16f, c.y - r * 0.16f, c.x, c.y - r)
        close()
    }
    drawPath(p, color)
}

@Composable
fun KairosMark(size: Dp, modifier: Modifier = Modifier, ripple: Boolean = true) {
    Canvas(modifier.size(size)) { drawKairosMark(Offset.Zero, this.size.minDimension, ripple) }
}

@Composable
fun KairosWordmark(
    modifier: Modifier = Modifier,
    markSize: Dp = 30.dp,
    textSize: TextUnit = 26.sp,
    color: Color = Kairos.colors.ink,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        KairosMark(markSize, ripple = false)
        Text(
            "kairos",
            style = Kairos.type.display.copy(fontSize = textSize, fontWeight = FontWeight(600), lineHeight = textSize),
            color = color,
        )
    }
}

/**
 * A round "moment stamp" for share cards: the mark in the middle and the brand line set on a
 * circle, like a postmark. Every shared card carries one.
 */
@Composable
fun KairosStamp(
    size: Dp,
    ink: Color,
    modifier: Modifier = Modifier,
    line: String = "KAIROS ✦ ONE GOOD MOMENT A DAY ✦ ",
) {
    val context = LocalContext.current
    val typeface = remember { ResourcesCompat.getFont(context, R.font.nunito) ?: Typeface.DEFAULT_BOLD }
    Canvas(modifier.size(size)) {
        val r = this.size.minDimension / 2f
        val c = Offset(r, r)
        drawCircle(ink.copy(alpha = 0.9f), r - 1.dp.toPx(), c, style = Stroke(1.3.dp.toPx()))
        drawCircle(ink.copy(alpha = 0.5f), r * 0.62f, c, style = Stroke(0.8.dp.toPx()))
        val markSize = r * 0.9f
        drawKairosMark(Offset(c.x - markSize / 2, c.y - markSize / 2), markSize, ripple = false)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = Typeface.create(typeface, Typeface.BOLD)
            textSize = r * 0.2f
            color = ink.toArgb()
            letterSpacing = 0.12f
        }
        val textR = r * 0.79f
        val arc = android.graphics.Path().apply { addCircle(c.x, c.y, textR, android.graphics.Path.Direction.CW) }
        // Stretch the line so it wraps the circle exactly once.
        val circumference = (2 * PI * textR).toFloat()
        val measured = paint.measureText(line)
        paint.letterSpacing = 0.12f + (circumference - measured) / (paint.textSize * line.length)
        drawContext.canvas.nativeCanvas.drawTextOnPath(line, arc, 0f, paint.textSize * 0.35f, paint)
    }
}

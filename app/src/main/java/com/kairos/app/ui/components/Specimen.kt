package com.kairos.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kairos.app.ui.art.drawGrain
import com.kairos.app.ui.theme.K
import kotlin.math.abs
import kotlin.random.Random

/** A small stamp that says where a borrowed word comes from ("from Greek"), slightly tilted like a passport stamp. */
@Composable
fun LanguageStamp(language: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        "from $language", style = K.type.caption.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp), color = color,
        modifier = modifier.rotate(-4f).border(1.4.dp, color, RoundedCornerShape(6.dp)).padding(horizontal = 7.dp, vertical = 2.dp),
    )
}

/** A loose hand-drawn underline. */
@Composable
fun Squiggle(color: Color, modifier: Modifier = Modifier) = Canvas(modifier) {
    val p = Path().apply {
        moveTo(0f, size.height * 0.6f)
        var x = 0f
        val step = size.width / 6f
        var up = true
        while (x < size.width) { quadraticTo(x + step / 2, if (up) 0f else size.height, x + step, size.height * 0.5f); x += step; up = !up }
    }
    drawPath(p, color, style = Stroke(size.height * 0.28f, cap = StrokeCap.Round))
}

private val coverPalettes = listOf(
    Triple(Color(0xFFF4EFE4), Color(0xFFE5322D), Color(0xFF1A1A1A)),
    Triple(Color(0xFFFF8A2B), Color(0xFF6A3FD1), Color(0xFF1A1A1A)),
    Triple(Color(0xFFFFD43B), Color(0xFF1F4FE0), Color(0xFF1A1A1A)),
    Triple(Color(0xFF14213D), Color(0xFFF2B8C6), Color(0xFFFFFFFF)),
    Triple(Color(0xFF2E7D5B), Color(0xFFFFE9A8), Color(0xFFFFFFFF)),
    Triple(Color(0xFFE6E0FF), Color(0xFFFF6A4D), Color(0xFF221A4A)),
    Triple(Color(0xFFE5322D), Color(0xFFFFFFFF), Color(0xFFFFFFFF)),
    Triple(Color(0xFFBFD9FF), Color(0xFF0A1CF0), Color(0xFF0A1440)),
)

/**
 * A typographic book cover, seeded from the book so it's always the same: a palette and one of four
 * mid-century layouts (Bauhaus blocks, a sun, stripes, a big vertical title), after the shelf in n17.
 */
@Composable
fun BookCover(title: String, author: String, seed: String, modifier: Modifier = Modifier) {
    val h = abs(seed.hashCode())
    val (bg, accent, ink) = coverPalettes[h % coverPalettes.size]
    val layout = ((h xor (h ushr 9)) * 31).mod(4)
    BoxWithConstraints(modifier.clip(RoundedCornerShape(6.dp)).background(bg)) {
        val coverW = maxWidth
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width; val hh = size.height
            when (layout) {
                0 -> { // Bauhaus blocks
                    drawRect(accent, Offset(0f, hh * 0.6f), Size(w * 0.5f, hh * 0.4f))
                    drawCircle(lerp(accent, ink, 0.5f), w * 0.2f, Offset(w * 0.75f, hh * 0.76f))
                    drawRect(ink, Offset(w * 0.5f, hh * 0.6f), Size(w * 0.06f, hh * 0.4f))
                }
                1 -> { // a sun over a horizon
                    drawCircle(accent, w * 0.26f, Offset(w * 0.5f, hh * 0.74f))
                    drawRect(bg, Offset(0f, hh * 0.76f), Size(w, hh * 0.24f))
                    for (i in 0..3) drawRect(accent, Offset(0f, hh * (0.8f + i * 0.05f)), Size(w, hh * 0.022f))
                }
                2 -> { // diagonal stripes
                    val r = Random(h)
                    for (i in -4..8) {
                        val x = i * w * 0.18f
                        drawLine(if (i % 2 == 0) accent else lerp(accent, bg, 0.5f), Offset(x, hh), Offset(x + w * 0.5f, hh * 0.58f), strokeWidth = w * (0.05f + r.nextFloat() * 0.03f))
                    }
                }
                else -> { // a frame
                    drawRect(accent, Offset(w * 0.06f, hh * 0.04f), Size(w * 0.88f, hh * 0.92f), style = Stroke(w * 0.025f))
                    drawRect(accent, Offset(w * 0.06f, hh * 0.66f), Size(w * 0.88f, hh * 0.3f))
                }
            }
            drawGrain(0.08f)
        }
        val titleInk = ink
        // Title and author live in the top half; the art lives in the bottom half, so they never touch.
        Column(Modifier.fillMaxWidth().fillMaxSize(0.5f).padding(start = coverW * 0.12f, end = coverW * 0.08f, top = maxHeight * 0.06f)) {
            FitText(title, K.type.display.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.02).sp), titleInk, Modifier.fillMaxWidth().weight(1f), max = 26.sp, min = 6.sp, lineHeight = 1.0f)
            Text(author, style = K.type.caption.copy(fontSize = (coverW.value / 12f).coerceIn(5f, 11f).sp), color = titleInk.copy(alpha = 0.75f), maxLines = 1, textAlign = TextAlign.Start)
        }
        // A light spine shading on the left edge makes it read as a book.
        Box(Modifier.fillMaxSize()) {
            Canvas(Modifier.fillMaxSize()) {
                drawRect(Color.Black.copy(alpha = 0.12f), Offset.Zero, Size(size.width * 0.04f, size.height))
                drawRect(Color.White.copy(alpha = 0.18f), Offset(size.width * 0.04f, 0f), Size(size.width * 0.012f, size.height))
            }
        }
    }
}

/** A dashed outline, for "more is coming" placeholders. */
@Composable
fun DashedBox(color: Color, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawRoundRect(color, style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx()))), cornerRadius = androidx.compose.ui.geometry.CornerRadius(22.dp.toPx()))
        }
        content()
    }
}


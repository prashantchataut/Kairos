package com.kairos.app.ui.screens.capsule

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import com.kairos.app.ui.brand.drawKairosMark
import com.kairos.app.ui.brand.drawSparkle
import com.kairos.app.ui.theme.Kairos
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * The capsule's envelope, drawn in code so it can open: [flap] goes 0 (sealed) → 1 (open, flap
 * folded up), and [sealCrack] 0 → 1 splits the wax seal into two halves that fall apart.
 */
@Composable
fun Envelope(
    color: Color,
    modifier: Modifier = Modifier,
    flap: Float = 0f,
    sealCrack: Float = 0f,
    glow: Float = 0f,
    letterRise: Float = 0f,
) {
    val palette = Kairos.colors
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val body = lerp(color, Color.White, if (palette.isDark) 0.1f else 0.35f)
        val deep = lerp(color, Color(0xFF1E2B4A), 0.18f)
        if (glow > 0f) {
            drawCircle(
                Brush.radialGradient(listOf(color.copy(alpha = 0.55f * glow), Color.Transparent)),
                radius = w * 0.8f, center = Offset(w / 2, h / 2),
            )
        }
        // Back of the envelope.
        drawRoundRect(deep, size = Size(w, h), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.06f))
        // The letter peeking out as it rises.
        if (letterRise > 0f) {
            val lh = h * 0.9f
            val top = h * 0.08f - lh * 0.55f * letterRise
            drawRoundRect(
                Color(0xFFFFFCF6), topLeft = Offset(w * 0.08f, top), size = Size(w * 0.84f, lh),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.03f),
            )
            for (i in 1..4) {
                val y = top + lh * 0.18f * i
                drawLine(Color(0xFF8EC5FF).copy(alpha = 0.45f), Offset(w * 0.16f, y), Offset(w * 0.84f, y), strokeWidth = 1.5f)
            }
        }
        // Front pocket.
        val pocket = Path().apply {
            moveTo(0f, h * 0.18f)
            lineTo(w / 2, h * 0.62f)
            lineTo(w, h * 0.18f)
            lineTo(w, h * 0.94f)
            quadraticTo(w, h, w * 0.94f, h)
            lineTo(w * 0.06f, h)
            quadraticTo(0f, h, 0f, h * 0.94f)
            close()
        }
        drawPath(pocket, Brush.verticalGradient(listOf(body, lerp(body, deep, 0.25f))))
        drawPath(Path().apply { moveTo(0f, h); lineTo(w / 2, h * 0.55f); lineTo(w, h) }, deep.copy(alpha = 0.25f), style = Stroke(2f))
        // Flap: a triangle that folds upward around the top edge.
        val fold = 1f - 2f * flap // 1 = down, -1 = up
        val flapPath = Path().apply {
            moveTo(0f, 0f)
            lineTo(w, 0f)
            lineTo(w / 2, h * 0.58f * fold)
            close()
        }
        drawPath(flapPath, if (fold > 0) lerp(body, Color.White, 0.15f) else deep)
        drawPath(flapPath, Color.White.copy(alpha = 0.45f), style = Stroke(2f))
        // Wax seal.
        if (flap < 0.5f) {
            val c = Offset(w / 2, h * 0.55f)
            val r = w * 0.11f
            val wax = Color(0xFFDD4F6A)
            val split = sealCrack * w * 0.12f
            listOf(-1f, 1f).forEach { side ->
                withTransform({
                    translate(side * split, split * 1.6f)
                    rotate(side * 25f * sealCrack, c)
                }) {
                    drawArc(
                        wax, startAngle = if (side < 0) 90f else -90f, sweepAngle = 180f, useCenter = true,
                        topLeft = Offset(c.x - r, c.y - r), size = Size(r * 2, r * 2),
                        alpha = 1f - sealCrack * 0.6f,
                    )
                }
            }
            if (sealCrack < 0.1f) {
                drawCircle(Color.White.copy(alpha = 0.25f), r * 0.72f, c, style = Stroke(r * 0.12f))
                drawKairosMark(Offset(c.x - r * 0.6f, c.y - r * 0.62f), r * 1.2f, ripple = false)
            }
        }
    }
}

/** A glass folder card, the shape used for sealed capsules (from the design references). */
fun folderPath(w: Float, h: Float, r: Float): Path = Path().apply {
    val tabW = w * 0.42f
    val tabH = h * 0.12f
    moveTo(0f, r + tabH)
    lineTo(0f, h - r)
    quadraticTo(0f, h, r, h)
    lineTo(w - r, h)
    quadraticTo(w, h, w, h - r)
    lineTo(w, tabH + r)
    quadraticTo(w, tabH, w - r, tabH)
    lineTo(tabW + r * 0.8f, tabH)
    quadraticTo(tabW, tabH, tabW - r * 0.3f, tabH * 0.4f)
    quadraticTo(tabW - r * 0.6f, 0f, tabW - r * 1.4f, 0f)
    lineTo(r, 0f)
    quadraticTo(0f, 0f, 0f, r)
    close()
}

/** Sparkles that orbit a ready-to-open letter. */
fun DrawScope.orbitSparkles(t: Float, color: Color, count: Int = 5) {
    val c = Offset(size.width / 2, size.height / 2)
    val rx = size.width * 0.55f
    val ry = size.height * 0.5f
    for (i in 0 until count) {
        val a = t + i * (2f * PI.toFloat() / count)
        val p = Offset(c.x + cos(a) * rx, c.y + sin(a) * ry)
        drawSparkle(p, size.minDimension * (0.035f + 0.02f * sin(a * 2f)), color)
    }
}


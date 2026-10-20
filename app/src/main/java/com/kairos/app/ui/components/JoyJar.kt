package com.kairos.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.kairos.app.ui.theme.Kairos
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * The joy jar: every good moment becomes a paint drop. Drops pack from the bottom in a loose
 * honeycomb (with a little jitter so it looks poured, not gridded). The newest drop falls in.
 */
@Composable
fun JoyJar(drops: List<Color>, modifier: Modifier = Modifier, capacity: Int = 60) {
    val palette = Kairos.colors
    val reduced = Kairos.reducedMotion
    val fall = remember { Animatable(1f) }
    val lastCount = remember { intArrayOf(-1) }
    LaunchedEffect(drops.size) {
        if (lastCount[0] in 0 until drops.size && !reduced) {
            fall.snapTo(0f)
            fall.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 180f))
        }
        lastCount[0] = drops.size
    }
    val shown = drops.takeLast(capacity)
    Canvas(modifier.semantics { contentDescription = "Joy jar with ${drops.size} good moments" }) {
        val w = size.width
        val h = size.height
        val neckW = w * 0.56f
        val bodyTop = h * 0.2f
        val jar = Path().apply {
            moveTo((w - neckW) / 2, h * 0.08f)
            lineTo((w + neckW) / 2, h * 0.08f)
            lineTo((w + neckW) / 2, bodyTop - h * 0.02f)
            cubicTo(w * 0.98f, bodyTop, w, bodyTop + h * 0.06f, w, bodyTop + h * 0.16f)
            lineTo(w, h * 0.86f)
            cubicTo(w, h * 0.96f, w * 0.94f, h, w * 0.82f, h)
            lineTo(w * 0.18f, h)
            cubicTo(w * 0.06f, h, 0f, h * 0.96f, 0f, h * 0.86f)
            lineTo(0f, bodyTop + h * 0.16f)
            cubicTo(0f, bodyTop + h * 0.06f, w * 0.02f, bodyTop, (w - neckW) / 2, bodyTop - h * 0.02f)
            close()
        }
        drawPath(jar, palette.glassFill)
        val r = w / 13f
        val rowH = r * sqrt(3f)
        val perRow = ((w - r * 0.8f) / (2 * r)).toInt().coerceAtLeast(1)
        clipPath(jar) {
            val rnd = Random(42)
            shown.forEachIndexed { i, color ->
                val row = i / perRow
                val col = i % perRow
                val offset = if (row % 2 == 1) r else 0f
                val x = r * 1.2f + col * 2 * r + offset + (rnd.nextFloat() - 0.5f) * r * 0.5f
                var y = h - r * 1.05f - row * rowH + (rnd.nextFloat() - 0.5f) * r * 0.3f
                if (i == shown.lastIndex) y = (bodyTop - r) + (y - (bodyTop - r)) * fall.value
                val c = Offset(x.coerceIn(r, w - r), y)
                drawCircle(Brush.radialGradient(listOf(lerp(color, Color.White, 0.45f), color), c.copy(c.x - r * 0.3f, c.y - r * 0.3f), r * 1.3f), r * 0.95f, c)
                drawCircle(lerp(color, Color(0xFF1E2B4A), 0.25f).copy(alpha = 0.35f), r * 0.95f, c, style = Stroke(r * 0.08f))
                drawCircle(Color.White.copy(alpha = 0.8f), r * 0.2f, Offset(c.x - r * 0.35f, c.y - r * 0.38f))
            }
        }
        // Glass: highlight streak and rim.
        drawRoundRect(
            Color.White.copy(alpha = if (palette.isDark) 0.18f else 0.55f),
            topLeft = Offset(w * 0.1f, bodyTop + h * 0.12f), size = Size(w * 0.06f, h * 0.5f),
            cornerRadius = CornerRadius(w * 0.03f),
        )
        drawPath(jar, palette.glassRimTop, style = Stroke(w * 0.022f))
        // Lid.
        drawRoundRect(
            palette.accent, topLeft = Offset((w - neckW * 1.12f) / 2, 0f), size = Size(neckW * 1.12f, h * 0.085f),
            cornerRadius = CornerRadius(h * 0.03f),
        )
        drawRoundRect(
            Color.White.copy(alpha = 0.35f), topLeft = Offset((w - neckW) / 2, h * 0.012f), size = Size(neckW, h * 0.022f),
            cornerRadius = CornerRadius(h * 0.01f),
        )
    }
}

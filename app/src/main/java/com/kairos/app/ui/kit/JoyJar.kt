package com.kairos.app.ui.kit

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kairos.app.ui.theme.Kairos
import kotlin.math.min
import kotlin.random.Random

/**
 * The joy jar: every small good thing you do drops a petal of paint into a glass jar.
 * Progress you can look at fondly, not a number to protect. The newest petal falls in.
 */
@Composable
fun JoyJar(count: Int, modifier: Modifier = Modifier) {
    val k = Kairos.colors
    val reduce = Kairos.reduceMotion
    val drop = remember { Animatable(1f) }
    val first = remember { mutableListOf(true) }
    LaunchedEffect(count) {
        if (first[0]) { first[0] = false; return@LaunchedEffect }
        if (!reduce) {
            drop.snapTo(0f)
            drop.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 160f))
        }
    }
    val washes = k.washes.all
    Canvas(modifier.semantics { contentDescription = "Joy jar with $count petals" }) {
        val w = size.width
        val h = size.height
        val neckW = w * 0.5f
        val jar = Path().apply {
            moveTo(w / 2 - neckW / 2, h * 0.08f)
            lineTo(w / 2 - neckW / 2, h * 0.18f)
            cubicTo(w * 0.04f, h * 0.24f, w * 0.04f, h * 0.3f, w * 0.06f, h * 0.5f)
            lineTo(w * 0.08f, h * 0.86f)
            quadraticTo(w * 0.1f, h * 0.98f, w * 0.26f, h * 0.98f)
            lineTo(w * 0.74f, h * 0.98f)
            quadraticTo(w * 0.9f, h * 0.98f, w * 0.92f, h * 0.86f)
            lineTo(w * 0.94f, h * 0.5f)
            cubicTo(w * 0.96f, h * 0.3f, w * 0.96f, h * 0.24f, w / 2 + neckW / 2, h * 0.18f)
            lineTo(w / 2 + neckW / 2, h * 0.08f)
            close()
        }
        drawPath(jar, Color.White.copy(alpha = if (k.isDark) 0.06f else 0.35f))
        val shown = min(count, 42)
        clipPath(jar) {
            val rnd = Random(11)
            val petal = w * 0.13f
            val perRow = 5
            for (i in 0 until shown) {
                val row = i / perRow
                val col = i % perRow
                val jitterX = (rnd.nextFloat() - 0.5f) * petal * 0.5f
                val x = w * 0.2f + col * (w * 0.6f / (perRow - 1)) + jitterX + if (row % 2 == 1) petal * 0.4f else 0f
                var y = h * 0.92f - row * petal * 0.82f + (rnd.nextFloat() - 0.5f) * petal * 0.3f
                if (i == shown - 1) y = h * 0.1f + (y - h * 0.1f) * drop.value
                val c = washes[(i * 7 + 3) % washes.size]
                drawCircle(
                    Brush.radialGradient(listOf(c.copy(alpha = 0.75f), c), center = Offset(x - petal * 0.2f, y - petal * 0.2f), radius = petal),
                    radius = petal * 0.62f,
                    center = Offset(x, y),
                )
                drawCircle(Color.White.copy(alpha = 0.45f), petal * 0.14f, Offset(x - petal * 0.2f, y - petal * 0.22f))
            }
        }
        drawPath(jar, Color.White.copy(alpha = if (k.isDark) 0.35f else 0.95f), style = Stroke(2.dp.toPx()))
        drawPath(jar, k.ink.copy(alpha = 0.12f), style = Stroke(1.dp.toPx()))
        // lid
        drawRoundRect(
            color = k.washes.peach,
            topLeft = Offset(w / 2 - neckW / 2 - w * 0.04f, 0f),
            size = androidx.compose.ui.geometry.Size(neckW + w * 0.08f, h * 0.1f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(h * 0.04f),
        )
        // glass highlight
        drawLine(Color.White.copy(alpha = 0.7f), Offset(w * 0.17f, h * 0.36f), Offset(w * 0.18f, h * 0.7f), 3.dp.toPx(), androidx.compose.ui.graphics.StrokeCap.Round)
    }
}

package com.kairos.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.Motion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Kinetic type: a headline whose words rise into place one after another, each with a small
 * overshoot and a slight tilt that straightens as it lands. Calm motion shows the words at once.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KineticText(text: String, style: TextStyle, color: Color, modifier: Modifier = Modifier, align: TextAlign = TextAlign.Center, delayMs: Long = 0, stagger: Long = 55) {
    val calm = K.calm
    val words = remember(text) { text.split(" ").filter { it.isNotEmpty() } }
    val anims = remember(text) { words.map { Animatable(if (calm) 1f else 0f) } }
    LaunchedEffect(text) {
        if (calm) return@LaunchedEffect
        delay(delayMs)
        anims.forEachIndexed { i, a -> launch { delay(i * stagger); a.animateTo(1f, Motion.bloom()) } }
    }
    val arrangement = when (align) {
        TextAlign.Center -> Arrangement.Center
        TextAlign.End -> Arrangement.End
        else -> Arrangement.Start
    }
    FlowRow(modifier, horizontalArrangement = arrangement) {
        words.forEachIndexed { i, w ->
            Text(
                "$w ", style = style, color = color,
                modifier = Modifier.graphicsLayer {
                    val t = anims[i].value
                    alpha = t.coerceIn(0f, 1f)
                    translationY = (1f - t) * size.height * 0.7f
                    rotationZ = (1f - t) * (if (i % 2 == 0) -6f else 6f)
                    val s = 0.85f + 0.15f * t
                    scaleX = s; scaleY = s
                },
            )
        }
    }
}

/** A slow field of tiny twinkling stars and sparkles drifting upward, for the sky behind onboarding. */
@Composable
fun SparkleField(phase: () -> Float, modifier: Modifier = Modifier, color: Color = Color.White, count: Int = 26) {
    val seeds = remember { List(count) { Random(it * 97 + 13).let { r -> floatArrayOf(r.nextFloat(), r.nextFloat(), 0.4f + r.nextFloat() * 0.6f, r.nextFloat()) } } }
    Canvas(modifier) {
        val t = phase()
        seeds.forEach { (x, y, s, ph) ->
            val yy = ((y - t * 0.08f * s) % 1f + 1f) % 1f
            val tw = 0.35f + 0.65f * ((sin((t * 2f + ph) * 2f * PI.toFloat()) + 1f) / 2f)
            val c = Offset(x * size.width, yy * size.height * 0.75f)
            val r = 2.2f * density * s
            if (s > 0.8f) {
                val p = androidx.compose.ui.graphics.Path().apply {
                    moveTo(c.x, c.y - r * 2.2f); quadraticTo(c.x, c.y, c.x + r * 2.2f, c.y); quadraticTo(c.x, c.y, c.x, c.y + r * 2.2f)
                    quadraticTo(c.x, c.y, c.x - r * 2.2f, c.y); quadraticTo(c.x, c.y, c.x, c.y - r * 2.2f); close()
                }
                drawPath(p, color.copy(alpha = 0.75f * tw))
            } else drawCircle(color.copy(alpha = 0.6f * tw), r * 0.7f, c)
        }
    }
}

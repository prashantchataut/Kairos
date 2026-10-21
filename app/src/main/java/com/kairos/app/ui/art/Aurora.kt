package com.kairos.app.ui.art

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.kairos.app.ui.theme.K
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * The "getting to know you" aurora (r32): violet, cyan and pink light that bleeds down from the top.
 * [rise] (0..1) is how far it has come down, [vivid] (0..1) how saturated it is.
 */
@Composable
fun Aurora(rise: () -> Float, vivid: () -> Float, modifier: Modifier = Modifier) {
    val calm = K.calm
    val phase: State<Float>? = if (!calm) rememberInfiniteTransition(label = "aurora").animateFloat(0f, 1f, infiniteRepeatable(tween(9000, easing = LinearEasing)), label = "ph") else null
    Canvas(modifier) {
        val r = rise().coerceIn(0f, 1f)
        if (r <= 0.001f) return@Canvas
        val v = vivid().coerceIn(0f, 1f)
        val t = (phase?.value ?: 0.2f) * 2f * PI.toFloat()
        val w = size.width
        val reach = size.height * (0.18f + 0.5f * r)
        val blobs = listOf(
            Triple(Color(0xFF7B5CFF), Offset(w * (0.2f + 0.08f * sin(t)), reach * 0.25f), w * 0.75f),
            Triple(Color(0xFF34CFFF), Offset(w * (0.55f + 0.1f * cos(t * 0.8f)), reach * 0.55f), w * 0.7f),
            Triple(Color(0xFFFF6FD8), Offset(w * (0.88f - 0.08f * sin(t * 1.2f)), reach * 0.3f), w * 0.65f),
            Triple(Color(0xFF2F5BFF), Offset(w * (0.5f + 0.06f * sin(t * 0.6f)), reach * 0.05f), w * 0.9f),
        )
        blobs.forEach { (c, o, rad) ->
            drawCircle(Brush.radialGradient(listOf(c.copy(alpha = (0.35f + 0.55f * v) * r), c.copy(alpha = 0f)), o, rad), rad, o)
        }
        drawGrain(0.05f * r)
    }
}

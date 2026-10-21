package com.kairos.app.ui.art

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import com.kairos.app.ui.theme.K

/**
 * Soft clouds, the recurring motif: headlines rest on them (r03), objects float on them (r10, r33).
 * A cloud is a flat-bottomed base with three puffs on top.
 */
fun cloudPath(left: Float, bottom: Float, width: Float): Path {
    val h = width * 0.42f
    val base = Path().apply {
        addRoundRect(androidx.compose.ui.geometry.RoundRect(Rect(left, bottom - h * 0.5f, left + width, bottom), h * 0.25f, h * 0.25f))
    }
    val puffs = listOf(Triple(0.28f, 0.52f, 0.22f), Triple(0.55f, 0.78f, 0.3f), Triple(0.8f, 0.5f, 0.2f))
    var out = base
    puffs.forEach { (x, y, r) ->
        val c = Path().apply { addOval(Rect(Offset(left + width * x, bottom - h * y), width * r)) }
        out = Path().apply { op(out, c, PathOperation.Union) }
    }
    return out
}

private val UnitCloud by lazy { cloudPath(0f, 0f, 1f) }

fun DrawScope.drawCloud(left: Float, bottom: Float, width: Float, color: Color, shade: Color = color) {
    withTransform({ translate(left, bottom); scale(width, width, Offset.Zero) }) {
        drawPath(UnitCloud, Brush.verticalGradient(listOf(color, shade), startY = -0.42f, endY = 0f))
    }
}

/**
 * A bank of three clouds along the bottom of this element, drifting slowly sideways. Used behind
 * headlines on the sky and in onboarding.
 */
@Composable
fun CloudBank(modifier: Modifier = Modifier, color: Color = Color.White, shade: Color = color, drift: Boolean = true) {
    val calm = K.calm
    val x = if (drift && !calm) rememberInfiniteTransition(label = "clouds").animateFloat(-1f, 1f, infiniteRepeatable(tween(14000, easing = LinearEasing), RepeatMode.Reverse), label = "x") else null
    Canvas(modifier) {
        val w = size.width
        val b = size.height
        val d = w * 0.04f * (x?.value ?: 0f)
        drawCloud(-w * 0.12f + d, b, w * 0.62f, color.copy(alpha = color.alpha * 0.85f), shade)
        drawCloud(w * 0.52f - d * 0.6f, b + w * 0.02f, w * 0.66f, color, shade)
        drawCloud(w * 0.22f + d * 0.4f, b + w * 0.08f, w * 0.7f, color, shade)
    }
}

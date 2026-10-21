package com.kairos.app.ui.art

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.Palette
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** A slow 0..2π clock for ambient motion; frozen when the person asked for calmer motion. */
@Composable
fun rememberAmbientClock(periodMs: Int = 24_000): State<Float> {
    if (K.calm) return remember { mutableFloatStateOf(1.1f) }
    return rememberInfiniteTransition(label = "ambient").animateFloat(
        0f, (2 * PI).toFloat(), infiniteRepeatable(tween(periodMs, easing = LinearEasing), RepeatMode.Restart), label = "t",
    )
}

/**
 * The sky (kairos-taste §2): cobalt → sky → pale, with aura blobs drifting over it and grain on
 * top (#606-01/02/34). [fadeTo] lets the bottom melt into the paper so content can follow.
 */
fun DrawScope.sky(p: Palette, t: Float, fadeTo: Color? = null, auraStrength: Float = 1f) {
    val h = size.height
    val w = size.width
    drawRect(
        Brush.verticalGradient(
            if (fadeTo != null) listOf(p.skyTop, p.skyMid, lerp(p.skyMid, p.skyLow, 0.6f), fadeTo)
            else listOf(p.skyTop, p.skyMid, p.skyLow),
        )
    )
    val blobs = listOf(
        Triple(Offset(w * (0.18f + 0.06f * sin(t)), h * (0.22f + 0.04f * cos(t * 1.3f))), w * 0.55f, p.auras[0]),
        Triple(Offset(w * (0.92f + 0.05f * cos(t)), h * (0.12f + 0.05f * sin(t * 0.9f))), w * 0.5f, p.auras[1]),
        Triple(Offset(w * (0.62f + 0.07f * sin(t * 0.7f + 2f)), h * (0.62f + 0.05f * cos(t))), w * 0.6f, p.auras[2]),
    )
    blobs.forEach { (c, r, col) ->
        drawCircle(Brush.radialGradient(listOf(col.copy(alpha = 0.55f * auraStrength), col.copy(alpha = 0f)), c, r), r, c)
    }
    if (fadeTo != null) drawRect(Brush.verticalGradient(0.7f to Color.Transparent, 1f to fadeTo))
    grain(if (p.isDark) 0.07f else 0.09f)
}

/** A full-bleed living sky behind [content]. */
@Composable
fun SkyBox(modifier: Modifier = Modifier, fadeToPaper: Boolean = true, content: @Composable BoxScope.() -> Unit) {
    val p = K.colors
    val t = rememberAmbientClock()
    Box(modifier) {
        Canvas(Modifier.matchParentSize()) { sky(p, t.value, if (fadeToPaper) p.paper else null) }
        content()
    }
}

/** Paper with an aura glow in one corner — for screens that start on paper. */
@Composable
fun PaperGlow(modifier: Modifier = Modifier, colors: List<Color> = K.colors.auras) {
    val p = K.colors
    val t = rememberAmbientClock(30_000)
    Canvas(modifier.fillMaxSize()) {
        drawRect(p.paper)
        val c1 = Offset(size.width * (0.95f + 0.04f * sin(t.value)), size.height * 0.04f)
        drawCircle(Brush.radialGradient(listOf(colors[0].copy(alpha = 0.32f), Color.Transparent), c1, size.width * 0.7f), size.width * 0.7f, c1)
        val c2 = Offset(size.width * 0.02f, size.height * (0.3f + 0.03f * cos(t.value)))
        drawCircle(Brush.radialGradient(listOf(colors[1].copy(alpha = 0.22f), Color.Transparent), c2, size.width * 0.6f), size.width * 0.6f, c2)
        grain(if (p.isDark) 0.05f else 0.06f)
    }
}

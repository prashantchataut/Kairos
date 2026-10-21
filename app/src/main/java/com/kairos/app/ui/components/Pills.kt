package com.kairos.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kairos.app.ui.art.Bit
import com.kairos.app.ui.art.Kai
import com.kairos.app.ui.art.KaiLook
import com.kairos.app.ui.art.burst
import com.kairos.app.ui.art.rememberBurst
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.Motion
import kotlinx.coroutines.launch

/**
 * A giant choice pill (r15). Unselected it is a dashed outline with a little character; tapping it
 * closes the dashes into a solid line, floods it with colour, turns + into −, pops, and throws a few
 * sparks from the button. [onSky] = false gives the canvas version.
 */
@Composable
fun BigPill(
    text: String,
    selected: Boolean,
    look: KaiLook,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSky: Boolean = true,
    fontSize: Int = 26,
) {
    val p = K.colors
    val calm = K.calm
    val on by animateFloatAsState(if (selected) 1f else 0f, Motion.soft(), label = "pill")
    val pop = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    val burst = rememberBurst()
    var size by remember { mutableStateOf(IntSize.Zero) }
    val dens = LocalDensity.current.density
    val line = if (onSky) Color.White else p.inkFaint
    val fill = if (onSky) Color.White else p.surface
    val ink = lerp(if (onSky) Color.White else p.ink, p.ink, on)
    Box(
        modifier
            .semantics { this.selected = selected }
            .onSizeChanged { size = it }
            .graphicsLayer { scaleX = pop.value; scaleY = pop.value }
            .burst(burst)
            .pressable(role = Role.Checkbox, label = text, scale = 0.95f) {
                if (!calm) {
                    scope.launch { pop.snapTo(0.9f); pop.animateTo(1f, Motion.bloom()) }
                    if (!selected) scope.launch {
                        burst.fire(Offset(size.width - 27f * dens, size.height / 2f), kinds = listOf(Bit.Spark, Bit.Dot, Bit.Petal), count = 10, power = 0.55f)
                    }
                }
                onClick()
            }
            .drawBehind {
                val r = CornerRadius(this.size.height / 2)
                drawRoundRect(fill.copy(alpha = on * (if (onSky) 0.94f else 1f)), cornerRadius = r)
                val gap = 7.dp.toPx() * (1f - on)
                drawRoundRect(
                    line.copy(alpha = (if (onSky) 0.8f else 0.6f) * (1f - on * 0.75f)),
                    cornerRadius = r,
                    style = Stroke(1.6.dp.toPx(), pathEffect = if (gap > 0.3f) PathEffect.dashPathEffect(floatArrayOf(9.dp.toPx(), gap)) else null),
                )
            }
            .padding(start = 8.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Kai(if (selected) look.copy(eyes = 0f, smile = 1f, blush = 0.7f) else look, size = 40.dp, shadow = false, alive = false, description = null)
            Spacer(Modifier.width(10.dp))
            Text(text, style = K.type.display.copy(fontSize = fontSize.sp, lineHeight = (fontSize + 4).sp, fontWeight = FontWeight.Bold), color = ink, maxLines = 1)
            Spacer(Modifier.width(12.dp))
            Canvas(Modifier.size(32.dp)) {
                drawCircle(lerp(if (onSky) Color.White.copy(alpha = 0.3f) else p.surfaceSoft, p.accent, on))
                val c = center
                val arm = this.size.width * 0.24f
                val w = 2.4.dp.toPx()
                val plusInk = lerp(if (onSky) Color.White else p.ink, Color.White, on)
                drawLine(plusInk, Offset(c.x - arm, c.y), Offset(c.x + arm, c.y), w, StrokeCap.Round)
                val v = arm * (1f - on)
                if (v > 0.5f) drawLine(plusInk, Offset(c.x, c.y - v), Offset(c.x, c.y + v), w, StrokeCap.Round)
            }
        }
    }
}

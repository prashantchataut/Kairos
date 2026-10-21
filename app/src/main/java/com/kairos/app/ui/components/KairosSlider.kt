package com.kairos.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kairos.app.ui.theme.Candy
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.Motion
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt

/** Colour at [f] (0..1) along a list of stops. */
fun List<Color>.at(f: Float): Color {
    if (size == 1) return first()
    val x = f.coerceIn(0f, 1f) * (size - 1)
    val i = x.toInt().coerceAtMost(size - 2)
    return lerp(this[i], this[i + 1], x - i)
}

/**
 * The adaptive slider. A row of rounded bars (r04) whose heights swell around the thumb and whose
 * colour follows the value; a glossy thumb that stretches with drag speed and springs back; a ripple
 * and a haptic tick at every step; and a [bubble] above the thumb whose content adapts to the value
 * (a word for the level, Kai's face for a mood, a date for a letter).
 *
 * @param stops number of discrete positions (0 = continuous). Values are reported in 0..1.
 */
@Composable
fun KairosSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    description: String,
    stateText: (Float) -> String,
    modifier: Modifier = Modifier,
    stops: Int = 0,
    colors: List<Color> = listOf(Candy.Sky, Candy.Cobalt),
    onDark: Boolean = false,
    bars: Int = 34,
    trackHeight: Dp = 56.dp,
    onValueChangeFinished: (() -> Unit)? = null,
    bubble: (@Composable (Float) -> Unit)? = null,
) {
    val p = K.colors
    val calm = K.calm
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val change by rememberUpdatedState(onValueChange)
    val finished by rememberUpdatedState(onValueChangeFinished)

    fun snap(f: Float) = if (stops >= 2) (f * (stops - 1)).roundToInt().toFloat() / (stops - 1) else f.coerceIn(0f, 1f)
    fun indexOf(f: Float) = if (stops >= 2) (f * (stops - 1)).roundToInt() else -1

    val thumb = remember { Animatable(value.coerceIn(0f, 1f)) }
    val stretch = remember { Animatable(0f) }
    val ripple = remember { Animatable(1f) }
    var dragging by remember { mutableStateOf(false) }
    var lastIndex by remember { mutableIntStateOf(indexOf(value)) }

    LaunchedEffect(value) { if (!dragging && abs(thumb.value - value) > 0.001f) thumb.animateTo(value.coerceIn(0f, 1f), Motion.soft()) }

    fun report(f: Float) {
        val s = snap(f)
        val idx = indexOf(s)
        if (stops >= 2 && idx != lastIndex) {
            lastIndex = idx
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            if (!calm) scope.launch { ripple.snapTo(0f); ripple.animateTo(1f, tween(520)) }
        }
        change(s)
    }

    Column(modifier) {
        if (bubble != null) {
            BoxWithConstraints(Modifier.fillMaxWidth().height(52.dp)) {
                val wPx = with(density) { maxWidth.toPx() }
                val pad = with(density) { 20.dp.toPx() }
                Box(
                    Modifier
                        .align(Alignment.BottomStart)
                        .layout { m, c ->
                            val pl = m.measure(c.copy(minWidth = 0))
                            layout(pl.width, pl.height) {
                                val x = (pad + (wPx - pad * 2) * thumb.value - pl.width / 2f).coerceIn(0f, wPx - pl.width)
                                pl.place(x.roundToInt(), 0)
                            }
                        }
                ) { bubble(snap(thumb.value)) }
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .semantics {
                    contentDescription = description
                    stateDescription = stateText(snap(thumb.value))
                    progressBarRangeInfo = ProgressBarRangeInfo(snap(thumb.value), 0f..1f, if (stops >= 2) stops - 2 else 0)
                    setProgress { v -> scope.launch { thumb.animateTo(snap(v), Motion.soft()) }; report(v); finished?.invoke(); true }
                }
                .pointerInput(stops) {
                    val pad = 20.dp.toPx()
                    fun frac(x: Float) = ((x - pad) / (size.width - pad * 2)).coerceIn(0f, 1f)
                    detectTapGestures { o ->
                        val f = snap(frac(o.x))
                        scope.launch { thumb.animateTo(f, Motion.bloom()) }
                        if (!calm) scope.launch { stretch.snapTo(0.6f); stretch.animateTo(0f, Motion.bloom()) }
                        report(f)
                        finished?.invoke()
                    }
                }
                .pointerInput(stops) {
                    val pad = 20.dp.toPx()
                    fun frac(x: Float) = ((x - pad) / (size.width - pad * 2)).coerceIn(0f, 1f)
                    detectHorizontalDragGestures(
                        onDragStart = { o -> dragging = true; scope.launch { thumb.snapTo(frac(o.x)) }; report(frac(o.x)) },
                        onDragEnd = {
                            dragging = false
                            val f = snap(thumb.value)
                            scope.launch { thumb.animateTo(f, Motion.bloom()) }
                            scope.launch { stretch.animateTo(0f, Motion.bloom()) }
                            finished?.invoke()
                        },
                        onDragCancel = { dragging = false; scope.launch { stretch.animateTo(0f, Motion.bloom()) } },
                    ) { change, dx ->
                        change.consume()
                        val f = frac(change.position.x)
                        scope.launch { thumb.snapTo(f) }
                        if (!calm) scope.launch { stretch.snapTo((abs(dx) / 14f).coerceIn(0f, 1f).coerceAtLeast(stretch.value * 0.85f)) }
                        report(f)
                    }
                },
        ) {
            Canvas(Modifier.fillMaxWidth().height(trackHeight)) {
                val pad = 20.dp.toPx()
                val w = size.width - pad * 2
                val cy = size.height / 2
                val f = thumb.value
                val barW = (w / bars) * 0.46f
                val maxH = size.height * 0.78f
                val minH = size.height * 0.16f
                val rip = ripple.value
                val idle = if (onDark) Color.White.copy(alpha = 0.22f) else p.line
                for (i in 0 until bars) {
                    val bf = i / (bars - 1f)
                    val d = (bf - f) * bars
                    var hh = minH + (maxH - minH) * exp(-(d * d) / (2f * 2.4f * 2.4f))
                    if (rip < 1f) {
                        val front = rip * bars * 0.6f
                        hh += maxH * 0.22f * (1f - rip) * exp(-((abs(d) - front) * (abs(d) - front)) / 3f)
                    }
                    val c = if (bf <= f) lerp(colors.at(bf), Color.White, if (onDark) 0.1f else 0f).copy(alpha = 0.55f + 0.45f * exp(-(d * d) / 40f)) else idle
                    val x = pad + bf * w - barW / 2
                    drawRoundRect(c, Offset(x, cy - hh / 2), Size(barW, hh.coerceAtMost(size.height)), CornerRadius(barW / 2))
                }
                // The thumb: a glossy pill, stretched by drag speed.
                val tx = pad + f * w
                val s = stretch.value
                val tw = 22.dp.toPx()
                val th = size.height * 0.9f
                val col = colors.at(f)
                withTransform({ scale(1f + s * 0.32f, 1f - s * 0.16f, Offset(tx, cy)) }) {
                    drawRoundRect(Color.Black.copy(alpha = 0.14f), Offset(tx - tw / 2, cy - th / 2 + 3.dp.toPx()), Size(tw, th), CornerRadius(tw / 2))
                    drawRoundRect(Color.White, Offset(tx - tw / 2, cy - th / 2), Size(tw, th), CornerRadius(tw / 2))
                    val iw = tw - 8.dp.toPx()
                    val ih = th - 8.dp.toPx()
                    drawRoundRect(Brush.verticalGradient(listOf(lerp(col, Color.White, 0.35f), col), startY = cy - ih / 2, endY = cy + ih / 2), Offset(tx - iw / 2, cy - ih / 2), Size(iw, ih), CornerRadius(iw / 2))
                    drawRoundRect(Color.White.copy(alpha = 0.5f), Offset(tx - iw / 2 + iw * 0.22f, cy - ih / 2 + 4.dp.toPx()), Size(iw * 0.28f, ih * 0.32f), CornerRadius(iw))
                }
            }
        }
    }
}

/** A small floating label for the slider bubble. */
@Composable
fun SliderBubble(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text,
        style = K.type.label.copy(fontWeight = FontWeight.SemiBold),
        color = Color.White,
        modifier = modifier
            .offset(y = (-4).dp)
            .background(color, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

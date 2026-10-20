package com.kairos.app.ui.today

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kairos.app.container
import com.kairos.app.engine.critters.DewReason
import com.kairos.app.ui.components.DetailTopBar
import com.kairos.app.ui.glass.ButtonTone
import com.kairos.app.ui.glass.KButton
import com.kairos.app.ui.glass.KChip
import com.kairos.app.ui.nav.LocalNavigator
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Space
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class Pattern(val name: String, val phases: List<Pair<String, Int>>)

private val patterns = listOf(
    Pattern("Calm", listOf("Breathe in" to 4000, "Breathe out" to 6000)),
    Pattern("Box", listOf("Breathe in" to 4000, "Hold" to 4000, "Breathe out" to 4000, "Hold" to 4000)),
    Pattern("Sleepy", listOf("Breathe in" to 4000, "Hold" to 7000, "Breathe out" to 8000)),
)

@Composable
fun BreatheScreen() {
    val nav = LocalNavigator.current
    val colors = Kairos.colors
    val haptics = LocalHapticFeedback.current
    val container = LocalContext.current.container
    val scope = rememberCoroutineScope()
    var pattern by remember { mutableStateOf(patterns[0]) }
    var running by remember { mutableStateOf(false) }
    var label by remember { mutableStateOf("Tap begin when you're ready") }
    var cycles by remember { mutableIntStateOf(0) }
    val size = remember { Animatable(0.42f) }
    val wobble = remember { Animatable(0f) }

    LaunchedEffect(running, pattern) {
        if (!running) {
            size.animateTo(0.42f, tween(800))
            return@LaunchedEffect
        }
        while (isActive) {
            for ((name, ms) in pattern.phases) {
                label = name
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                when (name) {
                    "Breathe in" -> size.animateTo(1f, tween(ms, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                    "Breathe out" -> size.animateTo(0.42f, tween(ms, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                    else -> kotlinx.coroutines.delay(ms.toLong())
                }
            }
            cycles++
            if (cycles == 4) scope.launch { container.pond.award(DewReason.Breathe) }
        }
    }
    LaunchedEffect(Unit) {
        while (isActive) wobble.animateTo(wobble.value + 2f * PI.toFloat(), tween(6000, easing = LinearEasing))
    }

    Column(Modifier.fillMaxSize()) {
        DetailTopBar(onBack = nav::back, title = "Breathe")
        Row(Modifier.fillMaxWidth().padding(horizontal = Space.screen), horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
            patterns.forEach { p -> KChip(p.name, p == pattern, { pattern = p; running = false; cycles = 0 }) }
        }
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val c = Offset(this.size.width / 2, this.size.height / 2)
                val maxR = this.size.minDimension * 0.42f
                val r = maxR * size.value
                // ripples drifting out as you exhale
                repeat(3) { i ->
                    val rr = r * (1.12f + i * 0.14f)
                    drawCircle(colors.pond.copy(alpha = 0.10f - i * 0.03f), rr, c, style = Stroke(2.dp.toPx()))
                }
                val path = androidx.compose.ui.graphics.Path()
                val steps = 90
                for (i in 0..steps) {
                    val a = i / steps.toFloat() * 2f * PI.toFloat()
                    val k = 1f + 0.025f * sin(3 * a + wobble.value) + 0.018f * cos(5 * a - wobble.value * 1.4f)
                    val x = c.x + cos(a) * r * k
                    val y = c.y + sin(a) * r * k
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                drawPath(path, Brush.radialGradient(listOf(Color.White.copy(alpha = 0.9f), colors.sky, colors.mint.copy(alpha = 0.9f)), center = Offset(c.x - r * 0.3f, c.y - r * 0.4f), radius = r * 1.8f))
                drawPath(path, Brush.linearGradient(listOf(Color.White, Color.White.copy(alpha = 0f)), start = Offset(c.x - r, c.y - r), end = Offset(c.x + r, c.y + r)), style = Stroke(3.dp.toPx()))
                drawArc(Color.White.copy(alpha = 0.8f), 200f, 50f, false, Offset(c.x - r * 0.75f, c.y - r * 0.75f), androidx.compose.ui.geometry.Size(r * 1.1f, r * 1.1f), style = Stroke(r * 0.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
            }
            AnimatedContent(label, transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(300)) }, label = "phase") { l ->
                Text(
                    l, style = Kairos.type.title, color = Color(0xFF1B2340), textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 64.dp).semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
        }
        Column(
            Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).padding(Space.screen),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                if (cycles == 0) "Follow the bubble. Nothing else to do." else "$cycles breath${if (cycles > 1) "s" else ""} · that's enough, or keep going",
                style = Kairos.type.bodyRegular, color = colors.inkSoft, textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Space.lg))
            KButton(if (running) "Pause" else "Begin", { running = !running }, tone = if (running) ButtonTone.Glass else ButtonTone.Pond, modifier = Modifier.fillMaxWidth())
        }
    }
}

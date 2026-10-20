package com.kairos.app.ui.you

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.kairos.app.AppContainer
import com.kairos.app.engine.Critter
import com.kairos.app.engine.PondState
import com.kairos.app.engine.critters.Hatchery
import com.kairos.app.engine.critters.Rarity
import com.kairos.app.ui.art.CritterView
import com.kairos.app.ui.art.drawSparkle
import com.kairos.app.ui.capsule.formatDate
import com.kairos.app.ui.components.DetailTopBar
import com.kairos.app.ui.components.DewBadge
import com.kairos.app.ui.components.Eyebrow
import com.kairos.app.ui.components.PillLabel
import com.kairos.app.ui.components.ProgressRing
import com.kairos.app.ui.components.SectionTitle
import com.kairos.app.ui.components.clickableNoRipple
import com.kairos.app.ui.components.kairosViewModel
import com.kairos.app.ui.glass.ButtonTone
import com.kairos.app.ui.glass.GlassCard
import com.kairos.app.ui.glass.GlassWeight
import com.kairos.app.ui.glass.KButton
import com.kairos.app.ui.nav.LocalNavigator
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Motion
import com.kairos.app.ui.theme.Radii
import com.kairos.app.ui.theme.Space
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class PondexViewModel(private val c: AppContainer) : ViewModel() {
    val pond = c.pond.observe().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    private val _hatched = MutableStateFlow<Critter?>(null)
    val hatched = _hatched.asStateFlow()

    fun hatch(first: Boolean) = viewModelScope.launch { _hatched.value = c.pond.hatch(if (first) "first" else "egg") }
    fun dismiss() { _hatched.value = null }
    fun buddy(id: String) = viewModelScope.launch { c.pond.setBuddy(id) }
    fun rename(id: String, name: String) = viewModelScope.launch { if (name.isNotBlank()) c.pond.rename(id, name) }
}

@Composable
fun PondexScreen() {
    val vm = kairosViewModel { PondexViewModel(it) }
    val pond by vm.pond.collectAsStateWithLifecycle()
    val hatched by vm.hatched.collectAsStateWithLifecycle()
    val nav = LocalNavigator.current
    var selected by remember { mutableStateOf<String?>(null) }
    val p = pond ?: return
    val colors = Kairos.colors

    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = Space.huge)) {
            item { DetailTopBar(onBack = nav::back, title = "The pond", actions = { DewBadge(p.dew) }) }
            item { EggCard(p, onHatch = { vm.hatch(p.critters.isEmpty()) }, modifier = Modifier.padding(horizontal = Space.screen)) }
            item {
                Column(Modifier.padding(horizontal = Space.screen)) {
                    SectionTitle("Puddlings · ${p.critters.size}")
                    Text(
                        "Dew comes from checking in, learning, writing letters, kind things and play. Every ${Hatchery.EGG_COST} dew lays an egg. Some hatchlings are rare — and once in a long while, one shimmers.",
                        style = Kairos.type.bodyRegular, color = colors.inkSoft,
                    )
                    Spacer(Modifier.height(Space.md))
                }
            }
            items(p.critters.chunked(3), key = { row -> row.first().entity.id }) { row ->
                Row(Modifier.padding(horizontal = Space.screen, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                    row.forEach { c -> CritterTile(c, Modifier.weight(1f)) { selected = c.entity.id } }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
        val sel = p.critters.firstOrNull { it.entity.id == selected }
        AnimatedVisibility(sel != null, enter = fadeIn(tween(220)), exit = fadeOut(tween(160))) {
            if (sel != null) CritterSheet(sel, onClose = { selected = null }, onBuddy = { vm.buddy(sel.entity.id) }, onRename = { vm.rename(sel.entity.id, it) })
        }
        AnimatedVisibility(hatched != null, enter = fadeIn(tween(250)), exit = fadeOut(tween(200))) {
            hatched?.let { HatchOverlay(it, onDone = vm::dismiss) }
        }
    }
}

@Composable
private fun EggCard(p: PondState, onHatch: () -> Unit, modifier: Modifier = Modifier) {
    val colors = Kairos.colors
    val ready = p.eggsReady > 0 || p.critters.isEmpty()
    GlassCard(modifier.fillMaxWidth(), blur = true, weight = GlassWeight.Frosted, shape = Radii.sheet) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(104.dp), contentAlignment = Alignment.Center) {
                ProgressRing(if (ready) 1f else p.progress, Modifier.size(104.dp), stroke = 6.dp, color = if (ready) colors.success else colors.pond)
                Egg(Modifier.size(70.dp), wobble = ready)
            }
            Spacer(Modifier.width(Space.lg))
            Column(Modifier.weight(1f)) {
                Eyebrow(if (ready) "Something is moving!" else "Next egg")
                Text(
                    when {
                        p.critters.isEmpty() -> "Your first egg is a gift"
                        ready -> "${p.eggsReady} egg${if (p.eggsReady > 1) "s" else ""} ready to hatch"
                        else -> "${(p.progress * Hatchery.EGG_COST).toInt()} / ${Hatchery.EGG_COST} dew"
                    },
                    style = Kairos.type.heading, color = colors.ink,
                )
                Spacer(Modifier.height(Space.sm))
                if (ready) KButton("Hatch", onHatch, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 22.dp, vertical = 10.dp))
                else Text("Keep being kind to yourself. It adds up.", style = Kairos.type.bodyRegular, color = colors.inkSoft)
            }
        }
    }
}

/** A speckled watercolor egg. With [wobble] it rocks like something inside wants out. */
@Composable
fun Egg(modifier: Modifier = Modifier, wobble: Boolean = false, crack: Float = 0f) {
    val rock = remember { Animatable(0f) }
    val reduced = Kairos.reducedMotion
    LaunchedEffect(wobble) {
        while (wobble && !reduced) {
            rock.animateTo(0f, keyframes { durationMillis = 900; 8f at 120; -7f at 260; 5f at 400; -3f at 520; 0f at 640 })
            delay(1400)
        }
    }
    Canvas(modifier.graphicsLayer { rotationZ = rock.value; transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.9f) }) {
        val w = size.width
        val h = size.height
        val egg = Path().apply {
            moveTo(w / 2, h * 0.04f)
            cubicTo(w * 0.86f, h * 0.06f, w * 0.98f, h * 0.62f, w * 0.9f, h * 0.74f)
            cubicTo(w * 0.8f, h * 0.98f, w * 0.2f, h * 0.98f, w * 0.1f, h * 0.74f)
            cubicTo(w * 0.02f, h * 0.62f, w * 0.14f, h * 0.06f, w / 2, h * 0.04f)
            close()
        }
        drawPath(egg, Brush.radialGradient(listOf(Color.White, Color(0xFFFFF1DC), Color(0xFFF6D9B8)), center = Offset(w * 0.38f, h * 0.3f), radius = w))
        listOf(Offset(0.35f, 0.5f) to 0.06f, Offset(0.62f, 0.36f) to 0.045f, Offset(0.58f, 0.68f) to 0.07f, Offset(0.3f, 0.75f) to 0.04f).forEach { (o, r) ->
            drawCircle(Color(0xFF9CC4FF).copy(alpha = 0.7f), w * r, Offset(w * o.x, h * o.y))
        }
        drawPath(egg, Color.White.copy(alpha = 0.8f), style = Stroke(w * 0.03f))
        drawCircle(Color.White.copy(alpha = 0.9f), w * 0.06f, Offset(w * 0.36f, h * 0.24f))
        if (crack > 0f) {
            val zig = Path().apply {
                moveTo(w * 0.12f, h * 0.52f)
                val pts = listOf(0.22f to 0.44f, 0.32f to 0.55f, 0.44f to 0.43f, 0.56f to 0.56f, 0.68f to 0.44f, 0.78f to 0.55f, 0.9f to 0.47f)
                val n = (pts.size * crack).toInt().coerceIn(1, pts.size)
                pts.take(n).forEach { (x, y) -> lineTo(w * x, h * y) }
            }
            drawPath(zig, Color(0xFF8C6B4A), style = Stroke(w * 0.025f, cap = StrokeCap.Round))
        }
    }
}

@Composable
private fun CritterTile(c: Critter, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = Kairos.colors
    GlassCard(modifier, onClick = onClick, contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            CritterView(c.genome, size = 84.dp, alive = false, name = c.entity.name)
            Text(c.entity.name, style = Kairos.type.label, color = colors.ink, maxLines = 1)
            Text(
                if (c.entity.isBuddy) "Buddy" else c.genome.rarity.label,
                style = Kairos.type.caption.copy(letterSpacing = 0.sp),
                color = when {
                    c.entity.isBuddy -> colors.pond
                    c.genome.rarity >= Rarity.Rare -> colors.warn
                    else -> colors.inkSoft
                },
            )
        }
    }
}

@Composable
private fun CritterSheet(c: Critter, onClose: () -> Unit, onBuddy: () -> Unit, onRename: (String) -> Unit) {
    val colors = Kairos.colors
    var name by remember(c.entity.id) { mutableStateOf(c.entity.name) }
    Box(Modifier.fillMaxSize().background(colors.paper.copy(alpha = 0.6f)).clickableNoRipple(onClose), contentAlignment = Alignment.BottomCenter) {
        GlassCard(
            Modifier.fillMaxWidth().padding(Space.md).clickableNoRipple { },
            blur = true, weight = GlassWeight.Frosted, shape = Radii.sheet,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                CritterView(c.genome, size = 180.dp, name = c.entity.name)
                Box {
                    BasicTextField(
                        name, { name = it.take(18) }, singleLine = true,
                        textStyle = Kairos.type.title.copy(color = colors.ink, textAlign = TextAlign.Center),
                        cursorBrush = SolidColor(colors.pond),
                        modifier = Modifier.width(240.dp),
                    )
                }
                Text("Tap the name to rename", style = Kairos.type.caption, color = colors.inkFaint)
                Spacer(Modifier.height(Space.md))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PillLabel(c.genome.rarity.label, color = if (c.genome.rarity >= Rarity.Rare) colors.warn else colors.ink)
                    PillLabel(c.genome.habitat)
                }
                Spacer(Modifier.height(Space.md))
                Text("${c.entity.name} ${c.genome.trait}.", style = Kairos.type.body, color = colors.ink, textAlign = TextAlign.Center)
                Text("Hatched ${formatDate(c.entity.hatchedAt)}", style = Kairos.type.caption, color = colors.inkSoft)
                Spacer(Modifier.height(Space.lg))
                Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                    KButton(if (c.entity.isBuddy) "Your buddy" else "Make buddy", onBuddy, tone = ButtonTone.Glass, enabled = !c.entity.isBuddy, modifier = Modifier.weight(1f))
                    KButton("Done", { if (name != c.entity.name) onRename(name); onClose() }, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/** Egg rocks, cracks, bursts into light, and a Puddling bounces out. */
@Composable
fun HatchOverlay(c: Critter, onDone: () -> Unit) {
    val colors = Kairos.colors
    val haptics = LocalHapticFeedback.current
    val crack = remember { Animatable(0f) }
    val burst = remember { Animatable(0f) }
    val pop = remember { Animatable(0f) }
    var reveal by remember { mutableStateOf(false) }
    LaunchedEffect(c.entity.id) {
        delay(300)
        repeat(3) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            crack.animateTo((it + 1) / 3f, tween(380))
            delay(260)
        }
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        reveal = true
        launch { burst.animateTo(1f, tween(700, easing = FastOutSlowInEasing)) }
        pop.animateTo(1f, Motion.playful())
    }
    Box(Modifier.fillMaxSize().background(colors.paper.copy(alpha = 0.88f)).clickableNoRipple { if (reveal) onDone() }, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(260.dp), contentAlignment = Alignment.Center) {
                if (burst.value > 0f && burst.value < 1f) {
                    Canvas(Modifier.fillMaxSize()) {
                        val b = burst.value
                        repeat(12) { i ->
                            val a = i / 12f * 2f * PI.toFloat()
                            val d = size.width * (0.15f + 0.4f * b)
                            drawSparkle(Offset(center.x + cos(a) * d, center.y + sin(a) * d), size.width * 0.04f * (1 - b), Color(0xFFFFC857).copy(alpha = 1 - b))
                        }
                        drawCircle(Color(0xFFFFF4C8).copy(alpha = (1 - b) * 0.8f), size.width * 0.5f * b, center)
                    }
                }
                if (!reveal) {
                    Egg(Modifier.size(150.dp, 180.dp), wobble = true, crack = crack.value)
                } else {
                    Box(Modifier.graphicsLayer { scaleX = pop.value; scaleY = pop.value }) {
                        CritterView(c.genome, size = 220.dp, name = c.entity.name)
                    }
                }
            }
            Spacer(Modifier.height(Space.lg))
            AnimatedVisibility(reveal, enter = fadeIn(tween(400, delayMillis = 200))) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Eyebrow(if (c.genome.rarity == Rarity.Shiny) "A shimmering one!" else "${c.genome.rarity.label} Puddling", color = colors.pond)
                    Text("Meet ${c.entity.name}!", style = Kairos.type.display, color = colors.ink)
                    Text("${c.entity.name} ${c.genome.trait}.", style = Kairos.type.bodyRegular, color = colors.inkSoft, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = Space.xl))
                    Spacer(Modifier.height(Space.xl))
                    KButton("Welcome to the pond", onDone)
                }
            }
        }
    }
}

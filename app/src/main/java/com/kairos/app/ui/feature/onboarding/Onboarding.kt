package com.kairos.app.ui.feature.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.engine.Topics
import com.kairos.app.ui.art.Aurora
import com.kairos.app.ui.art.Bit
import com.kairos.app.ui.art.CloudBank
import com.kairos.app.ui.art.Kai
import com.kairos.app.ui.art.KaiLook
import com.kairos.app.ui.art.KaiLooks
import com.kairos.app.ui.art.burst
import com.kairos.app.ui.art.drawBloom
import com.kairos.app.ui.art.rememberBurst
import com.kairos.app.ui.components.BigPill
import com.kairos.app.ui.components.Icons
import com.kairos.app.ui.components.KairosSlider
import com.kairos.app.ui.components.KineticText
import com.kairos.app.ui.components.SparkleField
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.SliderBubble
import com.kairos.app.ui.components.at
import com.kairos.app.ui.components.pressable
import com.kairos.app.ui.feature.learn.categoryTint
import com.kairos.app.ui.feature.learn.themeTint
import com.kairos.app.ui.feature.you.KeyPhase
import com.kairos.app.ui.feature.you.KeyRitual
import com.kairos.app.ui.theme.Candy
import com.kairos.app.ui.theme.Gap
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.Motion
import com.kairos.app.ui.theme.candy
import com.kairos.app.util.appContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

/*
 * Onboarding, after r03 (Abode), r15, r02 and r32. One hero layer (Kai and three friends) persists
 * across every step and moves, resizes and changes expression while the words slide. A white cloud
 * field rises and falls between steps: at the start the headline rests on clouds, during the
 * choices it sinks away to leave a full sky, and at the end it rises over everything for the
 * aurora finale.
 */

private const val HELLO = 0
private const val GIFTS = 1
private const val TOPICS = 2
private const val THEMES = 3
private const val LEVEL = 4
private const val NAME = 5
private const val KEY = 6
private const val FINALE = 7

private data class Spot(val x: Float, val y: Float, val size: Dp)

private fun kaiSpot(step: Int) = when (step) {
    HELLO -> Spot(0.5f, 0.3f, 156.dp)
    GIFTS -> Spot(0.5f, 0.16f, 92.dp)
    TOPICS, THEMES -> Spot(0.84f, 0.085f, 64.dp)
    LEVEL -> Spot(0.5f, 0.17f, 100.dp)
    NAME -> Spot(0.5f, 0.22f, 132.dp)
    KEY -> Spot(0.5f, 0.17f, 116.dp)
    else -> Spot(0.5f, 0.3f, 172.dp)
}

private val friendLooks = listOf(
    KaiLooks.Okay.copy(smile = 0.6f, blush = 0.4f, gaze = Offset(0.4f, 0.2f)),
    KaiLooks.Low.copy(lid = 0f, gaze = Offset.Zero, smile = 0.5f, blush = 0.5f),
    KaiLooks.Radiant,
)
private val friendLabels = listOf("a word", "a thought", "a small good thing")

private fun friendSpot(step: Int, i: Int): Spot = when (step) {
    HELLO -> listOf(Spot(0.17f, 0.15f, 58.dp), Spot(0.85f, 0.2f, 50.dp), Spot(0.21f, 0.43f, 44.dp))[i]
    GIFTS -> listOf(Spot(0.2f, 0.37f, 86.dp), Spot(0.5f, 0.41f, 98.dp), Spot(0.8f, 0.37f, 86.dp))[i]
    FINALE -> listOf(Spot(0.15f, 0.15f, 56.dp), Spot(0.86f, 0.2f, 50.dp), Spot(0.74f, 0.07f, 40.dp))[i]
    else -> friendSpot(GIFTS, i).copy(size = 0.dp)
}

/** Where the top of the white cloud field sits, as a fraction of the screen height. */
private fun fieldTop(step: Int) = when (step) {
    HELLO, GIFTS -> 0.58f
    FINALE -> -0.25f
    else -> 1.12f
}

private val shapes = listOf(KaiLooks.Good, KaiLooks.Okay, KaiLooks.Low, KaiLooks.Radiant, KaiLooks.Rough)
private fun pillLook(i: Int, color: Color) = shapes[i % shapes.size].copy(body = color, eyes = 1f, lid = 0f, gaze = Offset(0.3f, 0f), smile = 0.35f, blush = 0.3f)

private val levelWords = listOf("cosy", "serene", "halcyon", "apricity", "sprezzatura")
private val levelNotes = listOf(
    "Warm words you could use at breakfast.",
    "Words you know, but rarely reach for.",
    "Words with a little more colour in them.",
    "Words most people have never met.",
    "Words from old books and good poems.",
)
private val levelColors = listOf(Candy.Mint, Candy.Sun, Candy.Coral, Candy.Bubblegum, Candy.Lilac)

@Composable
fun OnboardingScreen(onDone: () -> Unit, startStep: Int = HELLO) {
    val c = appContainer()
    val p = K.colors
    val calm = K.calm
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    val hasKey by c.keys.awake.collectAsStateWithLifecycle()
    var step by rememberSaveable { mutableIntStateOf(startStep) }
    var name by rememberSaveable { mutableStateOf("") }
    var interests by rememberSaveable { mutableStateOf(if (startStep > TOPICS) setOf("nature", "wonder") else setOf()) }
    var themes by rememberSaveable { mutableStateOf(if (startStep > THEMES) setOf("hope", "calm") else setOf()) }
    var level by rememberSaveable { mutableIntStateOf(3) }
    var keyPhase by remember { mutableStateOf(KeyPhase.Idle) }
    var cheerAt by remember { mutableLongStateOf(0L) }
    var cheering by remember { mutableStateOf(false) }
    LaunchedEffect(cheerAt) { if (cheerAt > 0) { cheering = true; delay(700); cheering = false } }
    BackHandler(step in GIFTS..KEY) { focus.clearFocus(); step-- }
    BackHandler(step == FINALE) { step = NAME }

    // The wake-up: petals unfold, the bloom becomes Kai, friends pop in.
    val petals = remember { List(8) { Animatable(if (calm) 1f else 0f) } }
    val core = remember { Animatable(if (calm) 1f else 0f) }
    val kaiIn = remember { Animatable(if (calm) 1f else 0f) }
    val friendsIn = remember { List(3) { Animatable(if (calm) 1f else 0f) } }
    val textIn = remember { Animatable(if (calm) 1f else 0f) }
    val wave = remember { Animatable(0f) }
    var awake by remember { mutableStateOf(calm || startStep != HELLO) }
    val burst = rememberBurst()
    val density = LocalDensity.current

    // The finale: aurora, sentences about you, then Kai.
    val aurora = remember { Animatable(0f) }
    val vivid = remember { Animatable(0f) }
    val lines = remember { List(3) { Animatable(0f) } }
    val finaleKai = remember { Animatable(0f) }
    var finaleReady by remember { mutableStateOf(false) }

    BoxWithConstraints(Modifier.fillMaxSize().background(p.canvas)) {
        val W = constraints.maxWidth.toFloat()
        val H = constraints.maxHeight.toFloat()
        LaunchedEffect(Unit) {
            if (calm) return@LaunchedEffect
            if (startStep != HELLO) {
                (petals + core + kaiIn + friendsIn + textIn).forEach { it.snapTo(1f) }
                awake = true
                return@LaunchedEffect
            }
            delay(250)
            petals.forEachIndexed { i, a -> launch { delay(i * 70L); a.animateTo(1f, Motion.bloom()) } }
            delay(8 * 70L + 120)
            core.animateTo(1f, Motion.bloom())
            delay(260)
            launch { kaiIn.animateTo(1f, Motion.bloom()) }
            launch { delay(120); wave.animateTo(1f, tween(900, easing = Motion.Out)) }
            delay(380)
            awake = true
            val ks = kaiSpot(HELLO)
            launch { burst.fire(Offset(ks.x * W, ks.y * H), count = 26, power = 1.4f) }
            friendsIn.forEachIndexed { i, a -> launch { delay(i * 90L); a.animateTo(1f, Motion.bloom()) } }
            delay(260)
            textIn.animateTo(1f, Motion.enter(520))
        }
        LaunchedEffect(step == FINALE) {
            if (step != FINALE) return@LaunchedEffect
            scope.launch { c.prefs.completeOnboarding(name, interests, themes, level) }
            if (calm) { aurora.snapTo(1f); vivid.snapTo(1f); lines.forEach { it.snapTo(1f) }; finaleKai.snapTo(1f); finaleReady = true; return@LaunchedEffect }
            delay(250)
            launch { aurora.animateTo(1f, Motion.enter(1000)) }
            delay(450)
            lines.forEach { l -> l.animateTo(1f, Motion.enter(420)); delay(330) }
            launch { vivid.animateTo(1f, Motion.enter(900)) }
            finaleKai.animateTo(1f, Motion.bloom())
            val ks = kaiSpot(FINALE)
            launch { burst.fire(Offset(ks.x * W, ks.y * H), count = 30, power = 1.5f, kinds = listOf(Bit.Petal, Bit.Spark, Bit.Squiggle, Bit.Heart)) }
            delay(300)
            finaleReady = true
        }

        // Sky.
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(p.skyTop, p.skyMid, androidx.compose.ui.graphics.lerp(p.skyMid, p.skyFade, 0.5f)))))
        // A slow field of sparkles in the sky.
        val drift = if (!calm) rememberInfiniteTransition(label = "drift").animateFloat(0f, 1f, infiniteRepeatable(tween(24000, easing = androidx.compose.animation.core.LinearEasing)), label = "d") else null
        SparkleField({ drift?.value ?: 0.3f }, Modifier.fillMaxSize().graphicsLayer { alpha = (1f - aurora.value).coerceIn(0f, 1f) })
        // The cloud field.
        val top by animateFloatAsState(fieldTop(step), Motion.soft(), label = "field")
        Box(Modifier.fillMaxWidth().wrapContentHeight(Alignment.Top, unbounded = true).height(with(density) { (H * 1.4f).toDp() }).offset { IntOffset(0, (top * H).roundToInt()) }) {
            CloudBank(Modifier.fillMaxWidth().height(150.dp).offset(y = (-112).dp), Color.White, p.canvas)
            Box(Modifier.fillMaxSize().padding(top = 36.dp).background(p.canvas))
        }
        Aurora({ aurora.value }, { vivid.value }, Modifier.fillMaxSize())

        // Hero layer.
        val bob = if (!calm) rememberInfiniteTransition(label = "bob").animateFloat(0f, 6.283f, infiniteRepeatable(tween(4200), RepeatMode.Restart), label = "b") else null
        friendLooks.forEachIndexed { i, look ->
            val sp = friendSpot(step, i)
            val fx by animateFloatAsState(sp.x, Motion.soft(), label = "fx$i")
            val fy by animateFloatAsState(sp.y, Motion.soft(), label = "fy$i")
            val fs by animateDpAsState(sp.size, Motion.bloom(), label = "fs$i")
            // In the finale the friends circle Kai.
            val ks0 = kaiSpot(FINALE)
            val orbit = finaleKai.value.coerceIn(0f, 1f)
            val ang = (bob?.value ?: 0f) * 0.6f + i * 2.094f
            val ox = ks0.x + kotlin.math.cos(ang) * 0.36f
            val oy = ks0.y + kotlin.math.sin(ang) * 0.36f * (W / H)
            val hx = if (step == FINALE) fx + (ox - fx) * orbit else fx
            val hy = if (step == FINALE) fy + (oy - fy) * orbit else fy
            Hero(hx, hy, fs, W, H, bobPhase = { (bob?.value ?: 0f) + i * 2.1f }, scale = { friendsIn[i].value }) {
                Kai(if (step == FINALE || (step == GIFTS && cheering)) look.copy(eyes = 0f, smile = 1f, blush = 0.8f) else look, size = fs, shadow = true, description = null)
            }
            val labelAlpha by animateFloatAsState(if (step == GIFTS) 1f else 0f, Motion.fade(), label = "fl$i")
            if (labelAlpha > 0.01f) {
                Text(
                    friendLabels[i], style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = p.ink,
                    modifier = Modifier
                        .offset { IntOffset((fx * W).roundToInt() - 200, (fy * H + with(density) { (fs / 2 + 14.dp).toPx() }).roundToInt()) }
                        .width(with(density) { 400.toDp() })
                        .graphicsLayer { alpha = labelAlpha },
                    textAlign = TextAlign.Center,
                )
            }
        }
        val ks = kaiSpot(step)
        val kx by animateFloatAsState(ks.x, Motion.soft(), label = "kx")
        val ky by animateFloatAsState(ks.y, Motion.soft(), label = "ky")
        val kSize by animateDpAsState(ks.size, Motion.bloom(), label = "ks")
        val look = when {
            !awake -> KaiLooks.Sleepy
            cheering -> KaiLooks.Delighted
            step == HELLO -> KaiLooks.Curator
            step == GIFTS -> KaiLooks.Delighted
            step == TOPICS || step == THEMES -> KaiLooks.Curator.copy(gaze = Offset(-0.7f, 0.6f))
            step == LEVEL -> when (level) { 1 -> KaiLooks.Curator.copy(smile = 0.3f); 2 -> KaiLooks.Curator; 3 -> KaiLooks.Curator.copy(smile = 0.8f, blush = 0.6f); 4 -> KaiLooks.Wow; else -> KaiLooks.Delighted }
            step == NAME -> if (name.isBlank()) KaiLooks.Curator.copy(gaze = Offset(0f, 0.9f)) else KaiLooks.Delighted
            step == KEY -> when {
                hasKey && keyPhase != KeyPhase.Done && keyPhase != KeyPhase.Checking -> KaiLooks.Curator
                else -> when (keyPhase) { KeyPhase.Checking -> KaiLooks.Thinking; KeyPhase.Done -> KaiLooks.Delighted; KeyPhase.Failed -> KaiLooks.Sad; else -> KaiLooks.Sleepy }
            }
            else -> KaiLooks.Delighted
        }
        Hero(kx, ky, kSize, W, H, bobPhase = { (bob?.value ?: 0f) * 0.5f }, scale = { if (step == FINALE) finaleKai.value else kaiIn.value }) {
            Kai(look, size = kSize)
        }
        // The mark becomes Kai: the petals fold into the centre, which swells into Kai's body, and a
        // ring of light rolls outward.
        if (kaiIn.value < 1f) {
            val bs = kaiSpot(HELLO)
            val k = kaiIn.value.coerceIn(0f, 1f)
            Canvas(Modifier.size(bs.size * 1.2f).offset { IntOffset((bs.x * W - with(density) { (bs.size * 0.6f).toPx() }).roundToInt(), (bs.y * H - with(density) { (bs.size * 0.6f).toPx() }).roundToInt()) }.graphicsLayer {
                alpha = (1f - k * 1.4f).coerceIn(0f, 1f)
            }) {
                rotate(k * 70f) {
                    drawBloom(unfold = { petals[it].value * (1f - k) }, core01 = core.value * (1f + k * 2.2f))
                }
            }
        }
        if (wave.value in 0.001f..0.999f) {
            val bs = kaiSpot(HELLO)
            Canvas(Modifier.fillMaxSize()) {
                val c = Offset(bs.x * size.width, bs.y * size.height)
                val r = size.width * 0.75f * wave.value
                drawCircle(Color.White.copy(alpha = 0.55f * (1f - wave.value)), r, c, style = androidx.compose.ui.graphics.drawscope.Stroke(10f * density.density * (1f - wave.value) + 1f))
                drawCircle(Color.White.copy(alpha = 0.18f * (1f - wave.value)), r * 0.82f, c)
            }
        }
        Box(Modifier.fillMaxSize().burst(burst))

        // Words.
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).navigationBarsPadding().imePadding()) {
            Dots(step, Modifier.padding(start = Gap.gutter, top = Gap.md).graphicsLayer { alpha = if (step == FINALE) 0f else textIn.value })
            AnimatedContent(
                step, Modifier.weight(1f).fillMaxWidth(),
                transitionSpec = {
                    val dir = if (targetState > initialState) 1 else -1
                    (slideInHorizontally(Motion.offset) { it / 3 * dir } + fadeIn(Motion.fade(320))) togetherWith (slideOutHorizontally(Motion.offset) { -it / 3 * dir } + fadeOut(Motion.fade(160)))
                },
                label = "words",
            ) { s ->
                when (s) {
                    HELLO -> Bottom(H, Modifier.graphicsLayer { alpha = textIn.value; translationY = (1f - textIn.value) * 40f }) {
                        Shout("Hi, I'm Kai.", p.ink)
                        Spacer(Modifier.height(Gap.sm))
                        Sub("Every morning I'll write you something small and good. Just for you, never for everyone.", p.inkSoft)
                    }
                    GIFTS -> Bottom(H) {
                        Shout("Three little gifts a day", p.ink)
                        Spacer(Modifier.height(Gap.sm))
                        Sub("A word worth knowing, a thought worth keeping, and one small good thing to do.", p.inkSoft)
                    }
                    TOPICS -> Choices(
                        "What makes you curious?", "Pick as many as you like. I'll learn the rest from what you save.",
                        Topics.words.map { it.key to it.label }, interests,
                        colorOf = { k -> p.candy(categoryTint(k)) },
                        onToggle = { k -> interests = if (k in interests) interests - k else interests + k; cheerAt = System.nanoTime() },
                        onAll = { interests = if (interests.size == Topics.words.size) emptySet() else Topics.words.map { it.key }.toSet(); cheerAt = System.nanoTime() },
                    )
                    THEMES -> Choices(
                        "And more of what?", "The thoughts I pick will lean this way.",
                        Topics.themes.map { it.key to it.label }, themes,
                        colorOf = { k -> p.candy(themeTint(k)) },
                        onToggle = { k -> themes = if (k in themes) themes - k else themes + k; cheerAt = System.nanoTime() },
                        onAll = { themes = if (themes.size == Topics.themes.size) emptySet() else Topics.themes.map { it.key }.toSet(); cheerAt = System.nanoTime() },
                    )
                    LEVEL -> LevelStep(level) { level = it }
                    NAME -> NameStep(name, { name = it.take(24) }) { focus.clearFocus(); step = if (hasKey) FINALE else KEY }
                    KEY -> KeyStep(hasKey, { keyPhase = it }) { scope.launch { delay(250); step = FINALE } }
                    else -> Finale(name, interests, themes, level, hasKey, { i -> lines[i].value }, { finaleKai.value })
                }
            }
            // Controls.
            Row(Modifier.fillMaxWidth().padding(Gap.gutter), horizontalArrangement = Arrangement.spacedBy(Gap.md), verticalAlignment = Alignment.CenterVertically) {
                val onSky = step in TOPICS..KEY
                if (step in GIFTS..KEY) {
                    Box(
                        Modifier.size(54.dp).pressable(label = "Back") { focus.clearFocus(); step-- }.clip(CircleShape)
                            .background(if (onSky) Color.White.copy(alpha = 0.22f) else p.surfaceSoft)
                            .border(1.dp, if (onSky) Color.White.copy(alpha = 0.45f) else p.line, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { Icon(Icons.Back, null, tint = if (onSky) Color.White else p.ink, modifier = Modifier.size(22.dp)) }
                }
                val alpha = when (step) { HELLO -> textIn.value; FINALE -> if (finaleReady) 1f else 0f; else -> 1f }
                val (label, enabled) = when (step) {
                    HELLO -> "Nice to meet you" to true
                    GIFTS -> "Make it mine" to true
                    TOPICS -> (if (interests.isEmpty()) "Pick at least one" else "Next") to interests.isNotEmpty()
                    THEMES -> (if (themes.isEmpty()) "Pick at least one" else "Next") to themes.isNotEmpty()
                    LEVEL -> "Sounds right" to true
                    NAME -> (if (name.isBlank()) "Skip" else "That's me") to true
                    KEY -> (if (hasKey) "Next" else "I'll do this later") to true
                    else -> "Show me today" to true
                }
                if (step == KEY && !hasKey) {
                    Text(label, style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = Color.White, textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f).pressable { step = FINALE }.padding(vertical = Gap.lg))
                } else {
                    PrimaryButton(
                        label, {
                            focus.clearFocus()
                            if (step == FINALE) onDone() else if (step == NAME && hasKey) step = FINALE else step++
                        },
                        Modifier.weight(1f).graphicsLayer { this.alpha = alpha },
                        enabled = enabled && alpha > 0.5f, dark = !onSky, onSky = onSky,
                    )
                }
            }
        }
    }
}

/**
 * A character on the hero layer. It squashes and stretches along the way it is travelling and leans
 * into the move (the old animation principle), then settles back to round when it stops.
 */
@Composable
private fun Hero(x: Float, y: Float, size: Dp, W: Float, H: Float, bobPhase: () -> Float, scale: () -> Float, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val calm = K.calm
    val last = remember { floatArrayOf(x, y) }
    val vx = (x - last[0]) * W
    val vy = (y - last[1]) * H
    last[0] = x; last[1] = y
    val speed = kotlin.math.sqrt(vx * vx + vy * vy)
    val st = if (calm) 0f else (speed / (40f * density.density)).coerceIn(0f, 0.22f)
    val nx = if (speed > 0.01f) kotlin.math.abs(vx) / speed else 0f
    val ny = if (speed > 0.01f) kotlin.math.abs(vy) / speed else 0f
    Box(
        Modifier
            .offset { IntOffset((x * W - with(density) { size.toPx() } / 2).roundToInt(), (y * H - with(density) { size.toPx() } / 2).roundToInt()) }
            .graphicsLayer {
                translationY = sin(bobPhase()) * 4f * density.density
                val s = scale()
                scaleX = s * (1f + nx * st - ny * st * 0.5f)
                scaleY = s * (1f + ny * st - nx * st * 0.5f)
                rotationZ = if (speed > 0.01f) (vx / speed) * st * 40f else 0f
            },
    ) { content() }
}

@Composable
private fun Dots(step: Int, modifier: Modifier) {
    val onSky = step in TOPICS..KEY
    Row(modifier.height(20.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(FINALE) { i ->
            val w by animateDpAsState(if (i == step) 22.dp else 6.dp, Motion.soft(), label = "d")
            val col by animateColorAsState(
                when {
                    onSky -> if (i <= step) Color.White else Color.White.copy(alpha = 0.35f)
                    else -> if (i <= step) Color.White else Color.White.copy(alpha = 0.45f)
                }, label = "dc",
            )
            Box(Modifier.padding(horizontal = 3.dp).height(6.dp).width(w).clip(CircleShape).background(col))
        }
    }
}

/** Lays its content out on the cloud field at the bottom of the screen. */
@Composable
private fun Bottom(H: Float, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    Column(
        modifier.fillMaxSize().padding(horizontal = Gap.xl).padding(top = with(density) { (H * 0.6f).toDp() } - 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) { content() }
}

/** Headlines arrive word by word (kinetic type). */
@Composable
private fun Shout(text: String, color: Color, modifier: Modifier = Modifier, align: TextAlign = TextAlign.Center, size: Int = 40) =
    KineticText(text, K.type.shout.copy(fontSize = size.sp, lineHeight = (size + 2).sp, textAlign = align), color, modifier.fillMaxWidth(), align = align, delayMs = 140)

@Composable
private fun Sub(text: String, color: Color, align: TextAlign = TextAlign.Center) =
    Text(text, style = K.type.body.copy(fontSize = 17.sp, lineHeight = 25.sp), color = color, textAlign = align)

@Composable
private fun Choices(
    title: String, sub: String, options: List<Pair<String, String>>, chosen: Set<String>,
    colorOf: (String) -> Color, onToggle: (String) -> Unit, onAll: () -> Unit,
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = Gap.lg)) {
        Column(Modifier.padding(start = Gap.gutter, end = 96.dp)) {
            Shout(title, Color.White, align = TextAlign.Start, size = 38)
            Spacer(Modifier.height(Gap.sm))
            Sub(sub, Color.White.copy(alpha = 0.85f), TextAlign.Start)
        }
        Spacer(Modifier.height(Gap.xl))
        options.forEachIndexed { i, (key, label) ->
            Box(Modifier.fillMaxWidth().padding(horizontal = Gap.lg, vertical = 5.dp), contentAlignment = when (i % 3) { 0 -> Alignment.CenterStart; 1 -> Alignment.Center; else -> Alignment.CenterEnd }) {
                BigPill(label, key in chosen, pillLook(i, colorOf(key)), { onToggle(key) })
            }
        }
        Box(Modifier.fillMaxWidth().padding(horizontal = Gap.lg, vertical = 5.dp), contentAlignment = Alignment.Center) {
            BigPill(if (chosen.size == options.size) "Hmm, fewer" else "Surprise me", chosen.size == options.size, KaiLooks.Wow.copy(body = Candy.Coral), onAll, fontSize = 22)
        }
        Spacer(Modifier.height(Gap.lg))
    }
}

@Composable
private fun LevelStep(level: Int, onLevel: (Int) -> Unit) {
    val lv by animateFloatAsState((level - 1).toFloat(), Motion.soft(), label = "lv")
    val density = LocalDensity.current
    Column(Modifier.fillMaxSize().padding(horizontal = Gap.gutter), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(0.62f))
        Shout("How adventurous should your words be?", Color.White, size = 32)
        Spacer(Modifier.height(Gap.lg))
        // The word wheel (r02): today's example big in the middle, its neighbours fading away.
        Box(Modifier.fillMaxWidth().height(176.dp), contentAlignment = Alignment.Center) {
            levelWords.forEachIndexed { i, w ->
                val d = i - lv
                if (abs(d) < 2.6f) {
                    Text(
                        w, style = K.type.hero.copy(fontSize = 54.sp, lineHeight = 58.sp), color = Color.White, maxLines = 1,
                        modifier = Modifier.graphicsLayer {
                            translationY = d * 58f * density.density
                            val s = (1f - 0.42f * abs(d)).coerceAtLeast(0.3f)
                            scaleX = s; scaleY = s
                            alpha = (1f - 0.62f * abs(d)).coerceIn(0f, 1f)
                        },
                    )
                }
            }
        }
        Spacer(Modifier.height(Gap.md))
        val f = (level - 1) / 4f
        KairosSlider(
            f, { onLevel((it * 4).roundToInt() + 1) }, "Word level", { Topics.levels[(it * 4).roundToInt()] },
            stops = 5, colors = levelColors, onDark = true,
            bubble = { v -> SliderBubble(Topics.levels[(v * 4).roundToInt()], Color.White.copy(alpha = 0.25f)) },
        )
        Spacer(Modifier.height(Gap.sm))
        AnimatedContent(level, transitionSpec = { fadeIn(Motion.fade()) togetherWith fadeOut(Motion.fade(120)) }, label = "note") { l ->
            Sub(levelNotes[l - 1], Color.White.copy(alpha = 0.85f))
        }
        Spacer(Modifier.weight(0.38f))
    }
}

@Composable
private fun NameStep(name: String, onName: (String) -> Unit, done: () -> Unit) {
    val line by animateFloatAsState(if (name.isBlank()) 0.25f else (0.3f + name.length * 0.06f).coerceAtMost(0.9f), Motion.bloom(), label = "line")
    Column(Modifier.fillMaxSize().padding(horizontal = Gap.gutter), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(0.7f))
        Shout("What should I call you?", Color.White, size = 36)
        Spacer(Modifier.height(Gap.xl))
        Box(Modifier.fillMaxWidth().height(64.dp), contentAlignment = Alignment.Center) {
            if (name.isEmpty()) Text("Your name", style = K.type.display.copy(fontSize = 34.sp), color = Color.White.copy(alpha = 0.45f))
            BasicTextField(
                name, onName, singleLine = true,
                textStyle = K.type.display.copy(fontSize = 34.sp, color = Color.White, textAlign = TextAlign.Center),
                cursorBrush = SolidColor(Color.White),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { done() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Canvas(Modifier.fillMaxWidth().height(6.dp)) {
            val w = size.width * line
            drawLine(Color.White, Offset((size.width - w) / 2, size.height / 2), Offset((size.width + w) / 2, size.height / 2), 4.dp.toPx(), StrokeCap.Round)
        }
        Spacer(Modifier.height(Gap.lg))
        Sub("Only used to say hello.", Color.White.copy(alpha = 0.8f))
        Spacer(Modifier.weight(0.3f))
    }
}

@Composable
private fun KeyStep(hasKey: Boolean, onPhase: (KeyPhase) -> Unit, onConnected: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = Gap.gutter), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(210.dp))
        if (hasKey) {
            Shout("I'm already awake", Color.White, size = 34)
            Spacer(Modifier.height(Gap.sm))
            Sub("Your OpenRouter key is on this phone. Let's go.", Color.White.copy(alpha = 0.85f))
        } else {
            Shout("Wake me up with a key", Color.White, size = 34)
            Spacer(Modifier.height(Gap.sm))
            Sub("I write with AI through OpenRouter. Your key is encrypted on this phone and only ever sent to them. A few cents lasts months.", Color.White.copy(alpha = 0.88f))
            Spacer(Modifier.height(Gap.xl))
            KeyRitual(onPhase, onConnected, onSky = true)
            Spacer(Modifier.height(Gap.lg))
            Text(
                "Open openrouter.ai/keys, sign in, create a key, copy it, and tap the paste button above.",
                style = K.type.label, color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center,
            )
        }
    }
}

private fun joinNice(xs: List<String>): String = when (xs.size) {
    0 -> ""
    1 -> xs[0]
    2 -> "${xs[0]} and ${xs[1]}"
    else -> xs.dropLast(1).joinToString(", ") + " and " + xs.last()
}

@Composable
private fun Finale(name: String, interests: Set<String>, themes: Set<String>, level: Int, hasKey: Boolean, line: (Int) -> Float, kai: () -> Float) {
    val p = K.colors
    val density = LocalDensity.current
    val topics = Topics.words.filter { it.key in interests }.map { it.label.lowercase() }.take(3)
    val feels = Topics.themes.filter { it.key in themes }.map { it.label.lowercase() }.take(3)
    val sentences = listOf(
        if (topics.isEmpty()) "Curious about a bit of everything" else "Curious about ${joinNice(topics)}",
        if (feels.isEmpty()) "Open to every kind of thought" else "Here for more ${joinNice(feels)}",
        listOf("Likes plain, warm words", "Likes words that feel familiar", "Ready for richer words", "Hungry for rare words", "In love with literary words")[level - 1],
    )
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = Gap.xl).align(Alignment.Center).graphicsLayer { alpha = 1f - kai(); translationY = -kai() * 40f * density.density },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Getting to know you", style = K.type.label, color = p.inkSoft, modifier = Modifier.graphicsLayer { alpha = line(0) })
            Spacer(Modifier.height(Gap.md))
            sentences.forEachIndexed { i, s ->
                Text(
                    s, style = K.type.title.copy(fontSize = 22.sp, lineHeight = 30.sp), color = p.ink, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 4.dp).graphicsLayer {
                        val a = line(i)
                        val next = if (i < 2) line(i + 1) else 0f
                        alpha = a * (1f - 0.55f * next)
                        translationY = (1f - a) * 18f * density.density
                    },
                )
            }
        }
        Column(
            Modifier.fillMaxWidth().padding(horizontal = Gap.xl).align(Alignment.BottomCenter).padding(bottom = Gap.lg).graphicsLayer { alpha = kai(); translationY = (1f - kai()) * 30f * density.density },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Shout(if (name.isBlank()) "Nice to meet you." else "Nice to meet you, ${name.trim()}.", p.ink, size = 36)
            Spacer(Modifier.height(Gap.sm))
            Sub(if (hasKey) "Your first word is already on its way." else "Add a key whenever you're ready and I'll start writing.", p.inkSoft)
            Spacer(Modifier.height(Gap.xl))
        }
    }
}

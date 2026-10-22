package com.kairos.app.ui.feature.today

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.data.repo.DayStatus
import com.kairos.app.data.repo.Today
import com.kairos.app.data.repo.today
import com.kairos.app.engine.Mood
import com.kairos.app.engine.Topics
import com.kairos.app.ui.art.Bit
import com.kairos.app.ui.art.Kai
import com.kairos.app.ui.art.KaiLooks
import com.kairos.app.ui.art.burst
import com.kairos.app.ui.art.drawKai
import com.kairos.app.ui.art.look
import com.kairos.app.ui.art.rememberBurst
import com.kairos.app.ui.components.Card
import com.kairos.app.ui.components.Icons
import com.kairos.app.ui.components.KairosSlider
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.SkyTail
import com.kairos.app.ui.components.SliderBubble
import com.kairos.app.ui.components.at
import com.kairos.app.ui.components.pressable
import com.kairos.app.ui.components.sky
import com.kairos.app.ui.nav.shared
import com.kairos.app.ui.theme.Candy
import com.kairos.app.ui.theme.Gap
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.Motion
import com.kairos.app.ui.theme.Radius
import com.kairos.app.ui.theme.Tint
import com.kairos.app.ui.theme.tint
import com.kairos.app.ui.theme.tintInk
import com.kairos.app.util.Fmt
import com.kairos.app.util.appViewModel
import com.kairos.app.util.rememberSpeaker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.abs
import kotlin.math.roundToInt

private const val WORD = 0
private const val THOUGHT = 1
private const val GOOD = 2

@Composable
fun TodayScreen(
    openWord: (String) -> Unit,
    openQuote: (String) -> Unit,
    openStudio: (String, String) -> Unit,
    openPractice: () -> Unit,
    openCapsule: () -> Unit,
    openAiSettings: () -> Unit,
    openYou: () -> Unit,
    openBook: (String) -> Unit = {},
) {
    val vm = appViewModel { TodayViewModel(it) }
    val s by vm.state.collectAsStateWithLifecycle()
    var order by rememberSaveable { mutableStateOf(listOf(WORD, THOUGHT, GOOD)) }
    val speaker = rememberSpeaker()
    LaunchedEffect(s.feedback) { if (s.feedback != null) { delay(3500); vm.clearFeedback() } }
    val front = order.first()

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 120.dp)) {
        item {
            Column(Modifier.fillMaxWidth().sky().windowInsetsPadding(WindowInsets.statusBars).padding(bottom = SkyTail)) {
                Greeting(s.name, s.mood, openYou)
                Spacer(Modifier.height(Gap.lg))
                WeekArc(s.activeDays, s.weekMoods)
                Spacer(Modifier.height(Gap.xl))
                val t = s.today
                if (t != null) {
                    Deck(
                        t, order, { order = it },
                        onSpeak = { speaker.say(t.word.word) },
                        openWord = openWord, openQuote = openQuote, openStudio = openStudio,
                    )
                    Spacer(Modifier.height(Gap.xl))
                    Controls(t, front, { order = order.drop(1) + order.first() }, { order = listOf(order.last()) + order.dropLast(1) }, vm::toggleWord, vm::toggleQuote, vm::completeSpark)
                } else Waiting(s.status, openAiSettings, vm::refresh)
            }
        }
        s.today?.let { t ->
            if (front != GOOD) item {
                TeachKai(t, front, s.feedback, { vm.wordFeedback(t.word.id, it) }, { more -> vm.quoteFeedback(t.quote.id, more) }, { if (front == WORD) vm.anotherWord() else vm.anotherQuote() }, Modifier.padding(horizontal = Gap.gutter))
            }
        }
        item { MoodCheckIn(s.mood, vm::logMood, Modifier.padding(start = Gap.gutter, end = Gap.gutter, top = Gap.lg)) }
        if (s.due > 0) item { ReviewNudge(s.due, openPractice, Modifier.padding(start = Gap.gutter, end = Gap.gutter, top = Gap.lg)) }
        s.book?.let { b -> item { BookPeek(b, { openBook(b.id) }, Modifier.padding(start = Gap.gutter, end = Gap.gutter, top = Gap.lg)) } }
        item { CapsulePeek(s, openCapsule, Modifier.padding(start = Gap.gutter, end = Gap.gutter, top = Gap.lg)) }
    }
}

@Composable
private fun Greeting(name: String, mood: Mood?, openYou: () -> Unit) {
    val p = K.colors
    Row(Modifier.fillMaxWidth().padding(start = Gap.gutter, end = Gap.md, top = Gap.sm), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d")), style = K.type.label, color = p.onSkySoft)
            Text(Fmt.greeting(LocalTime.now().hour) + if (name.isNotBlank()) ", $name" else "", style = K.type.display.copy(fontSize = 26.sp, lineHeight = 30.sp), color = p.onSky, maxLines = 2)
        }
        Box(Modifier.pressable(label = "You", onClick = openYou)) { Kai(mood?.look() ?: KaiLooks.Curator, size = 56.dp) }
    }
}

/** Five days on an arch, today lowest and boldest (r01), each wearing that day's mood shape (r09). */
@Composable
private fun WeekArc(active: Set<Long>, moods: Map<Long, Mood>) {
    val p = K.colors
    val t = today()
    Row(Modifier.fillMaxWidth().padding(horizontal = Gap.gutter), horizontalArrangement = Arrangement.SpaceBetween) {
        (-2..2).forEach { off ->
            val day = LocalDate.ofEpochDay(t + off)
            val dist = abs(off)
            val alpha = when (dist) { 0 -> 1f; 1 -> 0.8f; else -> 0.5f }
            Column(Modifier.offset(y = (14 - dist * 11).dp).width(56.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(day.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()), style = K.type.label, color = p.onSky.copy(alpha = alpha * 0.85f))
                Text(day.dayOfMonth.toString(), style = K.type.display.copy(fontSize = if (off == 0) 24.sp else 20.sp), color = p.onSky.copy(alpha = alpha))
                Spacer(Modifier.height(5.dp))
                val m = moods[t + off]
                Box(Modifier.size(18.dp), contentAlignment = Alignment.Center) {
                    when {
                        m != null -> Canvas(Modifier.size(18.dp)) { val l = m.look(); drawKai(l, l, 1f, shadow = false, face = false) }
                        t + off in active -> Box(Modifier.size(6.dp).clip(CircleShape).background(p.onSky.copy(alpha = alpha)))
                    }
                }
            }
        }
    }
}

/**
 * The deck: word, thought and small good thing as a stack of tilted cards (r04, r12, r14). Drag the
 * front card sideways and fling it; it flies off and slides back in at the back of the stack.
 */
@Composable
private fun Deck(
    t: Today, order: List<Int>, onOrder: (List<Int>) -> Unit, onSpeak: () -> Unit,
    openWord: (String) -> Unit, openQuote: (String) -> Unit, openStudio: (String, String) -> Unit,
) {
    val calm = K.calm
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val drag = remember { Animatable(0f) }
    var flying by remember { mutableStateOf(-1) }
    val returnX = remember { Animatable(0f) }
    BoxWithConstraints(Modifier.fillMaxWidth().height(410.dp).padding(horizontal = Gap.gutter)) {
        val w = constraints.maxWidth.toFloat()
        listOf(WORD, THOUGHT, GOOD).forEach { kind ->
            val depth by animateFloatAsState(order.indexOf(kind).toFloat(), Motion.soft(), label = "depth$kind")
            val isFront = order.first() == kind
            Box(
                Modifier
                    .zIndex(3f - order.indexOf(kind))
                    .fillMaxWidth()
                    .height(380.dp)
                    .align(Alignment.BottomCenter)
                    .graphicsLayer {
                        val d = depth
                        val x = when {
                            isFront -> drag.value
                            kind == flying -> returnX.value
                            else -> 0f
                        }
                        translationX = x
                        translationY = -d * 13.dp.toPx()
                        rotationZ = (if (isFront) drag.value / w * 18f else 0f) + listOf(0f, -4f, 3.5f)[d.roundToInt().coerceIn(0, 2)] * (d.coerceAtMost(1f))
                        val sc = 1f - d * 0.045f
                        scaleX = sc; scaleY = sc
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f)
                    }
                    .then(
                        if (isFront) Modifier.pointerInput(order) {
                            val tracker = VelocityTracker()
                            detectHorizontalDragGestures(
                                onDragStart = { tracker.resetTracking() },
                                onDragEnd = {
                                    val v = tracker.calculateVelocity().x
                                    val far = abs(drag.value) > w * 0.28f || abs(v) > 1100f
                                    scope.launch {
                                        if (far) {
                                            val dir = if ((if (abs(v) > 1100f) v else drag.value) > 0) 1f else -1f
                                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            if (!calm) drag.animateTo(dir * w * 1.25f, tween(200, easing = Motion.Out))
                                            flying = kind
                                            returnX.snapTo(if (calm) 0f else drag.value)
                                            drag.snapTo(0f)
                                            onOrder(order.drop(1) + order.first())
                                            returnX.animateTo(0f, Motion.soft())
                                            flying = -1
                                        } else drag.animateTo(0f, Motion.bloom())
                                    }
                                },
                            ) { change, dx ->
                                change.consume()
                                tracker.addPosition(change.uptimeMillis, change.position)
                                scope.launch { drag.snapTo(drag.value + dx) }
                            }
                        } else Modifier
                    ),
            ) {
                when (kind) {
                    WORD -> WordCard(t, onSpeak, { openWord(t.word.id) }, { openStudio("word", t.word.id) })
                    THOUGHT -> ThoughtCard(t, { openQuote(t.quote.id) }, { openStudio("quote", t.quote.id) })
                    else -> GoodCard(t)
                }
            }
        }
    }
}

@Composable
private fun DeckCard(bg: Color, onClick: () -> Unit, content: @Composable () -> Unit) {
    val p = K.colors
    Box(
        Modifier
            .fillMaxSize()
            .shadow(26.dp, Radius.deck, ambientColor = p.shadow, spotColor = p.shadow)
            .clip(Radius.deck)
            .background(bg)
            .pressable(scale = 0.985f, haptic = false, onClick = onClick)
            .padding(Gap.xl),
    ) { content() }
}

@Composable
private fun CardLabel(text: String, tint: Tint) {
    val p = K.colors
    Text(text, style = K.type.caption.copy(fontWeight = FontWeight.SemiBold), color = p.tintInk(tint), modifier = Modifier.clip(CircleShape).background(p.tint(tint)).padding(horizontal = 10.dp, vertical = 5.dp))
}

@Composable
private fun WordCard(t: Today, onSpeak: () -> Unit, onOpen: () -> Unit, onShare: () -> Unit) {
    val p = K.colors
    val w = t.word
    DeckCard(p.surface, onOpen) {
        Column(Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CardLabel("Word of the day", Tint.Sky)
                Spacer(Modifier.weight(1f))
                ShareDot(onShare)
            }
            Spacer(Modifier.weight(1f))
            val size = when { w.word.length <= 8 -> 64.sp; w.word.length <= 11 -> 52.sp; w.word.length <= 14 -> 42.sp; else -> 34.sp }
            Text(w.word, style = K.type.hero.copy(fontSize = size, lineHeight = size * 1.02f), color = p.ink, maxLines = 2, modifier = Modifier.shared("word-${w.id}"))
            Spacer(Modifier.height(Gap.sm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    Modifier.clip(CircleShape).background(p.surfaceSoft).pressable(label = "Listen", onClick = onSpeak).padding(start = 12.dp, end = 10.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(w.pronunciation, style = K.type.label, color = p.ink)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Speaker, "Listen", tint = p.accent, modifier = Modifier.size(17.dp))
                }
                Spacer(Modifier.width(Gap.sm))
                Text(w.partOfSpeech, style = K.type.label.copy(fontFamily = com.kairos.app.ui.theme.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontSize = 16.sp), color = p.inkSoft)
            }
            Spacer(Modifier.height(Gap.lg))
            Text(w.definition, style = K.type.body.copy(fontSize = 19.sp, lineHeight = 27.sp), color = p.ink, maxLines = 4, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Kai(KaiLooks.Curator.copy(gaze = Offset(0.6f, 0f)), size = 30.dp, shadow = false, alive = false, description = null)
                Spacer(Modifier.width(Gap.sm))
                Text(t.wordReason, style = K.type.caption, color = p.inkSoft, maxLines = 2, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ThoughtCard(t: Today, onOpen: () -> Unit, onShare: () -> Unit) {
    val p = K.colors
    val q = t.quote
    DeckCard(p.tint(Tint.Lavender), onOpen) {
        Column(Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CardLabel("Thought of the day", Tint.Lavender)
                Spacer(Modifier.weight(1f))
                ShareDot(onShare)
            }
            Spacer(Modifier.height(Gap.md))
            Text("“", style = K.type.hero.copy(fontSize = 88.sp, lineHeight = 60.sp), color = Candy.Lilac)
            val len = q.text.length
            val size = when { len < 70 -> 30.sp; len < 120 -> 25.sp; else -> 21.sp }
            Text(q.text, style = K.type.quote.copy(fontSize = size, lineHeight = size * 1.24f), color = p.ink, maxLines = 7, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(22.dp).height(2.dp).background(p.tintInk(Tint.Lavender)))
                Spacer(Modifier.width(Gap.sm))
                Text(q.author, style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = p.tintInk(Tint.Lavender))
            }
        }
    }
}

@Composable
private fun GoodCard(t: Today) {
    val p = K.colors
    DeckCard(p.tint(Tint.Butter), {}) {
        Box(Modifier.fillMaxSize()) {
            Kai(if (t.sparkDone) KaiLooks.Radiant else KaiLooks.Radiant.copy(eyes = 1f, smile = 0.6f), size = 92.dp, modifier = Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-8).dp))
            Column(Modifier.fillMaxSize()) {
                CardLabel("A small good thing", Tint.Butter)
                Spacer(Modifier.weight(1f))
                Text(t.spark.ifBlank { "Look up for a minute. Find one cloud with a shape." }, style = K.type.display.copy(fontSize = 28.sp, lineHeight = 33.sp), color = p.ink)
                Spacer(Modifier.height(Gap.md))
                Text(if (t.sparkDone) "Done. That counts, really." else "Small enough for today. Tap the button below when you've done it.", style = K.type.body, color = p.tintInk(Tint.Butter))
                Spacer(Modifier.weight(0.4f))
            }
        }
    }
}

@Composable
private fun ShareDot(onShare: () -> Unit) {
    val p = K.colors
    Box(Modifier.size(36.dp).clip(CircleShape).background(p.ink.copy(alpha = 0.06f)).pressable(label = "Make a card", onClick = onShare), contentAlignment = Alignment.Center) {
        Icon(Icons.Share, "Make a card", tint = p.ink, modifier = Modifier.size(18.dp))
    }
}

/** ← Save → (r12): arrows walk the deck, the pill acts on the front card. */
@Composable
private fun Controls(t: Today, front: Int, next: () -> Unit, prev: () -> Unit, saveWord: (String) -> Unit, saveQuote: (String) -> Unit, spark: () -> Unit) {
    val p = K.colors
    val calm = K.calm
    val scope = rememberCoroutineScope()
    val burst = rememberBurst()
    val pop = remember { Animatable(1f) }
    val density = LocalDensity.current
    var pillW by remember { mutableFloatStateOf(0f) }
    Row(Modifier.fillMaxWidth().padding(horizontal = Gap.gutter), horizontalArrangement = Arrangement.spacedBy(Gap.md), verticalAlignment = Alignment.CenterVertically) {
        GlassRound(Icons.Back, "Previous card", prev)
        val (label, on, icon) = when (front) {
            WORD -> Triple(if (t.word.saved) "Saved" else "Save", t.word.saved, if (t.word.saved) Icons.HeartOn else Icons.Heart)
            THOUGHT -> Triple(if (t.quote.saved) "Kept" else "Keep", t.quote.saved, if (t.quote.saved) Icons.HeartOn else Icons.Heart)
            else -> Triple(if (t.sparkDone) "Done" else "I did it", t.sparkDone, Icons.Check)
        }
        Row(
            Modifier
                .weight(1f)
                .height(58.dp)
                .burst(burst)
                .graphicsLayer { scaleX = pop.value; scaleY = pop.value }
                .shadow(16.dp, CircleShape, ambientColor = p.shadow, spotColor = p.shadow)
                .clip(CircleShape)
                .background(if (on) Color.White else Color.White.copy(alpha = 0.94f))
                .pressable(label = label, scale = 0.94f) {
                    val turningOn = !on
                    when (front) { WORD -> saveWord(t.word.id); THOUGHT -> saveQuote(t.quote.id); else -> if (!t.sparkDone) spark() }
                    if (turningOn && !calm) {
                        scope.launch { pop.snapTo(0.86f); pop.animateTo(1f, Motion.bloom()) }
                        scope.launch {
                            burst.fire(
                                with(density) { Offset(pillW / 2f, 29.dp.toPx()) },
                                kinds = if (front == GOOD) listOf(Bit.Spark, Bit.Petal, Bit.Squiggle) else listOf(Bit.Heart, Bit.Petal, Bit.Dot),
                                colors = if (front == GOOD) listOf(Candy.Sun, Candy.Coral, Candy.Mint) else listOf(Candy.Coral, Candy.Bubblegum, Candy.Sun),
                                count = 16, spread = 160f,
                            )
                        }
                    }
                }
                .onSizeChanged { pillW = it.width.toFloat() },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedContent(on to icon, transitionSpec = { (fadeIn(Motion.fade(140)) + scaleIn(Motion.bloom(), initialScale = 0.4f)) togetherWith fadeOut(Motion.fade(90)) }, label = "saveIcon") { (o, ic) ->
                Icon(ic, null, tint = if (o) (if (front == GOOD) p.positive else Candy.Coral) else Candy.Navy, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(Gap.sm))
            Text(label, style = K.type.label.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold), color = Candy.Navy)
        }
        GlassRound(Icons.Arrow, "Next card", next)
    }
}

@Composable
private fun GlassRound(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    val p = K.colors
    Box(
        Modifier.size(58.dp).pressable(label = label, scale = 0.9f, onClick = onClick).clip(CircleShape).background(p.glassOnSky).border(1.dp, p.glassOnSkyRim, CircleShape),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, label, tint = p.onSky, modifier = Modifier.size(22.dp)) }
}

/** No day yet: Kai asleep (no key), thinking (writing) or sad (it went wrong). */
@Composable
private fun Waiting(status: DayStatus, openAiSettings: () -> Unit, retry: () -> Unit) {
    val p = K.colors
    Column(Modifier.fillMaxWidth().height(460.dp).padding(horizontal = Gap.xl), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        val look = when (status) { is DayStatus.NeedsKey -> KaiLooks.Sleepy; is DayStatus.Failed -> KaiLooks.Sad; else -> KaiLooks.Thinking }
        Kai(look, size = 150.dp)
        Spacer(Modifier.height(Gap.xl))
        when (status) {
            is DayStatus.NeedsKey -> {
                Text("Kai is asleep", style = K.type.shout.copy(fontSize = 36.sp), color = p.onSky, textAlign = TextAlign.Center)
                Spacer(Modifier.height(Gap.sm))
                Text("Wake me with an OpenRouter key and I'll start writing words and thoughts just for you.", style = K.type.body, color = p.onSkySoft, textAlign = TextAlign.Center)
                Spacer(Modifier.height(Gap.xl))
                PrimaryButton("Wake Kai up", openAiSettings, icon = Icons.Key, onSky = true)
            }
            is DayStatus.Failed -> {
                Text("Today is still being written", style = K.type.shout.copy(fontSize = 32.sp), color = p.onSky, textAlign = TextAlign.Center)
                Spacer(Modifier.height(Gap.sm))
                Text(status.message, style = K.type.body, color = p.onSkySoft, textAlign = TextAlign.Center)
                Spacer(Modifier.height(Gap.xl))
                Row(horizontalArrangement = Arrangement.spacedBy(Gap.sm)) {
                    PrimaryButton("Try again", retry, icon = Icons.Shuffle, onSky = true)
                    PrimaryButton("Settings", openAiSettings, dark = true)
                }
            }
            else -> {
                Text("Writing today for you…", style = K.type.shout.copy(fontSize = 30.sp), color = p.onSky, textAlign = TextAlign.Center)
                Spacer(Modifier.height(Gap.sm))
                Text("A word, a thought and a small good thing. Nearly there.", style = K.type.body, color = p.onSkySoft, textAlign = TextAlign.Center)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TeachKai(t: Today, front: Int, feedback: String?, onWord: (WordFeedback) -> Unit, onQuote: (Boolean) -> Unit, another: () -> Unit, modifier: Modifier = Modifier) {
    val p = K.colors
    Card(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Kai(if (feedback != null) KaiLooks.Delighted else KaiLooks.Curator.copy(gaze = Offset(0.6f, 0.2f)), size = 52.dp, shadow = false)
            Spacer(Modifier.width(Gap.md))
            AnimatedContent(feedback, transitionSpec = { (fadeIn() + slideInVertically { it / 3 }) togetherWith fadeOut() }, label = "fb", modifier = Modifier.weight(1f)) { f ->
                Column {
                    Text(f ?: if (front == WORD) "How was ${t.word.word}?" else "Did this one land?", style = K.type.title, color = p.ink)
                    if (f == null) Text("Your answer shapes what I write next.", style = K.type.caption, color = p.inkSoft)
                }
            }
        }
        Spacer(Modifier.height(Gap.lg))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Gap.sm), verticalArrangement = Arrangement.spacedBy(Gap.sm)) {
            if (front == WORD) {
                WordFeedback.entries.forEach { fb -> React(fb.label, fb.glyph) { onWord(fb) } }
                React("Another word", "↻", another)
            } else {
                React("More like this", "♥") { onQuote(true) }
                React("Not for me", "✕") { onQuote(false) }
                React("Another one", "↻", another)
            }
        }
    }
}

@Composable
private fun React(text: String, glyph: String, onClick: () -> Unit) {
    val p = K.colors
    Row(
        Modifier.pressable(scale = 0.92f, onClick = onClick).clip(CircleShape).background(p.surfaceSoft).padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(26.dp).clip(CircleShape).background(p.surface), contentAlignment = Alignment.Center) { Text(glyph, style = K.type.label, color = p.ink) }
        Spacer(Modifier.width(8.dp))
        Text(text, style = K.type.label, color = p.ink)
    }
}

private val moodOrder = listOf(Mood.Rough, Mood.Low, Mood.Okay, Mood.Good, Mood.Radiant)
private val moodColors = listOf(Candy.Bubblegum, Candy.Lilac, Candy.Sky, Candy.Mint, Candy.Sun)

/** Mood check-in through the adaptive slider: Kai changes shape and colour as you slide. */
@Composable
private fun MoodCheckIn(mood: Mood?, onPick: (Mood) -> Unit, modifier: Modifier = Modifier) {
    val p = K.colors
    var v by remember(mood) { mutableFloatStateOf(mood?.let { moodOrder.indexOf(it) / 4f } ?: 0.5f) }
    val current = moodOrder[(v * 4).roundToInt()]
    Card(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("How are you, really?", style = K.type.title, color = p.ink)
                Spacer(Modifier.height(Gap.xs))
                AnimatedContent(mood, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "moodReply") { m ->
                    Text(m?.reply ?: "Slide to tell me. It quietly steers today's thought.", style = K.type.label, color = p.inkSoft)
                }
            }
            Spacer(Modifier.width(Gap.md))
            Kai(current.look(), size = 84.dp)
        }
        Spacer(Modifier.height(Gap.md))
        KairosSlider(
            v, { v = it }, "How are you feeling", { moodOrder[(it * 4).roundToInt()].label },
            stops = 5, colors = moodColors, trackHeight = 52.dp,
            onValueChangeFinished = { onPick(moodOrder[(v * 4).roundToInt()]) },
            bubble = { f -> SliderBubble(moodOrder[(f * 4).roundToInt()].label, moodColors.at(f).let { androidx.compose.ui.graphics.lerp(it, Candy.Navy, 0.25f) }) },
        )
    }
}

@Composable
private fun ReviewNudge(due: Int, onStart: () -> Unit, modifier: Modifier = Modifier) {
    val p = K.colors
    Card(modifier.fillMaxWidth(), onClick = onStart) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(width = 70.dp, height = 64.dp)) {
                listOf(Tint.Mint to -10f, Tint.Lavender to 6f, Tint.Sky to 0f).forEachIndexed { i, (tn, r) ->
                    Box(Modifier.align(Alignment.Center).size(width = 44.dp, height = 56.dp).graphicsLayer { rotationZ = r; translationX = (i - 1) * 9f * density }.shadow(4.dp, RoundedSmall).clip(RoundedSmall).background(p.tint(tn)))
                }
                Text("Aa", style = K.type.serifLarge.copy(fontSize = 20.sp), color = p.tintInk(Tint.Sky), modifier = Modifier.align(Alignment.Center))
            }
            Spacer(Modifier.width(Gap.lg))
            Column(Modifier.weight(1f)) {
                Text("$due ${if (due == 1) "word wants" else "words want"} a hello", style = K.type.title, color = p.ink)
                Text("Right before you'd forget them. Two minutes.", style = K.type.caption, color = p.inkSoft)
            }
            Icon(Icons.Chevron, null, tint = p.inkFaint, modifier = Modifier.size(18.dp))
        }
    }
}

private val RoundedSmall = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)

/** A peek at the time capsule: a little night sky with a glass folder (r06). */
@Composable
private fun CapsulePeek(s: TodayState, open: () -> Unit, modifier: Modifier = Modifier) {
    val p = K.colors
    val next = s.nextCapsule
    Box(
        modifier
            .fillMaxWidth()
            .shadow(18.dp, Radius.card, ambientColor = p.shadow, spotColor = p.shadow)
            .clip(Radius.card)
            .background(Brush.verticalGradient(p.night))
            .pressable(scale = 0.985f, haptic = false, onClick = open)
            .padding(Gap.xl),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                val (title, body) = when {
                    s.readyCapsules > 0 -> "A letter is ready" to "Past you left something for today."
                    next != null -> "Next letter opens ${Fmt.until(next.opensAt)}" to next.title.ifBlank { "Sealed ${Fmt.shortDate(next.createdAt)}" }
                    else -> "Write to future you" to "Seal a letter, a photo or a voice note."
                }
                Text(title, style = K.type.title, color = Color.White)
                Spacer(Modifier.height(Gap.xs))
                Text(body, style = K.type.label, color = Color.White.copy(alpha = 0.72f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.width(Gap.md))
            com.kairos.app.ui.feature.capsule.MiniFolder(if (s.readyCapsules > 0) Candy.Sun else Candy.Lilac, Modifier.size(width = 74.dp, height = 60.dp))
        }
    }
}

private val moodTints = mapOf(Mood.Radiant to Tint.Butter, Mood.Good to Tint.Mint, Mood.Okay to Tint.Sky, Mood.Low to Tint.Lavender, Mood.Rough to Tint.Rose)

/** The tint that goes with a mood, matching Kai's colour for it. */
fun Mood.tint() = moodTints.getValue(this)

/** A book of the day from the shelf: its cover, title and the first line of its summary. */
@Composable
private fun BookPeek(b: com.kairos.app.data.db.BookEntity, open: () -> Unit, modifier: Modifier = Modifier) {
    val p = K.colors
    Card(modifier.fillMaxWidth(), onClick = open) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            com.kairos.app.ui.components.BookCover(b.title, b.author, b.id, Modifier.size(width = 64.dp, height = 94.dp).graphicsLayer { rotationZ = -4f })
            Spacer(Modifier.width(Gap.lg))
            Column(Modifier.weight(1f)) {
                Text("A book worth knowing", style = K.type.caption.copy(fontWeight = FontWeight.SemiBold), color = p.accent)
                Text(b.title, style = K.type.title, color = p.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${b.author} · ${b.minutes} min", style = K.type.caption, color = p.inkSoft)
                Spacer(Modifier.height(Gap.xs))
                Text(b.summary, style = K.type.label.copy(fontWeight = FontWeight.Normal), color = p.ink.copy(alpha = 0.8f), maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

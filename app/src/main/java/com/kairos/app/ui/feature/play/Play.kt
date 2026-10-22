package com.kairos.app.ui.feature.play

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.kairos.app.AppContainer
import com.kairos.app.data.db.QuoteEntity
import com.kairos.app.data.db.WordEntity
import com.kairos.app.ui.art.Bit
import com.kairos.app.ui.art.Kai
import com.kairos.app.ui.art.KaiLooks
import com.kairos.app.ui.art.burst
import com.kairos.app.ui.art.drawGrain
import com.kairos.app.ui.art.rememberBurst
import com.kairos.app.ui.components.FitText
import com.kairos.app.ui.components.Icons
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.RoundIcon
import com.kairos.app.ui.components.pressable
import com.kairos.app.ui.theme.Candy
import com.kairos.app.ui.theme.Gap
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.Motion
import com.kairos.app.util.appViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

/** One random card. Every kind is answered by flipping; some ask you to decide first. */
sealed interface PlayCard {
    val id: String
    /** Meaning on the front, the word on the back. */
    data class Guess(val word: WordEntity) : PlayCard { override val id get() = "g:${word.id}" }
    /** A thought with one word hidden; the back shows it whole. */
    data class Finish(val quote: QuoteEntity, val hidden: String, val shown: String) : PlayCard { override val id get() = "f:${quote.id}" }
    /** A word and a meaning that may not be its own: true or not? */
    data class TrueOrNot(val word: WordEntity, val meaning: String, val isTrue: Boolean) : PlayCard { override val id get() = "t:${word.id}" }
}

data class PlayState(val deck: List<PlayCard> = emptyList(), val index: Int = 0, val score: Int = 0, val streak: Int = 0, val loading: Boolean = true)

class PlayViewModel(private val c: AppContainer) : ViewModel() {
    private val _s = MutableStateFlow(PlayState())
    val state = _s.asStateFlow()

    init { deal() }

    /** Deals a fresh random hand from everything you've met (and a few you haven't). */
    fun deal() = viewModelScope.launch {
        val r = Random(System.nanoTime())
        val words = c.db.words().all().filter { it.taste >= 0 }.shuffled(r)
        val quotes = c.db.quotes().all().filter { it.taste >= 0 && it.text.split(" ").size >= 5 }.shuffled(r)
        val cards = ArrayList<PlayCard>()
        words.take(8).forEachIndexed { i, w ->
            if (i % 2 == 0 || words.size < 2) cards += PlayCard.Guess(w)
            else {
                val honest = r.nextBoolean()
                val other = words.firstOrNull { it.id != w.id }
                cards += PlayCard.TrueOrNot(w, if (honest || other == null) w.definition else other.definition, honest || other == null)
            }
        }
        quotes.take(5).forEach { q ->
            val ws = q.text.split(" ")
            val candidates = ws.indices.filter { ws[it].trim('.', ',', ';', ':', '!', '?', '"', '“', '”').length >= 5 }
            if (candidates.isNotEmpty()) {
                val k = candidates[r.nextInt(candidates.size)]
                val hidden = ws[k].trim('.', ',', ';', ':', '!', '?', '"', '“', '”')
                cards += PlayCard.Finish(q, hidden, ws.mapIndexed { i, w -> if (i == k) w.replace(hidden, "_".repeat(hidden.length.coerceIn(4, 9))) else w }.joinToString(" "))
            }
        }
        _s.value = PlayState(deck = cards.shuffled(r), loading = false)
    }

    /** Swiped away: right means "got it", left means "not yet". */
    fun answer(gotIt: Boolean) {
        val s = _s.value
        val card = s.deck.getOrNull(s.index) ?: return
        _s.value = s.copy(index = s.index + 1, score = s.score + if (gotIt) 10 + s.streak * 2 else 0, streak = if (gotIt) s.streak + 1 else 0)
        // Words you got right count as a light review; ones you missed come back sooner.
        if (card is PlayCard.Guess || card is PlayCard.TrueOrNot) {
            val w = if (card is PlayCard.Guess) card.word else (card as PlayCard.TrueOrNot).word
            if (w.inPractice) viewModelScope.launch { c.kairos.grade(w, if (gotIt) com.kairos.app.engine.Recall.Good else com.kairos.app.engine.Recall.Hard) }
        }
    }
}

private val cardColors = listOf(
    listOf(Color(0xFF2F5BFF), Color(0xFF6C8CFF)), listOf(Color(0xFFFF6A4D), Color(0xFFFFA07A)), listOf(Color(0xFF14A46C), Color(0xFF6BD9A6)),
    listOf(Color(0xFF7A4BE0), Color(0xFFB79BFF)), listOf(Color(0xFFE8A200), Color(0xFFFFD45C)), listOf(Color(0xFFE0457B), Color(0xFFFF9BC0)),
)

/**
 * Play: random flashcards from your words and thoughts. Tap to flip; swipe right if you knew it,
 * left if not. The top card flies off the way you threw it and the next one rises from the fan
 * behind (r32, r07).
 */
@Composable
fun PlayScreen(onBack: () -> Unit) {
    val vm = appViewModel { PlayViewModel(it) }
    val s by vm.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF14204F), Color(0xFF2B3D8F), Color(0xFF6C7BE0)))).windowInsetsPadding(WindowInsets.statusBars).navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = Gap.gutter, vertical = Gap.sm), verticalAlignment = Alignment.CenterVertically) {
            RoundIcon(Icons.Close, "Close", onBack, onSky = true)
            Spacer(Modifier.width(Gap.md))
            Column(Modifier.weight(1f)) {
                Text("Play", style = K.type.shout.copy(fontSize = 26.sp), color = Color.White)
                Text(if (s.deck.isEmpty()) "" else "${(s.index + 1).coerceAtMost(s.deck.size)} of ${s.deck.size}", style = K.type.caption, color = Color.White.copy(alpha = 0.7f))
            }
            Score(s.score, s.streak)
        }
        Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal = Gap.xl, vertical = Gap.lg), contentAlignment = Alignment.Center) {
            when {
                s.loading -> Kai(KaiLooks.Thinking, size = 100.dp)
                s.deck.isEmpty() -> Empty(onBack)
                s.index >= s.deck.size -> Done(s, vm::deal, onBack)
                else -> {
                    // The fan: up to two cards waiting behind, slightly turned.
                    for (k in 2 downTo 1) {
                        val i = s.index + k
                        if (i < s.deck.size) Box(Modifier.zIndex(-k.toFloat()).fillMaxWidth().aspectRatio(0.7f).graphicsLayer {
                            rotationZ = if (k == 1) 5f else -6f; translationY = k * 14.dp.toPx(); val sc = 1f - k * 0.05f; scaleX = sc; scaleY = sc
                        }.shadow(10.dp, RoundedCornerShape(30.dp)).clip(RoundedCornerShape(30.dp)).background(Brush.linearGradient(cardColors[i % cardColors.size])))
                    }
                    key(s.deck[s.index].id) { TopCard(s.deck[s.index], s.index, vm::answer) }
                }
            }
        }
        if (!s.loading && s.index < s.deck.size && s.deck.isNotEmpty()) {
            Row(Modifier.fillMaxWidth().padding(Gap.gutter), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("← Not yet", style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = Color.White.copy(alpha = 0.75f))
                Text("Tap to flip", style = K.type.caption, color = Color.White.copy(alpha = 0.6f))
                Text("Got it →", style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = Color.White.copy(alpha = 0.75f))
            }
        }
    }
}

@Composable
private fun key(id: String, content: @Composable () -> Unit) = androidx.compose.runtime.key(id) { content() }

@Composable
private fun TopCard(card: PlayCard, index: Int, answer: (Boolean) -> Unit) {
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val calm = K.calm
    val dx = remember { Animatable(0f) }
    val dy = remember { Animatable(0f) }
    val rise = remember { Animatable(if (calm) 1f else 0f) }
    var flipped by remember { mutableStateOf(false) }
    var choice by remember { mutableIntStateOf(-1) }
    val flip by animateFloatAsState(if (flipped) 180f else 0f, Motion.settle(), label = "flip")
    val burst = rememberBurst()
    androidx.compose.runtime.LaunchedEffect(Unit) { if (!calm) rise.animateTo(1f, Motion.bloom()) }
    BoxWithConstraints(Modifier.fillMaxWidth().aspectRatio(0.7f)) {
        val w = constraints.maxWidth.toFloat()
        fun fling(right: Boolean) = scope.launch {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            if (right) launch { burst.fire(Offset(w * 0.5f, w * 0.6f), kinds = listOf(Bit.Spark, Bit.Petal, Bit.Dot), count = 14) }
            if (!calm) dx.animateTo(if (right) w * 1.6f else -w * 1.6f, tween(260, easing = Motion.Out))
            answer(right)
        }
        Box(
            Modifier.fillMaxSize().burst(burst)
                .graphicsLayer {
                    translationX = dx.value; translationY = dy.value + (1f - rise.value) * 40.dp.toPx()
                    rotationZ = dx.value / w * 16f
                    val sc = 0.92f + 0.08f * rise.value; scaleX = sc; scaleY = sc
                    rotationY = flip; cameraDistance = 14f * density
                }
                .pointerInput(card.id) {
                    detectDragGestures(
                        onDragEnd = {
                            if (abs(dx.value) > w * 0.28f) fling(dx.value > 0)
                            else scope.launch { launch { dx.animateTo(0f, Motion.bloom()) }; dy.animateTo(0f, Motion.bloom()) }
                        },
                    ) { change, drag -> change.consume(); scope.launch { dx.snapTo(dx.value + drag.x); dy.snapTo(dy.value + drag.y * 0.3f) } }
                }
                .pressable(scale = 0.98f, haptic = false) { flipped = !flipped }
                .shadow(24.dp, RoundedCornerShape(30.dp)).clip(RoundedCornerShape(30.dp)),
        ) {
            val colors = cardColors[index % cardColors.size]
            if (flip <= 90f) Front(card, colors, choice) { c -> choice = c; flipped = true }
            else Box(Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }) { Back(card, choice) { fling(it) } }
            // Hints as the card is dragged: green to the right, coral to the left.
            val hint = (dx.value / (w * 0.3f)).coerceIn(-1f, 1f)
            if (abs(hint) > 0.05f) Box(Modifier.fillMaxSize().background((if (hint > 0) Candy.Mint else Candy.Coral).copy(alpha = abs(hint) * 0.35f)))
        }
    }
}

@Composable
private fun Front(card: PlayCard, colors: List<Color>, choice: Int, choose: (Int) -> Unit) {
    Box(Modifier.fillMaxSize().background(Brush.linearGradient(colors))) {
        androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) { drawGrain(0.14f) }
        Column(Modifier.fillMaxSize().padding(26.dp)) {
            Text(
                when (card) { is PlayCard.Guess -> "WHICH WORD MEANS…"; is PlayCard.Finish -> "FINISH THE LINE"; is PlayCard.TrueOrNot -> "TRUE OR NOT?" },
                style = K.type.caption.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp), color = Color.White.copy(alpha = 0.8f),
            )
            Spacer(Modifier.height(Gap.lg))
            when (card) {
                is PlayCard.Guess -> FitText(card.word.definition, K.type.quote.copy(fontStyle = FontStyle.Normal), Color.White, Modifier.fillMaxWidth().weight(1f), max = 34.sp, min = 14.sp, lineHeight = 1.15f, contentAlignment = Alignment.CenterStart)
                is PlayCard.Finish -> FitText(card.shown, K.type.quote, Color.White, Modifier.fillMaxWidth().weight(1f), max = 32.sp, min = 14.sp, lineHeight = 1.2f, contentAlignment = Alignment.CenterStart)
                is PlayCard.TrueOrNot -> {
                    Text(card.word.word, style = K.type.hero.copy(fontSize = 48.sp, lineHeight = 50.sp), color = Color.White)
                    Spacer(Modifier.height(Gap.md))
                    FitText("means “${card.meaning.trimEnd('.')}”", K.type.body.copy(fontSize = 20.sp), Color.White, Modifier.fillMaxWidth().weight(1f), max = 22.sp, min = 12.sp, lineHeight = 1.3f)
                    Row(horizontalArrangement = Arrangement.spacedBy(Gap.md)) {
                        listOf("True", "Not quite").forEachIndexed { i, label ->
                            Text(label, style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = Color.White, textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f).pressable(scale = 0.92f) { choose(i) }.clip(CircleShape).background(Color.White.copy(alpha = if (choice == i) 0.4f else 0.2f)).border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape).padding(vertical = 12.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(Gap.md))
            Text("Tap to see the answer", style = K.type.caption, color = Color.White.copy(alpha = 0.75f))
        }
    }
}

@Composable
private fun Back(card: PlayCard, choice: Int, fling: (Boolean) -> Unit) {
    val ink = Candy.Navy
    val right = when (card) { is PlayCard.TrueOrNot -> choice == -1 || (choice == 0) == card.isTrue; else -> true }
    Column(Modifier.fillMaxSize().background(Color(0xFFFFF8EC)).padding(26.dp)) {
        if (card is PlayCard.TrueOrNot && choice >= 0) {
            Text(if (right) "Yes!" else "Not this time", style = K.type.shout.copy(fontSize = 30.sp), color = if (right) Color(0xFF14A46C) else Candy.Coral)
            Spacer(Modifier.height(Gap.sm))
        }
        when (card) {
            is PlayCard.Guess -> {
                Text("It's", style = K.type.label, color = ink.copy(alpha = 0.6f))
                Text(card.word.word, style = K.type.hero.copy(fontSize = 52.sp, lineHeight = 54.sp), color = ink)
                Text("${card.word.pronunciation} · ${card.word.partOfSpeech}", style = K.type.label, color = ink.copy(alpha = 0.6f))
                Spacer(Modifier.height(Gap.lg))
                Text("“${card.word.example}”", style = K.type.quote.copy(fontSize = 20.sp, lineHeight = 26.sp), color = ink)
            }
            is PlayCard.Finish -> {
                Text("The missing word was", style = K.type.label, color = ink.copy(alpha = 0.6f))
                Text(card.hidden, style = K.type.hero.copy(fontSize = 44.sp, lineHeight = 46.sp), color = ink)
                Spacer(Modifier.height(Gap.lg))
                FitText(card.quote.text, K.type.quote, ink, Modifier.fillMaxWidth().weight(1f), max = 22.sp, min = 12.sp, lineHeight = 1.25f)
                if (!card.quote.isLine && card.quote.author.isNotBlank()) Text("— ${card.quote.author}", style = K.type.label, color = ink.copy(alpha = 0.7f))
            }
            is PlayCard.TrueOrNot -> {
                Text(card.word.word, style = K.type.hero.copy(fontSize = 44.sp, lineHeight = 46.sp), color = ink)
                Spacer(Modifier.height(Gap.sm))
                Text(card.word.definition, style = K.type.body.copy(fontSize = 19.sp, lineHeight = 27.sp), color = ink)
            }
        }
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(Gap.md)) {
            Text("Not yet", style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = ink, textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f).pressable(scale = 0.92f) { fling(false) }.clip(CircleShape).background(ink.copy(alpha = 0.08f)).padding(vertical = 13.dp))
            Text("Got it", style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = Color.White, textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f).pressable(scale = 0.92f) { fling(right) }.clip(CircleShape).background(ink).padding(vertical = 13.dp))
        }
    }
}

@Composable
private fun Score(score: Int, streak: Int) {
    val pop = remember { Animatable(1f) }
    androidx.compose.runtime.LaunchedEffect(score) { if (score > 0) { pop.snapTo(1.25f); pop.animateTo(1f, Motion.bloom()) } }
    Row(Modifier.graphicsLayer { scaleX = pop.value; scaleY = pop.value }.clip(CircleShape).background(Color.White).padding(start = 10.dp, end = 12.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Sparkle, null, tint = Candy.Cobalt, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text("$score", style = K.type.label.copy(fontWeight = FontWeight.Bold), color = Candy.Navy)
        if (streak >= 2) { Spacer(Modifier.width(6.dp)); Text("×$streak", style = K.type.caption.copy(fontWeight = FontWeight.Bold), color = Candy.Coral) }
    }
}

@Composable
private fun Done(s: PlayState, again: () -> Unit, back: () -> Unit) {
    val burst = rememberBurst()
    androidx.compose.runtime.LaunchedEffect(Unit) { burst.fire(Offset(400f, 300f), count = 30, power = 1.5f) }
    Column(Modifier.burst(burst), horizontalAlignment = Alignment.CenterHorizontally) {
        Kai(KaiLooks.Delighted, size = 120.dp)
        Spacer(Modifier.height(Gap.lg))
        Text("${s.score} sparkles", style = K.type.shout.copy(fontSize = 40.sp), color = Color.White)
        Text("A new hand is shuffled every time.", style = K.type.body, color = Color.White.copy(alpha = 0.8f))
        Spacer(Modifier.height(Gap.xl))
        PrimaryButton("Deal again", again, Modifier.fillMaxWidth(), icon = Icons.Shuffle, onSky = true)
        Spacer(Modifier.height(Gap.sm))
        Text("Done for now", style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = Color.White, modifier = Modifier.pressable(onClick = back).padding(Gap.md))
    }
}

@Composable
private fun Empty(back: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Kai(KaiLooks.Sleepy, size = 110.dp)
        Spacer(Modifier.height(Gap.lg))
        Text("Nothing to play with yet", style = K.type.shout.copy(fontSize = 28.sp), color = Color.White, textAlign = TextAlign.Center)
        Text("Scroll the Feed for a bit and come back.", style = K.type.body, color = Color.White.copy(alpha = 0.8f))
        Spacer(Modifier.height(Gap.lg))
        PrimaryButton("Back", back, onSky = true)
    }
}

package com.kairos.app.ui.feature.practice

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.kairos.app.AppContainer
import com.kairos.app.data.db.WordEntity
import com.kairos.app.data.repo.MomentKind
import com.kairos.app.engine.Recall
import com.kairos.app.engine.Srs
import com.kairos.app.engine.SrsState
import com.kairos.app.ui.art.Bit
import com.kairos.app.ui.art.Bloom
import com.kairos.app.ui.art.Garden
import com.kairos.app.ui.art.Kai
import com.kairos.app.ui.art.KaiLooks
import com.kairos.app.ui.art.burst
import com.kairos.app.ui.art.drawGrain
import com.kairos.app.ui.art.rememberBurst
import com.kairos.app.ui.components.Empty
import com.kairos.app.ui.components.Icons
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.RoundIcon
import com.kairos.app.ui.components.SecondaryButton
import com.kairos.app.ui.components.pressable
import com.kairos.app.ui.theme.Candy
import com.kairos.app.ui.theme.Gap
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.Motion
import com.kairos.app.ui.theme.Radius
import com.kairos.app.util.appViewModel
import com.kairos.app.util.rememberSpeaker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class CardMode { Flip, Quiz }
data class PracticeCard(val word: WordEntity, val mode: CardMode, val options: List<WordEntity> = emptyList())
data class PracticeState(
    val loading: Boolean = true,
    val cards: List<PracticeCard> = emptyList(),
    val index: Int = 0,
    val revealed: Boolean = false,
    val picked: String? = null,
    val remembered: Int = 0,
    val rememberedIds: List<String> = emptyList(),
    /** Nothing was due: this is a bonus round from words scheduled for later. */
    val bonus: Boolean = false,
) {
    val current get() = cards.getOrNull(index)
    val done get() = !loading && cards.isNotEmpty() && index >= cards.size
}

class PracticeViewModel(private val c: AppContainer) : ViewModel() {
    private val _s = MutableStateFlow(PracticeState())
    val state = _s.asStateFlow()
    init { load() }

    fun load() = viewModelScope.launch {
        _s.value = PracticeState()
        val bonus = c.kairos.nothingDue()
        val deck = c.kairos.practiceDeck(10)
        val cards = deck.mapIndexed { i, w -> if (w.reps >= 1 && i % 3 == 1) PracticeCard(w, CardMode.Quiz, c.kairos.quizOptions(w)) else PracticeCard(w, CardMode.Flip) }
        _s.value = PracticeState(loading = false, cards = cards, bonus = bonus && cards.isNotEmpty())
    }

    fun reveal() = _s.update { it.copy(revealed = true) }

    fun pick(id: String) {
        val card = _s.value.current ?: return
        if (_s.value.picked != null) return
        _s.update { it.copy(picked = id, revealed = true) }
        viewModelScope.launch { delay(950); grade(if (id == card.word.id) Recall.Good else Recall.Again) }
    }

    fun grade(r: Recall) {
        val s = _s.value
        val card = s.current ?: return
        viewModelScope.launch {
            c.kairos.grade(card.word, r)
            val next = s.index + 1
            val ok = r == Recall.Good || r == Recall.Easy
            _s.update { it.copy(index = next, revealed = false, picked = null, remembered = it.remembered + if (ok) 1 else 0, rememberedIds = if (ok) it.rememberedIds + card.word.id else it.rememberedIds) }
            if (next >= s.cards.size) c.kairos.addMoment(MomentKind.Practice, "${s.cards.size} words")
        }
    }
}

/** Deep, grainy card colours (r07): plum, navy, teal, coral-red, forest. */
private val cardInks = listOf(
    listOf(Color(0xFF4B2E6E), Color(0xFF7A4A8C)),
    listOf(Color(0xFF14246B), Color(0xFF2F4FC2)),
    listOf(Color(0xFF0F5A5A), Color(0xFF2C8F7E)),
    listOf(Color(0xFF8A2B3A), Color(0xFFD9573F)),
    listOf(Color(0xFF1F4D2B), Color(0xFF4C8F3A)),
)

@Composable
fun PracticeScreen(onBack: () -> Unit) {
    val vm = appViewModel { PracticeViewModel(it) }
    val s by vm.state.collectAsStateWithLifecycle()
    val p = K.colors
    val speaker = rememberSpeaker()
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFEAF3FD), p.canvas))).windowInsetsPadding(WindowInsets.statusBars).navigationBarsPadding().padding(horizontal = Gap.gutter)) {
        Row(Modifier.fillMaxWidth().padding(vertical = Gap.sm), verticalAlignment = Alignment.CenterVertically) {
            RoundIcon(Icons.Close, "Close practice", onBack)
            Spacer(Modifier.width(Gap.md))
            PetalProgress(if (s.cards.isEmpty()) 0 else s.index.coerceAtMost(s.cards.size), s.cards.size, Modifier.weight(1f))
            Spacer(Modifier.width(Gap.md))
            Points(s.remembered * 10)
        }
        if (s.bonus && s.index == 0 && !s.done) {
            Text(
                "Nothing is due today, so here's a bonus round from words coming up soon.", style = K.type.label, color = p.inkSoft,
                modifier = Modifier.fillMaxWidth().clip(Radius.tile).background(p.accentSoft).padding(Gap.md),
            )
        }
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            when {
                s.loading -> Kai(KaiLooks.Thinking, size = 96.dp)
                s.cards.isEmpty() -> Empty("Nothing to practise yet", "Save words you like on Today or in Learn, and they'll come back here at the right moment.", action = { PrimaryButton("Back", onBack, dark = true) })
                s.done -> Summary(s, vm::load, onBack)
                else -> {
                    Fan(s.index)
                    AnimatedContent(
                        s.index,
                        transitionSpec = { (slideInHorizontally(Motion.offset) { it / 3 } + fadeIn() + scaleIn(Motion.bloom(), initialScale = 0.92f)) togetherWith (slideOutHorizontally(Motion.offset) { -it } + fadeOut(Motion.fade(160))) },
                        label = "card",
                    ) { idx ->
                        val card = s.cards.getOrNull(idx) ?: return@AnimatedContent
                        if (card.mode == CardMode.Flip) FlipCard(card.word, idx, s.revealed && idx == s.index, vm::reveal) { speaker.say(card.word.word) }
                        else QuizCard(card, if (idx == s.index) s.picked else null, vm::pick)
                    }
                }
            }
        }
        val current = s.current
        if (!s.done && current != null && current.mode == CardMode.Flip) {
            Box(Modifier.fillMaxWidth().height(104.dp), contentAlignment = Alignment.Center) {
                AnimatedContent(s.revealed, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "grades") { rev ->
                    if (!rev) PrimaryButton("Show me", vm::reveal, Modifier.fillMaxWidth(), dark = true)
                    else {
                        val st = SrsState(current.word.reps, current.word.ease, current.word.intervalDays, current.word.lapses)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Gap.sm)) {
                            Recall.entries.forEach { r ->
                                val look = when (r) { Recall.Again -> KaiLooks.Sad; Recall.Hard -> KaiLooks.Okay; Recall.Good -> KaiLooks.Good; Recall.Easy -> KaiLooks.Radiant }
                                Column(
                                    Modifier.weight(1f).pressable(scale = 0.92f) { vm.grade(r) }.clip(Radius.tile).background(p.surface).border(1.dp, p.line, Radius.tile).padding(vertical = Gap.sm),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Kai(look, size = 30.dp, shadow = false, alive = false, description = null)
                                    Text(r.label, style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = p.ink)
                                    Text(Srs.preview(st, r), style = K.type.caption, color = p.inkFaint)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Progress as a row of petals that fill with colour, one per card. */
@Composable
private fun PetalProgress(done: Int, total: Int, modifier: Modifier = Modifier) {
    val p = K.colors
    val shown by animateFloatAsState(done.toFloat(), Motion.bloom(), label = "petals")
    Canvas(modifier.height(28.dp)) {
        if (total == 0) return@Canvas
        val step = size.width / total
        for (i in 0 until total) {
            val f = (shown - i).coerceIn(0f, 1f)
            val cx = step * (i + 0.5f)
            val r = size.height * 0.5f
            val petal = Bloom.petal(r * 1.8f).also { it.translate(Offset(cx, size.height * 0.5f + r * 0.95f)) }
            drawPath(petal, p.line)
            if (f > 0f) {
                val c = listOf(Candy.Coral, Candy.Sun, Candy.Mint, Candy.Sky, Candy.Lilac, Candy.Bubblegum)[i % 6]
                scale(f, f, Offset(cx, size.height * 0.5f)) { drawPath(petal, c) }
            }
        }
    }
}

@Composable
private fun Points(n: Int) {
    val p = K.colors
    val pop = remember { Animatable(1f) }
    LaunchedEffect(n) { if (n > 0) { pop.snapTo(1.25f); pop.animateTo(1f, Motion.bloom()) } }
    Row(
        Modifier.graphicsLayer { scaleX = pop.value; scaleY = pop.value }.shadow(8.dp, CircleShape, ambientColor = p.shadow, spotColor = p.shadow).clip(CircleShape).background(Color.White).padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Sparkle, null, tint = Candy.Cobalt, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text("$n", style = K.type.label.copy(fontWeight = FontWeight.Bold), color = Candy.Navy)
    }
}

/** Two cards peeking behind the current one, and an orbit ring around the stack (r07). */
@Composable
private fun Fan(index: Int) {
    Box(Modifier.fillMaxWidth().aspectRatio(0.74f)) {
        listOf(-7f to 1, 6f to 2).forEach { (r, k) ->
            val inks = cardInks[(index + k) % cardInks.size]
            Box(
                Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 10.dp).graphicsLayer { rotationZ = r; translationX = r * 3f * density }
                    .shadow(10.dp, Radius.deck).clip(Radius.deck).background(Brush.linearGradient(inks)),
            )
        }
        Canvas(Modifier.fillMaxSize()) {
            val ring = Path().apply { addOval(androidx.compose.ui.geometry.Rect(Offset(-size.width * 0.06f, size.height * 0.8f), Size(size.width * 1.12f, size.height * 0.16f))) }
            drawPath(ring, Brush.horizontalGradient(listOf(Candy.Sky.copy(alpha = 0.2f), Candy.Sky, Candy.Cobalt.copy(alpha = 0.6f))), style = Stroke(5.dp.toPx()))
        }
    }
}

@Composable
private fun FlipCard(w: WordEntity, index: Int, revealed: Boolean, onReveal: () -> Unit, onSpeak: () -> Unit) {
    val rot by animateFloatAsState(if (revealed) 180f else 0f, Motion.settle(), label = "flip")
    val inks = cardInks[index % cardInks.size]
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(0.74f)
            .graphicsLayer { rotationY = rot; cameraDistance = 16f * density }
            .pressable(enabled = !revealed, scale = 0.98f, onClick = onReveal)
            .shadow(26.dp, Radius.deck)
            .clip(Radius.deck)
            .background(if (rot <= 90f) Brush.linearGradient(inks) else Brush.verticalGradient(listOf(Color(0xFFFFF8EC), Color(0xFFFFF1DE))))
    ) {
        if (rot <= 90f) {
            Canvas(Modifier.fillMaxSize()) {
                // A soft starburst behind the word (r07), then grain.
                val c = Offset(size.width / 2, size.height * 0.42f)
                val rays = 22
                val path = Path()
                for (i in 0 until rays * 2) {
                    val a = i * PI.toFloat() / rays
                    val r = if (i % 2 == 0) size.width * 0.42f else size.width * 0.3f
                    val x = c.x + cos(a) * r; val y = c.y + sin(a) * r
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                drawPath(path, Color.White.copy(alpha = 0.08f))
                drawCircle(Brush.radialGradient(listOf(lerp(inks[1], Color.White, 0.35f).copy(alpha = 0.55f), Color.Transparent), c, size.width * 0.45f), size.width * 0.45f, c)
                drawGrain(0.16f)
            }
            Column(Modifier.fillMaxSize().padding(Gap.xl), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Do you remember…", style = K.type.label, color = Color.White.copy(alpha = 0.7f))
                Spacer(Modifier.weight(1f))
                Text(w.word, style = K.type.hero.copy(fontSize = if (w.word.length > 10) 46.sp else 62.sp), color = Color.White, textAlign = TextAlign.Center)
                Spacer(Modifier.height(Gap.sm))
                Text("${w.pronunciation}  ·  ${w.partOfSpeech}", style = K.type.label, color = Color.White.copy(alpha = 0.75f))
                Spacer(Modifier.weight(1f))
                Row(
                    Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.16f)).border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape).pressable(label = "Hear it", onClick = onSpeak).padding(horizontal = 16.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Speaker, null, tint = Color.White, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Hear it", style = K.type.label, color = Color.White)
                }
            }
        } else {
            Column(Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }.verticalScroll(rememberScrollState()).padding(Gap.xl), verticalArrangement = Arrangement.Center) {
                Text(w.word, style = K.type.serifLarge, color = Candy.Navy)
                Spacer(Modifier.height(Gap.md))
                Text(w.definition, style = K.type.body.copy(fontSize = 21.sp, lineHeight = 30.sp), color = Candy.Navy)
                Spacer(Modifier.height(Gap.lg))
                Text("“${w.example}”", style = K.type.quote.copy(fontSize = 20.sp, lineHeight = 27.sp), color = Candy.Navy.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
private fun QuizCard(card: PracticeCard, picked: String?, onPick: (String) -> Unit) {
    val p = K.colors
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Which one means", style = K.type.label, color = p.inkSoft)
        Text(card.word.word, style = K.type.hero.copy(fontSize = 56.sp), color = p.ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(Gap.xl))
        card.options.forEachIndexed { i, o ->
            val answer = o.id == card.word.id
            val isPicked = o.id == picked
            val shake = remember { Animatable(0f) }
            val burst = rememberBurst()
            val density = LocalDensity.current
            var w by remember { mutableFloatStateOf(0f) }
            LaunchedEffect(isPicked) {
                if (!isPicked) return@LaunchedEffect
                if (answer) launch { burst.fire(Offset(w - with(density) { 30.dp.toPx() }, with(density) { 30.dp.toPx() }), kinds = listOf(Bit.Spark, Bit.Petal), colors = listOf(Candy.Mint, Candy.Sun, Candy.Sky), count = 14, power = 0.8f) }
                else for (x in listOf(10f, -9f, 7f, -4f, 0f)) shake.animateTo(x, tween(55))
            }
            val bg by animateColorAsState(when { picked == null -> p.surface; answer -> Candy.Mint.copy(alpha = 0.18f); isPicked -> Candy.Coral.copy(alpha = 0.16f); else -> p.surface }, label = "qbg")
            val rim by animateColorAsState(when { picked == null -> p.line; answer -> p.positive; isPicked -> p.negative; else -> p.line }, label = "qb")
            Row(
                Modifier
                    .padding(vertical = Gap.xs)
                    .fillMaxWidth()
                    .onSizeChanged { w = it.width.toFloat() }
                    .burst(burst)
                    .graphicsLayer { translationX = shake.value * density.density }
                    .pressable(enabled = picked == null, scale = 0.97f) { onPick(o.id) }
                    .clip(Radius.tile)
                    .background(bg)
                    .border(1.5.dp, rim, Radius.tile)
                    .padding(Gap.lg),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(30.dp).clip(CircleShape).background(p.surfaceSoft), contentAlignment = Alignment.Center) { Text("ABCD"[i % 4].toString(), style = K.type.label.copy(fontWeight = FontWeight.Bold), color = p.ink) }
                Spacer(Modifier.width(Gap.md))
                Text(o.definition, style = K.type.body, color = p.ink, modifier = Modifier.weight(1f))
            }
        }
    }
}

/** The end: a big reaction over ghost text (r09), and a flower for every word remembered. */
@Composable
private fun Summary(s: PracticeState, again: () -> Unit, done: () -> Unit) {
    val p = K.colors
    val burst = rememberBurst()
    val density = LocalDensity.current
    val ratio = s.remembered.toFloat() / s.cards.size.coerceAtLeast(1)
    val (shout, color) = when { ratio >= 0.8f -> "Nice!" to Candy.Mint; ratio >= 0.5f -> "Good!" to Candy.Sky; else -> "Ooh." to Candy.Coral }
    LaunchedEffect(Unit) { if (ratio >= 0.5f) burst.fire(with(density) { Offset(180.dp.toPx(), 80.dp.toPx()) }, count = 30, power = 1.5f) }
    Column(Modifier.fillMaxWidth().burst(burst), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Text("${s.remembered} of ${s.cards.size}", style = K.type.shout.copy(fontSize = 62.sp), color = p.ink.copy(alpha = 0.07f))
            Text(shout, style = K.type.shout.copy(fontSize = 72.sp, lineHeight = 72.sp), color = color)
        }
        Kai(if (ratio >= 0.5f) KaiLooks.Delighted else KaiLooks.Curator, size = 96.dp)
        Spacer(Modifier.height(Gap.md))
        Text("You remembered ${s.remembered} of ${s.cards.size}. Each word comes back right before you'd forget it.", style = K.type.body, color = p.inkSoft, textAlign = TextAlign.Center)
        if (s.rememberedIds.isNotEmpty()) {
            Spacer(Modifier.height(Gap.lg))
            Box(Modifier.fillMaxWidth().height(140.dp).clip(Radius.card).background(Candy.Navy)) {
                Garden(s.rememberedIds.take(8).map { it.hashCode() }, Modifier.fillMaxWidth().height(130.dp).align(Alignment.BottomCenter))
            }
        }
        Spacer(Modifier.height(Gap.xl))
        PrimaryButton("Done", done, Modifier.fillMaxWidth(), dark = true)
        Spacer(Modifier.height(Gap.sm))
        SecondaryButton("Play another round", again, Modifier.fillMaxWidth(), icon = Icons.Shuffle)
    }
}

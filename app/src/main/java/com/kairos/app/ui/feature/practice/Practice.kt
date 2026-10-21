package com.kairos.app.ui.feature.practice

import com.kairos.app.ui.theme.canvas
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
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
import com.kairos.app.ui.components.Empty
import com.kairos.app.ui.components.Icons
import com.kairos.app.ui.components.KairosMark
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.Progress
import com.kairos.app.ui.components.RoundIcon
import com.kairos.app.ui.components.SecondaryButton
import com.kairos.app.ui.components.pressable
import com.kairos.app.ui.theme.Gap
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.Motion
import com.kairos.app.ui.theme.Radius
import com.kairos.app.util.appContainer
import com.kairos.app.util.appViewModel
import com.kairos.app.util.rememberSpeaker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CardMode { Flip, Quiz }
data class PracticeCard(val word: WordEntity, val mode: CardMode, val options: List<WordEntity> = emptyList())
data class PracticeState(
    val loading: Boolean = true,
    val cards: List<PracticeCard> = emptyList(),
    val index: Int = 0,
    val revealed: Boolean = false,
    val picked: String? = null,
    val remembered: Int = 0,
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
        val deck = c.kairos.practiceDeck(10)
        val cards = deck.mapIndexed { i, w -> if (w.reps >= 1 && i % 3 == 1) PracticeCard(w, CardMode.Quiz, c.kairos.quizOptions(w)) else PracticeCard(w, CardMode.Flip) }
        _s.value = PracticeState(loading = false, cards = cards)
    }

    fun reveal() = _s.update { it.copy(revealed = true) }

    fun pick(id: String) {
        val card = _s.value.current ?: return
        if (_s.value.picked != null) return
        _s.update { it.copy(picked = id, revealed = true) }
        viewModelScope.launch { delay(900); grade(if (id == card.word.id) Recall.Good else Recall.Again) }
    }

    fun grade(r: Recall) {
        val s = _s.value
        val card = s.current ?: return
        viewModelScope.launch {
            c.kairos.grade(card.word, r)
            val next = s.index + 1
            _s.update { it.copy(index = next, revealed = false, picked = null, remembered = it.remembered + if (r == Recall.Good || r == Recall.Easy) 1 else 0) }
            if (next >= s.cards.size) c.kairos.addMoment(MomentKind.Practice, "${s.cards.size} words")
        }
    }
}

@Composable
fun PracticeScreen(onBack: () -> Unit) {
    val vm = appViewModel { PracticeViewModel(it) }
    val s by vm.state.collectAsStateWithLifecycle()
    val p = K.colors
    val speaker = rememberSpeaker()
    Column(Modifier.fillMaxSize().background(p.canvas).windowInsetsPadding(WindowInsets.statusBars).navigationBarsPadding().padding(horizontal = Gap.gutter)) {
        Row(Modifier.fillMaxWidth().padding(vertical = Gap.sm), verticalAlignment = Alignment.CenterVertically) {
            RoundIcon(Icons.Close, "Close practice", onBack)
            Spacer(Modifier.width(Gap.lg))
            Progress(if (s.cards.isEmpty()) 0f else s.index.toFloat() / s.cards.size, Modifier.weight(1f))
            Spacer(Modifier.width(Gap.lg))
            Text(if (s.cards.isEmpty()) "" else "${(s.index + 1).coerceAtMost(s.cards.size)} of ${s.cards.size}", style = K.type.label, color = p.inkSoft)
        }
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            when {
                s.loading -> KairosMark(40.dp, tinted = true)
                s.cards.isEmpty() -> Empty("Nothing to practise yet", "Save words you like on Today or in Learn, and they'll come back here at the right moment.", action = { PrimaryButton("Back", onBack) })
                s.done -> Summary(s, vm::load, onBack)
                else -> AnimatedContent(
                    s.index,
                    transitionSpec = { (slideInHorizontally(Motion.offset) { it / 3 } + fadeIn() + scaleIn(initialScale = 0.95f)) togetherWith (slideOutHorizontally(Motion.offset) { -it / 3 } + fadeOut()) },
                    label = "card",
                ) { idx ->
                    val card = s.cards.getOrNull(idx) ?: return@AnimatedContent
                    if (card.mode == CardMode.Flip) FlipCard(card.word, s.revealed && idx == s.index, vm::reveal) { speaker.say(card.word.word) }
                    else QuizCard(card, if (idx == s.index) s.picked else null, vm::pick)
                }
            }
        }
        val current = s.current
        if (!s.done && current != null && current.mode == CardMode.Flip) {
            Box(Modifier.fillMaxWidth().height(96.dp), contentAlignment = Alignment.Center) {
                AnimatedContent(s.revealed, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "grades") { rev ->
                    if (!rev) PrimaryButton("Show meaning", vm::reveal, Modifier.fillMaxWidth(), dark = true)
                    else {
                        val st = SrsState(current.word.reps, current.word.ease, current.word.intervalDays, current.word.lapses)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Gap.sm)) {
                            Recall.entries.forEach { r ->
                                val tint = when (r) { Recall.Again -> p.negative; Recall.Hard -> p.warning; Recall.Good -> p.positive; Recall.Easy -> p.accent }
                                Column(
                                    Modifier.weight(1f).pressable { vm.grade(r) }.clip(Radius.tile).background(p.surface).border(1.dp, p.line, Radius.tile).padding(vertical = Gap.md),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(r.label, style = K.type.label, color = tint)
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

@Composable
private fun FlipCard(w: WordEntity, revealed: Boolean, onReveal: () -> Unit, onSpeak: () -> Unit) {
    val p = K.colors
    val rot by animateFloatAsState(if (revealed) 180f else 0f, Motion.settle(), label = "flip")
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(0.74f)
            .graphicsLayer { rotationY = rot; cameraDistance = 16f * density }
            .pressable(enabled = !revealed, scale = 0.99f, onClick = onReveal)
            .shadow(24.dp, Radius.card, ambientColor = p.shadow, spotColor = p.shadow)
            .clip(Radius.card)
            .background(p.surface)
    ) {
        if (rot <= 90f) {
            Column(Modifier.fillMaxSize().padding(Gap.xl), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(w.word, style = K.type.hero.copy(fontSize = if (w.word.length > 10) 48.sp else 64.sp), color = p.ink, textAlign = TextAlign.Center)
                Spacer(Modifier.height(Gap.sm))
                Text("${w.pronunciation}  ·  ${w.partOfSpeech}", style = K.type.label, color = p.inkSoft)
                Spacer(Modifier.height(Gap.xl))
                SecondaryButton("Hear it", onSpeak, icon = Icons.Speaker)
                Spacer(Modifier.height(Gap.xxl))
                Text("Do you remember what it means?", style = K.type.label, color = p.inkFaint)
            }
        } else {
            Column(Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }.verticalScroll(rememberScrollState()).padding(Gap.xl), verticalArrangement = Arrangement.Center) {
                Text(w.word, style = K.type.serifLarge, color = p.ink)
                Spacer(Modifier.height(Gap.md))
                Text(w.definition, style = K.type.body.copy(fontSize = 20.sp, lineHeight = 29.sp), color = p.ink)
                Spacer(Modifier.height(Gap.lg))
                Text("“${w.example}”", style = K.type.quote.copy(fontSize = 20.sp, lineHeight = 27.sp), color = p.inkSoft)
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
        card.options.forEach { o ->
            val answer = o.id == card.word.id
            val isPicked = o.id == picked
            val shake = remember { Animatable(0f) }
            LaunchedEffect(isPicked) {
                if (isPicked && !answer) repeat(3) { shake.animateTo(8f, tween(50)); shake.animateTo(-8f, tween(50)) }
                shake.animateTo(0f, tween(50))
            }
            val border by animateColorAsState(when { picked == null -> p.line; answer -> p.positive; isPicked -> p.negative; else -> p.line }, label = "qb")
            Text(
                o.definition, style = K.type.body, color = p.ink,
                modifier = Modifier
                    .padding(vertical = Gap.xs)
                    .fillMaxWidth()
                    .graphicsLayer { translationX = shake.value }
                    .pressable(enabled = picked == null, scale = 0.98f) { onPick(o.id) }
                    .clip(Radius.tile)
                    .background(p.surface)
                    .border(1.5.dp, border, Radius.tile)
                    .padding(Gap.lg),
            )
        }
    }
}

@Composable
private fun Summary(s: PracticeState, again: () -> Unit, done: () -> Unit) {
    val p = K.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        KairosMark(64.dp, tinted = true)
        Spacer(Modifier.height(Gap.xl))
        Text("Nicely done", style = K.type.serifLarge, color = p.ink)
        Spacer(Modifier.height(Gap.sm))
        Text("You remembered ${s.remembered} of ${s.cards.size}. Each word will come back right before you'd forget it.", style = K.type.body, color = p.inkSoft, textAlign = TextAlign.Center)
        Spacer(Modifier.height(Gap.xxl))
        PrimaryButton("Done", done, Modifier.fillMaxWidth(), dark = true)
        Spacer(Modifier.height(Gap.sm))
        SecondaryButton("Practise more", again, Modifier.fillMaxWidth(), icon = Icons.Shuffle)
    }
}


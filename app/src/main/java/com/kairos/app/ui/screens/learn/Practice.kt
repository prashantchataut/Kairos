package com.kairos.app.ui.screens.learn

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kairos.app.AppContainer
import com.kairos.app.data.content.Word
import com.kairos.app.data.db.WordProgress
import com.kairos.app.data.today
import com.kairos.app.engine.Grade
import com.kairos.app.engine.Practice
import com.kairos.app.engine.PracticeCard
import com.kairos.app.engine.SpacedRepetition
import com.kairos.app.ui.kit.AcrylicCard
import com.kairos.app.ui.kit.CornerWash
import com.kairos.app.ui.kit.InkButton
import com.kairos.app.ui.kit.KairosTopBar
import com.kairos.app.ui.kit.PaintProgress
import com.kairos.app.ui.kit.Pip
import com.kairos.app.ui.kit.PipMood
import com.kairos.app.ui.kit.SoftButton
import com.kairos.app.ui.kit.Tag
import com.kairos.app.ui.kit.glassRim
import com.kairos.app.ui.kit.kairosViewModel
import com.kairos.app.ui.kit.rememberSpeaker
import com.kairos.app.ui.kit.squishClick
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Motion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.random.Random

data class PracticeUi(
    val loading: Boolean = true,
    val cards: List<PracticeCard> = emptyList(),
    val index: Int = 0,
    val progress: Map<String, WordProgress> = emptyMap(),
    val correct: Int = 0,
    val grown: Int = 0,
) {
    val done get() = !loading && index >= cards.size
    val current get() = cards.getOrNull(index)
}

class PracticeViewModel(private val c: AppContainer) : ViewModel() {
    val ui = MutableStateFlow(PracticeUi())
    private val rnd = Random(System.currentTimeMillis())
    private var all: List<Word> = emptyList()

    init { start() }

    fun start() = viewModelScope.launch {
        all = c.learn.words()
        val s = c.settings.current()
        val progress = c.learn.wordProgress.first().associateBy { it.wordId }
        val queue = Practice.buildQueue(all, progress, today(), s.sessionSize, s.level, s.wordThemes, rnd)
        ui.value = PracticeUi(loading = false, cards = queue.mapIndexed { i, w -> Practice.makeCard(w, all, i, rnd) }, progress = progress)
    }

    fun grade(grade: Grade, wasCorrect: Boolean) = viewModelScope.launch {
        val card = ui.value.current ?: return@launch
        val before = SpacedRepetition.growth(ui.value.progress[card.word.id])
        val next = c.learn.review(card.word.id, grade)
        val after = SpacedRepetition.growth(next)
        val u = ui.value
        // "Again" sends the word to the back of this session so it gets another go.
        val cards = if (grade == Grade.Again) u.cards + PracticeCard.Flash(card.word) else u.cards
        ui.value = u.copy(
            cards = cards,
            index = u.index + 1,
            progress = u.progress + (card.word.id to next),
            correct = u.correct + if (wasCorrect) 1 else 0,
            grown = u.grown + if (after.ordinal > before.ordinal) 1 else 0,
        )
    }
}

@Composable
fun PracticeScreen(onBack: () -> Unit) {
    val vm = kairosViewModel { PracticeViewModel(it) }
    val ui by vm.ui.collectAsState()
    val k = Kairos.colors

    Column(Modifier.fillMaxSize().navigationBarsPadding()) {
        KairosTopBar("Practice", onBack)
        if (!ui.loading && ui.cards.isNotEmpty()) {
            PaintProgress(
                ui.index.toFloat() / ui.cards.size,
                Modifier.padding(horizontal = 20.dp),
                color = k.washes.mint.copy(red = 0.35f),
            )
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when {
                ui.loading -> Unit
                ui.cards.isEmpty() -> Summary(ui, empty = true, onAgain = { vm.start() }, onDone = onBack)
                ui.done -> Summary(ui, empty = false, onAgain = { vm.start() }, onDone = onBack)
                else -> AnimatedContent(
                    targetState = ui.index,
                    transitionSpec = {
                        (fadeIn(Motion.enter(420)) + slideInHorizontally(Motion.enter(480)) { it / 3 }) togetherWith
                            (fadeOut(Motion.exit()) + slideOutHorizontally(Motion.exit(200)) { -it / 5 })
                    },
                    label = "card",
                ) { i ->
                    val card = ui.cards.getOrNull(i) ?: return@AnimatedContent
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
                        when (card) {
                            is PracticeCard.Flash -> FlashCard(card, ui.progress[card.word.id]) { g -> vm.grade(g, g != Grade.Again) }
                            is PracticeCard.Choice -> ChoiceCard(card.word.word, card.word, null, card.options, card.answer) { ok -> vm.grade(if (ok) Grade.Good else Grade.Again, ok) }
                            is PracticeCard.Fill -> ChoiceCard(null, card.word, card.sentence, card.options, card.answer) { ok -> vm.grade(if (ok) Grade.Good else Grade.Again, ok) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlashCard(card: PracticeCard.Flash, p: WordProgress?, onGrade: (Grade) -> Unit) {
    val k = Kairos.colors
    val w = card.word
    val speaker = rememberSpeaker()
    var flipped by remember { mutableStateOf(false) }
    val rot by animateFloatAsState(if (flipped) 180f else 0f, if (Kairos.reduceMotion) tween(0) else Motion.ui(), label = "flip")
    Text("Do you remember this one?", style = MaterialTheme.typography.titleMedium, color = k.inkSoft)
    Spacer(Modifier.height(14.dp))
    AcrylicCard(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 320.dp)
            .graphicsLayer { rotationY = rot; cameraDistance = 14f * density }
            .squishClick(pressedScale = 0.98f) { flipped = !flipped; if (!flipped) Unit else speaker.say(w.word) },
        tint = if (rot < 90f) k.washes.sky else k.washes.mint,
        elevation = 16.dp,
    ) {
        CornerWash(if (rot < 90f) k.washes.lilac else k.washes.butter, Modifier.matchParentSize(), seed = w.id.hashCode(), at = Offset(0.8f, 0.2f), scale = 0.5f)
        Box(Modifier.matchParentSize().graphicsLayer { rotationY = if (rot >= 90f) 180f else 0f }.padding(24.dp), contentAlignment = Alignment.Center) {
            if (rot < 90f) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(w.word, style = MaterialTheme.typography.displayMedium, color = k.ink, textAlign = TextAlign.Center)
                    Text("/${w.phonetic}/", style = MaterialTheme.typography.bodyLarge, color = k.inkSoft)
                    Spacer(Modifier.height(18.dp))
                    Text("Tap to flip", style = MaterialTheme.typography.labelMedium, color = k.inkFaint)
                }
            } else {
                Column {
                    Tag(w.pos, color = k.washes.sky)
                    Spacer(Modifier.height(10.dp))
                    Text(w.definition, style = MaterialTheme.typography.headlineSmall, color = k.ink)
                    Spacer(Modifier.height(10.dp))
                    Text("“${w.example}”", style = MaterialTheme.typography.bodyMedium, color = k.inkSoft)
                }
            }
        }
    }
    Spacer(Modifier.height(20.dp))
    if (flipped) {
        Text("How well did you know it?", style = MaterialTheme.typography.titleSmall, color = k.inkSoft)
        Spacer(Modifier.height(10.dp))
        val base = p ?: WordProgress(w.id)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Grade.entries.forEach { g ->
                val color = when (g) { Grade.Again -> k.washes.rose; Grade.Hard -> k.washes.peach; Grade.Good -> k.washes.mint; Grade.Easy -> k.washes.sky }
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(color.copy(alpha = if (k.isDark) 0.45f else 0.6f))
                        .glassRim(RoundedCornerShape(20.dp), k, sheen = true)
                        .squishClick { onGrade(g) }
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(g.label, style = MaterialTheme.typography.labelLarge, color = k.ink)
                    Text(SpacedRepetition.preview(base, g, today()), style = MaterialTheme.typography.labelSmall, color = k.inkSoft)
                }
            }
        }
    } else {
        SoftButton("Show the meaning", { flipped = true; speaker.say(w.word) }, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun ChoiceCard(prompt: String?, word: Word, sentence: String?, options: List<String>, answer: Int, onResult: (Boolean) -> Unit) {
    val k = Kairos.colors
    var picked by remember { mutableIntStateOf(-1) }
    Text(if (sentence != null) "Which word fits?" else "What does it mean?", style = MaterialTheme.typography.titleMedium, color = k.inkSoft)
    Spacer(Modifier.height(10.dp))
    AcrylicCard(Modifier.fillMaxWidth(), tint = k.washes.lilac) {
        Text(
            sentence ?: prompt ?: "",
            style = if (sentence != null) MaterialTheme.typography.titleLarge else MaterialTheme.typography.displaySmall,
            color = k.ink,
            modifier = Modifier.padding(24.dp),
        )
    }
    Spacer(Modifier.height(16.dp))
    options.forEachIndexed { i, o ->
        val state = when {
            picked < 0 -> 0
            i == answer -> 1
            i == picked -> 2
            else -> 3
        }
        val bg by animateColorAsState(
            when (state) { 1 -> k.washes.mint; 2 -> k.washes.rose; else -> k.acrylic },
            Motion.ui(), label = "opt",
        )
        Box(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp)
                .graphicsLayer { alpha = if (state == 3) 0.5f else 1f }
                .clip(RoundedCornerShape(22.dp))
                .background(bg)
                .glassRim(RoundedCornerShape(22.dp), k, sheen = false)
                .squishClick(enabled = picked < 0) { picked = i }
                .padding(18.dp),
        ) {
            Text(o, style = MaterialTheme.typography.bodyLarge, color = k.ink)
        }
    }
    if (picked >= 0) {
        Spacer(Modifier.height(14.dp))
        Text(
            if (picked == answer) "Yes! ${word.word} — ${word.definition.lowercase()}" else "Close. It's “${word.word}”: ${word.definition.lowercase()}",
            style = MaterialTheme.typography.bodyMedium,
            color = k.inkSoft,
        )
        Spacer(Modifier.height(12.dp))
        InkButton("Next", { onResult(picked == answer) }, icon = Icons.AutoMirrored.Rounded.ArrowForward, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun Summary(ui: PracticeUi, empty: Boolean, onAgain: () -> Unit, onDone: () -> Unit) {
    val k = Kairos.colors
    Column(
        Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AnimatedContent(true, transitionSpec = { scaleIn(Motion.bouncy()) togetherWith fadeOut() }, label = "pip") {
            Pip(if (empty) PipMood.Calm else PipMood.Proud, size = 180.dp, poke = 1)
        }
        Text(
            if (empty) "Your garden is all watered" else "Garden watered!",
            style = MaterialTheme.typography.headlineMedium,
            color = k.ink,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (empty) "Nothing is due right now. Come back tomorrow, or save some new words to grow."
            else "${ui.correct} remembered · ${ui.grown} word${if (ui.grown == 1) "" else "s"} grew a stage",
            style = MaterialTheme.typography.bodyLarge,
            color = k.inkSoft,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        InkButton("Done", onDone, modifier = Modifier.fillMaxWidth())
        if (!empty) {
            Spacer(Modifier.height(10.dp))
            SoftButton("Another round", onAgain, modifier = Modifier.fillMaxWidth())
        }
    }
}

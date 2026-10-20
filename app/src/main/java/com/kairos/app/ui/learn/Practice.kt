package com.kairos.app.ui.learn

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.kairos.app.AppContainer
import com.kairos.app.engine.daily.Mood
import com.kairos.app.engine.learn.Exercise
import com.kairos.app.engine.learn.Grade
import com.kairos.app.ui.art.Kiri
import com.kairos.app.ui.components.DetailTopBar
import com.kairos.app.ui.components.EmptyState
import com.kairos.app.ui.components.Eyebrow
import com.kairos.app.ui.components.clickableNoRipple
import com.kairos.app.ui.components.kairosViewModel
import com.kairos.app.ui.glass.ButtonTone
import com.kairos.app.ui.glass.GlassCard
import com.kairos.app.ui.glass.GlassIconButton
import com.kairos.app.ui.glass.GlassWeight
import com.kairos.app.ui.glass.KButton
import com.kairos.app.ui.glass.liquidGlass
import com.kairos.app.ui.glass.pressScale
import com.kairos.app.ui.nav.LocalNavigator
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Motion
import com.kairos.app.ui.theme.Radii
import com.kairos.app.ui.theme.Space
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PracticeUi(
    val loading: Boolean = true,
    val exercises: List<Exercise> = emptyList(),
    val index: Int = 0,
    val correct: Int = 0,
    val finished: Boolean = false,
)

class PracticeViewModel(private val c: AppContainer, focus: List<String>?) : ViewModel() {
    private val _state = MutableStateFlow(PracticeUi())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val ex = c.learn.buildSession(size = if (focus != null) focus.size.coerceAtLeast(1) else 8, focus = focus)
            _state.value = PracticeUi(loading = false, exercises = ex, finished = ex.isEmpty())
        }
    }

    fun answer(grade: Grade) {
        val s = _state.value
        val ex = s.exercises.getOrNull(s.index) ?: return
        viewModelScope.launch { c.learn.review(ex.word.id, grade) }
        val next = s.index + 1
        val correct = s.correct + if (grade.quality >= 3) 1 else 0
        _state.update { it.copy(index = next, correct = correct, finished = next >= it.exercises.size) }
        if (next >= s.exercises.size) viewModelScope.launch { c.learn.finishSession(correct, s.exercises.size) }
    }

    fun speak(text: String) = c.speaker.say(text)
}

@Composable
fun PracticeScreen(focus: List<String>?) {
    val vm = kairosViewModel(key = "practice-${focus?.joinToString()}") { PracticeViewModel(it, focus) }
    val ui by vm.state.collectAsStateWithLifecycle()
    val nav = LocalNavigator.current

    Column(Modifier.fillMaxSize()) {
        DetailTopBar(onBack = nav::back, title = if (ui.finished) "Round complete" else "Practice")
        if (!ui.finished && ui.exercises.isNotEmpty()) {
            LiquidProgress((ui.index) / ui.exercises.size.toFloat(), Modifier.padding(horizontal = Space.screen))
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when {
                ui.loading -> Unit
                ui.exercises.isEmpty() -> EmptyState(Mood.Okay, "Nothing to practice", "Plant a few words first — tap “Keep it” on today's word.")
                ui.finished -> Summary(ui.correct, ui.exercises.size) { nav.back() }
                else -> AnimatedContent(
                    ui.index,
                    transitionSpec = {
                        (slideInHorizontally(tween(380, easing = Motion.Move)) { it / 3 } + fadeIn(tween(260)))
                            .togetherWith(slideOutHorizontally(tween(260)) { -it / 4 } + fadeOut(tween(180)))
                    },
                    label = "exercise",
                ) { i ->
                    val ex = ui.exercises.getOrNull(i) ?: return@AnimatedContent
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Space.screen)) {
                        when (ex) {
                            is Exercise.Flashcard -> FlashcardView(ex, vm::speak, vm::answer)
                            is Exercise.Meaning -> ChoiceView(
                                eyebrow = "What does it mean?",
                                prompt = { Text(ex.word.word, style = Kairos.type.hero.copy(fontSize = 48.sp), color = Kairos.colors.ink) },
                                options = ex.options.map { it.id to it.definition },
                                answerId = ex.word.id,
                                onDone = vm::answer,
                            )
                            is Exercise.Cloze -> ChoiceView(
                                eyebrow = "Fill the gap",
                                prompt = { Text(ex.sentence, style = Kairos.type.quote.copy(fontStyle = FontStyle.Italic), color = Kairos.colors.ink) },
                                options = ex.options.map { it.id to it.word },
                                answerId = ex.word.id,
                                onDone = vm::answer,
                                bigOptions = true,
                            )
                            is Exercise.Unscramble -> UnscrambleView(ex, vm::answer)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiquidProgress(progress: Float, modifier: Modifier = Modifier) {
    val colors = Kairos.colors
    val p by animateFloatAsState(progress, Motion.gentle(), label = "progress")
    val wave = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        while (true) wave.animateTo(wave.value + 6.283f, tween(2400, easing = androidx.compose.animation.core.LinearEasing))
    }
    Canvas(modifier.fillMaxWidth().height(14.dp).semantics { contentDescription = "${(progress * 100).toInt()} percent done" }) {
        val r = CornerRadius(size.height / 2)
        drawRoundRect(colors.ink.copy(alpha = 0.08f), cornerRadius = r)
        val w = size.width * p.coerceIn(0f, 1f)
        if (w > 0f) {
            drawRoundRect(Brush.horizontalGradient(listOf(colors.sky, colors.pond)), size = Size(w, size.height), cornerRadius = r)
            // a travelling glint so the bar reads as liquid
            val gx = (wave.value / 6.283f % 1f) * w
            drawCircle(Color.White.copy(alpha = 0.55f), size.height * 0.22f, Offset(gx, size.height * 0.35f))
            drawRoundRect(Color.White.copy(alpha = 0.35f), topLeft = Offset(size.height / 2, 2f), size = Size((w - size.height).coerceAtLeast(0f), size.height * 0.25f), cornerRadius = r)
        }
    }
}

@Composable
private fun FlashcardView(ex: Exercise.Flashcard, speak: (String) -> Unit, onGrade: (Grade) -> Unit) {
    val colors = Kairos.colors
    var flipped by remember { mutableStateOf(false) }
    val rot by animateFloatAsState(if (flipped) 180f else 0f, tween(520, easing = Motion.Enter), label = "flip")
    val haptics = LocalHapticFeedback.current
    Eyebrow("Do you remember this one?")
    Spacer(Modifier.height(Space.lg))
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 320.dp)
            .graphicsLayer { rotationY = rot; cameraDistance = 14 * density }
            .liquidGlass(Radii.sheet, GlassWeight.Frosted, blur = true, elevation = 12.dp)
            .clickableNoRipple { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); flipped = !flipped }
            .padding(Space.xl)
            .semantics { contentDescription = if (flipped) "Card back. ${ex.word.definition}" else "Card front, ${ex.word.word}. Tap to flip." },
        contentAlignment = Alignment.Center,
    ) {
        if (rot <= 90f) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(ex.word.word, style = Kairos.type.hero, color = colors.ink, textAlign = TextAlign.Center)
                Spacer(Modifier.height(Space.md))
                GlassIconButton(Icons.AutoMirrored.Rounded.VolumeUp, "Hear it", { speak(ex.word.word) }, blur = false)
                Spacer(Modifier.height(Space.lg))
                Text("Tap to flip", style = Kairos.type.caption, color = colors.inkFaint)
            }
        } else {
            Column(Modifier.graphicsLayer { rotationY = 180f }) {
                Eyebrow(ex.word.pos)
                Spacer(Modifier.height(Space.sm))
                Text(ex.word.definition, style = Kairos.type.body.copy(fontSize = 20.sp, lineHeight = 28.sp), color = colors.ink)
                Spacer(Modifier.height(Space.lg))
                Text("“${ex.word.example}”", style = Kairos.type.quote.copy(fontStyle = FontStyle.Italic, fontSize = 18.sp), color = colors.inkSoft)
            }
        }
    }
    Spacer(Modifier.height(Space.xl))
    if (flipped) {
        Text("How well did it come back?", style = Kairos.type.label, color = colors.inkSoft)
        Spacer(Modifier.height(Space.md))
        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
            Grade.entries.forEach { g ->
                KButton(
                    g.label, { onGrade(g) },
                    tone = if (g == Grade.Good) ButtonTone.Pond else ButtonTone.Glass,
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 14.dp),
                )
            }
        }
    } else {
        KButton("Show me", { flipped = true }, tone = ButtonTone.Glass, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun ChoiceView(
    eyebrow: String,
    prompt: @Composable () -> Unit,
    options: List<Pair<String, String>>,
    answerId: String,
    onDone: (Grade) -> Unit,
    bigOptions: Boolean = false,
) {
    val colors = Kairos.colors
    val haptics = LocalHapticFeedback.current
    var picked by remember { mutableStateOf<String?>(null) }
    var firstTry by remember { mutableStateOf(true) }
    val shake = remember { Animatable(0f) }
    LaunchedEffect(picked) {
        if (picked != null && picked != answerId) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            shake.animateTo(0f, keyframes { durationMillis = 360; -14f at 60; 12f at 130; -8f at 200; 5f at 270; 0f at 360 })
        }
    }
    Eyebrow(eyebrow)
    Spacer(Modifier.height(Space.lg))
    prompt()
    Spacer(Modifier.height(Space.xl))
    Column(Modifier.graphicsLayer { translationX = shake.value.dp.toPx() }, verticalArrangement = Arrangement.spacedBy(Space.sm)) {
        options.forEach { (id, text) ->
            val state = when {
                picked == null -> 0
                id == answerId && (picked == answerId || !firstTry) -> 1
                id == picked -> 2
                else -> 0
            }
            val border by animateColorAsState(
                when (state) { 1 -> colors.success; 2 -> colors.danger; else -> Color.Transparent }, label = "optBorder",
            )
            val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            Box(
                Modifier
                    .fillMaxWidth()
                    .pressScale(interaction, 0.98f)
                    .liquidGlass(Radii.card, GlassWeight.Frosted)
                    .border(2.dp, border, Radii.card)
                    .then(
                        if (picked != answerId) Modifier.clickableNoRipple {
                            if (id != answerId) firstTry = false else haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            picked = id
                        } else Modifier,
                    )
                    .padding(horizontal = 18.dp, vertical = if (bigOptions) 18.dp else 14.dp),
            ) {
                Text(
                    text,
                    style = if (bigOptions) Kairos.type.title.copy(fontSize = 22.sp) else Kairos.type.body,
                    color = colors.ink,
                )
            }
        }
    }
    Spacer(Modifier.height(Space.xl))
    if (picked == answerId) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(if (firstTry) "Lovely — that's it." else "There it is. It'll come back sooner.", style = Kairos.type.body, color = colors.success, modifier = Modifier.weight(1f))
            KButton("Next", { onDone(if (firstTry) Grade.Good else Grade.Again) })
        }
    } else if (picked != null) {
        Text("Not quite — try another.", style = Kairos.type.body, color = colors.danger)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UnscrambleView(ex: Exercise.Unscramble, onDone: (Grade) -> Unit) {
    val colors = Kairos.colors
    val haptics = LocalHapticFeedback.current
    val used = remember { mutableStateListOf<Int>() }
    var hinted by remember { mutableStateOf(false) }
    val built = used.joinToString("") { ex.letters[it].toString() }
    val target = ex.word.word.lowercase()
    val solved = built == target
    LaunchedEffect(solved) { if (solved) haptics.performHapticFeedback(HapticFeedbackType.LongPress) }

    Eyebrow("Rebuild the word")
    Spacer(Modifier.height(Space.md))
    Text(ex.word.definition, style = Kairos.type.body.copy(fontSize = 18.sp, lineHeight = 26.sp), color = colors.ink)
    Spacer(Modifier.height(Space.xl))
    GlassCard(Modifier.fillMaxWidth().heightIn(min = 84.dp), weight = GlassWeight.Frosted, blur = true) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                built.ifEmpty { if (hinted) target.first() + "…" else " " },
                style = Kairos.type.display.copy(letterSpacing = 2.sp),
                color = if (solved) colors.success else if (built.isEmpty()) colors.inkFaint else colors.ink,
                modifier = Modifier.weight(1f),
            )
            if (used.isNotEmpty() && !solved) GlassIconButton(Icons.Rounded.Backspace, "Remove last letter", { used.removeAt(used.lastIndex) }, blur = false)
        }
    }
    Spacer(Modifier.height(Space.xl))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.sm), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
        ex.letters.forEachIndexed { i, ch ->
            val isUsed = i in used
            val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            val alpha by animateFloatAsState(if (isUsed) 0.25f else 1f, label = "tile")
            Box(
                Modifier
                    .size(52.dp)
                    .pressScale(interaction, 0.86f)
                    .graphicsLayer { this.alpha = alpha }
                    .liquidGlass(Radii.small, GlassWeight.Frosted)
                    .background(colors.sky.copy(alpha = 0.35f), Radii.small)
                    .then(if (!isUsed && !solved) Modifier.clickableNoRipple { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); used.add(i) } else Modifier)
                    .semantics { contentDescription = "Letter $ch" },
                contentAlignment = Alignment.Center,
            ) {
                Text(ch.uppercase(), style = Kairos.type.heading, color = colors.ink)
            }
        }
    }
    Spacer(Modifier.height(Space.xl))
    when {
        solved -> KButton("Next", { onDone(if (hinted) Grade.Hard else Grade.Good) }, modifier = Modifier.fillMaxWidth())
        built.length == target.length -> Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
            Text("Almost — shuffle a letter or two.", style = Kairos.type.body, color = colors.danger, modifier = Modifier.weight(1f))
            KButton("Clear", { used.clear() }, tone = ButtonTone.Glass)
        }
        else -> Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
            KButton("Hint", { hinted = true }, tone = ButtonTone.Glass, modifier = Modifier.weight(1f))
            KButton("Show me", { onDone(Grade.Again) }, tone = ButtonTone.Glass, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun Summary(correct: Int, total: Int, onDone: () -> Unit) {
    val colors = Kairos.colors
    val ratio = if (total == 0) 0f else correct / total.toFloat()
    val mood = when {
        ratio >= 0.85f -> Mood.Glowing
        ratio >= 0.6f -> Mood.Sunny
        else -> Mood.Okay
    }
    Column(
        Modifier.fillMaxSize().padding(Space.screen).windowInsetsPadding(WindowInsets.navigationBars),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Kiri(mood, size = 180.dp)
        Spacer(Modifier.height(Space.xl))
        Text("$correct of $total came back to you", style = Kairos.type.title, color = colors.ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(Space.sm))
        Text(
            when (mood) {
                Mood.Glowing -> "Your garden is thriving. Kiri is doing a little dance."
                Mood.Sunny -> "Good round. The tricky ones will come back sooner, gently."
                else -> "Every miss is a seed being watered. They'll return when you're ready."
            },
            style = Kairos.type.bodyRegular, color = colors.inkSoft, textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Space.xxl))
        KButton("Done", onDone, modifier = Modifier.fillMaxWidth())
    }
}

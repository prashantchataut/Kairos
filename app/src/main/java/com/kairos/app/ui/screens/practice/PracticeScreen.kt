package com.kairos.app.ui.screens.practice

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.engine.Recall
import com.kairos.app.engine.Srs
import com.kairos.app.engine.SrsState
import com.kairos.app.ui.components.EmptyState
import com.kairos.app.ui.components.GlassButton
import com.kairos.app.ui.components.IconBubble
import com.kairos.app.ui.components.PaintBurst
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.squishClickable
import com.kairos.app.ui.glass.liquidGlass
import com.kairos.app.ui.icons.KairosIcons
import com.kairos.app.ui.mascot.DropMascot
import com.kairos.app.ui.mascot.Expression
import com.kairos.app.ui.navigation.Scene
import com.kairos.app.ui.paint.WashSpot
import com.kairos.app.ui.screens.today.categoryWash
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Motion
import com.kairos.app.ui.theme.Radii
import com.kairos.app.ui.theme.Space
import com.kairos.app.ui.theme.Wash
import com.kairos.app.util.appViewModel
import com.kairos.app.util.rememberSpeaker

@Composable
fun PracticeScreen(onBack: () -> Unit) {
    val vm = appViewModel { PracticeViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    Scene(Wash.Mint, Wash.Cerulean, Wash.Lilac, Wash.Mint, Wash.Butter)
    val speaker = rememberSpeaker()

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .navigationBarsPadding()
            .padding(horizontal = Space.gutter)
    ) {
        Row(Modifier.fillMaxWidth().padding(vertical = Space.sm), verticalAlignment = Alignment.CenterVertically) {
            IconBubble(KairosIcons.Close, "Close practice", onBack, blur = true)
            Spacer(Modifier.size(Space.md))
            LiquidProgress(state.progress, Modifier.weight(1f))
            Spacer(Modifier.size(Space.md))
            Text(
                if (state.cards.isEmpty()) "" else "${(state.index + 1).coerceAtMost(state.cards.size)}/${state.cards.size}",
                style = Kairos.type.label, color = Kairos.colors.inkSoft,
            )
        }
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            when {
                state.loading -> DropMascot(size = 90.dp, expression = Expression.Sleepy)
                state.cards.isEmpty() -> EmptyState(
                    "Nothing to practise yet",
                    "Save a few words you like and they'll show up here, spaced out so they stick.",
                    action = { PrimaryButton("Back to Kairos", onBack) },
                )
                state.done -> Summary(state, onAgain = vm::load, onDone = onBack)
                else -> AnimatedContent(
                    targetState = state.index,
                    transitionSpec = {
                        (slideInHorizontally(Motion.glideOffset) { it / 2 } + fadeIn() + scaleIn(initialScale = 0.9f)) togetherWith
                            (slideOutHorizontally(Motion.glideOffset) { -it / 2 } + fadeOut())
                    },
                    label = "card",
                ) { idx ->
                    val card = state.cards.getOrNull(idx) ?: return@AnimatedContent
                    when (card.mode) {
                        CardMode.Flip -> FlipCard(card, revealed = state.revealed && idx == state.index, onReveal = vm::reveal, onSpeak = { speaker.say(card.word.word) })
                        CardMode.Quiz -> QuizCard(card, picked = if (idx == state.index) state.picked else null, onPick = vm::pick)
                    }
                }
            }
        }
        val current = state.current
        if (!state.done && current != null && current.mode == CardMode.Flip) {
            Box(Modifier.fillMaxWidth().height(96.dp), contentAlignment = Alignment.Center) {
                AnimatedContent(state.revealed, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "grade") { revealed ->
                    if (!revealed) {
                        PrimaryButton("Show meaning", vm::reveal, Modifier.fillMaxWidth())
                    } else {
                        val srs = SrsState(current.word.reps, current.word.ease, current.word.intervalDays, current.word.lapses)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                            Recall.entries.forEach { r ->
                                GradeButton(r, Srs.preview(srs, r), Modifier.weight(1f)) { vm.grade(r) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiquidProgress(progress: Float, modifier: Modifier = Modifier) {
    val p by animateFloatAsState(progress, Motion.settle(), label = "progress")
    val palette = Kairos.colors
    Box(modifier.height(14.dp).clip(CircleShape).background(palette.glassFillStrong)) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(p.coerceIn(0.04f, 1f))
                .clip(CircleShape)
                .background(androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Wash.Mint.color(palette.isDark), palette.accent)))
        )
    }
}

@Composable
private fun GradeButton(recall: Recall, preview: String, modifier: Modifier, onClick: () -> Unit) {
    val palette = Kairos.colors
    val tint = when (recall) {
        Recall.Again -> palette.danger
        Recall.Hard -> palette.warning
        Recall.Good -> palette.success
        Recall.Easy -> palette.accent
    }
    Column(
        modifier
            .squishClickable(onClick = onClick)
            .liquidGlass(Radii.Control, blur = false, elevation = 4.dp, strong = true)
            .padding(vertical = Space.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(recall.label, style = Kairos.type.label, color = tint)
        Text(preview, style = Kairos.type.caption, color = palette.inkSoft)
    }
}

/** A real 3D flip: the card turns on its Y axis and the back face is drawn mirrored back. */
@Composable
private fun FlipCard(card: PracticeCard, revealed: Boolean, onReveal: () -> Unit, onSpeak: () -> Unit) {
    val w = card.word
    val palette = Kairos.colors
    val rotation by animateFloatAsState(if (revealed) 180f else 0f, Motion.settle(), label = "flip")
    val wash = categoryWash(w.category).color(palette.isDark)
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(0.72f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density
            }
            .squishClickable(enabled = !revealed, pressedScale = 0.98f, onClick = onReveal)
            .liquidGlass(Radii.Card, blur = true, elevation = 20.dp)
    ) {
        WashSpot(wash, w.id.hashCode(), Modifier.size(320.dp).align(Alignment.TopStart))
        if (rotation <= 90f) {
            Column(Modifier.fillMaxSize().padding(Space.xl), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(w.word, style = Kairos.type.hero, color = palette.ink, textAlign = TextAlign.Center)
                Spacer(Modifier.height(Space.sm))
                Text(w.partOfSpeech, style = Kairos.type.body.copy(fontStyle = FontStyle.Italic), color = palette.inkSoft)
                Spacer(Modifier.height(Space.xl))
                GlassButton(w.pronunciation, onSpeak, icon = KairosIcons.Speaker)
                Spacer(Modifier.height(Space.xxl))
                Text("Do you remember what it means?", style = Kairos.type.label, color = palette.inkFaint)
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
                    .verticalScroll(rememberScrollState())
                    .padding(Space.xl),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(w.word, style = Kairos.type.display, color = palette.ink)
                Spacer(Modifier.height(Space.md))
                Text(w.definition, style = Kairos.type.title.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold), color = palette.ink)
                Spacer(Modifier.height(Space.lg))
                Text("“${w.example}”", style = Kairos.type.quote.copy(fontSize = Kairos.type.body.fontSize * 1.1f, lineHeight = Kairos.type.body.lineHeight * 1.1f), color = palette.inkSoft)
            }
        }
    }
}

@Composable
private fun QuizCard(card: PracticeCard, picked: String?, onPick: (String) -> Unit) {
    val palette = Kairos.colors
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Which one means", style = Kairos.type.label, color = palette.inkSoft)
        Spacer(Modifier.height(Space.xs))
        Text(card.word.word, style = Kairos.type.hero, color = palette.ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(Space.xl))
        card.options.forEach { option ->
            val isAnswer = option.id == card.word.id
            val isPicked = option.id == picked
            val shake = remember { Animatable(0f) }
            LaunchedEffect(isPicked) {
                if (isPicked && !isAnswer) repeat(3) {
                    shake.animateTo(10f, tween(50)); shake.animateTo(-10f, tween(50))
                }
                shake.animateTo(0f, tween(50))
            }
            val border by animateColorAsState(
                when {
                    picked == null -> Color.Transparent
                    isAnswer -> palette.success
                    isPicked -> palette.danger
                    else -> Color.Transparent
                }, label = "quizBorder",
            )
            Box(Modifier.padding(vertical = Space.xs)) {
                if (isPicked && isAnswer) PaintBurst(picked, palette.success, Modifier.size(220.dp).align(Alignment.Center))
                Text(
                    option.definition,
                    style = Kairos.type.body,
                    color = palette.ink,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { translationX = shake.value }
                        .squishClickable(enabled = picked == null, pressedScale = 0.97f) { onPick(option.id) }
                        .liquidGlass(Radii.Control, blur = false, elevation = 4.dp, strong = true)
                        .border(2.dp, border, Radii.Control)
                        .padding(Space.lg),
                )
            }
        }
    }
}

@Composable
private fun Summary(state: PracticeState, onAgain: () -> Unit, onDone: () -> Unit) {
    val palette = Kairos.colors
    val total = state.cards.size
    val good = (state.results[Recall.Good] ?: 0) + (state.results[Recall.Easy] ?: 0)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(Space.lg)) {
        Box(contentAlignment = Alignment.Center) {
            PaintBurst(total, palette.accent, Modifier.size(260.dp), count = 18)
            DropMascot(size = 140.dp, wash = Wash.Mint, expression = Expression.Giggle)
        }
        Spacer(Modifier.height(Space.lg))
        Text("Lovely work", style = Kairos.type.display, color = palette.ink)
        Spacer(Modifier.height(Space.sm))
        Text(
            "You practised $total ${if (total == 1) "word" else "words"} and remembered $good. " +
                "Each one will come back right before you'd forget it.",
            style = Kairos.type.body, color = palette.inkSoft, textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Space.xl))
        PrimaryButton("Done", onDone, Modifier.fillMaxWidth())
        Spacer(Modifier.height(Space.sm))
        GlassButton("Practise more", onAgain, Modifier.fillMaxWidth(), icon = KairosIcons.Shuffle)
    }
}

package com.kairos.app.ui.screens.today

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.data.content.Taxonomy
import com.kairos.app.data.repo.today
import com.kairos.app.engine.Mood
import com.kairos.app.ui.brand.KairosWordmark
import com.kairos.app.ui.components.GlassButton
import com.kairos.app.ui.components.HeartButton
import com.kairos.app.ui.components.IconBubble
import com.kairos.app.ui.components.JoyJar
import com.kairos.app.ui.components.MoodPicker
import com.kairos.app.ui.components.PaintBurst
import com.kairos.app.ui.components.Pill
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.squishClickable
import com.kairos.app.ui.glass.GlassPanel
import com.kairos.app.ui.icons.KairosIcons
import com.kairos.app.ui.mascot.DropMascot
import com.kairos.app.ui.mascot.Expression
import com.kairos.app.ui.mascot.expression
import com.kairos.app.ui.navigation.Scene
import com.kairos.app.ui.navigation.sharedText
import com.kairos.app.ui.paint.WashSpot
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Radii
import com.kairos.app.ui.theme.Space
import com.kairos.app.ui.theme.Wash
import com.kairos.app.util.Fmt
import com.kairos.app.util.appViewModel
import com.kairos.app.util.rememberSpeaker
import java.time.LocalTime

@Composable
fun TodayScreen(
    openWord: (String) -> Unit,
    openQuote: (String) -> Unit,
    openStudio: (String, String) -> Unit,
    openPractice: () -> Unit,
    openCompose: () -> Unit,
    openCapsuleTab: () -> Unit,
) {
    val vm = appViewModel { TodayViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    val mood = state.mood
    Scene(mood?.wash ?: Wash.Cerulean, Wash.Lilac, Wash.Peach, Wash.Mint, Wash.Butter)
    val speaker = rememberSpeaker()

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 130.dp),
    ) {
        item {
            Column(
                Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = Space.gutter)
                    .padding(top = Space.md)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    KairosWordmark(Modifier.weight(1f), markSize = 26.dp, textSize = androidx.compose.ui.unit.TextUnit(24f, androidx.compose.ui.unit.TextUnitType.Sp))
                    Pill("${state.joy.map { it.epochDay }.distinct().size} days of good", icon = KairosIcons.Sparkle)
                }
                Spacer(Modifier.height(Space.lg))
                WeekStrip(state)
            }
        }
        item { Greeting(state, onPoke = {}) }
        item {
            MoodCard(mood, onPick = vm::logMood, modifier = Modifier.padding(horizontal = Space.gutter))
        }
        state.today?.let { t ->
            item {
                WordOfTheDay(
                    t = t,
                    onOpen = { openWord(t.word.id) },
                    onSave = { vm.toggleWord(t.word.id) },
                    onShare = { openStudio("word", t.word.id) },
                    onSpeak = { speaker.say(t.word.word) },
                    onPractice = openPractice,
                    dueWords = state.dueWords,
                    modifier = Modifier.padding(horizontal = Space.gutter, vertical = Space.xl),
                )
            }
            item {
                QuoteCard(
                    t = t,
                    onOpen = { openQuote(t.quote.id) },
                    onSave = { vm.toggleQuote(t.quote.id) },
                    onShare = { openStudio("quote", t.quote.id) },
                    onAnother = vm::anotherQuote,
                    modifier = Modifier.padding(horizontal = Space.gutter),
                )
            }
            item {
                SparkCard(
                    text = t.spark?.text ?: "Drink a glass of water slowly, like it's a treat.",
                    done = t.sparkDone,
                    drops = state.joy.map { Wash.valueOf(it.wash).color(Kairos.colors.isDark) },
                    onDone = vm::completeSpark,
                    modifier = Modifier.padding(horizontal = Space.gutter, vertical = Space.xl),
                )
            }
        }
        item {
            CapsuleTeaser(state, openCompose = openCompose, openCapsuleTab = openCapsuleTab, modifier = Modifier.padding(horizontal = Space.gutter))
        }
    }
}

@Composable
private fun WeekStrip(state: TodayState) {
    val palette = Kairos.colors
    val todayDay = today()
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        state.week.forEach { date ->
            val epoch = date.toEpochDay()
            val isToday = epoch == todayDay
            val active = epoch in state.activeDays
            Column(
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .then(if (isToday) Modifier.background(palette.glassFillStrong) else Modifier)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()),
                    style = Kairos.type.caption,
                    color = if (isToday) palette.ink else palette.inkFaint,
                )
                Text(
                    date.dayOfMonth.toString(),
                    style = Kairos.type.title,
                    color = when {
                        isToday -> palette.ink
                        epoch > todayDay -> palette.inkFaint
                        else -> palette.inkSoft
                    },
                )
                Spacer(Modifier.height(4.dp))
                // A filled drop marks a day you showed up.
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (active) palette.washes[(date.dayOfWeek.value) % palette.washes.size]
                            else palette.inkFaint.copy(alpha = 0.18f)
                        )
                )
            }
        }
    }
}

@Composable
private fun Greeting(state: TodayState, onPoke: () -> Unit) {
    val mood = state.mood
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = Space.gutter, end = Space.md, top = Space.xl, bottom = Space.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(Fmt.greeting(LocalTime.now().hour) + ",", style = Kairos.type.body, color = Kairos.colors.inkSoft)
            Text(
                state.name.ifBlank { "friend" },
                style = Kairos.type.hero.copy(fontSize = Kairos.type.display.fontSize * 1.25f, lineHeight = Kairos.type.display.lineHeight * 1.2f),
                color = Kairos.colors.ink,
                maxLines = 2,
            )
            Spacer(Modifier.height(Space.xs))
            AnimatedContent(mood, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "reply") { m ->
                Text(
                    m?.reply ?: "Here's your small good moment for today.",
                    style = Kairos.type.body, color = Kairos.colors.inkSoft,
                )
            }
        }
        DropMascot(
            size = 108.dp,
            wash = mood?.wash ?: Wash.Cerulean,
            expression = mood?.expression() ?: Expression.Happy,
            onPoke = onPoke,
        )
    }
}

@Composable
private fun MoodCard(mood: Mood?, onPick: (Mood) -> Unit, modifier: Modifier = Modifier) {
    var editing by rememberSaveable { mutableStateOf(false) }
    val open = mood == null || editing
    GlassPanel(modifier.fillMaxWidth().animateContentSize(), shape = Radii.Card) {
        AnimatedContent(open, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "mood") { isOpen ->
            if (isOpen) {
                Column(Modifier.padding(Space.xl)) {
                    Text("How's your heart today?", style = Kairos.type.title, color = Kairos.colors.ink)
                    Spacer(Modifier.height(Space.xs))
                    Text("Your answer gently steers today's thought.", style = Kairos.type.label, color = Kairos.colors.inkSoft)
                    Spacer(Modifier.height(Space.lg))
                    MoodPicker(mood, onPick = { onPick(it); editing = false })
                }
            } else {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .squishClickable(pressedScale = 0.98f) { editing = true }
                        .padding(horizontal = Space.xl, vertical = Space.lg),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(12.dp).clip(CircleShape).background(mood!!.wash.color(Kairos.colors.isDark)))
                    Spacer(Modifier.width(Space.md))
                    Text("Feeling ${mood.label.lowercase()} today", style = Kairos.type.label, color = Kairos.colors.ink, modifier = Modifier.weight(1f))
                    Text("Change", style = Kairos.type.label, color = Kairos.colors.accent)
                }
            }
        }
    }
}

@Composable
private fun WordOfTheDay(
    t: com.kairos.app.data.repo.Today,
    onOpen: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onSpeak: () -> Unit,
    onPractice: () -> Unit,
    dueWords: Int,
    modifier: Modifier = Modifier,
) {
    val w = t.word
    val palette = Kairos.colors
    GlassPanel(modifier.fillMaxWidth().squishClickable(pressedScale = 0.985f, haptic = false, onClick = onOpen)) {
        // A pool of pigment sits behind the word, coloured by its topic.
        WashSpot(
            color = categoryWash(w.category).color(palette.isDark),
            seed = w.id.hashCode(),
            modifier = Modifier.size(260.dp).align(Alignment.TopEnd).offset(x = 80.dp, y = (-70).dp),
            strength = 0.9f,
        )
        Column(Modifier.padding(Space.xl)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Today's word", style = Kairos.type.label, color = palette.accent)
                Spacer(Modifier.width(Space.sm))
                Text(Taxonomy.wordLabel(w.category), style = Kairos.type.caption, color = palette.inkFaint)
            }
            Spacer(Modifier.height(Space.md))
            Text(
                w.word,
                style = Kairos.type.hero,
                color = palette.ink,
                modifier = Modifier.sharedText("word-${w.id}-today"),
            )
            Spacer(Modifier.height(Space.sm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Pill(w.pronunciation, icon = KairosIcons.Speaker, onClick = onSpeak)
                Spacer(Modifier.width(Space.sm))
                Text(w.partOfSpeech, style = Kairos.type.body.copy(fontStyle = FontStyle.Italic), color = palette.inkSoft)
            }
            Spacer(Modifier.height(Space.lg))
            Text(w.definition, style = Kairos.type.body.copy(fontSize = Kairos.type.title.fontSize, lineHeight = Kairos.type.title.lineHeight * 1.15f), color = palette.ink)
            Spacer(Modifier.height(Space.md))
            Row {
                Box(Modifier.width(3.dp).height(44.dp).clip(CircleShape).background(categoryWash(w.category).color(palette.isDark)))
                Spacer(Modifier.width(Space.md))
                Text("“${w.example}”", style = Kairos.type.quote.copy(fontSize = Kairos.type.body.fontSize, lineHeight = Kairos.type.body.lineHeight), color = palette.inkSoft)
            }
            Spacer(Modifier.height(Space.sm))
            Text(t.wordReason, style = Kairos.type.caption, color = palette.inkFaint)
            Spacer(Modifier.height(Space.lg))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                HeartButton(w.saved, onSave)
                IconBubble(KairosIcons.Share, "Make a share card", onShare)
                Spacer(Modifier.weight(1f))
                GlassButton(if (dueWords > 0) "Review $dueWords" else "Practise", onPractice, icon = KairosIcons.Cards)
            }
        }
    }
}

@Composable
private fun QuoteCard(
    t: com.kairos.app.data.repo.Today,
    onOpen: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onAnother: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = Kairos.colors
    var spins by remember { mutableIntStateOf(0) }
    GlassPanel(modifier.fillMaxWidth(), blur = true) {
        WashSpot(
            color = themeWash(t.quote.theme).color(palette.isDark),
            seed = t.quote.id.hashCode(),
            modifier = Modifier.size(220.dp).align(Alignment.BottomStart).offset(x = (-70).dp, y = 60.dp),
        )
        Column(Modifier.padding(Space.xl)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(KairosIcons.Quote, null, tint = themeWash(t.quote.theme).color(palette.isDark), modifier = Modifier.size(34.dp))
                Spacer(Modifier.weight(1f))
                Text(t.quoteReason, style = Kairos.type.caption, color = palette.inkFaint)
            }
            Spacer(Modifier.height(Space.md))
            AnimatedContent(t.quote, transitionSpec = {
                (fadeIn(com.kairos.app.ui.theme.Motion.fade()) + androidx.compose.animation.slideInVertically { it / 6 }) togetherWith fadeOut()
            }, label = "quote") { q ->
                Column(Modifier.squishClickable(pressedScale = 0.98f, haptic = false, onClick = onOpen)) {
                    Text(q.text, style = Kairos.type.quote, color = palette.ink)
                    Spacer(Modifier.height(Space.md))
                    Text("— ${q.author}", style = Kairos.type.label, color = palette.inkSoft)
                }
            }
            Spacer(Modifier.height(Space.lg))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                HeartButton(t.quote.saved, onSave)
                IconBubble(KairosIcons.Share, "Make a share card", onShare)
                Spacer(Modifier.weight(1f))
                GlassButton("Another", { spins++; onAnother() }, icon = KairosIcons.Shuffle)
            }
        }
    }
}

@Composable
private fun SparkCard(text: String, done: Boolean, drops: List<Color>, onDone: () -> Unit, modifier: Modifier = Modifier) {
    val palette = Kairos.colors
    var burst by remember { mutableIntStateOf(0) }
    GlassPanel(modifier.fillMaxWidth(), blur = false, tint = palette.card) {
        Row(Modifier.padding(Space.xl), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(KairosIcons.Leaf, null, tint = palette.success, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("A tiny good thing", style = Kairos.type.label, color = palette.success)
                }
                Spacer(Modifier.height(Space.sm))
                Text(text, style = Kairos.type.title, color = palette.ink)
                Spacer(Modifier.height(Space.lg))
                Box {
                    if (burst > 0) PaintBurst(burst, palette.success, Modifier.size(160.dp).align(Alignment.Center))
                    AnimatedContent(done, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "spark") { isDone ->
                        if (isDone) {
                            Text("Done. A drop went into your jar.", style = Kairos.type.label, color = palette.inkSoft)
                        } else {
                            PrimaryButton("I did it", { burst++; onDone() }, icon = KairosIcons.Check)
                        }
                    }
                }
            }
            Spacer(Modifier.width(Space.lg))
            JoyJar(drops, Modifier.size(width = 74.dp, height = 104.dp))
        }
    }
}

@Composable
private fun CapsuleTeaser(state: TodayState, openCompose: () -> Unit, openCapsuleTab: () -> Unit, modifier: Modifier = Modifier) {
    val palette = Kairos.colors
    val next = state.nextCapsule
    GlassPanel(
        modifier
            .fillMaxWidth()
            .squishClickable(pressedScale = 0.98f) { if (next == null && state.readyCapsules == 0) openCompose() else openCapsuleTab() },
        blur = false,
    ) {
        Row(Modifier.padding(Space.xl), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(52.dp).clip(CircleShape).background(Wash.Rose.color(palette.isDark).copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center,
            ) { Icon(KairosIcons.Capsule, null, tint = palette.ink, modifier = Modifier.size(26.dp)) }
            Spacer(Modifier.width(Space.lg))
            Column(Modifier.weight(1f)) {
                val (title, body) = when {
                    state.readyCapsules > 0 -> "A letter is ready" to "Past you left something. Open it when you're ready."
                    next != null -> "Next letter opens ${Fmt.until(next.opensAt)}" to (next.title.ifBlank { "Sealed ${Fmt.shortDate(next.createdAt)}" })
                    else -> "Write to future you" to "Seal a letter, a photo, a hope. Open it later."
                }
                Text(title, style = Kairos.type.title, color = palette.ink)
                Text(body, style = Kairos.type.label, color = palette.inkSoft, textAlign = TextAlign.Start)
            }
            Icon(KairosIcons.Chevron, null, tint = palette.inkFaint)
        }
    }
}

fun categoryWash(category: String) = when (category) {
    "feelings" -> Wash.Rose
    "mind" -> Wash.Lilac
    "nature" -> Wash.Mint
    "connection" -> Wash.Peach
    "craft" -> Wash.Butter
    "wonder" -> Wash.Cerulean
    else -> Wash.Mint
}

fun themeWash(theme: String) = when (theme) {
    "hope" -> Wash.Butter
    "courage" -> Wash.Peach
    "kindness" -> Wash.Rose
    "growth" -> Wash.Mint
    "calm" -> Wash.Cerulean
    "joy" -> Wash.Butter
    "wonder" -> Wash.Lilac
    "rest" -> Wash.Lilac
    else -> Wash.Cerulean
}

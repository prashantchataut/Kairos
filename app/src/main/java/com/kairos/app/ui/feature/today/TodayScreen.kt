package com.kairos.app.ui.feature.today

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.data.repo.DayStatus
import com.kairos.app.data.repo.Today
import com.kairos.app.data.repo.today
import com.kairos.app.engine.Mood
import com.kairos.app.engine.Topics
import com.kairos.app.ui.components.Card
import com.kairos.app.ui.components.Chip
import com.kairos.app.ui.components.GlassAction
import com.kairos.app.ui.components.GlassTile
import com.kairos.app.ui.components.Icons
import com.kairos.app.ui.components.KairosMark
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.RoundIcon
import com.kairos.app.ui.components.SectionHeader
import com.kairos.app.ui.components.Segmented
import com.kairos.app.ui.components.Shimmer
import com.kairos.app.ui.components.Status
import com.kairos.app.ui.components.pressable
import com.kairos.app.ui.components.SkyTail
import com.kairos.app.ui.components.sky
import com.kairos.app.ui.nav.shared
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
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun TodayScreen(
    openWord: (String) -> Unit,
    openQuote: (String) -> Unit,
    openStudio: (String, String) -> Unit,
    openPractice: () -> Unit,
    openCapsule: () -> Unit,
    openAiSettings: () -> Unit,
    openYou: () -> Unit,
) {
    val vm = appViewModel { TodayViewModel(it) }
    val s by vm.state.collectAsStateWithLifecycle()
    var mode by rememberSaveable { mutableIntStateOf(0) }
    var detailTab by rememberSaveable { mutableIntStateOf(0) }
    val speaker = rememberSpeaker()
    LaunchedEffect(s.feedback) { if (s.feedback != null) { delay(3500); vm.clearFeedback() } }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 120.dp)) {
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .sky()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(bottom = SkyTail)
            ) {
                TopRow(s.name, openYou)
                Spacer(Modifier.height(Gap.lg))
                WeekArc(s.activeDays)
                Spacer(Modifier.height(Gap.xl))
                Segmented(listOf("Word", "Thought"), mode, { mode = it }, Modifier.align(Alignment.CenterHorizontally).width(220.dp), onSky = true)
                Spacer(Modifier.height(Gap.xl))
                Hero(s, mode, openAiSettings, vm::refresh, openWord, openQuote)
                Spacer(Modifier.height(Gap.xl))
                val t = s.today
                if (t != null) {
                    Row(Modifier.padding(horizontal = Gap.gutter), horizontalArrangement = Arrangement.spacedBy(Gap.md)) {
                        if (mode == 0) {
                            GlassAction(Icons.Speaker, "Listen", { speaker.say(t.word.word) }, Modifier.weight(1f))
                            GlassAction(if (t.word.saved) Icons.HeartOn else Icons.Heart, if (t.word.saved) "Saved" else "Save", { vm.toggleWord(t.word.id) }, Modifier.weight(1f), active = t.word.saved)
                            GlassAction(Icons.Share, "Share", { openStudio("word", t.word.id) }, Modifier.weight(1f))
                        } else {
                            GlassAction(if (t.quote.saved) Icons.HeartOn else Icons.Heart, if (t.quote.saved) "Saved" else "Save", { vm.toggleQuote(t.quote.id) }, Modifier.weight(1f), active = t.quote.saved)
                            GlassAction(Icons.Share, "Share", { openStudio("quote", t.quote.id) }, Modifier.weight(1f))
                            GlassAction(Icons.Shuffle, "Another", vm::anotherQuote, Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        s.today?.let { t ->
            item {
                Column(Modifier.padding(horizontal = Gap.gutter)) {
                    SectionHeader(if (mode == 0) "About this word" else "About this thought")
                    Spacer(Modifier.height(Gap.md))
                    if (mode == 0) {
                        Segmented(listOf("Meaning", "In use", "Origin"), detailTab, { detailTab = it }, Modifier.fillMaxWidth())
                        Spacer(Modifier.height(Gap.md))
                        WordDetailCard(t, detailTab, s.feedback, onFeedback = { vm.wordFeedback(t.word.id, it) }, onOpen = { openWord(t.word.id) })
                    } else {
                        QuoteDetailCard(t, s.feedback, onMore = { vm.quoteFeedback(t.quote.id, true) }, onLess = { vm.quoteFeedback(t.quote.id, false) }, onOpen = { openQuote(t.quote.id) })
                    }
                }
            }
            item { MoodCard(s.mood, vm::logMood, Modifier.padding(start = Gap.gutter, end = Gap.gutter, top = Gap.xxl)) }
            if (t.spark.isNotBlank()) item { SparkCard(t.spark, t.sparkDone, vm::completeSpark, Modifier.padding(start = Gap.gutter, end = Gap.gutter, top = Gap.lg)) }
            if (s.due > 0) item { ReviewCard(s.due, openPractice, Modifier.padding(start = Gap.gutter, end = Gap.gutter, top = Gap.lg)) }
        }
        item { CapsuleCard(s, openCapsule, Modifier.padding(start = Gap.gutter, end = Gap.gutter, top = Gap.lg)) }
    }
}

@Composable
private fun TopRow(name: String, openYou: () -> Unit) {
    val p = K.colors
    Row(Modifier.fillMaxWidth().padding(horizontal = Gap.gutter, vertical = Gap.sm), verticalAlignment = Alignment.CenterVertically) {
        KairosMark(22.dp)
        Spacer(Modifier.width(Gap.sm))
        Column(Modifier.weight(1f)) {
            Text(Fmt.greeting(LocalTime.now().hour) + if (name.isNotBlank()) ", $name" else "", style = K.type.label, color = p.onSky)
            Text(LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d")), style = K.type.caption, color = p.onSkySoft)
        }
        Box(
            Modifier
                .size(40.dp)
                .pressable(label = "You", onClick = openYou)
                .clip(CircleShape)
                .background(p.glassOnSky)
                .border(1.dp, p.glassOnSkyRim, CircleShape),
            contentAlignment = Alignment.Center,
        ) { Text(name.take(1).uppercase().ifBlank { "K" }, style = K.type.label, color = p.onSky) }
    }
}

/** Five days on a gentle arc, today at the bottom of the curve — the reference's date strip. */
@Composable
private fun WeekArc(active: Set<Long>) {
    val p = K.colors
    val t = today()
    Row(Modifier.fillMaxWidth().padding(horizontal = Gap.gutter), horizontalArrangement = Arrangement.SpaceBetween) {
        (-2..2).forEach { off ->
            val day = LocalDate.ofEpochDay(t + off)
            val dist = kotlin.math.abs(off)
            val alpha = when (dist) { 0 -> 1f; 1 -> 0.8f; else -> 0.5f }
            Column(Modifier.offset(y = (12 - dist * 10).dp).width(52.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(day.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()), style = K.type.label, color = p.onSky.copy(alpha = alpha * 0.85f))
                Text(day.dayOfMonth.toString(), style = K.type.title.copy(fontSize = 20.sp), color = p.onSky.copy(alpha = alpha))
                Spacer(Modifier.height(4.dp))
                Box(Modifier.size(5.dp).clip(CircleShape).background(if (t + off in active) p.onSky.copy(alpha = alpha) else Color.Transparent))
            }
        }
    }
}

@Composable
private fun Hero(s: TodayState, mode: Int, openAiSettings: () -> Unit, retry: () -> Unit, openWord: (String) -> Unit, openQuote: (String) -> Unit) {
    val p = K.colors
    Box(Modifier.fillMaxWidth().height(232.dp).padding(horizontal = Gap.gutter), contentAlignment = Alignment.Center) {
        val t = s.today
        when {
            t != null -> AnimatedContent(
                mode to t,
                transitionSpec = { (fadeIn(Motion.fade(320)) + slideInVertically(Motion.offset) { it / 8 }) togetherWith (fadeOut(Motion.fade(180)) + slideOutVertically(Motion.offset) { -it / 10 }) },
                label = "hero",
                contentKey = { (m, d) -> "$m-${d.word.id}-${d.quote.id}" },
            ) { (m, d) ->
                if (m == 0) {
                    Column(Modifier.fillMaxWidth().pressable(scale = 0.99f, haptic = false) { openWord(d.word.id) }, horizontalAlignment = Alignment.CenterHorizontally) {
                        val w = d.word.word
                        val size = when { w.length <= 8 -> 72.sp; w.length <= 11 -> 58.sp; w.length <= 14 -> 46.sp; else -> 38.sp }
                        Text(w, style = K.type.hero.copy(fontSize = size, lineHeight = size * 1.05f), color = p.onSky, textAlign = TextAlign.Center, modifier = Modifier.shared("word-${d.word.id}"))
                        Spacer(Modifier.height(Gap.sm))
                        Text("${d.word.pronunciation}  ·  ${d.word.partOfSpeech}", style = K.type.label, color = p.onSkySoft)
                        Spacer(Modifier.height(Gap.lg))
                        Text(d.word.definition, style = K.type.body.copy(fontSize = 18.sp, lineHeight = 26.sp), color = p.onSky, textAlign = TextAlign.Center, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    }
                } else {
                    Column(Modifier.fillMaxWidth().pressable(scale = 0.99f, haptic = false) { openQuote(d.quote.id) }, horizontalAlignment = Alignment.CenterHorizontally) {
                        val len = d.quote.text.length
                        val size = when { len < 70 -> 30.sp; len < 130 -> 25.sp; else -> 21.sp }
                        Text("“${d.quote.text}”", style = K.type.quote.copy(fontSize = size, lineHeight = size * 1.25f), color = p.onSky, textAlign = TextAlign.Center, maxLines = 7, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(Gap.lg))
                        Text(d.quote.author, style = K.type.label, color = p.onSkySoft)
                    }
                }
            }
            s.status is DayStatus.NeedsKey -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                KairosMark(44.dp)
                Spacer(Modifier.height(Gap.lg))
                Text("Your curator is waiting", style = K.type.serifLarge.copy(fontSize = 36.sp), color = p.onSky, textAlign = TextAlign.Center)
                Spacer(Modifier.height(Gap.sm))
                Text("Connect an OpenRouter key and Kairos will start writing words and thoughts just for you.", style = K.type.body, color = p.onSkySoft, textAlign = TextAlign.Center)
                Spacer(Modifier.height(Gap.lg))
                PrimaryButton("Connect OpenRouter", openAiSettings, icon = Icons.Key, dark = true)
            }
            s.status is DayStatus.Failed -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Today is still being written", style = K.type.serifLarge.copy(fontSize = 34.sp), color = p.onSky, textAlign = TextAlign.Center)
                Spacer(Modifier.height(Gap.sm))
                Text((s.status as DayStatus.Failed).message, style = K.type.body, color = p.onSkySoft, textAlign = TextAlign.Center)
                Spacer(Modifier.height(Gap.lg))
                Row(horizontalArrangement = Arrangement.spacedBy(Gap.sm)) {
                    PrimaryButton("Try again", retry, icon = Icons.Shuffle, dark = true)
                    com.kairos.app.ui.components.SecondaryButton("Settings", openAiSettings, onSky = true)
                }
            }
            else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Shimmer(Modifier.size(width = 220.dp, height = 64.dp), onSky = true)
                Spacer(Modifier.height(Gap.md))
                Shimmer(Modifier.size(width = 140.dp, height = 16.dp), onSky = true)
                Spacer(Modifier.height(Gap.xl))
                Text("Writing today's word for you…", style = K.type.label, color = p.onSkySoft)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WordDetailCard(t: Today, tab: Int, feedback: String?, onFeedback: (WordFeedback) -> Unit, onOpen: () -> Unit) {
    val p = K.colors
    val w = t.word
    Card(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(w.word, style = K.type.title, color = p.ink, modifier = Modifier.weight(1f))
            Text("${Topics.wordLabel(w.category)}, ${Topics.levels[(w.level - 1).coerceIn(0, 4)].lowercase()}", style = K.type.caption, color = p.inkFaint)
        }
        Spacer(Modifier.height(Gap.md))
        AnimatedContent(tab, transitionSpec = { fadeIn(Motion.fade()) togetherWith fadeOut(Motion.fade(120)) }, label = "wordTab") { tb ->
            Column {
                when (tb) {
                    0 -> {
                        Text(w.definition, style = K.type.body.copy(fontSize = 18.sp, lineHeight = 27.sp), color = p.ink)
                        if (w.synonymList.isNotEmpty()) {
                            Spacer(Modifier.height(Gap.md))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(Gap.sm), verticalArrangement = Arrangement.spacedBy(Gap.sm)) {
                                w.synonymList.forEach { Text(it, style = K.type.caption, color = p.inkSoft, modifier = Modifier.clip(CircleShape).background(p.surfaceSoft).padding(horizontal = 10.dp, vertical = 5.dp)) }
                            }
                        }
                    }
                    1 -> {
                        Text("“${w.example}”", style = K.type.quote.copy(fontSize = 22.sp, lineHeight = 29.sp), color = p.ink)
                        Spacer(Modifier.height(Gap.md))
                        Text("Try using it in one message today. Words stick once they leave the app.", style = K.type.label, color = p.inkSoft)
                    }
                    else -> Text(w.origin.ifBlank { "No origin notes for this one." }, style = K.type.body, color = p.ink)
                }
            }
        }
        Spacer(Modifier.height(Gap.lg))
        Status(t.wordReason, p.positive)
        Spacer(Modifier.height(Gap.lg))
        Box(Modifier.fillMaxWidth().height(1.dp).background(p.line))
        Spacer(Modifier.height(Gap.md))
        AnimatedContent(feedback, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "fb") { f ->
            if (f != null) Text(f, style = K.type.label, color = p.accent, modifier = Modifier.padding(vertical = Gap.sm))
            else Column {
                Text("Teach your curator", style = K.type.caption, color = p.inkFaint)
                Spacer(Modifier.height(Gap.sm))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Gap.sm), verticalArrangement = Arrangement.spacedBy(Gap.sm)) {
                    WordFeedback.entries.forEach { fb -> Chip(fb.label, false, { onFeedback(fb) }) }
                }
            }
        }
        Spacer(Modifier.height(Gap.sm))
        Text("See everything about ${w.word}", style = K.type.label, color = p.accent, modifier = Modifier.pressable(onClick = onOpen).padding(vertical = Gap.sm))
    }
}

@Composable
private fun QuoteDetailCard(t: Today, feedback: String?, onMore: () -> Unit, onLess: () -> Unit, onOpen: () -> Unit) {
    val p = K.colors
    Card(Modifier.fillMaxWidth(), onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(t.quote.author, style = K.type.title, color = p.ink, modifier = Modifier.weight(1f))
            Text(Topics.themeLabel(t.quote.theme), style = K.type.caption, color = p.inkFaint)
        }
        if (t.quote.note.isNotBlank()) {
            Spacer(Modifier.height(Gap.sm))
            Text(t.quote.note, style = K.type.body, color = p.inkSoft)
        }
        Spacer(Modifier.height(Gap.lg))
        Status(t.quoteReason, p.positive)
        Spacer(Modifier.height(Gap.lg))
        AnimatedContent(feedback, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "qfb") { f ->
            if (f != null) Text(f, style = K.type.label, color = p.accent)
            else Row(horizontalArrangement = Arrangement.spacedBy(Gap.sm)) {
                Chip("More like this", false, onMore, leading = Icons.ThumbUp)
                Chip("Not for me", false, onLess, leading = Icons.ThumbDown)
            }
        }
    }
}

private val moodTints = mapOf(Mood.Radiant to Tint.Butter, Mood.Good to Tint.Mint, Mood.Okay to Tint.Sky, Mood.Low to Tint.Lavender, Mood.Rough to Tint.Rose)
fun Mood.tint() = moodTints.getValue(this)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MoodCard(mood: Mood?, onPick: (Mood) -> Unit, modifier: Modifier = Modifier) {
    val p = K.colors
    Card(modifier.fillMaxWidth()) {
        Text("How are you feeling?", style = K.type.title, color = p.ink)
        Spacer(Modifier.height(Gap.xs))
        AnimatedContent(mood, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "moodReply") { m ->
            Text(m?.reply ?: "Your answer quietly steers today's thought.", style = K.type.label, color = p.inkSoft)
        }
        Spacer(Modifier.height(Gap.lg))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Gap.sm), verticalArrangement = Arrangement.spacedBy(Gap.sm)) {
            Mood.entries.forEach { m ->
                val on = m == mood
                Row(
                    Modifier
                        .pressable { onPick(m) }
                        .clip(CircleShape)
                        .background(if (on) p.tint(m.tint()) else p.surfaceSoft)
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(p.tintInk(m.tint())))
                    Spacer(Modifier.width(7.dp))
                    Text(m.label, style = K.type.label, color = if (on) p.tintInk(m.tint()) else p.ink)
                }
            }
        }
    }
}

@Composable
private fun SparkCard(text: String, done: Boolean, onDone: () -> Unit, modifier: Modifier = Modifier) {
    val p = K.colors
    Card(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("A small good thing", style = K.type.caption, color = p.positive)
                Spacer(Modifier.height(Gap.xs))
                Text(text, style = K.type.title, color = if (done) p.inkFaint else p.ink)
            }
            Spacer(Modifier.width(Gap.lg))
            val bg = if (done) p.positive else p.surface
            Box(
                Modifier
                    .size(44.dp)
                    .pressable(enabled = !done, label = "Mark done", onClick = onDone)
                    .clip(CircleShape)
                    .background(bg)
                    .border(1.5.dp, if (done) p.positive else p.line, CircleShape),
                contentAlignment = Alignment.Center,
            ) { androidx.compose.animation.AnimatedVisibility(done, enter = fadeIn() + androidx.compose.animation.scaleIn(Motion.soft())) { Icon(Icons.Check, null, tint = Color.White, modifier = Modifier.size(22.dp)) } }
        }
    }
}

@Composable
private fun ReviewCard(due: Int, onStart: () -> Unit, modifier: Modifier = Modifier) {
    val p = K.colors
    Card(modifier.fillMaxWidth(), onClick = onStart) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Words to revisit", style = K.type.title, color = p.ink)
                Text("Right before you'd forget them", style = K.type.caption, color = p.inkSoft)
            }
            Text("$due", style = K.type.serifLarge, color = p.ink)
        }
        Spacer(Modifier.height(Gap.md))
        com.kairos.app.ui.components.Progress(0.15f + 0.85f * (1f / (1 + due)), Modifier.fillMaxWidth())
    }
}

@Composable
private fun CapsuleCard(s: TodayState, open: () -> Unit, modifier: Modifier = Modifier) {
    val p = K.colors
    val next = s.nextCapsule
    Card(modifier.fillMaxWidth(), onClick = open) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(Radius.chip).background(p.accentSoft), contentAlignment = Alignment.Center) { Icon(Icons.Capsule, null, tint = p.accent, modifier = Modifier.size(24.dp)) }
            Spacer(Modifier.width(Gap.lg))
            Column(Modifier.weight(1f)) {
                val (title, body) = when {
                    s.readyCapsules > 0 -> "A letter is ready to open" to "Past you left something for today."
                    next != null -> "Next letter opens ${Fmt.until(next.opensAt)}" to next.title.ifBlank { "Sealed ${Fmt.shortDate(next.createdAt)}" }
                    else -> "Write to future you" to "Seal a letter, a photo or a voice note."
                }
                Text(title, style = K.type.label.copy(fontSize = 16.sp), color = p.ink)
                Text(body, style = K.type.caption, color = p.inkSoft, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.Chevron, null, tint = p.inkFaint, modifier = Modifier.size(18.dp))
        }
    }
}


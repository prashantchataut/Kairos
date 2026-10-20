package com.kairos.app.ui.screens.today

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kairos.app.AppContainer
import com.kairos.app.data.TodayPicks
import com.kairos.app.data.content.Joy
import com.kairos.app.data.db.Capsule
import com.kairos.app.data.prefs.Settings
import com.kairos.app.data.today
import com.kairos.app.engine.SpacedRepetition
import com.kairos.app.ui.kit.AcrylicCard
import com.kairos.app.ui.kit.CornerWash
import com.kairos.app.ui.kit.GlassIconButton
import com.kairos.app.ui.kit.InkButton
import com.kairos.app.ui.kit.JoyJar
import com.kairos.app.ui.kit.Pip
import com.kairos.app.ui.kit.PipMood
import com.kairos.app.ui.kit.SoftButton
import com.kairos.app.ui.kit.SoftChip
import com.kairos.app.ui.kit.Tag
import com.kairos.app.ui.kit.kairosViewModel
import com.kairos.app.ui.kit.rememberSpeaker
import com.kairos.app.ui.kit.rise
import com.kairos.app.ui.kit.shareText
import com.kairos.app.ui.kit.squishClick
import com.kairos.app.ui.kit.tabContentPadding
import com.kairos.app.ui.kit.wash
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Motion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

data class TodayUi(
    val picks: TodayPicks? = null,
    val mood: PipMood? = null,
    val week: Map<Long, PipMood> = emptyMap(),
    val wordSaved: Boolean = false,
    val quoteSaved: Boolean = false,
    val joyCount: Int = 0,
    val didJoyToday: Boolean = false,
    val nextCapsule: Capsule? = null,
    val readyCapsule: Capsule? = null,
    val due: Int = 0,
    val glowDays: Int = 0,
)

class TodayViewModel(private val c: AppContainer) : ViewModel() {
    private val picks = MutableStateFlow<TodayPicks?>(null)
    private val glow = MutableStateFlow(0)
    private var shuffles = 0

    private val day = today()

    private val moods = combine(c.learn.observeMood(day), c.learn.observeWeekMoods(day)) { m, week ->
        m?.let { runCatching { PipMood.valueOf(it.mood) }.getOrNull() } to
            week.mapNotNull { e -> runCatching { PipMood.valueOf(e.mood) }.getOrNull()?.let { e.epochDay to it } }.toMap()
    }

    private val saved = combine(c.learn.wordProgress, c.learn.quoteStates, picks) { w, q, p ->
        Triple(
            w.firstOrNull { it.wordId == p?.word?.id }?.saved == true,
            q.firstOrNull { it.quoteId == p?.quote?.id }?.saved == true,
            w.count { SpacedRepetition.isDue(it, day) },
        )
    }

    private val capsules = c.capsules.capsules.map { list ->
        val now = System.currentTimeMillis()
        list.firstOrNull { it.openedAt == null && it.unlockAt > now } to list.firstOrNull { it.openedAt == null && it.unlockAt <= now }
    }

    private val joys = combine(c.learn.observeJoyCount(), c.learn.observeJoysToday(day)) { n, todays -> n to todays.isNotEmpty() }

    val ui = combine(picks, moods, saved, capsules, combine(joys, glow) { a, b -> a to b }) { p, (mood, week), (ws, qs, due), (next, ready), (j, g) ->
        TodayUi(p, mood, week, ws, qs, j.first, j.second, next, ready, due, g)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodayUi())

    init {
        viewModelScope.launch {
            picks.value = c.learn.todayPicks(day)
            glow.value = c.learn.glowDays(day)
        }
    }

    fun setMood(m: PipMood) = viewModelScope.launch {
        c.learn.setMood(m.name, day)
        glow.value = c.learn.glowDays(day)
    }

    fun toggleWord() = viewModelScope.launch { picks.value?.word?.id?.let { c.learn.toggleWordSaved(it) } }
    fun toggleQuote() = viewModelScope.launch { picks.value?.quote?.id?.let { c.learn.toggleQuoteSaved(it) } }

    fun didJoy() = viewModelScope.launch {
        picks.value?.joy?.id?.let { c.learn.logJoy(it, day) }
        glow.value = c.learn.glowDays(day)
    }

    fun shuffleJoy(mood: PipMood?) = viewModelScope.launch {
        val joy: Joy = c.learn.shuffleJoy(mood?.name, ++shuffles, day)
        picks.value = picks.value?.copy(joy = joy)
    }
}

private fun greeting(): String = when (LocalTime.now().hour) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    in 17..21 -> "Good evening"
    else -> "Hello, night owl"
}

@Composable
fun TodayScreen(
    settings: Settings,
    onOpenWord: (String) -> Unit,
    onOpenCapsule: (Long) -> Unit,
    onNewCapsule: () -> Unit,
    onPractice: () -> Unit,
    onSettings: () -> Unit,
    onPlay: (String) -> Unit,
) {
    val vm = kairosViewModel { TodayViewModel(it) }
    val ui by vm.ui.collectAsState()
    val k = Kairos.colors
    var poke by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = tabContentPadding(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth().rise(0), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM")),
                        style = MaterialTheme.typography.labelLarge,
                        color = k.inkSoft,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        if (settings.name.isBlank()) "${greeting()}!" else "${greeting()},\n${settings.name}",
                        style = MaterialTheme.typography.headlineLarge,
                        color = k.ink,
                    )
                }
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(k.washes.peach.copy(alpha = 0.7f))
                        .squishClick(onClick = onSettings),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        settings.name.firstOrNull()?.uppercase() ?: "☺",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF1D2640),
                    )
                }
            }
        }

        item { WeekStrip(ui.week, Modifier.rise(1)) }

        item {
            MoodCard(
                mood = ui.mood,
                poke = poke,
                glowDays = ui.glowDays,
                onPoke = { poke++ },
                onMood = { vm.setMood(it); poke++ },
                modifier = Modifier.rise(2),
            )
        }

        ui.readyCapsule?.let { ready ->
            item { CapsuleReadyBanner(ready, onOpen = { onOpenCapsule(ready.id) }, modifier = Modifier.rise(3)) }
        }

        ui.picks?.let { p ->
            item {
                WordOfDay(
                    picks = p,
                    saved = ui.wordSaved,
                    onSave = vm::toggleWord,
                    onOpen = { onOpenWord(p.word.id) },
                    modifier = Modifier.rise(3),
                )
            }
            item { ThoughtOfDay(p, ui.quoteSaved, onSave = vm::toggleQuote, modifier = Modifier.rise(4)) }
            item {
                SmallJoy(
                    joy = p.joy,
                    done = ui.didJoyToday,
                    count = ui.joyCount,
                    onDone = { vm.didJoy(); poke++ },
                    onShuffle = { vm.shuffleJoy(ui.mood) },
                    modifier = Modifier.rise(5),
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.rise(6)) {
                MiniTile(
                    title = if (ui.due > 0) "${ui.due} to water" else "Practice",
                    body = if (ui.due > 0) "Words ready for review" else "A few fresh words",
                    color = k.washes.mint,
                    icon = Icons.Rounded.Spa,
                    onClick = onPractice,
                    modifier = Modifier.weight(1f),
                )
                val next = ui.nextCapsule
                MiniTile(
                    title = if (next != null) "Sealed" else "Future you",
                    body = if (next != null) "Opens in ${daysUntil(next.unlockAt)}" else "Write a letter",
                    color = k.washes.lilac,
                    icon = Icons.Rounded.Lock,
                    onClick = { if (next != null) onOpenCapsule(next.id) else onNewCapsule() },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            AcrylicCard(Modifier.fillMaxWidth().rise(7).squishClick { onPlay("word-bloom") }, tint = k.washes.sky) {
                CornerWash(k.washes.mint, Modifier.matchParentSize(), seed = 9, at = Offset(0.95f, 0.9f), scale = 0.5f)
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Today's Word Bloom", style = MaterialTheme.typography.titleLarge, color = k.ink)
                        Text("Six tries. One five-letter word.", style = MaterialTheme.typography.bodyMedium, color = k.inkSoft)
                    }
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = k.ink)
                }
            }
        }
    }
}

fun daysUntil(at: Long): String {
    val ms = at - System.currentTimeMillis()
    val d = (ms / 86_400_000L)
    return when {
        ms <= 0 -> "now"
        d >= 365 -> "${d / 365} yr ${((d % 365) / 30).let { if (it > 0) "$it mo" else "" }}".trim()
        d >= 60 -> "${d / 30} months"
        d >= 1 -> if (d == 1L) "1 day" else "$d days"
        else -> "${(ms / 3_600_000L).coerceAtLeast(1)} h"
    }
}

/** Seven days, each a little mood blob — a week you can read at a glance (ref: shape-faces). */
@Composable
private fun WeekStrip(week: Map<Long, PipMood>, modifier: Modifier = Modifier) {
    val k = Kairos.colors
    val today = today()
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        for (d in today - 6..today) {
            val date = LocalDate.ofEpochDay(d)
            val mood = week[d]
            val isToday = d == today
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isToday) k.ink else k.inkFaint,
                )
                Spacer(Modifier.height(6.dp))
                Box(Modifier.size(30.dp), contentAlignment = Alignment.Center) {
                    if (mood != null) {
                        Pip(mood, size = 34.dp)
                    } else {
                        Canvas(Modifier.size(14.dp)) {
                            drawCircle(k.ink.copy(alpha = if (isToday) 0.25f else 0.1f))
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isToday) k.lagoon else k.inkSoft,
                )
            }
        }
    }
}

@Composable
private fun MoodCard(
    mood: PipMood?,
    poke: Int,
    glowDays: Int,
    onPoke: () -> Unit,
    onMood: (PipMood) -> Unit,
    modifier: Modifier = Modifier,
) {
    val k = Kairos.colors
    val options = listOf(PipMood.Glowing, PipMood.Happy, PipMood.Calm, PipMood.Curious, PipMood.Meh, PipMood.Sleepy, PipMood.Low)
    AcrylicCard(modifier.fillMaxWidth(), tint = (mood ?: PipMood.Happy).wash(k.washes)) {
        CornerWash((mood ?: PipMood.Happy).wash(k.washes), Modifier.matchParentSize(), seed = 21, at = Offset(0.5f, 0.32f), scale = 0.55f)
        Column(Modifier.padding(vertical = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.squishClick(pressedScale = 0.92f, onClick = onPoke)) {
                Pip(mood ?: PipMood.Happy, size = 150.dp, poke = poke)
            }
            AnimatedContent(
                targetState = mood,
                transitionSpec = {
                    (fadeIn(Motion.enter(420)) + slideInVertically(Motion.enter(420)) { it / 3 }) togetherWith fadeOut(Motion.exit())
                },
                label = "moodLine",
            ) { m ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        if (m == null) "How are you feeling?" else m.line,
                        style = MaterialTheme.typography.headlineSmall,
                        color = k.ink,
                        textAlign = TextAlign.Center,
                    )
                    if (m != null && glowDays > 1) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "$glowDays days of checking in with yourself",
                            style = MaterialTheme.typography.bodySmall,
                            color = k.inkSoft,
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            ) {
                items(options) { o ->
                    SoftChip(
                        o.label,
                        selected = o == mood,
                        onClick = { onMood(o) },
                        selectedColor = o.wash(k.washes),
                    )
                }
            }
        }
    }
}

@Composable
private fun WordOfDay(picks: TodayPicks, saved: Boolean, onSave: () -> Unit, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val k = Kairos.colors
    val w = picks.word
    val speaker = rememberSpeaker()
    AcrylicCard(modifier.fillMaxWidth().squishClick(pressedScale = 0.98f, onClick = onOpen)) {
        CornerWash(k.washes.mint, Modifier.matchParentSize(), seed = 5)
        Column(Modifier.padding(22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Word of the day", style = MaterialTheme.typography.labelLarge, color = k.inkSoft, modifier = Modifier.weight(1f))
                HeartButton(saved, onSave)
            }
            Text(w.word, style = MaterialTheme.typography.displayMedium, color = k.ink)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("/${w.phonetic}/", style = MaterialTheme.typography.bodyMedium, color = k.inkSoft)
                Spacer(Modifier.width(4.dp))
                Box(
                    Modifier.size(40.dp).clip(CircleShape).squishClick(pressedScale = 0.85f) { speaker.say(w.word) },
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.AutoMirrored.Rounded.VolumeUp, "Hear it", tint = k.lagoon, modifier = Modifier.size(20.dp)) }
                Spacer(Modifier.width(4.dp))
                Tag(w.pos, color = k.washes.mint)
            }
            Spacer(Modifier.height(8.dp))
            Text(w.definition, style = MaterialTheme.typography.bodyLarge, color = k.ink)
            Spacer(Modifier.height(8.dp))
            Text("“${w.example}”", style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic), color = k.inkSoft)
            Spacer(Modifier.height(14.dp))
            Text(picks.wordWhy, style = MaterialTheme.typography.labelMedium, color = k.lagoon)
        }
    }
}

@Composable
private fun ThoughtOfDay(picks: TodayPicks, saved: Boolean, onSave: () -> Unit, modifier: Modifier = Modifier) {
    val k = Kairos.colors
    val context = LocalContext.current
    val q = picks.quote
    AcrylicCard(modifier.fillMaxWidth(), tint = k.washes.lilac) {
        CornerWash(k.washes.rose, Modifier.matchParentSize(), seed = 14, at = Offset(0.1f, 0.95f), scale = 0.6f)
        Column(Modifier.padding(22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Thought of the day", style = MaterialTheme.typography.labelLarge, color = k.inkSoft, modifier = Modifier.weight(1f))
                Box(
                    Modifier.size(44.dp).clip(CircleShape).squishClick(pressedScale = 0.85f) {
                        shareText(context, "“${q.text}” — ${q.author}\n\nfrom Kairos")
                    },
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Rounded.IosShare, "Share", tint = k.ink, modifier = Modifier.size(20.dp)) }
                HeartButton(saved, onSave)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                q.text,
                style = if (q.text.length < 90) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge,
                color = k.ink,
            )
            Spacer(Modifier.height(12.dp))
            Text("— ${q.author}", style = MaterialTheme.typography.titleSmall, color = k.inkSoft)
            Spacer(Modifier.height(10.dp))
            Text(picks.quoteWhy, style = MaterialTheme.typography.labelMedium, color = k.lagoon)
        }
    }
}

@Composable
fun HeartButton(saved: Boolean, onToggle: () -> Unit) {
    val k = Kairos.colors
    val pop by animateFloatAsState(if (saved) 1f else 0f, Motion.bouncy(), label = "heart")
    Box(
        Modifier.size(44.dp).clip(CircleShape).squishClick(pressedScale = 0.8f, onClick = onToggle),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (saved) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            contentDescription = if (saved) "Saved" else "Save",
            tint = if (saved) k.washes.rose.copy(red = 0.95f) else k.ink,
            modifier = Modifier.size(22.dp).graphicsLayer {
                val s = 1f + 0.25f * (pop * (1f - pop)) * 4f
                scaleX = s; scaleY = s
            },
        )
    }
}

@Composable
private fun SmallJoy(
    joy: Joy,
    done: Boolean,
    count: Int,
    onDone: () -> Unit,
    onShuffle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val k = Kairos.colors
    AcrylicCard(modifier.fillMaxWidth(), tint = k.washes.butter) {
        Row(Modifier.padding(20.dp)) {
            Column(Modifier.weight(1f)) {
                Text("A small joy", style = MaterialTheme.typography.labelLarge, color = k.inkSoft)
                Spacer(Modifier.height(6.dp))
                AnimatedContent(
                    targetState = joy,
                    transitionSpec = { (fadeIn(Motion.enter(380)) + slideInVertically(Motion.enter(420)) { it / 4 }) togetherWith (fadeOut(Motion.exit()) + slideOutVertically { -it / 6 }) },
                    label = "joy",
                ) { j ->
                    Column {
                        Text(j.title, style = MaterialTheme.typography.titleLarge, color = k.ink)
                        Spacer(Modifier.height(4.dp))
                        Text("${j.detail} · ${j.minutes} min", style = MaterialTheme.typography.bodyMedium, color = k.inkSoft)
                    }
                }
                Spacer(Modifier.height(16.dp))
                AnimatedVisibility(visible = !done, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        InkButton("I did it", onDone, icon = Icons.Rounded.Check)
                        SoftButton("Another", onShuffle, icon = Icons.Rounded.Autorenew)
                    }
                }
                AnimatedVisibility(visible = done, enter = fadeIn(Motion.enter()) + expandVertically(), exit = fadeOut()) {
                    Column {
                        Text("A petal for the jar. Lovely.", style = MaterialTheme.typography.titleSmall, color = k.ink)
                        Spacer(Modifier.height(8.dp))
                        SoftButton("One more idea", onShuffle, icon = Icons.Rounded.Autorenew)
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                JoyJar(count, Modifier.size(width = 76.dp, height = 104.dp))
                Spacer(Modifier.height(4.dp))
                Text("$count petals", style = MaterialTheme.typography.labelSmall, color = k.inkSoft, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun MiniTile(
    title: String,
    body: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val k = Kairos.colors
    AcrylicCard(modifier.squishClick(onClick = onClick), tint = color) {
        CornerWash(color, Modifier.matchParentSize(), seed = title.length, at = Offset(0.9f, 0.1f), scale = 0.55f)
        Column(Modifier.padding(18.dp)) {
            Icon(icon, null, tint = k.ink, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(18.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, color = k.ink, maxLines = 1)
            Text(body, style = MaterialTheme.typography.bodySmall, color = k.inkSoft, maxLines = 2)
        }
    }
}

@Composable
private fun CapsuleReadyBanner(c: Capsule, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val k = Kairos.colors
    var wiggle by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { wiggle++ }
    AcrylicCard(modifier.fillMaxWidth().squishClick(onClick = onOpen), tint = k.washes.peach) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Pip(PipMood.Glowing, size = 64.dp, poke = wiggle)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("A capsule is ready!", style = MaterialTheme.typography.titleLarge, color = k.ink)
                Text("“${c.title}” — from past you", style = MaterialTheme.typography.bodyMedium, color = k.inkSoft, maxLines = 1)
            }
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = k.ink)
        }
    }
}

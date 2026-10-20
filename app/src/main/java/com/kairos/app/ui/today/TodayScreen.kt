package com.kairos.app.ui.today

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.kairos.app.AppContainer
import com.kairos.app.engine.PondState
import com.kairos.app.engine.Today
import com.kairos.app.engine.content.Kindness
import com.kairos.app.engine.daily.Mood
import com.kairos.app.engine.todayEpoch
import com.kairos.app.ui.art.CritterView
import com.kairos.app.ui.art.Kiri
import com.kairos.app.ui.art.moodColor
import com.kairos.app.ui.components.DewBadge
import com.kairos.app.ui.components.Eyebrow
import com.kairos.app.ui.components.HeartButton
import com.kairos.app.ui.components.PillLabel
import com.kairos.app.ui.components.ProgressRing
import com.kairos.app.ui.components.ScreenHeader
import com.kairos.app.ui.components.SectionTitle
import com.kairos.app.ui.components.clickableNoRipple
import com.kairos.app.ui.components.enter
import com.kairos.app.ui.components.kairosViewModel
import com.kairos.app.ui.components.screenPadding
import com.kairos.app.ui.components.shareText
import com.kairos.app.ui.glass.ButtonTone
import com.kairos.app.ui.glass.GlassCard
import com.kairos.app.ui.glass.GlassIconButton
import com.kairos.app.ui.glass.GlassWeight
import com.kairos.app.ui.glass.KButton
import com.kairos.app.ui.glass.liquidGlass
import com.kairos.app.ui.glass.pressScale
import com.kairos.app.ui.nav.LocalNavigator
import com.kairos.app.ui.nav.Routes
import com.kairos.app.ui.nav.Tab
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Motion
import com.kairos.app.ui.theme.Radii
import com.kairos.app.ui.theme.Space
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class TodayUi(
    val today: Today? = null,
    val mood: Mood? = null,
    val wordSaved: Boolean = false,
    val wordInDeck: Boolean = false,
    val quoteSaved: Boolean = false,
    val pond: PondState = PondState(0, emptyList()),
    val name: String = "",
    val readyCapsules: Int = 0,
    val sealedCapsules: Int = 0,
    val due: Int = 0,
    val kindness: List<Kindness> = emptyList(),
    val kindnessDone: Set<String> = emptySet(),
)

class TodayViewModel(private val c: AppContainer) : ViewModel() {
    private val base = combine(
        c.today.observe(todayEpoch()),
        c.moods.observeToday(),
        c.db.words().observeAll(),
        c.db.quotes().observeAll(),
        c.settings.settings,
    ) { today, mood, words, quotes, settings ->
        val wp = today?.let { t -> words.firstOrNull { it.wordId == t.word.item.id } }
        TodayUi(
            today = today, mood = mood,
            wordSaved = wp?.saved == true, wordInDeck = wp?.inDeck == true,
            quoteSaved = today?.let { t -> quotes.firstOrNull { it.quoteId == t.quote.item.id }?.saved } == true,
            name = settings.name,
        )
    }

    val state: StateFlow<TodayUi> = combine(
        base,
        c.pond.observe(),
        c.capsules.observeAll(),
        c.learn.observeDueCount(),
        combine(flow { emit(c.kindness.deck()) }, c.kindness.observeDoneToday()) { a, b -> a to b },
    ) { ui, pond, capsules, due, kind ->
        val now = System.currentTimeMillis()
        ui.copy(
            pond = pond,
            readyCapsules = capsules.count { it.openedAt == null && it.unlockAt <= now },
            sealedCapsules = capsules.count { it.openedAt == null && it.unlockAt > now },
            due = due,
            kindness = kind.first,
            kindnessDone = kind.second,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodayUi())

    init {
        viewModelScope.launch {
            val t = c.today.ensureToday()
            c.learn.markSeen(t.word.item.id)
            c.quotes.markShown(t.quote.item.id, t.day)
        }
    }

    fun checkIn(m: Mood) = viewModelScope.launch { c.moods.checkIn(m) }
    fun toggleWord() = viewModelScope.launch { state.value.today?.let { c.learn.toggleSave(it.word.item.id) } }
    fun toggleQuote() = viewModelScope.launch { state.value.today?.let { c.quotes.toggleSave(it.quote.item.id) } }
    fun reroll(word: Boolean) = viewModelScope.launch { c.today.reroll(word) }
    fun meet() = viewModelScope.launch { state.value.today?.let { c.learn.meetTodayWord(it.word.item.id) } }
    fun speak(text: String) = c.speaker.say(text)
}

@Composable
fun TodayScreen() {
    val vm = kairosViewModel { TodayViewModel(it) }
    val ui by vm.state.collectAsStateWithLifecycle()
    val nav = LocalNavigator.current
    val context = LocalContext.current

    LazyColumn(Modifier.fillMaxSize(), contentPadding = screenPadding()) {
        item("header") {
            ScreenHeader(
                eyebrow = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE · d MMMM", Locale.getDefault())),
                title = greeting(ui.name),
                trailing = { DewBadge(ui.pond.dew, onClick = { nav.open(Routes.PONDEX) }) },
            )
        }
        item("mood") { MoodCard(ui.mood, vm::checkIn, Modifier.enter(0)) }
        item("word") {
            ui.today?.let { t ->
                WordOfDayCard(
                    t, ui.wordSaved, ui.wordInDeck,
                    onOpen = { nav.open(Routes.word(t.word.item.id)) },
                    onSave = vm::toggleWord,
                    onPractice = { if (ui.wordInDeck) nav.open(Routes.practice(listOf(t.word.item.id))) else vm.meet() },
                    onReroll = { vm.reroll(true) },
                    onSpeak = { vm.speak(t.word.item.word) },
                    onShare = { shareText(context, "${t.word.item.word} (${t.word.item.pos}) — ${t.word.item.definition}\n\nvia Kairos") },
                    modifier = Modifier.padding(top = Space.lg).enter(1),
                )
            } ?: Box(Modifier.fillMaxWidth().height(300.dp))
        }
        item("quote") {
            ui.today?.let { t ->
                ThoughtCard(
                    t, ui.quoteSaved,
                    onSave = vm::toggleQuote,
                    onReroll = { vm.reroll(false) },
                    onShare = { shareText(context, "“${t.quote.item.text}” — ${t.quote.item.author}\n\nvia Kairos") },
                    modifier = Modifier.padding(top = Space.lg).enter(2),
                )
            }
        }
        item("kind") {
            SectionTitle("Something kind for you")
            KindnessStack(ui.kindness, ui.kindnessDone, onOpen = { nav.open(Routes.KINDNESS) }, modifier = Modifier.enter(3))
        }
        item("tiles") {
            SectionTitle("Little doors")
            Row(horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                DoorTile(
                    Icons.Rounded.Spa, "Breathe", "One slow minute", Kairos.colors.mint, Modifier.weight(1f),
                ) { nav.open(Routes.BREATHE) }
                DoorTile(
                    Icons.Rounded.MailOutline, "Capsule",
                    when {
                        ui.readyCapsules > 0 -> "${ui.readyCapsules} letter${if (ui.readyCapsules > 1) "s" else ""} ready!"
                        ui.sealedCapsules > 0 -> "${ui.sealedCapsules} sealed, waiting"
                        else -> "Write to future you"
                    },
                    Kairos.colors.lilac, Modifier.weight(1f), highlight = ui.readyCapsules > 0,
                ) { nav.tab(Tab.Capsule) }
            }
            Spacer(Modifier.height(Space.md))
            Row(horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                DoorTile(
                    Icons.Rounded.School, "Practice", if (ui.due > 0) "${ui.due} word${if (ui.due > 1) "s" else ""} to revisit" else "A quick round", Kairos.colors.sky, Modifier.weight(1f),
                ) { nav.open(Routes.practice()) }
                DoorTile(Icons.Rounded.SportsEsports, "Arcade", "Play something gentle", Kairos.colors.butter, Modifier.weight(1f)) { nav.tab(Tab.Play) }
            }
        }
        item("buddy") {
            SectionTitle("Your pond")
            BuddyCard(ui.pond) { nav.open(Routes.PONDEX) }
        }
    }
}

private fun greeting(name: String): String {
    val h = LocalTime.now().hour
    val part = when (h) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Hello, night owl"
    }
    return if (name.isBlank()) part else "$part,\n$name"
}

@Composable
private fun MoodCard(mood: Mood?, onPick: (Mood) -> Unit, modifier: Modifier = Modifier) {
    val colors = Kairos.colors
    GlassCard(modifier.fillMaxWidth(), blur = true, weight = GlassWeight.Regular, shape = Radii.sheet) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Kiri(mood, size = 104.dp)
                Spacer(Modifier.width(Space.md))
                Column(Modifier.weight(1f)) {
                    Eyebrow(if (mood == null) "Check in" else "Kiri hears you")
                    Spacer(Modifier.height(4.dp))
                    AnimatedContent(
                        mood,
                        transitionSpec = { fadeIn(tween(Motion.BASE)) togetherWith fadeOut(tween(Motion.FAST)) },
                        label = "moodLine",
                    ) { m ->
                        Text(
                            m?.line ?: "How's the weather inside you today?",
                            style = Kairos.type.title.copy(fontSize = 22.sp, lineHeight = 27.sp),
                            color = colors.ink,
                        )
                    }
                }
            }
            Spacer(Modifier.height(Space.lg))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Mood.entries.forEach { m -> MoodBubble(m, m == mood) { onPick(m) } }
            }
        }
    }
}

@Composable
private fun MoodBubble(mood: Mood, selected: Boolean, onClick: () -> Unit) {
    val colors = Kairos.colors
    val haptics = LocalHapticFeedback.current
    val interaction = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val scale by animateFloatAsState(if (selected) 1.12f else 1f, Motion.playful(), label = "bubble")
    val ring by animateColorAsState(if (selected) colors.pond else Color.Transparent, label = "ring")
    Column(
        Modifier
            .pressScale(interaction, 0.9f)
            .semantics {
                role = Role.RadioButton
                this.selected = selected
                contentDescription = mood.label
            }
            .androidxClickable(interaction) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(48.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .border(2.dp, ring, CircleShape)
                .padding(4.dp)
                .liquidGlass(CircleShape, tint = moodColor(mood).copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center,
        ) {
            MoodFace(mood, Modifier.size(26.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(mood.label, style = Kairos.type.caption, color = if (selected) colors.ink else colors.inkSoft)
    }
}

private fun Modifier.androidxClickable(interaction: androidx.compose.foundation.interaction.MutableInteractionSource, onClick: () -> Unit) =
    this.then(Modifier.clickable(interaction, null, onClick = onClick))

@Composable
fun MoodFace(mood: Mood, modifier: Modifier = Modifier) {
    val ink = Color(0xFF1B2340)
    androidx.compose.foundation.Canvas(modifier) {
        val w = size.width
        val eyeY = w * 0.4f
        val stroke = w * 0.085f
        for (s in listOf(-1f, 1f)) {
            val cx = w / 2 + s * w * 0.2f
            if (mood == Mood.Glowing || mood == Mood.Sunny) {
                val p = androidx.compose.ui.graphics.Path().apply {
                    moveTo(cx - w * 0.08f, eyeY + w * 0.03f)
                    quadraticTo(cx, eyeY - w * 0.08f, cx + w * 0.08f, eyeY + w * 0.03f)
                }
                drawPath(p, ink, style = androidx.compose.ui.graphics.drawscope.Stroke(stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round))
            } else {
                drawCircle(ink, w * 0.065f, androidx.compose.ui.geometry.Offset(cx, eyeY))
            }
        }
        val curve = when (mood) { Mood.Stormy -> -0.16f; Mood.Cloudy -> -0.05f; Mood.Okay -> 0.05f; Mood.Sunny -> 0.14f; Mood.Glowing -> 0.2f }
        val my = w * 0.66f
        val p = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.36f, my)
            quadraticTo(w / 2, my + w * curve, w * 0.64f, my)
        }
        drawPath(p, ink, style = androidx.compose.ui.graphics.drawscope.Stroke(stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round))
    }
}

@Composable
private fun WordOfDayCard(
    t: Today,
    saved: Boolean,
    inDeck: Boolean,
    onOpen: () -> Unit,
    onSave: () -> Unit,
    onPractice: () -> Unit,
    onReroll: () -> Unit,
    onSpeak: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Kairos.colors
    val w = t.word.item
    GlassCard(modifier.fillMaxWidth(), blur = true, weight = GlassWeight.Frosted, shape = Radii.sheet, onClick = onOpen) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Eyebrow("Word of the day", Modifier.weight(1f))
                PillLabel(w.pos)
            }
            Spacer(Modifier.height(Space.lg))
            AnimatedContent(w, transitionSpec = { (fadeIn(tween(320)) togetherWith fadeOut(tween(160))) }, label = "word") { word ->
                Column {
                    Text(
                        word.word,
                        style = if (word.word.length > 11) Kairos.type.display else Kairos.type.hero,
                        color = colors.ink,
                    )
                    if (word.phonetic.isNotBlank()) {
                        Spacer(Modifier.height(Space.sm))
                        Row(
                            Modifier
                                .liquidGlass(Radii.pill, GlassWeight.Clear)
                                .clickableNoRipple(onSpeak)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .semantics { contentDescription = "Hear ${word.word}" },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(word.phonetic, style = Kairos.type.label, color = colors.inkSoft)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Rounded.VolumeUp, null, tint = colors.pond, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(Modifier.height(Space.lg))
                    Text(word.definition, style = Kairos.type.body.copy(fontSize = 18.sp, lineHeight = 26.sp), color = colors.ink)
                    Spacer(Modifier.height(Space.md))
                    Text(
                        "“${word.example}”",
                        style = Kairos.type.quote.copy(fontSize = 17.sp, lineHeight = 25.sp, fontStyle = FontStyle.Italic),
                        color = colors.inkSoft,
                    )
                }
            }
            Spacer(Modifier.height(Space.lg))
            Reason(t.word.reason)
            Spacer(Modifier.height(Space.lg))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                HeartButton(saved, onSave, label = "Save word")
                KButton(
                    if (inDeck) "Practice" else "Keep it",
                    onPractice,
                    tone = if (inDeck) ButtonTone.Glass else ButtonTone.Pond,
                    icon = if (inDeck) Icons.Rounded.School else Icons.AutoMirrored.Rounded.ArrowForward,
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                )
                if (t.rerollsLeft > 0) GlassIconButton(Icons.Rounded.Refresh, "Another word (${t.rerollsLeft} left)", onReroll, blur = false)
                GlassIconButton(Icons.Rounded.Share, "Share", onShare, blur = false)
            }
        }
    }
}

@Composable
private fun Reason(text: String) {
    if (text.isBlank()) return
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.AutoAwesome, null, tint = Kairos.colors.pond, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, style = Kairos.type.caption.copy(letterSpacing = 0.sp), color = Kairos.colors.inkSoft)
    }
}

@Composable
private fun ThoughtCard(
    t: Today,
    saved: Boolean,
    onSave: () -> Unit,
    onReroll: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Kairos.colors
    GlassCard(modifier.fillMaxWidth(), blur = true, shape = Radii.sheet) {
        Text(
            "“", style = Kairos.type.hero.copy(fontSize = 120.sp, lineHeight = 120.sp), color = colors.pond.copy(alpha = 0.18f),
            modifier = Modifier.offset(x = (-6).dp, y = (-36).dp),
        )
        Column {
            Eyebrow("A thought worth keeping")
            Spacer(Modifier.height(Space.lg))
            AnimatedContent(t.quote.item, transitionSpec = { fadeIn(tween(360)) togetherWith fadeOut(tween(160)) }, label = "quote") { q ->
                Column {
                    Text(q.text, style = Kairos.type.quote, color = colors.ink)
                    Spacer(Modifier.height(Space.md))
                    Text("— ${q.author}", style = Kairos.type.label, color = colors.inkSoft)
                }
            }
            Spacer(Modifier.height(Space.lg))
            Reason(t.quote.reason)
            Spacer(Modifier.height(Space.lg))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                HeartButton(saved, onSave, label = "Keep thought")
                Spacer(Modifier.weight(1f))
                if (t.rerollsLeft > 0) GlassIconButton(Icons.Rounded.Refresh, "Another thought", onReroll, blur = false)
                GlassIconButton(Icons.Rounded.Share, "Share", onShare, blur = false)
            }
        }
    }
}

@Composable
private fun KindnessStack(items: List<Kindness>, done: Set<String>, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val colors = Kairos.colors
    val next = items.firstOrNull { it.id !in done }
    Box(modifier.fillMaxWidth().height(176.dp).clickableNoRipple(onOpen).semantics { contentDescription = "Open today's kindness deck" }) {
        val tints = listOf(colors.peach, colors.mint, colors.lilac)
        listOf(-5f, 4f).forEachIndexed { i, rot ->
            Box(
                Modifier
                    .fillMaxWidth(0.9f)
                    .height(150.dp)
                    .align(Alignment.Center)
                    .rotate(rot)
                    .liquidGlass(Radii.sheet, tint = tints[i + 1].copy(alpha = 0.55f)),
            )
        }
        GlassCard(
            Modifier.fillMaxWidth(0.94f).height(156.dp).align(Alignment.Center),
            shape = Radii.sheet, weight = GlassWeight.Frosted,
        ) {
            Column(Modifier.fillMaxSize()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Eyebrow("${done.size} of ${items.size} done today", Modifier.weight(1f))
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = colors.inkSoft, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.weight(1f))
                Text(
                    next?.title ?: "You've been kind to yourself today.",
                    style = Kairos.type.title.copy(fontSize = 22.sp, lineHeight = 27.sp), color = colors.ink,
                )
                Spacer(Modifier.height(4.dp))
                Text(next?.let { "${it.minutes} min · ${it.detail}" } ?: "Kiri is proud of you.", style = Kairos.type.bodyRegular, color = colors.inkSoft, maxLines = 1)
            }
        }
    }
}

@Composable
private fun DoorTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    wash: Color,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = Kairos.colors
    GlassCard(modifier.height(132.dp), onClick = onClick, contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
        Column(Modifier.fillMaxSize()) {
            Box(
                Modifier.size(42.dp).background(wash.copy(alpha = if (colors.isDark) 0.9f else 0.75f), CircleShape),
                contentAlignment = Alignment.Center,
            ) { Icon(icon, null, tint = colors.ink, modifier = Modifier.size(22.dp)) }
            Spacer(Modifier.weight(1f))
            Text(title, style = Kairos.type.heading.copy(fontSize = 17.sp), color = colors.ink)
            Text(subtitle, style = Kairos.type.caption.copy(letterSpacing = 0.sp), color = if (highlight) colors.pond else colors.inkSoft, maxLines = 1)
        }
    }
}

@Composable
private fun BuddyCard(pond: PondState, onClick: () -> Unit) {
    val colors = Kairos.colors
    val buddy = pond.buddy
    GlassCard(Modifier.fillMaxWidth(), onClick = onClick, shape = Radii.sheet) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (buddy != null) CritterView(buddy.genome, size = 88.dp, name = buddy.entity.name)
            else Box(Modifier.size(88.dp), contentAlignment = Alignment.Center) { Kiri(null, size = 80.dp) }
            Spacer(Modifier.width(Space.md))
            Column(Modifier.weight(1f)) {
                Text(
                    buddy?.let { "${it.entity.name} is splashing about" } ?: "Your pond is waiting",
                    style = Kairos.type.heading.copy(fontSize = 17.sp), color = colors.ink,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (pond.eggsReady > 0) "An egg is ready to hatch!" else "${(pond.progress * 60).toInt()} / 60 dew to the next egg",
                    style = Kairos.type.bodyRegular, color = if (pond.eggsReady > 0) colors.pond else colors.inkSoft,
                )
            }
            Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                ProgressRing(if (pond.eggsReady > 0) 1f else pond.progress, Modifier.size(44.dp), stroke = 5.dp)
                Text("${pond.critters.size}", style = Kairos.type.label, color = colors.ink, textAlign = TextAlign.Center)
            }
        }
    }
}

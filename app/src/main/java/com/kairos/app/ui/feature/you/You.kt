package com.kairos.app.ui.feature.you

import android.Manifest
import android.app.TimePickerDialog
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.data.db.MomentEntity
import com.kairos.app.data.db.MoodEntity
import com.kairos.app.data.prefs.Settings
import com.kairos.app.data.prefs.ThemeMode
import com.kairos.app.data.repo.MomentKind
import com.kairos.app.data.repo.today
import com.kairos.app.engine.Mood
import com.kairos.app.engine.Topics
import com.kairos.app.ui.art.Garden
import com.kairos.app.ui.art.Kai
import com.kairos.app.ui.art.KaiLook
import com.kairos.app.ui.art.KaiLooks
import com.kairos.app.ui.art.drawKai
import com.kairos.app.ui.art.look
import com.kairos.app.ui.components.BigPill
import com.kairos.app.ui.components.Card
import com.kairos.app.ui.components.Icons
import com.kairos.app.ui.components.KairosSlider
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.Segmented
import com.kairos.app.ui.components.SkyTail
import com.kairos.app.ui.components.SliderBubble
import com.kairos.app.ui.components.Wordmark
import com.kairos.app.ui.components.at
import com.kairos.app.ui.components.pressable
import com.kairos.app.ui.components.sky
import com.kairos.app.ui.feature.detail.TopBar
import com.kairos.app.ui.feature.learn.categoryTint
import com.kairos.app.ui.feature.learn.themeTint
import com.kairos.app.ui.theme.Candy
import com.kairos.app.ui.theme.Gap
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.Motion
import com.kairos.app.ui.theme.Radius
import com.kairos.app.ui.theme.Tint
import com.kairos.app.ui.theme.candy
import com.kairos.app.ui.theme.tint
import com.kairos.app.ui.theme.tintInk
import com.kairos.app.util.appContainer
import com.kairos.app.work.Notifier
import com.kairos.app.work.ReminderScheduler
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private data class YouState(val s: Settings, val moments: List<MomentEntity>, val moods: List<MoodEntity>, val saved: Int, val remembered: Int, val total: Int, val hasKey: Boolean, val gardenSeeds: List<Int>)

@Composable
fun YouScreen(openCurator: () -> Unit, openInterests: () -> Unit) {
    val c = appContainer()
    val from = today() - 27
    val flow = remember {
        combine(c.prefs.settings, c.kairos.observeMoments(), c.kairos.observeMoods(from, today()), c.kairos.observeWords(), c.keys.awake) { s, m, md, w, k ->
            YouState(s, m, md, w.count { it.saved }, w.count { it.mastered }, w.count { it.timesShown > 0 || it.saved }, k, w.filter { it.saved }.sortedBy { it.savedAt ?: 0 }.map { it.id.hashCode() })
        }
    }
    val st by flow.collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val y = st ?: return
    val p = K.colors
    val notif = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) scope.launch { c.prefs.setReminder(true, y.s.reminderHour, y.s.reminderMinute); ReminderScheduler.schedule(context, y.s.reminderHour, y.s.reminderMinute) }
    }
    val todayMood = y.moods.filter { it.epochDay == today() }.maxByOrNull { it.createdAt }?.let { Mood.of(it.mood) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 120.dp)) {
        item {
            Column(Modifier.fillMaxWidth().sky().windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = Gap.gutter).padding(top = Gap.xl, bottom = SkyTail)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Kai(todayMood?.look() ?: KaiLooks.Curator, size = 84.dp)
                    Spacer(Modifier.width(Gap.lg))
                    Column {
                        Text(y.s.name.ifBlank { "You" }, style = K.type.shout.copy(fontSize = 36.sp), color = p.onSky)
                        val days = y.moments.map { it.epochDay }.distinct().size
                        Text(if (days == 0) "Your first good moment is waiting" else "$days ${if (days == 1) "day" else "days"} with a good moment", style = K.type.label, color = p.onSkySoft)
                    }
                }
                Spacer(Modifier.height(Gap.xl))
                Row(horizontalArrangement = Arrangement.spacedBy(Gap.md)) {
                    Stat("Words met", y.total, Tint.Sky, Modifier.weight(1f))
                    Stat("Kept", y.saved, Tint.Rose, Modifier.weight(1f))
                    Stat("Remembered", y.remembered, Tint.Mint, Modifier.weight(1f))
                }
            }
        }
        item { GardenCard(y.gardenSeeds, Modifier.padding(horizontal = Gap.gutter)) }
        item { MoodMonth(y.moods, from, Modifier.padding(start = Gap.gutter, end = Gap.gutter, top = Gap.lg)) }
        item { Recent(y.moments, Modifier.padding(start = Gap.gutter, end = Gap.gutter, top = Gap.lg)) }
        item { Text("Settings", style = K.type.display.copy(fontSize = 24.sp), color = p.ink, modifier = Modifier.padding(start = Gap.gutter, top = Gap.xl, bottom = Gap.md)) }
        item {
            Card(Modifier.fillMaxWidth().padding(horizontal = Gap.gutter), padding = Gap.sm) {
                SettingRow(Icons.Sparkle, "Kai, your curator", if (y.hasKey) "Awake and writing for you" else "Asleep. Add a key to wake Kai", openCurator)
                SettingRow(Icons.Sliders, "Interests and level", "${y.s.wordInterests.size} topics, ${Topics.levels[(y.s.level - 1).coerceIn(0, 4)].lowercase()}", openInterests)
            }
        }
        item {
            Card(Modifier.fillMaxWidth().padding(horizontal = Gap.gutter, vertical = Gap.md), padding = Gap.sm) {
                SettingRow(Icons.Bell, "Daily reminder", if (y.s.remindersOn) "Every day at ${"%d:%02d".format(y.s.reminderHour, y.s.reminderMinute)}" else "Off",
                    onClick = if (y.s.remindersOn) ({ TimePickerDialog(context, { _, h, m -> scope.launch { c.prefs.setReminder(true, h, m); ReminderScheduler.schedule(context, h, m) } }, y.s.reminderHour, y.s.reminderMinute, false).show() }) else null) {
                    Toggle(y.s.remindersOn) { on ->
                        if (on && Build.VERSION.SDK_INT >= 33 && !Notifier.canNotify(context)) notif.launch(Manifest.permission.POST_NOTIFICATIONS)
                        else scope.launch {
                            c.prefs.setReminder(on, y.s.reminderHour, y.s.reminderMinute)
                            if (on) ReminderScheduler.schedule(context, y.s.reminderHour, y.s.reminderMinute) else ReminderScheduler.cancel(context)
                        }
                    }
                }
                SettingRow(Icons.Smile, "Calmer motion", "Fewer moving parts, no bursts") { Toggle(y.s.reducedMotion) { scope.launch { c.prefs.setReducedMotion(it) } } }
                Column(Modifier.padding(Gap.lg)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Moon, null, tint = p.ink, modifier = Modifier.size(21.dp)); Spacer(Modifier.width(Gap.lg))
                        Text("Appearance", style = K.type.label.copy(fontSize = 16.sp), color = p.ink)
                    }
                    Spacer(Modifier.height(Gap.md))
                    Segmented(listOf("Auto", "Light", "Dark"), y.s.themeMode.ordinal, { scope.launch { c.prefs.setThemeMode(ThemeMode.entries[it]) } }, Modifier.fillMaxWidth())
                }
            }
        }
        item {
            Column(Modifier.fillMaxWidth().padding(Gap.xxl), horizontalAlignment = Alignment.CenterHorizontally) {
                Wordmark(color = p.inkSoft)
                Spacer(Modifier.height(Gap.xs))
                Text("No account, no ads. Your letters never leave this phone.", style = K.type.caption, color = p.inkFaint, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: Int, tint: Tint, modifier: Modifier) {
    val p = K.colors
    Column(modifier.height(92.dp).clip(Radius.tile).background(Color.White.copy(alpha = 0.92f)).padding(Gap.md), verticalArrangement = Arrangement.Center) {
        Text("$value", style = K.type.serifLarge.copy(fontSize = 36.sp, lineHeight = 38.sp, fontWeight = FontWeight.SemiBold), color = p.tintInk(tint).let { if (p.isDark) lerp(it, Color.Black, 0.4f) else it })
        Text(label, style = K.type.caption, color = Candy.Navy.copy(alpha = 0.6f))
    }
}

/** One flower for every word you kept, grown on a little night sky (r28). */
@Composable
private fun GardenCard(seeds: List<Int>, modifier: Modifier) {
    Column(modifier.fillMaxWidth().clip(Radius.card).background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFF1B2A7A), Color(0xFF080C26)))).padding(top = Gap.xl, start = Gap.xl, end = Gap.xl)) {
        Text("Your garden", style = K.type.title, color = Color.White)
        Text(if (seeds.isEmpty()) "Keep a word and the first flower grows here." else "One flower for every word you kept.", style = K.type.caption, color = Color.White.copy(alpha = 0.7f))
        Spacer(Modifier.height(Gap.md))
        if (seeds.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.BottomCenter) {
                Kai(KaiLooks.Curator.copy(gaze = Offset(0f, 0.8f)), size = 64.dp, modifier = Modifier.padding(bottom = 16.dp))
            }
        } else {
            Garden(seeds.takeLast(9), Modifier.fillMaxWidth().height(150.dp))
        }
    }
}

/** Four weeks of moods as little shapes (r09). */
@Composable
private fun MoodMonth(moods: List<MoodEntity>, from: Long, modifier: Modifier) {
    val p = K.colors
    val byDay = moods.groupBy { it.epochDay }.mapValues { (_, v) -> Mood.of(v.maxBy { it.createdAt }.mood) }
    val top = byDay.values.filterNotNull().groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
    Card(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Four weeks of moods", style = K.type.title, color = p.ink)
                Text(top?.let { "Mostly ${it.label.lowercase()} lately." } ?: "Check in on Today and this fills with little shapes.", style = K.type.caption, color = p.inkSoft)
            }
            if (top != null) Kai(top.look(), size = 44.dp, shadow = false, alive = false, description = null)
        }
        Spacer(Modifier.height(Gap.lg))
        for (w in 0 until 4) Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            for (d in 0 until 7) {
                val day = from + w * 7 + d
                val m = byDay[day]
                Box(Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                    if (m != null) Canvas(Modifier.size(30.dp)) { val l = m.look(); drawKai(l, l, 1f, shadow = false, face = false) }
                    else Box(Modifier.size(if (day == today()) 12.dp else 8.dp).clip(CircleShape).background(if (day == today()) p.accent else p.surfaceSoft))
                }
            }
        }
    }
}

@Composable
private fun Recent(moments: List<MomentEntity>, modifier: Modifier) {
    if (moments.isEmpty()) return
    val p = K.colors
    Card(modifier.fillMaxWidth()) {
        Text("Recent good moments", style = K.type.title, color = p.ink)
        Spacer(Modifier.height(Gap.md))
        moments.take(5).forEachIndexed { i, m ->
            val kind = runCatching { MomentKind.valueOf(m.kind) }.getOrNull() ?: return@forEachIndexed
            Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(p.candy(Tint.entries[i % Tint.entries.size])))
                Spacer(Modifier.width(Gap.md))
                Text(kind.label, style = K.type.label, color = p.ink, modifier = Modifier.weight(1f))
                Text(com.kairos.app.util.Fmt.ago(m.createdAt), style = K.type.caption, color = p.inkFaint)
            }
        }
    }
}

@Composable
private fun SettingRow(icon: ImageVector, title: String, sub: String, onClick: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null) {
    val p = K.colors
    Row(
        Modifier.fillMaxWidth().then(if (onClick != null) Modifier.pressable(scale = 0.99f, onClick = onClick) else Modifier).padding(Gap.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = p.ink, modifier = Modifier.size(21.dp))
        Spacer(Modifier.width(Gap.lg))
        Column(Modifier.weight(1f)) {
            Text(title, style = K.type.label.copy(fontSize = 16.sp), color = p.ink)
            Text(sub, style = K.type.caption, color = p.inkSoft)
        }
        if (trailing != null) trailing() else if (onClick != null) Icon(Icons.Chevron, null, tint = p.inkFaint, modifier = Modifier.size(18.dp))
    }
}

/** A toggle whose knob is a tiny face: asleep when off, smiling when on. */
@Composable
fun Toggle(on: Boolean, onChange: (Boolean) -> Unit) {
    val p = K.colors
    val x by animateFloatAsState(if (on) 1f else 0f, Motion.bloom(), label = "toggle")
    val track by animateColorAsState(if (on) p.accent else p.surfaceSoft, Motion.fade(), label = "track")
    val offLook = KaiLooks.Sleepy.copy(body = Color.White, flat = 0f, depth = 0f, lobes = 8, blush = 0.25f)
    val onLook = KaiLooks.Delighted.copy(body = Color.White, depth = 0f, blush = 0.6f)
    Canvas(
        Modifier
            .size(width = 56.dp, height = 34.dp)
            .semantics { role = Role.Switch; stateDescription = if (on) "On" else "Off" }
            .pressable(role = Role.Switch, scale = 0.92f) { onChange(!on) },
    ) {
        drawRoundRect(track, cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2))
        val r = size.height * 0.4f
        val cx = size.height / 2 + (size.width - size.height) * x
        drawCircle(Color.Black.copy(alpha = 0.12f), r, Offset(cx, size.height / 2 + 1.5f * density))
        drawKai(offLook, onLook, x.coerceIn(0f, 1f), center = Offset(cx, size.height / 2), r = r, shadow = false)
    }
}

/** Interests and level, with the giant pills and the adaptive slider. */
@Composable
fun InterestsScreen(onBack: () -> Unit) {
    val c = appContainer()
    val scope = rememberCoroutineScope()
    val p = K.colors
    var loaded by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var interests by remember { mutableStateOf(emptySet<String>()) }
    var themes by remember { mutableStateOf(emptySet<String>()) }
    var level by remember { mutableFloatStateOf(0.5f) }
    LaunchedEffect(Unit) { val s = c.prefs.current(); name = s.name; interests = s.wordInterests; themes = s.quoteThemes; level = (s.level - 1) / 4f; loaded = true }
    if (!loaded) return
    val shapes = listOf(KaiLooks.Good, KaiLooks.Okay, KaiLooks.Low, KaiLooks.Radiant, KaiLooks.Rough)
    fun pillLook(i: Int, color: Color): KaiLook = shapes[i % shapes.size].copy(body = color, eyes = 1f, lid = 0f, gaze = Offset(0.3f, 0f), smile = 0.35f, blush = 0.3f, asleep = 0f)
    val levelColors = listOf(Candy.Mint, Candy.Sun, Candy.Coral, Candy.Bubblegum, Candy.Lilac)
    Column(Modifier.fillMaxSize().background(p.canvas).verticalScroll(rememberScrollState()).navigationBarsPadding()) {
        TopBar(onBack, onSky = false)
        Column(Modifier.padding(horizontal = Gap.gutter)) {
            Text("Make it yours", style = K.type.shout.copy(fontSize = 38.sp), color = p.ink)
            Text("It shapes what Kai writes next.", style = K.type.body, color = p.inkSoft)
            Spacer(Modifier.height(Gap.xl))
            Text("Your name", style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = p.accent)
            Spacer(Modifier.height(Gap.sm))
            Box(Modifier.fillMaxWidth().clip(CircleShape).background(p.surface).border(1.dp, p.line, CircleShape).padding(horizontal = Gap.lg, vertical = Gap.md)) {
                BasicTextField(name, { name = it.take(24) }, singleLine = true, textStyle = K.type.body.copy(color = p.ink), cursorBrush = SolidColor(p.accent), modifier = Modifier.fillMaxWidth())
            }
            Spacer(Modifier.height(Gap.xl))
            Text("Words about", style = K.type.title, color = p.ink)
            Spacer(Modifier.height(Gap.sm))
        }
        Topics.words.forEachIndexed { i, t ->
            Box(Modifier.fillMaxWidth().padding(horizontal = Gap.lg, vertical = 4.dp), contentAlignment = when (i % 3) { 0 -> Alignment.CenterStart; 1 -> Alignment.Center; else -> Alignment.CenterEnd }) {
                BigPill(t.label, t.key in interests, pillLook(i, p.candy(categoryTint(t.key))), { interests = if (t.key in interests) interests - t.key else interests + t.key }, onSky = false, fontSize = 22)
            }
        }
        Column(Modifier.padding(horizontal = Gap.gutter)) {
            Spacer(Modifier.height(Gap.xl))
            Text("Thoughts that feel like", style = K.type.title, color = p.ink)
            Spacer(Modifier.height(Gap.sm))
        }
        Topics.themes.forEachIndexed { i, t ->
            Box(Modifier.fillMaxWidth().padding(horizontal = Gap.lg, vertical = 4.dp), contentAlignment = when (i % 3) { 0 -> Alignment.CenterStart; 1 -> Alignment.Center; else -> Alignment.CenterEnd }) {
                BigPill(t.label, t.key in themes, pillLook(i + 2, p.candy(themeTint(t.key))), { themes = if (t.key in themes) themes - t.key else themes + t.key }, onSky = false, fontSize = 22)
            }
        }
        Column(Modifier.padding(horizontal = Gap.gutter)) {
            Spacer(Modifier.height(Gap.xl))
            Text("Word level", style = K.type.title, color = p.ink)
            KairosSlider(
                level, { level = it }, "Word level", { Topics.levels[(it * 4).roundToInt()] }, stops = 5, colors = levelColors,
                bubble = { f -> SliderBubble(Topics.levels[(f * 4).roundToInt()], lerp(levelColors.at(f), Candy.Navy, 0.3f)) },
            )
            Row { Text("Everyday", style = K.type.caption, color = p.inkFaint, modifier = Modifier.weight(1f)); Text("Literary", style = K.type.caption, color = p.inkFaint) }
            Spacer(Modifier.height(Gap.xxl))
            PrimaryButton("Save", { scope.launch { c.prefs.setName(name); c.prefs.setInterests(interests, themes, (level * 4).roundToInt() + 1); onBack() } }, Modifier.fillMaxWidth(), dark = true, enabled = interests.isNotEmpty())
            Spacer(Modifier.height(Gap.huge))
        }
    }
}

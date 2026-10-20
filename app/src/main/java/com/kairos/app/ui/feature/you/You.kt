package com.kairos.app.ui.feature.you

import android.Manifest
import android.app.TimePickerDialog
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import com.kairos.app.ui.components.Card
import com.kairos.app.ui.components.Chip
import com.kairos.app.ui.components.GlassTile
import com.kairos.app.ui.components.Icons
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.Segmented
import com.kairos.app.ui.components.Status
import com.kairos.app.ui.components.Wordmark
import com.kairos.app.ui.components.pressable
import com.kairos.app.ui.components.sky
import com.kairos.app.ui.feature.detail.TopBar
import com.kairos.app.ui.feature.today.tint
import com.kairos.app.ui.theme.Gap
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.tint
import com.kairos.app.ui.theme.tintInk
import com.kairos.app.util.appContainer
import com.kairos.app.work.Notifier
import com.kairos.app.work.ReminderScheduler
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

private data class YouState(val s: Settings, val moments: List<MomentEntity>, val moods: List<MoodEntity>, val saved: Int, val remembered: Int, val total: Int, val hasKey: Boolean)

@Composable
fun YouScreen(openCurator: () -> Unit, openInterests: () -> Unit) {
    val c = appContainer()
    val from = today() - 27
    val flow = remember {
        combine(c.prefs.settings, c.kairos.observeMoments(), c.kairos.observeMoods(from, today()), c.kairos.observeWords(), c.keys.hasKey) { s, m, md, w, k ->
            YouState(s, m, md, w.count { it.saved }, w.count { it.mastered }, w.count { it.timesShown > 0 || it.saved }, k)
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
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 120.dp)) {
        item {
            Column(Modifier.fillMaxWidth().sky().windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = Gap.gutter).padding(top = Gap.xl, bottom = Gap.xxl)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(56.dp).clip(CircleShape).background(p.glassOnSky).border(1.dp, p.glassOnSkyRim, CircleShape), contentAlignment = Alignment.Center) {
                        Text(y.s.name.take(1).uppercase().ifBlank { "K" }, style = K.type.serifLarge.copy(fontSize = 28.sp), color = p.onSky)
                    }
                    Spacer(Modifier.width(Gap.lg))
                    Column {
                        Text(y.s.name.ifBlank { "You" }, style = K.type.display, color = p.onSky)
                        Text("${y.moments.map { it.epochDay }.distinct().size} days with a good moment", style = K.type.label, color = p.onSkySoft)
                    }
                }
                Spacer(Modifier.height(Gap.xl))
                Row(horizontalArrangement = Arrangement.spacedBy(Gap.md)) {
                    Stat("Words met", y.total, Modifier.weight(1f))
                    Stat("Kept", y.saved, Modifier.weight(1f))
                    Stat("Remembered", y.remembered, Modifier.weight(1f))
                }
            }
        }
        item { MoodMonth(y.moods, from, Modifier.padding(horizontal = Gap.gutter)) }
        item { Recent(y.moments, Modifier.padding(start = Gap.gutter, end = Gap.gutter, top = Gap.lg)) }
        item { Text("Settings", style = K.type.title, color = p.ink, modifier = Modifier.padding(start = Gap.gutter, top = Gap.xl, bottom = Gap.md)) }
        item {
            Card(Modifier.fillMaxWidth().padding(horizontal = Gap.gutter), padding = Gap.sm) {
                Row_(Icons.Sparkle, "Curator", if (y.hasKey) "Connected, ${y.s.aiModel.substringAfter('/')}" else "Connect OpenRouter to begin", openCurator)
                Row_(Icons.Sliders, "Interests and level", "${y.s.wordInterests.size} topics, ${Topics.levels[(y.s.level - 1).coerceIn(0, 4)].lowercase()}", openInterests)
                Row_(Icons.Bell, "Daily reminder", if (y.s.remindersOn) "Every day at ${"%d:%02d".format(y.s.reminderHour, y.s.reminderMinute)}" else "Off",
                    onClick = if (y.s.remindersOn) ({ TimePickerDialog(context, { _, h, m -> scope.launch { c.prefs.setReminder(true, h, m); ReminderScheduler.schedule(context, h, m) } }, y.s.reminderHour, y.s.reminderMinute, false).show() }) else null) {
                    Toggle(y.s.remindersOn) { on ->
                        if (on && Build.VERSION.SDK_INT >= 33 && !Notifier.canNotify(context)) notif.launch(Manifest.permission.POST_NOTIFICATIONS)
                        else scope.launch {
                            c.prefs.setReminder(on, y.s.reminderHour, y.s.reminderMinute)
                            if (on) ReminderScheduler.schedule(context, y.s.reminderHour, y.s.reminderMinute) else ReminderScheduler.cancel(context)
                        }
                    }
                }
                Row_(Icons.Smile, "Calmer motion", "Fewer moving parts on screen") { Toggle(y.s.reducedMotion) { scope.launch { c.prefs.setReducedMotion(it) } } }
                Column(Modifier.padding(Gap.lg)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Moon, null, tint = p.ink, modifier = Modifier.size(21.dp)); Spacer(Modifier.width(Gap.lg))
                        Text("Appearance", style = K.type.label, color = p.ink)
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
                Text("No account, no ads. Your letters never leave this phone.", style = K.type.caption, color = p.inkFaint)
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: Int, modifier: Modifier) {
    val p = K.colors
    GlassTile(modifier.height(86.dp)) {
        Column(Modifier.fillMaxSize().padding(Gap.md), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$value", style = K.type.serifLarge.copy(fontSize = 32.sp, lineHeight = 34.sp), color = p.onSky)
            Text(label, style = K.type.caption, color = p.onSkySoft)
        }
    }
}

@Composable
private fun MoodMonth(moods: List<MoodEntity>, from: Long, modifier: Modifier) {
    val p = K.colors
    val byDay = moods.groupBy { it.epochDay }.mapValues { (_, v) -> Mood.of(v.maxBy { it.createdAt }.mood) }
    val top = byDay.values.filterNotNull().groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
    Card(modifier.fillMaxWidth()) {
        Text("Four weeks of moods", style = K.type.title, color = p.ink)
        Text(top?.let { "Mostly ${it.label.lowercase()} lately." } ?: "Check in on Today and this fills with colour.", style = K.type.caption, color = p.inkSoft)
        Spacer(Modifier.height(Gap.lg))
        for (w in 0 until 4) Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            for (d in 0 until 7) {
                val day = from + w * 7 + d
                val m = byDay[day]
                Box(Modifier.size(30.dp).clip(CircleShape).background(m?.let { p.tint(it.tint()) } ?: p.surfaceSoft), contentAlignment = Alignment.Center) {
                    if (m != null) Box(Modifier.size(8.dp).clip(CircleShape).background(p.tintInk(m.tint())))
                    else if (day == today()) Box(Modifier.size(5.dp).clip(CircleShape).background(p.ink))
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
        moments.take(5).forEach { m ->
            val kind = runCatching { MomentKind.valueOf(m.kind) }.getOrNull() ?: return@forEach
            Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Status(kind.label, p.accent, Modifier.weight(1f))
                Text(com.kairos.app.util.Fmt.ago(m.createdAt), style = K.type.caption, color = p.inkFaint)
            }
        }
    }
}

@Composable
private fun Row_(icon: ImageVector, title: String, sub: String, onClick: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null) {
    val p = K.colors
    Row(
        Modifier.fillMaxWidth().then(if (onClick != null) Modifier.pressable(scale = 0.99f, onClick = onClick) else Modifier).padding(Gap.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = p.ink, modifier = Modifier.size(21.dp))
        Spacer(Modifier.width(Gap.lg))
        Column(Modifier.weight(1f)) {
            Text(title, style = K.type.label, color = p.ink)
            Text(sub, style = K.type.caption, color = p.inkSoft)
        }
        if (trailing != null) trailing() else if (onClick != null) Icon(Icons.Chevron, null, tint = p.inkFaint, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun Toggle(on: Boolean, onChange: (Boolean) -> Unit) {
    val p = K.colors
    Switch(on, onChange, colors = SwitchDefaults.colors(checkedTrackColor = p.accent, checkedThumbColor = p.onAccent, uncheckedTrackColor = p.surfaceSoft, uncheckedThumbColor = p.inkFaint, uncheckedBorderColor = p.line))
}

/** Topic chips plus a five-step level slider. Shared by onboarding and settings. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TastePicker(interests: Set<String>, onInterests: (Set<String>) -> Unit, themes: Set<String>, onThemes: (Set<String>) -> Unit, level: Int, onLevel: (Int) -> Unit) {
    val p = K.colors
    Text("Words about", style = K.type.title, color = p.ink)
    Spacer(Modifier.height(Gap.md))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Gap.sm), verticalArrangement = Arrangement.spacedBy(Gap.sm)) {
        Topics.words.forEach { t -> Chip(t.label, t.key in interests, { onInterests(if (t.key in interests) interests - t.key else interests + t.key) }) }
    }
    Spacer(Modifier.height(Gap.xl))
    Text("Thoughts that feel like", style = K.type.title, color = p.ink)
    Spacer(Modifier.height(Gap.md))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Gap.sm), verticalArrangement = Arrangement.spacedBy(Gap.sm)) {
        Topics.themes.forEach { t -> Chip(t.label, t.key in themes, { onThemes(if (t.key in themes) themes - t.key else themes + t.key) }) }
    }
    Spacer(Modifier.height(Gap.xl))
    Row(verticalAlignment = Alignment.Bottom) {
        Text("Word level", style = K.type.title, color = p.ink, modifier = Modifier.weight(1f))
        Text(Topics.levels[(level - 1).coerceIn(0, 4)], style = K.type.label, color = p.accent)
    }
    var v by remember(level) { mutableFloatStateOf(level.toFloat()) }
    Slider(v, { v = it }, valueRange = 1f..5f, steps = 3, onValueChangeFinished = { onLevel(v.toInt()) },
        colors = SliderDefaults.colors(thumbColor = p.accent, activeTrackColor = p.accent, inactiveTrackColor = p.surfaceSoft, activeTickColor = p.onAccent, inactiveTickColor = p.inkFaint))
    Row { Text("Everyday", style = K.type.caption, color = p.inkFaint, modifier = Modifier.weight(1f)); Text("Literary", style = K.type.caption, color = p.inkFaint) }
}

@Composable
fun InterestsScreen(onBack: () -> Unit) {
    val c = appContainer()
    val scope = rememberCoroutineScope()
    val p = K.colors
    var loaded by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var interests by remember { mutableStateOf(emptySet<String>()) }
    var themes by remember { mutableStateOf(emptySet<String>()) }
    var level by remember { mutableStateOf(3) }
    LaunchedEffect(Unit) { val s = c.prefs.current(); name = s.name; interests = s.wordInterests; themes = s.quoteThemes; level = s.level; loaded = true }
    if (!loaded) return
    Column(Modifier.fillMaxSize().background(p.canvas).verticalScroll(rememberScrollState()).navigationBarsPadding()) {
        TopBar(onBack, onSky = false)
        Column(Modifier.padding(horizontal = Gap.gutter)) {
            Text("Make it yours", style = K.type.display, color = p.ink)
            Text("Shapes what your curator writes next.", style = K.type.body, color = p.inkSoft)
            Spacer(Modifier.height(Gap.xl))
            Text("Your name", style = K.type.caption, color = p.inkFaint)
            Spacer(Modifier.height(Gap.sm))
            Box(Modifier.fillMaxWidth().clip(CircleShape).background(p.surface).border(1.dp, p.line, CircleShape).padding(horizontal = Gap.lg, vertical = Gap.md)) {
                BasicTextField(name, { name = it.take(24) }, singleLine = true, textStyle = K.type.body.copy(color = p.ink), cursorBrush = SolidColor(p.accent), modifier = Modifier.fillMaxWidth())
            }
            Spacer(Modifier.height(Gap.xl))
            TastePicker(interests, { interests = it }, themes, { themes = it }, level, { level = it })
            Spacer(Modifier.height(Gap.xxl))
            PrimaryButton("Save", { scope.launch { c.prefs.setName(name); c.prefs.setInterests(interests, themes, level); onBack() } }, Modifier.fillMaxWidth(), dark = true)
            Spacer(Modifier.height(Gap.huge))
        }
    }
}

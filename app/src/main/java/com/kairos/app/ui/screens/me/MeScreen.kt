package com.kairos.app.ui.screens.me

import android.Manifest
import android.app.TimePickerDialog
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.data.db.JoyDropEntity
import com.kairos.app.data.prefs.Settings
import com.kairos.app.data.prefs.ThemeMode
import com.kairos.app.data.repo.JoySource
import com.kairos.app.data.repo.today
import com.kairos.app.engine.Mood
import com.kairos.app.ui.brand.KairosWordmark
import com.kairos.app.ui.components.JoyJar
import com.kairos.app.ui.components.Segmented
import com.kairos.app.ui.components.squishClickable
import com.kairos.app.ui.glass.GlassPanel
import com.kairos.app.ui.icons.KairosIcons
import com.kairos.app.ui.mascot.DropMascot
import com.kairos.app.ui.mascot.Expression
import com.kairos.app.ui.navigation.Scene
import com.kairos.app.ui.screens.today.categoryWash
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Space
import com.kairos.app.ui.theme.Wash
import com.kairos.app.util.appContainer
import com.kairos.app.work.Notifier
import com.kairos.app.work.ReminderScheduler
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate

private data class MeState(
    val settings: Settings,
    val joy: List<JoyDropEntity>,
    val savedWords: List<com.kairos.app.data.db.WordEntity>,
    val savedQuotes: List<com.kairos.app.data.db.QuoteEntity>,
    val moods: List<com.kairos.app.data.db.MoodEntity>,
)

@Composable
fun MeScreen(openInterests: () -> Unit, openWord: (String) -> Unit, openQuote: (String) -> Unit) {
    val c = appContainer()
    val from = today() - 27
    val flow = remember {
        combine(
            c.prefs.settings, c.kairos.observeJoy(), c.kairos.observeWords(), c.kairos.observeQuotes(), c.kairos.observeMoods(from, today()),
        ) { s, j, w, q, m -> MeState(s, j, w.filter { it.saved }.sortedByDescending { it.savedAt }, q.filter { it.saved }.sortedByDescending { it.savedAt }, m) }
    }
    val state by flow.collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    Scene(Wash.Butter, Wash.Peach, Wash.Mint, Wash.Rose, Wash.Cerulean)
    val st = state ?: return
    val palette = Kairos.colors
    val notifPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) scope.launch {
            c.prefs.setReminder(true, st.settings.reminderHour, st.settings.reminderMinute)
            ReminderScheduler.schedule(context, st.settings.reminderHour, st.settings.reminderMinute)
        }
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 130.dp)) {
        item {
            Row(
                Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = Space.gutter)
                    .padding(top = Space.xl),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(st.settings.name.ifBlank { "You" }, style = Kairos.type.display, color = palette.ink)
                    val days = st.joy.map { it.epochDay }.distinct().size
                    Text(
                        if (days == 0) "Your jar is ready for its first drop." else "$days ${if (days == 1) "day" else "days"} of good moments, ${st.joy.size} drops in the jar.",
                        style = Kairos.type.body, color = palette.inkSoft,
                    )
                }
                DropMascot(size = 84.dp, wash = Wash.Butter, expression = Expression.Wink)
            }
        }
        item { JoyJarCard(st.joy, Modifier.padding(horizontal = Space.gutter, vertical = Space.xl)) }
        item { MoodGarden(st.moods, from, Modifier.padding(horizontal = Space.gutter)) }
        if (st.savedWords.isNotEmpty()) {
            item { Title("Words you kept", "${st.savedWords.size}") }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = Space.gutter), horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                    items(st.savedWords, key = { it.id }) { w ->
                        Column(
                            Modifier
                                .width(150.dp)
                                .squishClickable { openWord(w.id) }
                                .clip(RoundedCornerShape(24.dp))
                                .background(categoryWash(w.category).color(palette.isDark).copy(alpha = 0.45f))
                                .padding(Space.lg)
                        ) {
                            Text(w.word, style = Kairos.type.title, color = palette.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(w.definition, style = Kairos.type.caption, color = palette.inkSoft, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
        if (st.savedQuotes.isNotEmpty()) {
            item { Title("Thoughts you kept", "${st.savedQuotes.size}") }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = Space.gutter), horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                    items(st.savedQuotes, key = { it.id }) { q ->
                        Column(
                            Modifier
                                .width(220.dp)
                                .squishClickable { openQuote(q.id) }
                                .clip(RoundedCornerShape(24.dp))
                                .background(palette.card)
                                .padding(Space.lg)
                        ) {
                            Text(q.text, style = Kairos.type.quote.copy(fontSize = Kairos.type.label.fontSize, lineHeight = Kairos.type.label.lineHeight), color = palette.ink, maxLines = 3, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(Space.xs))
                            Text(q.author, style = Kairos.type.caption, color = palette.inkSoft)
                        }
                    }
                }
            }
        }
        item { Title("Settings") }
        item {
            GlassPanel(Modifier.padding(horizontal = Space.gutter).fillMaxWidth(), blur = false) {
                Column(Modifier.padding(vertical = Space.sm)) {
                    SettingRow(KairosIcons.Sliders, "Interests and level", "Shapes which words and thoughts you get", onClick = openInterests)
                    SettingRow(
                        KairosIcons.Bell, "Daily nudge",
                        if (st.settings.remindersOn) "Every day at ${"%d:%02d".format(st.settings.reminderHour, st.settings.reminderMinute)}" else "Off",
                        onClick = if (st.settings.remindersOn) {
                            {
                                TimePickerDialog(context, { _, h, m ->
                                    scope.launch { c.prefs.setReminder(true, h, m); ReminderScheduler.schedule(context, h, m) }
                                }, st.settings.reminderHour, st.settings.reminderMinute, false).show()
                            }
                        } else null,
                    ) {
                        KairosSwitch(st.settings.remindersOn) { on ->
                            if (on && Build.VERSION.SDK_INT >= 33 && !Notifier.canNotify(context)) {
                                notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else scope.launch {
                                c.prefs.setReminder(on, st.settings.reminderHour, st.settings.reminderMinute)
                                if (on) ReminderScheduler.schedule(context, st.settings.reminderHour, st.settings.reminderMinute)
                                else ReminderScheduler.cancel(context)
                            }
                        }
                    }
                    SettingRow(KairosIcons.Sparkle, "Calmer motion", "Fewer moving things on screen") {
                        KairosSwitch(st.settings.reducedMotion) { scope.launch { c.prefs.setReducedMotion(it) } }
                    }
                    Column(Modifier.padding(horizontal = Space.xl, vertical = Space.md)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(KairosIcons.Moon, null, tint = palette.ink, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(Space.lg))
                            Text("Paper", style = Kairos.type.label, color = palette.ink)
                        }
                        Spacer(Modifier.height(Space.md))
                        Segmented(
                            listOf("Auto", "Daybreak", "Dusk"), st.settings.themeMode.ordinal,
                            { scope.launch { c.prefs.setThemeMode(ThemeMode.entries[it]) } },
                            Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
        item {
            Column(Modifier.fillMaxWidth().padding(Space.xxl), horizontalAlignment = Alignment.CenterHorizontally) {
                KairosWordmark(markSize = 22.dp)
                Spacer(Modifier.height(Space.xs))
                Text("Everything stays on this phone. No account, no ads.", style = Kairos.type.caption, color = palette.inkFaint)
            }
        }
    }
}

@Composable
private fun Title(text: String, count: String? = null) {
    Row(Modifier.padding(start = Space.gutter, end = Space.gutter, top = Space.xl, bottom = Space.md), verticalAlignment = Alignment.Bottom) {
        Text(text, style = Kairos.type.title, color = Kairos.colors.ink, modifier = Modifier.weight(1f))
        if (count != null) Text(count, style = Kairos.type.label, color = Kairos.colors.inkSoft)
    }
}

@Composable
private fun JoyJarCard(joy: List<JoyDropEntity>, modifier: Modifier = Modifier) {
    val palette = Kairos.colors
    val bySource = joy.groupingBy { it.source }.eachCount()
    GlassPanel(modifier.fillMaxWidth()) {
        Row(Modifier.padding(Space.xl), verticalAlignment = Alignment.CenterVertically) {
            JoyJar(
                joy.map { runCatching { Wash.valueOf(it.wash) }.getOrDefault(Wash.Cerulean).color(palette.isDark) },
                Modifier.size(width = 120.dp, height = 168.dp),
                capacity = 80,
            )
            Spacer(Modifier.width(Space.xl))
            Column(Modifier.weight(1f)) {
                Text("Joy jar", style = Kairos.type.title, color = palette.ink)
                Text("Every good thing you do adds a drop. No streaks to lose.", style = Kairos.type.label, color = palette.inkSoft)
                Spacer(Modifier.height(Space.md))
                JoySource.entries.filter { (bySource[it.name] ?: 0) > 0 }.take(5).forEach { s ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(s.wash.color(palette.isDark)))
                        Spacer(Modifier.width(Space.sm))
                        Text(s.label, style = Kairos.type.caption, color = palette.ink, modifier = Modifier.weight(1f))
                        Text("${bySource[s.name]}", style = Kairos.type.caption, color = palette.inkSoft)
                    }
                }
            }
        }
    }
}

/** Four weeks of moods as a little garden of painted dots — tap-free, just a gentle look back. */
@Composable
private fun MoodGarden(moods: List<com.kairos.app.data.db.MoodEntity>, from: Long, modifier: Modifier = Modifier) {
    val palette = Kairos.colors
    val byDay = moods.groupBy { it.epochDay }.mapValues { (_, v) -> Mood.of(v.maxBy { it.createdAt }.mood) }
    val counts = byDay.values.filterNotNull().groupingBy { it }.eachCount()
    GlassPanel(modifier.fillMaxWidth(), blur = false) {
        Column(Modifier.padding(Space.xl)) {
            Text("Last four weeks", style = Kairos.type.title, color = palette.ink)
            val top = counts.maxByOrNull { it.value }?.key
            Text(
                if (top == null) "Check in on Today and your weeks will fill with colour." else "Mostly ${top.label.lowercase()} lately.",
                style = Kairos.type.label, color = palette.inkSoft,
            )
            Spacer(Modifier.height(Space.lg))
            for (week in 0 until 4) {
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    for (d in 0 until 7) {
                        val day = from + week * 7 + d
                        val mood = byDay[day]
                        val isToday = day == today()
                        Box(
                            Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(mood?.wash?.color(palette.isDark) ?: palette.inkFaint.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isToday) Box(Modifier.size(6.dp).clip(CircleShape).background(palette.ink))
                            else if (mood == null) Text(LocalDate.ofEpochDay(day).dayOfMonth.toString(), style = Kairos.type.caption.copy(fontSize = Kairos.type.caption.fontSize * 0.85f), color = palette.inkFaint)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val palette = Kairos.colors
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.squishClickable(pressedScale = 0.98f, onClick = onClick) else Modifier)
            .padding(horizontal = Space.xl, vertical = Space.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = palette.ink, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(Space.lg))
        Column(Modifier.weight(1f)) {
            Text(title, style = Kairos.type.label, color = palette.ink)
            Text(subtitle, style = Kairos.type.caption, color = palette.inkSoft)
        }
        if (trailing != null) trailing() else if (onClick != null) Icon(KairosIcons.Chevron, null, tint = palette.inkFaint)
    }
}

@Composable
fun KairosSwitch(checked: Boolean, onChange: (Boolean) -> Unit) {
    val palette = Kairos.colors
    Switch(
        checked, onChange,
        colors = SwitchDefaults.colors(
            checkedTrackColor = palette.accent, checkedThumbColor = palette.onAccent,
            uncheckedTrackColor = palette.glassFillStrong, uncheckedThumbColor = palette.inkFaint, uncheckedBorderColor = palette.inkFaint.copy(alpha = 0.4f),
        ),
    )
}


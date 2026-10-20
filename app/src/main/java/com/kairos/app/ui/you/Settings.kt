package com.kairos.app.ui.you

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.container
import com.kairos.app.data.prefs.Settings
import com.kairos.app.engine.content.Topics
import com.kairos.app.engine.notify.Notify
import com.kairos.app.engine.notify.Scheduler
import com.kairos.app.ui.components.DetailTopBar
import com.kairos.app.ui.components.Eyebrow
import com.kairos.app.ui.components.SectionTitle
import com.kairos.app.ui.glass.GlassCard
import com.kairos.app.ui.glass.GlassWeight
import com.kairos.app.ui.glass.KChip
import com.kairos.app.ui.glass.liquidGlass
import com.kairos.app.ui.nav.LocalNavigator
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Motion
import com.kairos.app.ui.theme.Radii
import com.kairos.app.ui.theme.Space
import com.kairos.app.ui.theme.ThemeMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen() {
    val nav = LocalNavigator.current
    val context = LocalContext.current
    val c = context.container
    val scope = rememberCoroutineScope()
    val s by c.settings.settings.collectAsStateWithLifecycle(initialValue = Settings())
    val colors = Kairos.colors
    var name by remember { mutableStateOf<String?>(null) }
    var pickTime by remember { mutableStateOf(false) }
    LaunchedEffect(s.name) { if (name == null) name = s.name }

    fun setReminders(on: Boolean, minutes: Int = s.reminderMinutes) = scope.launch {
        c.settings.setReminders(on, minutes)
        if (on) Scheduler.scheduleNudge(context, minutes) else Scheduler.cancelNudge(context)
    }
    val notifPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> setReminders(granted) }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = Space.huge)) {
        item { DetailTopBar(onBack = nav::back, title = "Settings") }
        item {
            Column(Modifier.padding(horizontal = Space.screen)) {
                SectionTitle("You")
                GlassCard(Modifier.fillMaxWidth()) {
                    Column {
                        Eyebrow("What Kiri calls you")
                        Spacer(Modifier.height(Space.sm))
                        BasicTextField(
                            name.orEmpty(),
                            { name = it.take(24); scope.launch { c.settings.setName(it.take(24)) } },
                            singleLine = true, textStyle = Kairos.type.title.copy(color = colors.ink), cursorBrush = SolidColor(colors.pond),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                SectionTitle("Look & feel")
                GlassCard(Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(Space.lg)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                            ThemeMode.entries.forEach { m ->
                                KChip(when (m) { ThemeMode.System -> "Match phone"; ThemeMode.Light -> "Day pond"; ThemeMode.Dark -> "Night pond" }, s.theme == m, { scope.launch { c.settings.setTheme(m) } })
                            }
                        }
                        ToggleRow("Calmer motion", "Softens springs and stops looping animation.", s.reduceMotion) { scope.launch { c.settings.setReduceMotion(it) } }
                        ToggleRow("Still watercolor", "Keep the pond painting from drifting (saves a little battery).", s.calmBackground) { scope.launch { c.settings.setCalmBackground(it) } }
                        ToggleRow("Say words aloud", "Use your phone's voice for pronunciations.", s.ttsOn) { scope.launch { c.settings.setTts(it) } }
                    }
                }

                SectionTitle("Daily nudge")
                GlassCard(Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(Space.md)) {
                        ToggleRow("One gentle reminder a day", "Today's word and a thought. Never more than one.", s.remindersOn) { on ->
                            if (on && Build.VERSION.SDK_INT >= 33 && !Notify.canPost(context)) notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                            else setReminders(on)
                        }
                        if (s.remindersOn) {
                            KChip("At %d:%02d".format(s.reminderMinutes / 60, s.reminderMinutes % 60), false, { pickTime = true })
                        }
                    }
                }

                SectionTitle("What you're curious about")
                GlassCard(Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(Space.md)) {
                        Eyebrow("Word interests")
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.sm), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                            Topics.wordInterests.forEach { t ->
                                KChip(t.label, t.id in s.wordInterests, {
                                    scope.launch { c.settings.setWordInterests(if (t.id in s.wordInterests) s.wordInterests - t.id else s.wordInterests + t.id) }
                                })
                            }
                        }
                        Eyebrow("Thought themes")
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.sm), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                            Topics.quoteThemes.forEach { t ->
                                KChip(t.label, t.id in s.quoteThemes, {
                                    scope.launch { c.settings.setQuoteThemes(if (t.id in s.quoteThemes) s.quoteThemes - t.id else s.quoteThemes + t.id) }
                                })
                            }
                        }
                        Eyebrow("Word depth")
                        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                            listOf(1 to "Everyday", 2 to "Rich", 3 to "Rare & delicious").forEach { (lvl, label) ->
                                KChip(label, s.level == lvl, { scope.launch { c.settings.setLevel(lvl) } })
                            }
                        }
                        Text("Changes shape tomorrow's picks — today's stay put.", style = Kairos.type.caption.copy(letterSpacing = 0.sp), color = colors.inkSoft)
                    }
                }

                SectionTitle("Your data")
                GlassCard(Modifier.fillMaxWidth()) {
                    Text(
                        "Everything lives on this phone: your words, moods, letters and pond. Letters are encrypted with a key kept in Android's secure hardware. Kairos has no account, no ads and no tracking. The only things that touch the internet are dictionary lookups, the game shelf, and game downloads you start.",
                        style = Kairos.type.bodyRegular, color = colors.ink,
                    )
                }

                SectionTitle("Thanks")
                Text(
                    "Fraunces (Undercase Type) and Nunito (Vernon Adams et al.) under the SIL Open Font License. Definitions by the Free Dictionary API. Fan games belong to their creators.",
                    style = Kairos.type.bodyRegular, color = colors.inkSoft,
                )
            }
        }
    }

    if (pickTime) {
        val state = rememberTimePickerState(s.reminderMinutes / 60, s.reminderMinutes % 60)
        AlertDialog(
            onDismissRequest = { pickTime = false },
            confirmButton = { TextButton({ pickTime = false; setReminders(true, state.hour * 60 + state.minute) }) { Text("Set", style = Kairos.type.label) } },
            dismissButton = { TextButton({ pickTime = false }) { Text("Cancel", style = Kairos.type.label) } },
            text = { TimePicker(state, colors = TimePickerDefaults.colors(clockDialColor = colors.paperDeep, selectorColor = colors.pond)) },
            containerColor = colors.paper,
        )
    }
}

@Composable
fun ToggleRow(title: String, sub: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val colors = Kairos.colors
    Row(
        Modifier.fillMaxWidth().toggleable(checked, role = Role.Switch, onValueChange = onChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = Kairos.type.label, color = colors.ink)
            Text(sub, style = Kairos.type.bodyRegular.copy(fontSize = 14.sp, lineHeight = 19.sp), color = colors.inkSoft)
        }
        Spacer(Modifier.size(Space.md))
        LiquidSwitch(checked)
    }
}

/** A glass track with a droplet knob that stretches as it slides. */
@Composable
fun LiquidSwitch(checked: Boolean) {
    val colors = Kairos.colors
    val pos by animateFloatAsState(if (checked) 1f else 0f, Motion.snappy(), label = "switchPos")
    val lag by animateFloatAsState(if (checked) 1f else 0f, Motion.gentle(), label = "switchLag")
    val track by animateColorAsState(if (checked) colors.pond else colors.ink.copy(alpha = 0.12f), label = "track")
    Canvas(Modifier.size(width = 54.dp, height = 32.dp).liquidGlass(Radii.pill, GlassWeight.Clear)) {
        drawRoundRect(track, cornerRadius = CornerRadius(size.height / 2))
        val r = size.height / 2 - 3.dp.toPx()
        val travel = size.width - 2 * (r + 3.dp.toPx())
        val a = 3.dp.toPx() + travel * minOf(pos, lag)
        val b = 3.dp.toPx() + travel * maxOf(pos, lag) + 2 * r
        drawRoundRect(
            Brush.verticalGradient(listOf(Color.White, Color(0xFFEFF3FF))),
            topLeft = Offset(a, 3.dp.toPx()), size = Size(b - a, r * 2), cornerRadius = CornerRadius(r),
        )
        drawCircle(Color.White, r * 0.25f, Offset(a + r * 0.7f, 3.dp.toPx() + r * 0.6f))
    }
}

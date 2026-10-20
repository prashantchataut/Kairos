package com.kairos.app.ui.feature.capsule

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.kairos.app.AppContainer
import com.kairos.app.data.repo.CapsuleDraft
import com.kairos.app.data.repo.DraftAttachment
import com.kairos.app.engine.Mood
import com.kairos.app.ui.components.Card
import com.kairos.app.ui.components.Chip
import com.kairos.app.ui.components.Icons
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.pressable
import com.kairos.app.ui.components.SkyTail
import com.kairos.app.ui.components.sky
import com.kairos.app.ui.feature.detail.TopBar
import com.kairos.app.ui.feature.today.tint
import com.kairos.app.ui.nav.frosted
import com.kairos.app.ui.theme.Gap
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.Motion
import com.kairos.app.ui.theme.Serif
import com.kairos.app.ui.theme.Tint
import com.kairos.app.ui.theme.tint
import com.kairos.app.ui.theme.tintInk
import com.kairos.app.util.AudioRecorder
import com.kairos.app.util.Fmt
import com.kairos.app.util.appViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

enum class OpenIn(val label: String, val months: Long) { Month("1 month", 1), Season("3 months", 3), Half("6 months", 6), Year("1 year", 12), Five("5 years", 60) }

data class ComposeState(
    val title: String = "",
    val letter: String = "",
    val date: LocalDate = LocalDate.now().plusMonths(12),
    val preset: OpenIn? = OpenIn.Year,
    val mood: Mood? = null,
    val tint: Tint = Tint.Sky,
    val attachments: List<DraftAttachment> = emptyList(),
    val prompt: String = "What do you hope is true when you read this?",
    val sealing: Boolean = false,
    val sealed: Boolean = false,
) {
    val canSeal get() = (letter.isNotBlank() || attachments.isNotEmpty()) && !sealing
    val dirty get() = letter.isNotBlank() || title.isNotBlank() || attachments.isNotEmpty()
}

class ComposeViewModel(private val c: AppContainer) : ViewModel() {
    private val _s = MutableStateFlow(ComposeState())
    val state = _s.asStateFlow()
    init { nextPrompt() }

    fun nextPrompt() = viewModelScope.launch { c.kairos.nextPrompt()?.let { p -> _s.update { it.copy(prompt = p) } } }
    fun title(v: String) = _s.update { it.copy(title = v.take(60)) }
    fun letter(v: String) = _s.update { it.copy(letter = v) }
    fun preset(p: OpenIn) = _s.update { it.copy(preset = p, date = LocalDate.now().plusMonths(p.months)) }
    fun date(d: LocalDate) = _s.update { it.copy(preset = null, date = d) }
    fun mood(m: Mood) = _s.update { it.copy(mood = m, tint = m.tint()) }
    fun tint(t: Tint) = _s.update { it.copy(tint = t) }
    fun usePrompt() = _s.update { it.copy(letter = (if (it.letter.isBlank()) "" else it.letter + "\n\n") + it.prompt + "\n") }
    fun add(uris: List<Uri>) {
        val d = uris.mapNotNull { runCatching { c.capsules.describe(it) }.getOrNull() }
        _s.update { s -> s.copy(attachments = (s.attachments + d).distinctBy { it.uri }.take(12)) }
    }
    fun addVoice(uri: Uri, ms: Long, size: Long) {
        val secs = ms / 1000
        _s.update { it.copy(attachments = it.attachments + DraftAttachment(uri, "Voice note ${secs / 60}:${"%02d".format(secs % 60)}.m4a", "audio/mp4", size)) }
    }
    fun remove(a: DraftAttachment) = _s.update { it.copy(attachments = it.attachments - a) }
    fun seal() {
        val s = _s.value
        if (!s.canSeal) return
        _s.update { it.copy(sealing = true) }
        val opensAt = s.date.atTime(LocalTime.of(9, 0)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        viewModelScope.launch {
            c.capsules.seal(CapsuleDraft(s.title, s.letter, opensAt, s.mood?.name, s.tint.name, s.attachments))
            _s.update { it.copy(sealed = true) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComposeLetter(onBack: () -> Unit, onSealed: () -> Unit) {
    val vm = appViewModel { ComposeViewModel(it) }
    val s by vm.state.collectAsStateWithLifecycle()
    val p = K.colors
    val context = LocalContext.current
    var confirm by remember { mutableStateOf(false) }
    var pickDate by remember { mutableStateOf(false) }
    BackHandler(s.dirty && !s.sealing) { confirm = true }

    val photos = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(8)) { vm.add(it) }
    val files = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { vm.add(it) }
    val recorder = remember { AudioRecorder(context) }
    var recording by remember { mutableStateOf(false) }
    var recStart by remember { mutableLongStateOf(0L) }
    DisposableEffect(Unit) { onDispose { recorder.release() } }
    fun toggleRec() {
        if (recording) { recorder.stop()?.let { (f, ms) -> vm.addVoice(Uri.fromFile(f), ms, f.length()) }; recording = false }
        else if (recorder.start()) { recording = true; recStart = System.currentTimeMillis() }
    }
    val mic = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) toggleRec() }

    Box(Modifier.fillMaxSize().background(p.canvas)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(bottom = 120.dp)) {
            Column(Modifier.fillMaxWidth().sky().padding(bottom = SkyTail)) {
                TopBar({ if (s.dirty) confirm = true else onBack() }, onSky = true)
                Column(Modifier.padding(horizontal = Gap.gutter)) {
                    Spacer(Modifier.height(Gap.md))
                    Text("This letter opens on", style = K.type.label, color = p.onSkySoft)
                    AnimatedContent(s.date, transitionSpec = { (fadeIn() + slideInVertically { it / 4 }) togetherWith fadeOut() }, label = "date") { d ->
                        Column {
                            Text(Fmt.ordinalDate(d), style = K.type.hero.copy(fontSize = 56.sp, lineHeight = 58.sp), color = p.onSky)
                            Text("${d.year}, ${Fmt.until(d.atTime(9, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())}", style = K.type.label, color = p.onSkySoft)
                        }
                    }
                }
                Spacer(Modifier.height(Gap.lg))
                LazyRow(contentPadding = PaddingValues(horizontal = Gap.gutter), horizontalArrangement = Arrangement.spacedBy(Gap.sm)) {
                    items(OpenIn.entries.toList()) { o -> SkyChip(o.label, s.preset == o) { vm.preset(o) } }
                    item { SkyChip("Pick a day", s.preset == null, icon = Icons.Calendar) { pickDate = true } }
                }
            }
            Card(Modifier.fillMaxWidth().padding(horizontal = Gap.gutter)) {
                Box {
                    if (s.title.isEmpty()) Text("Give it a name (optional)", style = K.type.title, color = p.inkFaint)
                    BasicTextField(s.title, vm::title, singleLine = true, textStyle = K.type.title.copy(color = p.ink), cursorBrush = SolidColor(p.accent), modifier = Modifier.fillMaxWidth())
                }
                Spacer(Modifier.height(Gap.lg))
                Text("Dear future me,", style = K.type.serifLarge.copy(fontSize = 26.sp, lineHeight = 30.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic), color = p.ink)
                Spacer(Modifier.height(Gap.sm))
                Box(Modifier.fillMaxWidth().heightIn(min = 220.dp)) {
                    val letterStyle = K.type.body.copy(fontFamily = Serif, fontSize = 21.sp, lineHeight = 31.sp, color = p.ink)
                    if (s.letter.isEmpty()) Text(s.prompt, style = letterStyle.copy(color = p.inkFaint))
                    BasicTextField(s.letter, vm::letter, textStyle = letterStyle, cursorBrush = SolidColor(p.accent), modifier = Modifier.fillMaxWidth().heightIn(min = 220.dp))
                }
                Spacer(Modifier.height(Gap.md))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Sparkle, null, tint = p.accent, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Use this prompt", style = K.type.label, color = p.accent, modifier = Modifier.pressable(onClick = vm::usePrompt))
                    Spacer(Modifier.weight(1f))
                    Text("Another", style = K.type.label, color = p.inkSoft, modifier = Modifier.pressable(onClick = vm::nextPrompt))
                }
            }
            if (s.attachments.isNotEmpty()) {
                LazyRow(contentPadding = PaddingValues(Gap.gutter), horizontalArrangement = Arrangement.spacedBy(Gap.lg)) {
                    itemsIndexed(s.attachments, key = { _, a -> a.uri.toString() }) { i, a ->
                        AttachmentCard(a.kind, a.uri, a.name, tiltFor(i), onRemove = { vm.remove(a) }, modifier = Modifier.animateItem())
                    }
                }
            } else Spacer(Modifier.height(Gap.lg))
            Card(Modifier.fillMaxWidth().padding(horizontal = Gap.gutter)) {
                Text("How are you right now?", style = K.type.title, color = p.ink)
                Text("Future you will see it next to how they feel.", style = K.type.caption, color = p.inkSoft)
                Spacer(Modifier.height(Gap.md))
                androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(Gap.sm), verticalArrangement = Arrangement.spacedBy(Gap.sm)) {
                    Mood.entries.forEach { m -> Chip(m.label, s.mood == m, { vm.mood(m) }) }
                }
                Spacer(Modifier.height(Gap.lg))
                Text("Envelope", style = K.type.caption, color = p.inkFaint)
                Spacer(Modifier.height(Gap.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(Gap.md)) {
                    Tint.entries.forEach { t ->
                        Box(Modifier.size(34.dp).pressable(label = t.name) { vm.tint(t) }.clip(CircleShape).background(p.tint(t)).border(if (t == s.tint) 2.dp else 1.dp, if (t == s.tint) p.tintInk(t) else p.line, CircleShape))
                    }
                }
            }
        }

        Row(
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().imePadding().padding(Gap.gutter).fillMaxWidth().frosted().padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BarIcon(Icons.Image, "Add photos") { photos.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) }
            BarIcon(Icons.Folder, "Attach files") { files.launch(arrayOf("*/*")) }
            BarIcon(if (recording) Icons.Stop else Icons.Mic, if (recording) "Stop recording" else "Record a voice note", tint = if (recording) p.negative else p.ink) {
                if (recording || ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) toggleRec()
                else mic.launch(Manifest.permission.RECORD_AUDIO)
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { if (recording) RecTimer(recStart) }
            PrimaryButton("Seal", vm::seal, icon = Icons.Lock, enabled = s.canSeal)
        }

        AnimatedVisibility(s.sealing, enter = fadeIn(), exit = fadeOut()) { Sealing(p.tint(s.tint), s.date, s.sealed, onSealed) }
    }

    if (confirm) AlertDialog(
        onDismissRequest = { confirm = false },
        title = { Text("Discard this letter?", style = K.type.title) },
        text = { Text("What you've written won't be saved.", style = K.type.body) },
        confirmButton = { TextButton({ confirm = false; onBack() }) { Text("Discard", color = p.negative) } },
        dismissButton = { TextButton({ confirm = false }) { Text("Keep writing") } },
        containerColor = p.surface,
    )
    if (pickDate) {
        val tomorrow = LocalDate.now().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val st = rememberDatePickerState(
            initialSelectedDateMillis = s.date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= tomorrow
                override fun isSelectableYear(year: Int) = year >= LocalDate.now().year
            },
        )
        DatePickerDialog(
            onDismissRequest = { pickDate = false },
            confirmButton = { TextButton({ st.selectedDateMillis?.let { vm.date(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) }; pickDate = false }) { Text("Choose") } },
            dismissButton = { TextButton({ pickDate = false }) { Text("Cancel") } },
        ) { DatePicker(st) }
    }
}

@Composable
private fun SkyChip(text: String, on: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector? = null, onClick: () -> Unit) {
    val p = K.colors
    Row(
        Modifier.pressable(onClick = onClick).clip(CircleShape).background(if (on) p.surface else p.glassOnSky).border(1.dp, if (on) p.surface else p.glassOnSkyRim, CircleShape).padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) { Icon(icon, null, tint = if (on) p.ink else p.onSky, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(6.dp)) }
        Text(text, style = K.type.label, color = if (on) p.ink else p.onSky)
    }
}

@Composable
private fun BarIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, tint: androidx.compose.ui.graphics.Color = K.colors.ink, onClick: () -> Unit) {
    Box(Modifier.size(48.dp).pressable(label = label, onClick = onClick), contentAlignment = Alignment.Center) { Icon(icon, label, tint = tint, modifier = Modifier.size(23.dp)) }
}

@Composable
private fun RecTimer(start: Long) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(start) { while (true) { delay(250); now = System.currentTimeMillis() } }
    val secs = (now - start) / 1000
    val pulse by rememberInfiniteTransition(label = "rec").animateFloat(0.35f, 1f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "p")
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(9.dp).graphicsLayer { alpha = pulse }.clip(CircleShape).background(K.colors.negative))
        Spacer(Modifier.width(6.dp))
        Text("${secs / 60}:${"%02d".format(secs % 60)}", style = K.type.label, color = K.colors.ink)
    }
}

/** The letter slides into its envelope, the flap folds shut, the seal presses on, and it floats off. */
@Composable
private fun Sealing(lining: androidx.compose.ui.graphics.Color, date: LocalDate, done: Boolean, onFinished: () -> Unit) {
    val p = K.colors
    val calm = K.calm
    val letter = remember { Animatable(1f) }
    val flap = remember { Animatable(1f) }
    val seal = remember { Animatable(0f) }
    val lift = remember { Animatable(0f) }
    var text by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (calm) { letter.snapTo(0f); flap.snapTo(0f); seal.snapTo(1f); text = true; return@LaunchedEffect }
        letter.animateTo(0f, tween(620, easing = Motion.Ease))
        flap.animateTo(0f, tween(420, easing = Motion.Ease))
        seal.animateTo(1f, Motion.soft())
        text = true
    }
    LaunchedEffect(done, text) {
        if (!done || !text) return@LaunchedEffect
        delay(1000)
        if (!calm) lift.animateTo(1f, tween(600, easing = Motion.Ease))
        onFinished()
    }
    Box(Modifier.fillMaxSize().background(p.canvas.copy(alpha = 0.94f)).pressable(scale = 1f, haptic = false) {}, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Envelope(
                lining, flap = flap.value, rise = letter.value, glow = seal.value * 0.6f,
                modifier = Modifier.size(width = 220.dp, height = 152.dp).graphicsLayer {
                    translationY = -lift.value * 800f; alpha = 1f - lift.value
                    val sc = 0.9f + 0.1f * seal.value; scaleX = sc; scaleY = sc
                },
            )
            Spacer(Modifier.height(Gap.xxl))
            AnimatedVisibility(text, enter = fadeIn() + slideInVertically { it / 2 }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sealed", style = K.type.serifLarge, color = p.ink)
                    Text("See you on ${Fmt.ordinalDate(date)}, ${date.year}.", style = K.type.body, color = p.inkSoft, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

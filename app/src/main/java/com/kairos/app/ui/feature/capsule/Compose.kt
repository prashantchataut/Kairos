package com.kairos.app.ui.feature.capsule

import android.Manifest
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.scaleOut
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.kairos.app.AppContainer
import com.kairos.app.data.repo.CapsuleDraft
import com.kairos.app.data.repo.DraftAttachment
import com.kairos.app.engine.Mood
import com.kairos.app.ui.art.Bit
import com.kairos.app.ui.art.Kai
import com.kairos.app.ui.art.burst
import com.kairos.app.ui.art.look
import com.kairos.app.ui.art.rememberBurst
import com.kairos.app.ui.components.Icons
import com.kairos.app.ui.components.KairosSlider
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.SliderBubble
import com.kairos.app.ui.components.at
import com.kairos.app.ui.components.pressable
import com.kairos.app.ui.feature.detail.TopBar
import com.kairos.app.ui.feature.today.tint
import com.kairos.app.ui.theme.Candy
import com.kairos.app.ui.theme.Gap
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.LightPalette
import com.kairos.app.ui.theme.Motion
import com.kairos.app.ui.theme.Radius
import com.kairos.app.ui.theme.SerifText
import com.kairos.app.ui.theme.Tint
import com.kairos.app.ui.theme.candy
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
import kotlin.math.roundToInt

enum class OpenIn(val label: String, val months: Long) { Month("1 month", 1), Season("3 months", 3), Half("6 months", 6), Year("1 year", 12), Two("2 years", 24), Five("5 years", 60) }

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

private val openColors = listOf(Candy.Sky, Candy.Mint, Candy.Lilac, Candy.Bubblegum, Candy.Coral, Candy.Sun)
private val Paper = Color(0xFFFFF8EC)
private val PaperInk = Color(0xFF2A2140)

@Composable
fun ComposeLetter(onBack: () -> Unit, onSealed: () -> Unit, startStep: Int = 0) {
    val vm = appViewModel { ComposeViewModel(it) }
    val s by vm.state.collectAsStateWithLifecycle()
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

    // Three steps: write, tuck things in, choose when it opens. Then seal.
    var step by remember { mutableIntStateOf(startStep) }
    BackHandler(step > 0 && !s.sealing) { step-- }
    Box(Modifier.fillMaxSize().nightSky()) {
        Column(Modifier.fillMaxSize().imePadding()) {
            TopBar({ if (step > 0) step-- else if (s.dirty) confirm = true else onBack() }, onSky = true) { StepDots(step) }
            AnimatedContent(
                step, Modifier.weight(1f),
                transitionSpec = {
                    val dir = if (targetState > initialState) 1 else -1
                    (slideInHorizontally(Motion.offset) { it / 3 * dir } + fadeIn(Motion.fade(280)) + scaleIn(Motion.bloom(), initialScale = 0.96f)) togetherWith
                        (slideOutHorizontally(Motion.offset) { -it / 3 * dir } + fadeOut(Motion.fade(160)) + scaleOut(targetScale = 0.96f))
                },
                label = "step",
            ) { st ->
                when (st) {
                    0 -> WriteStep(s, vm)
                    1 -> TuckStep(s, vm, recording, recStart,
                        onPhotos = { photos.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) },
                        onFiles = { files.launch(arrayOf("*/*")) },
                        onMic = {
                            if (recording || ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) toggleRec()
                            else mic.launch(Manifest.permission.RECORD_AUDIO)
                        })
                    else -> WhenStep(s, vm) { pickDate = true }
                }
            }
            Row(Modifier.navigationBarsPadding().padding(Gap.gutter).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Gap.md), verticalAlignment = Alignment.CenterVertically) {
                if (step < 2) {
                    PrimaryButton(
                        if (step == 0) (if (s.letter.isBlank()) "Write a little first" else "Next: tuck things in") else "Next: choose when",
                        { step++ }, Modifier.weight(1f), icon = Icons.Arrow, enabled = step == 1 || s.letter.isNotBlank() || s.attachments.isNotEmpty(), onSky = true,
                    )
                } else {
                    Row(
                        Modifier.weight(1f).height(56.dp).pressable(enabled = s.canSeal, label = "Seal the letter", onClick = vm::seal).clip(CircleShape)
                            .background(if (s.canSeal) Candy.Sun else Color.White.copy(alpha = 0.15f)),
                        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Lock, null, tint = Candy.Navy, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(Gap.sm))
                        Text("Seal it until ${Fmt.ordinalDate(s.date)}", style = K.type.label.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold), color = Candy.Navy)
                    }
                }
            }
        }
        AnimatedVisibility(s.sealing, enter = fadeIn(Motion.fade(220)), exit = fadeOut()) { Sealing(LightPalette.candy(s.tint), s.date, s.sealed, onSealed) }
    }

    if (confirm) AlertDialog(
        onDismissRequest = { confirm = false },
        title = { Text("Discard this letter?", style = K.type.title) },
        text = { Text("What you've written won't be saved.", style = K.type.body) },
        confirmButton = { TextButton({ confirm = false; onBack() }) { Text("Discard", color = K.colors.negative) } },
        dismissButton = { TextButton({ confirm = false }) { Text("Keep writing") } },
        containerColor = K.colors.surface,
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
private fun NightChip(text: String, on: Boolean, icon: ImageVector? = null, onClick: () -> Unit) {
    Row(
        Modifier.pressable(scale = 0.94f, onClick = onClick).clip(CircleShape).background(if (on) Color.White else Color.White.copy(alpha = 0.12f))
            .border(1.dp, Color.White.copy(alpha = if (on) 1f else 0.3f), CircleShape).padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) { Icon(icon, null, tint = if (on) Candy.Navy else Color.White, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(6.dp)) }
        Text(text, style = K.type.label, color = if (on) Candy.Navy else Color.White)
    }
}

@Composable
private fun BarIcon(icon: ImageVector, label: String, tint: Color = Color.White, onClick: () -> Unit) {
    Box(Modifier.size(48.dp).pressable(label = label, onClick = onClick), contentAlignment = Alignment.Center) { Icon(icon, label, tint = tint, modifier = Modifier.size(23.dp)) }
}

@Composable
private fun RecTimer(start: Long) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(start) { while (true) { delay(250); now = System.currentTimeMillis() } }
    val secs = (now - start) / 1000
    val pulse by rememberInfiniteTransition(label = "rec").animateFloat(0.35f, 1f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "p")
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(9.dp).graphicsLayer { alpha = pulse }.clip(CircleShape).background(Candy.Coral))
        Spacer(Modifier.width(6.dp))
        Text("${secs / 60}:${"%02d".format(secs % 60)}", style = K.type.label, color = Color.White)
    }
}

/**
 * The sealing ceremony: the letter slides down into its glass folder, a wax bloom stamps it shut,
 * dandelion seeds drift up and away (r25), and the folder floats off into the night.
 */
@Composable
private fun Sealing(paper: Color, date: LocalDate, done: Boolean, onFinished: () -> Unit) {
    val calm = K.calm
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    val rise = remember { Animatable(if (calm) 0f else 1.7f) }
    val seal = remember { Animatable(if (calm) 1f else 0f) }
    val lift = remember { Animatable(0f) }
    val seeds = rememberBurst()
    var text by remember { mutableStateOf(calm) }
    LaunchedEffect(Unit) {
        if (calm) return@LaunchedEffect
        delay(150)
        rise.animateTo(0f, tween(760, easing = Motion.InOut))
        seal.snapTo(1.6f)
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        launch { seal.animateTo(1f, Motion.bloom()) }
        launch {
            seeds.fire(
                with(density) { Offset(130.dp.toPx(), 110.dp.toPx()) }, kinds = listOf(Bit.Seed), colors = listOf(Color.White, Color.White.copy(alpha = 0.7f)),
                count = 16, spread = 140f, direction = -90f, power = 1.5f, gravity = -0.6f, durationMs = 1800,
            )
        }
        delay(250)
        text = true
    }
    LaunchedEffect(done, text) {
        if (!done || !text) return@LaunchedEffect
        delay(1400)
        if (!calm) lift.animateTo(1f, tween(650, easing = Motion.InOut))
        onFinished()
    }
    Box(Modifier.fillMaxSize().nightSky().pressable(scale = 1f, haptic = false) {}, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(width = 260.dp, height = 220.dp).burst(seeds).graphicsLayer {
                    translationY = -lift.value * 900f; alpha = 1f - lift.value
                    rotationZ = lift.value * -8f
                },
            ) { GlassFolder(paper, Modifier.fillMaxSize(), rise = rise.value, seal = seal.value, papers = 1) }
            Spacer(Modifier.height(Gap.xxl))
            AnimatedVisibility(text, enter = fadeIn(Motion.fade(400)) + slideInVertically(Motion.offset) { it / 2 }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sealed.", style = K.type.shout, color = Color.White)
                    Spacer(Modifier.height(Gap.xs))
                    Text("See you on ${Fmt.ordinalDate(date)}, ${date.year}.", style = K.type.body, color = Color.White.copy(alpha = 0.75f), textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun StepDots(step: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        listOf("Write", "Tuck in", "When").forEachIndexed { i, label ->
            val on = i == step
            val w by androidx.compose.animation.core.animateDpAsState(if (on) 64.dp else 8.dp, Motion.soft(), label = "dot")
            Box(Modifier.padding(horizontal = 3.dp).height(26.dp).width(w).clip(CircleShape).background(if (i <= step) Color.White else Color.White.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                if (on) Text(label, style = K.type.caption.copy(fontWeight = FontWeight.Bold), color = Candy.Navy, maxLines = 1)
            }
        }
    }
}

/** Step 1: just you and the paper. Prompts sit above as chips you can tap to borrow. */
@Composable
private fun WriteStep(s: ComposeState, vm: ComposeViewModel) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("Dear future me…", style = K.type.shout.copy(fontSize = 34.sp), color = Color.White, modifier = Modifier.padding(horizontal = Gap.gutter))
        Spacer(Modifier.height(Gap.sm))
        Row(Modifier.padding(horizontal = Gap.gutter), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Sparkle, null, tint = Candy.Sun, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(s.prompt, style = K.type.label, color = Color.White.copy(alpha = 0.85f), modifier = Modifier.weight(1f).pressable(onClick = vm::usePrompt), maxLines = 2)
            Spacer(Modifier.width(Gap.sm))
            Text("Another", style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = Candy.Sun, modifier = Modifier.pressable(onClick = vm::nextPrompt).padding(Gap.xs))
        }
        Spacer(Modifier.height(Gap.lg))
        LetterPaper(LightPalette.candy(s.tint), "TODAY\n" + Fmt.shortDate(System.currentTimeMillis()).uppercase(), Modifier.fillMaxWidth().padding(horizontal = Gap.gutter).heightIn(min = 460.dp), firstLine = 112.dp) {
            Column(Modifier.padding(start = 52.dp, end = 20.dp, top = 22.dp, bottom = 26.dp)) {
                Box(Modifier.padding(end = 112.dp)) {
                    if (s.title.isEmpty()) Text("A name for it, if you like", style = K.type.title, color = PaperInk.copy(alpha = 0.35f))
                    BasicTextField(s.title, vm::title, singleLine = true, textStyle = K.type.title.copy(color = PaperInk), cursorBrush = SolidColor(Candy.Coral), modifier = Modifier.fillMaxWidth())
                }
                Spacer(Modifier.height(34.dp))
                Box(Modifier.fillMaxWidth().heightIn(min = 340.dp)) {
                    val letterStyle = K.type.body.copy(fontFamily = SerifText, fontSize = 19.sp, lineHeight = 31.sp, color = PaperInk)
                    if (s.letter.isEmpty()) Text("Start anywhere. What's on your mind today? What do you hope is true when you read this?", style = letterStyle.copy(color = PaperInk.copy(alpha = 0.35f)))
                    BasicTextField(s.letter, vm::letter, textStyle = letterStyle, cursorBrush = SolidColor(Candy.Coral), modifier = Modifier.fillMaxWidth().heightIn(min = 340.dp))
                }
                Text("${s.letter.split(Regex("\\s+")).count { it.isNotBlank() }} words", style = K.type.caption, color = PaperInk.copy(alpha = 0.4f))
            }
        }
        Spacer(Modifier.height(Gap.xl))
    }
}

/** Step 2: things slipped into the envelope (n02, n03): photos, files, a voice note, how you feel, a colour. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TuckStep(s: ComposeState, vm: ComposeViewModel, recording: Boolean, recStart: Long, onPhotos: () -> Unit, onFiles: () -> Unit, onMic: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = Gap.gutter)) {
        Text("Tuck something in", style = K.type.shout.copy(fontSize = 34.sp), color = Color.White)
        Text("A photo of today, a voice note, a ticket stub. Future you will love the details.", style = K.type.body, color = Color.White.copy(alpha = 0.8f))
        Spacer(Modifier.height(Gap.lg))
        // The open folder with everything tucked in rising out of it.
        Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.BottomCenter) {
            GlassFolder(LightPalette.candy(s.tint), Modifier.size(width = 250.dp, height = 190.dp), rise = 0.25f + 0.08f * s.attachments.size.coerceAtMost(4), sealed = false, papers = (1 + s.attachments.size).coerceAtMost(3))
        }
        if (s.attachments.isNotEmpty()) {
            LazyRow(contentPadding = PaddingValues(vertical = Gap.lg), horizontalArrangement = Arrangement.spacedBy(Gap.lg)) {
                itemsIndexed(s.attachments, key = { _, a -> a.uri.toString() }) { i, a ->
                    AttachmentCard(a.kind, a.uri, a.name, tiltFor(i), onRemove = { vm.remove(a) }, modifier = Modifier.animateItem())
                }
            }
        } else Spacer(Modifier.height(Gap.lg))
        Row(horizontalArrangement = Arrangement.spacedBy(Gap.md)) {
            TuckButton(Icons.Image, "Photos", Modifier.weight(1f), onClick = onPhotos)
            TuckButton(if (recording) Icons.Stop else Icons.Mic, if (recording) "Stop" else "Voice note", Modifier.weight(1f), active = recording, onClick = onMic)
            TuckButton(Icons.Folder, "A file", Modifier.weight(1f), onClick = onFiles)
        }
        if (recording) { Spacer(Modifier.height(Gap.sm)); RecTimer(recStart) }
        Spacer(Modifier.height(Gap.xl))
        Text("How are you right now?", style = K.type.title, color = Color.White)
        Text("Future you will see it next to how they feel.", style = K.type.caption, color = Color.White.copy(alpha = 0.65f))
        Spacer(Modifier.height(Gap.md))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Mood.entries.forEach { m ->
                val on = s.mood == m
                val sc by animateFloatAsState(if (on) 1.15f else if (s.mood == null) 1f else 0.85f, Motion.bloom(), label = "mood")
                Column(Modifier.pressable(label = m.label) { vm.mood(m) }, horizontalAlignment = Alignment.CenterHorizontally) {
                    Kai(m.look(), size = 54.dp, shadow = false, modifier = Modifier.graphicsLayer { scaleX = sc; scaleY = sc; alpha = if (s.mood == null || on) 1f else 0.55f })
                    Text(m.label, style = K.type.caption, color = Color.White.copy(alpha = if (on) 1f else 0.6f))
                }
            }
        }
        Spacer(Modifier.height(Gap.xl))
        Text("Envelope colour", style = K.type.title, color = Color.White)
        Spacer(Modifier.height(Gap.sm))
        Row(horizontalArrangement = Arrangement.spacedBy(Gap.md)) {
            Tint.entries.forEach { t ->
                val on = t == s.tint
                val sc by animateFloatAsState(if (on) 1.15f else 1f, Motion.bloom(), label = "tint")
                Box(Modifier.size(38.dp).graphicsLayer { scaleX = sc; scaleY = sc }.pressable(label = t.name) { vm.tint(t) }.clip(CircleShape).background(LightPalette.candy(t)).border(if (on) 3.dp else 0.dp, Color.White, CircleShape))
            }
        }
        Spacer(Modifier.height(Gap.xl))
    }
}

@Composable
private fun TuckButton(icon: ImageVector, label: String, modifier: Modifier, active: Boolean = false, onClick: () -> Unit) {
    Column(
        modifier.height(84.dp).pressable(scale = 0.93f, label = label, onClick = onClick).clip(RoundedCornerShape(20.dp))
            .background(if (active) Candy.Coral.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.12f)).border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(20.dp)),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, tint = if (active) Candy.Coral else Color.White, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(6.dp))
        Text(label, style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
    }
}

/** Step 3: when it opens. A huge date on the night sky; slide through the presets, or pick a day. */
@Composable
private fun WhenStep(s: ComposeState, vm: ComposeViewModel, pickDay: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Column(Modifier.padding(horizontal = Gap.gutter)) {
            Text("When should it open?", style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = Color.White.copy(alpha = 0.75f))
            Spacer(Modifier.height(Gap.sm))
            AnimatedContent(s.date, transitionSpec = { (fadeIn(Motion.fade(240)) + slideInVertically(Motion.offset) { it / 4 }) togetherWith (fadeOut(Motion.fade(120)) + slideOutVertically(Motion.offset) { -it / 4 }) }, label = "date") { d ->
                FadingTitle(Fmt.ordinalDate(d), "${d.year}", size = 62)
            }
            Text(Fmt.until(s.date.atTime(9, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()).replaceFirstChar { it.uppercase() } + ", at nine in the morning.", style = K.type.body, color = Color.White.copy(alpha = 0.75f))
        }
        Spacer(Modifier.height(Gap.xl))
        val idx = s.preset?.ordinal ?: OpenIn.Year.ordinal
        KairosSlider(
            idx / 5f, { vm.preset(OpenIn.entries[(it * 5).roundToInt()]) }, "Open the letter in", { OpenIn.entries[(it * 5).roundToInt()].label },
            Modifier.padding(horizontal = 4.dp), stops = 6, colors = openColors, onDark = true,
            bubble = { f -> SliderBubble(if (s.preset == null) "Your day" else OpenIn.entries[(f * 5).roundToInt()].label, openColors.at(f).let { androidx.compose.ui.graphics.lerp(it, Candy.Navy, 0.3f) }) },
        )
        Row(Modifier.padding(horizontal = Gap.gutter), verticalAlignment = Alignment.CenterVertically) {
            NightChip("Pick a day", s.preset == null, Icons.Calendar, pickDay)
        }
        Spacer(Modifier.height(Gap.xl))
        // A preview of the sealed letter as it will wait in the capsule.
        Row(Modifier.fillMaxWidth().padding(horizontal = Gap.gutter).clip(RoundedCornerShape(22.dp)).background(Color.White.copy(alpha = 0.08f)).padding(Gap.lg), verticalAlignment = Alignment.CenterVertically) {
            GlassFolder(LightPalette.candy(s.tint), Modifier.size(width = 96.dp, height = 80.dp), sticker = s.mood?.look(), papers = (1 + s.attachments.size).coerceAtMost(3))
            Spacer(Modifier.width(Gap.lg))
            Column(Modifier.weight(1f)) {
                Text(s.title.ifBlank { "A letter to future you" }, style = K.type.title, color = Color.White, maxLines = 2)
                Text("${s.letter.split(Regex("\\s+")).count { it.isNotBlank() }} words" + if (s.attachments.isNotEmpty()) ", ${s.attachments.size} tucked in" else "", style = K.type.caption, color = Color.White.copy(alpha = 0.65f))
                Text("Stays on this phone. Nobody else can read it.", style = K.type.caption, color = Color.White.copy(alpha = 0.65f))
            }
        }
        Spacer(Modifier.height(Gap.xl))
    }
}

package com.kairos.app.ui.screens.capsule

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
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.ui.components.IconBubble
import com.kairos.app.ui.components.MoodPicker
import com.kairos.app.ui.components.PaintBurst
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.SelectChip
import com.kairos.app.ui.components.squishClickable
import com.kairos.app.ui.glass.GlassPanel
import com.kairos.app.ui.glass.liquidGlass
import com.kairos.app.ui.icons.KairosIcons
import com.kairos.app.ui.navigation.Scene
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Motion
import com.kairos.app.ui.theme.Radii
import com.kairos.app.ui.theme.Space
import com.kairos.app.ui.theme.Wash
import com.kairos.app.util.AudioRecorder
import com.kairos.app.util.Fmt
import com.kairos.app.util.appViewModel
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun CapsuleComposeScreen(onBack: () -> Unit, onSealed: () -> Unit) {
    val vm = appViewModel { CapsuleComposeViewModel(it) }
    val s by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val palette = Kairos.colors
    Scene(s.wash, Wash.Rose, Wash.Cerulean, s.wash, Wash.Peach)

    var confirmDiscard by remember { mutableStateOf(false) }
    var pickDate by remember { mutableStateOf(false) }
    BackHandler(enabled = s.dirty && !s.sealing) { confirmDiscard = true }

    val photos = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(8)) { vm.addUris(it) }
    val files = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { vm.addUris(it) }
    val recorder = remember { AudioRecorder(context) }
    var recording by remember { mutableStateOf(false) }
    var recordStart by remember { mutableLongStateOf(0L) }
    DisposableEffect(Unit) { onDispose { recorder.release() } }
    fun toggleRecord() {
        if (recording) {
            recorder.stop()?.let { (file, ms) -> vm.addVoice(Uri.fromFile(file), ms, file.length()) }
            recording = false
        } else if (recorder.start()) {
            recording = true
            recordStart = System.currentTimeMillis()
        }
    }
    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) toggleRecord() }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(bottom = 120.dp)
        ) {
            Spacer(Modifier.windowInsetsPadding(WindowInsets.statusBars).height(68.dp))
            // Big date header — the day this letter will open.
            Column(Modifier.padding(horizontal = Space.gutter)) {
                Text("Opens on", style = Kairos.type.body, color = palette.inkSoft)
                AnimatedContent(s.openDate, transitionSpec = { (fadeIn() + slideInVertically { it / 3 }) togetherWith fadeOut() }, label = "date") { d ->
                    Column {
                        Text(Fmt.ordinalDate(d), style = Kairos.type.hero, color = palette.ink)
                        Text("${d.year}", style = Kairos.type.hero, color = palette.inkFaint)
                    }
                }
            }
            Spacer(Modifier.height(Space.lg))
            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Space.gutter),
                horizontalArrangement = Arrangement.spacedBy(Space.sm),
            ) {
                itemsIndexed(OpenIn.entries.toList()) { _, p -> SelectChip(p.label, s.preset == p, { vm.setPreset(p) }) }
                item {
                    SelectChip("Pick a day", s.preset == null, { pickDate = true }, leading = {
                        Icon(KairosIcons.Calendar, null, Modifier.size(16.dp), tint = if (s.preset == null) palette.paper else palette.ink)
                    })
                }
            }
            Spacer(Modifier.height(Space.xl))
            // The letter itself: handwriting on ruled paper.
            LetterPaper(
                title = s.title, onTitle = vm::setTitle, letter = s.letter, onLetter = vm::setLetter,
                hint = s.prompt, modifier = Modifier.padding(horizontal = Space.gutter),
            )
            Spacer(Modifier.height(Space.md))
            Row(
                Modifier.padding(horizontal = Space.gutter).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(KairosIcons.Sparkle, null, tint = palette.warning, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(Space.sm))
                Text("Need a nudge? Use today's prompt", style = Kairos.type.label, color = palette.accent, modifier = Modifier.squishClickable(onClick = vm::usePrompt))
                Spacer(Modifier.weight(1f))
                Text("Another", style = Kairos.type.label, color = palette.inkSoft, modifier = Modifier.squishClickable(onClick = vm::nextPrompt))
            }
            if (s.attachments.isNotEmpty()) {
                Spacer(Modifier.height(Space.xl))
                LazyRow(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Space.gutter, vertical = Space.lg),
                    horizontalArrangement = Arrangement.spacedBy(Space.lg),
                ) {
                    itemsIndexed(s.attachments, key = { _, a -> a.uri.toString() }) { i, a ->
                        AttachmentChip(a.kind, a.uri, a.name, tiltFor(i), onRemove = { vm.remove(a) }, modifier = Modifier.animateItem())
                    }
                }
            }
            Spacer(Modifier.height(Space.xl))
            GlassPanel(Modifier.padding(horizontal = Space.gutter).fillMaxWidth(), blur = false) {
                Column(Modifier.padding(Space.xl)) {
                    Text("How are you, right now?", style = Kairos.type.title, color = palette.ink)
                    Text("Future you will see it next to how they feel.", style = Kairos.type.label, color = palette.inkSoft)
                    Spacer(Modifier.height(Space.lg))
                    MoodPicker(s.mood, vm::setMood)
                    Spacer(Modifier.height(Space.xl))
                    Text("Envelope colour", style = Kairos.type.label, color = palette.inkSoft)
                    Spacer(Modifier.height(Space.sm))
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                        Wash.entries.forEach { w ->
                            Box(
                                Modifier
                                    .size(36.dp)
                                    .squishClickable(label = w.name) { vm.setWash(w) }
                                    .background(w.color(palette.isDark), CircleShape)
                                    .border(if (w == s.wash) 3.dp else 1.dp, if (w == s.wash) palette.ink else palette.glassRimTop, CircleShape)
                            )
                        }
                    }
                }
            }
        }

        // Top bar.
        Row(
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = Space.gutter, vertical = Space.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBubble(KairosIcons.Close, "Close", { if (s.dirty) confirmDiscard = true else onBack() }, blur = true)
            Spacer(Modifier.weight(1f))
            Text("New letter", style = Kairos.type.title, color = palette.ink)
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(48.dp))
        }

        // Composer bar, like a message field: attach, record, seal.
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = Space.gutter, vertical = Space.md)
                .fillMaxWidth()
                .liquidGlass(CircleShape, blur = true, elevation = 18.dp)
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            BarIcon(KairosIcons.Image, "Add photos") {
                photos.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
            }
            BarIcon(KairosIcons.Clip, "Attach files") { files.launch(arrayOf("*/*")) }
            BarIcon(if (recording) KairosIcons.Stop else KairosIcons.Mic, if (recording) "Stop recording" else "Record a voice note", tint = if (recording) palette.danger else palette.ink) {
                if (recording || ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) toggleRecord()
                else micPermission.launch(Manifest.permission.RECORD_AUDIO)
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                if (recording) RecordingTimer(recordStart)
            }
            PrimaryButton("Seal", vm::seal, icon = KairosIcons.Send, enabled = s.canSeal)
        }

        AnimatedVisibility(s.sealing, enter = fadeIn(), exit = fadeOut()) {
            SealingOverlay(s.wash.color(palette.isDark), s.openDate, done = s.sealed, onFinished = onSealed)
        }
    }

    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            title = { Text("Discard this letter?", style = Kairos.type.title) },
            text = { Text("What you've written won't be saved.", style = Kairos.type.body) },
            confirmButton = { TextButton({ confirmDiscard = false; onBack() }) { Text("Discard", color = palette.danger) } },
            dismissButton = { TextButton({ confirmDiscard = false }) { Text("Keep writing") } },
            containerColor = palette.paper,
        )
    }

    if (pickDate) {
        val tomorrowUtc = LocalDate.now().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val state = rememberDatePickerState(
            initialSelectedDateMillis = s.openDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= tomorrowUtc
                override fun isSelectableYear(year: Int) = year >= LocalDate.now().year
            },
        )
        DatePickerDialog(
            onDismissRequest = { pickDate = false },
            confirmButton = {
                TextButton({
                    state.selectedDateMillis?.let { vm.setDate(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) }
                    pickDate = false
                }) { Text("Choose") }
            },
            dismissButton = { TextButton({ pickDate = false }) { Text("Cancel") } },
        ) { DatePicker(state) }
    }
}

@Composable
private fun BarIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, tint: Color = Kairos.colors.ink, onClick: () -> Unit) {
    Box(Modifier.size(48.dp).squishClickable(label = label, onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, label, tint = tint, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun RecordingTimer(start: Long) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(start) { while (true) { delay(250); now = System.currentTimeMillis() } }
    val secs = (now - start) / 1000
    val pulse by rememberInfiniteTransition(label = "rec").animateFloat(0.4f, 1f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "p")
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).graphicsLayer { alpha = pulse }.background(Kairos.colors.danger, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text("${secs / 60}:${"%02d".format(secs % 60)}", style = Kairos.type.label, color = Kairos.colors.ink)
    }
}

/** Handwritten letter on cream, ruled like a notebook. The rules line up with Caveat's line height. */
@Composable
fun LetterPaper(
    title: String,
    onTitle: (String) -> Unit,
    letter: String,
    onLetter: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
) {
    val hand = Kairos.type.hand
    val ink = Color(0xFF22305A)
    val lineH = with(LocalDensity.current) { hand.lineHeight.toPx() }
    Column(
        modifier
            .fillMaxWidth()
            .liquidGlass(Radii.Card, blur = false, tint = Color(0xFFFFFCF6), elevation = 16.dp)
            .padding(Space.xl)
    ) {
        Box {
            if (title.isEmpty()) Text("Give it a name (optional)", style = Kairos.type.title, color = Color(0xFFA9B1C6))
            BasicTextField(
                title, onTitle, singleLine = true,
                textStyle = Kairos.type.title.copy(color = ink),
                cursorBrush = SolidColor(Color(0xFF3461E8)),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(Space.md))
        Text("Dear future me,", style = hand, color = ink)
        Box(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 260.dp)
                .drawBehind {
                    var y = lineH - 4f
                    while (y < size.height) {
                        drawLine(Color(0xFF8EC5FF).copy(alpha = 0.45f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1.5f)
                        y += lineH
                    }
                }
        ) {
            if (letter.isEmpty()) Text(hint, style = hand, color = Color(0xFFA9B1C6))
            BasicTextField(
                letter, onLetter,
                textStyle = hand.copy(color = ink),
                cursorBrush = SolidColor(Color(0xFF3461E8)),
                modifier = Modifier.fillMaxWidth().heightIn(min = 260.dp),
            )
        }
    }
}

/**
 * The sealing moment: the letter slides into the envelope, the flap folds down, the wax seal
 * stamps on with a squish, and the envelope floats away to wait for its day.
 */
@Composable
private fun SealingOverlay(color: Color, date: LocalDate, done: Boolean, onFinished: () -> Unit) {
    val palette = Kairos.colors
    val reduced = Kairos.reducedMotion
    val letter = remember { Animatable(1f) }
    val flap = remember { Animatable(1f) }
    val seal = remember { Animatable(0f) }
    val lift = remember { Animatable(0f) }
    var burst by remember { mutableStateOf(0) }
    var showText by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (reduced) { letter.snapTo(0f); flap.snapTo(0f); seal.snapTo(1f); showText = true; return@LaunchedEffect }
        letter.animateTo(0f, tween(600, easing = Motion.Emphasized))
        flap.animateTo(0f, tween(420, easing = Motion.Emphasized))
        seal.animateTo(1f, Motion.squish())
        burst++
        showText = true
    }
    LaunchedEffect(done, showText) {
        if (!done || !showText) return@LaunchedEffect
        delay(1100)
        if (!reduced) lift.animateTo(1f, tween(650, easing = LinearEasing))
        onFinished()
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(palette.paper.copy(alpha = 0.78f))
            .squishClickable(haptic = false, pressedScale = 1f) {},
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                if (burst > 0) PaintBurst(burst, color, Modifier.size(320.dp), count = 16)
                Envelope(
                    color,
                    flap = flap.value,
                    letterRise = letter.value,
                    sealCrack = 0f,
                    glow = seal.value,
                    modifier = Modifier
                        .size(width = 230.dp, height = 160.dp)
                        .graphicsLayer {
                            translationY = -lift.value * 900f
                            rotationZ = -lift.value * 12f
                            alpha = 1f - lift.value
                        },
                )
            }
            Spacer(Modifier.height(Space.xxl))
            AnimatedVisibility(showText, enter = fadeIn() + slideInVertically { it / 2 }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sealed", style = Kairos.type.display, color = palette.ink)
                    Text(
                        "See you on ${Fmt.ordinalDate(date)}, ${date.year}.",
                        style = Kairos.type.body, color = palette.inkSoft, textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}


package com.kairos.app.ui.capsule

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Stop
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.kairos.app.container
import com.kairos.app.engine.capsule.AttachmentKind
import com.kairos.app.engine.capsule.DraftAttachment
import com.kairos.app.engine.capsule.VoiceRecorder
import com.kairos.app.engine.content.Prompt
import com.kairos.app.engine.critters.DewReason
import com.kairos.app.engine.daily.Mood
import com.kairos.app.ui.art.Kiri
import com.kairos.app.ui.components.DetailTopBar
import com.kairos.app.ui.components.Eyebrow
import com.kairos.app.ui.components.SectionTitle
import com.kairos.app.ui.components.clickableNoRipple
import com.kairos.app.ui.glass.ButtonTone
import com.kairos.app.ui.glass.GlassCard
import com.kairos.app.ui.glass.GlassWeight
import com.kairos.app.ui.glass.KButton
import com.kairos.app.ui.glass.KChip
import com.kairos.app.ui.glass.liquidGlass
import com.kairos.app.ui.nav.LocalNavigator
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Motion
import com.kairos.app.ui.theme.Radii
import com.kairos.app.ui.theme.Space
import com.kairos.app.ui.today.MoodFace
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

private enum class When(val label: String, val days: Long) {
    Week("1 week", 7), Month("1 month", 30), Season("6 months", 182), Year("1 year", 365), Custom("Pick a date", -1),
}

@Composable
fun CapsuleComposeScreen() {
    val nav = LocalNavigator.current
    val context = LocalContext.current
    val c = context.container
    val colors = Kairos.colors
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }
    var choice by remember { mutableStateOf(When.Month) }
    var customDay by remember { mutableStateOf<LocalDate?>(null) }
    var showPicker by remember { mutableStateOf(false) }
    var mood by remember { mutableStateOf<Mood?>(null) }
    var stamp by remember { mutableIntStateOf(0) }
    val attachments = remember { mutableStateListOf<DraftAttachment>() }
    var prompts by remember { mutableStateOf<List<Prompt>>(emptyList()) }
    var sealing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        prompts = c.content.prompts().shuffled().take(6)
        mood = c.moods.observeToday().first()
    }

    val unlockDay: LocalDate = when (choice) {
        When.Custom -> customDay ?: LocalDate.now().plusMonths(1)
        else -> LocalDate.now().plusDays(choice.days)
    }
    val unlockAt = unlockDay.atTime(LocalTime.of(9, 0)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    val media = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) { uris ->
        uris.forEach { attachments.add(c.capsules.describe(it)) }
    }
    val files = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris.forEach { attachments.add(c.capsules.describe(it)) }
    }
    val notifPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    // voice note
    val recorder = remember { VoiceRecorder(context) }
    var recording by remember { mutableStateOf(false) }
    var recordStart by remember { mutableLongStateOf(0L) }
    var level by remember { mutableFloatStateOf(0f) }
    DisposableEffect(Unit) { onDispose { recorder.release() } }
    fun startRecording() {
        if (recorder.start(c.capsules.newVoiceFile())) {
            recording = true
            recordStart = System.currentTimeMillis()
        } else error = "Couldn't start the microphone."
    }
    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startRecording() else error = "Kairos needs the microphone only while you record a voice note."
    }
    LaunchedEffect(recording) {
        while (recording) {
            level = (recorder.amplitude() / 12000f).coerceIn(0f, 1f)
            delay(90)
        }
    }

    fun seal() {
        if (body.isBlank() && attachments.isEmpty()) {
            error = "Write a few words or add something first."
            return
        }
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        sealing = true
        scope.launch {
            val text = if (prompt.isNotBlank()) "$prompt\n\n$body" else body
            c.capsules.seal(title, text, unlockAt, (mood ?: Mood.Okay).ordinal, stamp, prompt, attachments.toList())
            c.pond.award(DewReason.Capsule)
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.ime)) {
            DetailTopBar(onBack = nav::back, title = "New letter")
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Space.screen),
            ) {
                Eyebrow("To: me, on ${unlockDay.format(java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy"))}")
                Spacer(Modifier.height(Space.sm))
                Box {
                    if (title.isEmpty()) Text("Give it a name", style = Kairos.type.title, color = colors.inkFaint)
                    BasicTextField(
                        title, { title = it.take(80) }, singleLine = true,
                        textStyle = Kairos.type.title.copy(color = colors.ink), cursorBrush = SolidColor(colors.pond),
                        modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Letter title" },
                    )
                }
                Spacer(Modifier.height(Space.lg))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                    prompts.forEach { p -> KChip(p.text, prompt == p.text, { prompt = if (prompt == p.text) "" else p.text }) }
                }
                Spacer(Modifier.height(Space.md))
                GlassCard(Modifier.fillMaxWidth().heightIn(min = 240.dp), blur = true, weight = GlassWeight.Frosted, shape = Radii.sheet) {
                    Column {
                        AnimatedVisibility(prompt.isNotBlank()) {
                            Text(prompt, style = Kairos.type.label, color = colors.pond, modifier = Modifier.padding(bottom = Space.sm))
                        }
                        Box {
                            if (body.isEmpty()) Text("Dear future me,…", style = Kairos.type.quote, color = colors.inkFaint)
                            BasicTextField(
                                body, { body = it },
                                textStyle = Kairos.type.quote.copy(color = colors.ink),
                                cursorBrush = SolidColor(colors.pond),
                                modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp).semantics { contentDescription = "Your letter" },
                            )
                        }
                    }
                }

                SectionTitle("Tuck something inside")
                Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                    KChip("Photos", false, { media.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) }, leading = Icons.Rounded.Image)
                    KChip("Files", false, { files.launch(arrayOf("*/*")) }, leading = Icons.Rounded.AttachFile)
                    KChip(if (recording) "Stop" else "Voice", recording, {
                        if (recording) {
                            recorder.stop()?.let { (f, d) ->
                                attachments.add(DraftAttachment(file = f, name = "Voice note.m4a", mime = "audio/mp4", bytes = f.length(), kind = AttachmentKind.Audio, durationMs = d))
                            }
                            recording = false
                        } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                            startRecording()
                        } else micPermission.launch(Manifest.permission.RECORD_AUDIO)
                    }, leading = if (recording) Icons.Rounded.Stop else Icons.Rounded.Mic)
                }
                AnimatedVisibility(recording) { RecordingStrip(recordStart, level) }
                if (attachments.isNotEmpty()) {
                    Spacer(Modifier.height(Space.md))
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                        attachments.forEachIndexed { i, a -> DraftTile(a) { attachments.removeAt(i) } }
                    }
                }

                SectionTitle("When should it arrive?")
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                    When.entries.forEach { w ->
                        KChip(
                            if (w == When.Custom && customDay != null) customDay!!.format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy")) else w.label,
                            w == choice,
                            { if (w == When.Custom) showPicker = true else choice = w },
                        )
                    }
                }

                SectionTitle("How do you feel, writing this?")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Mood.entries.forEach { m ->
                        Box(
                            Modifier.size(46.dp)
                                .border(2.dp, if (m == mood) colors.pond else Color.Transparent, CircleShape)
                                .padding(3.dp)
                                .liquidGlass(CircleShape, tint = com.kairos.app.ui.art.moodColor(m).copy(alpha = 0.85f))
                                .clickableNoRipple { mood = m }
                                .semantics { contentDescription = m.label },
                            contentAlignment = Alignment.Center,
                        ) { MoodFace(m, Modifier.size(24.dp)) }
                    }
                }

                SectionTitle("Choose a seal")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StampTints.indices.forEach { i ->
                        val sel = i == stamp
                        val scale = if (sel) 1.12f else 1f
                        Box(
                            Modifier.size(50.dp).graphicsLayer { scaleX = scale; scaleY = scale }
                                .clickableNoRipple { stamp = i; haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove) }
                                .semantics { contentDescription = "${StampNames[i]} seal" },
                        ) {
                            androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                                drawWaxSeal(center, size.minDimension * 0.42f, StampTints[i], i)
                                if (sel) drawCircle(colors.pond, size.minDimension * 0.5f, center, style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
                            }
                        }
                    }
                }
                error?.let {
                    Spacer(Modifier.height(Space.md))
                    Text(it, style = Kairos.type.body, color = colors.danger)
                }
                Spacer(Modifier.height(Space.xxl))
            }
            Box(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).padding(horizontal = Space.screen, vertical = Space.md)) {
                KButton("Seal until ${unlockDay.format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy"))}", ::seal, modifier = Modifier.fillMaxWidth())
            }
        }

        AnimatedVisibility(sealing, enter = fadeIn(tween(300)), exit = fadeOut()) {
            SealingOverlay(stamp, unlockDay) { nav.back() }
        }
    }

    if (showPicker) {
        val tomorrow = LocalDate.now().plusDays(1)
        val state = rememberDatePickerState(
            initialSelectedDateMillis = (customDay ?: LocalDate.now().plusMonths(3)).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= tomorrow.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
                override fun isSelectableYear(year: Int) = year >= LocalDate.now().year && year <= LocalDate.now().year + 25
            },
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton({
                    state.selectedDateMillis?.let {
                        customDay = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        choice = When.Custom
                    }
                    showPicker = false
                }) { Text("Choose", style = Kairos.type.label) }
            },
            dismissButton = { TextButton({ showPicker = false }) { Text("Cancel", style = Kairos.type.label) } },
        ) { DatePicker(state) }
    }
}

@Composable
private fun RecordingStrip(start: Long, level: Float) {
    val colors = Kairos.colors
    var elapsed by remember { mutableLongStateOf(0L) }
    LaunchedEffect(start) { while (true) { elapsed = System.currentTimeMillis() - start; delay(250) } }
    val t = rememberInfiniteTransition(label = "rec")
    val pulse by t.animateFloat(0.6f, 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "pulse")
    Row(
        Modifier.padding(top = Space.md).fillMaxWidth().liquidGlass(Radii.pill, GlassWeight.Frosted).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(12.dp).graphicsLayer { scaleX = pulse; scaleY = pulse }.background(colors.danger, CircleShape))
        Spacer(Modifier.width(10.dp))
        Text("Recording %d:%02d".format(elapsed / 60000, (elapsed / 1000) % 60), style = Kairos.type.label, color = colors.ink)
        Spacer(Modifier.width(Space.md))
        Row(Modifier.weight(1f).height(24.dp), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
            repeat(18) { i ->
                val h = (0.2f + level * (0.5f + 0.5f * kotlin.math.sin(i * 1.3f + elapsed / 120f))).coerceIn(0.15f, 1f)
                Box(Modifier.width(3.dp).height(24.dp * h).background(colors.pond.copy(alpha = 0.7f), Radii.pill))
            }
        }
    }
}

@Composable
private fun DraftTile(a: DraftAttachment, onRemove: () -> Unit) {
    val colors = Kairos.colors
    Box(Modifier.size(96.dp).clip(Radii.card).liquidGlass(Radii.card, GlassWeight.Frosted)) {
        if (a.kind == AttachmentKind.Image && a.uri != null) {
            AsyncImage(a.uri, a.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Column(Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Icon(
                    when (a.kind) {
                        AttachmentKind.Video -> Icons.Rounded.Movie
                        AttachmentKind.Audio -> Icons.Rounded.AudioFile
                        else -> Icons.Rounded.Description
                    },
                    null, tint = colors.pond,
                )
                Text(
                    if (a.kind == AttachmentKind.Audio && a.durationMs > 0) "%d:%02d".format(a.durationMs / 60000, (a.durationMs / 1000) % 60) else a.name,
                    style = Kairos.type.caption, color = colors.ink, maxLines = 2, overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Box(
            Modifier.align(Alignment.TopEnd).padding(4.dp).size(26.dp).background(Color(0x991B2340), CircleShape).clickableNoRipple(onRemove)
                .semantics { contentDescription = "Remove ${a.name}" },
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Rounded.Close, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
    }
}

/** Letter folds into the envelope, the wax seal drops and sets, Kiri waves it off. */
@Composable
private fun SealingOverlay(stamp: Int, day: LocalDate, onDone: () -> Unit) {
    val colors = Kairos.colors
    val haptics = LocalHapticFeedback.current
    val letter = remember { Animatable(0f) }
    val flap = remember { Animatable(1f) }
    val seal = remember { Animatable(0f) }
    val fly = remember { Animatable(0f) }
    var caption by remember { mutableStateOf("Folding your letter…") }
    LaunchedEffect(Unit) {
        letter.animateTo(1f, tween(650, easing = Motion.Enter))
        flap.animateTo(0f, tween(450, easing = Motion.Move))
        caption = "Pressing the seal…"
        seal.snapTo(2.4f)
        seal.animateTo(1f, Motion.playful())
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        caption = "Sealed until ${day.format(java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy"))}"
        delay(900)
        fly.animateTo(1f, tween(700, easing = Motion.Drawer))
        onDone()
    }
    Box(
        Modifier.fillMaxSize().background(colors.paper.copy(alpha = 0.82f)).clickableNoRipple { },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(width = 260.dp, height = 300.dp), contentAlignment = Alignment.BottomCenter) {
                // the letter sliding down into the envelope
                Box(
                    Modifier
                        .size(width = 210.dp, height = 220.dp)
                        .graphicsLayer {
                            translationY = (-80 + 150 * letter.value).dp.toPx() - fly.value * 600.dp.toPx()
                            alpha = (1f - letter.value * 1.2f).coerceIn(0f, 1f)
                            rotationZ = -3f * (1 - letter.value)
                        }
                        .liquidGlass(Radii.small, GlassWeight.Frosted),
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(6) { Box(Modifier.fillMaxWidth(if (it == 5) 0.5f else 1f).height(6.dp).background(colors.inkFaint.copy(alpha = 0.3f), Radii.pill)) }
                    }
                }
                Envelope(
                    stamp, open = flap.value, glow = 0f, sealScale = seal.value,
                    modifier = Modifier
                        .size(width = 260.dp, height = 180.dp)
                        .graphicsLayer {
                            translationY = -fly.value * 700.dp.toPx()
                            rotationZ = fly.value * -12f
                        },
                )
            }
            Spacer(Modifier.height(Space.xl))
            Kiri(Mood.Glowing, size = 96.dp)
            Spacer(Modifier.height(Space.md))
            Text(caption, style = Kairos.type.heading, color = colors.ink, textAlign = TextAlign.Center)
            Text("Kiri will let you know when it arrives.", style = Kairos.type.bodyRegular, color = colors.inkSoft)
        }
    }
}

package com.kairos.app.ui.capsule

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.kairos.app.AppContainer
import com.kairos.app.data.db.AttachmentEntity
import com.kairos.app.engine.capsule.VoicePlayer
import com.kairos.app.engine.critters.DewReason
import com.kairos.app.engine.daily.Mood
import com.kairos.app.ui.art.Kiri
import com.kairos.app.ui.components.DetailTopBar
import com.kairos.app.ui.components.Eyebrow
import com.kairos.app.ui.components.SectionTitle
import com.kairos.app.ui.components.clickableNoRipple
import com.kairos.app.ui.components.kairosViewModel
import com.kairos.app.ui.glass.ButtonTone
import com.kairos.app.ui.glass.GlassCard
import com.kairos.app.ui.glass.GlassIconButton
import com.kairos.app.ui.glass.GlassWeight
import com.kairos.app.ui.glass.KButton
import com.kairos.app.ui.glass.liquidGlass
import com.kairos.app.ui.nav.LocalNavigator
import com.kairos.app.ui.nav.Routes
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Motion
import com.kairos.app.ui.theme.Radii
import com.kairos.app.ui.theme.Space
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class CapsuleViewModel(private val c: AppContainer, private val id: String) : ViewModel() {
    val capsule = c.capsules.observe(id).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val attachments = c.capsules.observeAttachments(id).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _body = MutableStateFlow<String?>(null)
    val body = _body.asStateFlow()

    fun load() = viewModelScope.launch { _body.value = c.capsules.read(id) }

    fun open() = viewModelScope.launch {
        c.capsules.markOpened(id)
        c.pond.award(DewReason.CapsuleOpened)
        _body.value = c.capsules.read(id)
    }

    fun delete(done: () -> Unit) = viewModelScope.launch { c.capsules.delete(id); done() }
    fun uri(a: AttachmentEntity) = c.games.uriFor(File(a.path))
}

@Composable
fun CapsuleOpenScreen(id: String) {
    val vm = kairosViewModel(key = "capsule-$id") { CapsuleViewModel(it, id) }
    val capsule by vm.capsule.collectAsStateWithLifecycle()
    val attachments by vm.attachments.collectAsStateWithLifecycle()
    val body by vm.body.collectAsStateWithLifecycle()
    val nav = LocalNavigator.current
    val now = rememberNow(1000)
    var confirmDelete by remember { mutableStateOf(false) }
    val c = capsule

    LaunchedEffect(c?.openedAt) { if (c?.openedAt != null) vm.load() }

    Column(Modifier.fillMaxSize()) {
        DetailTopBar(onBack = nav::back, actions = {
            if (c != null) GlassIconButton(Icons.Rounded.Delete, "Delete letter", { confirmDelete = true })
        })
        if (c == null) return@Column
        val state = when {
            c.openedAt != null -> 2
            now >= c.unlockAt -> 1
            else -> 0
        }
        AnimatedContent(state, transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(300)) }, label = "capsuleState", modifier = Modifier.weight(1f)) { s ->
            when (s) {
                0 -> SealedView(c.title, c.stamp, c.createdAt, c.unlockAt, now)
                1 -> UnsealView(c.title, c.stamp, c.createdAt) { vm.open() }
                else -> LetterView(
                    c.title, body, c.createdAt, c.openedAt ?: now, Mood.of(c.mood), attachments, vm::uri,
                    onReply = { nav.open(Routes.CAPSULE_NEW) },
                )
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Let this letter go?", style = Kairos.type.heading) },
            text = { Text("The letter and everything tucked inside it will be deleted from this device. This can't be undone.", style = Kairos.type.bodyRegular) },
            confirmButton = { TextButton({ confirmDelete = false; vm.delete { nav.back() } }) { Text("Delete", color = Kairos.colors.danger, style = Kairos.type.label) } },
            dismissButton = { TextButton({ confirmDelete = false }) { Text("Keep it", style = Kairos.type.label) } },
            containerColor = Kairos.colors.paper,
        )
    }
}

@Composable
private fun SealedView(title: String, stamp: Int, created: Long, unlock: Long, now: Long) {
    val colors = Kairos.colors
    val diff = (unlock - now).coerceAtLeast(0)
    val days = diff / 86_400_000
    val hours = (diff / 3_600_000) % 24
    val mins = (diff / 60_000) % 60
    val secs = (diff / 1000) % 60
    Column(Modifier.fillMaxSize().padding(Space.screen), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Envelope(stamp, 0f, 0f, Modifier.fillMaxWidth(0.78f).aspectRatio(1.45f))
        Spacer(Modifier.height(Space.xl))
        Text(title, style = Kairos.type.title, color = colors.ink, textAlign = TextAlign.Center)
        Text("Written ${formatDate(created)} · opens ${formatDate(unlock)}", style = Kairos.type.bodyRegular, color = colors.inkSoft, textAlign = TextAlign.Center)
        Spacer(Modifier.height(Space.xl))
        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
            listOf(days to "days", hours to "hours", mins to "min", secs to "sec").forEach { (v, l) ->
                Column(
                    Modifier.width(70.dp).liquidGlass(Radii.card, GlassWeight.Frosted, blur = true).padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("$v", style = Kairos.type.title, color = colors.ink)
                    Text(l, style = Kairos.type.caption, color = colors.inkSoft)
                }
            }
        }
        Spacer(Modifier.height(Space.xl))
        Text("Still travelling. No peeking — not even Kiri can read it yet.", style = Kairos.type.bodyRegular, color = colors.inkSoft, textAlign = TextAlign.Center)
    }
}

/** Press and hold the wax seal: a ring fills, the seal cracks, the flap lifts, the letter rises. */
@Composable
private fun UnsealView(title: String, stamp: Int, created: Long, onOpened: () -> Unit) {
    val colors = Kairos.colors
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val hold = remember { Animatable(0f) }
    val crack = remember { Animatable(0f) }
    val flap = remember { Animatable(0f) }
    val rise = remember { Animatable(0f) }
    var done by remember { mutableStateOf(false) }

    suspend fun finish() {
        done = true
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        crack.animateTo(1f, tween(420, easing = Motion.Enter))
        flap.animateTo(1f, tween(520, easing = Motion.Move))
        rise.animateTo(1f, tween(620, easing = Motion.Enter))
        delay(150)
        onOpened()
    }

    Column(Modifier.fillMaxSize().padding(Space.screen), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Eyebrow("A letter from ${formatDate(created, "d MMMM yyyy")}", color = colors.pond)
        Spacer(Modifier.height(Space.sm))
        Text(title, style = Kairos.type.title, color = colors.ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(Space.xxl))
        Box(
            Modifier
                .fillMaxWidth(0.86f)
                .aspectRatio(1.2f)
                .pointerInput(done) {
                    if (done) return@pointerInput
                    detectTapGestures(onPress = {
                        val job = scope.launch {
                            hold.animateTo(1f, tween(((1f - hold.value) * 1300).toInt(), easing = LinearEasing))
                            finish()
                        }
                        val released = tryAwaitRelease()
                        if (released && !done) {
                            job.cancel()
                            scope.launch { hold.animateTo(0f, tween(300)) }
                        }
                    })
                }
                .semantics {
                    contentDescription = "Sealed letter. Press and hold the seal to open."
                    onClick(label = "Open letter") { scope.launch { finish() }; true }
                },
            contentAlignment = Alignment.BottomCenter,
        ) {
            // the letter rising out of the envelope
            Box(
                Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1.1f)
                    .graphicsLayer {
                        translationY = (1f - rise.value) * 40.dp.toPx() - rise.value * 90.dp.toPx()
                        alpha = flap.value
                    }
                    .liquidGlass(Radii.small, GlassWeight.Frosted),
            )
            Envelope(
                stamp, open = flap.value, glow = 0.6f + 0.4f * hold.value, sealCrack = crack.value,
                sealScale = 1f + 0.08f * hold.value,
                modifier = Modifier.fillMaxWidth().aspectRatio(1.45f).graphicsLayer {
                    val shake = if (hold.value > 0.05f && !done) kotlin.math.sin(hold.value * 90f) * 2.5f * hold.value else 0f
                    rotationZ = shake
                },
            )
            Canvas(Modifier.fillMaxWidth().aspectRatio(1.45f)) {
                if (hold.value > 0f && crack.value == 0f) {
                    val c = Offset(size.width / 2, size.height * 0.54f)
                    val r = size.height * 0.26f
                    drawArc(colors.pond, -90f, 360f * hold.value, false, Offset(c.x - r, c.y - r), Size(r * 2, r * 2), style = Stroke(5.dp.toPx(), cap = StrokeCap.Round))
                }
            }
        }
        Spacer(Modifier.height(Space.xxl))
        Text(if (hold.value > 0f) "Keep holding…" else "Press and hold the seal", style = Kairos.type.heading, color = colors.ink)
        Text("Take a breath first. Past-you was thinking of you.", style = Kairos.type.bodyRegular, color = colors.inkSoft, textAlign = TextAlign.Center)
    }
}

@Composable
private fun LetterView(
    title: String,
    body: String?,
    created: Long,
    opened: Long,
    mood: Mood,
    attachments: List<AttachmentEntity>,
    uriOf: (AttachmentEntity) -> android.net.Uri,
    onReply: () -> Unit,
) {
    val colors = Kairos.colors
    val context = LocalContext.current
    val player = remember { VoicePlayer() }
    var playing by remember { mutableStateOf<String?>(null) }
    var pos by remember { mutableFloatStateOf(0f) }
    DisposableEffect(Unit) { onDispose { player.stop() } }
    LaunchedEffect(playing) { while (playing != null) { pos = player.position(); delay(100) } }

    fun openExternal(a: AttachmentEntity) {
        val intent = Intent(Intent.ACTION_VIEW).setDataAndType(uriOf(a), a.mime).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        runCatching { context.startActivity(Intent.createChooser(intent, a.name)) }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = Space.screen)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        Spacer(Modifier.height(Space.md))
        GlassCard(Modifier.fillMaxWidth(), blur = true, weight = GlassWeight.Frosted, shape = Radii.sheet, contentPadding = androidx.compose.foundation.layout.PaddingValues(24.dp)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Eyebrow("Written ${formatDate(created, "d MMMM yyyy, h:mm a")}")
                        Eyebrow("Opened ${formatDate(opened, "d MMMM yyyy")}", color = colors.inkFaint)
                    }
                    Kiri(mood, size = 56.dp)
                }
                Spacer(Modifier.height(Space.lg))
                Text(title, style = Kairos.type.title, color = colors.ink)
                Spacer(Modifier.height(Space.lg))
                Text(body ?: "…", style = Kairos.type.quote.copy(fontSize = 20.sp, lineHeight = 31.sp), color = colors.ink)
                Spacer(Modifier.height(Space.lg))
                Text("You felt ${mood.label.lowercase()} when you wrote this.", style = Kairos.type.caption.copy(letterSpacing = 0.sp), color = colors.inkSoft)
            }
        }
        val images = attachments.filter { it.kind == "Image" }
        val others = attachments.filter { it.kind != "Image" }
        if (images.isNotEmpty()) {
            SectionTitle("Pictures you tucked in")
            images.chunked(2).forEach { row ->
                Row(Modifier.padding(bottom = Space.md), horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                    row.forEachIndexed { i, a ->
                        AsyncImage(
                            File(a.path), a.name, contentScale = ContentScale.Crop,
                            modifier = Modifier.weight(1f).aspectRatio(if (i % 2 == 0) 0.85f else 0.85f).clip(Radii.card)
                                .graphicsLayer { rotationZ = if (i % 2 == 0) -1.2f else 1.4f }
                                .clickableNoRipple { openExternal(a) },
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
        if (others.isNotEmpty()) {
            SectionTitle("Also inside")
            others.forEach { a ->
                GlassCard(Modifier.fillMaxWidth().padding(bottom = Space.sm), onClick = {
                    if (a.kind == "Audio") {
                        val nowPlaying = player.toggle(a.path) { playing = null }
                        playing = if (nowPlaying) a.path else null
                    } else openExternal(a)
                }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).background(colors.lilac, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(
                                when (a.kind) {
                                    "Audio" -> if (playing == a.path) Icons.Rounded.Pause else Icons.Rounded.PlayArrow
                                    "Video" -> Icons.Rounded.PlayCircle
                                    else -> Icons.Rounded.Description
                                },
                                null, tint = colors.ink,
                            )
                        }
                        Spacer(Modifier.width(Space.md))
                        Column(Modifier.weight(1f)) {
                            Text(if (a.kind == "Audio") "Voice from the past" else a.name, style = Kairos.type.label, color = colors.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (a.kind == "Audio") {
                                Canvas(Modifier.fillMaxWidth().height(8.dp).padding(top = 4.dp)) {
                                    drawLine(colors.ink.copy(alpha = 0.1f), Offset(0f, size.height / 2), Offset(size.width, size.height / 2), strokeWidth = size.height, cap = StrokeCap.Round)
                                    val p = if (playing == a.path) pos else 0f
                                    drawLine(colors.pond, Offset(0f, size.height / 2), Offset(size.width * p, size.height / 2), strokeWidth = size.height, cap = StrokeCap.Round)
                                }
                            } else {
                                Text("%.1f MB".format(a.bytes / 1_048_576f), style = Kairos.type.caption, color = colors.inkSoft)
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(Space.xl))
        KButton("Write back to future you", onReply, tone = ButtonTone.Pond, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(Space.huge))
    }
}

package com.kairos.app.ui.screens.capsule

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.kairos.app.data.db.AttachmentEntity
import com.kairos.app.data.db.CapsuleWithAttachments
import com.kairos.app.data.repo.AttachmentKind
import com.kairos.app.engine.Mood
import com.kairos.app.ui.components.GlassButton
import com.kairos.app.ui.components.IconBubble
import com.kairos.app.ui.components.PaintBurst
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.squishClickable
import com.kairos.app.ui.glass.GlassPanel
import com.kairos.app.ui.glass.liquidGlass
import com.kairos.app.ui.icons.KairosIcons
import com.kairos.app.ui.mascot.DropMascot
import com.kairos.app.ui.mascot.Expression
import com.kairos.app.ui.mascot.expression
import com.kairos.app.ui.navigation.Scene
import com.kairos.app.ui.navigation.sharedElement
import com.kairos.app.ui.screens.words.DetailTopBar
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Motion
import com.kairos.app.ui.theme.Radii
import com.kairos.app.ui.theme.Space
import com.kairos.app.ui.theme.Wash
import com.kairos.app.util.AudioPlayer
import com.kairos.app.util.Fmt
import com.kairos.app.util.appContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.PI

@Composable
fun CapsuleDetailScreen(id: Long, onBack: () -> Unit, onWriteAnother: () -> Unit) {
    val c = appContainer()
    val item by c.capsules.observe(id).collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()
    var confirmDelete by remember { mutableStateOf(false) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { delay(1000); now = System.currentTimeMillis() } }
    val cap = item ?: return
    val capsule = cap.capsule
    val wash = washOf(capsule.wash)
    Scene(wash, Wash.Rose, Wash.Lilac, wash, Wash.Butter)
    val phase = when {
        capsule.openedAt != null -> 2
        capsule.opensAt <= now -> 1
        else -> 0
    }

    Box(Modifier.fillMaxSize()) {
        AnimatedContent(
            phase,
            transitionSpec = { (fadeIn(Motion.fade(Motion.Slow)) + scaleIn(Motion.settle(), initialScale = 0.96f)) togetherWith fadeOut() },
            label = "capsulePhase",
        ) { p ->
            when (p) {
                0 -> Sealed(cap, now)
                // The envelope animation plays first; marking it opened then cross-fades to the letter.
                1 -> Ready(cap, onOpen = { scope.launch { c.capsules.open(id) } })
                else -> Opened(cap, onReply = { scope.launch { c.capsules.saveReply(id, it) } }, onWriteAnother = onWriteAnother)
            }
        }
        DetailTopBar(onBack) {
            IconBubble(KairosIcons.Trash, "Delete letter", { confirmDelete = true }, blur = true)
        }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(if (phase == 0) "Unseal and delete?" else "Delete this letter?", style = Kairos.type.title) },
            text = { Text("The letter and everything inside it will be gone for good.", style = Kairos.type.body) },
            confirmButton = {
                TextButton({
                    confirmDelete = false
                    scope.launch { c.capsules.delete(id); onBack() }
                }) { Text("Delete", color = Kairos.colors.danger) }
            },
            dismissButton = { TextButton({ confirmDelete = false }) { Text("Keep it") } },
            containerColor = Kairos.colors.paper,
        )
    }
}

private val NotYet = listOf("Not yet!", "Patience, friend.", "No peeking.", "It's still brewing.", "Soon, promise.")

@Composable
private fun Sealed(cap: CapsuleWithAttachments, now: Long) {
    val capsule = cap.capsule
    val palette = Kairos.colors
    val wiggle = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var pokes by remember { mutableIntStateOf(0) }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = Space.gutter),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.windowInsetsPadding(WindowInsets.statusBars).height(96.dp))
        Envelope(
            washOf(capsule.wash).color(palette.isDark),
            modifier = Modifier
                .size(width = 250.dp, height = 172.dp)
                .sharedElement("capsule-${capsule.id}")
                .graphicsLayer { rotationZ = wiggle.value }
                .squishClickable(label = "Try to open") {
                    pokes++
                    scope.launch {
                        repeat(3) { wiggle.animateTo(5f, tween(55)); wiggle.animateTo(-5f, tween(55)) }
                        wiggle.animateTo(0f, tween(55))
                    }
                },
        )
        Spacer(Modifier.height(Space.lg))
        Text(
            if (pokes == 0) "Sealed until" else NotYet[(pokes - 1) % NotYet.size],
            style = Kairos.type.body, color = palette.inkSoft,
        )
        Text(Fmt.ordinalDate(Fmt.localDate(capsule.opensAt)), style = Kairos.type.display, color = palette.ink)
        Text("${Fmt.localDate(capsule.opensAt).year}", style = Kairos.type.title, color = palette.inkFaint)
        Spacer(Modifier.height(Space.xl))
        Countdown(capsule.opensAt - now)
        Spacer(Modifier.height(Space.xl))
        GlassPanel(Modifier.fillMaxWidth(), blur = false) {
            Row(Modifier.padding(Space.xl), verticalAlignment = Alignment.CenterVertically) {
                val mood = Mood.of(capsule.moodAtSeal)
                DropMascot(size = 56.dp, wash = mood?.wash ?: Wash.Cerulean, expression = mood?.expression() ?: Expression.Calm, interactive = false)
                Spacer(Modifier.width(Space.lg))
                Column {
                    Text(capsule.title.ifBlank { "A letter to future you" }, style = Kairos.type.title, color = palette.ink)
                    Text(
                        "Written ${Fmt.date(capsule.createdAt)}" + (mood?.let { ", feeling ${it.label.lowercase()}" } ?: "") +
                            if (cap.attachments.isNotEmpty()) ". ${cap.attachments.size} ${if (cap.attachments.size == 1) "thing" else "things"} tucked inside." else ".",
                        style = Kairos.type.label, color = palette.inkSoft,
                    )
                }
            }
        }
        Spacer(Modifier.height(Space.huge))
    }
}

@Composable
private fun Countdown(remaining: Long) {
    val secs = (remaining / 1000).coerceAtLeast(0)
    val parts = listOf(
        secs / 86_400 to "days",
        (secs % 86_400) / 3600 to "hours",
        (secs % 3600) / 60 to "min",
        secs % 60 to "sec",
    )
    Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
        parts.forEach { (v, label) ->
            Column(
                Modifier
                    .liquidGlass(Radii.Control, blur = true, elevation = 6.dp)
                    .padding(horizontal = Space.md, vertical = Space.md)
                    .width(56.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AnimatedContent(v, transitionSpec = { (fadeIn() + androidx.compose.animation.slideInVertically { -it / 2 }) togetherWith fadeOut() }, label = "count") { n ->
                    Text("$n", style = Kairos.type.title.copy(fontSize = Kairos.type.title.fontSize * 1.2f), color = Kairos.colors.ink)
                }
                Text(label, style = Kairos.type.caption, color = Kairos.colors.inkSoft)
            }
        }
    }
}

@Composable
private fun Ready(cap: CapsuleWithAttachments, onOpen: () -> Unit) {
    val capsule = cap.capsule
    val palette = Kairos.colors
    val reduced = Kairos.reducedMotion
    val color = washOf(capsule.wash).color(palette.isDark)
    val crack = remember { Animatable(0f) }
    val flap = remember { Animatable(0f) }
    val rise = remember { Animatable(0f) }
    var started by remember { mutableStateOf(false) }
    var burst by remember { mutableIntStateOf(0) }
    val t by rememberInfiniteTransition(label = "readyOrbit").animateFloat(
        0f, 2f * PI.toFloat(), infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart), label = "o",
    )
    val float by rememberInfiniteTransition(label = "readyFloat").animateFloat(-6f, 6f, infiniteRepeatable(tween(1800), RepeatMode.Reverse), label = "f")
    LaunchedEffect(started) {
        if (!started) return@LaunchedEffect
        if (!reduced) {
            crack.animateTo(1f, tween(380))
            burst++
            flap.animateTo(1f, tween(480, easing = Motion.Emphasized))
            rise.animateTo(1f, tween(650, easing = Motion.Emphasized))
            delay(250)
        }
        onOpen()
    }
    Column(
        Modifier.fillMaxSize().padding(horizontal = Space.gutter),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("A letter from", style = Kairos.type.body, color = palette.inkSoft)
        Text(Fmt.ordinalDate(Fmt.localDate(capsule.createdAt)) + ", ${Fmt.localDate(capsule.createdAt).year}", style = Kairos.type.display, color = palette.ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(Space.huge))
        Box(Modifier.size(width = 300.dp, height = 240.dp), contentAlignment = Alignment.Center) {
            if (!reduced) Canvas(Modifier.fillMaxSize()) { orbitSparkles(t, Color(0xFFFFE59C), 6) }
            if (burst > 0) PaintBurst(burst, color, Modifier.size(340.dp), count = 18)
            Envelope(
                color, flap = flap.value, sealCrack = crack.value, glow = 1f, letterRise = rise.value,
                modifier = Modifier
                    .size(width = 250.dp, height = 172.dp)
                    .sharedElement("capsule-${capsule.id}")
                    .graphicsLayer { translationY = if (started) 0f else float },
            )
        }
        Spacer(Modifier.height(Space.huge))
        PrimaryButton("Break the seal", { started = true }, icon = KairosIcons.Sparkle, enabled = !started)
        Spacer(Modifier.height(Space.sm))
        Text("Past you has been waiting ${Fmt.ago(capsule.createdAt).removeSuffix(" ago")} for this.", style = Kairos.type.label, color = palette.inkSoft)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Opened(cap: CapsuleWithAttachments, onReply: (String) -> Unit, onWriteAnother: () -> Unit) {
    val capsule = cap.capsule
    val palette = Kairos.colors
    val context = LocalContext.current
    val player = remember { AudioPlayer() }
    var playing by remember { mutableStateOf<String?>(null) }
    var viewing by remember { mutableStateOf<AttachmentEntity?>(null) }
    var reply by rememberSaveable(capsule.id) { mutableStateOf(capsule.reply) }
    DisposableEffect(Unit) { onDispose { player.stop() } }
    val ink = Color(0xFF22305A)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = Space.gutter)
    ) {
        Spacer(Modifier.windowInsetsPadding(WindowInsets.statusBars).height(76.dp))
        Text("From you, ${Fmt.ago(capsule.createdAt)}", style = Kairos.type.body, color = palette.inkSoft)
        Text(capsule.title.ifBlank { Fmt.ordinalDate(Fmt.localDate(capsule.createdAt)) }, style = Kairos.type.display, color = palette.ink)
        Spacer(Modifier.height(Space.xl))
        Column(
            Modifier
                .fillMaxWidth()
                .liquidGlass(Radii.Card, blur = false, tint = Color(0xFFFFFCF6), elevation = 16.dp)
                .padding(Space.xl)
        ) {
            Text("Dear future me,", style = Kairos.type.hand, color = ink)
            Spacer(Modifier.height(Space.sm))
            if (capsule.letter.isNotBlank()) Text(capsule.letter, style = Kairos.type.hand, color = ink)
            Spacer(Modifier.height(Space.lg))
            Text("Written ${Fmt.date(capsule.createdAt)}", style = Kairos.type.caption, color = Color(0xFF8D98B2))
        }
        if (cap.attachments.isNotEmpty()) {
            Spacer(Modifier.height(Space.xl))
            Text("Tucked inside", style = Kairos.type.title, color = palette.ink)
            Spacer(Modifier.height(Space.lg))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.lg), verticalArrangement = Arrangement.spacedBy(Space.xl)) {
                cap.attachments.forEachIndexed { i, a ->
                    val kind = runCatching { AttachmentKind.valueOf(a.kind) }.getOrDefault(AttachmentKind.File)
                    AttachmentChip(
                        kind, File(a.path), a.displayName, tiltFor(i + 1),
                        width = 140.dp,
                        playing = playing == a.path,
                        onClick = {
                            when (kind) {
                                AttachmentKind.Image -> viewing = a
                                AttachmentKind.Audio -> player.toggle(a.path) { playing = null }.also { playing = player.playingPath }
                                else -> openExternally(context, a)
                            }
                        },
                    )
                }
            }
        }
        Spacer(Modifier.height(Space.xl))
        GlassPanel(Modifier.fillMaxWidth(), blur = false) {
            Column(Modifier.padding(Space.xl)) {
                val then = Mood.of(capsule.moodAtSeal)
                if (then != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DropMascot(size = 48.dp, wash = then.wash, expression = then.expression(), interactive = false)
                        Spacer(Modifier.width(Space.md))
                        Text("Back then you felt ${then.label.lowercase()}.", style = Kairos.type.label, color = palette.inkSoft)
                    }
                    Spacer(Modifier.height(Space.lg))
                }
                Text("Write back to past you", style = Kairos.type.title, color = palette.ink)
                Spacer(Modifier.height(Space.sm))
                Box(Modifier.fillMaxWidth().heightIn(min = 90.dp)) {
                    if (reply.isEmpty()) Text("What would you tell them now?", style = Kairos.type.hand, color = palette.inkFaint)
                    BasicTextField(
                        reply, { reply = it },
                        textStyle = Kairos.type.hand.copy(color = palette.ink),
                        cursorBrush = SolidColor(palette.accent),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 90.dp),
                    )
                }
                if (reply != capsule.reply) {
                    Spacer(Modifier.height(Space.sm))
                    GlassButton("Save reply", { onReply(reply) }, icon = KairosIcons.Check)
                }
            }
        }
        Spacer(Modifier.height(Space.xl))
        PrimaryButton("Write another letter", onWriteAnother, Modifier.fillMaxWidth(), icon = KairosIcons.Edit)
        Spacer(Modifier.height(Space.huge))
    }

    viewing?.let { a ->
        Dialog(onDismissRequest = { viewing = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f))
                    .squishClickable(pressedScale = 1f, haptic = false) { viewing = null },
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(File(a.path), a.displayName, Modifier.fillMaxWidth(), contentScale = ContentScale.Fit)
            }
        }
    }
}

private fun openExternally(context: android.content.Context, a: AttachmentEntity) {
    val uri = FileProvider.getUriForFile(context, context.packageName + ".files", File(a.path))
    val intent = Intent(Intent.ACTION_VIEW).setDataAndType(uri, a.mime).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    runCatching { context.startActivity(Intent.createChooser(intent, a.displayName)) }
}

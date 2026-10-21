package com.kairos.app.ui.feature.capsule

import com.kairos.app.ui.theme.canvas
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.kairos.app.data.db.AttachmentEntity
import com.kairos.app.data.db.CapsuleWithAttachments
import com.kairos.app.data.repo.AttachmentKind
import com.kairos.app.engine.Mood
import com.kairos.app.ui.components.Card
import com.kairos.app.ui.components.GlassTile
import com.kairos.app.ui.components.Icons
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.RoundIcon
import com.kairos.app.ui.components.SecondaryButton
import com.kairos.app.ui.components.Status
import com.kairos.app.ui.components.pressable
import com.kairos.app.ui.components.SkyTail
import com.kairos.app.ui.components.sky
import com.kairos.app.ui.feature.detail.TopBar
import com.kairos.app.ui.feature.today.tint
import com.kairos.app.ui.nav.shared
import com.kairos.app.ui.theme.Gap
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.Motion
import com.kairos.app.ui.theme.Serif
import com.kairos.app.ui.theme.tint
import com.kairos.app.ui.theme.tintInk
import com.kairos.app.util.AudioPlayer
import com.kairos.app.util.Fmt
import com.kairos.app.util.appContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun LetterScreen(id: Long, onBack: () -> Unit, onWriteAnother: () -> Unit) {
    val c = appContainer()
    val item by c.capsules.observe(id).collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()
    var confirm by remember { mutableStateOf(false) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { delay(1000); now = System.currentTimeMillis() } }
    val cap = item ?: return
    val phase = when { cap.capsule.openedAt != null -> 2; cap.capsule.opensAt <= now -> 1; else -> 0 }
    Box(Modifier.fillMaxSize().background(K.colors.canvas)) {
        AnimatedContent(phase, transitionSpec = { (fadeIn(Motion.fade(420)) + scaleIn(Motion.settle(), initialScale = 0.97f)) togetherWith fadeOut() }, label = "phase") { ph ->
            when (ph) {
                0 -> Sealed(cap, now)
                1 -> Ready(cap) { scope.launch { c.capsules.open(id) } }
                else -> Opened(cap, { r -> scope.launch { c.capsules.saveReply(id, r) } }, onWriteAnother)
            }
        }
        TopBar(onBack, onSky = true) { RoundIcon(Icons.Trash, "Delete letter", { confirm = true }, onSky = true) }
    }
    if (confirm) AlertDialog(
        onDismissRequest = { confirm = false },
        title = { Text(if (phase == 0) "Unseal and delete?" else "Delete this letter?", style = K.type.title) },
        text = { Text("The letter and everything inside it will be gone for good.", style = K.type.body) },
        confirmButton = { TextButton({ confirm = false; scope.launch { c.capsules.delete(id); onBack() } }) { Text("Delete", color = K.colors.negative) } },
        dismissButton = { TextButton({ confirm = false }) { Text("Keep it") } },
        containerColor = K.colors.surface,
    )
}

private val NotYet = listOf("Not yet.", "No peeking.", "Still brewing.", "Soon, promise.")

@Composable
private fun Sealed(cap: CapsuleWithAttachments, now: Long) {
    val p = K.colors
    val c = cap.capsule
    val wiggle = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var pokes by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).navigationBarsPadding()) {
        Column(Modifier.fillMaxWidth().sky().padding(top = 96.dp, bottom = SkyTail), horizontalAlignment = Alignment.CenterHorizontally) {
            Envelope(
                p.tint(tintOf(c.tint)),
                modifier = Modifier.size(width = 230.dp, height = 158.dp).shared("capsule-${c.id}").graphicsLayer { rotationZ = wiggle.value }
                    .pressable(label = "Try to open") {
                        pokes++
                        scope.launch { repeat(3) { wiggle.animateTo(4f, tween(50)); wiggle.animateTo(-4f, tween(50)) }; wiggle.animateTo(0f, tween(50)) }
                    },
            )
            Spacer(Modifier.height(Gap.xl))
            Text(if (pokes == 0) "Opens on" else NotYet[(pokes - 1) % NotYet.size], style = K.type.label, color = p.onSkySoft)
            Text(Fmt.ordinalDate(Fmt.localDate(c.opensAt)) + ", " + Fmt.localDate(c.opensAt).year, style = K.type.serifLarge, color = p.onSky)
            Spacer(Modifier.height(Gap.xl))
            val secs = ((c.opensAt - now) / 1000).coerceAtLeast(0)
            Row(Modifier.padding(horizontal = Gap.gutter), horizontalArrangement = Arrangement.spacedBy(Gap.sm)) {
                listOf(secs / 86_400 to "days", (secs % 86_400) / 3600 to "hours", (secs % 3600) / 60 to "min", secs % 60 to "sec").forEach { (v, l) ->
                    GlassTile(Modifier.weight(1f).height(76.dp)) {
                        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            AnimatedContent(v, transitionSpec = { (fadeIn() + slideInVertically { -it / 2 }) togetherWith fadeOut() }, label = "n") { n ->
                                Text("$n", style = K.type.title.copy(fontSize = 24.sp), color = p.onSky)
                            }
                            Text(l, style = K.type.caption, color = p.onSkySoft)
                        }
                    }
                }
            }
        }
        Card(Modifier.fillMaxWidth().padding(horizontal = Gap.gutter)) {
            Text(c.title.ifBlank { "A letter to future you" }, style = K.type.title, color = p.ink)
            Spacer(Modifier.height(Gap.xs))
            val mood = Mood.of(c.moodAtSeal)
            Text(
                "Written ${Fmt.date(c.createdAt)}" + (mood?.let { ", feeling ${it.label.lowercase()}" } ?: "") + ".",
                style = K.type.body, color = p.inkSoft,
            )
            if (cap.attachments.isNotEmpty()) { Spacer(Modifier.height(Gap.md)); Status("${cap.attachments.size} ${if (cap.attachments.size == 1) "thing" else "things"} tucked inside", p.accent) }
        }
    }
}

@Composable
private fun Ready(cap: CapsuleWithAttachments, onOpen: () -> Unit) {
    val p = K.colors
    val calm = K.calm
    val c = cap.capsule
    val crack = remember { Animatable(0f) }
    val flap = remember { Animatable(0f) }
    val rise = remember { Animatable(0f) }
    var started by remember { mutableStateOf(false) }
    val bob by rememberInfiniteTransition(label = "rb").animateFloat(-5f, 5f, infiniteRepeatable(tween(1700), RepeatMode.Reverse), label = "b")
    LaunchedEffect(started) {
        if (!started) return@LaunchedEffect
        if (!calm) {
            crack.animateTo(1f, tween(360))
            flap.animateTo(1f, tween(460, easing = Motion.Ease))
            rise.animateTo(1f, tween(620, easing = Motion.Ease))
            delay(220)
        }
        onOpen()
    }
    Column(Modifier.fillMaxSize().sky(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("A letter from", style = K.type.label, color = p.onSkySoft)
        Text(Fmt.ordinalDate(Fmt.localDate(c.createdAt)) + ", " + Fmt.localDate(c.createdAt).year, style = K.type.serifLarge, color = p.onSky, textAlign = TextAlign.Center)
        Spacer(Modifier.height(Gap.huge))
        Envelope(
            p.tint(tintOf(c.tint)), flap = flap.value, crack = crack.value, rise = rise.value, glow = 1f,
            modifier = Modifier.size(width = 250.dp, height = 172.dp).shared("capsule-${c.id}").graphicsLayer { translationY = if (started || calm) 0f else bob },
        )
        Spacer(Modifier.height(Gap.huge))
        PrimaryButton("Break the seal", { started = true }, icon = Icons.Sparkle, enabled = !started, dark = true)
        Spacer(Modifier.height(Gap.sm))
        Text("Past you has been waiting ${Fmt.ago(c.createdAt).removeSuffix(" ago")}.", style = K.type.caption, color = p.inkSoft)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Opened(cap: CapsuleWithAttachments, onReply: (String) -> Unit, onWriteAnother: () -> Unit) {
    val p = K.colors
    val c = cap.capsule
    val context = LocalContext.current
    val player = remember { AudioPlayer() }
    var playing by remember { mutableStateOf<String?>(null) }
    var viewing by remember { mutableStateOf<AttachmentEntity?>(null) }
    var reply by rememberSaveable(c.id) { mutableStateOf(c.reply) }
    DisposableEffect(Unit) { onDispose { player.stop() } }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().navigationBarsPadding().padding(bottom = Gap.huge)) {
        Column(Modifier.fillMaxWidth().sky().padding(top = 92.dp, bottom = SkyTail).padding(horizontal = Gap.gutter)) {
            Text("From you, ${Fmt.ago(c.createdAt)}", style = K.type.label, color = p.onSkySoft)
            Text(c.title.ifBlank { Fmt.ordinalDate(Fmt.localDate(c.createdAt)) }, style = K.type.serifLarge, color = p.onSky)
        }
        Card(Modifier.fillMaxWidth().padding(horizontal = Gap.gutter)) {
            Text("Dear future me,", style = K.type.serifLarge.copy(fontSize = 26.sp, fontStyle = FontStyle.Italic), color = p.ink)
            Spacer(Modifier.height(Gap.md))
            if (c.letter.isNotBlank()) Text(c.letter, style = K.type.body.copy(fontFamily = Serif, fontSize = 21.sp, lineHeight = 31.sp), color = p.ink)
            Spacer(Modifier.height(Gap.lg))
            Text("Written ${Fmt.date(c.createdAt)}", style = K.type.caption, color = p.inkFaint)
        }
        if (cap.attachments.isNotEmpty()) {
            Text("Tucked inside", style = K.type.title, color = p.ink, modifier = Modifier.padding(start = Gap.gutter, top = Gap.xl, bottom = Gap.md))
            FlowRow(Modifier.padding(horizontal = Gap.gutter), horizontalArrangement = Arrangement.spacedBy(Gap.lg), verticalArrangement = Arrangement.spacedBy(Gap.lg)) {
                cap.attachments.forEachIndexed { i, a ->
                    val kind = runCatching { AttachmentKind.valueOf(a.kind) }.getOrDefault(AttachmentKind.File)
                    AttachmentCard(kind, File(a.path), a.displayName, tiltFor(i + 1), width = 140.dp, playing = playing == a.path, onClick = {
                        when (kind) {
                            AttachmentKind.Image -> viewing = a
                            AttachmentKind.Audio -> { player.toggle(a.path) { playing = null }; playing = player.playingPath }
                            else -> openExternally(context, a)
                        }
                    })
                }
            }
        }
        Spacer(Modifier.height(Gap.xl))
        Card(Modifier.fillMaxWidth().padding(horizontal = Gap.gutter)) {
            Mood.of(c.moodAtSeal)?.let { m ->
                Status("Back then you felt ${m.label.lowercase()}", p.tintInk(m.tint()))
                Spacer(Modifier.height(Gap.lg))
            }
            Text("Write back to past you", style = K.type.title, color = p.ink)
            Spacer(Modifier.height(Gap.sm))
            Box(Modifier.fillMaxWidth().heightIn(min = 90.dp)) {
                val st = K.type.body.copy(fontFamily = Serif, fontSize = 20.sp, lineHeight = 29.sp)
                if (reply.isEmpty()) Text("What would you tell them now?", style = st, color = p.inkFaint)
                BasicTextField(reply, { reply = it }, textStyle = st.copy(color = p.ink), cursorBrush = SolidColor(p.accent), modifier = Modifier.fillMaxWidth().heightIn(min = 90.dp))
            }
            if (reply != c.reply) { Spacer(Modifier.height(Gap.sm)); SecondaryButton("Save reply", { onReply(reply) }, icon = Icons.Check) }
        }
        Spacer(Modifier.height(Gap.xl))
        PrimaryButton("Write another letter", onWriteAnother, Modifier.fillMaxWidth().padding(horizontal = Gap.gutter), icon = Icons.Edit, dark = true)
    }
    viewing?.let { a ->
        Dialog({ viewing = null }, DialogProperties(usePlatformDefaultWidth = false)) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.94f)).pressable(scale = 1f, haptic = false) { viewing = null }, contentAlignment = Alignment.Center) {
                AsyncImage(File(a.path), a.displayName, Modifier.fillMaxWidth(), contentScale = ContentScale.Fit)
            }
        }
    }
}

private fun openExternally(context: android.content.Context, a: AttachmentEntity) {
    val uri = FileProvider.getUriForFile(context, context.packageName + ".files", File(a.path))
    runCatching { context.startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW).setDataAndType(uri, a.mime).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION), a.displayName)) }
}


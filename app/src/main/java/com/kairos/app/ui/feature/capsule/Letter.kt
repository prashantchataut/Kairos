package com.kairos.app.ui.feature.capsule

import android.content.Intent
import com.kairos.app.ui.components.drawMark
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
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
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
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
import com.kairos.app.ui.art.Bit
import com.kairos.app.ui.art.Kai
import com.kairos.app.ui.art.burst
import com.kairos.app.ui.art.look
import com.kairos.app.ui.art.rememberBurst
import com.kairos.app.ui.components.Icons
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.RoundIcon
import com.kairos.app.ui.components.pressable
import com.kairos.app.ui.feature.detail.TopBar
import com.kairos.app.ui.nav.shared
import com.kairos.app.ui.theme.Candy
import com.kairos.app.ui.theme.Gap
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.LightPalette
import com.kairos.app.ui.theme.Motion
import com.kairos.app.ui.theme.Radius
import com.kairos.app.ui.theme.SerifText
import com.kairos.app.ui.theme.candy
import com.kairos.app.util.AudioPlayer
import com.kairos.app.util.Fmt
import com.kairos.app.util.appContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

private val Paper = Color(0xFFFFF8EC)
private val PaperInk = Color(0xFF2A2140)

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
    Box(Modifier.fillMaxSize().nightSky()) {
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

private val NotYet = listOf("Not yet.", "No peeking.", "Still brewing.", "Soon, promise.", "Patience, you.")

private fun CapsuleWithAttachments.paper() = LightPalette.candy(tintOf(capsule.tint))

@Composable
private fun Sealed(cap: CapsuleWithAttachments, now: Long) {
    val c = cap.capsule
    val wiggle = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    var pokes by remember { mutableIntStateOf(0) }
    val mood = Mood.of(c.moodAtSeal)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(top = 92.dp, bottom = Gap.huge), horizontalAlignment = Alignment.CenterHorizontally) {
        GlassFolder(
            cap.paper(),
            Modifier.size(width = 250.dp, height = 210.dp).shared("capsule-${c.id}").graphicsLayer { rotationZ = wiggle.value }
                .pressable(label = "Try to open", scale = 0.96f) {
                    pokes++
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    scope.launch { repeat(3) { wiggle.animateTo(5f, tween(50)); wiggle.animateTo(-5f, tween(50)) }; wiggle.animateTo(0f, tween(60)) }
                },
            sticker = mood?.look(), papers = (1 + cap.attachments.size).coerceAtMost(3),
        )
        Spacer(Modifier.height(Gap.xl))
        AnimatedContent(pokes, transitionSpec = { (fadeIn() + slideInVertically { it / 2 }) togetherWith fadeOut() }, label = "poke") { n ->
            Text(if (n == 0) "Opens on" else NotYet[(n - 1) % NotYet.size], style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = Color.White.copy(alpha = 0.75f))
        }
        val d = Fmt.localDate(c.opensAt)
        FadingTitle(Fmt.ordinalDate(d), "${d.year}", size = 50, modifier = Modifier.padding(horizontal = Gap.gutter))
        Spacer(Modifier.height(Gap.xl))
        val secs = ((c.opensAt - now) / 1000).coerceAtLeast(0)
        Row(Modifier.padding(horizontal = Gap.gutter), horizontalArrangement = Arrangement.spacedBy(Gap.sm)) {
            listOf(secs / 86_400 to "days", (secs % 86_400) / 3600 to "hours", (secs % 3600) / 60 to "min", secs % 60 to "sec").forEach { (v, l) ->
                Column(
                    Modifier.weight(1f).height(80.dp).clip(Radius.tile).background(Color.White.copy(alpha = 0.1f)).border(1.dp, Color.White.copy(alpha = 0.2f), Radius.tile),
                    verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AnimatedContent(v, transitionSpec = { (slideInVertically(Motion.offset) { -it } + fadeIn()) togetherWith (slideOutVertically(Motion.offset) { it } + fadeOut()) }, label = "n") { n ->
                        Text("$n", style = K.type.display.copy(fontSize = 26.sp), color = Color.White)
                    }
                    Text(l, style = K.type.caption, color = Color.White.copy(alpha = 0.6f))
                }
            }
        }
        Spacer(Modifier.height(Gap.xl))
        Column(Modifier.fillMaxWidth().padding(horizontal = Gap.gutter).clip(Radius.card).background(Color.White.copy(alpha = 0.08f)).padding(Gap.xl)) {
            Text(c.title.ifBlank { "A letter to future you" }, style = K.type.title, color = Color.White)
            Spacer(Modifier.height(Gap.xs))
            Text("Written ${Fmt.date(c.createdAt)}" + (mood?.let { ", feeling ${it.label.lowercase()}" } ?: "") + ".", style = K.type.body, color = Color.White.copy(alpha = 0.7f))
            if (cap.attachments.isNotEmpty()) {
                Spacer(Modifier.height(Gap.sm))
                Text("${cap.attachments.size} ${if (cap.attachments.size == 1) "thing" else "things"} tucked inside", style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = Candy.Sun)
            }
        }
    }
}

/**
 * Ready: the folder glows and bobs. Breaking the seal pops the wax, lifts the folder and tips it
 * toward you in 3D (r31), then the letter slides out to a burst of confetti.
 */
@Composable
private fun Ready(cap: CapsuleWithAttachments, onOpen: () -> Unit) {
    val calm = K.calm
    val c = cap.capsule
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current
    val seal = remember { Animatable(1f) }
    val lift = remember { Animatable(0f) }
    val rise = remember { Animatable(0f) }
    val burst = rememberBurst()
    var started by remember { mutableStateOf(false) }
    val bob = if (!calm) rememberInfiniteTransition(label = "rb").animateFloat(-6f, 6f, infiniteRepeatable(tween(1700), RepeatMode.Reverse), label = "b") else null
    val glow = if (!calm) rememberInfiniteTransition(label = "rg").animateFloat(0.6f, 1f, infiniteRepeatable(tween(1300), RepeatMode.Reverse), label = "g") else null
    LaunchedEffect(started) {
        if (!started) return@LaunchedEffect
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        if (!calm) {
            seal.animateTo(1.35f, tween(140))
            seal.animateTo(0f, tween(220))
            lift.animateTo(1f, Motion.bloom())
            launch { burst.fire(with(density) { Offset(130.dp.toPx(), 60.dp.toPx()) }, count = 28, power = 1.5f, kinds = listOf(Bit.Petal, Bit.Spark, Bit.Squiggle, Bit.Heart)) }
            rise.animateTo(1.4f, tween(700, easing = Motion.Out))
            delay(300)
        }
        onOpen()
    }
    Column(Modifier.fillMaxSize().padding(top = 80.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("A letter from", style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = Color.White.copy(alpha = 0.75f))
        val d = Fmt.localDate(c.createdAt)
        FadingTitle(Fmt.ordinalDate(d), "${d.year}", size = 46)
        Spacer(Modifier.height(Gap.xl))
        Box(
            Modifier.size(width = 260.dp, height = 220.dp).burst(burst).shared("capsule-${c.id}").graphicsLayer {
                translationY = (if (started) 0f else (bob?.value ?: 0f)) * density.density - lift.value * 30f * density.density
                rotationX = -lift.value * 22f
                cameraDistance = 14f * density.density
                val s = 1f + lift.value * 0.08f; scaleX = s; scaleY = s
            },
        ) { GlassFolder(Candy.Sun, Modifier.fillMaxSize(), rise = rise.value, glow = glow?.value ?: 1f, sticker = Mood.of(c.moodAtSeal)?.look(), seal = seal.value, sealed = seal.value > 0.01f, papers = 2) }
        Spacer(Modifier.height(Gap.huge))
        PrimaryButton("Break the seal", { started = true }, icon = Icons.Sparkle, enabled = !started, onSky = true)
        Spacer(Modifier.height(Gap.md))
        Text("Past you has been waiting ${Fmt.ago(c.createdAt).removeSuffix(" ago")}.", style = K.type.caption, color = Color.White.copy(alpha = 0.6f))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Opened(cap: CapsuleWithAttachments, onReply: (String) -> Unit, onWriteAnother: () -> Unit) {
    val c = cap.capsule
    val context = LocalContext.current
    val player = remember { AudioPlayer() }
    var playing by remember { mutableStateOf<String?>(null) }
    var viewing by remember { mutableStateOf<AttachmentEntity?>(null) }
    var reply by rememberSaveable(c.id) { mutableStateOf(c.reply) }
    DisposableEffect(Unit) { onDispose { player.stop() } }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().navigationBarsPadding().padding(top = 92.dp, bottom = Gap.huge)) {
        Column(Modifier.padding(horizontal = Gap.gutter)) {
            Text("From you, ${Fmt.ago(c.createdAt)}", style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = Color.White.copy(alpha = 0.75f))
            val d = Fmt.localDate(c.createdAt)
            FadingTitle(c.title.ifBlank { Fmt.ordinalDate(d) }, "${d.year}", size = 44)
        }
        Spacer(Modifier.height(Gap.xl))
        // The letter unfolds as you arrive: three folds of paper open one after another.
        val calm = K.calm
        val folds = remember { List(3) { Animatable(if (calm) 1f else 0f) } }
        LaunchedEffect(Unit) { folds.forEachIndexed { i, f -> launch { delay(150L + i * 220L); f.animateTo(1f, Motion.settle()) } } }
        Box(Modifier.fillMaxWidth().padding(horizontal = Gap.gutter).graphicsLayer {
            rotationX = (1f - folds[0].value) * 70f; cameraDistance = 16f * density; transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f)
            alpha = (folds[0].value * 1.5f).coerceAtMost(1f)
        }) {
            LetterPaper(cap.paper(), "SEALED\n" + Fmt.shortDate(c.createdAt).uppercase(), Modifier.fillMaxWidth(), firstLine = 104.dp) {
                Column(Modifier.padding(start = 52.dp, end = 20.dp, top = 22.dp, bottom = 26.dp)) {
                    Text("Dear future me,", style = K.type.title.copy(fontFamily = com.kairos.app.ui.theme.Hand, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 36.sp), color = PaperInk, modifier = Modifier.padding(end = 112.dp, top = 18.dp))
                    Spacer(Modifier.height(4.dp))
                    // Each paragraph fades up in turn, as if being read for the first time.
                    c.letter.split("\n").forEachIndexed { i, para ->
                        val k = folds[(1 + i).coerceAtMost(2)].value
                        Text(para, style = K.type.body.copy(fontFamily = SerifText, fontSize = 19.sp, lineHeight = 31.sp), color = PaperInk,
                            modifier = Modifier.graphicsLayer { alpha = k; translationY = (1f - k) * 12f * density })
                    }
                    Spacer(Modifier.height(Gap.lg))
                    Text("— you, ${Fmt.date(c.createdAt)}", style = K.type.title.copy(fontFamily = com.kairos.app.ui.theme.Hand, fontSize = 22.sp), color = PaperInk.copy(alpha = 0.7f))
                }
            }
        }
        if (cap.attachments.isNotEmpty()) {
            Text("Tucked inside", style = K.type.shout.copy(fontSize = 26.sp), color = Color.White, modifier = Modifier.padding(start = Gap.gutter, top = Gap.xxl, bottom = Gap.md))
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
        // The reply is a postcard back to past you: a stamp corner, an address line, your words.
        Column(
            Modifier.fillMaxWidth().padding(horizontal = Gap.gutter).graphicsLayer { rotationZ = -1.5f }
                .shadow(16.dp, RoundedCornerShape(8.dp)).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF7F1E3)).padding(Gap.xl),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text("A postcard back", style = K.type.title.copy(fontFamily = com.kairos.app.ui.theme.Hand, fontWeight = FontWeight.Bold, fontSize = 26.sp), color = PaperInk)
                    Mood.of(c.moodAtSeal)?.let { m ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Kai(m.look(), size = 26.dp, shadow = false, alive = false, description = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Back then you felt ${m.label.lowercase()}.", style = K.type.caption, color = PaperInk.copy(alpha = 0.7f))
                        }
                    }
                }
                Box(Modifier.size(width = 46.dp, height = 54.dp).border(1.5.dp, PaperInk.copy(alpha = 0.3f), RoundedCornerShape(3.dp)).padding(4.dp).background(cap.paper()), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.size(22.dp)) { drawMark(Color.White, 0f) }
                }
            }
            Spacer(Modifier.height(Gap.md))
            Text("To: me, ${Fmt.date(c.createdAt)}", style = K.type.label, color = PaperInk.copy(alpha = 0.6f))
            Box(Modifier.fillMaxWidth().padding(vertical = 6.dp).height(1.dp).background(PaperInk.copy(alpha = 0.2f)))
            Box(Modifier.fillMaxWidth().heightIn(min = 110.dp)) {
                val st = K.type.body.copy(fontFamily = com.kairos.app.ui.theme.Hand, fontSize = 23.sp, lineHeight = 30.sp)
                if (reply.isEmpty()) Text("What would you tell them now?", style = st, color = PaperInk.copy(alpha = 0.4f))
                BasicTextField(reply, { reply = it }, textStyle = st.copy(color = PaperInk), cursorBrush = SolidColor(Candy.Coral), modifier = Modifier.fillMaxWidth().heightIn(min = 110.dp))
            }
            if (reply != c.reply) { Spacer(Modifier.height(Gap.sm)); PrimaryButton("Keep this postcard", { onReply(reply) }, icon = Icons.Check, dark = true) }
        }
        Spacer(Modifier.height(Gap.xl))
        PrimaryButton("Write another letter", onWriteAnother, Modifier.fillMaxWidth().padding(horizontal = Gap.gutter), icon = Icons.Edit, onSky = true)
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

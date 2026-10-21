package com.kairos.app.ui.feature.you

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.kairos.app.ui.art.Bit
import com.kairos.app.ui.art.burst
import com.kairos.app.ui.art.rememberBurst
import com.kairos.app.ui.components.Icons
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.pressable
import com.kairos.app.ui.theme.Gap
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.Motion
import com.kairos.app.ui.theme.Radius
import com.kairos.app.util.appContainer
import com.kairos.app.work.RefillWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Where the key ritual is, so the host screen can make Kai react. */
enum class KeyPhase { Idle, Typing, Checking, Done, Failed }

/**
 * The key ritual. Paste a key and the little key glides along the field into the lock and turns;
 * if OpenRouter accepts it the lock pops open, petals burst and the card glows mint. If not, the
 * card shakes firmly (no bounce: errors should feel solid) and says why.
 */
@Composable
fun KeyRitual(onPhase: (KeyPhase) -> Unit, onConnected: () -> Unit, modifier: Modifier = Modifier, showGetKey: Boolean = true, onSky: Boolean = false) {
    val c = appContainer()
    val p = K.colors
    val calm = K.calm
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    var key by remember { mutableStateOf("") }
    var phase by remember { mutableStateOf(KeyPhase.Idle) }
    var error by remember { mutableStateOf<String?>(null) }
    val travel = remember { Animatable(0f) }
    val turn = remember { Animatable(0f) }
    val shake = remember { Animatable(0f) }
    val pop = remember { Animatable(1f) }
    val burst = rememberBurst()
    fun set(ph: KeyPhase) { phase = ph; onPhase(ph) }

    fun connect() {
        if (key.length < 10 || phase == KeyPhase.Checking) return
        set(KeyPhase.Checking)
        error = null
        scope.launch {
            val check = scope.launch {
                if (!calm) { travel.animateTo(1f, tween(620, easing = Motion.InOut)); turn.animateTo(1f, Motion.bloom()) }
            }
            val problem = c.ai.validate(key)
            check.join()
            if (problem == null) {
                c.keys.setApiKey(key)
                RefillWorker.enqueue(context)
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                set(KeyPhase.Done)
                if (!calm) {
                    launch { pop.snapTo(0.6f); pop.animateTo(1f, Motion.bloom()) }
                    launch { burst.fire(with(density) { Offset(22.dp.toPx(), 22.dp.toPx()) }, kinds = listOf(Bit.Petal, Bit.Spark, Bit.Dot), count = 22) }
                }
                delay(if (calm) 300 else 1100)
                onConnected()
            } else {
                error = problem.message
                set(KeyPhase.Failed)
                launch { turn.animateTo(0f, tween(160)); travel.animateTo(0f, tween(320, easing = Motion.Out)) }
                if (!calm) for (x in listOf(14f, -12f, 9f, -6f, 3f, 0f)) shake.animateTo(x, tween(55))
            }
        }
    }

    val rim by animateColorAsState(
        when (phase) { KeyPhase.Done -> p.positive; KeyPhase.Failed -> p.negative; KeyPhase.Checking -> p.accent; else -> p.line }, Motion.fade(), label = "rim",
    )
    val glow by animateFloatAsState(if (phase == KeyPhase.Done) 1f else 0f, Motion.fade(400), label = "glow")

    Column(modifier.graphicsLayer { translationX = shake.value * density.density }) {
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(18.dp + 10.dp * glow, Radius.tile, ambientColor = if (glow > 0f) p.positive else p.shadow, spotColor = if (glow > 0f) p.positive else p.shadow)
                .clip(Radius.tile)
                .background(p.surface)
                .border(1.5.dp, rim, Radius.tile),
        ) {
            val trackPx = with(density) { (maxWidth - 64.dp - 44.dp).toPx() }
            // The key, which travels to the lock while we check.
            Box(
                Modifier
                    .padding(start = 14.dp)
                    .align(Alignment.CenterStart)
                    .graphicsLayer { translationX = travel.value * trackPx; rotationZ = turn.value * 90f }
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (phase == KeyPhase.Done) p.positive.copy(alpha = 0.14f) else p.accentSoft),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Key, null, tint = if (phase == KeyPhase.Done) p.positive else p.accent, modifier = Modifier.size(19.dp)) }
            Box(Modifier.align(Alignment.CenterStart).padding(start = 62.dp, end = 58.dp).fillMaxWidth().graphicsLayer { alpha = 1f - travel.value }) {
                if (key.isEmpty()) Text("Paste your OpenRouter key", style = K.type.body, color = p.inkFaint, maxLines = 1)
                BasicTextField(
                    key, { key = it.trim(); error = null; if (phase != KeyPhase.Checking) set(if (it.isBlank()) KeyPhase.Idle else KeyPhase.Typing) },
                    singleLine = true, enabled = phase != KeyPhase.Checking && phase != KeyPhase.Done,
                    textStyle = K.type.body.copy(color = p.ink), cursorBrush = SolidColor(p.accent),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (travel.value > 0.6f) {
                Text(
                    if (phase == KeyPhase.Done) "Connected" else "Checking with OpenRouter…",
                    style = K.type.label, color = if (phase == KeyPhase.Done) p.positive else p.inkSoft,
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 18.dp).graphicsLayer { alpha = (travel.value - 0.6f) / 0.4f },
                )
            }
            // The lock, or a paste button while the field is empty.
            Box(
                Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp)
                    .size(44.dp)
                    .burst(burst)
                    .graphicsLayer { scaleX = pop.value; scaleY = pop.value }
                    .then(if (key.isEmpty() && phase == KeyPhase.Idle) Modifier.pressable(label = "Paste key") { clipboard.getText()?.text?.trim()?.let { key = it; set(KeyPhase.Typing) } } else Modifier)
                    .clip(CircleShape)
                    .background(if (phase == KeyPhase.Done) p.positive else p.surfaceSoft),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(
                    when { phase == KeyPhase.Done -> 2; key.isEmpty() && phase == KeyPhase.Idle -> 0; else -> 1 },
                    transitionSpec = { (fadeIn(Motion.fade(160)) + scaleIn(Motion.bloom(), initialScale = 0.6f)) togetherWith fadeOut(Motion.fade(100)) },
                    label = "lock",
                ) { s ->
                    when (s) {
                        0 -> Icon(Icons.Paste, "Paste", tint = p.inkSoft, modifier = Modifier.size(20.dp))
                        1 -> Icon(Icons.Lock, null, tint = p.inkSoft, modifier = Modifier.size(20.dp))
                        else -> Icon(Icons.Unlock, null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
        error?.let { Spacer(Modifier.height(Gap.sm)); Text(it, style = K.type.label, color = p.negative) }
        Spacer(Modifier.height(Gap.md))
        Row(horizontalArrangement = Arrangement.spacedBy(Gap.sm), verticalAlignment = Alignment.CenterVertically) {
            PrimaryButton(
                when (phase) { KeyPhase.Checking -> "Checking…"; KeyPhase.Done -> "Awake!"; else -> "Wake Kai up" }, ::connect,
                Modifier.weight(1f), icon = if (phase == KeyPhase.Done) Icons.Check else Icons.Sparkle,
                enabled = key.length > 10 && phase != KeyPhase.Checking && phase != KeyPhase.Done, dark = !onSky, onSky = onSky,
            )
            if (showGetKey) Text(
                "Get a free key", style = K.type.label.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, textDecoration = if (onSky) androidx.compose.ui.text.style.TextDecoration.Underline else null), color = if (onSky) androidx.compose.ui.graphics.Color.White else p.accent,
                modifier = Modifier
                    .pressable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://openrouter.ai/keys")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                    .padding(horizontal = Gap.md, vertical = Gap.md),
            )
        }
    }
}

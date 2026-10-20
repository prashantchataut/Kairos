package com.kairos.app.ui.today

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Forest
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.container
import com.kairos.app.engine.content.Kindness
import com.kairos.app.engine.daily.Mood
import com.kairos.app.ui.components.DetailTopBar
import com.kairos.app.ui.components.EmptyState
import com.kairos.app.ui.components.Eyebrow
import com.kairos.app.ui.glass.GlassIconButton
import com.kairos.app.ui.glass.GlassWeight
import com.kairos.app.ui.glass.liquidGlass
import com.kairos.app.ui.nav.LocalNavigator
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Radii
import com.kairos.app.ui.theme.Space
import kotlinx.coroutines.launch
import kotlin.math.abs

fun kindIcon(kind: String): ImageVector = when (kind) {
    "body" -> Icons.Rounded.SelfImprovement
    "people" -> Icons.Rounded.Groups
    "world" -> Icons.Rounded.Forest
    "mind" -> Icons.Rounded.Psychology
    "joy" -> Icons.Rounded.Celebration
    else -> Icons.Rounded.Favorite
}

@Composable
fun KindnessScreen() {
    val nav = LocalNavigator.current
    val c = LocalContext.current.container
    val colors = Kairos.colors
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    var deck by remember { mutableStateOf<List<Kindness>>(emptyList()) }
    val done by c.kindness.observeDoneToday().collectAsStateWithLifecycle(initialValue = emptySet())
    var index by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { deck = c.kindness.deck() }
    val remaining = deck.filter { it.id !in done }
    val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    fun settle(accept: Boolean, card: Kindness) {
        scope.launch {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            offset.animateTo(Offset(if (accept) 1400f else -1400f, offset.value.y + 80f), tween(260))
            if (accept) c.kindness.done(card.id) else index++
            offset.snapTo(Offset.Zero)
        }
    }

    Column(Modifier.fillMaxSize()) {
        DetailTopBar(onBack = nav::back, title = "Kindness deck")
        Text(
            "Swipe right for “I'll do this”, left for “not today.” Nothing here is homework.",
            style = Kairos.type.bodyRegular, color = colors.inkSoft,
            modifier = Modifier.padding(horizontal = Space.screen),
        )
        val visible = if (remaining.isEmpty()) emptyList() else List(minOf(3, remaining.size)) { remaining[(index + it) % remaining.size] }
        Box(Modifier.weight(1f).fillMaxWidth().padding(Space.screen), contentAlignment = Alignment.Center) {
            if (visible.isEmpty()) {
                EmptyState(Mood.Glowing, "All done for today", "Every card in today's deck is done. That's a lot of kindness. A new deck arrives tomorrow.")
            }
            visible.asReversed().forEachIndexed { revIdx, card ->
                val depth = visible.size - 1 - revIdx
                val isTop = depth == 0
                val tint = listOf(colors.peach, colors.mint, colors.lilac, colors.sky, colors.butter, colors.rose)[abs(card.id.hashCode()) % 6]
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .graphicsLayer {
                            val o = if (isTop) offset.value else Offset.Zero
                            translationX = o.x
                            translationY = o.y + depth * 18.dp.toPx()
                            rotationZ = if (isTop) o.x / 40f else (if (depth % 2 == 0) 2.5f else -3f)
                            scaleX = 1f - depth * 0.05f
                            scaleY = 1f - depth * 0.05f
                        }
                        .then(
                            if (isTop) Modifier.pointerInput(card.id) {
                                detectDragGestures(
                                    onDragEnd = {
                                        val x = offset.value.x
                                        when {
                                            x > size.width * 0.28f -> settle(true, card)
                                            x < -size.width * 0.28f -> settle(false, card)
                                            else -> scope.launch { offset.animateTo(Offset.Zero, androidx.compose.animation.core.spring(0.6f, 400f)) }
                                        }
                                    },
                                ) { change, drag ->
                                    change.consume()
                                    scope.launch { offset.snapTo(offset.value + drag) }
                                }
                            } else Modifier,
                        )
                        .liquidGlass(Radii.sheet, GlassWeight.Frosted, elevation = if (isTop) 16.dp else 0.dp)
                        .background(tint.copy(alpha = 0.35f))
                        .padding(28.dp),
                ) {
                    Column(Modifier.fillMaxSize()) {
                        Box(Modifier.size(56.dp).background(tint, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(kindIcon(card.kind), null, tint = Color(0xFF1B2340), modifier = Modifier.size(28.dp))
                        }
                        Spacer(Modifier.weight(1f))
                        Eyebrow("${card.minutes} minute${if (card.minutes > 1) "s" else ""} · ${card.kind}")
                        Spacer(Modifier.height(Space.sm))
                        Text(card.title, style = Kairos.type.title, color = colors.ink)
                        Spacer(Modifier.height(Space.sm))
                        Text(card.detail, style = Kairos.type.bodyRegular.copy(fontSize = 17.sp), color = colors.inkSoft)
                    }
                    if (isTop) {
                        val x = offset.value.x
                        val yes = (x / 300f).coerceIn(0f, 1f)
                        val no = (-x / 300f).coerceIn(0f, 1f)
                        Text("I'll do it", style = Kairos.type.heading, color = colors.success, modifier = Modifier.align(Alignment.TopEnd).graphicsLayer { alpha = yes; rotationZ = 8f })
                        Text("Not today", style = Kairos.type.heading, color = colors.inkSoft, modifier = Modifier.align(Alignment.TopEnd).graphicsLayer { alpha = no; rotationZ = -8f })
                    }
                }
            }
        }
        if (visible.isNotEmpty()) {
            Row(
                Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).padding(bottom = Space.xl),
                horizontalArrangement = Arrangement.spacedBy(Space.xxl, Alignment.CenterHorizontally),
            ) {
                GlassIconButton(Icons.Rounded.Close, "Not today", { settle(false, visible.first()) }, size = 64.dp)
                GlassIconButton(Icons.Rounded.Check, "I'll do it", { settle(true, visible.first()) }, size = 64.dp, tint = colors.success)
            }
        }
        if (done.isNotEmpty()) {
            Text(
                "${done.size} kind thing${if (done.size > 1) "s" else ""} today",
                style = Kairos.type.caption, color = colors.inkSoft, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = Space.lg),
            )
        }
    }
}

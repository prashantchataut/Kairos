package com.kairos.app.ui.nav

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.kairos.app.ui.art.Kai
import com.kairos.app.ui.art.KaiLooks
import com.kairos.app.ui.theme.Candy
import com.kairos.app.ui.theme.Tint
import com.kairos.app.ui.theme.candy
import com.kairos.app.ui.theme.tint
import com.kairos.app.ui.theme.tintInk
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kairos.app.ui.components.Icons
import com.kairos.app.ui.components.pressable
import com.kairos.app.ui.theme.Gap
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.Motion
import com.kairos.app.ui.theme.Radius
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeChild

val LocalHaze = staticCompositionLocalOf<HazeState?> { null }
val LocalShared = staticCompositionLocalOf<SharedTransitionScope?> { null }
val LocalAnim = staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

/** Frosted glass over whatever scrolls beneath it. */
fun Modifier.frosted(shape: androidx.compose.ui.graphics.Shape = CircleShape): Modifier = composed {
    val p = K.colors
    val haze = LocalHaze.current
    clip(shape).then(
        if (haze != null) Modifier.hazeChild(haze, HazeStyle(backgroundColor = p.canvas, tint = HazeTint(p.surface.copy(alpha = 0.8f)), blurRadius = 24.dp, noiseFactor = 0f, fallbackTint = HazeTint(p.surface)))
        else Modifier.background(p.surface)
    ).border(1.dp, p.line.copy(alpha = 0.7f), shape)
}

/** Shared-bounds morph, e.g. a library tile growing into the word page. No-op outside a transition. */
fun Modifier.shared(key: String): Modifier = composed {
    val s = LocalShared.current
    val a = LocalAnim.current
    if (s == null || a == null) this else with(s) {
        this@composed.sharedBounds(rememberSharedContentState(key), a, resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds())
    }
}

data class Tab(val label: String, val icon: ImageVector, val iconOn: ImageVector)

val Tabs = listOf(
    Tab("Today", Icons.Today, Icons.TodayOn),
    Tab("Feed", Icons.Feed, Icons.FeedOn),
    Tab("Capsule", Icons.Capsule, Icons.CapsuleOn),
    Tab("You", Icons.You, Icons.YouOn),
)

/**
 * Icons on the left, one round action on the right. The selected tab sits on a soft pill that
 * stretches as it slides (its two edges spring at different speeds), and the icon gives a little
 * hop. [night] is the capsule's version: a dark bar and a white action.
 */
@Composable
fun BottomBar(selected: Int, onSelect: (Int) -> Unit, onAction: () -> Unit, actionOpen: Boolean, modifier: Modifier = Modifier, night: Boolean = false) {
    val p = K.colors
    var last by remember { mutableIntStateOf(selected) }
    val movingRight = selected >= last
    LaunchedEffect(selected) { last = selected }
    Row(
        modifier
            .navigationBarsPadding()
            .padding(horizontal = Gap.gutter, vertical = Gap.md)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BoxWithConstraints(
            Modifier
                .weight(1f)
                .height(62.dp)
                .shadow(20.dp, CircleShape, ambientColor = p.shadow, spotColor = p.shadow)
                .then(if (night) Modifier.clip(CircleShape).background(Color(0xFF16205A).copy(alpha = 0.94f)).border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape) else Modifier.frosted())
                .padding(horizontal = 6.dp),
        ) {
            val tabW = maxWidth / Tabs.size
            val left by animateDpAsState(tabW * selected, spring(dampingRatio = 0.8f, stiffness = if (movingRight) 260f else 900f), label = "l")
            val right by animateDpAsState(tabW * (selected + 1), spring(dampingRatio = 0.8f, stiffness = if (movingRight) 900f else 260f), label = "r")
            Box(
                Modifier
                    .offset(x = left + 4.dp)
                    .padding(vertical = 7.dp)
                    .width((right - left - 8.dp).coerceAtLeast(8.dp))
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(if (night) Color.White.copy(alpha = 0.14f) else p.accentSoft)
            )
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Tabs.forEachIndexed { i, t ->
                    val on = i == selected
                    val tint by animateColorAsState(
                        when {
                            night -> if (on) Color.White else Color.White.copy(alpha = 0.5f)
                            on -> p.accent
                            else -> p.inkFaint
                        }, label = "tab",
                    )
                    val scale by animateFloatAsState(if (on) 1.12f else 1f, Motion.bloom(), label = "tabS")
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .semantics { this.selected = on }
                            .pressable(role = Role.Tab, label = t.label, scale = 0.88f) { onSelect(i) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(if (on) t.iconOn else t.icon, t.label, tint = tint, modifier = Modifier.size(24.dp).graphicsLayer { scaleX = scale; scaleY = scale })
                    }
                }
            }
        }
        Spacer(Modifier.width(Gap.md))
        val rot by animateFloatAsState(if (actionOpen) 45f else 0f, Motion.bloom(), label = "fab")
        Box(
            Modifier
                .size(62.dp)
                .pressable(label = "Quick actions", scale = 0.9f, onClick = onAction)
                .shadow(20.dp, CircleShape, ambientColor = p.shadow, spotColor = p.shadow)
                .clip(CircleShape)
                .background(if (night) Brush.verticalGradient(listOf(Color.White, Color(0xFFE6ECFF))) else Brush.verticalGradient(listOf(p.ink.copy(alpha = 0.92f), p.ink))),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Plus, null, tint = if (night) Candy.Navy else p.canvas, modifier = Modifier.size(26.dp).graphicsLayer { rotationZ = rot }) }
    }
}

data class QuickAction(val icon: ImageVector, val title: String, val body: String, val tint: Tint, val onClick: () -> Unit)

/** The + sheet: Kai asks what would feel good, and four candy tiles pop in one after another. */
@Composable
fun QuickSheet(actions: List<QuickAction>, onDismiss: () -> Unit) {
    val p = K.colors
    val calm = K.calm
    val pops = remember { actions.map { Animatable(if (calm) 1f else 0f) } }
    LaunchedEffect(Unit) { pops.forEachIndexed { i, a -> launch { delay(120L + i * 60L); a.animateTo(1f, Motion.bloom()) } } }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = p.surface,
        shape = Radius.sheet,
        dragHandle = { Box(Modifier.padding(top = 10.dp).size(width = 36.dp, height = 4.dp).clip(CircleShape).background(p.line)) },
    ) {
        Column(Modifier.padding(horizontal = Gap.gutter).padding(bottom = Gap.xxl, top = Gap.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Kai(KaiLooks.Curator.copy(smile = 0.7f, blush = 0.5f), size = 56.dp, shadow = false)
                Spacer(Modifier.width(Gap.md))
                Text("What would feel good?", style = K.type.display.copy(fontSize = 26.sp), color = p.ink)
            }
            Spacer(Modifier.height(Gap.lg))
            actions.chunked(2).forEachIndexed { row, pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(Gap.md)) {
                    pair.forEachIndexed { col, a ->
                        val k = row * 2 + col
                        Column(
                            Modifier
                                .weight(1f)
                                .height(140.dp)
                                .graphicsLayer { val s = 0.6f + 0.4f * pops[k].value; scaleX = s; scaleY = s; alpha = pops[k].value.coerceIn(0f, 1f) }
                                .pressable(scale = 0.95f) { onDismiss(); a.onClick() }
                                .clip(Radius.card)
                                .background(p.tint(a.tint))
                                .padding(Gap.lg),
                        ) {
                            Box(Modifier.size(44.dp).clip(CircleShape).background(p.candy(a.tint)), contentAlignment = Alignment.Center) {
                                Icon(a.icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.weight(1f))
                            Text(a.title, style = K.type.label.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold), color = p.ink)
                            Text(a.body, style = K.type.caption, color = p.tintInk(a.tint), maxLines = 2)
                        }
                    }
                }
                Spacer(Modifier.height(Gap.md))
            }
        }
    }
}

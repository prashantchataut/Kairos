package com.kairos.app.ui.nav

import androidx.compose.animation.AnimatedVisibilityScope
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
        if (haze != null) Modifier.hazeChild(haze, HazeStyle(backgroundColor = p.canvas, tint = HazeTint(p.surface.copy(alpha = 0.72f)), blurRadius = 24.dp, noiseFactor = 0f, fallbackTint = HazeTint(p.surface.copy(alpha = 0.96f))))
        else Modifier.background(p.surface.copy(alpha = 0.96f))
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
    Tab("Learn", Icons.Learn, Icons.LearnOn),
    Tab("Capsule", Icons.Capsule, Icons.CapsuleOn),
    Tab("You", Icons.You, Icons.YouOn),
)

/** Icons on the left, one dark action on the right — straight from the reference's quiet bar. */
@Composable
fun BottomBar(selected: Int, onSelect: (Int) -> Unit, onAction: () -> Unit, actionOpen: Boolean, modifier: Modifier = Modifier) {
    val p = K.colors
    Row(
        modifier
            .navigationBarsPadding()
            .padding(horizontal = Gap.gutter, vertical = Gap.md)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier
                .weight(1f)
                .height(60.dp)
                .shadow(20.dp, CircleShape, ambientColor = p.shadow, spotColor = p.shadow)
                .frosted()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Tabs.forEachIndexed { i, t ->
                val on = i == selected
                val tint by animateColorAsState(if (on) p.ink else p.inkFaint, label = "tab")
                val scale by animateFloatAsState(if (on) 1.08f else 1f, Motion.soft(), label = "tabS")
                Column(
                    Modifier
                        .semantics { this.selected = on }
                        .pressable(role = Role.Tab, label = t.label) { onSelect(i) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(if (on) t.iconOn else t.icon, t.label, tint = tint, modifier = Modifier.size(24.dp).graphicsLayer { scaleX = scale; scaleY = scale })
                    Spacer(Modifier.height(3.dp))
                    Box(Modifier.size(4.dp).clip(CircleShape).background(if (on) p.ink else Color.Transparent))
                }
            }
        }
        Spacer(Modifier.width(Gap.md))
        val rot by animateFloatAsState(if (actionOpen) 45f else 0f, Motion.soft(), label = "fab")
        Box(
            Modifier
                .size(60.dp)
                .pressable(label = "Quick actions", onClick = onAction)
                .shadow(20.dp, CircleShape, ambientColor = p.shadow, spotColor = p.shadow)
                .clip(CircleShape)
                .background(Brush.verticalGradient(listOf(p.ink.copy(alpha = 0.92f), p.ink))),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Plus, null, tint = p.canvas, modifier = Modifier.size(26.dp).graphicsLayer { rotationZ = rot }) }
    }
}

data class QuickAction(val icon: ImageVector, val title: String, val body: String, val onClick: () -> Unit)

@Composable
fun QuickSheet(actions: List<QuickAction>, onDismiss: () -> Unit) {
    val p = K.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = p.surface,
        shape = Radius.sheet,
        dragHandle = { Box(Modifier.padding(top = 10.dp).size(width = 36.dp, height = 4.dp).clip(CircleShape).background(p.line)) },
    ) {
        Column(Modifier.padding(horizontal = Gap.gutter).padding(bottom = Gap.xxl, top = Gap.md)) {
            Text("What would feel good?", style = K.type.title, color = p.ink)
            Spacer(Modifier.height(Gap.lg))
            actions.forEach { a ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .pressable(scale = 0.98f) { onDismiss(); a.onClick() }
                        .padding(vertical = Gap.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(46.dp).clip(Radius.chip).background(p.accentSoft), contentAlignment = Alignment.Center) {
                        Icon(a.icon, null, tint = p.accent, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(Gap.lg))
                    Column(Modifier.weight(1f)) {
                        Text(a.title, style = K.type.label.copy(fontSize = K.type.body.fontSize), color = p.ink)
                        Text(a.body, style = K.type.caption, color = p.inkSoft)
                    }
                    Icon(Icons.Chevron, null, tint = p.inkFaint, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

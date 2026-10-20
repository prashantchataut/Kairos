package com.kairos.app.ui.nav

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.kairos.app.ui.glass.GlassWeight
import com.kairos.app.ui.glass.liquidGlass
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Radii
import kotlinx.coroutines.launch

enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    Today("today", "Today", Icons.Rounded.WbSunny),
    Learn("learn", "Learn", Icons.Rounded.AutoStories),
    Capsule("capsules", "Capsule", Icons.Rounded.MailOutline),
    Play("play", "Play", Icons.Rounded.SportsEsports),
    You("you", "You", Icons.Rounded.Face),
}

/**
 * Floating glass tab bar. The selection is a droplet of pond-water that stretches toward its
 * destination and snaps back into a bead — a leading edge on a stiff spring, a trailing edge
 * on a soft one. Reduced motion collapses it to a single quick slide.
 */
@Composable
fun LiquidTabBar(
    selected: Tab,
    onSelect: (Tab) -> Unit,
    modifier: Modifier = Modifier,
    badges: Map<Tab, Boolean> = emptyMap(),
) {
    val colors = Kairos.colors
    val reduced = Kairos.reducedMotion
    val haptics = LocalHapticFeedback.current
    val count = Tab.entries.size
    val target = selected.ordinal.toFloat()
    val lead = remember { Animatable(target) }
    val tail = remember { Animatable(target) }
    LaunchedEffect(target) {
        launch { lead.animateTo(target, spring(dampingRatio = 0.75f, stiffness = if (reduced) 1500f else 900f)) }
        launch { tail.animateTo(target, spring(dampingRatio = 0.8f, stiffness = if (reduced) 1500f else Spring.StiffnessLow)) }
    }

    Box(
        modifier
            .fillMaxWidth()
            .height(68.dp)
            .liquidGlass(Radii.pill, GlassWeight.Regular, blur = true, elevation = 18.dp),
    ) {
        Canvas(Modifier.fillMaxWidth().fillMaxHeight().padding(6.dp)) {
            val slot = size.width / count
            val start = minOf(lead.value, tail.value) * slot
            val end = (maxOf(lead.value, tail.value) + 1) * slot
            val stretch = (end - start) / slot - 1f
            val inset = 4.dp.toPx() + stretch * 2.dp.toPx()
            val top = inset * 0.6f
            val h = size.height - top * 2
            val rect = Size(end - start - 8.dp.toPx(), h)
            val o = Offset(start + 4.dp.toPx(), top)
            drawRoundRect(
                Brush.verticalGradient(listOf(colors.pond.copy(alpha = 0.22f), colors.pond.copy(alpha = 0.32f)), startY = o.y, endY = o.y + h),
                o, rect, CornerRadius(h / 2),
            )
            drawRoundRect(
                Brush.verticalGradient(listOf(Color.White.copy(alpha = if (colors.isDark) 0.35f else 0.9f), Color.White.copy(alpha = 0.05f)), startY = o.y, endY = o.y + h),
                o, rect, CornerRadius(h / 2), style = Stroke(1.2.dp.toPx()),
            )
            // a little specular bead on the droplet
            drawCircle(Color.White.copy(alpha = 0.55f), 2.4.dp.toPx(), Offset(o.x + h * 0.42f, o.y + h * 0.26f))
        }
        Row(Modifier.fillMaxWidth().fillMaxHeight().padding(6.dp)) {
            Tab.entries.forEach { tab ->
                val isSel = tab == selected
                val tint by animateColorAsState(if (isSel) colors.pondDeep.takeIf { !colors.isDark } ?: colors.pond else colors.inkSoft, label = "tabTint")
                val lift by animateFloatAsState(if (isSel && !reduced) 1f else 0f, spring(dampingRatio = 0.5f, stiffness = 500f), label = "lift")
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .selectable(isSel, interactionSource = remember { MutableInteractionSource() }, indication = null, role = Role.Tab) {
                            if (!isSel) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSelect(tab)
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                ) {
                    Box {
                        Icon(
                            tab.icon, null, tint = tint,
                            modifier = Modifier.size(24.dp).graphicsLayer {
                                translationY = -lift * 2.dp.toPx()
                                scaleX = 1f + lift * 0.08f
                                scaleY = 1f + lift * 0.08f
                            },
                        )
                        if (badges[tab] == true) {
                            Box(
                                Modifier.align(Alignment.TopEnd).size(8.dp)
                                    .graphicsLayer { translationX = 3.dp.toPx(); translationY = -2.dp.toPx() }
                                    .liquidGlass(Radii.pill, tint = colors.danger),
                            )
                        }
                    }
                    Text(tab.label, style = Kairos.type.caption.copy(letterSpacing = Kairos.type.caption.letterSpacing * 0), color = tint)
                }
            }
        }
    }
}

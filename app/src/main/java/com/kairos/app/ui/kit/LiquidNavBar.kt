package com.kairos.app.ui.kit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kairos.app.ui.theme.Kairos
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.sqrt

data class NavItem(val route: String, val label: String, val icon: ImageVector, val selectedIcon: ImageVector)

/**
 * Floating liquid-glass navigation. The selection is a drop of water: when you change tab the
 * leading edge leaps on a stiff spring and the trailing edge follows on a soft one, so the drop
 * stretches, thins (volume is conserved) and then settles with a tiny wobble.
 */
@Composable
fun LiquidNavBar(
    items: List<NavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val k = Kairos.colors
    val reduce = Kairos.reduceMotion
    val shape = RoundedCornerShape(50)
    GlassSurface(modifier.height(70.dp).fillMaxWidth(), shape = shape, elevation = 24.dp) {
        BoxWithConstraints(Modifier.fillMaxSize().padding(horizontal = 7.dp, vertical = 7.dp)) {
            val density = LocalDensity.current
            val wide = maxWidth > 300.dp
            val slotW = with(density) { maxWidth.toPx() } / items.size
            val left = remember { Animatable(selectedIndex * slotW) }
            val right = remember { Animatable((selectedIndex + 1) * slotW) }

            LaunchedEffect(selectedIndex, slotW) {
                val l = selectedIndex * slotW
                val r = l + slotW
                if (reduce) { left.snapTo(l); right.snapTo(r); return@LaunchedEffect }
                val goingRight = r > right.value
                val fast = spring<Float>(dampingRatio = 0.72f, stiffness = 900f)
                val slow = spring<Float>(dampingRatio = 0.62f, stiffness = 210f)
                coroutineScope {
                    launch { left.animateTo(l, if (goingRight) slow else fast) }
                    launch { right.animateTo(r, if (goingRight) fast else slow) }
                }
            }

            val dropColor = if (k.isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.78f)
            Canvas(Modifier.fillMaxSize()) {
                val w = (right.value - left.value).coerceAtLeast(1f)
                val stretch = (w / slotW).coerceAtLeast(0.6f)
                val h = size.height * (1f / sqrt(stretch)).coerceIn(0.62f, 1.06f)
                val top = (size.height - h) / 2f
                val inset = 2.dp.toPx()
                drawRoundRect(
                    color = dropColor,
                    topLeft = Offset(left.value + inset, top),
                    size = Size(w - inset * 2, h),
                    cornerRadius = CornerRadius(h / 2f),
                )
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = if (k.isDark) 0.35f else 1f), Color.Transparent),
                        startY = top, endY = top + h,
                    ),
                    topLeft = Offset(left.value + inset, top),
                    size = Size(w - inset * 2, h),
                    cornerRadius = CornerRadius(h / 2f),
                    style = Stroke(1.2.dp.toPx()),
                )
                // a little lagoon "meniscus" under the active drop
                drawRoundRect(
                    color = k.lagoon.copy(alpha = 0.9f),
                    topLeft = Offset(left.value + w / 2f - 7.dp.toPx(), top + h - 7.dp.toPx()),
                    size = Size(14.dp.toPx(), 3.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx()),
                )
            }

            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                items.forEachIndexed { i, item ->
                    val selected = i == selectedIndex
                    val tint by animateColorAsState(if (selected) k.ink else k.inkSoft, label = "tint")
                    val lift by animateFloatAsState(if (selected) 1f else 0f, spring(0.6f, 500f), label = "lift")
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .semantics { this.selected = selected }
                            .squishClick(role = Role.Tab, pressedScale = 0.9f) { onSelect(i) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(
                                if (selected) item.selectedIcon else item.icon,
                                contentDescription = item.label,
                                tint = tint,
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer {
                                        val s = 1f + 0.08f * lift
                                        scaleX = s; scaleY = s
                                    },
                            )
                            AnimatedVisibility(
                                visible = selected && wide,
                                enter = fadeIn() + expandHorizontally(),
                                exit = fadeOut() + shrinkHorizontally(),
                            ) {
                                Row {
                                    Spacer(Modifier.width(6.dp))
                                    Text(item.label, style = MaterialTheme.typography.labelMedium, color = tint, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

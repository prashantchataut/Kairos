package com.kairos.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kairos.app.ui.glass.liquidGlass
import com.kairos.app.ui.theme.Kairos

data class NavItem(val label: String, val icon: ImageVector)

/**
 * Floating liquid-glass tab bar. The selected blob is drawn from two independently sprung edges:
 * the leading edge races ahead and the trailing edge follows, so the blob stretches like a bead
 * of water sliding across glass, then settles round.
 */
@Composable
fun LiquidNavBar(items: List<NavItem>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val palette = Kairos.colors
    val reduced = Kairos.reducedMotion
    BoxWithConstraints(
        modifier
            .height(68.dp)
            .liquidGlass(CircleShape, blur = true, elevation = 22.dp)
            .padding(6.dp)
    ) {
        val count = items.size
        // Selected slot is wider so its label fits.
        val unit = 1f / (count + 1.2f)
        fun start(i: Int) = if (i <= selected) i * unit else (i + 1.2f) * unit
        val targetL = start(selected)
        val targetR = targetL + unit * 2.2f
        val fast = spring<Float>(dampingRatio = 0.72f, stiffness = 520f)
        val slow = spring<Float>(dampingRatio = 0.8f, stiffness = 170f)
        // Remember which way we're travelling so the correct edge leads.
        val previous = remember { intArrayOf(selected) }
        val movingRight = remember(selected) { (selected >= previous[0]).also { previous[0] = selected } }
        val snap = spring<Float>(stiffness = 2000f)
        val left by animateFloatAsState(targetL, if (reduced) snap else if (movingRight) slow else fast, label = "navL")
        val right by animateFloatAsState(targetR, if (reduced) snap else if (movingRight) fast else slow, label = "navR")
        val l = minOf(left, right - unit)
        val r = maxOf(right, l + unit)
        Box(
            Modifier
                .fillMaxSize()
                .drawBehind {
                    val x0 = l * size.width
                    val x1 = r * size.width
                    val h = size.height
                    drawRoundRect(palette.accent, Offset(x0, 0f), Size(x1 - x0, h), CornerRadius(h / 2))
                    drawRoundRect(
                        Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.35f), Color.Transparent), endY = h * 0.6f),
                        Offset(x0, 0f), Size(x1 - x0, h), CornerRadius(h / 2),
                    )
                }
        )
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            items.forEachIndexed { i, item ->
                val isSel = i == selected
                val tint by animateColorAsState(if (isSel) palette.onAccent else palette.inkSoft, label = "navTint")
                Row(
                    Modifier
                        .weight(if (isSel) 2.2f else 1f)
                        .fillMaxHeight()
                        .semantics { this.selected = isSel }
                        .squishClickable(role = Role.Tab, label = item.label) { onSelect(i) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(item.icon, item.label, tint = tint, modifier = Modifier.size(24.dp))
                    AnimatedVisibility(
                        isSel,
                        enter = fadeIn() + expandHorizontally(),
                        exit = fadeOut() + shrinkHorizontally(),
                    ) {
                        Row {
                            Spacer(Modifier.width(8.dp))
                            Text(item.label, style = Kairos.type.label, color = tint, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

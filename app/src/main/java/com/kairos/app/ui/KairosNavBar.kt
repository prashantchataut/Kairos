package com.kairos.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.kairos.app.core.design.KairosTheme
import com.kairos.app.core.design.glass
import com.kairos.app.core.design.pressScale

enum class Dest(val route: String, val label: String, val icon: ImageVector) {
    Today("today", "Today", Icons.Rounded.WbSunny),
    Words("words", "Words", Icons.Rounded.MenuBook),
    Capsules("capsules", "Capsules", Icons.Rounded.HourglassEmpty),
    Studio("studio", "Studio", Icons.Rounded.Palette),
    You("you", "You", Icons.Rounded.Person),
}

/**
 * The floating glass capsule. Inactive destinations are quiet circles; the active one grows
 * into an ink pill with its name, so the bar itself shows where you are without a separate
 * highlight rule.
 */
@Composable
fun KairosNavBar(
    current: String?,
    onSelect: (Dest) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = KairosTheme.colors
    Row(
        modifier = modifier
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .glass(
                shape = com.kairos.app.core.design.KairosShape.pill,
                fill = colors.glassFillStrong,
                elevation = 22.dp,
            )
            .padding(horizontal = 8.dp, vertical = 7.dp)
            .animateContentSize(animationSpec = spring(dampingRatio = 0.8f, stiffness = 380f)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Dest.entries.forEach { dest ->
            NavItem(
                dest = dest,
                selected = current == dest.route,
                onClick = { onSelect(dest) },
            )
        }
    }
}

@Composable
private fun NavItem(dest: Dest, selected: Boolean, onClick: () -> Unit) {
    val colors = KairosTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val background by animateColorAsState(
        targetValue = if (selected) colors.ink else Color.Transparent,
        animationSpec = tween(240),
        label = "nav-bg",
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.94f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 500f),
        label = "nav-scale",
    )
    Row(
        modifier = Modifier
            .pressScale(interaction, 0.92f)
            .clip(CircleShape)
            .background(background)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = if (selected) 15.dp else 13.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = dest.icon,
            contentDescription = dest.label,
            tint = if (selected) colors.paper else colors.inkFaint,
            modifier = Modifier.size((21 * iconScale).dp),
        )
        AnimatedVisibility(
            visible = selected,
            enter = expandHorizontally(tween(240)) + fadeIn(tween(200)),
            exit = shrinkHorizontally(tween(160)) + fadeOut(tween(120)),
        ) {
            Row {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = dest.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.paper,
                    maxLines = 1,
                )
            }
        }
    }
}

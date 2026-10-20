package com.kairos.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kairos.app.ui.art.drawDrop
import com.kairos.app.ui.glass.GlassWeight
import com.kairos.app.ui.glass.liquidGlass
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Motion
import kotlin.math.cos
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Save toggle. Saving pops the heart and flicks six droplets outward — the only "burst" in the
 * app, kept for the moment you tell Kairos something matters to you.
 */
@Composable
fun HeartButton(
    saved: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    label: String = "Save",
) {
    val colors = Kairos.colors
    val haptics = LocalHapticFeedback.current
    val scale = remember { Animatable(1f) }
    val burst = remember { Animatable(1f) }
    var bursts by remember { mutableIntStateOf(0) }
    val reduced = Kairos.reducedMotion
    LaunchedEffect(bursts) {
        if (bursts > 0 && !reduced) {
            burst.snapTo(0f)
            scale.snapTo(0.7f)
            coroutineScope {
                launch { scale.animateTo(1f, Motion.playful()) }
                launch { burst.animateTo(1f, tween(520, easing = Motion.Enter)) }
            }
        }
    }
    Box(
        modifier
            .size(size)
            .liquidGlass(androidx.compose.foundation.shape.CircleShape, GlassWeight.Regular)
            .toggleable(saved, remember { MutableInteractionSource() }, null, role = Role.Switch) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                if (!saved) bursts++
                onToggle()
            }
            .semantics { contentDescription = if (saved) "$label: saved" else label },
        contentAlignment = Alignment.Center,
    ) {
        val b = burst.value
        if (b < 1f) {
            Canvas(Modifier.fillMaxSize()) {
                val c = Offset(this.size.width / 2, this.size.height / 2)
                repeat(6) { i ->
                    val a = i / 6f * 6.283f - 1.57f
                    val d = this.size.width * (0.25f + 0.45f * b)
                    drawDrop(Offset(c.x + cos(a) * d, c.y + sin(a) * d), this.size.width * 0.05f * (1f - b), colors.danger.copy(alpha = 1f - b))
                }
            }
        }
        Icon(
            if (saved) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            null,
            tint = if (saved) colors.danger else colors.ink,
            modifier = Modifier.size(size * 0.46f).graphicsLayer { scaleX = scale.value; scaleY = scale.value },
        )
    }
}

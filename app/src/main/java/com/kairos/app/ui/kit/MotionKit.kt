package com.kairos.app.ui.kit

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Motion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Click with a squishy press (scale 0.96 on a stiff spring) and a light haptic.
 * High-frequency interaction, so the spring is fast and there is no ripple flash.
 */
fun Modifier.squishClick(
    enabled: Boolean = true,
    pressedScale: Float = 0.96f,
    role: Role = Role.Button,
    haptic: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && enabled) pressedScale else 1f, Motion.press(), label = "press")
    val hf = LocalHapticFeedback.current
    this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(
            interactionSource = source,
            indication = null,
            enabled = enabled,
            role = role,
        ) {
            if (haptic) hf.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        }
}

/**
 * Entrance: opacity + rise + a breath of scale, staggered by [index].
 * Plays once per composition; reduced motion shows content immediately.
 */
fun Modifier.rise(index: Int = 0, distance: Int = 18, baseDelay: Int = 0): Modifier = composed {
    val reduce = Kairos.reduceMotion
    val progress = remember { Animatable(if (reduce) 1f else 0f) }
    val px = with(LocalDensity.current) { distance.dp.toPx() }
    LaunchedEffect(Unit) {
        if (!reduce) {
            delay((baseDelay + index * 70).toLong())
            launch { progress.animateTo(1f, Motion.enter(640)) }
        }
    }
    graphicsLayer {
        val p = progress.value
        alpha = p
        translationY = (1f - p) * px
        val s = 0.96f + 0.04f * p
        scaleX = s
        scaleY = s
    }
}

@Composable
fun rememberHaptics() = LocalHapticFeedback.current

package com.kairos.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import com.kairos.app.ui.theme.Motion

/**
 * Press feedback used everywhere: the element squishes like a soft toy and springs back,
 * with a light haptic tick. No ripples — they fight the watercolour surfaces.
 */
fun Modifier.squishClickable(
    enabled: Boolean = true,
    pressedScale: Float = 0.94f,
    haptic: Boolean = true,
    role: Role = Role.Button,
    label: String? = null,
    onClick: () -> Unit,
): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && enabled) pressedScale else 1f, Motion.squish(), label = "squish")
    val haptics = LocalHapticFeedback.current
    this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(source, indication = null, enabled = enabled, role = role, onClickLabel = label) {
            if (haptic) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        }
}

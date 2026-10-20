package com.kairos.app.ui.glass

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Motion
import com.kairos.app.ui.theme.Radii
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/** The pond (and scrolling content) that glass surfaces can refract. Null in previews. */
val LocalHazeState = staticCompositionLocalOf<HazeState?> { null }

enum class GlassWeight { Clear, Regular, Frosted }

/**
 * Liquid glass: real backdrop blur where it carries hierarchy (bars, sheets, hero cards),
 * a frosted approximation everywhere else. Every pane gets a light rim that is brighter
 * at the top-left — the edge where light enters — plus a soft specular sheen.
 */
fun Modifier.liquidGlass(
    shape: Shape = Radii.card,
    weight: GlassWeight = GlassWeight.Regular,
    blur: Boolean = false,
    elevation: Dp = 0.dp,
    tint: Color? = null,
): Modifier = composed {
    val colors = Kairos.colors
    val haze = LocalHazeState.current
    val fill = tint ?: when (weight) {
        GlassWeight.Clear -> colors.glass.copy(alpha = colors.glass.alpha * 0.6f)
        GlassWeight.Regular -> colors.glass
        GlassWeight.Frosted -> colors.glassStrong
    }
    var m: Modifier = this
    if (elevation > 0.dp) {
        m = m.shadow(elevation, shape, clip = false, ambientColor = colors.shadow, spotColor = colors.shadow)
    }
    m = m.clip(shape)
    m = if (blur && haze != null) {
        m.hazeEffect(
            haze,
            HazeStyle(
                backgroundColor = colors.paper,
                tints = listOf(HazeTint(fill)),
                blurRadius = 26.dp,
                noiseFactor = 0.06f,
                fallbackTint = HazeTint(colors.glassStrong),
            ),
        )
    } else {
        m.background(fill)
    }
    m
        .drawWithContent {
            drawContent()
            // specular sheen: a wide, faint highlight across the top third
            drawRect(
                Brush.verticalGradient(
                    0f to Color.White.copy(alpha = if (colors.isDark) 0.10f else 0.32f),
                    0.38f to Color.Transparent,
                ),
            )
        }
        .border(
            BorderStroke(
                1.dp,
                Brush.linearGradient(
                    listOf(colors.rimTop, colors.rimBottom, colors.rimTop.copy(alpha = colors.rimTop.alpha * 0.4f)),
                    start = Offset.Zero,
                    end = Offset.Infinite,
                ),
            ),
            shape,
        )
}

/** Springy press feedback on any clickable: scales down instantly, settles back with a spring. */
fun Modifier.pressScale(interaction: MutableInteractionSource, pressed: Float = 0.95f): Modifier = composed {
    val isPressed by interaction.collectIsPressedAsState()
    val reduced = Kairos.reducedMotion
    val scale by animateFloatAsState(
        if (isPressed && !reduced) pressed else 1f,
        if (isPressed) Motion.snappy() else Motion.playful(),
        label = "press",
    )
    graphicsLayer { scaleX = scale; scaleY = scale }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = Radii.card,
    weight: GlassWeight = GlassWeight.Regular,
    blur: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val haptics = LocalHapticFeedback.current
    Box(
        modifier
            .then(if (onClick != null) Modifier.pressScale(interaction, 0.97f) else Modifier)
            .liquidGlass(shape, weight, blur)
            .then(
                if (onClick != null) Modifier.clickable(interaction, null) {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                } else Modifier,
            )
            .padding(contentPadding),
        content = content,
    )
}

@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    tint: Color = Kairos.colors.ink,
    blur: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val haptics = LocalHapticFeedback.current
    Box(
        modifier
            .size(size)
            .pressScale(interaction, 0.88f)
            .liquidGlass(CircleShape, GlassWeight.Regular, blur)
            .clickable(interaction, null, role = Role.Button) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(size * 0.46f))
    }
}

enum class ButtonTone { Pond, Glass, Ink }

/** The one obvious action on a screen. Pond-filled with a liquid highlight, or quiet glass. */
@Composable
fun KButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: ButtonTone = ButtonTone.Pond,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 15.dp),
) {
    val colors = Kairos.colors
    val interaction = remember { MutableInteractionSource() }
    val haptics = LocalHapticFeedback.current
    val alpha by animateFloatAsState(if (enabled) 1f else 0.45f, label = "btnAlpha")
    val content = when (tone) {
        ButtonTone.Pond -> colors.onPond
        ButtonTone.Glass -> colors.ink
        ButtonTone.Ink -> colors.paper
    }
    val base = Modifier
        .pressScale(interaction, 0.96f)
        .graphicsLayer { this.alpha = alpha }
    val surface = when (tone) {
        ButtonTone.Pond -> Modifier
            .shadow(14.dp, Radii.pill, ambientColor = colors.pond.copy(alpha = 0.5f), spotColor = colors.pond.copy(alpha = 0.5f))
            .clip(Radii.pill)
            .background(Brush.verticalGradient(listOf(colors.pond.copy(alpha = 0.92f).compositeOver(Color.White, 0.18f), colors.pondDeep)))
            .drawWithContent {
                drawContent()
                drawRect(Brush.verticalGradient(0f to Color.White.copy(alpha = 0.28f), 0.5f to Color.Transparent))
            }
            .border(1.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.55f), Color.White.copy(alpha = 0.05f))), Radii.pill)
        ButtonTone.Glass -> Modifier.liquidGlass(Radii.pill, GlassWeight.Frosted, blur = true)
        ButtonTone.Ink -> Modifier.clip(Radii.pill).background(colors.ink)
    }
    Row(
        modifier
            .then(base)
            .then(surface)
            .clickable(interaction, null, enabled = enabled, role = Role.Button) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .defaultMinSize(minHeight = 52.dp)
            .padding(contentPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = content, modifier = Modifier.size(20.dp))
            Box(Modifier.size(8.dp))
        }
        Text(text, style = Kairos.type.label.copy(fontSize = Kairos.type.body.fontSize), color = content, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

private fun Color.compositeOver(other: Color, amount: Float): Color = Color(
    red = red + (other.red - red) * amount,
    green = green + (other.green - green) * amount,
    blue = blue + (other.blue - blue) * amount,
    alpha = alpha,
)

/** Selectable glass chip. The selected state fills with pond ink and lifts slightly. */
@Composable
fun KChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: ImageVector? = null,
) {
    val colors = Kairos.colors
    val interaction = remember { MutableInteractionSource() }
    val bg by animateColorAsState(if (selected) colors.ink else Color.Transparent, label = "chipBg")
    val fg by animateColorAsState(if (selected) colors.paper else colors.ink, label = "chipFg")
    Row(
        modifier
            .pressScale(interaction, 0.93f)
            .liquidGlass(Radii.pill, GlassWeight.Regular)
            .background(bg, Radii.pill)
            .clickable(interaction, null, role = Role.Checkbox, onClick = onClick)
            .defaultMinSize(minHeight = 40.dp)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (leading != null) Icon(leading, null, tint = fg, modifier = Modifier.size(16.dp))
        Text(text, style = Kairos.type.label, color = fg)
    }
}

/** A row of chips that wraps nothing and scrolls horizontally when needed. */
@Composable
fun ChipRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, content = content)
}

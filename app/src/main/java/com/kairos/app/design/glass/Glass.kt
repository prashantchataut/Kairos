package com.kairos.app.design.glass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kairos.app.design.Kairos
import com.kairos.app.design.Radius
import com.kairos.app.design.Space
import kotlin.math.min

/**
 * Glass, honestly.
 *
 * Compose cannot sample the backdrop, so instead of faking a blur over a flat
 * fill we build depth the way paint does: a translucent pane, a lit top edge,
 * a shaded bottom edge, and a tinted shadow that picks up the mood. The result
 * reads as a sheet of glass resting on watercolour rather than a grey box.
 */

@Composable
fun glassFillBrush(tint: Color? = null, strong: Boolean = false): Brush {
    val c = Kairos.colors
    val fill = if (strong) c.glassFillStrong else c.glassFill
    val top = fill
    val bottom = (tint ?: c.accent).copy(
        alpha = if (c.dark) 0.10f else 0.10f,
    )
    return Brush.verticalGradient(
        colors = listOf(
            top,
            Color.White.copy(alpha = if (c.dark) 0.02f else 0.30f),
            bottom,
        ),
    )
}

@Composable
fun glassEdgeBrush(): Brush {
    val c = Kairos.colors
    return Brush.linearGradient(
        colors = listOf(
            c.glassEdge,
            c.glassEdge.copy(alpha = 0.22f),
            c.glassEdge.copy(alpha = 0.75f),
        ),
        start = Offset.Zero,
        end = Offset.Infinite,
    )
}

/**
 * The base surface. Everything that looks like glass in the app comes from
 * here — cards, bars, chips, sheets — so the material stays consistent.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(Radius.card),
    tint: Color? = null,
    strong: Boolean = false,
    border: Boolean = true,
    shadow: Dp = 18.dp,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    content: @Composable () -> Unit,
) {
    val c = Kairos.colors
    val haptics = Kairos.haptics
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.977f else 1f,
        animationSpec = Kairos.motion.settle(),
        label = "press",
    )

    val shapeMod = modifier
        .graphicsScale(scale)
        .shadow(
            elevation = if (c.dark) shadow * 0.2f else shadow,
            shape = shape,
            ambientColor = (tint ?: c.accent).copy(alpha = 0.55f),
            spotColor = (tint ?: c.accent).copy(alpha = 0.45f),
        )
        .clip(shape)
        .background(glassFillBrush(tint, strong))
        .then(
            if (border) Modifier.border(BorderStroke(1.dp, glassEdgeBrush()), shape) else Modifier
        )
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interaction,
                    indication = null,
                    onClickLabel = onClickLabel,
                    onClick = { haptics.tick(); onClick() },
                )
            } else Modifier
        )

    Box(shapeMod) {
        // liquid light: a faint diagonal sheen across the top third
        Box(
            Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    val h = size.height * 0.55f
                    drawRect(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (c.dark) 0.05f else 0.16f),
                                Color.Transparent,
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(size.width * 0.85f, h),
                        ),
                        size = Size(size.width, h),
                    )
                }
        )
        content()
    }
}

private fun Modifier.graphicsScale(scale: Float): Modifier =
    this.graphicsLayer { scaleX = scale; scaleY = scale }

/** Convenience: a glass card with standard padding. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(Radius.card),
    tint: Color? = null,
    strong: Boolean = false,
    padding: PaddingValues = PaddingValues(Space.md),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    GlassSurface(
        modifier = modifier,
        shape = shape,
        tint = tint,
        strong = strong,
        onClick = onClick,
    ) {
        Box(Modifier.padding(padding)) { content() }
    }
}

/** Solid accent button — the one place saturated pigment is allowed to fill. */
@Composable
fun SauceButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    color: Color? = null,
    textStyle: TextStyle? = null,
) {
    val c = Kairos.colors
    val haptics = Kairos.haptics
    val resolvedStyle = textStyle ?: Kairos.type.bodyStrong
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = Kairos.motion.playful(),
        label = "btn",
    )
    val bg = (color ?: c.accent).let { if (enabled) it else it.copy(alpha = 0.35f) }
    Row(
        modifier = modifier
            .graphicsScale(scale)
            .shadow(
                elevation = if (c.dark) 0.dp else 14.dp,
                shape = RoundedCornerShape(Radius.pill),
                ambientColor = bg.copy(alpha = 0.7f),
                spotColor = bg.copy(alpha = 0.8f),
            )
            .clip(RoundedCornerShape(Radius.pill))
            .background(
                Brush.verticalGradient(
                    listOf(bg.copy(alpha = 0.98f), bg),
                )
            )
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.5f), Color.White.copy(alpha = 0.05f))
                ),
                RoundedCornerShape(Radius.pill),
            )
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
            ) { haptics.success(); onClick() }
            .padding(horizontal = Space.lg, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(Space.xs, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val fg = if (enabled) c.onAccent else c.inkFaint
        if (icon != null) Icon(icon, null, tint = fg, modifier = Modifier.size(19.dp))
        Text(text, style = resolvedStyle, color = fg)
    }
}

@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    val c = Kairos.colors
    val haptics = Kairos.haptics
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.pill))
            .background(c.glassFill)
            .border(1.dp, c.glassEdge.copy(alpha = 0.6f), RoundedCornerShape(Radius.pill))
            .clickable { haptics.tick(); onClick() }
            .padding(horizontal = Space.md, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(Space.xs, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) Icon(icon, null, tint = c.ink, modifier = Modifier.size(17.dp))
        Text(text, style = Kairos.type.label, color = c.ink)
    }
}

@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    tint: Color? = null,
    active: Boolean = false,
) {
    val c = Kairos.colors
    val haptics = Kairos.haptics
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = Kairos.motion.playful(),
        label = "icon",
    )
    Box(
        modifier = modifier
            .graphicsScale(scale)
            .size(size)
            .clip(CircleShape)
            .background(if (active) (tint ?: c.accent) else c.glassFill)
            .border(1.dp, c.glassEdge.copy(alpha = 0.7f), CircleShape)
            .clickable(interactionSource = interaction, indication = null) {
                haptics.tick(); onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon, contentDescription,
            tint = when {
                active -> c.onAccent
                tint != null -> tint
                else -> c.ink
            },
            modifier = Modifier.size(size * 0.46f),
        )
    }
}

/** Pill chip used for filters and moods. */
@Composable
fun GlassChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    dot: Color? = null,
    icon: ImageVector? = null,
) {
    val c = Kairos.colors
    val haptics = Kairos.haptics
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f, Kairos.motion.settle(), label = "chip")
    Row(
        modifier = modifier
            .graphicsScale(scale)
            .clip(RoundedCornerShape(Radius.pill))
            .background(if (selected) c.ink else c.glassFill)
            .border(
                1.dp,
                if (selected) Color.Transparent else c.glassEdge.copy(alpha = 0.55f),
                RoundedCornerShape(Radius.pill),
            )
            .clickable(interactionSource = interaction, indication = null) {
                haptics.soft(); onClick()
            }
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val fg = if (selected) (if (c.dark) c.canvas else Color.White) else c.inkSoft
        if (dot != null) Box(Modifier.size(8.dp).clip(CircleShape).background(dot))
        if (icon != null) Icon(icon, null, tint = fg, modifier = Modifier.size(14.dp))
        Text(label, style = Kairos.type.label, color = fg)
    }
}

/** Label above a value, used in stat blocks. */
@Composable
fun StatBlock(value: String, label: String, modifier: Modifier = Modifier, accent: Color? = null) {
    val c = Kairos.colors
    androidx.compose.foundation.layout.Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = Kairos.type.title, color = accent ?: c.ink)
        Text(label, style = Kairos.type.caption, color = c.inkFaint)
    }
}

/** Inner well — used for inputs, quote bodies and anything that should read as recessed. */
@Composable
fun GlassWell(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(Radius.control),
    content: @Composable () -> Unit,
) {
    val c = Kairos.colors
    Box(
        modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        c.ink.copy(alpha = if (c.dark) 0.16f else 0.035f),
                        c.ink.copy(alpha = if (c.dark) 0.08f else 0.015f),
                    )
                )
            )
            .border(1.dp, c.hairline.copy(alpha = 0.7f), shape)
    ) { content() }
}

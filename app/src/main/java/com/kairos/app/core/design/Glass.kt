package com.kairos.app.core.design

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Liquid glass.
 *
 * A translucent pane with four things that make it read as glass rather than a grey box:
 *  1. a soft coloured shadow that lifts it off the paper,
 *  2. a vertical fill that thickens toward the bottom,
 *  3. a bright hairline that is strongest at the top edge,
 *  4. a sheen that fades out by 42% of the height.
 */
@Composable
fun Modifier.glass(
    shape: Shape = KairosShape.card,
    fill: Color = KairosTheme.colors.glassFill,
    stroke: Color = KairosTheme.colors.glassStroke,
    elevation: Dp = 14.dp,
    sheen: Boolean = true,
): Modifier {
    val colors = KairosTheme.colors
    val shaded = this
        .shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            ambientColor = colors.ink.copy(alpha = 0.10f),
            spotColor = colors.ink.copy(alpha = 0.16f),
        )
        .clip(shape)
        .background(
            Brush.verticalGradient(
                listOf(
                    fill.copy(alpha = fill.alpha * 0.72f),
                    fill,
                    fill.copy(alpha = (fill.alpha * 1.06f).coerceAtMost(1f)),
                ),
            ),
        )
        .border(
            BorderStroke(
                1.dp,
                Brush.verticalGradient(
                    listOf(stroke, stroke.copy(alpha = stroke.alpha * 0.18f)),
                ),
            ),
            shape,
        )
    return if (sheen) {
        shaded.drawWithContent {
            drawContent()
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = if (colors.dark) 0.07f else 0.34f),
                        Color.Transparent,
                    ),
                    startY = 0f,
                    endY = size.height * 0.42f,
                ),
                size = Size(size.width, size.height * 0.42f),
            )
        }
    } else {
        shaded
    }
}

/** A glass card with padding, for static content. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = KairosShape.card,
    fill: Color = KairosTheme.colors.glassFill,
    elevation: Dp = 14.dp,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.glass(shape = shape, fill = fill, elevation = elevation),
    ) {
        Box(modifier = Modifier.padding(contentPadding), content = content)
    }
}

/** A springy press response for any surface. */
@Composable
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.965f,
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 720f),
        label = "press",
    )
    return this.scale(scale)
}

/** A small glass capsule — filters, tags, counters. */
@Composable
fun GlassPill(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    accent: Color = KairosTheme.colors.ink,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = KairosTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val shape = KairosShape.pill
    Box(
        modifier = modifier
            .pressScale(interaction, 0.94f)
            .glass(
                shape = shape,
                fill = if (selected) accent else colors.glassFill,
                stroke = if (selected) accent.copy(alpha = 0f) else colors.glassStroke,
                elevation = if (selected) 8.dp else 2.dp,
                sheen = !selected,
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        content()
    }
}

/** Round shapes used by chips and avatars. */
internal val AvatarShape = RoundedCornerShape(percent = 50)

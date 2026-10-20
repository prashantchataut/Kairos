package com.kairos.app.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Radii
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeChild

/** The backdrop that glass surfaces blur. Provided by the root scaffold around the watercolour paper. */
val LocalHazeState = staticCompositionLocalOf<HazeState?> { null }

/**
 * Liquid glass: real backdrop blur (API 32+, a frosted tint below), a soft coloured shadow,
 * a light caustic across the top and a bevelled rim that is bright where light hits and
 * fades towards the bottom, the way thick glass catches light.
 *
 * [blur] should be off for repeated list items — the tint alone reads as glass and costs nothing.
 */
fun Modifier.liquidGlass(
    shape: Shape = Radii.Card,
    blur: Boolean = true,
    tint: Color? = null,
    elevation: Dp = 14.dp,
    strong: Boolean = false,
): Modifier = composed {
    val palette = Kairos.colors
    val haze = LocalHazeState.current
    val fill = tint ?: if (strong) palette.glassFillStrong else palette.glassFill
    this
        .then(if (elevation > 0.dp) Modifier.shadow(elevation, shape, clip = false, ambientColor = palette.glassShadow, spotColor = palette.glassShadow) else Modifier)
        .clip(shape)
        .then(
            if (blur && haze != null) {
                Modifier.hazeChild(
                    haze,
                    HazeStyle(
                        backgroundColor = palette.paper,
                        tint = HazeTint(fill),
                        blurRadius = 26.dp,
                        noiseFactor = 0.08f,
                        fallbackTint = HazeTint(if (strong) palette.glassFillStrong else palette.card),
                    ),
                )
            } else Modifier.background(if (blur) palette.card else fill)
        )
        .glassRim(shape)
}

/** Draws the caustic sheen and the bevelled rim on top of any content clipped to [shape]. */
fun Modifier.glassRim(shape: Shape): Modifier = composed {
    val palette = Kairos.colors
    drawWithContent {
        // Caustic: a soft white wash over the top of the surface.
        drawRect(
            Brush.verticalGradient(
                0f to Color.White.copy(alpha = if (palette.isDark) 0.10f else 0.32f),
                0.45f to Color.Transparent,
            )
        )
        drawContent()
        val outline = shape.createOutline(size, layoutDirection, this)
        val path = when (outline) {
            is Outline.Generic -> outline.path
            else -> Path().apply { addOutline(outline) }
        }
        val w = 1.4.dp.toPx()
        drawPath(
            path,
            brush = Brush.linearGradient(
                0f to palette.glassRimTop,
                0.35f to palette.glassRimTop.copy(alpha = palette.glassRimTop.alpha * 0.25f),
                0.7f to Color.Transparent,
                1f to palette.glassRimBottom,
                start = Offset(0f, 0f), end = Offset(size.width * 0.35f, size.height),
            ),
            style = Stroke(width = w * 2),
        )
    }
}

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = Radii.Card,
    blur: Boolean = true,
    tint: Color? = null,
    strong: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier.liquidGlass(shape, blur, tint, strong = strong), content = content)
}

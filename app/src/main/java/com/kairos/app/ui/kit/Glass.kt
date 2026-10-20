package com.kairos.app.ui.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.KairosColors
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/** The haze source for floating chrome — provided once at the app root. */
val LocalHazeState = staticCompositionLocalOf<HazeState?> { null }

val CardShape = RoundedCornerShape(28.dp)

/**
 * The liquid-glass rim: a bright specular edge on the top-left that fades round the shape,
 * plus a soft sheen across the upper third. This is what sells "glass" more than blur does.
 */
fun Modifier.glassRim(shape: Shape, k: KairosColors, width: Dp = 1.2.dp, sheen: Boolean = true): Modifier =
    drawWithContent {
        drawContent()
        if (sheen) {
            drawRect(
                Brush.verticalGradient(
                    0f to Color.White.copy(alpha = if (k.isDark) 0.06f else 0.28f),
                    0.45f to Color.Transparent,
                ),
            )
        }
        val outline = shape.createOutline(size, layoutDirection, this)
        drawOutline(
            outline,
            brush = Brush.linearGradient(
                0f to k.glassRim,
                0.35f to k.glassRim.copy(alpha = k.glassRim.alpha * 0.15f),
                0.7f to k.glassRim.copy(alpha = k.glassRim.alpha * 0.05f),
                1f to k.glassRim.copy(alpha = k.glassRim.alpha * 0.55f),
                start = Offset.Zero,
                end = Offset(size.width, size.height),
            ),
            style = Stroke(width.toPx()),
        )
    }

/** Real backdrop-blurred glass. Use for floating chrome only (nav, top bars, sheets). */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = CardShape,
    elevation: Dp = 18.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val k = Kairos.colors
    val haze = LocalHazeState.current
    Box(
        modifier
            .shadow(elevation, shape, ambientColor = k.shadow, spotColor = k.shadow)
            .clip(shape)
            .then(
                if (haze != null) {
                    Modifier.hazeEffect(haze) {
                        blurRadius = 28.dp
                        tints = listOf(HazeTint(k.glassTint))
                        noiseFactor = 0.06f
                    }
                } else {
                    Modifier.background(k.acrylic)
                },
            )
            .glassRim(shape, k),
        content = content,
    )
}

/** Frosted acrylic card — translucent paper with the glass rim, no blur, so text stays crisp. */
@Composable
fun AcrylicCard(
    modifier: Modifier = Modifier,
    shape: Shape = CardShape,
    tint: Color? = null,
    elevation: Dp = 10.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val k = Kairos.colors
    Box(
        modifier
            .shadow(elevation, shape, ambientColor = k.shadow, spotColor = k.shadow)
            .clip(shape)
            .background(k.acrylic)
            .then(if (tint != null) Modifier.background(tint.copy(alpha = if (k.isDark) 0.18f else 0.22f)) else Modifier)
            .glassRim(shape, k),
        content = content,
    )
}

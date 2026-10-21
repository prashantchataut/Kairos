package com.kairos.app.ui.components

import com.kairos.app.ui.theme.glassOnSkyRim
import com.kairos.app.ui.theme.glassOnSky
import com.kairos.app.ui.theme.skyFade
import com.kairos.app.ui.theme.canvas
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.kairos.app.ui.theme.Gap
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.Motion
import com.kairos.app.ui.theme.Radius
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Press feedback: a slight, quick compression and a light tick. No ripple. */
fun Modifier.pressable(
    enabled: Boolean = true,
    scale: Float = 0.97f,
    role: Role = Role.Button,
    label: String? = null,
    haptic: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed {
    val source = MutableInteractionSourceRemembered()
    val pressed by source.collectIsPressedAsState()
    val s by animateFloatAsState(if (pressed && enabled) scale else 1f, Motion.press(), label = "press")
    val haptics = LocalHapticFeedback.current
    graphicsLayer { scaleX = s; scaleY = s }
        .clickable(source, null, enabled = enabled, role = role, onClickLabel = label) {
            if (haptic) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        }
}

@Composable
private fun MutableInteractionSourceRemembered() = androidx.compose.runtime.remember { MutableInteractionSource() }

/**
 * The sky: a vertical gradient into the canvas with two soft light orbs for depth. It stays blue
 * through the content and only melts into the canvas in the last stretch, so sky sections end
 * with [SkyTail] of breathing room.
 */
val SkyTail = 64.dp

/** A solid sky panel (no fade), for cards that sit on the canvas. */
fun Modifier.skyPanel(): Modifier = composed {
    val p = K.colors
    background(Brush.verticalGradient(listOf(p.skyTop, androidx.compose.ui.graphics.lerp(p.skyTop, p.skyMid, 0.7f))))
}

fun Modifier.sky(height: Dp? = null): Modifier = composed {
    val p = K.colors
    drawBehind {
        val h = height?.toPx() ?: size.height
        drawRect(Brush.verticalGradient(0f to p.skyTop, 0.55f to p.skyMid, 0.84f to androidx.compose.ui.graphics.lerp(p.skyMid, p.skyFade, 0.45f), 1f to p.canvas, startY = 0f, endY = h), size = size.copy(height = h))
        drawCircle(Brush.radialGradient(listOf(Color.White.copy(alpha = if (p.isDark) 0.10f else 0.28f), Color.Transparent), Offset(size.width * 0.85f, h * 0.12f), size.width * 0.55f), size.width * 0.55f, Offset(size.width * 0.85f, h * 0.12f))
        drawCircle(Brush.radialGradient(listOf(Color(0xFFBFD7FF).copy(alpha = if (p.isDark) 0.08f else 0.35f), Color.Transparent), Offset(size.width * 0.1f, h * 0.5f), size.width * 0.6f), size.width * 0.6f, Offset(size.width * 0.1f, h * 0.5f))
    }
}

/** A white card with a soft, wide shadow. The main container on the canvas. */
@Composable
fun Card(modifier: Modifier = Modifier, shape: Shape = Radius.card, padding: Dp = Gap.xl, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val p = K.colors
    Column(
        modifier
            .then(if (onClick != null) Modifier.pressable(scale = 0.985f, haptic = false, onClick = onClick) else Modifier)
            .shadow(18.dp, shape, ambientColor = p.shadow, spotColor = p.shadow)
            .background(p.surface, shape)
            .padding(padding),
        content = content,
    )
}

/** Frosted tile on the sky, like the stat tiles in the reference. */
@Composable
fun GlassTile(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable BoxScope.() -> Unit) {
    val p = K.colors
    Box(
        modifier
            .then(if (onClick != null) Modifier.pressable(onClick = onClick) else Modifier)
            .clip(Radius.tile)
            .background(p.glassOnSky)
            .border(1.dp, Brush.verticalGradient(listOf(p.glassOnSkyRim, p.glassOnSkyRim.copy(alpha = 0.08f))), Radius.tile),
        content = content,
    )
}

/** Glass tile with an icon over a label: the quick actions under the hero. */
@Composable
fun GlassAction(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier, active: Boolean = false) {
    val p = K.colors
    GlassTile(modifier.height(84.dp), onClick = onClick) {
        Column(Modifier.fillMaxSize().padding(Gap.md), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = if (active) Color(0xFFFFD1DC) else p.onSky, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(Gap.sm))
            Text(label, style = K.type.label, color = p.onSky)
        }
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, enabled: Boolean = true, dark: Boolean = false) {
    val p = K.colors
    val bg by animateColorAsState(
        when {
            !enabled -> p.inkFaint.copy(alpha = 0.35f)
            dark -> p.ink
            else -> p.accent
        }, label = "btn",
    )
    val fg = if (dark) p.canvas else p.onAccent
    Row(
        modifier
            .pressable(enabled = enabled, onClick = onClick)
            .defaultMinSize(minHeight = 52.dp)
            .clip(CircleShape)
            .background(bg)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) { Icon(icon, null, tint = fg, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(Gap.sm)) }
        Text(text, style = K.type.label.copy(fontSize = K.type.body.fontSize), color = fg)
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, onSky: Boolean = false) {
    val p = K.colors
    Row(
        modifier
            .pressable(onClick = onClick)
            .defaultMinSize(minHeight = 48.dp)
            .clip(CircleShape)
            .background(if (onSky) p.glassOnSky else p.surfaceSoft)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) { Icon(icon, null, tint = if (onSky) p.onSky else p.ink, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(Gap.sm)) }
        Text(text, style = K.type.label, color = if (onSky) p.onSky else p.ink)
    }
}

@Composable
fun RoundIcon(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier, onSky: Boolean = false, size: Dp = 44.dp, tint: Color? = null) {
    val p = K.colors
    Box(
        modifier
            .size(size)
            .pressable(label = label, onClick = onClick)
            .clip(CircleShape)
            .background(if (onSky) p.glassOnSky else p.surface)
            .then(if (onSky) Modifier.border(1.dp, p.glassOnSkyRim, CircleShape) else Modifier.border(1.dp, p.line, CircleShape)),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, label, tint = tint ?: if (onSky) p.onSky else p.ink, modifier = Modifier.size(size * 0.45f)) }
}

/** Segmented control; the white pill slides between options. [onSky] uses the glass variant. */
@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier, onSky: Boolean = false) {
    val p = K.colors
    BoxWithConstraints(
        modifier
            .height(44.dp)
            .clip(CircleShape)
            .background(if (onSky) p.glassOnSky else p.surfaceSoft)
            .padding(4.dp)
    ) {
        val w = maxWidth / options.size
        val x by androidx.compose.animation.core.animateDpAsState(w * selected, Motion.soft(), label = "seg")
        Box(
            Modifier
                .offset(x = x)
                .width(w)
                .fillMaxHeight()
                .shadow(if (onSky) 0.dp else 4.dp, CircleShape, ambientColor = p.shadow, spotColor = p.shadow)
                .clip(CircleShape)
                .background(if (onSky) Color.White.copy(alpha = 0.32f) else if (p.isDark) p.line else p.surface)
        )
        Row(Modifier.fillMaxSize()) {
            options.forEachIndexed { i, label ->
                val c by animateColorAsState(
                    when {
                        onSky -> if (i == selected) p.onSky else p.onSkySoft
                        i == selected -> p.ink
                        else -> p.inkFaint
                    }, label = "segc",
                )
                Box(Modifier.weight(1f).fillMaxHeight().pressable(scale = 0.98f) { onSelect(i) }, contentAlignment = Alignment.Center) {
                    Text(label, style = K.type.label, color = c)
                }
            }
        }
    }
}

@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, leading: ImageVector? = null) {
    val p = K.colors
    val bg by animateColorAsState(if (selected) p.ink else p.surface, Motion.fade(160), label = "chip")
    val fg by animateColorAsState(if (selected) p.canvas else p.ink, Motion.fade(160), label = "chipFg")
    Row(
        modifier
            .pressable(role = Role.Checkbox, onClick = onClick)
            .defaultMinSize(minHeight = 40.dp)
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, if (selected) Color.Transparent else p.line, CircleShape)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) { Icon(leading, null, tint = fg, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(6.dp)) }
        Text(text, style = K.type.label, color = fg)
    }
}

/** Small status label: a coloured dot and text, like "Normal" in the reference. */
@Composable
fun Status(text: String, color: Color, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(text, style = K.type.caption, color = K.colors.inkSoft)
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = K.type.title, color = K.colors.ink, modifier = Modifier.weight(1f))
        if (action != null && onAction != null) Text(action, style = K.type.label, color = K.colors.accent, modifier = Modifier.pressable(onClick = onAction))
    }
}

/** Placeholder bars that breathe while the curator writes. */
@Composable
fun Shimmer(modifier: Modifier = Modifier, onSky: Boolean = false, shape: Shape = Radius.chip) {
    val p = K.colors
    val calm = K.calm
    val a by rememberInfiniteTransition(label = "shimmer").animateFloat(0.35f, 0.75f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "a")
    Box(modifier.graphicsLayer { alpha = if (calm) 0.55f else a }.clip(shape).background(if (onSky) Color.White.copy(alpha = 0.35f) else p.surfaceSoft))
}


@Composable
fun Empty(title: String, body: String, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    Column(modifier.fillMaxWidth().padding(Gap.xl), horizontalAlignment = Alignment.CenterHorizontally) {
        KairosMark(44.dp, tinted = true)
        Spacer(Modifier.height(Gap.lg))
        Text(title, style = K.type.title, color = K.colors.ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(Gap.xs))
        Text(body, style = K.type.body, color = K.colors.inkSoft, textAlign = TextAlign.Center)
        if (action != null) { Spacer(Modifier.height(Gap.lg)); action() }
    }
}

/**
 * The Kairos mark: eight rounded rays — a moment opening up. [tinted] paints it cobalt on a soft
 * badge; otherwise it is drawn in [color] alone (for use on the sky).
 */
@Composable
fun KairosMark(size: Dp, modifier: Modifier = Modifier, color: Color = Color.White, tinted: Boolean = false) {
    val p = K.colors
    Canvas(modifier.size(size)) {
        if (tinted) {
            drawCircle(Brush.linearGradient(listOf(Color(0xFF5B9BFF), Color(0xFF2D6BFF), Color(0xFF6A5CFF)), Offset.Zero, Offset(this.size.width, this.size.height)))
            drawMark(Color.White, inset = 0.24f)
        } else drawMark(color, inset = 0f)
    }
}

fun DrawScope.drawMark(color: Color, inset: Float) {
    val r = size.minDimension / 2f
    val c = Offset(size.width / 2, size.height / 2)
    val outer = r * (1f - inset)
    val inner = outer * 0.18f
    val w = outer * 0.2f
    for (i in 0 until 8) {
        val a = (i * 45f - 90f) * (PI / 180f).toFloat()
        val len = if (i % 2 == 0) outer - w / 2 else (outer - w / 2) * 0.78f
        drawLine(color, Offset(c.x + cos(a) * inner, c.y + sin(a) * inner), Offset(c.x + cos(a) * len, c.y + sin(a) * len), strokeWidth = w, cap = StrokeCap.Round)
    }
}

@Composable
fun Wordmark(modifier: Modifier = Modifier, color: Color = K.colors.ink, markColor: Color = color) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        KairosMark(18.dp, color = markColor)
        Spacer(Modifier.width(7.dp))
        Text("Kairos", style = K.type.title.copy(fontFamily = com.kairos.app.ui.theme.Serif, fontWeight = androidx.compose.ui.text.font.FontWeight.Normal, fontSize = K.type.title.fontSize * 1.25f), color = color)
    }
}

/** A thin rounded progress track. */
@Composable
fun Progress(value: Float, modifier: Modifier = Modifier, brush: Brush? = null) {
    val p = K.colors
    val v by animateFloatAsState(value.coerceIn(0f, 1f), Motion.settle(), label = "progress")
    Box(modifier.height(8.dp).clip(CircleShape).background(p.surfaceSoft)) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(v.coerceAtLeast(0.02f)).clip(CircleShape).background(brush ?: Brush.horizontalGradient(listOf(Color(0xFFFFB86B), p.accent))))
    }
}


package com.kairos.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kairos.app.ui.brand.drawSparkle
import com.kairos.app.ui.glass.liquidGlass
import com.kairos.app.ui.icons.KairosIcons
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Motion
import com.kairos.app.ui.theme.Radii
import com.kairos.app.ui.theme.Space
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** The one filled button style: cobalt ink with a glassy top highlight. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    color: Color = Kairos.colors.accent,
    contentColor: Color = Kairos.colors.onAccent,
) {
    val bg by animateColorAsState(if (enabled) color else Kairos.colors.inkFaint.copy(alpha = 0.4f), label = "btn")
    Row(
        modifier
            .squishClickable(enabled = enabled, onClick = onClick)
            .defaultMinSize(minHeight = 54.dp)
            .background(bg, CircleShape)
            .drawBehind {
                drawRoundRect(
                    Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.28f), Color.Transparent), endY = size.height * 0.55f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2),
                )
            }
            .border(1.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.5f), Color.Transparent)), CircleShape)
            .padding(horizontal = 26.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = contentColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(Space.sm))
        }
        Text(text, style = Kairos.type.label.copy(fontSize = Kairos.type.body.fontSize), color = contentColor)
    }
}

/** Secondary action: a pill of liquid glass. */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    blur: Boolean = false,
) {
    Row(
        modifier
            .squishClickable(onClick = onClick)
            .defaultMinSize(minHeight = 48.dp)
            .liquidGlass(CircleShape, blur = blur, elevation = 6.dp, strong = true)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = Kairos.colors.ink, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Space.sm))
        }
        Text(text, style = Kairos.type.label, color = Kairos.colors.ink)
    }
}

/** Round glass icon button, 48dp touch target. */
@Composable
fun IconBubble(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    tint: Color = Kairos.colors.ink,
    blur: Boolean = false,
) {
    Box(
        modifier
            .size(size)
            .squishClickable(label = contentDescription, onClick = onClick)
            .liquidGlass(CircleShape, blur = blur, elevation = 4.dp, strong = true),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(size * 0.46f))
    }
}

@Composable
fun SelectChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
) {
    val palette = Kairos.colors
    val bg by animateColorAsState(if (selected) palette.ink else palette.glassFillStrong, Motion.fade(Motion.Quick), label = "chipBg")
    val fg by animateColorAsState(if (selected) palette.paper else palette.ink, Motion.fade(Motion.Quick), label = "chipFg")
    Row(
        modifier
            .squishClickable(role = Role.Checkbox, onClick = onClick)
            .defaultMinSize(minHeight = 44.dp)
            .background(bg, CircleShape)
            .border(1.dp, if (selected) Color.Transparent else palette.glassRimTop.copy(alpha = 0.7f), CircleShape)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        leading?.invoke()
        Text(text, style = Kairos.type.label, color = fg)
    }
}

/** A segmented control whose selected pill slides between options. */
@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val palette = Kairos.colors
    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier
            .height(46.dp)
            .liquidGlass(CircleShape, blur = false, elevation = 0.dp)
            .padding(4.dp)
    ) {
        val w = maxWidth / options.size
        val x by androidx.compose.animation.core.animateDpAsState(w * selected, Motion.squish(), label = "seg")
        Box(
            Modifier
                .graphicsLayer { translationX = x.toPx() }
                .width(w)
                .fillMaxSize()
                .background(palette.card, CircleShape)
                .border(1.dp, palette.glassRimTop, CircleShape)
        )
        Row(Modifier.fillMaxSize()) {
            options.forEachIndexed { i, label ->
                val c by animateColorAsState(if (i == selected) palette.ink else palette.inkSoft, label = "segText")
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .squishClickable(haptic = true, pressedScale = 0.97f) { onSelect(i) },
                    contentAlignment = Alignment.Center,
                ) { Text(label, style = Kairos.type.label, color = c) }
            }
        }
    }
}

@Composable
fun ScreenTitle(title: String, modifier: Modifier = Modifier, subtitle: String? = null) {
    Column(modifier) {
        Text(title, style = Kairos.type.display, color = Kairos.colors.ink)
        if (subtitle != null) {
            Spacer(Modifier.height(Space.xs))
            Text(subtitle, style = Kairos.type.body, color = Kairos.colors.inkSoft)
        }
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, trailing: (@Composable RowScope.() -> Unit)? = null) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(text, style = Kairos.type.title, color = Kairos.colors.ink, modifier = Modifier.weight(1f))
        trailing?.invoke(this)
    }
}

/**
 * Heart that fills with a squish and throws a ring of paint flecks when saved.
 */
@Composable
fun HeartButton(
    saved: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    glass: Boolean = true,
) {
    val palette = Kairos.colors
    var bursts by remember { mutableIntStateOf(0) }
    val scale = remember { Animatable(1f) }
    LaunchedEffect(saved) {
        if (saved) {
            scale.snapTo(0.6f)
            scale.animateTo(1f, Motion.squish())
        }
    }
    Box(
        modifier
            .size(size)
            .squishClickable(label = if (saved) "Remove from saved" else "Save") {
                if (!saved) bursts++
                onToggle()
            }
            .then(if (glass) Modifier.liquidGlass(CircleShape, blur = false, elevation = 4.dp, strong = true) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        if (bursts > 0) PaintBurst(key = bursts, color = palette.danger, modifier = Modifier.size(size * 2.2f))
        Icon(
            if (saved) KairosIcons.HeartFilled else KairosIcons.Heart,
            contentDescription = null,
            tint = if (saved) palette.danger else palette.ink,
            modifier = Modifier
                .size(size * 0.48f)
                .graphicsLayer { scaleX = scale.value; scaleY = scale.value },
        )
    }
}

/** A one-shot ring of paint flecks and sparkles. Re-runs whenever [key] changes. */
@Composable
fun PaintBurst(key: Any, color: Color, modifier: Modifier = Modifier, count: Int = 10) {
    val reduced = Kairos.reducedMotion
    val washes = Kairos.colors.washes
    val progress = remember(key) { Animatable(0f) }
    LaunchedEffect(key) { if (!reduced) progress.animateTo(1f, tween(650, easing = Motion.Emphasized)) }
    if (reduced || progress.value >= 1f) return
    val rnd = remember(key) { Random(key.hashCode()) }
    val specs = remember(key) { List(count) { Triple(rnd.nextFloat() * 2f * PI.toFloat(), 0.7f + rnd.nextFloat() * 0.5f, rnd.nextInt(3)) } }
    Canvas(modifier) {
        val p = progress.value
        val c = Offset(size.width / 2, size.height / 2)
        val maxR = size.minDimension / 2
        specs.forEachIndexed { i, (a, dist, kind) ->
            val r = maxR * dist * p
            val o = Offset(c.x + cos(a) * r, c.y + sin(a) * r)
            val alpha = (1f - p).coerceIn(0f, 1f)
            val tint = if (i % 3 == 0) color else washes[i % washes.size]
            when (kind) {
                0 -> drawSparkle(o, 5.dp.toPx() * (1f - p * 0.5f), tint.copy(alpha = alpha))
                else -> drawCircle(tint.copy(alpha = alpha * 0.9f), 3.dp.toPx() * (1f - p * 0.6f), o)
            }
        }
        drawCircle(color.copy(alpha = (1f - p) * 0.35f), maxR * 0.45f * p, c, style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
    }
}

/** A small glass pill showing a label, e.g. pronunciation or a category. */
@Composable
fun Pill(text: String, modifier: Modifier = Modifier, icon: ImageVector? = null, onClick: (() -> Unit)? = null) {
    Row(
        modifier
            .then(if (onClick != null) Modifier.squishClickable(onClick = onClick) else Modifier)
            .liquidGlass(CircleShape, blur = false, elevation = 0.dp, strong = true)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(text, style = Kairos.type.caption, color = Kairos.colors.inkSoft)
        if (icon != null) Icon(icon, null, tint = Kairos.colors.inkSoft, modifier = Modifier.size(15.dp))
    }
}

/** Gentle empty state: Drop plus one sentence and, ideally, the next action. */
@Composable
fun EmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Column(modifier.padding(Space.xl), horizontalAlignment = Alignment.CenterHorizontally) {
        com.kairos.app.ui.mascot.DropMascot(size = 92.dp, expression = com.kairos.app.ui.mascot.Expression.Calm)
        Spacer(Modifier.height(Space.lg))
        Text(title, style = Kairos.type.title, color = Kairos.colors.ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(Space.xs))
        Text(body, style = Kairos.type.body, color = Kairos.colors.inkSoft, textAlign = TextAlign.Center)
        if (action != null) {
            Spacer(Modifier.height(Space.lg))
            action()
        }
    }
}

/** Fades and pops content in once, used for the single orchestrated entrance on each screen. */
@Composable
fun PopIn(visible: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible, modifier,
        enter = fadeIn(Motion.fade()) + scaleIn(Motion.settle(), initialScale = 0.92f),
        exit = fadeOut(Motion.fade(Motion.Quick)) + scaleOut(targetScale = 0.96f),
    ) { content() }
}

val BottomBarSpace = PaddingValues(bottom = 120.dp)

@Composable
fun Glow(modifier: Modifier = Modifier, color: Color) {
    Box(modifier.background(Brush.radialGradient(listOf(color.copy(alpha = 0.45f), Color.Transparent)), CircleShape))
}


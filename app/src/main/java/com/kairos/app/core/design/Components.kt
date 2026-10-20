package com.kairos.app.core.design

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** The primary action: an ink pill with a light travelling across its top edge. */
@Composable
fun InkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    fill: Color = KairosTheme.colors.ink,
    contentColor: Color = KairosTheme.colors.paper,
) {
    val interaction = remember { MutableInteractionSource() }
    val colors = KairosTheme.colors
    Box(
        modifier = modifier
            .height(56.dp)
            .pressScale(interaction, 0.97f)
            .clip(KairosShape.pill)
            .background(fill.copy(alpha = if (enabled) 1f else 0.35f))
            .border(1.dp, Color.White.copy(alpha = if (colors.dark) 0.14f else 0.24f), KairosShape.pill)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled && !loading,
                onClick = onClick,
            )
            .padding(horizontal = 26.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = contentColor,
                )
            } else {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(9.dp))
                }
                Text(
                    text = text,
                    color = contentColor,
                    style = MaterialTypeLabel(),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun MaterialTypeLabel() = androidx.compose.material3.MaterialTheme.typography.labelLarge

/** Secondary action: glass pill with a hairline. */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    selected: Boolean = false,
) {
    val colors = KairosTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .height(50.dp)
            .pressScale(interaction, 0.96f)
            .glass(
                shape = KairosShape.pill,
                fill = if (selected) colors.ink else colors.glassFillStrong,
                elevation = 6.dp,
                sheen = !selected,
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        val tint = if (selected) colors.paper else colors.ink
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text = text, color = tint, style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
    }
}

/** Circular icon button in glass. */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: Dp = 46.dp,
    tint: Color = KairosTheme.colors.ink,
    filled: Boolean = false,
) {
    val colors = KairosTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(size)
            .pressScale(interaction, 0.9f)
            .glass(
                shape = CircleShape,
                fill = if (filled) colors.ink else colors.glassFillStrong,
                elevation = if (filled) 10.dp else 3.dp,
                sheen = !filled,
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (filled) colors.paper else tint,
            modifier = Modifier.size(size * 0.44f),
        )
    }
}

/** Back button with a soft shadow, used at the top of detail screens. */
@Composable
fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    GlassIconButton(
        icon = Icons.Rounded.ArrowBack,
        onClick = onClick,
        modifier = modifier,
        contentDescription = "Back",
        size = 44.dp,
    )
}

/** Section label: a tiny eyebrow, an editorial headline, optional trailing action. */
@Composable
fun SectionHeader(
    eyebrow: String?,
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    titleColor: Color = KairosTheme.colors.ink,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (eyebrow != null) {
                Text(
                    text = eyebrow.uppercase(),
                    style = KairosType.eyebrow,
                    color = KairosTheme.colors.inkFaint,
                )
                Spacer(Modifier.height(4.dp))
            }
            Text(
                text = title,
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                color = titleColor,
            )
        }
        if (trailing != null) trailing()
    }
}

/** A progress ring with a soft track. */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    stroke: Dp = 6.dp,
    trackColor: Color = KairosTheme.colors.hairline,
    brush: Brush = Brush.linearGradient(
        listOf(KairosTheme.colors.apricot, KairosTheme.colors.blossom),
    ),
    content: (@Composable () -> Unit)? = null,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(720),
        label = "ring",
    )
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(Modifier.size(size)) {
            val strokePx = stroke.toPx()
            drawArc(
                color = trackColor.copy(alpha = 0.5f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(strokePx, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                topLeft = androidx.compose.ui.geometry.Offset(strokePx / 2, strokePx / 2),
                size = androidx.compose.ui.geometry.Size(this.size.width - strokePx, this.size.height - strokePx),
            )
            if (animated > 0.001f) {
                drawArc(
                    brush = brush,
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(strokePx, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                    topLeft = androidx.compose.ui.geometry.Offset(strokePx / 2, strokePx / 2),
                    size = androidx.compose.ui.geometry.Size(this.size.width - strokePx, this.size.height - strokePx),
                )
            }
        }
        content?.invoke()
    }
}

/** Small checkmark used in lists and confirmations. */
@Composable
fun TickBadge(modifier: Modifier = Modifier, tint: Color = KairosTheme.colors.mint) {
    Box(
        modifier = modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.22f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.Check, contentDescription = null, tint = tint, modifier = Modifier.size(13.dp))
    }
}

/** A quiet, centred empty state starring Kai. */
@Composable
fun EmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    mood: KaiMood = KaiMood.Curious,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Kai(modifier = Modifier.size(132.dp), mood = mood)
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
            color = KairosTheme.colors.ink,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = body,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = KairosTheme.colors.inkSoft,
            textAlign = TextAlign.Center,
        )
        if (action != null) {
            Spacer(Modifier.height(20.dp))
            action()
        }
    }
}

/** Divider that looks like a brush hair, not a rule. */
@Composable
fun Hairline(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(Color.Transparent, KairosTheme.colors.hairline, Color.Transparent),
                ),
            ),
    )
}

/** A tag with a hand-drawn feel. */
@Composable
fun Tag(text: String, modifier: Modifier = Modifier, tint: Color = KairosTheme.colors.inkSoft) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(tint.copy(alpha = 0.10f))
            .padding(horizontal = 9.dp, vertical = 4.dp),
    ) {
        Text(text = text, style = KairosType.handSmall, color = tint, fontSize = 16.sp)
    }
}

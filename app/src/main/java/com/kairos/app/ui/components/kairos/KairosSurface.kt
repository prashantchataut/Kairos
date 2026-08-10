package com.kairos.app.ui.components.kairos

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kairos.app.ui.animation.kairosScale
import com.kairos.app.ui.animation.rememberKairosPressScale
import com.kairos.app.ui.theme.KairosElevation
import com.kairos.app.ui.theme.KairosRadius
import com.kairos.app.ui.theme.KairosSpacing
import com.kairos.app.ui.theme.KairosTheme

/**
 * Edge-to-edge ambient background for the focused product surface.
 * Flat ground; screens add their own atmospheric glows.
 */
@Composable
fun KairosAppBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        content = content
    )
}

/**
 * Frosted glass panel: translucent tonal fill, hairline border with a bright
 * top edge, soft tinted shadow, and a gentle top sheen. The name is kept for
 * compatibility; the material is the Moment Blue frosted glass.
 */
@Composable
fun KairosGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(KairosRadius.controlLarge),
    strong: Boolean = false,
    elevation: Dp = KairosElevation.glass,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val glass = KairosTheme.glass
    val scheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val pressScale = rememberKairosPressScale(
        interactionSource = interactionSource,
        pressedScale = if (onClick != null) 0.985f else 1f
    )
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            role = Role.Button,
            onClick = onClick
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .kairosScale(pressScale)
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = glass.shadow,
                spotColor = glass.shadow
            )
            .clip(shape)
            .background(
                if (strong) glass.fillStrong else glass.fill,
                shape
            )
            .border(
                BorderStroke(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(glass.highlight, glass.border)
                    )
                ),
                shape = shape
            )
            .then(clickableModifier)
            .padding(contentPadding)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(scheme.surface.copy(alpha = 0.06f), Color.Transparent),
                        startY = 0f,
                        endY = 200f
                    )
                )
        )
        content()
    }
}

@Composable
fun KairosIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme
    KairosGlassSurface(
        modifier = modifier.size(48.dp),
        shape = RoundedCornerShape(KairosRadius.controlLarge),
        strong = selected,
        onClick = onClick
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (selected) scheme.primary else scheme.onSurfaceVariant,
                modifier = Modifier.size(21.dp)
            )
        }
    }
}

/**
 * Object-like reading card: large radius, soft depth, no hard borders.
 */
@Composable
fun KairosReadingSurface(
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    contentPadding: PaddingValues = PaddingValues(KairosSpacing.xl),
    content: @Composable BoxScope.() -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(KairosRadius.readingSurface)
    Box(
        modifier = modifier
            .shadow(
                elevation = 3.dp,
                shape = shape,
                ambientColor = Color(0x1F1B2A4A),
                spotColor = Color(0x261B2A4A)
            )
            .clip(shape)
            .background(scheme.surface, shape)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun KairosPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        enabled = enabled,
        shape = RoundedCornerShape(KairosRadius.controlLarge),
        contentPadding = PaddingValues(horizontal = 18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        }
        Text(
            text = text,
            modifier = if (icon != null) Modifier.padding(start = 8.dp) else Modifier,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun KairosSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        enabled = enabled,
        shape = RoundedCornerShape(KairosRadius.controlLarge),
        contentPadding = PaddingValues(horizontal = 18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        }
        Text(
            text = text,
            modifier = if (icon != null) Modifier.padding(start = 8.dp) else Modifier,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun KairosSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(KairosRadius.controlLarge))
            .background(scheme.surfaceContainer, RoundedCornerShape(KairosRadius.controlLarge)),
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items.forEachIndexed { index, item ->
                    val selected = selectedIndex == index
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .semantics {
                                role = Role.Tab
                                this.selected = selected
                                stateDescription = if (selected) "Selected" else "Not selected"
                            },
                        shape = RoundedCornerShape(KairosRadius.control),
                        color = if (selected) scheme.primary else Color.Transparent,
                        contentColor = if (selected) scheme.onPrimary else scheme.onSurfaceVariant,
                        onClick = { onSelected(index) }
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = item,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun KairosEmptyState(
    icon: ImageVector,
    title: String,
    body: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = KairosSpacing.xl, vertical = KairosSpacing.section),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(KairosSpacing.md)
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 360.dp)
        )
        KairosPrimaryButton(text = actionLabel, onClick = onAction)
    }
}

@Composable
fun KairosSkeletonList(
    modifier: Modifier = Modifier,
    rows: Int = 4
) {
    val transition = rememberInfiniteTransition(label = "kairos-skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.36f,
        targetValue = 0.72f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton-alpha"
    )
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        repeat(rows) { index ->
            val widthFraction = when (index % 3) {
                0 -> 0.74f
                1 -> 0.58f
                else -> 0.82f
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    Modifier
                        .fillMaxWidth(widthFraction)
                        .height(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = alpha))
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = alpha * 0.75f))
                )
                Box(
                    Modifier
                        .fillMaxWidth(0.66f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = alpha * 0.7f))
                )
            }
        }
    }
}

@Composable
fun KairosActionRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    KairosGlassSurface(
        modifier = modifier.fillMaxWidth(),
        strong = true,
        shape = RoundedCornerShape(KairosRadius.controlLarge),
        contentPadding = PaddingValues(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

package com.kairos.app.ui.kit

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Motion

/** Primary action: a pill of navy ink. One per screen. */
@Composable
fun InkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    color: Color? = null,
) {
    val k = Kairos.colors
    val bg = color ?: k.ink
    val fg = if (color == null) k.paper else Color.White
    Row(
        modifier
            .defaultMinSize(minHeight = 56.dp)
            .shadow(if (enabled) 14.dp else 0.dp, CircleShape, ambientColor = bg.copy(alpha = 0.4f), spotColor = bg.copy(alpha = 0.4f))
            .clip(CircleShape)
            .background(if (enabled) bg else k.inkFaint.copy(alpha = 0.4f))
            .glassRim(CircleShape, k.copy(glassRim = Color.White.copy(alpha = 0.35f)), sheen = true)
            .squishClick(enabled = enabled, onClick = onClick)
            .padding(horizontal = 26.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, color = fg)
    }
}

/** Secondary action: frosted pill. */
@Composable
fun SoftButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val k = Kairos.colors
    Row(
        modifier
            .defaultMinSize(minHeight = 52.dp)
            .clip(CircleShape)
            .background(k.acrylic)
            .glassRim(CircleShape, k, sheen = false)
            .squishClick(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = if (enabled) k.ink else k.inkFaint, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, color = if (enabled) k.ink else k.inkFaint)
    }
}

/** Round glass icon button, 48dp target. */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    tint: Color? = null,
) {
    val k = Kairos.colors
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(k.acrylic)
            .glassRim(CircleShape, k)
            .squishClick(pressedScale = 0.9f, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = tint ?: k.ink, modifier = Modifier.size(22.dp))
    }
}

/** A selectable pill. Selected chips are filled with butter like a highlighter swipe. */
@Composable
fun SoftChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    selectedColor: Color? = null,
) {
    val k = Kairos.colors
    val fill by animateColorAsState(
        if (selected) (selectedColor ?: k.washes.butter) else k.acrylic, Motion.ui(), label = "chip",
    )
    Row(
        modifier
            .height(40.dp)
            .clip(CircleShape)
            .background(fill)
            .glassRim(CircleShape, k, sheen = false)
            .squishClick(role = androidx.compose.ui.semantics.Role.Checkbox, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = if (selected && !k.isDark) Color(0xFF1D2640) else k.ink, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected && !k.isDark) Color(0xFF1D2640) else k.ink,
            maxLines = 1,
        )
    }
}

/** Segmented control with a sliding white drop, like the nav bar's little sibling. */
@Composable
fun Segmented(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val k = Kairos.colors
    val pos by animateFloatAsState(selected.toFloat(), Motion.ui(), label = "seg")
    Box(
        modifier
            .height(48.dp)
            .clip(CircleShape)
            .background(k.acrylic)
            .glassRim(CircleShape, k, sheen = false)
            .padding(4.dp),
    ) {
        Canvas(Modifier.matchParentSize()) {
            val w = size.width / options.size
            drawRoundRect(
                color = if (k.isDark) Color.White.copy(alpha = 0.14f) else Color.White,
                topLeft = Offset(pos * w, 0f),
                size = Size(w, size.height),
                cornerRadius = CornerRadius(size.height / 2),
            )
        }
        Row(Modifier.matchParentSize()) {
            options.forEachIndexed { i, label ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(40.dp)
                        .squishClick(role = androidx.compose.ui.semantics.Role.Tab, pressedScale = 0.95f) { onSelect(i) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (i == selected) k.ink else k.inkSoft,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/** Top bar for pushed screens: back button, title, optional trailing action. */
@Composable
fun KairosTopBar(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            GlassIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onBack)
            Spacer(Modifier.width(12.dp))
        }
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = Kairos.colors.ink,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        actions()
    }
}

/** Watercolor progress: a wet stroke of paint that fills left to right. */
@Composable
fun PaintProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color? = null,
    height: Dp = 10.dp,
) {
    val k = Kairos.colors
    val p by animateFloatAsState(progress.coerceIn(0f, 1f), Motion.ui(), label = "progress")
    val c = color ?: k.lagoon
    Canvas(modifier.fillMaxWidth().height(height)) {
        val r = CornerRadius(size.height / 2)
        drawRoundRect(k.ink.copy(alpha = 0.08f), cornerRadius = r)
        if (p > 0f) {
            drawRoundRect(
                Brush.horizontalGradient(listOf(c.copy(alpha = 0.55f), c), endX = size.width * p),
                size = Size(size.width * p, size.height),
                cornerRadius = r,
            )
            drawRoundRect(
                Color.White.copy(alpha = 0.45f),
                topLeft = Offset(size.height / 2, size.height * 0.2f),
                size = Size((size.width * p - size.height).coerceAtLeast(0f), size.height * 0.22f),
                cornerRadius = r,
            )
        }
    }
}

/** Counter that rolls when its value changes. */
@Composable
fun RollingNumber(value: Int, style: androidx.compose.ui.text.TextStyle, color: Color, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = value,
        transitionSpec = {
            (fadeIn(Motion.enter(380)) + scaleIn(initialScale = 0.8f)) togetherWith (fadeOut(Motion.exit()) + scaleOut(targetScale = 1.1f))
        },
        label = "rolling",
        modifier = modifier,
    ) { Text(it.toString(), style = style, color = color) }
}

@Composable
fun EmptyNote(title: String, body: String, modifier: Modifier = Modifier, mood: PipMood = PipMood.Curious) {
    Column(modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Pip(mood, size = 110.dp)
        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, color = Kairos.colors.ink)
        Spacer(Modifier.height(6.dp))
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = Kairos.colors.inkSoft,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

/** Small rounded label used for parts of speech, themes, file types. */
@Composable
fun Tag(text: String, modifier: Modifier = Modifier, color: Color? = null) {
    val k = Kairos.colors
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = k.ink,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background((color ?: k.washes.sky).copy(alpha = if (k.isDark) 0.35f else 0.45f))
            .border(1.dp, Color.White.copy(alpha = if (k.isDark) 0.08f else 0.6f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

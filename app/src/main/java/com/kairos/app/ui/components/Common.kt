package com.kairos.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kairos.app.AppContainer
import com.kairos.app.container
import com.kairos.app.engine.daily.Mood
import com.kairos.app.ui.art.Kiri
import com.kairos.app.ui.art.drawDrop
import com.kairos.app.ui.glass.ButtonTone
import com.kairos.app.ui.glass.GlassIconButton
import com.kairos.app.ui.glass.GlassWeight
import com.kairos.app.ui.glass.KButton
import com.kairos.app.ui.glass.liquidGlass
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Radii
import com.kairos.app.ui.theme.Space

/** Space reserved under scrolling content for the floating tab bar. */
val TabBarClearance = 120.dp

@Composable
inline fun <reified VM : ViewModel> kairosViewModel(key: String? = null, crossinline create: (AppContainer) -> VM): VM {
    val container = LocalContext.current.container
    return viewModel(key = key, factory = viewModelFactory { initializer { create(container) } })
}

fun screenPadding(bottom: Dp = TabBarClearance) = PaddingValues(start = Space.screen, end = Space.screen, top = Space.sm, bottom = bottom)

@Composable
fun ScreenHeader(
    eyebrow: String,
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(top = Space.lg, bottom = Space.md),
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(Modifier.weight(1f)) {
            Text(eyebrow.uppercase(), style = Kairos.type.caption, color = Kairos.colors.inkSoft)
            Spacer(Modifier.height(Space.xs))
            Text(title, style = Kairos.type.display, color = Kairos.colors.ink)
        }
        trailing?.invoke()
    }
}

@Composable
fun DetailTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    actions: @Composable () -> Unit = {},
) {
    Row(
        modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = Space.lg, vertical = Space.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.md),
    ) {
        GlassIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onBack)
        if (title != null) {
            Text(title, style = Kairos.type.heading, color = Kairos.colors.ink, modifier = Modifier.weight(1f))
        } else {
            Spacer(Modifier.weight(1f))
        }
        actions()
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    Row(modifier.fillMaxWidth().padding(top = Space.xl, bottom = Space.md), verticalAlignment = Alignment.CenterVertically) {
        Text(text, style = Kairos.type.heading, color = Kairos.colors.ink, modifier = Modifier.weight(1f))
        action?.invoke()
    }
}

@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier, color: Color = Kairos.colors.inkSoft) {
    Text(text.uppercase(), style = Kairos.type.caption, color = color, modifier = modifier)
}

/** A droplet with a number that rolls when it changes. Dew is the pond's only currency. */
@Composable
fun DewBadge(dew: Int, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val colors = Kairos.colors
    val m = modifier
        .liquidGlass(Radii.pill, GlassWeight.Frosted, blur = true)
        .then(if (onClick != null) Modifier.clickableNoRipple(onClick) else Modifier)
        .padding(horizontal = 14.dp, vertical = 9.dp)
        .semantics { contentDescription = "$dew dew" }
    Row(m, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        DropGlyph(Modifier.size(16.dp))
        RollingNumber(dew)
    }
}

@Composable
fun DropGlyph(modifier: Modifier = Modifier, color: Color = Kairos.colors.pond) {
    Canvas(modifier) {
        drawDrop(Offset(size.width / 2, size.height * 0.58f), size.width * 0.36f, color)
        drawCircle(Color.White.copy(alpha = 0.8f), size.width * 0.08f, Offset(size.width * 0.4f, size.height * 0.5f))
    }
}

@Composable
fun RollingNumber(value: Int, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = value,
        transitionSpec = {
            val up = targetState > initialState
            (slideInVertically(tween(260)) { if (up) it else -it } + fadeIn(tween(200)))
                .togetherWith(slideOutVertically(tween(260)) { if (up) -it else it } + fadeOut(tween(160)))
        },
        label = "rolling",
        modifier = modifier,
    ) { v ->
        Text("$v", style = Kairos.type.label, color = Kairos.colors.ink)
    }
}

@Composable
fun EmptyState(
    mood: Mood?,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: () -> Unit = {},
) {
    Column(
        modifier.fillMaxWidth().padding(vertical = Space.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Kiri(mood, size = 120.dp)
        Spacer(Modifier.height(Space.lg))
        Text(title, style = Kairos.type.title, color = Kairos.colors.ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(Space.sm))
        Text(body, style = Kairos.type.bodyRegular, color = Kairos.colors.inkSoft, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = Space.xl))
        if (action != null) {
            Spacer(Modifier.height(Space.xl))
            KButton(action, onAction, tone = ButtonTone.Pond)
        }
    }
}

@Composable
fun ProgressRing(progress: Float, modifier: Modifier = Modifier, stroke: Dp = 6.dp, track: Color = Kairos.colors.ink.copy(alpha = 0.08f), color: Color = Kairos.colors.pond) {
    Canvas(modifier) {
        val s = stroke.toPx()
        val d = size.minDimension - s
        val tl = Offset((size.width - d) / 2, (size.height - d) / 2)
        drawArc(track, 0f, 360f, false, tl, androidx.compose.ui.geometry.Size(d, d), style = androidx.compose.ui.graphics.drawscope.Stroke(s))
        drawArc(
            Brush.sweepGradient(listOf(color.copy(alpha = 0.6f), color, color.copy(alpha = 0.6f))),
            -90f, 360f * progress.coerceIn(0f, 1f), false, tl, androidx.compose.ui.geometry.Size(d, d),
            style = androidx.compose.ui.graphics.drawscope.Stroke(s, cap = androidx.compose.ui.graphics.StrokeCap.Round),
        )
    }
}

@Composable
fun HSpace(w: Dp) = Spacer(Modifier.width(w))

@Composable
fun VSpace(h: Dp) = Spacer(Modifier.height(h))

@Composable
fun PillLabel(text: String, modifier: Modifier = Modifier, color: Color = Kairos.colors.ink, fill: Color = Kairos.colors.glassStrong) {
    Box(
        modifier
            .liquidGlass(Radii.pill, GlassWeight.Frosted, tint = fill)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text, style = Kairos.type.caption, color = color)
    }
}

package com.kairos.app.ui.studio

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kairos.app.core.design.GlassButton
import com.kairos.app.core.design.GlassCard
import com.kairos.app.core.design.GlassIconButton
import com.kairos.app.core.design.GlassPill
import com.kairos.app.core.design.KairosMark
import com.kairos.app.core.design.KairosTheme
import com.kairos.app.core.design.KairosType
import com.kairos.app.core.design.shimmer
import com.kairos.app.core.design.staggerIn
import com.kairos.app.domain.share.PosterPalette
import com.kairos.app.domain.share.PosterPalettes
import com.kairos.app.domain.share.PosterRatio
import com.kairos.app.domain.share.PosterStyle
import com.kairos.app.ui.common.Page
import com.kairos.app.ui.common.PageTitle

@Composable
fun StudioScreen(
    state: StudioUi,
    onStyle: (PosterStyle) -> Unit,
    onPalette: (PosterPalette) -> Unit,
    onRatio: (PosterRatio) -> Unit,
    onSource: (StudioSource) -> Unit,
    onWord: (String) -> Unit,
    onQuote: (String) -> Unit,
    onShuffle: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    status: String?,
) {
    val colors = KairosTheme.colors
    val design = state.design
    Page {
        item {
            PageTitle(
                eyebrow = "the studio",
                title = "Make a keeper",
                subtitle = "a word or a thought, worth pinning up",
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggerIn(0)
                    .shadow(
                        elevation = 26.dp,
                        shape = RoundedCornerShape(28.dp),
                        clip = false,
                        ambientColor = colors.ink.copy(alpha = 0.18f),
                        spotColor = colors.ink.copy(alpha = 0.24f),
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(colors.paperDeep)
                    .aspectRatio(design.ratio.w.toFloat() / design.ratio.h),
            ) {
                Crossfade(
                    targetState = state.bitmap,
                    animationSpec = tween(280),
                    label = "poster",
                ) { bitmap ->
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Poster preview",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(listOf(colors.apricot, colors.blossom, colors.periwinkle)),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            KairosMark(modifier = Modifier.size(48.dp), tint = Color.White)
                        }
                    }
                }
                if (state.rendering) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .shimmer()
                            .background(Color.White.copy(alpha = 0.02f)),
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggerIn(1),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StudioSource.entries.forEach { source ->
                    GlassPill(
                        selected = design.source == source,
                        onClick = { onSource(source) },
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        Text(
                            text = source.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (design.source == source) colors.paper else colors.inkSoft,
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                GlassIconButton(
                    icon = Icons.Rounded.Shuffle,
                    onClick = onShuffle,
                    size = 42.dp,
                    contentDescription = "Another",
                )
            }
        }

        item {
            Column(modifier = Modifier.staggerIn(2)) {
                Text("ART DIRECTION", style = KairosType.eyebrow, color = colors.inkFaint)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PosterStyle.entries.forEach { style ->
                        GlassPill(selected = design.style == style, onClick = { onStyle(style) }) {
                            Text(
                                text = style.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (design.style == style) colors.paper else colors.inkSoft,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = design.style.blurb,
                    style = KairosType.handSmall,
                    color = colors.inkFaint,
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggerIn(3),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("PIGMENT", style = KairosType.eyebrow, color = colors.inkFaint)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PosterPalettes.all.forEach { palette ->
                            val selected = design.palette.id == palette.id
                            Box(
                                modifier = Modifier
                                    .size(if (selected) 40.dp else 34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(palette.top), Color(palette.bloomA), Color(palette.bloomB)),
                                        ),
                                    )
                                    .border(
                                        width = if (selected) 2.dp else 1.dp,
                                        color = if (selected) colors.ink else colors.glassStroke,
                                        shape = CircleShape,
                                    )
                                    .clickable { onPalette(palette) },
                            )
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("SHAPE", style = KairosType.eyebrow, color = colors.inkFaint)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PosterRatio.entries.forEach { ratio ->
                            GlassPill(
                                selected = design.ratio == ratio,
                                onClick = { onRatio(ratio) },
                            ) {
                                Text(
                                    text = ratio.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (design.ratio == ratio) colors.paper else colors.inkSoft,
                                )
                            }
                        }
                    }
                }
            }
        }

        if (design.source == StudioSource.Word && state.wordOptions.isNotEmpty()) {
            item {
                Column(modifier = Modifier.staggerIn(4)) {
                    Text("PICK A WORD", style = KairosType.eyebrow, color = colors.inkFaint)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        state.wordOptions.take(14).forEach { word ->
                            GlassPill(
                                selected = state.word.id == word.id,
                                onClick = { onWord(word.id) },
                            ) {
                                Text(
                                    text = word.term,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (state.word.id == word.id) colors.paper else colors.inkSoft,
                                )
                            }
                        }
                    }
                }
            }
        }

        if (design.source == StudioSource.Quote) {
            item {
                Column(modifier = Modifier.staggerIn(4)) {
                    Text("PICK A THOUGHT", style = KairosType.eyebrow, color = colors.inkFaint)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        state.quoteOptions.take(12).forEach { quote ->
                            GlassPill(
                                selected = state.quote.id == quote.id,
                                onClick = { onQuote(quote.id) },
                            ) {
                                Text(
                                    text = quote.author,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (state.quote.id == quote.id) colors.paper else colors.inkSoft,
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.staggerIn(5)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassButton(
                        text = "Save",
                        onClick = onSave,
                        icon = Icons.Rounded.Download,
                        modifier = Modifier.weight(1f),
                    )
                    GlassButton(
                        text = "Share",
                        onClick = onShare,
                        icon = Icons.Rounded.IosShare,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = status ?: "exported at 1080px — sharp enough for a print",
                    style = KairosType.handSmall,
                    color = if (status != null) colors.ink else colors.inkFaint,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

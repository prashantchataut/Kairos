package com.kairos.app.ui.screens.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.domain.identity.CosmeticRarity
import com.kairos.app.domain.identity.KairosBanners
import com.kairos.app.ui.animation.KairosReveal
import com.kairos.app.ui.animation.rememberKairosReducedMotion
import com.kairos.app.ui.components.BannerRenderer
import com.kairos.app.ui.components.kairos.KairosAppBackground
import com.kairos.app.ui.components.kairos.KairosGlassSurface
import com.kairos.app.ui.components.kairos.KairosIconButton
import com.kairos.app.ui.components.kairos.KairosPrimaryButton
import com.kairos.app.ui.icons.KairosIcons
import com.kairos.app.ui.theme.KairosRadius
import com.kairos.app.ui.theme.KairosSpacing
import kotlinx.coroutines.delay

@Composable
fun BannerSelectionScreen(
    onNavigateBack: () -> Unit,
    viewModel: BannerSelectionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var previewBanner by remember { mutableStateOf<BannerOption?>(null) }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            delay(180)
            onNavigateBack()
        }
    }
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbar.showSnackbar(it)
            viewModel.clearError()
        }
    }

    KairosAppBackground {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = KairosSpacing.screen, vertical = KairosSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                KairosIconButton(
                    icon = KairosIcons.ArrowBack,
                    contentDescription = "Back",
                    onClick = onNavigateBack
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Identity canvas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Earned, not purchased",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.size(48.dp))
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val selected = state.banners.firstOrNull { it.id == state.selectedBannerId }
                KairosReveal(visible = true, delayMillis = 40) {
                    SelectedBannerPreview(selected)
                }

                CategorySelector(
                    selected = state.selectedCategory,
                    onSelected = viewModel::selectCategory
                )

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 156.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        start = KairosSpacing.screen,
                        end = KairosSpacing.screen,
                        top = KairosSpacing.sm,
                        bottom = 104.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(KairosSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(KairosSpacing.sm)
                ) {
                    items(state.filteredBanners, key = { it.id }) { banner ->
                        BannerOptionCard(
                            banner = banner,
                            isSelected = state.selectedBannerId == banner.id,
                            onClick = {
                                viewModel.selectBanner(banner.id)
                                previewBanner = banner
                            }
                        )
                    }
                }
            }
        }

        KairosGlassSurface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = KairosSpacing.screen, vertical = KairosSpacing.lg),
            strong = true,
            shape = RoundedCornerShape(KairosRadius.floating),
            contentPadding = PaddingValues(KairosSpacing.xs)
        ) {
            KairosPrimaryButton(
                text = if (state.isSaving) "Applying…" else "Use this banner",
                onClick = viewModel::saveBanner,
                enabled = !state.isSaving && state.hasChanges,
                icon = KairosIcons.Check,
                modifier = Modifier.fillMaxWidth()
            )
        }

        previewBanner?.let { banner ->
            BannerLightbox(
                banner = banner,
                onClose = { previewBanner = null },
                onUse = {
                    viewModel.selectBanner(banner.id)
                    viewModel.saveBanner()
                    previewBanner = null
                }
            )
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 72.dp)
        )
    }
}

/**
 * Full-screen animated lightbox for a banner — the wow moment.
 * Renders the banner with animation, its rarity, description, and the
 * unlock requirement when locked, plus Use / Close actions.
 */
@Composable
private fun BannerLightbox(
    banner: BannerOption,
    onClose: () -> Unit,
    onUse: () -> Unit
) {
    val canonical = remember(banner.id) { KairosBanners.findById(banner.id) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.66f))
            .clickable(onClick = onClose)
            .semantics { contentDescription = "Banner preview for ${banner.name}" },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(220)) + scaleIn(initialScale = 0.94f, animationSpec = tween(260))
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clickable(enabled = false) { },
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 18.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(RoundedCornerShape(22.dp))
                    ) {
                        if (canonical != null) {
                            BannerRenderer(
                                banner = canonical,
                                modifier = Modifier.fillMaxSize(),
                                showAnimation = true,
                                cornerRadius = 22.dp
                            )
                        } else {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(listOf(banner.primaryColor, banner.secondaryColor))
                                    )
                            )
                        }
                        if (banner.isAnimated && !banner.isLocked) {
                            Text(
                                text = "● Motion",
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(12.dp)
                                    .background(Color.Black.copy(alpha = 0.42f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = banner.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = banner.rarity.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                color = banner.rarityColor()
                            )
                        }
                        if (banner.isLocked) {
                            KairosGlassSurface(
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(13.dp),
                                strong = true
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        KairosIcons.Lock,
                                        contentDescription = "Locked",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = if (banner.isLocked) {
                            banner.unlockRequirement ?: "Earn this banner through your practice."
                        } else {
                            banner.description
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    KairosPrimaryButton(
                        text = if (banner.isLocked) "Locked" else "Use this banner",
                        onClick = onUse,
                        enabled = !banner.isLocked,
                        icon = if (banner.isLocked) KairosIcons.Lock else KairosIcons.Check,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(
                        onClick = onClose,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Close", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

private fun BannerOption.rarityColor(): Color = when (rarity) {
    CosmeticRarity.RARE -> Color(0xFF8FA6FF)
    CosmeticRarity.EPIC -> Color(0xFFC792EA)
    CosmeticRarity.LEGENDARY -> Color(0xFFF5C14E)
    else -> Color(0xFF7BD9A5)
}

@Composable
private fun SelectedBannerPreview(banner: BannerOption?) {
    val canonical = banner?.let { KairosBanners.findById(it.id) }
    Column(
        modifier = Modifier.padding(horizontal = KairosSpacing.screen),
        verticalArrangement = Arrangement.spacedBy(KairosSpacing.sm)
    ) {
        Text(
            text = "Selected",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(164.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            if (canonical != null) {
                BannerRenderer(
                    banner = canonical,
                    modifier = Modifier.fillMaxSize(),
                    showAnimation = true,
                    cornerRadius = 16.dp
                )
            } else {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.62f))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(22.dp)
            ) {
                Text(
                    banner?.name ?: "Choose a banner",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    banner?.description ?: "Your profile will carry the story of your practice.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.82f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CategorySelector(
    selected: BannerCategory,
    onSelected: (BannerCategory) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KairosSpacing.screen, vertical = KairosSpacing.lg),
        horizontalArrangement = Arrangement.spacedBy(KairosSpacing.xs)
    ) {
        BannerCategory.entries.forEach { category ->
            val active = category == selected
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .semantics {
                        role = Role.Tab
                        this.selected = active
                        contentDescription = "${category.displayName} banners"
                    },
                shape = RoundedCornerShape(16.dp),
                color = if (active) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                contentColor = if (active) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant,
                border = if (active) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                onClick = { onSelected(category) }
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        category.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun BannerOptionCard(
    banner: BannerOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val reducedMotion = rememberKairosReducedMotion()
    val scale by animateFloatAsState(
        targetValue = if (isSelected && !reducedMotion) 1.025f else 1f,
        label = "banner_selection_scale"
    )
    val canonical = remember(banner.id) { KairosBanners.findById(banner.id) }
    val shape = RoundedCornerShape(
        topStart = if (banner.patternType == KairosBanners.PatternType.AURORA) 36.dp else 22.dp,
        topEnd = 22.dp,
        bottomEnd = if (banner.patternType == KairosBanners.PatternType.GEOMETRIC) 8.dp else 22.dp,
        bottomStart = 22.dp
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.32f)
            .scale(scale)
            .clip(shape)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = shape
            )
            .clickable(enabled = !banner.isLocked, onClick = onClick)
            .semantics {
                role = Role.Button
                selected = isSelected
                contentDescription = buildString {
                    append(banner.name)
                    append(", ")
                    append(banner.rarity.displayName)
                    if (banner.isLocked) append(", locked: ${banner.unlockRequirement}")
                }
            }
    ) {
        if (canonical != null) {
            BannerRenderer(
                banner = canonical,
                modifier = Modifier.fillMaxSize(),
                showAnimation = banner.isAnimated && isSelected,
                cornerRadius = 0.dp
            )
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(banner.primaryColor, banner.secondaryColor)))
            )
        }

        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f))))
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
        ) {
            Text(
                banner.name,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                if (banner.isLocked) banner.unlockRequirement.orEmpty() else banner.rarity.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        AnimatedVisibility(
            visible = banner.isLocked,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            KairosGlassSurface(
                modifier = Modifier.padding(10.dp).size(38.dp),
                shape = RoundedCornerShape(14.dp),
                strong = true,
                elevation = 2.dp
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        KairosIcons.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        if (banner.isAnimated && !banner.isLocked) {
            Text(
                text = "Motion",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

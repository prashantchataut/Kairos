package com.kairos.app.ui.screens.profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kairos.app.data.local.entity.AchievementEntity
import com.kairos.app.domain.identity.KairosBanners
import com.kairos.app.ui.animation.KairosDurations
import com.kairos.app.ui.animation.KairosEasing
import com.kairos.app.ui.animation.KairosReveal
import com.kairos.app.ui.animation.rememberKairosReducedMotion
import com.kairos.app.ui.components.BannerRenderer
import com.kairos.app.ui.components.kairos.KairosGlassSurface
import com.kairos.app.ui.components.kairos.KairosIconButton
import com.kairos.app.ui.components.kairos.KairosMark
import com.kairos.app.ui.components.kairos.KairosReadingSurface
import com.kairos.app.ui.icons.KairosIcons
import com.kairos.app.ui.theme.KairosClay
import com.kairos.app.ui.theme.KairosRadius
import com.kairos.app.ui.theme.KairosSpacing
import com.kairos.app.ui.theme.ThemeMode
import kotlinx.coroutines.delay

/**
 * The Kairos profile — a personal, expressive space.
 *
 * One strong visual moment (the level ring + identity hero with a breathing
 * aura), then evidence of practice as tactile tiles, earned milestones as
 * medallions, and a customization section that previews the user's actual
 * banner and avatar. Fully theme-driven: light is pearl, dark is ink-navy.
 */
@Composable
fun FocusedProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToCosmetics: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val heroScrollOffset by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex == 0) listState.firstVisibleItemScrollOffset else 0
        }
    }
    var reveal by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(40)
        reveal = true
    }

    when {
        state.isLoading -> ProfileLoading(onNavigateBack)
        state.error != null -> ProfileError(
            message = state.error ?: "Your profile could not be loaded.",
            onBack = onNavigateBack,
            onRetry = viewModel::retry
        )
        else -> LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            item(key = "profile-hero") {
                ProfileHero(
                    state = state,
                    visible = reveal,
                    scrollOffset = heroScrollOffset,
                    onBack = onNavigateBack,
                    onSettings = onNavigateToSettings,
                    onEdit = onNavigateToEditProfile,
                    onCustomize = onNavigateToCosmetics
                )
            }
            item(key = "profile-journey") {
                KairosReveal(visible = reveal, delayMillis = 90) {
                    JourneyCard(
                        state = state,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 820.dp)
                            .padding(horizontal = KairosSpacing.screen, vertical = 20.dp)
                    )
                }
            }
            item(key = "profile-rhythm") {
                KairosReveal(visible = reveal, delayMillis = 140) {
                    ProfileRhythmSection(
                        state = state,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 820.dp)
                            .padding(horizontal = KairosSpacing.screen)
                    )
                }
            }
            item(key = "profile-week") {
                KairosReveal(visible = reveal, delayMillis = 190) {
                    WeeklyReflectionSection(
                        state = state,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 820.dp)
                            .padding(horizontal = KairosSpacing.screen, vertical = 20.dp)
                    )
                }
            }
            item(key = "profile-milestones") {
                KairosReveal(visible = reveal, delayMillis = 230) {
                    MilestonesStrip(
                        state = state,
                        onClick = onNavigateToAchievements,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 820.dp)
                            .padding(horizontal = KairosSpacing.screen)
                    )
                }
            }
            item(key = "profile-customize") {
                KairosReveal(visible = reveal, delayMillis = 270) {
                    CustomizeSection(
                        state = state,
                        onCustomize = onNavigateToCosmetics,
                        onEdit = onNavigateToEditProfile,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 820.dp)
                            .padding(horizontal = KairosSpacing.screen, vertical = 20.dp)
                    )
                }
            }
        }
    }
}

// =============================================================================
// HERO — aura, avatar with level badge, banner, identity
// =============================================================================

@Composable
private fun ProfileHero(
    state: ProfileUiState,
    visible: Boolean,
    scrollOffset: Int,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onEdit: () -> Unit,
    onCustomize: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(scheme.background)
            .statusBarsPadding()
            .padding(horizontal = KairosSpacing.screen, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 820.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            KairosIconButton(
                icon = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                onClick = onBack
            )
            KairosMark(
                modifier = Modifier.size(38.dp),
                tint = scheme.onSurface,
                accent = scheme.primary,
                revealed = visible
            )
            KairosIconButton(
                icon = Icons.Outlined.Settings,
                contentDescription = "Settings",
                onClick = onSettings
            )
        }

        Spacer(Modifier.height(10.dp))

        // Identity core with a breathing aura behind it.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            ProfileAura(Modifier.size(230.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileAvatar(
                    photoUrl = state.authPhotoUrl,
                    name = resolvedName(state),
                    level = state.level,
                    modifier = Modifier.size(96.dp)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = resolvedName(state),
                        style = MaterialTheme.typography.displaySmall,
                        color = scheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        text = profileHandle(state),
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileTag(state.title, accent = scheme.primary)
                    state.userContext.recentThemes
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .take(3)
                        .forEach { ProfileTag(it, accent = null) }
                }

                Text(
                    text = state.bio.ifBlank {
                        "Learning a little more clearly, one useful word and reflection at a time."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.12f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 460.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onEdit)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = scheme.primary
                    )
                    Text(
                        text = "Edit profile",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.primary
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        // Banner card with parallax and a floating edit affordance.
        val activeBanner = remember(state.bannerId) {
            KairosBanners.findById(state.bannerId) ?: KairosBanners.getDefaultBanner()
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp)
                .height(150.dp)
                .clip(RoundedCornerShape(KairosRadius.feature))
                .border(1.dp, scheme.outlineVariant, RoundedCornerShape(KairosRadius.feature))
        ) {
            BannerRenderer(
                banner = activeBanner,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationY = scrollOffset * 0.12f
                        scaleX = 1.03f
                        scaleY = 1.03f
                    },
                showAnimation = true,
                cornerRadius = KairosRadius.feature
            )
            KairosGlassSurface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .height(36.dp),
                shape = RoundedCornerShape(14.dp),
                strong = true,
                onClick = onCustomize
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Change banner",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = scheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * A slow-breathing warm aura behind the identity (reduced-motion aware).
 */
@Composable
private fun ProfileAura(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val reducedMotion = rememberKairosReducedMotion()
    val transition = rememberInfiniteTransition(label = "profile-aura")
    val pulse by transition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(5200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "profile-aura-alpha"
    )
    val alpha = if (reducedMotion) 0.08f else pulse

    Box(
        modifier = modifier.drawBehind {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(scheme.primary.copy(alpha = alpha), Color.Transparent),
                    center = center,
                    radius = size.minDimension * 0.5f
                ),
                radius = size.minDimension * 0.5f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(scheme.tertiary.copy(alpha = alpha * 0.6f), Color.Transparent),
                    center = center.copy(x = size.width * 0.42f, y = size.height * 0.58f),
                    radius = size.minDimension * 0.42f
                ),
                radius = size.minDimension * 0.42f,
                center = center.copy(x = size.width * 0.42f, y = size.height * 0.58f)
            )
        }
    )
}

@Composable
private fun ProfileAvatar(
    photoUrl: String?,
    name: String,
    level: Int,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Gradient ring + glow.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(14.dp, CircleShape, ambientColor = scheme.primary.copy(alpha = 0.45f), spotColor = scheme.primary.copy(alpha = 0.35f))
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(scheme.primary, scheme.tertiary)
                    )
                )
                .padding(3.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(scheme.surfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                if (!photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(photoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "$name profile photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = initialsFor(name),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = scheme.primary
                    )
                }
            }
        }
        // Level badge.
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 4.dp),
            shape = RoundedCornerShape(10.dp),
            color = scheme.primary,
            contentColor = scheme.onPrimary,
            shadowElevation = 3.dp
        ) {
            Text(
                text = "Lv $level",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

// =============================================================================
// JOURNEY — the level ring, the strong visual moment
// =============================================================================

@Composable
private fun JourneyCard(
    state: ProfileUiState,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val level = state.level.coerceAtLeast(1)
    val progress = if (state.totalPoints <= 0) 0f
    else ((state.totalPoints % 1000) / 1000f).coerceIn(0f, 1f)
    val toNext = (level * 1000 - state.totalPoints).coerceAtLeast(0)

    KairosGlassSurface(
        modifier = modifier,
        shape = RoundedCornerShape(KairosRadius.card),
        strong = true,
        contentPadding = PaddingValues(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            LevelRing(level = level, progress = progress)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Your journey",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() }
                )
                JourneyFact(
                    icon = KairosIcons.LocalFireDepartment,
                    text = "${state.currentStreak} day rhythm",
                    accent = KairosClay
                )
                JourneyFact(
                    icon = KairosIcons.History,
                    text = "${state.longestStreak} day best",
                    accent = scheme.tertiary
                )
                JourneyFact(
                    icon = KairosIcons.EmojiEvents,
                    text = "${state.achievementsUnlocked} milestones earned",
                    accent = scheme.primary
                )
                Text(
                    text = if (toNext > 0) "$toNext points to Level ${level + 1}" else "Top of Level $level",
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Animated level ring with a primary→tertiary sweep gradient.
 */
@Composable
private fun LevelRing(
    level: Int,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val reducedMotion = rememberKairosReducedMotion()
    val animated by animateFloatAsState(
        targetValue = progress,
        animationSpec = if (reducedMotion) tween(0) else tween(KairosDurations.State * 2, easing = KairosEasing.EaseOutExpo),
        label = "level-ring-progress"
    )
    val stroke = 10f

    Box(
        modifier = modifier.size(112.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawArc(
                        color = scheme.surfaceContainerHighest,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = androidx.compose.ui.geometry.Offset(stroke / 2f, stroke / 2f),
                        size = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke),
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(scheme.primary, scheme.tertiary, scheme.primary),
                            center = center
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * animated,
                        useCenter = false,
                        topLeft = androidx.compose.ui.geometry.Offset(stroke / 2f, stroke / 2f),
                        size = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke),
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                text = "$level",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface
            )
            Text(
                text = "LEVEL",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun JourneyFact(
    icon: ImageVector,
    text: String,
    accent: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(17.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// =============================================================================
// RHYTHM — tactile evidence tiles
// =============================================================================

@Composable
private fun ProfileRhythmSection(
    state: ProfileUiState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(15.dp)) {
        SectionHeading(title = "Quiet evidence of progress")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RhythmTile(
                icon = KairosIcons.MenuBook,
                value = state.wordsLearned.toString(),
                label = "words retained",
                accent = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            RhythmTile(
                icon = KairosIcons.Outlined.CheckCircle,
                value = state.daysOnKairos.toString(),
                label = "days with Kairos",
                accent = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            RhythmTile(
                icon = KairosIcons.LocalFireDepartment,
                value = state.currentStreak.toString(),
                label = "day rhythm",
                accent = KairosClay,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RhythmTile(
    icon: ImageVector,
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .heightIn(min = 138.dp)
            .shadow(6.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x2E1B2A4A), spotColor = Color(0x261B2A4A)),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(accent.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}

// =============================================================================
// WEEK — the reflective insight
// =============================================================================

@Composable
private fun WeeklyReflectionSection(
    state: ProfileUiState,
    modifier: Modifier = Modifier
) {
    val pattern = state.weeklyPattern
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(15.dp)) {
        SectionHeading(title = "What your practice is showing")
        KairosReadingSurface(
            modifier = Modifier.fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = pattern,
                transitionSpec = {
                    (fadeIn(tween(KairosDurations.State)) togetherWith fadeOut(tween(KairosDurations.Micro)))
                        .using(SizeTransform(clip = false))
                },
                label = "weekly-pattern"
            ) { currentPattern ->
                if (currentPattern != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = currentPattern.keyPattern,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = currentPattern.summary,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = currentPattern.suggestion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "A pattern needs a little history.",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "After three reflections in a week, Kairos can summarize recurring themes without turning your profile into a scorecard.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// MILESTONES — earned medallions
// =============================================================================

@Composable
private fun MilestonesStrip(
    state: ProfileUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeading(title = "Milestones")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onClick)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "View all",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (state.unlockedAchievements.isEmpty()) {
            KairosReadingSurface(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Your first milestone is closer than you think — saved words and written reflections both count toward it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(18.dp)
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(end = 8.dp)
            ) {
                items(state.unlockedAchievements.take(10), key = { it.id }) { achievement ->
                    MilestoneMedallion(achievement)
                }
            }
        }
    }
}

@Composable
private fun MilestoneMedallion(achievement: AchievementEntity) {
    val scheme = MaterialTheme.colorScheme
    val accent = when {
        "streak" in achievement.iconId || "consistency" in achievement.category -> KairosClay
        "master" in achievement.iconId -> scheme.primary
        else -> scheme.tertiary
    }
    Column(
        modifier = Modifier.widthIn(max = 76.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = accent.copy(alpha = 0.35f), spotColor = accent.copy(alpha = 0.25f))
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(accent.copy(alpha = 0.85f), accent.copy(alpha = 0.55f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = achievementIcon(achievement),
                contentDescription = achievement.name,
                tint = scheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = achievement.name,
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

private fun achievementIcon(achievement: AchievementEntity): ImageVector {
    val source = "${achievement.iconId} ${achievement.category}".lowercase()
    return when {
        "streak" in source || "consistency" in source -> KairosIcons.LocalFireDepartment
        "word" in source || "wisdom" in source || "learn" in source -> KairosIcons.MenuBook
        "journal" in source || "reflection" in source -> KairosIcons.Edit
        "future" in source || "temporal" in source || "letter" in source -> KairosIcons.Mail
        "presence" in source || "calm" in source -> KairosIcons.SelfImprovement
        "master" in source -> KairosIcons.WorkspacePremium
        else -> KairosIcons.EmojiEvents
    }
}

// =============================================================================
// CUSTOMIZE — live previews of the user's actual banner + avatar
// =============================================================================

@Composable
private fun CustomizeSection(
    state: ProfileUiState,
    onCustomize: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionHeading(title = "Customize")
        val activeBanner = remember(state.bannerId) {
            KairosBanners.findById(state.bannerId) ?: KairosBanners.getDefaultBanner()
        }
        CustomizeRow(
            onClick = onCustomize,
            leading = {
                BannerRenderer(
                    banner = activeBanner,
                    modifier = Modifier
                        .size(64.dp, 44.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    showAnimation = false,
                    cornerRadius = 12.dp
                )
            },
            title = "Banner",
            subtitle = activeBanner.name
        )
        CustomizeRow(
            onClick = onEdit,
            leading = {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        )
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initialsFor(resolvedName(state)),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            title = "Avatar & details",
            subtitle = resolvedName(state)
        )
    }
}

@Composable
private fun CustomizeRow(
    onClick: () -> Unit,
    leading: @Composable () -> Unit,
    title: String,
    subtitle: String
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(22.dp), ambientColor = Color(0x1F1B2A4A), spotColor = Color(0x1A1B2A4A)),
        shape = RoundedCornerShape(22.dp),
        color = scheme.surface
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            leading()
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// =============================================================================
// SHARED PIECES
// =============================================================================

@Composable
private fun SectionHeading(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.semantics { heading() }
    )
}

@Composable
private fun ProfileTag(label: String, accent: Color? = null) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (accent != null) accent.copy(alpha = 0.12f) else scheme.surfaceContainer,
        border = if (accent != null) {
            androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.35f))
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, scheme.outlineVariant)
        }
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (accent != null) FontWeight.SemiBold else FontWeight.Medium,
            color = if (accent != null) accent else scheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun ProfileLoading(onBack: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            KairosIconButton(
                icon = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                onClick = onBack
            )
        }
        KairosMark(
            modifier = Modifier.align(Alignment.Center).size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface,
            accent = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ProfileError(
    message: String,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(KairosSpacing.screen),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Profile unavailable", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            KairosIconButton(Icons.AutoMirrored.Outlined.ArrowBack, "Back", onBack)
            Surface(onClick = onRetry, shape = RoundedCornerShape(KairosRadius.control), color = MaterialTheme.colorScheme.primary) {
                Text("Try again", modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private fun resolvedName(state: ProfileUiState): String =
    state.authDisplayName?.takeIf { it.isNotBlank() && it != "Local profile" }
        ?: state.displayName.takeIf { it.isNotBlank() && it != "Growth Seeker" }
        ?: "Kairos learner"

private fun profileHandle(state: ProfileUiState): String =
    state.authEmail?.takeIf { it.isNotBlank() }
        ?: "@${resolvedName(state).lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_').ifBlank { "local" }}"

private fun initialsFor(name: String): String = name
    .split(Regex("\\s+"))
    .filter { it.isNotBlank() }
    .take(2)
    .joinToString("") { it.first().uppercase() }
    .ifBlank { "K" }

@Preview(name = "Focused profile", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun FocusedProfilePreview() {
    KairosTheme(themeMode = ThemeMode.DARK) {
        ProfileHero(
            state = ProfileUiState(
                displayName = "Maya Chen",
                bio = "Learning to express difficult ideas with more clarity and care.",
                title = "Thoughtful learner",
                wordsLearned = 128,
                journalEntries = 34,
                currentStreak = 12,
                longestStreak = 19,
                daysOnKairos = 86,
                level = 7,
                isLoading = false
            ),
            visible = true,
            scrollOffset = 0,
            onBack = {},
            onSettings = {},
            onEdit = {},
            onCustomize = {}
        )
    }
}

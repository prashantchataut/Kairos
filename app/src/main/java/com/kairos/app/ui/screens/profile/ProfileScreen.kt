package com.kairos.app.ui.screens.profile
import com.kairos.app.ui.icons.KairosIcons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import com.kairos.app.domain.gamification.AchievementRarity
import com.kairos.app.ui.theme.UiAchievements
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.ui.theme.isDarkTheme
import com.kairos.app.R
import com.kairos.app.data.ai.WeeklyPatternResult
import com.kairos.app.data.local.entity.AchievementEntity
import com.kairos.app.ui.components.AmbientBackground
import com.kairos.app.ui.components.FloatingParticles
import com.kairos.app.ui.components.GrowthGarden
import com.kairos.app.ui.components.MoodBreathingHalo
import com.kairos.app.ui.components.PlayerSkillsCard
import com.kairos.app.ui.components.getCurrentTimeOfDay
import com.kairos.app.ui.theme.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay

/**
 * Profile Screen - Identity & Trophy Room - Premium Phase 2 Redesign
 *
 * A completely redesigned gamified profile experience featuring:
 *
 * Design Philosophy:
 * - Extreme minimalism, flat design - NO shadows, gradients, or hi-fi elements
 * - Deep teal dark (#0D2826), clean off-white light (#F0F4F3)
 * - Vibrant neon green accent (#36F97F) for interactive elements
 * - Poppins typography throughout
 * - 8dp grid spacing system
 *
 * Features:
 * - Avatar with animated neon green progress ring
 * - DEV and BETA PIONEER badges
 * - Key metrics (Level, Streak, Words) with clean cards
 * - Story of Growth narrative section
 * - Trophy Room achievement showcase
 * - Weekly AI Insights card
 */

// ============================================================================
// DESIGN SYSTEM COLORS - Identity Room Theme (Using Theme Colors)
// ============================================================================

private object IdentityRoomColors {
    // Dark Mode - Using theme colors
    val BackgroundDark = KairosBackgroundDark
    val CardBackgroundDark = KairosSurfaceVariantDark
    val CardBackgroundElevatedDark = KairosSurfaceContainerDark
    val AccentGreen = KairosAccentGreen
    val AccentGreenDim = KairosAccentGreenDark
    val TextPrimaryDark = KairosTextPrimaryDark
    val TextSecondaryDark = KairosTextSecondaryDark
    val TextTertiaryDark = KairosTextTertiaryDark
    val BorderDark = KairosOutlineDark

    // Light Mode - Using theme colors
    val BackgroundLight = KairosBackgroundLight
    val CardBackgroundLight = KairosSurfaceLight
    val CardBackgroundElevatedLight = KairosSurfaceContainerLight
    val AccentGreenLight = KairosAccentGreen
    val TextPrimaryLight = KairosTextPrimaryLight
    val TextSecondaryLight = KairosTextSecondaryLight
    val TextTertiaryLight = KairosTextTertiaryLight
    val BorderLight = KairosOutlineLight

    // Badge Colors - Using theme colors
    val DevBadgeBackground = KairosSurfaceVariantDark
    val DevBadgeText = KairosAccentGreen
    val BetaBadgeBackground = KairosPremiumVioletContainer
    val BetaBadgeText = KairosPremiumViolet

    // Achievement Rarity - Using theme colors
    val RarityCommon = com.kairos.app.ui.theme.RarityCommon
    val RarityUncommon = com.kairos.app.ui.theme.RarityUncommon
    val RarityRare = com.kairos.app.ui.theme.RarityRare
    val RarityEpic = com.kairos.app.ui.theme.RarityEpic
    val RarityLegendary = com.kairos.app.ui.theme.RarityLegendary
}

@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val screenState = rememberProfileScreenState(uiState)
    val isDarkMode = isDarkTheme()

    // Premium theme colors using MaterialTheme
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceElevated = MaterialTheme.colorScheme.surfaceVariant
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val textTertiary = if (isDarkMode) KairosTextTertiaryDark else KairosTextTertiaryLight
    val accentColor = MaterialTheme.colorScheme.primary
    val dividerColor = MaterialTheme.colorScheme.outlineVariant

    // Soul Layer Context for Identity Card
    val context = uiState.userContext

    // Entry animation
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(50)
        isVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Minimal horizontal line at the top of content for structure
        HorizontalDivider(
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 72.dp),
            color = textPrimary.copy(alpha = 0.05f)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header with title and actions
            item {
                com.kairos.app.ui.components.PremiumHeader(
                    title = "Identity",
                    subtitle = "Soul Layer",
                    onBackClick = null, // Profile is a main tab or top-level here
                    actions = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = accentColor
                            )
                        }
                    }
                )
            }

            item {
                SoulIdentityCard(
                    context = context,
                    level = uiState.level,
                    isDarkMode = isDarkMode,
                    onEditClick = onNavigateToEditProfile,
                    authDisplayName = uiState.authDisplayName,
                    authEmail = uiState.authEmail,
                    authPhotoUrl = uiState.authPhotoUrl
                )
            }

            // Growth Journey Card - The heart of the profile
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(400, delayMillis = 200)) + slideInVertically(
                        initialOffsetY = { it / 3 },
                        animationSpec = tween(400, delayMillis = 200, easing = EaseOutCubic)
                    )
                ) {
                    GrowthJourneyCard(
                        currentStreak = uiState.currentStreak,
                        dominantTheme = uiState.weeklyPattern?.keyPattern,
                        todayLearning = uiState.weeklyPattern?.suggestion,
                        totalPoints = uiState.totalPoints,
                        surfaceColor = surfaceColor,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        textTertiary = textTertiary,
                        accentColor = accentColor,
                        isDarkMode = isDarkMode
                    )
                }
            }

            // Consistency Score Ring
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(400, delayMillis = 225)) + slideInVertically(
                        initialOffsetY = { it / 3 },
                        animationSpec = tween(400, delayMillis = 225, easing = EaseOutCubic)
                    )
                ) {
                    ConsistencyScoreCard(
                        currentStreak = uiState.currentStreak,
                        longestStreak = uiState.longestStreak,
                        journalEntries = uiState.journalEntries,
                        daysOnKairos = uiState.daysOnKairos,
                        surfaceColor = surfaceColor,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        textTertiary = textTertiary,
                        accentColor = accentColor
                    )
                }
            }

            // Weekly AI Pattern Card
            item {
                AnimatedVisibility(
                    visible = isVisible && (uiState.weeklyPattern != null || uiState.isLoadingWeeklyPattern),
                    enter = fadeIn(tween(400, delayMillis = 250)) + slideInVertically(
                        initialOffsetY = { it / 3 },
                        animationSpec = tween(400, delayMillis = 250, easing = EaseOutCubic)
                    )
                ) {
                    PremiumWeeklyPatternSection(
                        weeklyPattern = uiState.weeklyPattern,
                        isLoading = uiState.isLoadingWeeklyPattern,
                        hasEnoughData = uiState.hasEnoughDataForPattern,
                        surfaceColor = surfaceColor,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        textTertiary = textTertiary,
                        accentColor = accentColor
                    )
                }
            }

            // Achievement Shelf Section
            item {
                AchievementShelf(
                    unlockedCount = uiState.unlockedAchievements.size,
                    totalCount = uiState.unlockedAchievements.size + uiState.lockedAchievements.size,
                    achievements = uiState.unlockedAchievements.take(5),
                    onViewAllClick = onNavigateToAchievements,
                    isDarkMode = isDarkMode
                )
            }


            // Recent Unlocks Section
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(400, delayMillis = 400))
                ) {
                    PremiumRecentUnlocksSection(
                        unlockedAchievements = uiState.unlockedAchievements,
                        lockedAchievements = uiState.lockedAchievements.take(4),
                        surfaceColor = surfaceColor,
                        surfaceElevated = surfaceElevated,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        textTertiary = textTertiary
                    )
                }
            }

            // Sign Out
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(400, delayMillis = 450))
                ) {
                    SignOutSection(
                        onSignOut = { viewModel.signOut() },
                        accentColor = accentColor,
                        textSecondary = textSecondary
                    )
                }
            }

            // Growth Quote
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(400, delayMillis = 500))
                ) {
                    PremiumGrowthQuoteCard(
                        textSecondary = textSecondary,
                        accentColor = accentColor
                    )
                }
            }
        }
    }
}

// ============================================================================
// HEADER COMPONENT
// ============================================================================

@Composable
private fun PremiumProfileHeader(
    onSettingsClick: () -> Unit,
    onEditClick: () -> Unit,
    textPrimary: Color,
    textSecondary: Color,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Profile",
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            color = textPrimary
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onEditClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = KairosIcons.Outlined.Edit,
                    contentDescription = "Edit Profile",
                    tint = textSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = KairosIcons.Outlined.Settings,
                    contentDescription = stringResource(R.string.settings),
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// ============================================================================
// HERO SECTION - Avatar, Name, Badges
// ============================================================================

@Composable
internal fun PremiumHeroSection(
    displayName: String,
    title: String,
    bio: String,
    level: Int,
    levelProgress: Float,
    isDev: Boolean,
    isBetaPioneer: Boolean,
    isDarkMode: Boolean,
    onEditClick: () -> Unit,
    textPrimary: Color,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar with Progress Ring and Magical Breathing Halo
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(160.dp) // Slightly larger to accommodate halo
        ) {
            // Magical breathing halo effect around the avatar
            MoodBreathingHalo(
                modifier = Modifier.size(156.dp),
                mood = com.kairos.app.ui.components.AmbientMood.Calm,
                size = 156.dp
            ) {
                // Empty - just provides halo effect
            }

            // Avatar container
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(140.dp)
            ) {
                // Animated Progress Ring
                NeonProgressRing(
                    progress = levelProgress,
                    modifier = Modifier.fillMaxSize(),
                    accentColor = if (isDarkMode) IdentityRoomColors.AccentGreen
                                  else IdentityRoomColors.AccentGreenLight
                )

                // Avatar Circle
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDarkMode) IdentityRoomColors.CardBackgroundDark
                            else IdentityRoomColors.CardBackgroundLight
                        )
                        .border(
                            width = 3.dp,
                            color = if (isDarkMode) IdentityRoomColors.BorderDark
                                    else IdentityRoomColors.BorderLight,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = KairosIcons.Person,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = if (isDarkMode) IdentityRoomColors.TextSecondaryDark
                               else IdentityRoomColors.TextSecondaryLight
                    )
                }

                // Level Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-8).dp, y = (-8).dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDarkMode) IdentityRoomColors.AccentGreen
                            else IdentityRoomColors.AccentGreenLight
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = level.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Display Name
        Text(
            text = displayName,
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = textPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Equipped Title
        Text(
            text = title,
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = if (isDarkMode) IdentityRoomColors.TextSecondaryDark
                    else IdentityRoomColors.TextSecondaryLight
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Badges Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isDev) {
                PremiumDevBadge(accentColor = accentColor)
            }
            if (isBetaPioneer) {
                PremiumBetaPioneerBadge()
            }
        }

        // Bio Section
        if (bio.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            BioSection(
                bio = bio,
                isDarkMode = isDarkMode,
                onEditClick = onEditClick
            )
        }
    }
}

@Composable
private fun BioSection(
    bio: String,
    isDarkMode: Boolean,
    onEditClick: () -> Unit
) {
    Surface(
        color = if (isDarkMode) IdentityRoomColors.CardBackgroundDark.copy(alpha = 0.5f)
                else IdentityRoomColors.CardBackgroundLight.copy(alpha = 0.8f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = KairosIcons.Outlined.FormatQuote,
                contentDescription = null,
                tint = if (isDarkMode) IdentityRoomColors.AccentGreen
                       else IdentityRoomColors.AccentGreenLight,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = bio,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDarkMode) IdentityRoomColors.TextSecondaryDark
                        else IdentityRoomColors.TextSecondaryLight,
                modifier = Modifier.weight(1f),
                lineHeight = 22.sp
            )
            Icon(
                imageVector = KairosIcons.Outlined.Edit,
                contentDescription = "Edit bio",
                tint = if (isDarkMode) IdentityRoomColors.TextTertiaryDark
                       else IdentityRoomColors.TextTertiaryLight,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun NeonProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    accentColor: Color
) {
    val animatedProgressState = animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "progress_ring"
    )

    // Subtle glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Canvas(modifier = modifier) {
        val strokeWidth = 6.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)

        // Background ring
        drawCircle(
            color = accentColor.copy(alpha = 0.15f),
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth)
        )

        // Glow effect (subtle)
        drawCircle(
            color = accentColor.copy(alpha = glowAlpha * 0.15f),
            radius = radius + 4.dp.toPx(),
            center = center,
            style = Stroke(width = 8.dp.toPx())
        )

        // Progress arc
        drawArc(
            color = accentColor,
            startAngle = -90f,
            sweepAngle = animatedProgressState.value * 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
            size = Size(size.width - strokeWidth, size.height - strokeWidth)
        )
    }
}

@Composable
private fun PremiumDevBadge(accentColor: Color) {
    Surface(
        color = accentColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = KairosIcons.Code,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "DEV",
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = accentColor,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun PremiumBetaPioneerBadge() {
    val badgeColor = Color(0xFFB57EDC) // Premium violet

    Surface(
        color = badgeColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = KairosIcons.Rocket,
                contentDescription = null,
                tint = badgeColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "BETA PIONEER",
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = badgeColor,
                letterSpacing = 0.5.sp
            )
        }
    }
}

// ============================================================================
// KEY METRICS ROW
// ============================================================================

@Composable
internal fun PremiumKeyMetricsRow(
    level: Int,
    streak: Int,
    wordsLearned: Int,
    surfaceColor: Color,
    textPrimary: Color,
    textTertiary: Color,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PremiumMetricCard(
            label = "Level",
            value = level.toString(),
            icon = KairosIcons.TrendingUp,
            iconColor = accentColor,
            surfaceColor = surfaceColor,
            textPrimary = textPrimary,
            textTertiary = textTertiary,
            modifier = Modifier.weight(1f)
        )
        PremiumMetricCard(
            label = "Consistency",
            value = streak.toString(),
            icon = KairosIcons.LocalFireDepartment,
            iconColor = Color(0xFFE65C2C), // Fire orange
            surfaceColor = surfaceColor,
            textPrimary = textPrimary,
            textTertiary = textTertiary,
            modifier = Modifier.weight(1f)
        )
        PremiumMetricCard(
            label = "Words",
            value = formatCompactNumber(wordsLearned),
            icon = KairosIcons.School,
            iconColor = Color(0xFFFFD166), // Energetic amber
            surfaceColor = surfaceColor,
            textPrimary = textPrimary,
            textTertiary = textTertiary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PremiumMetricCard(
    label: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    surfaceColor: Color,
    textPrimary: Color,
    textTertiary: Color,
    modifier: Modifier = Modifier
) {
    var isAnimated by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(200)
        isAnimated = true
    }

    val scaleState = animateFloatAsState(
        targetValue = if (isAnimated) 1f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "metric_scale"
    )

    Surface(
        modifier = modifier
            .graphicsLayer {
                val s = scaleState.value
                scaleX = s
                scaleY = s
            }
            .clip(RoundedCornerShape(20.dp)),
        color = surfaceColor,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 0.dp // Flat design
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = textPrimary
            )
            Text(
                text = label,
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = textTertiary
            )
        }
    }
}

// ============================================================================
// STORY OF GROWTH SECTION
// ============================================================================

@Composable
private fun PremiumStoryOfGrowthSection(
    currentStreak: Int,
    longestStreak: Int,
    totalPoints: Int,
    journalEntries: Int,
    wordsLearned: Int,
    daysOnKairos: Int,
    surfaceColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    textTertiary: Color,
    accentColor: Color
) {
    // Determine the highlight achievement
    val highlight = remember(currentStreak, longestStreak, journalEntries, wordsLearned, totalPoints) {
        when {
            currentStreak >= longestStreak && currentStreak >= 7 -> GrowthHighlight(
                title = "Consistency Master",
                description = "You're on your longest streak ever!",
                icon = KairosIcons.LocalFireDepartment,
                color = Color(0xFFE65C2C)
            )
            journalEntries >= 30 -> GrowthHighlight(
                title = "Dedicated Writer",
                description = "$journalEntries journal entries and counting",
                icon = KairosIcons.AutoStories,
                color = MoodCalm // Serene blue from design system
            )
            wordsLearned >= 100 -> GrowthHighlight(
                title = "Word Collector",
                description = "Over $wordsLearned words mastered",
                icon = KairosIcons.School,
                color = Color(0xFFFFD166)
            )
            totalPoints >= 500 -> GrowthHighlight(
                title = "Rising Star",
                description = "Earned ${formatCompactNumber(totalPoints)} XP",
                icon = KairosIcons.Stars,
                color = Color(0xFFD4AF37)
            )
            currentStreak >= 3 -> GrowthHighlight(
                title = "Building Momentum",
                description = "$currentStreak day streak going strong",
                icon = KairosIcons.LocalFireDepartment,
                color = Color(0xFFE65C2C)
            )
            else -> GrowthHighlight(
                title = "New Journey",
                description = "Every expert was once a beginner",
                icon = KairosIcons.EmojiNature,
                color = Color(0xFF7EC8A3)
            )
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp)),
        color = surfaceColor,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Section Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = KairosIcons.AutoGraph,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = "Story of Growth",
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = textPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Highlight Card
            Surface(
                color = highlight.color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(highlight.color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = highlight.icon,
                            contentDescription = null,
                            tint = highlight.color,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = highlight.title,
                            fontFamily = PoppinsFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = highlight.color
                        )
                        Text(
                            text = highlight.description,
                            fontFamily = PoppinsFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PremiumMiniStat(
                    value = daysOnKairos.toString(),
                    label = "Days Active",
                    textPrimary = textPrimary,
                    textTertiary = textTertiary
                )
                PremiumMiniStat(
                    value = journalEntries.toString(),
                    label = "Entries",
                    textPrimary = textPrimary,
                    textTertiary = textTertiary
                )
                PremiumMiniStat(
                    value = longestStreak.toString(),
                    label = "Best Consistency",
                    textPrimary = textPrimary,
                    textTertiary = textTertiary
                )
            }
        }
    }
}

private data class GrowthHighlight(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
private fun PremiumMiniStat(
    value: String,
    label: String,
    textPrimary: Color,
    textTertiary: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = textPrimary
        )
        Text(
            text = label,
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            color = textTertiary
        )
    }
}

// ============================================================================
// WEEKLY PATTERN SECTION
// ============================================================================

@Composable
private fun PremiumWeeklyPatternSection(
    weeklyPattern: WeeklyPatternResult?,
    isLoading: Boolean,
    hasEnoughData: Boolean,
    surfaceColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    textTertiary: Color,
    accentColor: Color
) {
    val violetColor = Color(0xFFB3401F) // Accent vermilion

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp)),
        color = surfaceColor,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(violetColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = KairosIcons.Psychology,
                            contentDescription = null,
                            tint = violetColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Weekly Insights",
                            fontFamily = PoppinsFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = textPrimary
                        )
                        Text(
                            text = "AI-powered analysis",
                            fontFamily = PoppinsFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            color = textTertiary
                        )
                    }
                }

                if (weeklyPattern?.isAiGenerated == true) {
                    Surface(
                        color = violetColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        tonalElevation = 0.dp
                    ) {
                        Text(
                            text = "AI",
                            fontFamily = PoppinsFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = violetColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = violetColor,
                            strokeWidth = 2.dp
                        )
                    }
                }
                !hasEnoughData -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Write 3+ entries this week",
                            fontFamily = PoppinsFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = textSecondary
                        )
                        Text(
                            text = "to unlock AI insights",
                            fontFamily = PoppinsFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            color = textTertiary
                        )
                    }
                }
                weeklyPattern != null -> {
                    Text(
                        text = weeklyPattern.summary,
                        fontFamily = PoppinsFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = textSecondary,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = accentColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = KairosIcons.Lightbulb,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = weeklyPattern.suggestion,
                                fontFamily = PoppinsFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp,
                                color = textPrimary,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// TROPHY ROOM SECTION
// ============================================================================

@Composable
private fun PremiumTrophyRoomHeader(
    unlockedCount: Int,
    totalCount: Int,
    isDarkMode: Boolean,
    onClick: () -> Unit,
    textPrimary: Color
) {
    val goldColor = Color(0xFFD4AF37)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = KairosIcons.EmojiEvents,
                contentDescription = null,
                tint = goldColor,
                modifier = Modifier.size(26.dp)
            )
            Text(
                text = "Pinned Badges",
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = textPrimary
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = if (isDarkMode) IdentityRoomColors.CardBackgroundDark
                        else IdentityRoomColors.CardBackgroundLight,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "$unlockedCount / $totalCount",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isDarkMode) IdentityRoomColors.TextSecondaryDark
                            else IdentityRoomColors.TextSecondaryLight,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            Icon(
                imageVector = KairosIcons.ChevronRight,
                contentDescription = "View all achievements",
                tint = if (isDarkMode) IdentityRoomColors.TextTertiaryDark
                       else IdentityRoomColors.TextTertiaryLight,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun PremiumFeaturedAchievementsRow(
    achievements: List<AchievementEntity>,
    surfaceElevated: Color,
    textPrimary: Color,
    accentColor: Color
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(
            items = achievements,
            key = { _, item -> item.id }
        ) { index, achievement ->
            PremiumFeaturedAchievementCard(
                achievement = achievement,
                surfaceElevated = surfaceElevated,
                textPrimary = textPrimary,
                index = index
            )
        }
    }
}

@Composable
private fun PremiumFeaturedAchievementCard(
    achievement: AchievementEntity,
    surfaceElevated: Color,
    textPrimary: Color,
    index: Int
) {
    val rarity = try {
        AchievementRarity.valueOf(achievement.rarity.uppercase())
    } catch (e: Exception) {
        AchievementRarity.COMMON
    }

    val achievementData = UiAchievements.getAchievementById(achievement.id)

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay((index * 80).toLong())
        isVisible = true
    }

    val scaleState = animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "achievement_scale"
    )

    Surface(
        modifier = Modifier
            .width(100.dp)
            .graphicsLayer {
                val s = scaleState.value
                scaleX = s
                scaleY = s
            }
            .clip(RoundedCornerShape(20.dp)),
        color = surfaceElevated,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon Circle
            val rarityColor = getRarityColor(rarity)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(rarityColor.copy(alpha = 0.15f))
                    .border(2.dp, rarityColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = achievementData?.icon ?: KairosIcons.EmojiEvents,
                    contentDescription = null,
                    tint = rarityColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = achievement.name,
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun PremiumRecentUnlocksSection(
    unlockedAchievements: List<AchievementEntity>,
    lockedAchievements: List<AchievementEntity>,
    surfaceColor: Color,
    surfaceElevated: Color,
    textPrimary: Color,
    textSecondary: Color,
    textTertiary: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Recent Unlocks",
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = textSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Show recent unlocked (last 2) and next locked (first 2)
            val recentUnlocked = unlockedAchievements.takeLast(2)
            val nextLocked = lockedAchievements.take(2)

            recentUnlocked.forEach { achievement ->
                PremiumCompactBadge(
                    achievement = achievement,
                    isUnlocked = true,
                    surfaceColor = surfaceElevated,
                    textPrimary = textPrimary,
                    textTertiary = textTertiary,
                    modifier = Modifier.weight(1f)
                )
            }

            nextLocked.forEach { achievement ->
                PremiumCompactBadge(
                    achievement = achievement,
                    isUnlocked = false,
                    surfaceColor = surfaceColor,
                    textPrimary = textPrimary,
                    textTertiary = textTertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PremiumCompactBadge(
    achievement: AchievementEntity,
    isUnlocked: Boolean,
    surfaceColor: Color,
    textPrimary: Color,
    textTertiary: Color,
    modifier: Modifier = Modifier
) {
    val rarity = try {
        AchievementRarity.valueOf(achievement.rarity.uppercase())
    } catch (e: Exception) {
        AchievementRarity.COMMON
    }

    val achievementData = UiAchievements.getAchievementById(achievement.id)
    val lockedGray = Color(0xFF888888)
    val rarityColor = getRarityColor(rarity)

    Surface(
        modifier = modifier.clip(RoundedCornerShape(16.dp)),
        color = if (isUnlocked) surfaceColor else surfaceColor.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUnlocked) rarityColor.copy(alpha = 0.15f)
                        else lockedGray.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isUnlocked) {
                    Icon(
                        imageVector = achievementData?.icon ?: KairosIcons.EmojiEvents,
                        contentDescription = null,
                        tint = rarityColor,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = KairosIcons.Lock,
                        contentDescription = null,
                        tint = lockedGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = achievement.name,
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                color = if (isUnlocked) textPrimary else textTertiary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ============================================================================
// GROWTH QUOTE CARD
// ============================================================================

@Composable
private fun PremiumGrowthQuoteCard(
    textSecondary: Color,
    accentColor: Color
) {
    val quotes = remember {
        listOf(
            "Every step forward is progress.",
            "Your growth journey is unique and beautiful.",
            "Small daily improvements lead to stunning results.",
            "The path to wisdom begins with self-reflection.",
            "You're building something remarkable."
        )
    }
    val quote = remember { quotes.random() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(accentColor.copy(alpha = 0.08f))
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = KairosIcons.FormatQuote,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = quote,
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                color = textSecondary,
                fontStyle = FontStyle.Italic,
                lineHeight = 24.sp
            )
        }
    }
}

// ============================================================================
// SIGN OUT SECTION
// ============================================================================

@Composable
private fun SignOutSection(
    onSignOut: () -> Unit,
    accentColor: Color,
    textSecondary: Color
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    text = "Sign Out",
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to sign out of your account?",
                    fontFamily = PoppinsFamily
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        onSignOut()
                    }
                ) {
                    Text(
                        text = "Sign Out",
                        fontFamily = PoppinsFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE53935)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(
                        text = "Cancel",
                        fontFamily = PoppinsFamily
                    )
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedButton(
            onClick = { showConfirmDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFFE53935)
            ),
            border = BorderStroke(1.dp, Color(0xFFE53935).copy(alpha = 0.3f))
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Sign Out",
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You can always sign back in",
            fontFamily = PoppinsFamily,
            fontSize = 12.sp,
            color = textSecondary.copy(alpha = 0.6f)
        )
    }
}

// ============================================================================
// HELPER FUNCTIONS
// ============================================================================

/**
 * Maps achievement rarity enum to its corresponding color.
 */
private fun getRarityColor(rarity: AchievementRarity): Color {
    return when (rarity) {
        AchievementRarity.COMMON -> RarityCommon
        AchievementRarity.UNCOMMON -> RarityUncommon
        AchievementRarity.RARE -> RarityRare
        AchievementRarity.EPIC -> RarityEpic
        AchievementRarity.LEGENDARY -> RarityLegendary
        AchievementRarity.MYTHIC -> RarityMythic
    }
}

private fun formatCompactNumber(number: Int): String {
    return when {
        number >= 1000000 -> String.format("%.1fM", number / 1000000.0)
        number >= 1000 -> String.format("%.1fk", number / 1000.0)
        else -> number.toString()
    }
}

private fun getLevelFromPoints(points: Int): Int {
    return when {
        points >= 10000 -> 10
        points >= 7500 -> 9
        points >= 5000 -> 8
        points >= 3500 -> 7
        points >= 2500 -> 6
        points >= 1500 -> 5
        points >= 1000 -> 4
        points >= 500 -> 3
        points >= 200 -> 2
        else -> 1
    }
}

private fun calculateLevelProgress(points: Int): Float {
    val level = getLevelFromPoints(points)
    val currentThreshold = getLevelThreshold(level)
    val nextThreshold = getLevelThreshold(level + 1)

    return if (nextThreshold > currentThreshold) {
        (points - currentThreshold).toFloat() / (nextThreshold - currentThreshold)
    } else {
        1f
    }
}

private fun getLevelThreshold(level: Int): Int {
    return when (level) {
        1 -> 0
        2 -> 200
        3 -> 500
        4 -> 1000
        5 -> 1500
        6 -> 2500
        7 -> 3500
        8 -> 5000
        9 -> 7500
        10 -> 10000
        else -> 15000
    }
}

// ============================================================================
// GROWTH JOURNEY CARD
// ============================================================================

@Composable
private fun GrowthJourneyCard(
    currentStreak: Int,
    dominantTheme: String?,
    todayLearning: String?,
    totalPoints: Int,
    surfaceColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    textTertiary: Color,
    accentColor: Color,
    isDarkMode: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = surfaceColor
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = KairosIcons.TrendingUp,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "The Growth Journey",
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = textPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Most Common Theme
            GrowthJourneyRow(
                label = "Your focus this week",
                value = dominantTheme?.replaceFirstChar { it.uppercase() } ?: "Start journaling to discover",
                textSecondary = textSecondary,
                textPrimary = textPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Consistency narrative
            GrowthJourneyRow(
                label = "Consistency",
                value = when {
                    currentStreak >= 30 -> "$currentStreak days of unwavering commitment"
                    currentStreak >= 7 -> "$currentStreak days of consistent journaling"
                    currentStreak >= 3 -> "$currentStreak days and building momentum"
                    currentStreak == 1 -> "Today is day one. Every journey starts here"
                    else -> "Ready to begin your streak"
                },
                textSecondary = textSecondary,
                textPrimary = textPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // What you've learned
            GrowthJourneyRow(
                label = "Today's insight",
                value = todayLearning ?: "Journal today to unlock your insight",
                textSecondary = textSecondary,
                textPrimary = textPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Points to Grow
            GrowthJourneyRow(
                label = "Points to Grow",
                value = formatCompactNumber(totalPoints),
                textSecondary = textSecondary,
                textPrimary = textPrimary
            )
        }
    }
}

@Composable
fun SoulIdentityCard(
    context: com.kairos.app.domain.intelligence.UserContext,
    level: Int,
    isDarkMode: Boolean,
    onEditClick: () -> Unit,
    authDisplayName: String? = null,
    authEmail: String? = null,
    authPhotoUrl: String? = null
) {
    val cardBg = if (isDarkMode) IdentityRoomColors.CardBackgroundDark else IdentityRoomColors.CardBackgroundLight
    val accent = IdentityRoomColors.AccentGreen
    val textPrimary = if (isDarkMode) IdentityRoomColors.TextPrimaryDark else IdentityRoomColors.TextPrimaryLight
    val textSecondary = if (isDarkMode) IdentityRoomColors.TextSecondaryDark else IdentityRoomColors.TextSecondaryLight

    val displayName = authDisplayName ?: context.displayName

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(32.dp),
        color = cardBg,
        border = BorderStroke(1.dp, textPrimary.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar & Level Ring Concept
            Box(contentAlignment = Alignment.Center) {
                // Static progress ring (minimalist)
                Canvas(modifier = Modifier.size(120.dp)) {
                    drawCircle(
                        color = textPrimary.copy(alpha = 0.05f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawArc(
                        color = accent,
                        startAngle = -90f,
                        sweepAngle = 280f, // Example progress
                        useCenter = false,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                
                // Avatar Circle
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(textPrimary.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (authPhotoUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(authPhotoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = textSecondary
                        )
                    }
                }
                
                // Level Badge (Floating Neon)
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp),
                    shape = CircleShape,
                    color = accent,
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = level.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Archetype Name
            Text(
                text = context.userArchetype.name,
                style = TextStyle(
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                    color = accent
                )
            )
            
            Text(
                text = displayName,
                style = TextStyle(
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = textPrimary
                )
            )

            if (!authEmail.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = authEmail,
                    style = TextStyle(
                        fontFamily = PoppinsFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = textSecondary
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Growth Journey Narrative Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = textPrimary.copy(alpha = 0.03f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "JOURNEY SUMMARY",
                            style = TextStyle(
                                fontFamily = PoppinsFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp,
                                color = textSecondary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You've been consistent for ${context.daysWithKairos} days. Your mood has been ${context.recentMoodTrend.name.lowercase()}, showing real resilience.",
                        style = TextStyle(
                            fontFamily = PoppinsFamily,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = textSecondary
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun GrowthJourneyRow(
    label: String,
    value: String,
    textSecondary: Color,
    textPrimary: Color
) {
    Column {
        Text(
            text = label,
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = textSecondary
        )
        Text(
            text = value,
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = textPrimary,
            lineHeight = 20.sp
        )
    }
}

// ============================================================================
// CONSISTENCY SCORE CARD
// ============================================================================

@Composable
private fun ConsistencyScoreCard(
    currentStreak: Int,
    longestStreak: Int,
    journalEntries: Int,
    daysOnKairos: Int,
    surfaceColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    textTertiary: Color,
    accentColor: Color
) {
    val consistencyRatio = if (daysOnKairos > 0) {
        (journalEntries.toFloat() / daysOnKairos).coerceIn(0f, 1f)
    } else 0f

    val streakProgress = if (longestStreak > 0) {
        (currentStreak.toFloat() / longestStreak).coerceIn(0f, 1f)
    } else if (currentStreak > 0) 1f else 0f

    val animatedProgressState = animateFloatAsState(
        targetValue = streakProgress,
        animationSpec = tween(durationMillis = 1000, easing = EaseInOutCubic),
        label = "consistency_progress"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = surfaceColor
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Consistency ring
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 8.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2

                    // Track
                    drawCircle(
                        color = accentColor.copy(alpha = 0.15f),
                        radius = radius,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Progress arc
                    drawArc(
                        color = accentColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgressState.value,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        topLeft = Offset(
                            (size.width - 2 * radius) / 2,
                            (size.height - 2 * radius) / 2
                        ),
                        size = Size(2 * radius, 2 * radius)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(streakProgress * 100).toInt()}%",
                        fontFamily = PoppinsFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = textPrimary
                    )
                }
            }

            // Stats
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Consistency Score",
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when {
                        currentStreak >= longestStreak && currentStreak > 0 -> "Personal best! Keep going"
                        streakProgress >= 0.8f -> "Almost at your best"
                        streakProgress >= 0.5f -> "Solid progress this period"
                        currentStreak > 0 -> "Building your rhythm"
                        else -> "Start today to build your score"
                    },
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = textSecondary,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Current",
                            fontFamily = PoppinsFamily,
                            fontSize = 11.sp,
                            color = textTertiary
                        )
                        Text(
                            text = "$currentStreak days",
                            fontFamily = PoppinsFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = textPrimary
                        )
                    }
                    Column {
                        Text(
                            text = "Best",
                            fontFamily = PoppinsFamily,
                            fontSize = 11.sp,
                            color = textTertiary
                        )
                        Text(
                            text = "$longestStreak days",
                            fontFamily = PoppinsFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = textPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementShelf(
    unlockedCount: Int,
    totalCount: Int,
    achievements: List<AchievementEntity>,
    onViewAllClick: () -> Unit,
    isDarkMode: Boolean
) {
    val textPrimary = if (isDarkMode) IdentityRoomColors.TextPrimaryDark else IdentityRoomColors.TextPrimaryLight
    val textSecondary = if (isDarkMode) IdentityRoomColors.TextSecondaryDark else IdentityRoomColors.TextSecondaryLight

    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Trophy Shelf",
                    style = TextStyle(
                        fontFamily = PoppinsFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = textPrimary
                    )
                )
                Text(
                    text = "$unlockedCount of $totalCount collected",
                    style = TextStyle(
                        fontFamily = PoppinsFamily,
                        fontSize = 13.sp,
                        color = textSecondary
                    )
                )
            }
            
            Text(
                text = "View All",
                modifier = Modifier.clickable { onViewAllClick() },
                style = TextStyle(
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = IdentityRoomColors.AccentGreen
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(end = 24.dp)
        ) {
            items(achievements) { achievement ->
                AchievementBadge(achievement = achievement, isDarkMode = isDarkMode)
            }
        }
    }
}

@Composable
fun AchievementBadge(
    achievement: AchievementEntity,
    isDarkMode: Boolean
) {
    val cardBg = if (isDarkMode) IdentityRoomColors.CardBackgroundElevatedDark else IdentityRoomColors.CardBackgroundElevatedLight
    val textPrimary = if (isDarkMode) IdentityRoomColors.TextPrimaryDark else IdentityRoomColors.TextPrimaryLight

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = RoundedCornerShape(16.dp),
            color = cardBg,
            border = BorderStroke(1.dp, textPrimary.copy(alpha = 0.05f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = IdentityRoomColors.AccentGreen,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = achievement.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                color = textPrimary
            )
        )
    }
}

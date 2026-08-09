package com.kairos.app.ui.screens.profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.kairos.app.ui.animation.KairosDurations
import com.kairos.app.ui.animation.KairosReveal
import com.kairos.app.domain.identity.KairosBanners
import com.kairos.app.ui.components.BannerRenderer
import com.kairos.app.ui.components.kairos.KairosGlassSurface
import com.kairos.app.ui.components.kairos.KairosIconButton
import com.kairos.app.ui.components.kairos.KairosMark
import com.kairos.app.ui.components.kairos.KairosReadingSurface
import com.kairos.app.ui.theme.KairosClay
import com.kairos.app.ui.theme.KairosPeriwinkle
import com.kairos.app.ui.theme.KairosRadius
import com.kairos.app.ui.theme.KairosSpacing
import com.kairos.app.ui.theme.KairosTheme
import com.kairos.app.ui.theme.ThemeMode
import kotlinx.coroutines.delay

/**
 * Focused profile surface inspired by editorial profile cards rather than a
 * gamification dashboard. It shows only identity, learning evidence, recent
 * themes, and one useful weekly observation.
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
            contentPadding = PaddingValues(bottom = 36.dp)
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
            item(key = "profile-rhythm") {
                KairosReveal(visible = reveal, delayMillis = 110) {
                    ProfileRhythmSection(
                        state = state,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 820.dp)
                            .padding(horizontal = KairosSpacing.screen, vertical = 28.dp)
                    )
                }
            }
            item(key = "profile-week") {
                KairosReveal(visible = reveal, delayMillis = 170) {
                    WeeklyReflectionSection(
                        state = state,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 820.dp)
                            .padding(horizontal = KairosSpacing.screen)
                    )
                }
            }
            item(key = "profile-achievements") {
                KairosReveal(visible = reveal, delayMillis = 230) {
                    AchievementEntry(
                        state = state,
                        onClick = onNavigateToAchievements,
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 640.dp)
            .background(ProfileInk)
    ) {
        val activeBanner = remember(state.bannerId) {
            KairosBanners.findById(state.bannerId) ?: KairosBanners.getDefaultBanner()
        }
        BannerRenderer(
            banner = activeBanner,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = scrollOffset * 0.14f
                    scaleX = 1.035f
                    scaleY = 1.035f
                },
            showAnimation = true,
            cornerRadius = 0.dp
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.18f),
                            ProfileInk.copy(alpha = 0.42f),
                            ProfileInk.copy(alpha = 0.88f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
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
                DarkGlassIconButton(
                    icon = Icons.AutoMirrored.Outlined.ArrowBack,
                    description = "Back",
                    onClick = onBack
                )
                KairosMark(
                    modifier = Modifier.size(38.dp),
                    tint = ProfilePaper,
                    accent = MaterialTheme.colorScheme.primary,
                    revealed = visible
                )
                DarkGlassIconButton(
                    icon = Icons.Outlined.Settings,
                    description = "Settings",
                    onClick = onSettings
                )
            }

            Spacer(Modifier.height(34.dp))

            ProfileAvatar(
                photoUrl = state.authPhotoUrl,
                name = resolvedName(state),
                modifier = Modifier.size(88.dp)
            )

            Spacer(Modifier.height(18.dp))

            KairosReveal(
                visible = visible,
                delayMillis = 70,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
            ) {
                ProfileIdentityPanel(
                    state = state,
                    onEdit = onEdit,
                    onCustomize = onCustomize
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileIdentityPanel(
    state: ProfileUiState,
    onEdit: () -> Unit,
    onCustomize: () -> Unit
) {
    val name = resolvedName(state)
    val themes = state.userContext.recentThemes
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .take(4)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(18.dp, RoundedCornerShape(26.dp), ambientColor = Color(0x66000000), spotColor = Color(0x59000000))
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF232C3F), Color(0xFF141A25))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(26.dp))
            .padding(horizontal = 24.dp, vertical = 26.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Medium,
                        color = ProfilePaper,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        text = profileHandle(state),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ProfilePaper.copy(alpha = 0.66f)
                    )
                }
                DarkGlassIconButton(
                    icon = Icons.Outlined.Edit,
                    description = "Edit profile",
                    onClick = onEdit,
                    compact = true
                )
            }

            Text(
                text = state.bio.ifBlank {
                    "Learning a little more clearly, one useful word and reflection at a time."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = ProfilePaper.copy(alpha = 0.88f),
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.12f
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileStat(
                    value = state.wordsLearned.toString(),
                    label = "Words",
                    modifier = Modifier.weight(1f)
                )
                ProfileStat(
                    value = state.journalEntries.toString(),
                    label = "Reflections",
                    modifier = Modifier.weight(1f)
                )
                ProfileStat(
                    value = state.currentStreak.toString(),
                    label = "Day rhythm",
                    modifier = Modifier.weight(1f)
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileTag(state.title)
                ProfileTag("Level ${state.level}")
                themes.forEach { ProfileTag(it) }
            }

            Surface(
                onClick = onCustomize,
                shape = RoundedCornerShape(18.dp),
                color = Color.White.copy(alpha = 0.10f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color.White.copy(alpha = 0.14f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = ProfilePaper,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Customize earned identity",
                        style = MaterialTheme.typography.labelLarge,
                        color = ProfilePaper,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = ProfilePaper.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

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
                icon = Icons.Outlined.AutoStories,
                value = state.wordsLearned.toString(),
                label = "words retained",
                accent = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            RhythmTile(
                icon = Icons.Outlined.History,
                value = state.longestStreak.toString(),
                label = "best rhythm",
                accent = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            RhythmTile(
                icon = Icons.Outlined.CheckCircle,
                value = state.daysOnKairos.toString(),
                label = "days with Kairos",
                accent = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

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

@Composable
private fun AchievementEntry(
    state: ProfileUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.shadow(6.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x2E1B2A4A), spotColor = Color(0x261B2A4A)),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            KairosGlassSurface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(KairosRadius.controlLarge),
                strong = true
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.EmojiEvents,
                        contentDescription = null,
                        tint = KairosClay
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Milestones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${state.achievementsUnlocked} earned, kept secondary to the learning itself",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProfileAvatar(
    photoUrl: String?,
    name: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(ProfilePaper.copy(alpha = 0.12f))
            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.9f), CircleShape)
            .shadow(12.dp, CircleShape, ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
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
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = ProfilePaper
            )
        }
    }
}

@Composable
private fun DarkGlassIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val size = if (compact) 44.dp else 48.dp
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(size)
            .semantics { contentDescription = description },
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.36f),
        contentColor = ProfilePaper,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(if (compact) 19.dp else 21.dp))
        }
    }
}

@Composable
private fun ProfileStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = ProfilePaper
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = ProfilePaper.copy(alpha = 0.56f),
            maxLines = 1
        )
    }
}

@Composable
private fun ProfileTag(label: String) {
    Surface(
        shape = RoundedCornerShape(KairosRadius.control),
        color = Color.White.copy(alpha = 0.09f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = ProfilePaper.copy(alpha = 0.78f),
            maxLines = 1
        )
    }
}

@Composable
private fun RhythmTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .heightIn(min = 144.dp)
            .shadow(6.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x2E1B2A4A), spotColor = Color(0x261B2A4A)),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
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

@Composable
private fun SectionHeading(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.semantics { heading() }
    )
}

@Composable
private fun ProfileLoading(onBack: () -> Unit) {
    Box(Modifier.fillMaxSize().background(ProfileInk)) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            DarkGlassIconButton(
                icon = Icons.AutoMirrored.Outlined.ArrowBack,
                description = "Back",
                onClick = onBack
            )
        }
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center).size(30.dp),
            color = KairosPeriwinkle,
            strokeWidth = 2.dp
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

/** Night paper tokens for the profile hero, matching the Paper & Ink dark scheme. */
private val ProfileInk = Color(0xFF0B0E15)
private val ProfilePaper = Color(0xFFE9EDF6)
private val ProfilePanel = Color(0xFF1E2636)
private val ProfilePanelHairline = Color(0xFF2A3345)

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

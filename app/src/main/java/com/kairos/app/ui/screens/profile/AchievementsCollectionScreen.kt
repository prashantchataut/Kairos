package com.kairos.app.ui.screens.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.kairos.app.domain.model.AchievementProgress
import com.kairos.app.ui.animation.KairosDurations
import com.kairos.app.ui.animation.KairosReveal
import com.kairos.app.ui.animation.rememberKairosReducedMotion
import com.kairos.app.ui.components.kairos.KairosAppBackground
import com.kairos.app.ui.components.kairos.KairosEmptyState
import com.kairos.app.ui.components.kairos.KairosGlassSurface
import com.kairos.app.ui.components.kairos.KairosIconButton
import com.kairos.app.ui.components.kairos.KairosReadingSurface
import com.kairos.app.ui.icons.KairosIcons
import com.kairos.app.ui.theme.KairosRadius
import com.kairos.app.ui.theme.KairosSpacing
import com.kairos.app.ui.theme.SerifFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Milestones are evidence of practice, not a second home-screen dashboard.
 * Locked items explain what meaningful behaviour remains; they never reward
 * opening the app, tapping repeatedly, or maintaining artificial engagement.
 */
@Composable
fun AchievementsCollectionScreen(
    onNavigateBack: () -> Unit,
    viewModel: AchievementsCollectionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

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
                KairosIconButton(KairosIcons.ArrowBack, "Back", onNavigateBack)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Milestones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Evidence of learning and reflection",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(Modifier.size(48.dp))
            }

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.error != null && state.allAchievements.isEmpty() -> KairosEmptyState(
                    icon = KairosIcons.ErrorOutline,
                    title = "Milestones unavailable",
                    body = state.error ?: "Kairos could not read your progress.",
                    actionLabel = "Return",
                    onAction = onNavigateBack
                )
                else -> AchievementCollectionContent(
                    state = state,
                    onFilter = viewModel::selectFilter,
                    onSelect = viewModel::selectAchievement
                )
            }
        }
    }

    state.selectedAchievement?.let { achievement ->
        AchievementDetailSheet(
            achievement = achievement,
            onDismiss = viewModel::clearSelectedAchievement
        )
    }
}

@Composable
private fun AchievementCollectionContent(
    state: AchievementsCollectionUiState,
    onFilter: (AchievementFilter) -> Unit,
    onSelect: (AchievementProgress) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = KairosSpacing.screen,
            end = KairosSpacing.screen,
            bottom = 48.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            KairosReveal(visible = true, delayMillis = 20) {
                MilestoneOverview(state)
            }
        }

        if (state.closestMilestones.isNotEmpty()) {
            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Within reach",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = SerifFamily,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            items(state.closestMilestones, key = { "near_${it.id}" }) { milestone ->
                NearMilestoneCard(milestone, onClick = { onSelect(milestone) })
            }
        }

        item {
            Spacer(Modifier.height(6.dp))
            AchievementFilterRow(selected = state.selectedFilter, onSelected = onFilter)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    state.selectedFilter.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = SerifFamily
                )
                Text(
                    "${state.filteredAchievements.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (state.filteredAchievements.isEmpty()) {
            item {
                KairosEmptyState(
                    icon = KairosIcons.EmojiEvents,
                    title = "No milestones here yet",
                    body = "Try another collection filter.",
                    actionLabel = "Show all",
                    onAction = { onFilter(AchievementFilter.ALL) }
                )
            }
        } else {
            items(state.filteredAchievements, key = AchievementProgress::id) { achievement ->
                AchievementRow(achievement = achievement, onClick = { onSelect(achievement) })
            }
        }
    }
}

@Composable
private fun MilestoneOverview(state: AchievementsCollectionUiState) {
    val reduceMotion = rememberKairosReducedMotion()
    val animatedProgress by animateFloatAsState(
        targetValue = state.completionFraction,
        animationSpec = tween(if (reduceMotion) 1 else 700),
        label = "achievement_completion"
    )
    val scheme = MaterialTheme.colorScheme

    KairosReadingSurface(
        modifier = Modifier.fillMaxWidth(),
        accent = Color(0xFF7C70D9),
        contentPadding = PaddingValues(22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(Modifier.size(104.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize().semantics { contentDescription = "${(animatedProgress * 100).toInt()} percent of milestones earned" }) {
                    val stroke = 9.dp.toPx()
                    drawArc(
                        color = scheme.outlineVariant,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(stroke)
                    )
                    drawArc(
                        color = scheme.primary,
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        style = Stroke(stroke)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${state.unlockedCount}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("of ${state.totalCount}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("A record of real practice", style = MaterialTheme.typography.headlineSmall, fontFamily = SerifFamily)
                Text(
                    "Milestones unlock through recall, reflection, consistency, and promises kept—not passive screen time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${state.totalPointsFromAchievements} milestone XP earned",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun NearMilestoneCard(achievement: AchievementProgress, onClick: () -> Unit) {
    val accent = rarityColor(achievement.rarity)
    KairosGlassSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = achievementShape(achievement),
        onClick = onClick,
        contentPadding = PaddingValues(17.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AchievementGlyph(achievement, accent, modifier = Modifier.size(46.dp))
                Column(Modifier.weight(1f)) {
                    Text(visibleName(achievement), fontWeight = FontWeight.SemiBold)
                    Text(
                        "${achievement.remaining} ${progressUnit(achievement)} to go",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text("${(achievement.progressFraction * 100).toInt()}%", style = MaterialTheme.typography.labelLarge, color = accent)
            }
            LinearProgressIndicator(
                progress = { achievement.progressFraction.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = accent,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        }
    }
}

@Composable
private fun AchievementFilterRow(
    selected: AchievementFilter,
    onSelected: (AchievementFilter) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AchievementFilter.entries.forEach { filter ->
            val active = filter == selected
            Surface(
                onClick = { onSelected(filter) },
                shape = RoundedCornerShape(18.dp),
                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                border = BorderStroke(1.dp, if (active) Color.Transparent else MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.semantics {
                    this.selected = active
                    role = Role.RadioButton
                }
            ) {
                Text(
                    filter.displayName,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun AchievementRow(achievement: AchievementProgress, onClick: () -> Unit) {
    val accent = rarityColor(achievement.rarity)
    val mystery = achievement.isHidden && !achievement.isUnlocked
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = achievementShape(achievement),
        color = if (achievement.isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            1.dp,
            if (achievement.isUnlocked) accent.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                AchievementGlyph(achievement, accent, modifier = Modifier.size(52.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            if (mystery) "Undiscovered" else achievement.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (achievement.isUnlocked) {
                            Icon(KairosIcons.CheckCircle, contentDescription = "Earned", tint = accent, modifier = Modifier.size(17.dp))
                        }
                    }
                    Text(
                        if (mystery) "Keep exploring meaningful practices to reveal this milestone." else achievement.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                RarityPill(achievement.rarity, accent)
            }

            if (!achievement.isUnlocked && !mystery) {
                LinearProgressIndicator(
                    progress = { achievement.progressFraction.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(5.dp),
                    color = accent,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "${achievement.currentProgress} / ${achievement.requirement}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "+${achievement.rewardPoints} XP",
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else if (achievement.isUnlocked) {
                Text(
                    achievement.unlockedAt?.let { "Earned ${formatAchievementDate(it)} · +${achievement.rewardPoints} XP" }
                        ?: "Earned · +${achievement.rewardPoints} XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = accent,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun AchievementGlyph(
    achievement: AchievementProgress,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .background(
                if (achievement.isUnlocked) accent.copy(alpha = 0.16f) else scheme.surfaceContainerHighest,
                glyphShape(achievement.rarity)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = achievementIcon(achievement),
            contentDescription = null,
            tint = if (achievement.isUnlocked) accent else scheme.onSurfaceVariant.copy(alpha = 0.65f),
            modifier = Modifier.size(25.dp)
        )
    }
}

@Composable
private fun RarityPill(rarity: String, accent: Color) {
    Surface(shape = RoundedCornerShape(12.dp), color = accent.copy(alpha = 0.12f)) {
        Text(
            rarity.replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = accent,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AchievementDetailSheet(
    achievement: AchievementProgress,
    onDismiss: () -> Unit
) {
    val accent = rarityColor(achievement.rarity)
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = KairosSpacing.screen)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AchievementGlyph(achievement, accent, modifier = Modifier.size(88.dp))
            Text(
                visibleName(achievement),
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = SerifFamily,
                fontWeight = FontWeight.SemiBold
            )
            RarityPill(achievement.rarity, accent)
            Text(
                if (achievement.isHidden && !achievement.isUnlocked) {
                    "This milestone reveals itself only after the underlying practice is completed."
                } else achievement.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider()
            if (achievement.isUnlocked) {
                Text(
                    achievement.celebrationMessage.ifBlank { "This milestone records a practice you genuinely completed." },
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = SerifFamily
                )
                Text(
                    achievement.unlockedAt?.let { "Earned ${formatAchievementDate(it)}" } ?: "Earned",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LinearProgressIndicator(
                    progress = { achievement.progressFraction.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = accent,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${achievement.currentProgress} of ${achievement.requirement}")
                    Text("${achievement.remaining} remaining", color = accent, fontWeight = FontWeight.SemiBold)
                }
            }
            Text(
                "Reward: ${achievement.rewardPoints} XP",
                style = MaterialTheme.typography.labelLarge,
                color = accent
            )
        }
    }
}

private fun achievementShape(achievement: AchievementProgress): RoundedCornerShape = when (achievement.category.lowercase()) {
    "reflection" -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 6.dp)
    "temporal" -> RoundedCornerShape(topStart = 6.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    "mastery" -> RoundedCornerShape(topStart = 16.dp, topEnd = 8.dp, bottomStart = 12.dp, bottomEnd = 16.dp)
    else -> RoundedCornerShape(16.dp)
}

private fun glyphShape(rarity: String): RoundedCornerShape = when (rarity.lowercase()) {
    "mythic" -> RoundedCornerShape(topStart = 16.dp, topEnd = 6.dp, bottomStart = 16.dp, bottomEnd = 6.dp)
    "legendary" -> RoundedCornerShape(topStart = 6.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 6.dp)
    "epic" -> RoundedCornerShape(16.dp)
    else -> RoundedCornerShape(16.dp)
}

private fun rarityColor(rarity: String): Color = when (rarity.lowercase()) {
    "mythic" -> Color(0xFFB56A22)
    "legendary" -> Color(0xFFAD7A15)
    "epic" -> Color(0xFF8A64C5)
    "rare" -> Color(0xFF497FC6)
    "uncommon" -> Color(0xFF3E9B85)
    else -> Color(0xFF74828D)
}

private fun achievementIcon(achievement: AchievementProgress): ImageVector {
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

private fun visibleName(achievement: AchievementProgress): String =
    if (achievement.isHidden && !achievement.isUnlocked) "Undiscovered milestone" else achievement.name

private fun progressUnit(achievement: AchievementProgress): String = when (achievement.category.lowercase()) {
    "wisdom" -> "learning actions"
    "reflection" -> "reflections"
    "consistency" -> "days of practice"
    "temporal" -> "future letters"
    else -> "meaningful actions"
}

private fun formatAchievementDate(timestamp: Long): String =
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestamp))

package com.prody.prashant.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prody.prashant.R
import com.prody.prashant.domain.progress.NextAction
import com.prody.prashant.domain.progress.NextActionType
import com.prody.prashant.domain.progress.TodayProgress
import com.prody.prashant.ui.icons.ProdyIcons
import com.prody.prashant.ui.theme.*

/**
 * ================================================================================================
 * ACTIVE PROGRESS LAYER COMPONENTS
 * ================================================================================================
 *
 * These components visualize the user's active progress and behavior-driven suggestions.
 * They follow Prody's flat design principles and performance best practices.
 */

// ================================================================================================
// NEXT ACTION CARD
// ================================================================================================

/**
 * Displays a contextual "Next Action" suggestion based on user behavior.
 * Uses a notification-style card with an accent border for visual prominence.
 */
@Composable
fun NextActionCard(
    nextAction: NextAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = when (nextAction.type) {
        NextActionType.START_JOURNAL, NextActionType.FOLLOW_UP_JOURNAL -> ProdyForestGreen
        NextActionType.REVIEW_WORDS, NextActionType.LEARN_WORD -> ProdyWarmAmber
        NextActionType.WRITE_FUTURE_MESSAGE -> Color(0xFF9C27B0)
        NextActionType.REFLECT_ON_QUOTE -> ProdyInfo
        NextActionType.COMPLETE_CHALLENGE -> Color(0xFFF59E0B)
    }

    val icon = when (nextAction.type) {
        NextActionType.START_JOURNAL, NextActionType.FOLLOW_UP_JOURNAL -> ProdyIcons.Edit
        NextActionType.REVIEW_WORDS, NextActionType.LEARN_WORD -> ProdyIcons.School
        NextActionType.WRITE_FUTURE_MESSAGE -> ProdyIcons.Send
        NextActionType.REFLECT_ON_QUOTE -> ProdyIcons.Lightbulb
        NextActionType.COMPLETE_CHALLENGE -> ProdyIcons.EmojiEvents
    }

    ProdyNotificationCard(
        accentColor = accentColor,
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        backgroundColor = ProdySurfaceLight
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ProdyTokens.Spacing.md)
        ) {
            // Icon with background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nextAction.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ProdyTextPrimaryLight
                )
                Text(
                    text = nextAction.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = ProdyTextSecondaryLight
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = ProdyTextSecondaryLight.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ================================================================================================
// TODAY PROGRESS CARD
// ================================================================================================

/**
 * Summarizes the user's progress for today (points, entries, word count).
 * Features subtle animations for point increases.
 */
@Composable
fun TodayProgressCard(
    progress: TodayProgress,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ProdyClickableCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        backgroundColor = ProdySurfaceLight
    ) {
        Column(
            modifier = Modifier.padding(ProdyTokens.Spacing.cardPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S PROGRESS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = ProdyForestGreen,
                    letterSpacing = 1.sp
                )

                if (progress.pointsEarned > 0) {
                    PointsBadge(points = progress.pointsEarned)
                }
            }

            Spacer(modifier = Modifier.height(ProdyTokens.Spacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProgressStatItem(
                    label = "Entries",
                    value = progress.journalEntries.toString(),
                    icon = ProdyIcons.Book,
                    color = ProdyForestGreen
                )
                ProgressStatItem(
                    label = "Words",
                    value = progress.wordsWritten.toString(),
                    icon = ProdyIcons.Edit,
                    color = ProdyInfo
                )
                ProgressStatItem(
                    label = "Learned",
                    value = progress.wordsLearned.toString(),
                    icon = ProdyIcons.School,
                    color = ProdyWarmAmber
                )
            }

            if (progress.isEmpty) {
                Spacer(modifier = Modifier.height(ProdyTokens.Spacing.sm))
                Text(
                    text = "Your journey for today starts here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ProdyTextSecondaryLight,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PointsBadge(points: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "points_glow")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Surface(
        color = ProdyWarmAmber.copy(alpha = 0.15f),
        shape = CircleShape,
        modifier = Modifier.graphicsLayer { this.alpha = alpha }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = ProdyWarmAmber,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "+$points pts",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = ProdyWarmAmber
            )
        }
    }
}

@Composable
private fun ProgressStatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(IntrinsicSize.Min)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ProdyTextPrimaryLight
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = ProdyTextSecondaryLight
        )
    }
}

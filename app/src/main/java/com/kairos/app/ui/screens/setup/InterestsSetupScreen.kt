package com.kairos.app.ui.screens.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.ui.components.kairos.KairosGlassSurface
import com.kairos.app.ui.components.kairos.KairosIconButton
import com.kairos.app.ui.components.kairos.KairosPrimaryButton
import com.kairos.app.ui.components.kairos.KairosScreenHeader
import com.kairos.app.ui.components.kairos.KairosSecondaryButton
import com.kairos.app.ui.icons.KairosIcons
import com.kairos.app.ui.theme.KairosRadius
import com.kairos.app.ui.theme.KairosSpacing

/** Word interest categories, aligned with the vocabulary catalog. */
val WordInterestOptions = listOf(
    "self-improvement" to "Self-improvement",
    "communication" to "Communication",
    "emotion" to "Emotion",
    "mindfulness" to "Mindfulness",
    "learning" to "Learning",
    "reflection" to "Reflection",
    "business" to "Business",
    "academic" to "Academic"
)

/** Quote interest themes, aligned with the quote catalog. */
val QuoteInterestOptions = listOf(
    "wisdom" to "Wisdom",
    "growth" to "Growth",
    "mindfulness" to "Mindfulness",
    "reflection" to "Reflection",
    "resilience" to "Resilience",
    "gratitude" to "Gratitude",
    "perspective" to "Perspective",
    "stoic" to "Stoic",
    "learning" to "Learning",
    "action" to "Action",
    "rest" to "Rest",
    "courage" to "Courage"
)

/**
 * Setup screen for content interests, reachable from onboarding and Settings.
 * Lets the user tune vocabulary pace, session size, word interests, and quote
 * themes — all of which feed the recommendation algorithms.
 */
@Composable
fun InterestsSetupScreen(
    onNavigateBack: () -> Unit,
    viewModel: InterestsSetupViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) {
            viewModel.consumeSaved()
            onNavigateBack()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        KairosScreenHeader(
            title = "Interests & setup",
            subtitle = "Words and quotes Kairos should favor",
            actions = {
                KairosIconButton(
                    icon = KairosIcons.ArrowBack,
                    contentDescription = "Back",
                    onClick = onNavigateBack
                )
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = KairosSpacing.screen, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SetupGlassCard(title = "Vocabulary pace") {
                Text(
                    text = "Gentler words first, or a stretch? Recommendations use this as a starting level.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                PaceSelector(
                    difficulty = state.difficulty,
                    onSelect = viewModel::setDifficulty
                )
            }

            SetupGlassCard(title = "Cards per session") {
                Text(
                    text = "How many words each practice session should aim for.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                SessionSizeSelector(
                    sessionSize = state.sessionSize,
                    onSelect = viewModel::setSessionSize
                )
            }

            SetupGlassCard(title = "Words you want to learn") {
                Text(
                    text = "Words from these areas surface first in practice and daily picks.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                InterestChipRow(
                    options = WordInterestOptions,
                    selected = state.wordCategories,
                    onToggle = viewModel::toggleWordCategory
                )
            }

            SetupGlassCard(title = "Quotes and ideas") {
                Text(
                    text = "Quotes from these themes appear more often in Today.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                InterestChipRow(
                    options = QuoteInterestOptions,
                    selected = state.quoteCategories,
                    onToggle = viewModel::toggleQuoteCategory
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            KairosPrimaryButton(
                text = "Save preferences",
                onClick = viewModel::save,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .align(Alignment.CenterHorizontally)
            )
            KairosSecondaryButton(
                text = "Cancel",
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SetupGlassCard(
    title: String,
    content: @Composable () -> Unit
) {
    KairosGlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 560.dp),
        shape = RoundedCornerShape(KairosRadius.controlLarge),
        contentPadding = PaddingValues(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            content()
        }
    }
}

@Composable
private fun PaceSelector(
    difficulty: Int,
    onSelect: (Int) -> Unit
) {
    val options = listOf(2 to "Gentle", 3 to "Balanced", 4 to "Stretch")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (value, label) ->
            SelectorPill(
                label = label,
                selected = difficulty == value,
                onClick = { onSelect(value) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SessionSizeSelector(
    sessionSize: Int,
    onSelect: (Int) -> Unit
) {
    val options = listOf(3, 5, 10)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { value ->
            SelectorPill(
                label = "$value",
                selected = sessionSize == value,
                onClick = { onSelect(value) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SelectorPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(KairosRadius.control),
        color = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        },
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
            else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InterestChipRow(
    options: List<Pair<String, String>>,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (key, label) ->
            InterestChip(
                label = label,
                selected = key in selected,
                onClick = { onToggle(key) }
            )
        }
    }
}

@Composable
private fun InterestChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(KairosRadius.control),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

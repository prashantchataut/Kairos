package com.kairos.app.ui.screens.vocabulary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.data.local.entity.VocabularyEntity
import com.kairos.app.ui.animation.KairosDurations
import com.kairos.app.ui.animation.KairosEasing
import com.kairos.app.ui.animation.KairosReveal
import com.kairos.app.ui.animation.rememberKairosReducedMotion
import com.kairos.app.ui.components.kairos.KairosEmptyState
import com.kairos.app.ui.components.kairos.KairosIconButton
import com.kairos.app.ui.components.kairos.KairosPrimaryButton
import com.kairos.app.ui.components.kairos.KairosReadingSurface
import com.kairos.app.ui.components.kairos.KairosScreenHeader
import com.kairos.app.ui.components.kairos.KairosSecondaryButton
import com.kairos.app.ui.components.kairos.KairosSegmentedControl
import com.kairos.app.ui.components.kairos.KairosSkeletonList
import com.kairos.app.ui.icons.KairosIcons
import com.kairos.app.ui.theme.KairosRadius
import com.kairos.app.ui.theme.KairosSpacing

@Composable
fun FocusedLearnScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToSession: (SessionMode) -> Unit,
    viewModel: VocabularyListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var searchVisible by rememberSaveable { mutableStateOf(false) }
    val refreshMessage by viewModel.refreshMessage.collectAsStateWithLifecycle()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(refreshMessage) {
        refreshMessage?.let { message ->
            snackbar.showSnackbar(message)
            viewModel.clearRefreshMessage()
        }
    }
    val filters = VocabularyFilter.entries
    val selectedFilter = filters.indexOfFirst { it.key == state.currentFilter }.coerceAtLeast(0)

    Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
        KairosScreenHeader(
            title = "Learn",
            subtitle = learningSummary(state),
            actions = {
                KairosIconButton(
                    icon = if (searchVisible) KairosIcons.Close else KairosIcons.Outlined.Search,
                    contentDescription = if (searchVisible) "Close search" else "Search words",
                    selected = searchVisible,
                    onClick = {
                        searchVisible = !searchVisible
                        if (!searchVisible) viewModel.setQuery("")
                    }
                )
                KairosIconButton(
                    icon = if (state.showFavoritesOnly) KairosIcons.Favorite else KairosIcons.FavoriteBorder,
                    contentDescription = if (state.showFavoritesOnly) "Show all words" else "Show favorite words",
                    selected = state.showFavoritesOnly,
                    onClick = viewModel::toggleFavoritesOnly
                )
                KairosIconButton(
                    icon = KairosIcons.Outlined.Refresh,
                    contentDescription = "Find fresh words",
                    selected = refreshing,
                    onClick = viewModel::refreshOnlineContent
                )
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 840.dp)
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = KairosSpacing.screen),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (!state.isLoading) {
                SessionLauncher(
                    dueReviewCount = state.dueReviewCount,
                    totalCount = state.totalCount,
                    onStartReview = { onNavigateToSession(SessionMode.REVIEW_DUE) },
                    onStartNewWords = { onNavigateToSession(SessionMode.NEW_WORDS) }
                )
            }
            if (!state.isLoading && state.totalCount > 0) {
                KairosReveal(visible = true) {
                    LearningOverview(state = state)
                }
            }
            AnimatedVisibility(
                visible = searchVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                KairosSearchField(
                    query = state.query,
                    onQueryChange = viewModel::setQuery,
                    placeholder = "Search words, meanings, or topics"
                )
            }
            KairosSegmentedControl(
                items = filters.map { it.label },
                selectedIndex = selectedFilter,
                onSelected = { index -> viewModel.setFilter(filters[index].key) }
            )
        }

        when {
            state.isLoading -> {
                KairosReadingSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 840.dp)
                        .align(Alignment.CenterHorizontally)
                        .padding(KairosSpacing.screen)
                ) {
                    KairosSkeletonList(rows = 5)
                }
            }
            state.error != null -> {
                KairosEmptyState(
                    icon = KairosIcons.ErrorOutline,
                    title = "Vocabulary is unavailable",
                    body = state.error ?: "The local library could not be read.",
                    actionLabel = "Try again",
                    onAction = viewModel::retry,
                    modifier = Modifier.fillMaxSize()
                )
            }
            state.words.isEmpty() -> {
                KairosEmptyState(
                    icon = KairosIcons.Outlined.MenuBook,
                    title = emptyTitle(state),
                    body = emptyBody(state),
                    actionLabel = "Reset filters",
                    onAction = {
                        viewModel.setQuery("")
                        viewModel.setFilter(VocabularyFilter.ALL.key)
                        if (state.showFavoritesOnly) viewModel.toggleFavoritesOnly()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 840.dp)
                        .align(Alignment.CenterHorizontally),
                    contentPadding = PaddingValues(
                        start = KairosSpacing.screen,
                        end = KairosSpacing.screen,
                        top = 16.dp,
                        bottom = 40.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(state.words, key = { _, word -> word.id }) { index, word ->
                        KairosReveal(
                            visible = true,
                            delayMillis = (index.coerceAtMost(7) * 24)
                        ) {
                            VocabularyRow(
                                word = word,
                                onClick = { onNavigateToDetail(word.id) },
                                onFavorite = { viewModel.toggleFavorite(word.id) }
                            )
                        }
                    }
                }
            }
        }
    }
    SnackbarHost(
        hostState = snackbar,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 96.dp)
    )
    }
}

/**
 * Flat paper launcher for the hybrid practice session. Shows the spaced-repetition
 * review queue when words are due, plus a fresh-words option so the stream never runs dry.
 */
@Composable
private fun SessionLauncher(
    dueReviewCount: Int,
    totalCount: Int,
    onStartReview: () -> Unit,
    onStartNewWords: () -> Unit
) {
    KairosReadingSurface(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Practice session",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Recall cards, then match meanings",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(KairosRadius.control),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = if (dueReviewCount > 0) "$dueReviewCount due" else "All reviewed",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KairosPrimaryButton(
                    text = if (dueReviewCount > 0) "Review $dueReviewCount words" else "Review",
                    onClick = onStartReview,
                    modifier = Modifier.weight(1f),
                    icon = KairosIcons.Outlined.Refresh
                )
                KairosSecondaryButton(
                    text = "New words",
                    onClick = onStartNewWords,
                    modifier = Modifier.weight(1f),
                    icon = if (totalCount > 0) KairosIcons.Outlined.School else null
                )
            }
        }
    }
}

/**
 * Quiet progress line: "X of Y saved" over a thin 4dp rule.
 * No hero percentages, no stat tiles.
 */
@Composable
private fun LearningOverview(state: VocabularyListUiState) {
    // Group the curated catalog into themed modules so Learn reads as a
    // journey through topics, not a flat database list.
    val modules = remember(state.words) {
        state.words
            .filter { it.category.isNotBlank() && it.category != "general" }
            .groupBy { it.category }
            .map { (category, words) ->
                ModuleSummary(
                    category = category,
                    total = words.size,
                    saved = words.count { it.isFavorite }
                )
            }
            .sortedByDescending { it.total }
            .take(6)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Continue learning",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${state.savedCount} of ${state.totalCount} saved",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (modules.isEmpty()) {
            Text(
                text = "Words you save will collect into themes here.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(end = 8.dp)
            ) {
                items(modules, key = { it.category }) { module ->
                    ModuleCard(module)
                }
            }
        }
    }
}

private data class ModuleSummary(
    val category: String,
    val total: Int,
    val saved: Int
)

@Composable
private fun ModuleCard(module: ModuleSummary) {
    val scheme = MaterialTheme.colorScheme
    val accent = when (module.category) {
        "literary" -> MaterialTheme.colorScheme.tertiary
        "academic" -> MaterialTheme.colorScheme.primary
        "business" -> Color(0xFFE8960C)
        "communication" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    val progress = if (module.total == 0) 0f else module.saved.toFloat() / module.total

    Surface(
        modifier = Modifier
            .width(168.dp)
            .shadow(6.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x2E1B2A4A), spotColor = Color(0x261B2A4A)),
        shape = RoundedCornerShape(24.dp),
        color = scheme.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = module.category.take(2).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
            }
            Text(
                text = module.category.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${module.saved} saved · ${module.total} words",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = accent,
                trackColor = scheme.outlineVariant
            )
        }
    }
}

@Composable
private fun KairosSearchField(@Composable
private fun KairosSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String
) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(KairosRadius.controlLarge),
        leadingIcon = {
            Icon(KairosIcons.Outlined.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(KairosIcons.Close, contentDescription = "Clear search")
                }
            }
        },
        placeholder = { Text(placeholder) },
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.72f),
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}

@Composable
private fun VocabularyRow(
    word: VocabularyEntity,
    onClick: () -> Unit,
    onFavorite: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = buildString {
                    append(word.word)
                    if (word.partOfSpeech.isNotBlank()) append(", ${word.partOfSpeech}")
                    append(". ${word.definition}")
                    if (word.isFavorite) append(". Saved")
                }
            },
        shape = RoundedCornerShape(KairosRadius.controlLarge),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(start = 18.dp, top = 16.dp, bottom = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DifficultyMark(difficulty = word.difficulty, saved = word.isFavorite)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = word.word,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (word.partOfSpeech.isNotBlank()) {
                        Text(
                            text = word.partOfSpeech,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = word.definition,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (word.category.isNotBlank() && word.category != "general") {
                    Text(
                        text = word.category.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                    )
                }
            }
            IconButton(
                onClick = onFavorite,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (word.isFavorite) KairosIcons.Favorite else KairosIcons.FavoriteBorder,
                    contentDescription = if (word.isFavorite) "Remove ${word.word} from favorites" else "Add ${word.word} to favorites",
                    tint = if (word.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DifficultyMark(difficulty: Int, saved: Boolean) {
    val color = if (saved) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .size(42.dp)
            .semantics { contentDescription = if (saved) "Saved" else "Difficulty ${difficulty.coerceIn(1, 5)} of 5" },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(38.dp),
            shape = RoundedCornerShape(14.dp),
            color = color.copy(alpha = 0.12f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (saved) KairosIcons.Favorite else KairosIcons.Outlined.School,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

private fun learningSummary(state: VocabularyListUiState): String = when {
    state.totalCount == 0 -> "A focused vocabulary practice space"
    state.savedCount == 0 -> "${state.totalCount} words in your library"
    else -> "${state.savedCount} saved of ${state.totalCount}"
}

private fun emptyTitle(state: VocabularyListUiState): String = when {
    state.query.isNotBlank() -> "No matching words"
    state.showFavoritesOnly -> "No favorite words yet"
    state.currentFilter == VocabularyFilter.SAVED.key -> "No saved words yet"
    else -> "No words available"
}

private fun emptyBody(state: VocabularyListUiState): String = when {
    state.query.isNotBlank() -> "Try a broader spelling, meaning, or topic."
    state.showFavoritesOnly -> "Favorite useful words as you find them, then return here for a clean shortlist."
    state.currentFilter == VocabularyFilter.SAVED.key -> "Tap the heart on any word to keep it here."
    else -> "The local vocabulary catalog is empty."
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Vocabulary row",
    showBackground = true,
    backgroundColor = 0xFFF6F4EF,
    widthDp = 390
)
@Composable
private fun VocabularyRowPreview() {
    com.kairos.app.ui.theme.KairosTheme(themeMode = com.kairos.app.ui.theme.ThemeMode.LIGHT) {
        VocabularyRow(
            word = VocabularyEntity(
                id = 1,
                word = "Lucid",
                definition = "Expressed clearly and easy to understand.",
                partOfSpeech = "adjective",
                category = "communication",
                difficulty = 2,
                isFavorite = true
            ),
            onClick = {},
            onFavorite = {}
        )
    }
}

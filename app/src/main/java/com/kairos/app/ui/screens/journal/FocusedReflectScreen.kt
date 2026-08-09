package com.kairos.app.ui.screens.journal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.data.local.entity.JournalEntryEntity
import com.kairos.app.domain.model.Mood
import com.kairos.app.ui.components.kairos.KairosEmptyState
import com.kairos.app.ui.components.kairos.KairosIconButton
import com.kairos.app.ui.components.kairos.KairosPrimaryButton
import com.kairos.app.ui.components.kairos.KairosScreenHeader
import com.kairos.app.ui.components.kairos.KairosSkeletonList
import com.kairos.app.ui.icons.KairosIcons
import com.kairos.app.ui.theme.KairosClay
import com.kairos.app.ui.theme.KairosRadius
import com.kairos.app.ui.theme.KairosSpacing
import com.kairos.app.ui.theme.SerifFamily
import com.kairos.app.ui.theme.color
import com.kairos.app.ui.theme.icon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Reflection, Moment Blue style: one large intimate prompt card, then the
 * journal as stacked object-like cards. The screen should feel meditative —
 * one strong visual moment, minimal chrome.
 */
@Composable
fun FocusedReflectScreen(
    onNavigateToNewEntry: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: JournalViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var searchVisible by rememberSaveable { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        KairosScreenHeader(
            title = "Reflect",
            subtitle = journalSummary(state),
            actions = {
                KairosIconButton(
                    icon = if (searchVisible) KairosIcons.Close else KairosIcons.Outlined.Search,
                    contentDescription = if (searchVisible) "Close search" else "Search reflections",
                    selected = searchVisible,
                    onClick = {
                        searchVisible = !searchVisible
                        if (!searchVisible) viewModel.setQuery("")
                    }
                )
                KairosIconButton(
                    icon = if (state.showBookmarkedOnly) KairosIcons.Bookmark else KairosIcons.Outlined.BookmarkBorder,
                    contentDescription = if (state.showBookmarkedOnly) "Show all reflections" else "Show bookmarked reflections",
                    selected = state.showBookmarkedOnly,
                    onClick = viewModel::toggleBookmarkFilter
                )
                KairosIconButton(
                    icon = KairosIcons.History,
                    contentDescription = "Reflection history",
                    onClick = onNavigateToHistory
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
            AnimatedVisibility(
                visible = searchVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ReflectSearchField(
                    query = state.query,
                    onQueryChange = viewModel::setQuery
                )
            }
            StartReflectionSurface(onStart = onNavigateToNewEntry)
        }

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 840.dp)
                        .align(Alignment.CenterHorizontally)
                        .padding(KairosSpacing.screen)
                        .clip(RoundedCornerShape(KairosRadius.readingSurface))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(24.dp)
                ) {
                    KairosSkeletonList(rows = 4)
                }
            }
            state.error != null -> {
                KairosEmptyState(
                    icon = KairosIcons.ErrorOutline,
                    title = "Reflections are unavailable",
                    body = state.error ?: "The local journal could not be read.",
                    actionLabel = "Try again",
                    onAction = viewModel::retry,
                    modifier = Modifier.fillMaxSize()
                )
            }
            state.entries.isEmpty() -> {
                KairosEmptyState(
                    icon = KairosIcons.Outlined.Edit,
                    title = reflectEmptyTitle(state),
                    body = reflectEmptyBody(state),
                    actionLabel = if (state.query.isNotBlank() || state.showBookmarkedOnly) "Reset filters" else "Write first reflection",
                    onAction = {
                        if (state.query.isNotBlank() || state.showBookmarkedOnly) {
                            viewModel.setQuery("")
                            if (state.showBookmarkedOnly) viewModel.toggleBookmarkFilter()
                        } else {
                            onNavigateToNewEntry()
                        }
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
                        top = 18.dp,
                        bottom = 28.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.entries, key = { it.id }) { entry ->
                        ReflectionRow(
                            entry = entry,
                            onClick = { onNavigateToDetail(entry.id) },
                            onBookmark = { viewModel.toggleBookmark(entry.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * The one strong moment: a large prompt card with a gentle blue wash, a serif
 * question, and a single primary action. A row of mood orbs below adds the
 * emotional register without turning the screen into a dashboard.
 */
@Composable
private fun StartReflectionSurface(onStart: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(KairosRadius.card)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, shape, ambientColor = Color(0x331B2A4A), spotColor = Color(0x2E1B2A4A))
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        scheme.primaryContainer,
                        scheme.surface
                    ),
                    startY = 0f,
                    endY = 560f
                )
            )
            .padding(horizontal = 24.dp, vertical = 26.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text(
                text = "Make one honest note",
                style = MaterialTheme.typography.headlineMedium,
                color = scheme.onSurface
            )
            Text(
                text = "What happened today that you want to remember?",
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = SerifFamily),
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Italic,
                lineHeight = MaterialTheme.typography.titleLarge.lineHeight * 1.2f,
                color = scheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KairosPrimaryButton(
                    text = "Write",
                    onClick = onStart,
                    icon = KairosIcons.Outlined.Edit,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Mood.entries.take(8).forEach { mood ->
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(mood.color.copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = mood.icon,
                            contentDescription = null,
                            tint = mood.color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReflectSearchField(
    query: String,
    onQueryChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(KairosRadius.controlLarge),
        leadingIcon = { Icon(KairosIcons.Outlined.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(KairosIcons.Close, contentDescription = "Clear search")
                }
            }
        },
        placeholder = { Text("Search words, titles, or tags") },
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
private fun ReflectionRow(
    entry: JournalEntryEntity,
    onClick: () -> Unit,
    onBookmark: () -> Unit
) {
    val title = remember(entry.id, entry.title, entry.content) {
        entry.title.ifBlank { entry.content.lineSequence().firstOrNull().orEmpty().take(72) }
    }
    val excerpt = remember(entry.id, entry.content) {
        entry.content.replace("\n", " ").trim()
    }
    val dateLabel = remember(entry.createdAt) { formatDateLabel(entry.createdAt) }
    val displayWordCount = remember(entry.wordCount, entry.content) {
        entry.wordCount.coerceAtLeast(
            entry.content.trim().split(Regex("\\s+")).count { it.isNotBlank() }
        )
    }
    val shape = RoundedCornerShape(KairosRadius.readingSurface)

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, shape, ambientColor = Color(0x1F1B2A4A), spotColor = Color(0x261B2A4A))
            .semantics {
                contentDescription = "$dateLabel. $title. ${displayWordCount} words${if (entry.isBookmarked) ". Bookmarked" else ""}"
            },
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(start = 20.dp, top = 18.dp, bottom = 18.dp, end = 8.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MoodMark(mood = entry.mood)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = title.ifBlank { "Untitled reflection" },
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = SerifFamily),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (excerpt.isNotBlank() && excerpt != title) {
                    Text(
                        text = excerpt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "$displayWordCount words",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.74f)
                )
            }
            IconButton(
                onClick = onBookmark,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (entry.isBookmarked) KairosIcons.Bookmark else KairosIcons.Outlined.BookmarkBorder,
                    contentDescription = if (entry.isBookmarked) "Remove bookmark" else "Bookmark reflection",
                    tint = if (entry.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MoodMark(mood: String) {
    val moodEnum = Mood.entries.firstOrNull { it.name.equals(mood, ignoreCase = true) }
    Surface(
        modifier = Modifier.size(42.dp),
        shape = RoundedCornerShape(14.dp),
        color = (moodEnum?.color ?: KairosClay).copy(alpha = 0.16f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (moodEnum != null) {
                Icon(
                    imageVector = moodEnum.icon,
                    contentDescription = null,
                    tint = moodEnum.color,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = mood.take(1).uppercase().ifBlank { "·" },
                    style = MaterialTheme.typography.labelLarge,
                    color = KairosClay,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun formatDateLabel(timestamp: Long): String {
    val formatter = java.util.TimeZone.getDefault().let { zone ->
        SimpleDateFormat("MMM d · h:mm a", Locale.getDefault()).apply { timeZone = zone }
    }
    return formatter.format(Date(timestamp))
}

private fun journalSummary(state: JournalUiState): String = when (state.totalEntries) {
    0 -> "A quiet place to notice what matters"
    1 -> "1 reflection"
    else -> "${state.totalEntries} reflections"
}

private fun reflectEmptyTitle(state: JournalUiState): String = when {
    state.query.isNotBlank() -> "No matching reflections"
    state.showBookmarkedOnly -> "No bookmarks yet"
    else -> "Begin with one sentence"
}

private fun reflectEmptyBody(state: JournalUiState): String = when {
    state.query.isNotBlank() -> "Try a broader phrase or clear the search."
    state.showBookmarkedOnly -> "Bookmark entries you want to revisit and they will collect here."
    else -> "Write what happened, what you noticed, or what you want to remember."
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Reflection row",
    showBackground = true,
    backgroundColor = 0xFFF5F7FC,
    widthDp = 390
)
@Composable
private fun ReflectionRowPreview() {
    com.kairos.app.ui.theme.KairosTheme(themeMode = com.kairos.app.ui.theme.ThemeMode.LIGHT) {
        ReflectionRow(
            entry = JournalEntryEntity(
                id = 1,
                title = "What changed today",
                content = "I slowed down before answering and the conversation became easier.",
                mood = "calm",
                wordCount = 12,
                isBookmarked = true,
                createdAt = System.currentTimeMillis()
            ),
            onClick = {},
            onBookmark = {}
        )
    }
}

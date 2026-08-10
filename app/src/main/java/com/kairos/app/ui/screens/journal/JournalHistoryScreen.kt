package com.kairos.app.ui.screens.journal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.data.local.entity.JournalEntryEntity
import com.kairos.app.domain.model.Mood
import com.kairos.app.ui.animation.KairosReveal
import com.kairos.app.ui.components.kairos.KairosAppBackground
import com.kairos.app.ui.components.kairos.KairosEmptyState
import com.kairos.app.ui.components.kairos.KairosGlassSurface
import com.kairos.app.ui.components.kairos.KairosIconButton
import com.kairos.app.ui.components.kairos.KairosPrimaryButton
import com.kairos.app.ui.icons.KairosIcons
import com.kairos.app.ui.theme.KairosRadius
import com.kairos.app.ui.theme.KairosSpacing
import com.kairos.app.ui.theme.SerifFamily
import com.kairos.app.ui.theme.color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalHistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: JournalHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilters by remember { mutableStateOf(false) }

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
                    Text("Reflection archive", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${state.totalEntryCount} entries",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                KairosIconButton(
                    KairosIcons.Tune,
                    "Filter reflections${if (state.activeFilterCount > 0) ", ${state.activeFilterCount} active" else ""}",
                    { showFilters = true },
                    selected = state.hasActiveFilters
                )
            }

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                state.error != null -> KairosEmptyState(
                    icon = KairosIcons.ErrorOutline,
                    title = "Archive unavailable",
                    body = state.error ?: "Your reflections could not be loaded.",
                    actionLabel = "Try again",
                    onAction = viewModel::retry
                )
                state.thisWeekEntries.isEmpty() && state.lastWeekEntries.isEmpty() && state.olderEntries.isEmpty() -> KairosEmptyState(
                    icon = KairosIcons.AutoStories,
                    title = if (state.hasActiveFilters) "Nothing matches these filters" else "Your archive is quiet",
                    body = if (state.hasActiveFilters) "Try a broader mood or date range." else "Reflections will collect here as a private record of change.",
                    actionLabel = if (state.hasActiveFilters) "Clear filters" else "Go back",
                    onAction = if (state.hasActiveFilters) viewModel::clearAllFilters else onNavigateBack
                )
                else -> HistoryTimeline(state, onNavigateToDetail, viewModel::loadMoreOlderEntries)
            }
        }
    }

    if (showFilters) {
        ModalBottomSheet(onDismissRequest = { showFilters = false }) {
            HistoryFilterSheet(
                state = state,
                onMood = viewModel::setFilterMood,
                onBookmarked = viewModel::setBookmarkedOnly,
                onDate = viewModel::setDateRangeFilter,
                onSort = viewModel::setSortOrder,
                onClear = viewModel::clearAllFilters,
                onDone = { showFilters = false }
            )
        }
    }
}

@Composable
private fun HistoryTimeline(
    state: JournalHistoryUiState,
    onOpen: (Long) -> Unit,
    onLoadMore: () -> Unit
) {
    val visibleOlder = state.olderEntries.take(state.displayedOlderCount)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = KairosSpacing.screen,
            end = KairosSpacing.screen,
            top = KairosSpacing.sm,
            bottom = 40.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            KairosReveal(visible = true, delayMillis = 20) {
                ArchiveSummary(state)
            }
        }
        historySection("This week", state.thisWeekEntries, onOpen)
        historySection("Last week", state.lastWeekEntries, onOpen)
        historySection("Earlier", visibleOlder, onOpen)
        if (visibleOlder.size < state.totalOlderCount) {
            item {
                KairosPrimaryButton(
                    text = "Load ${minOf(5, state.totalOlderCount - visibleOlder.size)} more",
                    onClick = onLoadMore,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.historySection(
    title: String,
    entries: List<JournalEntryEntity>,
    onOpen: (Long) -> Unit
) {
    if (entries.isEmpty()) return
    item(key = "header-$title") {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 14.dp, bottom = 2.dp)
        )
    }
    items(entries, key = { it.id }) { entry ->
        HistoryEntryCard(entry = entry, onClick = { onOpen(entry.id) })
    }
}

@Composable
private fun ArchiveSummary(state: JournalHistoryUiState) {
    val all = state.thisWeekEntries + state.lastWeekEntries + state.olderEntries
    val totalWords = all.sumOf { it.wordCount }
    val commonMood = all.groupingBy { it.mood }.eachCount().maxByOrNull { it.value }?.key
    KairosGlassSurface(
        modifier = Modifier.fillMaxWidth(),
        strong = true,
        shape = RoundedCornerShape(KairosRadius.readingSurface),
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            SummaryMetric("Entries", state.totalEntryCount.toString())
            SummaryMetric("Words", totalWords.toString())
            SummaryMetric("Common tone", commonMood?.let(Mood::fromString)?.displayName ?: "—")
        }
    }
}

@Composable
private fun SummaryMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun HistoryEntryCard(entry: JournalEntryEntity, onClick: () -> Unit) {
    val mood = Mood.fromString(entry.mood)
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(KairosRadius.controlLarge),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .background(mood.color.copy(alpha = 0.16f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) { Text(mood.emoji) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        entry.title.ifBlank { mood.displayName },
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = SerifFamily,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (entry.isBookmarked) {
                        Icon(KairosIcons.Bookmark, contentDescription = "Bookmarked", modifier = Modifier.size(17.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Text(
                    entry.content.replace('\n', ' '),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${formatHistoryDate(entry.createdAt)} · ${entry.wordCount} words",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.76f)
                )
            }
        }
    }
}

@Composable
private fun HistoryFilterSheet(
    state: JournalHistoryUiState,
    onMood: (String?) -> Unit,
    onBookmarked: (Boolean) -> Unit,
    onDate: (DateRangeFilter) -> Unit,
    onSort: (SortOrder) -> Unit,
    onClear: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KairosSpacing.screen)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("Shape the archive", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Bookmarked only", fontWeight = FontWeight.SemiBold)
                Text("Show entries you deliberately kept close.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = state.showBookmarkedOnly, onCheckedChange = onBookmarked)
        }
        FilterLabel("Mood")
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip("All", state.selectedFilterMood == null) { onMood(null) }
            Mood.entries.forEach { mood ->
                FilterChip("${mood.emoji} ${mood.displayName}", state.selectedFilterMood == mood.name) { onMood(mood.name) }
            }
        }
        FilterLabel("Date range")
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DateRangeFilter.entries.forEach { range ->
                FilterChip(range.displayLabel(), state.dateRangeFilter == range) { onDate(range) }
            }
        }
        FilterLabel("Order")
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SortOrder.entries.forEach { order ->
                FilterChip(order.displayLabel(), state.sortOrder == order) { onSort(order) }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(
                onClick = onClear,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                color = Color.Transparent,
                modifier = Modifier.weight(1f).height(52.dp)
            ) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Clear", fontWeight = FontWeight.SemiBold) } }
            KairosPrimaryButton("Done", onDone, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun FilterLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun FilterChip(text: String, active: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(KairosRadius.control),
        color = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        border = if (active) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.semantics { role = Role.RadioButton; contentDescription = text }
    ) { Text(text, modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp), style = MaterialTheme.typography.labelMedium) }
}

private fun DateRangeFilter.displayLabel(): String = when (this) {
    DateRangeFilter.ALL_TIME -> "All time"
    DateRangeFilter.THIS_WEEK -> "This week"
    DateRangeFilter.THIS_MONTH -> "This month"
    DateRangeFilter.LAST_3_MONTHS -> "3 months"
    DateRangeFilter.THIS_YEAR -> "This year"
}

private fun SortOrder.displayLabel(): String = when (this) {
    SortOrder.NEWEST_FIRST -> "Newest"
    SortOrder.OLDEST_FIRST -> "Oldest"
    SortOrder.HIGHEST_INTENSITY -> "Most intense"
    SortOrder.LOWEST_INTENSITY -> "Least intense"
}

private fun formatHistoryDate(timestamp: Long): String =
    SimpleDateFormat("MMM d · h:mm a", Locale.getDefault()).format(Date(timestamp))

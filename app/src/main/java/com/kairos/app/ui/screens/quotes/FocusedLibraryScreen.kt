package com.kairos.app.ui.screens.quotes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.data.local.entity.IdiomEntity
import com.kairos.app.data.local.entity.PhraseEntity
import com.kairos.app.data.local.entity.ProverbEntity
import com.kairos.app.data.local.entity.QuoteEntity
import com.kairos.app.ui.components.kairos.KairosEmptyState
import com.kairos.app.ui.components.kairos.KairosIconButton
import com.kairos.app.ui.components.kairos.KairosReadingSurface
import com.kairos.app.ui.components.kairos.KairosScreenHeader
import com.kairos.app.ui.components.kairos.KairosSegmentedControl
import com.kairos.app.ui.components.kairos.KairosSkeletonList
import com.kairos.app.ui.icons.KairosIcons
import com.kairos.app.ui.theme.KairosClay
import com.kairos.app.ui.theme.KairosRadius
import com.kairos.app.ui.theme.KairosSpacing
import com.kairos.app.ui.theme.SerifFamily

@Composable
fun FocusedLibraryScreen(
    initialTab: WisdomTab = WisdomTab.QUOTES,
    viewModel: QuotesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab.ordinal) }
    var searchVisible by rememberSaveable { mutableStateOf(false) }
    var favoritesOnly by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    val tabs = WisdomTab.entries

    Column(modifier = Modifier.fillMaxSize()) {
        KairosScreenHeader(
            title = "Library",
            subtitle = librarySummary(state),
            actions = {
                KairosIconButton(
                    icon = if (searchVisible) KairosIcons.Close else KairosIcons.Outlined.Search,
                    contentDescription = if (searchVisible) "Close search" else "Search library",
                    selected = searchVisible,
                    onClick = {
                        searchVisible = !searchVisible
                        if (!searchVisible) query = ""
                    }
                )
                KairosIconButton(
                    icon = if (favoritesOnly) KairosIcons.Favorite else KairosIcons.FavoriteBorder,
                    contentDescription = if (favoritesOnly) "Show all library items" else "Show favorite items",
                    selected = favoritesOnly,
                    onClick = { favoritesOnly = !favoritesOnly }
                )
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 840.dp)
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = KairosSpacing.screen),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedVisibility(
                visible = searchVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LibrarySearchField(
                    query = query,
                    onQueryChange = { query = it }
                )
            }
            KairosSegmentedControl(
                items = tabs.map { it.shortLabel },
                selectedIndex = selectedTab,
                onSelected = { selectedTab = it }
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
                    KairosSkeletonList(rows = 4)
                }
            }
            state.error != null -> {
                KairosEmptyState(
                    icon = KairosIcons.ErrorOutline,
                    title = "Library is unavailable",
                    body = state.error ?: "The local collection could not be read.",
                    actionLabel = "Try again",
                    onAction = viewModel::retry,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> LibraryContent(
                tab = tabs[selectedTab],
                state = state,
                query = query,
                favoritesOnly = favoritesOnly,
                onReset = {
                    query = ""
                    favoritesOnly = false
                },
                onQuoteFavorite = viewModel::toggleQuoteFavorite,
                onProverbFavorite = viewModel::toggleProverbFavorite,
                onIdiomFavorite = viewModel::toggleIdiomFavorite,
                onPhraseFavorite = viewModel::togglePhraseFavorite
            )
        }
    }
}

@Composable
private fun LibraryContent(
    tab: WisdomTab,
    state: QuotesUiState,
    query: String,
    favoritesOnly: Boolean,
    onReset: () -> Unit,
    onQuoteFavorite: (QuoteEntity) -> Unit,
    onProverbFavorite: (ProverbEntity) -> Unit,
    onIdiomFavorite: (IdiomEntity) -> Unit,
    onPhraseFavorite: (PhraseEntity) -> Unit
) {
    when (tab) {
        WisdomTab.QUOTES -> {
            val filteredItems = remember(state.quotes, query, favoritesOnly) {
                state.quotes.filter { quote ->
                    (!favoritesOnly || quote.isFavorite) && quote.matches(query)
                }
            }
            LibraryListOrEmpty(filteredItems.isEmpty(), query, favoritesOnly, onReset) {
                items(filteredItems, key = { it.id }) { quote ->
                    QuoteLibraryRow(quote, onFavorite = { onQuoteFavorite(quote) })
                }
            }
        }
        WisdomTab.PROVERBS -> {
            val filteredItems = remember(state.proverbs, query, favoritesOnly) {
                state.proverbs.filter { proverb ->
                    (!favoritesOnly || proverb.isFavorite) && proverb.matches(query)
                }
            }
            LibraryListOrEmpty(filteredItems.isEmpty(), query, favoritesOnly, onReset) {
                items(filteredItems, key = { it.id }) { proverb ->
                    ProverbLibraryRow(proverb, onFavorite = { onProverbFavorite(proverb) })
                }
            }
        }
        WisdomTab.IDIOMS -> {
            val filteredItems = remember(state.idioms, query, favoritesOnly) {
                state.idioms.filter { idiom ->
                    (!favoritesOnly || idiom.isFavorite) && idiom.matches(query)
                }
            }
            LibraryListOrEmpty(filteredItems.isEmpty(), query, favoritesOnly, onReset) {
                items(filteredItems, key = { it.id }) { idiom ->
                    IdiomLibraryRow(idiom, onFavorite = { onIdiomFavorite(idiom) })
                }
            }
        }
        WisdomTab.PHRASES -> {
            val filteredItems = remember(state.phrases, query, favoritesOnly) {
                state.phrases.filter { phrase ->
                    (!favoritesOnly || phrase.isFavorite) && phrase.matches(query)
                }
            }
            LibraryListOrEmpty(filteredItems.isEmpty(), query, favoritesOnly, onReset) {
                items(filteredItems, key = { it.id }) { phrase ->
                    PhraseLibraryRow(phrase, onFavorite = { onPhraseFavorite(phrase) })
                }
            }
        }
    }
}

@Composable
private fun LibraryListOrEmpty(
    isEmpty: Boolean,
    query: String,
    favoritesOnly: Boolean,
    onReset: () -> Unit,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    if (isEmpty) {
        KairosEmptyState(
            icon = KairosIcons.Outlined.AutoStories,
            title = when {
                query.isNotBlank() -> "Nothing matches that search"
                favoritesOnly -> "No favorites here yet"
                else -> "This collection is empty"
            },
            body = when {
                query.isNotBlank() -> "Try a broader word, author, meaning, or topic."
                favoritesOnly -> "Save ideas worth returning to and they will collect here."
                else -> "Add curated content to the local catalog to populate this section."
            },
            actionLabel = "Reset filters",
            onAction = onReset,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                contentPadding = PaddingValues(
                    start = KairosSpacing.screen,
                    end = KairosSpacing.screen,
                    top = 18.dp,
                    bottom = 40.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
    }
}

@Composable
private fun QuoteLibraryRow(
    quote: QuoteEntity,
    onFavorite: () -> Unit
) {
    LibrarySurface(
        favorite = quote.isFavorite,
        favoriteDescription = if (quote.isFavorite) "Remove quote from favorites" else "Save quote",
        onFavorite = onFavorite,
        contentDescription = "Quote by ${quote.author}. ${quote.content}"
    ) {
        Text(
            text = "“",
            style = MaterialTheme.typography.displayMedium.copy(fontFamily = SerifFamily),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
            modifier = Modifier.height(18.dp)
        )
        Text(
            text = quote.content,
            style = MaterialTheme.typography.headlineSmall.copy(fontFamily = SerifFamily),
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Normal,
            lineHeight = MaterialTheme.typography.headlineSmall.lineHeight * 1.18f
        )
        Text(
            text = quote.author.ifBlank { "Unknown author" },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val detail = listOf(quote.source, quote.category.takeUnless { it == "wisdom" }.orEmpty())
            .filter { it.isNotBlank() }
            .joinToString(" · ")
        if (detail.isNotBlank()) LibraryMetadata(detail)
        if (quote.reflectionPrompt.isNotBlank()) {
            Text(
                text = quote.reflectionPrompt,
                style = MaterialTheme.typography.bodyMedium,
                color = KairosClay
            )
        }
    }
}

@Composable
private fun ProverbLibraryRow(
    proverb: ProverbEntity,
    onFavorite: () -> Unit
) {
    LibrarySurface(
        favorite = proverb.isFavorite,
        favoriteDescription = if (proverb.isFavorite) "Remove proverb from favorites" else "Save proverb",
        onFavorite = onFavorite,
        contentDescription = "Proverb. ${proverb.content}. Meaning: ${proverb.meaning}"
    ) {
        Text(
            text = proverb.content,
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = SerifFamily),
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = proverb.meaning,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (proverb.origin.isNotBlank()) LibraryMetadata(proverb.origin)
    }
}

@Composable
private fun IdiomLibraryRow(
    idiom: IdiomEntity,
    onFavorite: () -> Unit
) {
    LibrarySurface(
        favorite = idiom.isFavorite,
        favoriteDescription = if (idiom.isFavorite) "Remove idiom from favorites" else "Save idiom",
        onFavorite = onFavorite,
        contentDescription = "Idiom. ${idiom.phrase}. Meaning: ${idiom.meaning}"
    ) {
        Text(
            text = idiom.phrase,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = idiom.meaning,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (idiom.exampleSentence.isNotBlank()) {
            Text(
                text = idiom.exampleSentence,
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = SerifFamily),
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PhraseLibraryRow(
    phrase: PhraseEntity,
    onFavorite: () -> Unit
) {
    LibrarySurface(
        favorite = phrase.isFavorite,
        favoriteDescription = if (phrase.isFavorite) "Remove phrase from favorites" else "Save phrase",
        onFavorite = onFavorite,
        contentDescription = "Phrase. ${phrase.phrase}. Meaning: ${phrase.meaning}"
    ) {
        Text(
            text = phrase.phrase,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = phrase.meaning,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val detail = listOf(phrase.formality, phrase.category)
            .filter { it.isNotBlank() && it != "general" && it != "neutral" }
            .joinToString(" · ")
        if (detail.isNotBlank()) LibraryMetadata(detail)
    }
}

@Composable
private fun LibrarySurface(
    favorite: Boolean,
    favoriteDescription: String,
    onFavorite: () -> Unit,
    contentDescription: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(KairosRadius.readingSurface), ambientColor = Color(0x1F1B2A4A), spotColor = Color(0x1A1B2A4A))
            .semantics { this.contentDescription = contentDescription },
        shape = RoundedCornerShape(KairosRadius.readingSurface),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 20.dp, end = 8.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = content
            )
            IconButton(onClick = onFavorite, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = if (favorite) KairosIcons.Favorite else KairosIcons.FavoriteBorder,
                    contentDescription = favoriteDescription,
                    tint = if (favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LibraryMetadata(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun LibrarySearchField(
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
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(KairosIcons.Close, contentDescription = "Clear search")
                }
            }
        },
        placeholder = { Text("Search ideas, authors, and meanings") },
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.72f),
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}

private val WisdomTab.shortLabel: String
    get() = when (this) {
        WisdomTab.QUOTES -> "Quotes"
        WisdomTab.PROVERBS -> "Proverbs"
        WisdomTab.IDIOMS -> "Idioms"
        WisdomTab.PHRASES -> "Phrases"
    }

private fun QuoteEntity.matches(query: String): Boolean = query.isBlank() ||
    content.contains(query, ignoreCase = true) ||
    author.contains(query, ignoreCase = true) ||
    category.contains(query, ignoreCase = true) ||
    tags.contains(query, ignoreCase = true)

private fun ProverbEntity.matches(query: String): Boolean = query.isBlank() ||
    content.contains(query, ignoreCase = true) ||
    meaning.contains(query, ignoreCase = true) ||
    origin.contains(query, ignoreCase = true)

private fun IdiomEntity.matches(query: String): Boolean = query.isBlank() ||
    phrase.contains(query, ignoreCase = true) ||
    meaning.contains(query, ignoreCase = true) ||
    category.contains(query, ignoreCase = true)

private fun PhraseEntity.matches(query: String): Boolean = query.isBlank() ||
    phrase.contains(query, ignoreCase = true) ||
    meaning.contains(query, ignoreCase = true) ||
    category.contains(query, ignoreCase = true)

private fun librarySummary(state: QuotesUiState): String {
    val total = state.quotes.size + state.proverbs.size + state.idioms.size + state.phrases.size
    return if (total == 0) "A curated place for language and thought" else "$total ideas to revisit"
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Library quote",
    showBackground = true,
    backgroundColor = 0xFFF6F4EF,
    widthDp = 390
)
@Composable
private fun QuoteLibraryRowPreview() {
    com.kairos.app.ui.theme.KairosTheme(themeMode = com.kairos.app.ui.theme.ThemeMode.LIGHT) {
        QuoteLibraryRow(
            quote = QuoteEntity(
                id = 1,
                content = "Attention is the beginning of devotion.",
                author = "Mary Oliver",
                source = "Upstream",
                reflectionPrompt = "What received your full attention today?",
                isFavorite = true
            ),
            onFavorite = {}
        )
    }
}

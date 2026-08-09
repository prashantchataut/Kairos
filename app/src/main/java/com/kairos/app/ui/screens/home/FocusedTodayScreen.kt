package com.kairos.app.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.domain.recommendation.ContentInteractionType
import com.kairos.app.ui.components.kairos.KairosActionRow
import com.kairos.app.ui.components.kairos.KairosEmptyState
import com.kairos.app.ui.components.kairos.KairosGlassSurface
import com.kairos.app.ui.components.kairos.KairosIconButton
import com.kairos.app.ui.components.kairos.KairosMark
import com.kairos.app.ui.components.kairos.KairosPrimaryButton
import com.kairos.app.ui.components.kairos.KairosSecondaryButton
import com.kairos.app.ui.components.kairos.KairosSkeletonList
import com.kairos.app.ui.icons.KairosIcons
import com.kairos.app.ui.theme.KairosMotion
import com.kairos.app.ui.theme.KairosRadius
import com.kairos.app.ui.theme.KairosSpacing
import com.kairos.app.ui.theme.SerifFamily
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * The focused daily loop, Moment Blue style.
 *
 * One dominant moment at a time: a large blue word card and a layered thought
 * card, preceded by a strong personalized greeting. Content stays the daily
 * vocabulary + reflection ritual — only the hierarchy changed.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FocusedTodayScreen(
    onNavigateToVocabulary: () -> Unit,
    onNavigateToQuotes: () -> Unit,
    onNavigateToJournal: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(state.isLoading, state.error, state.wordOfTheDay, state.dailyQuote) {
        if (!state.isLoading && state.error == null) viewModel.onDailyContentVisible()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TodayGreeting(
            userName = state.userName,
            actions = {
                KairosIconButton(
                    icon = KairosIcons.Outlined.Notifications,
                    contentDescription = "Notification settings",
                    onClick = onNavigateToNotificationSettings
                )
                KairosIconButton(
                    icon = KairosIcons.Outlined.Person,
                    contentDescription = "Profile",
                    onClick = onNavigateToProfile
                )
            }
        )

        when {
            state.isLoading -> TodayLoading()
            state.error != null -> TodayError(
                message = state.error ?: "Today could not be loaded.",
                onRetry = viewModel::retry
            )
            state.wordOfTheDay.isBlank() && state.dailyQuote.isBlank() -> {
                KairosEmptyState(
                    icon = KairosIcons.Outlined.AutoStories,
                    title = "Your daily moment is not ready",
                    body = "Kairos needs at least one word or quote in the local library. Add content and return here; the selection will remain stable for the day.",
                    actionLabel = "Open library",
                    onAction = onNavigateToQuotes,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    flingBehavior = snapBehavior,
                    contentPadding = PaddingValues(
                        start = KairosSpacing.screen,
                        end = KairosSpacing.screen,
                        top = 4.dp,
                        bottom = 40.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (state.wordOfTheDay.isNotBlank()) {
                        item(key = "daily-word") {
                            WordMoment(
                                state = state,
                                onToggleSave = viewModel::toggleWordSaved,
                                onPractice = {
                                    viewModel.onDailyWordFeedback(ContentInteractionType.OPENED)
                                    onNavigateToVocabulary()
                                },
                                onTooEasy = { viewModel.onDailyWordFeedback(ContentInteractionType.TOO_EASY) },
                                onTooHard = { viewModel.onDailyWordFeedback(ContentInteractionType.TOO_HARD) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 680.dp)
                            )
                        }
                    }
                    if (state.dailyQuote.isNotBlank()) {
                        item(key = "daily-thought") {
                            ThoughtMoment(
                                state = state,
                                onReflect = {
                                    viewModel.onDailyQuoteFeedback(ContentInteractionType.COMPLETED)
                                    onNavigateToJournal()
                                },
                                onLibrary = {
                                    viewModel.onDailyQuoteFeedback(ContentInteractionType.OPENED)
                                    onNavigateToQuotes()
                                },
                                onToggleSave = viewModel::toggleQuoteSaved,
                                onMoreLikeThis = { viewModel.onDailyQuoteFeedback(ContentInteractionType.MORE_LIKE_THIS) },
                                onLessLikeThis = { viewModel.onDailyQuoteFeedback(ContentInteractionType.LESS_LIKE_THIS) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 680.dp)
                            )
                        }
                    }
                    item(key = "weekly-rhythm") {
                        Text(
                            text = progressLine(state),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Large personalized greeting — typography as the design element.
 */
@Composable
private fun TodayGreeting(
    userName: String,
    actions: @Composable () -> Unit
) {
    val cleanName = userName.takeIf { it.isNotBlank() && it != "Growth Seeker" }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(
                start = KairosSpacing.screen,
                end = KairosSpacing.screen,
                top = 12.dp,
                bottom = 14.dp
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = LocalDate.now().format(DATE_FORMATTER),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = cleanName?.let { "Good ${timeOfDay()}, ${it.substringBefore(' ')}" }
                        ?: "One word. One thought worth keeping.",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = { actions() }
            )
        }
    }
}

@Composable
private fun WordMoment(
    state: TodayUiState,
    onToggleSave: () -> Unit,
    onPractice: () -> Unit,
    onTooEasy: () -> Unit,
    onTooHard: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tuneExpanded by rememberSaveable { mutableStateOf(false) }
    val scheme = MaterialTheme.colorScheme
    val cardShape = RoundedCornerShape(KairosRadius.card)

    Box(modifier = modifier) {
        // Stacked card behind, for depth.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .height(120.dp)
                .clip(cardShape)
                .background(scheme.surfaceContainerHighest)
                .graphicsLayer { rotationZ = -1.2f }
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(14.dp, cardShape, ambientColor = Color(0x66102A8A), spotColor = Color(0x55102A8A))
                .clip(cardShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            scheme.primary,
                            Color(0xFF1B3FBF),
                            Color(0xFF0F2C8F)
                        )
                    )
                )
                .graphicsLayer { rotationZ = 0.5f }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KairosMark(
                            modifier = Modifier.size(42.dp),
                            tint = Color.White,
                            accent = Color.White.copy(alpha = 0.9f)
                        )
                        KairosGlassSurface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            strong = state.wordSaved,
                            onClick = onToggleSave
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (state.wordSaved) KairosIcons.Favorite else KairosIcons.FavoriteBorder,
                                    contentDescription = if (state.wordSaved) "Remove from saved words" else "Save this word",
                                    tint = if (state.wordSaved) scheme.primary else Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "DAILY WORD",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.72f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = state.wordOfTheDay,
                        style = MaterialTheme.typography.displayMedium.copy(fontFamily = SerifFamily),
                        color = Color.White,
                        modifier = Modifier.semantics { heading() }
                    )
                    val metadata = listOfNotNull(
                        state.wordPartOfSpeech.takeIf(String::isNotBlank),
                        state.wordPronunciation.takeIf(String::isNotBlank)?.let { "/$it/" }
                    ).joinToString("  ·  ")
                    if (metadata.isNotBlank()) {
                        Text(
                            text = metadata,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.72f)
                        )
                    }
                    Text(
                        text = state.wordDefinition,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White.copy(alpha = 0.95f)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (state.wordExampleSentence.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(KairosRadius.controlLarge))
                                .background(Color.White.copy(alpha = 0.12f))
                                .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(KairosRadius.controlLarge))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "“${state.wordExampleSentence}”",
                                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = SerifFamily),
                                fontStyle = FontStyle.Italic,
                                color = Color.White.copy(alpha = 0.92f)
                            )
                        }
                    }
                    AnimatedVisibility(
                        visible = tuneExpanded,
                        enter = expandVertically(animationSpec = androidx.compose.animation.core.tween(KairosMotion.state)) + fadeIn(),
                        exit = shrinkVertically(animationSpec = androidx.compose.animation.core.tween(KairosMotion.state)) + fadeOut()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                onClick = onTooEasy,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(KairosRadius.controlLarge),
                                color = Color.White.copy(alpha = 0.14f),
                                contentColor = Color.White
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Too easy", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                            Surface(
                                onClick = onTooHard,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(KairosRadius.controlLarge),
                                color = Color.White.copy(alpha = 0.14f),
                                contentColor = Color.White
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Too hard", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = onPractice,
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(KairosRadius.controlLarge),
                            color = Color.White,
                            contentColor = Color(0xFF0F2C8F)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Practice", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                        TextButton(
                            onClick = { tuneExpanded = !tuneExpanded },
                            modifier = Modifier.height(52.dp)
                        ) {
                            Text(
                                if (tuneExpanded) "Close" else "Tune",
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThoughtMoment(
    state: TodayUiState,
    onReflect: () -> Unit,
    onLibrary: () -> Unit,
    onToggleSave: () -> Unit,
    onMoreLikeThis: () -> Unit,
    onLessLikeThis: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tuneExpanded by rememberSaveable { mutableStateOf(false) }
    val scheme = MaterialTheme.colorScheme
    val cardShape = RoundedCornerShape(KairosRadius.card)

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .height(120.dp)
                .clip(cardShape)
                .background(scheme.surfaceContainerHighest)
                .graphicsLayer { rotationZ = 1.2f }
        )
        KairosGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = cardShape,
            strong = true,
            elevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 380.dp)
                    .padding(horizontal = 26.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(min = 56.dp)
                                .height(2.dp)
                                .clip(CircleShape)
                                .background(scheme.outlineVariant)
                        )
                        KairosGlassSurface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            strong = state.quoteSaved,
                            onClick = onToggleSave
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (state.quoteSaved) KairosIcons.Favorite else KairosIcons.FavoriteBorder,
                                    contentDescription = if (state.quoteSaved) "Remove from saved quotes" else "Save this quote",
                                    tint = if (state.quoteSaved) scheme.primary else scheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "“${state.dailyQuote}”",
                        style = MaterialTheme.typography.headlineLarge.copy(fontFamily = SerifFamily),
                        fontWeight = FontWeight.Normal,
                        lineHeight = MaterialTheme.typography.headlineLarge.lineHeight * 1.15f,
                        color = scheme.onSurface,
                        modifier = Modifier.semantics { heading() }
                    )
                    if (state.dailyQuoteAuthor.isNotBlank()) {
                        Text(
                            text = state.dailyQuoteAuthor,
                            style = MaterialTheme.typography.labelLarge,
                            color = scheme.onSurfaceVariant
                        )
                    }
                    RecommendationReason(state.dailyQuoteReason)
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnimatedVisibility(
                        visible = tuneExpanded,
                        enter = expandVertically(animationSpec = androidx.compose.animation.core.tween(KairosMotion.state)) + fadeIn(),
                        exit = shrinkVertically(animationSpec = androidx.compose.animation.core.tween(KairosMotion.state)) + fadeOut()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            KairosSecondaryButton(
                                text = "More like this",
                                onClick = onMoreLikeThis,
                                modifier = Modifier.weight(1f)
                            )
                            KairosSecondaryButton(
                                text = "Less like this",
                                onClick = onLessLikeThis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    KairosActionRow {
                        KairosPrimaryButton(
                            text = "Reflect",
                            onClick = onReflect,
                            icon = KairosIcons.Outlined.Edit,
                            modifier = Modifier.weight(1f)
                        )
                        KairosSecondaryButton(
                            text = "Library",
                            onClick = onLibrary,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { tuneExpanded = !tuneExpanded },
                            modifier = Modifier.height(54.dp)
                        ) {
                            Text(if (tuneExpanded) "Close" else "Tune")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendationReason(reason: String) {
    if (reason.isBlank()) return
    Text(
        text = "Chosen because $reason",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun TodayLoading() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = KairosSpacing.screen, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(KairosRadius.card))
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            KairosSkeletonList(rows = 5, modifier = Modifier.padding(24.dp))
        }
    }
}

@Composable
private fun TodayError(
    message: String,
    onRetry: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .padding(KairosSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Today is unavailable",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            KairosPrimaryButton(text = "Try again", onClick = onRetry)
        }
    }
}

private fun timeOfDay(): String = when (java.time.LocalTime.now().hour) {
    in 5..11 -> "morning"
    in 12..16 -> "afternoon"
    else -> "evening"
}


private fun progressLine(state: TodayUiState): String {
    val parts = buildList {
        if (state.wordsLearnedThisWeek > 0) add("${state.wordsLearnedThisWeek} words this week")
        if (state.journalEntriesThisWeek > 0) add("${state.journalEntriesThisWeek} reflections")
        if (state.currentStreak > 1) add("${state.currentStreak}-day rhythm")
    }
    return parts.joinToString("  ·  ").ifBlank { "Your weekly rhythm will appear after the first action." }
}

private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE · MMMM d")

@androidx.compose.ui.tooling.preview.Preview(
    name = "Today word — light",
    showBackground = true,
    backgroundColor = 0xFFF5F7FC,
    widthDp = 390,
    heightDp = 760
)
@Composable
private fun WordMomentPreview() {
    com.kairos.app.ui.theme.KairosTheme(themeMode = com.kairos.app.ui.theme.ThemeMode.LIGHT) {
        WordMoment(
            state = TodayUiState(
                wordOfTheDay = "Liminal",
                wordDefinition = "Existing at a threshold; between one state and the next.",
                wordPronunciation = "lim-uh-nuhl",
                wordPartOfSpeech = "adjective",
                wordExampleSentence = "The quiet hour before sunrise felt like a liminal space.",
                wordRecommendationReason = "it matches your recent interest in change and is due at your current level"
            ),
            onToggleSave = {},
            onPractice = {},
            onTooEasy = {},
            onTooHard = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Today thought — dark",
    showBackground = true,
    backgroundColor = 0xFF0B0E15,
    widthDp = 390,
    heightDp = 760
)
@Composable
private fun ThoughtMomentPreview() {
    com.kairos.app.ui.theme.KairosTheme(themeMode = com.kairos.app.ui.theme.ThemeMode.DARK) {
        ThoughtMoment(
            state = TodayUiState(
                dailyQuote = "We do not learn from experience. We learn from reflecting on experience.",
                dailyQuoteAuthor = "John Dewey",
                dailyQuoteReason = "reflection has been part of your recent rhythm"
            ),
            onReflect = {},
            onLibrary = {},
            onToggleSave = {},
            onMoreLikeThis = {},
            onLessLikeThis = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

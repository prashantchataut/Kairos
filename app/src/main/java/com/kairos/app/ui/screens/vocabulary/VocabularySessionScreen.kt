package com.kairos.app.ui.screens.vocabulary

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.ui.components.kairos.KairosEmptyState
import com.kairos.app.ui.components.kairos.KairosGlassSurface
import com.kairos.app.ui.components.kairos.KairosIconButton
import com.kairos.app.ui.components.kairos.KairosPrimaryButton
import com.kairos.app.ui.components.kairos.KairosScreenHeader
import com.kairos.app.ui.components.kairos.KairosSecondaryButton
import com.kairos.app.ui.components.kairos.KairosSkeletonList
import com.kairos.app.ui.icons.KairosIcons
import com.kairos.app.ui.theme.KairosClay
import com.kairos.app.ui.theme.KairosRadius
import com.kairos.app.ui.theme.KairosSeaGlass
import com.kairos.app.ui.theme.KairosSpacing
import com.kairos.app.ui.theme.SerifFamily

/**
 * The hybrid vocabulary learning session — the Kairos equivalent of a dedicated
 * vocabulary trainer. Users recall a word, reveal its meaning, self-grade with
 * the spaced-repetition scale (Again/Hard/Good/Easy), then re-encode weak cards
 * in a definition-matching quiz. Cards sit on flat paper reading surfaces; compact
 * controls; definitions sit on matte reading surfaces for legibility.
 */
@Composable
fun VocabularySessionScreen(
    initialMode: SessionMode = SessionMode.MIXED,
    onNavigateBack: () -> Unit,
    viewModel: VocabularySessionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.startSession(initialMode)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        KairosScreenHeader(
            title = "Practice",
            subtitle = when (state.phase) {
                SessionPhase.FLASHCARD -> "Recall, then reveal"
                SessionPhase.QUIZ -> "Match the meanings"
                SessionPhase.SUMMARY -> "Session complete"
            },
            actions = {
                KairosIconButton(
                    icon = KairosIcons.ArrowBack,
                    contentDescription = "Close practice",
                    onClick = onNavigateBack
                )
            }
        )

        when {
            state.isLoading -> {
                KairosGlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 560.dp)
                        .align(Alignment.CenterHorizontally)
                        .padding(KairosSpacing.screen)
                        .height(420.dp)
                ) {
                    KairosSkeletonList(rows = 4)
                }
            }
            state.error != null -> {
                KairosEmptyState(
                    icon = KairosIcons.ErrorOutline,
                    title = "Practice is unavailable",
                    body = state.error ?: "The vocabulary library could not be loaded.",
                    actionLabel = "Try again",
                    onAction = viewModel::retry,
                    modifier = Modifier.fillMaxSize()
                )
            }
            state.cards.isEmpty() -> {
                KairosEmptyState(
                    icon = KairosIcons.Outlined.School,
                    title = "Nothing in the queue",
                    body = "There are no words due for review or ready to learn. Come back after today's word, or browse the library.",
                    actionLabel = "Close",
                    onAction = onNavigateBack,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                AnimatedContent(
                    targetState = state.phase,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "session-phase"
                ) { phase ->
                    when (phase) {
                        SessionPhase.FLASHCARD -> FlashcardPhase(
                            state = state,
                            onReveal = viewModel::revealCurrent,
                            onGrade = viewModel::gradeCurrent,
                            onToggleSave = viewModel::toggleSaveCurrent
                        )
                        SessionPhase.QUIZ -> QuizPhase(
                            state = state,
                            onAnswer = viewModel::answerQuiz,
                            onNext = viewModel::nextQuizQuestion
                        )
                        SessionPhase.SUMMARY -> SummaryPhase(
                            state = state,
                            onFinish = onNavigateBack,
                            onPracticeAgain = viewModel::retry
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FlashcardPhase(
    state: VocabularySessionUiState,
    onReveal: () -> Unit,
    onGrade: (SessionGrade) -> Unit,
    onToggleSave: () -> Unit = {}
) {
    val card = state.currentCard ?: return
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = KairosSpacing.screen, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        SessionProgress(state = state)

        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .shadow(16.dp, RoundedCornerShape(KairosRadius.card), ambientColor = Color(0x66102A8A), spotColor = Color(0x55102A8A))
                .clip(RoundedCornerShape(KairosRadius.card))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            Color(0xFF1B3FBF),
                            Color(0xFF0F2C8F)
                        )
                    )
                )
                .padding(28.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = card.word.word,
                        style = MaterialTheme.typography.displayMedium.copy(fontFamily = SerifFamily),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .semantics { heading() }
                    )
                    Surface(
                        onClick = onToggleSave,
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (card.word.isFavorite) KairosIcons.Favorite else KairosIcons.FavoriteBorder,
                                contentDescription = if (card.word.isFavorite) "Remove from saved words" else "Save this word",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (card.word.partOfSpeech.isNotBlank()) {
                        PartOfSpeechChip(card.word.partOfSpeech)
                    }
                    if (card.word.pronunciation.isNotBlank()) {
                        Text(
                            text = card.word.pronunciation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.72f)
                        )
                    }
                }

                if (card.revealed) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = card.word.definition,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color.White.copy(alpha = 0.95f)
                    )
                    if (card.word.exampleSentence.isNotBlank()) {
                        Text(
                            text = "“${card.word.exampleSentence}”",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = SerifFamily,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center,
                            color = Color.White.copy(alpha = 0.78f)
                        )
                    }
                    if (card.word.synonyms.isNotBlank()) {
                        Text(
                            text = "Related: ${card.word.synonyms}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!card.revealed) {
            KairosPrimaryButton(
                text = "Reveal meaning",
                onClick = onReveal,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp),
                icon = KairosIcons.Outlined.School
            )
        } else {
            Text(
                text = "How well did you recall it?",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            GradeRow(
                onGrade = onGrade,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
            )
        }
    }
}

@Composable
private fun QuizPhase(
    state: VocabularySessionUiState,
    onAnswer: (QuizOption) -> Unit,
    onNext: () -> Unit
) {
    val card = state.currentCard ?: return
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = KairosSpacing.screen, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Which meaning belongs to “${card.word.word}”?",
            style = MaterialTheme.typography.titleLarge,
            fontFamily = SerifFamily,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.quizOptions.forEachIndexed { index, option ->
                QuizOptionRow(
                    option = option,
                    letter = OPTION_LETTERS.getOrElse(index) { "" },
                    answered = state.quizAnswered,
                    selected = card.quizCorrect == option.isCorrect && state.quizAnswered,
                    onClick = { onAnswer(option) }
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))
        if (state.quizAnswered) {
            val correct = card.quizCorrect == true
            Text(
                text = if (correct) "Correct — that meaning is right." else "Not quite — check the definition again.",
                style = MaterialTheme.typography.bodyMedium,
                color = if (correct) KairosSeaGlass else KairosClay
            )
            Spacer(modifier = Modifier.height(12.dp))
            KairosPrimaryButton(
                text = "Continue",
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
            )
        }
    }
}

@Composable
private fun SummaryPhase(
    state: VocabularySessionUiState,
    onFinish: () -> Unit,
    onPracticeAgain: () -> Unit
) {
    val summary = state.summary ?: return
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = KairosSpacing.screen, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        KairosGlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp),
            shape = RoundedCornerShape(KairosRadius.readingSurface),
            contentPadding = PaddingValues(28.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "${summary.remembered} of ${summary.totalCards}",
                    style = MaterialTheme.typography.displaySmall,
                    fontFamily = SerifFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    text = "words remembered on the first try",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (summary.quizAnswered > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SummaryMetric(
                            value = "${summary.quizCorrect}/${summary.quizAnswered}",
                            label = "Quiz correct",
                            modifier = Modifier.weight(1f)
                        )
                        SummaryMetric(
                            value = formatDuration(summary.secondsSpent),
                            label = "Time",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Text(
                    text = "Words you graded “again” return sooner; the rest are scheduled by your recall.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        KairosPrimaryButton(
            text = "Done",
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        KairosSecondaryButton(
            text = "Practice again",
            onClick = onPracticeAgain,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
        )
    }
}

@Composable
private fun SessionProgress(state: VocabularySessionUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 560.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Card ${state.currentIndex + 1} of ${state.cards.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = state.mode.label(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        LinearProgressIndicator(
            progress = { state.progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        )
    }
}

private val GradeEmoji = mapOf(
    SessionGrade.AGAIN to "😖",
    SessionGrade.HARD to "😅",
    SessionGrade.GOOD to "🙂",
    SessionGrade.EASY to "😎"
)

@Composable
private fun GradeRow(
    onGrade: (SessionGrade) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top
    ) {
        GradeButton(
            label = SessionGrade.AGAIN.label,
            emoji = GradeEmoji.getValue(SessionGrade.AGAIN),
            color = KairosClay,
            onClick = { onGrade(SessionGrade.AGAIN) },
            modifier = Modifier.weight(1f)
        )
        GradeButton(
            label = SessionGrade.HARD.label,
            emoji = GradeEmoji.getValue(SessionGrade.HARD),
            color = Color(0xFFE8960C),
            onClick = { onGrade(SessionGrade.HARD) },
            modifier = Modifier.weight(1f)
        )
        GradeButton(
            label = SessionGrade.GOOD.label,
            emoji = GradeEmoji.getValue(SessionGrade.GOOD),
            color = MaterialTheme.colorScheme.tertiary,
            onClick = { onGrade(SessionGrade.GOOD) },
            modifier = Modifier.weight(1f)
        )
        GradeButton(
            label = SessionGrade.EASY.label,
            emoji = GradeEmoji.getValue(SessionGrade.EASY),
            color = KairosSeaGlass,
            onClick = { onGrade(SessionGrade.EASY) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun GradeButton(
    label: String,
    emoji: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(58.dp),
            shape = CircleShape,
            color = color.copy(alpha = 0.14f),
            border = BorderStroke(2.dp, color.copy(alpha = 0.55f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun QuizOptionRow(
    option: QuizOption,
    letter: String,
    answered: Boolean,
    selected: Boolean,
    onClick: () -> Unit
) {
    val background = when {
        !answered -> MaterialTheme.colorScheme.surface.copy(alpha = 0.86f)
        option.isCorrect -> KairosSeaGlass.copy(alpha = 0.16f)
        selected && !option.isCorrect -> KairosClay.copy(alpha = 0.16f)
        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
    }
    val border = when {
        !answered -> MaterialTheme.colorScheme.outlineVariant
        option.isCorrect -> KairosSeaGlass.copy(alpha = 0.7f)
        selected && !option.isCorrect -> KairosClay.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    Surface(
        onClick = onClick,
        enabled = !answered,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(KairosRadius.controlLarge),
        color = background,
        border = BorderStroke(1.dp, border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Box(
                        modifier = Modifier.size(26.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (answered) {
                            Icon(
                                imageVector = if (option.isCorrect) KairosIcons.CheckCircle else KairosIcons.Close,
                                contentDescription = null,
                                tint = if (option.isCorrect) KairosSeaGlass else KairosClay,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = letter,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            Text(
                text = option.definition,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PartOfSpeechChip(text: String) {
    Surface(
        shape = RoundedCornerShape(KairosRadius.control),
        color = Color.White.copy(alpha = 0.14f),
        contentColor = Color.White
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun SummaryMetric(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(KairosRadius.controlLarge),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private val OPTION_LETTERS = listOf("A", "B", "C", "D")

private fun SessionMode.label(): String = when (this) {
    SessionMode.REVIEW_DUE -> "Review"
    SessionMode.NEW_WORDS -> "New words"
    SessionMode.MIXED -> "Mixed session"
}

private fun formatDuration(seconds: Long): String {
    val minutes = seconds / 60
    val remainder = seconds % 60
    return if (minutes > 0) "${minutes}m ${remainder}s" else "${remainder}s"
}

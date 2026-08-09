package com.kairos.app.ui.screens.onboarding

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.ui.animation.KairosDurations
import com.kairos.app.ui.animation.KairosEasing
import com.kairos.app.ui.animation.kairosScale
import com.kairos.app.ui.animation.rememberKairosPressScale
import com.kairos.app.ui.animation.rememberKairosReducedMotion
import com.kairos.app.ui.components.kairos.KairosMark
import com.kairos.app.ui.components.kairos.KairosWordmark
import com.kairos.app.ui.theme.KairosPeriwinkle
import com.kairos.app.ui.theme.KairosSeaGlass
import com.kairos.app.ui.theme.KairosTheme
import com.kairos.app.ui.theme.ThemeMode
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

private const val OnboardingPageCount = 4

private data class WisdomCategory(val key: String, val label: String)

private val wisdomCategories = listOf(
    WisdomCategory("wisdom", "Wisdom"),
    WisdomCategory("life", "Life"),
    WisdomCategory("motivation", "Momentum"),
    WisdomCategory("creativity", "Creativity"),
    WisdomCategory("communication", "Communication"),
    WisdomCategory("growth", "Growth")
)

/** Word interests align with the categories used across the expanded vocabulary catalog. */
private val wordInterestCategories = listOf(
    WisdomCategory("self-improvement", "Self-improvement"),
    WisdomCategory("communication", "Communication"),
    WisdomCategory("emotion", "Emotion"),
    WisdomCategory("mindfulness", "Mindfulness"),
    WisdomCategory("learning", "Learning"),
    WisdomCategory("reflection", "Reflection"),
    WisdomCategory("business", "Business"),
    WisdomCategory("academic", "Academic")
)

/** Onboarding night tokens — charcoal ink-navy with the Moment Blue brand. */
private val OnboardingInk = Color(0xFF0B0E15)
private val OnboardingPaper = Color(0xFFE9EDF6)
private val OnboardingMuted = Color(0xFF9AA5BE)
private val OnboardingPanel = Color(0xFF151B28)
private val OnboardingPanelHairline = Color(0xFF2A3345)
private val OnboardingBlue = Color(0xFF2E5BFF)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val completionState by viewModel.completionState.collectAsStateWithLifecycle()
    OnboardingContent(
        completionState = completionState,
        onComplete = onComplete,
        onSubmit = viewModel::completeOnboarding,
        onClearError = viewModel::clearError
    )
}

@Composable
private fun OnboardingContent(
    completionState: OnboardingCompletionState,
    onComplete: () -> Unit,
    onSubmit: (Int, Set<String>, Set<String>, Int) -> Unit,
    onClearError: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { OnboardingPageCount })
    val scope = rememberCoroutineScope()
    var difficulty by rememberSaveable { mutableIntStateOf(3) }
    var sessionSize by rememberSaveable { mutableIntStateOf(5) }
    var selectedCategoryKeys by rememberSaveable { mutableStateOf("wisdom,life,motivation") }
    var selectedWordCategoryKeys by rememberSaveable {
        mutableStateOf("self-improvement,communication,mindfulness,reflection")
    }
    val selectedCategories = remember(selectedCategoryKeys) {
        selectedCategoryKeys.split(',').filter(String::isNotBlank).toSet()
    }
    val selectedWordCategories = remember(selectedWordCategoryKeys) {
        selectedWordCategoryKeys.split(',').filter(String::isNotBlank).toSet()
    }
    val saving = completionState is OnboardingCompletionState.Saving

    KeepOnboardingSystemBarsDark()

    LaunchedEffect(completionState) {
        if (completionState is OnboardingCompletionState.Completed) onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OnboardingInk)
    ) {
        OnboardingGlowField(Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            OnboardingTopBar(
                currentPage = pagerState.currentPage,
                enabled = !saving,
                onSkip = {
                    onSubmit(difficulty, selectedCategories, selectedWordCategories, sessionSize)
                }
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = !saving,
                beyondViewportPageCount = 1
            ) { page ->
                val offset = (
                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                ).absoluteValue.coerceIn(0f, 1f)
                val pageModifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = 1f - (offset * 0.30f)
                        scaleX = 1f - (offset * 0.035f)
                        scaleY = 1f - (offset * 0.035f)
                        translationX = offset * 20.dp.toPx()
                    }

                when (page) {
                    0 -> IntroPage(pageModifier)
                    1 -> QuoteInterestsPage(
                        modifier = pageModifier,
                        selectedCategories = selectedCategories,
                        onCategoryToggle = { key ->
                            val updated = if (key in selectedCategories) {
                                if (selectedCategories.size > 1) selectedCategories - key else selectedCategories
                            } else {
                                selectedCategories + key
                            }
                            selectedCategoryKeys = updated.sorted().joinToString(",")
                        }
                    )
                    2 -> WordInterestsPage(
                        modifier = pageModifier,
                        selectedWordCategories = selectedWordCategories,
                        onWordCategoryToggle = { key ->
                            val updated = if (key in selectedWordCategories) {
                                if (selectedWordCategories.size > 1) selectedWordCategories - key else selectedWordCategories
                            } else {
                                selectedWordCategories + key
                            }
                            selectedWordCategoryKeys = updated.sorted().joinToString(",")
                        },
                        difficulty = difficulty,
                        onDifficultyChange = { difficulty = it },
                        sessionSize = sessionSize,
                        onSessionSizeChange = { sessionSize = it }
                    )
                    else -> ReadyPage(
                        modifier = pageModifier,
                        completionState = completionState,
                        onRetry = {
                            onSubmit(difficulty, selectedCategories, selectedWordCategories, sessionSize)
                        }
                    )
                }
            }

            OnboardingActions(
                currentPage = pagerState.currentPage,
                pageProgress = pagerState.currentPage + pagerState.currentPageOffsetFraction,
                isSaving = saving,
                onNext = {
                    onClearError()
                    scope.launch {
                        pagerState.animateScrollToPage(
                            page = (pagerState.currentPage + 1).coerceAtMost(OnboardingPageCount - 1),
                            animationSpec = tween(KairosDurations.Page, easing = KairosEasing.EaseOutExpo)
                        )
                    }
                },
                onFinish = {
                    onSubmit(difficulty, selectedCategories, selectedWordCategories, sessionSize)
                }
            )
        }
    }
}

@Composable
private fun OnboardingTopBar(
    currentPage: Int,
    enabled: Boolean,
    onSkip: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        KairosWordmark(
            tint = OnboardingPaper,
            accent = OnboardingBlue
        )
        Text(
            text = if (currentPage == OnboardingPageCount - 1) "" else "Skip",
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .clickable(enabled = enabled && currentPage < OnboardingPageCount - 1, onClick = onSkip)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            color = KairosPeriwinkle,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Page 0 — the poster. One moment, the logo breathing, one sentence.
 */
@Composable
private fun IntroPage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        KairosMark(
            modifier = Modifier.size(140.dp),
            tint = Color.White,
            accent = KairosPeriwinkle
        )
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = "Make space for\nwhat matters.",
            style = MaterialTheme.typography.displayLarge,
            color = OnboardingPaper,
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.displayLarge.lineHeight * 1.04f
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "A daily word, a thought worth keeping, and a quiet place to reflect.",
            style = MaterialTheme.typography.bodyLarge,
            color = OnboardingMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 320.dp)
        )
        Spacer(modifier = Modifier.height(28.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = OnboardingMuted,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Works fully offline. No account needed.",
                style = MaterialTheme.typography.bodySmall,
                color = OnboardingMuted
            )
        }
    }
}

/**
 * Pages 1-2 — full-screen questions with big tactile chips.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuoteInterestsPage(
    modifier: Modifier = Modifier,
    selectedCategories: Set<String>,
    onCategoryToggle: (String) -> Unit
) {
    OnboardingQuestionPage(
        modifier = modifier,
        title = "Understand\nyourself.",
        body = "Choose the ideas you want more of in your daily moment."
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            wisdomCategories.forEach { category ->
                InterestChip(
                    text = category.label,
                    selected = category.key in selectedCategories,
                    onClick = { onCategoryToggle(category.key) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WordInterestsPage(
    modifier: Modifier = Modifier,
    selectedWordCategories: Set<String>,
    onWordCategoryToggle: (String) -> Unit,
    difficulty: Int,
    onDifficultyChange: (Int) -> Unit,
    sessionSize: Int,
    onSessionSizeChange: (Int) -> Unit
) {
    OnboardingQuestionPage(
        modifier = modifier,
        title = "Learn what\nchanges you.",
        body = "Words from these areas surface first. Pace and session size tune the daily loop."
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                wordInterestCategories.forEach { category ->
                    InterestChip(
                        text = category.label,
                        selected = category.key in selectedWordCategories,
                        onClick = { onWordCategoryToggle(category.key) }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pace", style = MaterialTheme.typography.labelLarge, color = OnboardingMuted)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(2 to "Gentle", 3 to "Balanced", 4 to "Stretch").forEach { (value, label) ->
                            SelectorPill(
                                label = label,
                                selected = difficulty == value,
                                onClick = { onDifficultyChange(value) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Session size", style = MaterialTheme.typography.labelLarge, color = OnboardingMuted)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(3, 5, 10).forEach { value ->
                            SelectorPill(
                                label = "$value",
                                selected = sessionSize == value,
                                onClick = { onSessionSizeChange(value) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingQuestionPage(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.displayMedium,
                color = OnboardingPaper,
                lineHeight = MaterialTheme.typography.displayMedium.lineHeight * 1.05f
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = OnboardingMuted,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.12f,
                modifier = Modifier.widthIn(max = 360.dp)
            )
        }
        content()
        Spacer(modifier = Modifier.height(8.dp))
    }
}

/**
 * Page 3 — "Building your Kairos": destinations plus a live build state.
 */
@Composable
private fun ReadyPage(
    modifier: Modifier = Modifier,
    completionState: OnboardingCompletionState,
    onRetry: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BuildingMoment(isSaving = completionState is OnboardingCompletionState.Saving)

        Spacer(modifier = Modifier.height(26.dp))

        Text(
            text = "Turn insight into\naction.",
            style = MaterialTheme.typography.displayMedium,
            color = OnboardingPaper,
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.displayMedium.lineHeight * 1.05f
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Today, Learn, Reflect, and Library — the whole core, built around you.",
            style = MaterialTheme.typography.bodyLarge,
            color = OnboardingMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 340.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        OnboardingGlass(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DestinationRow(Icons.Outlined.AutoStories, "Today", "One word and one thought")
                DestinationRow(Icons.Outlined.School, "Learn", "Review at the right time")
                DestinationRow(Icons.Outlined.EditNote, "Reflect", "Turn ideas into your own words")
            }
        }

        AnimatedVisibility(
            visible = completionState is OnboardingCompletionState.Error,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            OnboardingGlass(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = (completionState as? OnboardingCompletionState.Error)?.message.orEmpty(),
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFFF8A80),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Surface(onClick = onRetry, shape = CircleShape, color = Color.White.copy(alpha = 0.10f)) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Retry setup", tint = OnboardingPaper, modifier = Modifier.padding(10.dp))
                    }
                }
            }
        }
    }
}

/**
 * Animated "building your Kairos" moment: the mark breathes inside a ring
 * that sweeps progress while saving.
 */
@Composable
private fun BuildingMoment(isSaving: Boolean) {
    val reducedMotion = rememberKairosReducedMotion()
    val transition = rememberInfiniteTransition(label = "building-ring")
    val sweep by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "building-ring-sweep"
    )
    val progress = if (reducedMotion) 0.6f else sweep

    Box(modifier = Modifier.size(150.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .drawBehind {
                    drawArc(
                        color = OnboardingPanelHairline,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5.dp.toPx())
                    )
                }
        )
        Box(
            modifier = Modifier
                .size(150.dp)
                .drawBehind {
                    drawArc(
                        color = if (isSaving) KairosPeriwinkle else OnboardingBlue,
                        startAngle = -90f,
                        sweepAngle = progress * 360f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 5.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )
                }
        )
        KairosMark(
            modifier = Modifier.size(84.dp),
            tint = Color.White,
            accent = if (isSaving) KairosPeriwinkle else OnboardingBlue
        )
    }
}

@Composable
private fun OnboardingActions(
    currentPage: Int,
    pageProgress: Float,
    isSaving: Boolean,
    onNext: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(OnboardingPageCount) { index ->
                val fillFraction by animateFloatAsState(
                    targetValue = (pageProgress - index).coerceIn(0f, 1f),
                    animationSpec = tween(KairosDurations.State, easing = KairosEasing.EaseOutExpo),
                    label = "onboarding-progress-segment"
                )
                Box(
                    Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(OnboardingPaper.copy(alpha = 0.18f))
                        .semantics { contentDescription = "Step ${index + 1} of $OnboardingPageCount" }
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(fillFraction)
                            .fillMaxHeight()
                            .background(KairosPeriwinkle)
                    )
                }
            }
        }

        val ctaInteractionSource = remember { MutableInteractionSource() }
        val ctaPressScale = rememberKairosPressScale(interactionSource = ctaInteractionSource)
        val ctaColor by animateColorAsState(
            targetValue = if (currentPage == OnboardingPageCount - 1) KairosPeriwinkle else OnboardingBlue,
            animationSpec = tween(KairosDurations.State),
            label = "onboarding-cta-color"
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp)
                .height(58.dp)
                .kairosScale(ctaPressScale)
                .alpha(if (isSaving) 0.72f else 1f)
                .clip(RoundedCornerShape(20.dp))
                .background(ctaColor)
                .clickable(
                    interactionSource = ctaInteractionSource,
                    indication = androidx.compose.foundation.LocalIndication.current,
                    enabled = !isSaving,
                    onClick = if (currentPage == OnboardingPageCount - 1) onFinish else onNext
                ),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = isSaving,
                transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(120)) },
                label = "onboarding-action"
            ) { saving ->
                if (saving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp, color = Color.White)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (currentPage == OnboardingPageCount - 1) "Begin with Kairos" else "Continue",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                        Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * Frosted glass panel on night paper.
 */
@Composable
private fun OnboardingGlass(
    modifier: Modifier = Modifier,
    strong: Boolean = false,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(26.dp)
    Box(
        modifier = modifier
            .shadow(12.dp, shape, ambientColor = Color(0x59000000), spotColor = Color(0x59000000))
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        if (strong) Color(0xFF20283A) else OnboardingPanel.copy(alpha = 0.94f),
                        if (strong) Color(0xFF151B28) else OnboardingPanel.copy(alpha = 0.78f)
                    )
                )
            )
            .border(
                BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.18f), OnboardingPanelHairline)
                    )
                ),
                shape
            )
    ) {
        Box(
            Modifier
                .matchParentSize()
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.05f), Color.Transparent),
                        startY = 0f,
                        endY = 180f
                    )
                )
        )
        content()
    }
}

@Composable
private fun SelectorPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container by animateColorAsState(
        targetValue = if (selected) OnboardingBlue else Color.White.copy(alpha = 0.06f),
        animationSpec = tween(KairosDurations.State),
        label = "selector-container"
    )
    Surface(
        onClick = onClick,
        modifier = modifier.height(46.dp),
        shape = RoundedCornerShape(16.dp),
        color = container,
        contentColor = if (selected) Color.White else OnboardingMuted,
        border = BorderStroke(1.dp, if (selected) OnboardingBlue else OnboardingPanelHairline)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun InterestChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val container by animateColorAsState(
        targetValue = if (selected) OnboardingBlue else Color.White.copy(alpha = 0.06f),
        animationSpec = tween(KairosDurations.State),
        label = "chip-container"
    )
    val content by animateColorAsState(
        targetValue = if (selected) Color.White else OnboardingMuted,
        animationSpec = tween(KairosDurations.State),
        label = "chip-content"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) KairosPeriwinkle.copy(alpha = 0.9f) else OnboardingPanelHairline,
        animationSpec = tween(KairosDurations.State),
        label = "chip-border"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.04f else 1f,
        animationSpec = tween(KairosDurations.State, easing = KairosEasing.EaseOutQuart),
        label = "chip-scale"
    )
    Surface(
        onClick = onClick,
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        shape = RoundedCornerShape(18.dp),
        color = container,
        contentColor = content,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedVisibility(
                visible = selected,
                enter = scaleIn(initialScale = 0.4f) + fadeIn(tween(140))
            ) {
                Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun DestinationRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Surface(shape = RoundedCornerShape(14.dp), color = Color.White.copy(alpha = 0.08f)) {
            Icon(icon, contentDescription = null, tint = OnboardingPaper, modifier = Modifier.padding(10.dp).size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = OnboardingPaper, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = OnboardingMuted)
        }
    }
}

/**
 * Atmospheric night glow — slow-breathing blue and cyan fields.
 */
@Composable
private fun OnboardingGlowField(modifier: Modifier = Modifier) {
    val reducedMotion = rememberKairosReducedMotion()
    val transition = rememberInfiniteTransition(label = "onboarding-glow")
    val pulse by transition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.11f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "onboarding-glow-alpha"
    )
    val alpha = if (reducedMotion) 0.08f else pulse
    Box(modifier) {
        Box(
            Modifier
                .size(520.dp)
                .align(Alignment.TopCenter)
                .graphicsLayer { translationY = -180f }
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(KairosPeriwinkle.copy(alpha = alpha), Color.Transparent),
                            center = center,
                            radius = size.minDimension * 0.5f
                        ),
                        radius = size.minDimension * 0.5f
                    )
                }
        )
        Box(
            Modifier
                .size(420.dp)
                .align(Alignment.BottomStart)
                .graphicsLayer { translationX = -180f; translationY = 140f }
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(KairosSeaGlass.copy(alpha = alpha * 0.5f), Color.Transparent),
                            center = center,
                            radius = size.minDimension * 0.5f
                        ),
                        radius = size.minDimension * 0.5f
                    )
                }
        )
        Box(
            Modifier
                .size(380.dp)
                .align(Alignment.BottomEnd)
                .graphicsLayer { translationX = 160f; translationY = 60f }
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(OnboardingBlue.copy(alpha = alpha * 0.7f), Color.Transparent),
                            center = center,
                            radius = size.minDimension * 0.5f
                        ),
                        radius = size.minDimension * 0.5f
                    )
                }
        )
    }
}

@Composable
private fun KeepOnboardingSystemBarsDark() {
    val view = LocalView.current
    if (view.isInEditMode) return
    val activity = view.context as? Activity ?: return
    DisposableEffect(view) {
        val controller = WindowCompat.getInsetsController(activity.window, view)
        val oldStatus = controller.isAppearanceLightStatusBars
        val oldNavigation = controller.isAppearanceLightNavigationBars
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false
        onDispose {
            controller.isAppearanceLightStatusBars = oldStatus
            controller.isAppearanceLightNavigationBars = oldNavigation
        }
    }
}

@Preview(name = "Kairos onboarding", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun OnboardingPreview() {
    KairosTheme(themeMode = ThemeMode.DARK) {
        OnboardingContent(
            completionState = OnboardingCompletionState.Idle,
            onComplete = {},
            onSubmit = { _, _, _, _ -> },
            onClearError = {}
        )
    }
}

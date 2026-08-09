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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Work
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
import androidx.compose.ui.graphics.vector.ImageVector
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

private const val OnboardingPageCount = 5

private data class WisdomCategory(val key: String, val label: String, val icon: ImageVector)

private val wisdomCategories = listOf(
    WisdomCategory("wisdom", "Wisdom", Icons.Outlined.AutoStories),
    WisdomCategory("life", "Life", Icons.Outlined.Public),
    WisdomCategory("motivation", "Momentum", Icons.Outlined.LocalFireDepartment),
    WisdomCategory("creativity", "Creativity", Icons.Outlined.Lightbulb),
    WisdomCategory("communication", "Communication", Icons.Outlined.EditNote),
    WisdomCategory("growth", "Growth", Icons.Outlined.TrendingUp)
)

/** Word interests align with the categories used across the expanded vocabulary catalog. */
private val wordInterestCategories = listOf(
    WisdomCategory("self-improvement", "Self-improvement", Icons.Outlined.TrendingUp),
    WisdomCategory("communication", "Communication", Icons.Outlined.EditNote),
    WisdomCategory("emotion", "Emotion", Icons.Outlined.Favorite),
    WisdomCategory("mindfulness", "Mindfulness", Icons.Outlined.Psychology),
    WisdomCategory("learning", "Learning", Icons.Outlined.School),
    WisdomCategory("reflection", "Reflection", Icons.Outlined.Lightbulb),
    WisdomCategory("business", "Business", Icons.Outlined.Work),
    WisdomCategory("academic", "Academic", Icons.Outlined.MenuBook)
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
                    1 -> ReflectionStoryPage(
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
                    2 -> LearningStoryPage(
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
                    3 -> JourneyPage(pageModifier)
                    else -> RevealPage(
                        modifier = pageModifier,
                        difficulty = difficulty,
                        sessionSize = sessionSize,
                        selectedCategories = selectedCategories,
                        selectedWordCategories = selectedWordCategories,
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
 * Page 1 — poster. The mark above a headline, with two layered product
 * fragments floating beside it so the page reads as a product moment rather
 * than an empty splash.
 */
@Composable
private fun IntroPage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            KairosMark(
                modifier = Modifier.size(120.dp),
                tint = Color.White,
                accent = OnboardingBlue
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Make space for\nwhat matters.",
                style = MaterialTheme.typography.displayLarge,
                color = OnboardingPaper,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.displayLarge.lineHeight * 1.04f
            )
            Text(
                text = "A daily word, a thought worth keeping, and a quiet place to reflect.",
                style = MaterialTheme.typography.bodyLarge,
                color = OnboardingMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 320.dp)
            )
        }

        // Floating layered fragments — the product peeking around the poster.
        FloatingFragment(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 6.dp, y = 40.dp),
            rotation = -6f
        ) {
            Text(
                text = "lucid",
                style = MaterialTheme.typography.titleLarge,
                color = OnboardingPaper,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "clear · easy to understand",
                style = MaterialTheme.typography.labelSmall,
                color = OnboardingMuted,
                maxLines = 1
            )
        }
        FloatingFragment(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 10.dp, y = -30.dp),
            rotation = 5f
        ) {
            Text(
                text = "“Attention is the beginning of devotion.”",
                style = MaterialTheme.typography.labelMedium,
                color = OnboardingPaper,
                maxLines = 2
            )
        }
    }
}

/**
 * Page 2 — understand yourself. A layered reflection visual, then the ideas
 * the user wants more of as tactile cards.
 */
@Composable
private fun ReflectionStoryPage(
    modifier: Modifier = Modifier,
    selectedCategories: Set<String>,
    onCategoryToggle: (String) -> Unit
) {
    OnboardingStoryPage(
        modifier = modifier,
        title = "Understand\nyourself.",
        body = "A short reflection each day turns passing moments into patterns."
    ) {
        // Layered journal fragments.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            FloatingFragment(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = 4.dp),
                rotation = -5f
            ) {
                Text("“I slowed down today.”", style = MaterialTheme.typography.labelMedium, color = OnboardingMuted, maxLines = 1)
            }
            FloatingFragment(
                modifier = Modifier.align(Alignment.CenterEnd),
                rotation = 4f,
                strong = true
            ) {
                Text("“What felt different?”", style = MaterialTheme.typography.labelMedium, color = OnboardingPaper, maxLines = 1)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Ideas you want more of",
            style = MaterialTheme.typography.labelLarge,
            color = OnboardingMuted
        )
        InterestGrid(
            options = wisdomCategories,
            selected = selectedCategories,
            onToggle = onCategoryToggle
        )
    }
}

/**
 * Page 3 — learn what changes you. A flashcard fragment, then learning areas
 * and pace/session controls (labels never wrap).
 */
@Composable
private fun LearningStoryPage(
    modifier: Modifier = Modifier,
    selectedWordCategories: Set<String>,
    onWordCategoryToggle: (String) -> Unit,
    difficulty: Int,
    onDifficultyChange: (Int) -> Unit,
    sessionSize: Int,
    onSessionSizeChange: (Int) -> Unit
) {
    OnboardingStoryPage(
        modifier = modifier,
        title = "Learn what\nchanges you.",
        body = "Words from your chosen areas surface first, at a pace that fits."
    ) {
        // Flashcard fragment.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp)
                .padding(horizontal = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .shadow(14.dp, RoundedCornerShape(22.dp), ambientColor = Color(0x66102A8A), spotColor = Color(0x55102A8A))
                    .clip(RoundedCornerShape(22.dp))
                    .background(OnboardingBlue)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "serendipity",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "a happy accident, found by chance",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 1
                        )
                    }
                    Text(
                        text = "3 / 5",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            // Stacked card behind.
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .offset(y = 8.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF1B2F6E))
                    .graphicsLayer { rotationZ = -1.5f }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Learning areas",
            style = MaterialTheme.typography.labelLarge,
            color = OnboardingMuted
        )
        InterestGrid(
            options = wordInterestCategories,
            selected = selectedWordCategories,
            onToggle = onWordCategoryToggle
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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

/**
 * Page 4 — turn insight into action. A journey path, not a bordered row list.
 */
@Composable
private fun JourneyPage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Turn insight into\naction.",
            style = MaterialTheme.typography.displayMedium,
            color = OnboardingPaper,
            lineHeight = MaterialTheme.typography.displayMedium.lineHeight * 1.05f
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "One quiet loop keeps everything connected.",
            style = MaterialTheme.typography.bodyLarge,
            color = OnboardingMuted
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Vertical path with nodes.
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            JourneyNode(
                icon = Icons.Outlined.AutoStories,
                title = "Today",
                subtitle = "One word and one thought, every day",
                accent = OnboardingBlue
            )
            JourneyNode(
                icon = Icons.Outlined.School,
                title = "Learn",
                subtitle = "Review at the right moment",
                accent = Color(0xFF5B7FFF)
            )
            JourneyNode(
                icon = Icons.Outlined.EditNote,
                title = "Reflect",
                subtitle = "Turn ideas into your own words",
                accent = Color(0xFF8FA6FF)
            )
            JourneyNode(
                icon = Icons.Outlined.Favorite,
                title = "Library",
                subtitle = "Keep what is worth returning to",
                accent = Color(0xFF7BD9A5)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun JourneyNode(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .shadow(8.dp, RoundedCornerShape(18.dp), ambientColor = Color(0x40000000), spotColor = Color(0x40000000))
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(accent.copy(alpha = 0.90f), accent.copy(alpha = 0.65f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = OnboardingPaper, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = OnboardingMuted)
        }
    }
}

/**
 * Page 5 — personalized reveal. "Your Kairos is ready" with the user's actual
 * choices previewed, then a single CTA.
 */
@Composable
private fun RevealPage(
    modifier: Modifier = Modifier,
    difficulty: Int,
    sessionSize: Int,
    selectedCategories: Set<String>,
    selectedWordCategories: Set<String>,
    completionState: OnboardingCompletionState,
    onRetry: () -> Unit
) {
    val paceLabel = when (difficulty) {
        2 -> "a gentle pace"
        4 -> "a stretching pace"
        else -> "a balanced pace"
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BuildingMoment(isSaving = completionState is OnboardingCompletionState.Saving)
        Text(
            text = "Your Kairos is ready.",
            style = MaterialTheme.typography.displayMedium,
            color = OnboardingPaper,
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.displayMedium.lineHeight * 1.05f
        )
        OnboardingGlass(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RevealRow(Icons.Outlined.AutoStories, "Daily word at $paceLabel")
                RevealRow(Icons.Outlined.School, "$sessionSize-word practice sessions")
                RevealRow(
                    Icons.Outlined.Favorite,
                    "${selectedCategories.size} idea themes · ${selectedWordCategories.size} learning areas"
                )
            }
        }
        Text(
            text = "Today, Learn, Reflect, and Library are the whole core. Everything else stays secondary until it earns a place.",
            style = MaterialTheme.typography.bodySmall,
            color = OnboardingMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 340.dp)
        )

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

@Composable
private fun RevealRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = KairosPeriwinkle, modifier = Modifier.size(18.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = OnboardingPaper)
    }
}

/**
 * Shared story-page scaffold: headline, body, then page content.
 */
@Composable
private fun OnboardingStoryPage(
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
 * Tactile interest cards — a grid of icon + label objects, not a chip pile.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InterestGrid(
    options: List<WisdomCategory>,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        options.forEach { category ->
            InterestCard(
                label = category.label,
                icon = category.icon,
                selected = category.key in selected,
                onClick = { onToggle(category.key) },
                modifier = Modifier.widthIn(max = 168.dp)
            )
        }
    }
}

@Composable
private fun InterestCard(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container by animateColorAsState(
        targetValue = if (selected) OnboardingBlue else Color.White.copy(alpha = 0.06f),
        animationSpec = tween(KairosDurations.State),
        label = "interest-container"
    )
    val content by animateColorAsState(
        targetValue = if (selected) Color.White else OnboardingMuted,
        animationSpec = tween(KairosDurations.State),
        label = "interest-content"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) KairosPeriwinkle.copy(alpha = 0.9f) else OnboardingPanelHairline,
        animationSpec = tween(KairosDurations.State),
        label = "interest-border"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.03f else 1f,
        animationSpec = tween(KairosDurations.State, easing = KairosEasing.EaseOutQuart),
        label = "interest-scale"
    )
    Surface(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        shape = RoundedCornerShape(20.dp),
        color = container,
        contentColor = content,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(19.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

/**
 * A floating glass fragment with gentle drift (reduced-motion aware).
 */
@Composable
private fun FloatingFragment(
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
    strong: Boolean = false,
    content: @Composable () -> Unit
) {
    val reducedMotion = rememberKairosReducedMotion()
    val transition = rememberInfiniteTransition(label = "fragment-float")
    val drift by transition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fragment-drift"
    )
    val dy = if (reducedMotion) 0f else drift

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationZ = rotation
                translationY = dy
            }
            .shadow(10.dp, RoundedCornerShape(18.dp), ambientColor = Color(0x55000000), spotColor = Color(0x55000000))
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (strong) Color(0xFF20283A) else OnboardingPanel.copy(alpha = 0.92f)
            )
            .border(1.dp, OnboardingPanelHairline, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        content = { content() }
    )
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
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(14.dp),
        color = container,
        contentColor = if (selected) Color.White else OnboardingMuted,
        border = BorderStroke(1.dp, if (selected) OnboardingBlue else OnboardingPanelHairline)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

/**
 * Animated "building your Kairos" ring.
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

    Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(140.dp)
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
                .size(140.dp)
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
            modifier = Modifier.size(80.dp),
            tint = Color.White,
            accent = if (isSaving) KairosPeriwinkle else OnboardingBlue
        )
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

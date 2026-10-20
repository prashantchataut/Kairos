package com.kairos.app.ui

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kairos.app.AppContainer
import com.kairos.app.core.design.KairosTheme
import com.kairos.app.core.design.KairosType
import com.kairos.app.core.design.MarkTile
import com.kairos.app.core.design.WatercolorBackdrop
import com.kairos.app.data.WordMemory
import com.kairos.app.domain.share.PosterSharing
import com.kairos.app.ui.capsule.CapsuleComposerScreen
import com.kairos.app.ui.capsule.CapsuleDetailScreen
import com.kairos.app.ui.capsule.CapsuleViewModel
import com.kairos.app.ui.capsule.CapsulesScreen
import com.kairos.app.core.design.EmptyState
import com.kairos.app.ui.onboarding.OnboardingScreen
import com.kairos.app.ui.onboarding.OnboardingViewModel
import com.kairos.app.ui.studio.StudioScreen
import com.kairos.app.ui.studio.StudioViewModel
import com.kairos.app.ui.today.TodayScreen
import com.kairos.app.ui.today.TodayViewModel
import com.kairos.app.ui.words.ReviewScreen
import com.kairos.app.ui.words.WordDetailScreen
import com.kairos.app.ui.words.WordsScreen
import com.kairos.app.ui.words.WordsViewModel
import com.kairos.app.ui.you.YouScreen
import com.kairos.app.ui.you.YouViewModel
import kotlinx.coroutines.delay

private val topLevelRoutes = setOf("today", "words", "capsules", "studio", "you")

@Composable
fun KairosRoot(container: AppContainer) {
    val context = LocalContext.current
    val factory = remember { KairosViewModelFactory(container, context.applicationContext) }
    val appState by container.repository.state.collectAsStateWithLifecycle()
    val profile = appState.profile
    val systemDark = isSystemInDarkTheme()
    val dark = when (profile.themeMode) {
        "light" -> false
        "dark" -> true
        else -> systemDark
    }

    KairosTheme(
        darkTheme = dark,
        reduceMotion = profile.reduceMotion,
        accentName = profile.accent,
    ) {
        val view = LocalView.current
        SideEffect {
            val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            WatercolorBackdrop {
                if (!profile.onboarded) {
                    val onboarding: OnboardingViewModel = viewModel(factory = factory)
                    val state by onboarding.state.collectAsStateWithLifecycle()
                    Box(Modifier.fillMaxSize().statusBarsPadding()) {
                        OnboardingScreen(
                            state = state,
                            onName = onboarding::setName,
                            onToggleInterest = onboarding::toggleInterest,
                            onToggleQuoteTheme = onboarding::toggleQuoteTheme,
                            onPace = onboarding::setPace,
                            onNext = onboarding::next,
                            onBack = onboarding::back,
                            onFinish = onboarding::finish,
                        )
                    }
                } else {
                    KairosNavHost(container = container, factory = factory)
                }
            }
            SplashOverlay()
        }
    }
}

@Composable
private fun SplashOverlay() {
    var visible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(1250)
        visible = false
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(120)),
        exit = fadeOut(tween(520)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(KairosTheme.colors.paper),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                MarkTile(size = 96.dp, animated = true)
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "Kairos",
                    style = MaterialTheme.typography.displayMedium,
                    color = KairosTheme.colors.ink,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "a small moment, kept",
                    style = KairosType.handSmall,
                    color = KairosTheme.colors.inkSoft,
                )
            }
        }
    }
}

@Composable
private fun KairosNavHost(container: AppContainer, factory: KairosViewModelFactory) {
    val nav = rememberNavController()
    val today: TodayViewModel = viewModel(factory = factory)
    val words: WordsViewModel = viewModel(factory = factory)
    val capsules: CapsuleViewModel = viewModel(factory = factory)
    val studio: StudioViewModel = viewModel(factory = factory)
    val you: YouViewModel = viewModel(factory = factory)

    val appState by container.repository.state.collectAsStateWithLifecycle()
    val todayState by today.state.collectAsStateWithLifecycle()
    val wordsState by words.state.collectAsStateWithLifecycle()
    val reviewState by words.session.collectAsStateWithLifecycle()
    val capsulesState by capsules.state.collectAsStateWithLifecycle()
    val studioState by studio.state.collectAsStateWithLifecycle()
    val youState by you.state.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var studioStatus by remember { mutableStateOf<String?>(null) }

    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val showBar = route in topLevelRoutes

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = nav,
            startDestination = "today",
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            enterTransition = {
                fadeIn(tween(260)) + slideInVertically(tween(360)) { it / 24 }
            },
            exitTransition = { fadeOut(tween(180)) },
            popEnterTransition = {
                fadeIn(tween(220)) + slideInVertically(tween(320)) { -it / 30 }
            },
            popExitTransition = {
                fadeOut(tween(160)) + slideOutVertically(tween(220)) { it / 30 }
            },
        ) {
            composable("today") {
                TodayScreen(
                    state = todayState,
                    onOpenWord = { nav.navigate("word/$it") },
                    onStartReview = { nav.navigate("review") },
                    onLove = today::loveQuote,
                    onAnother = today::nextQuote,
                    onShare = { nav.navigate("studio") },
                    onMood = today::logMood,
                    onOpenYou = { nav.navigate("you") },
                    onNewCapsule = {
                        capsules.startDraft()
                        nav.navigate("capsule/new")
                    },
                    onOpenCapsule = { nav.navigate("capsules") },
                )
            }

            composable("words") {
                WordsScreen(
                    state = wordsState,
                    onQuery = words::setQuery,
                    onFilter = words::setFilter,
                    onStart = {
                        words.startSession()
                        nav.navigate("review")
                    },
                    onOpen = { nav.navigate("word/$it") },
                    onToggleSave = words::toggleSaved,
                )
            }

            composable("word/{wordId}") { backStackEntry ->
                val wordId = backStackEntry.arguments?.getString("wordId").orEmpty()
                val memory = appState.memories[wordId] ?: WordMemory(wordId = wordId)
                WordDetailScreen(
                    wordId = wordId,
                    memory = memory,
                    onBack = { nav.popBackStack() },
                    onToggleSave = { words.toggleSaved(wordId) },
                    onShare = {
                        studio.selectWord(wordId)
                        nav.navigate("studio")
                    },
                    onPractice = {
                        words.startSession(size = 4)
                        nav.navigate("review")
                    },
                )
            }

            composable("review") {
                LaunchedEffect(Unit) {
                    if (reviewState == null) words.startSession()
                }
                val session = reviewState
                if (session == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("gathering words…", style = KairosType.handSmall)
                    }
                } else {
                    val speaker = com.kairos.app.ui.common.rememberSpeaker(context)
                    ReviewScreen(
                        session = session,
                        preview = words::preview,
                        onReveal = words::reveal,
                        onGrade = words::grade,
                        onClose = {
                            words.closeSession()
                            nav.popBackStack()
                        },
                        onSpeak = speaker::speak,
                    )
                }
            }

            composable("capsules") {
                LaunchedEffect(capsulesState.now) { }
                CapsulesScreen(
                    state = capsulesState,
                    onCompose = {
                        capsules.startDraft()
                        nav.navigate("capsule/new")
                    },
                    onOpen = { nav.navigate("capsule/${it.id}") },
                )
            }

            composable("capsule/new") {
                CapsuleComposerScreen(
                    state = capsulesState,
                    onBack = {
                        capsules.discardDraft()
                        nav.popBackStack()
                    },
                    onTitle = capsules::updateTitle,
                    onBody = capsules::updateBody,
                    onMood = capsules::setMood,
                    onUnlock = capsules::setUnlockAt,
                    onAttach = capsules::attach,
                    onRemove = capsules::removeAttachment,
                    onSeal = capsules::seal,
                    onDismissSealed = {
                        capsules.dismissSealed()
                        nav.popBackStack("capsules", inclusive = false)
                    },
                    onOpenVault = {
                        capsules.dismissSealed()
                        nav.popBackStack("capsules", inclusive = false)
                    },
                    onStartDraft = capsules::startDraft,
                )
            }

            composable("capsule/{capsuleId}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("capsuleId").orEmpty()
                val capsule = capsulesState.vault.all.firstOrNull { it.id == id }
                if (capsule == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyState(
                            title = "That letter moved on",
                            body = "It isn't in the vault any more.",
                            action = null,
                        )
                    }
                } else {
                    CapsuleDetailScreen(
                        capsule = capsule,
                        onBack = { nav.popBackStack() },
                        onOpen = { capsules.open(capsule) },
                        onDelete = {
                            capsules.delete(capsule)
                            nav.popBackStack()
                        },
                    )
                }
            }

            composable("studio") {
                StudioScreen(
                    state = studioState,
                    onStyle = studio::setStyle,
                    onPalette = studio::setPalette,
                    onRatio = studio::setRatio,
                    onSource = studio::setSource,
                    onWord = studio::selectWord,
                    onQuote = studio::selectQuote,
                    onShuffle = {
                        if (studioState.design.source == com.kairos.app.ui.studio.StudioSource.Word) {
                            studio.shuffleWord()
                        } else {
                            studio.shuffleQuote()
                        }
                    },
                    onSave = {
                        val bitmap = studioState.bitmap
                        if (bitmap != null) {
                            val uri = PosterSharing.saveToGallery(context, bitmap, studio.suggestedName())
                            studioStatus = if (uri != null) "saved to Pictures/Kairos" else "couldn't save"
                        }
                    },
                    onShare = {
                        val bitmap = studioState.bitmap
                        if (bitmap != null) {
                            val intent = PosterSharing.shareIntent(context, bitmap, studio.suggestedName())
                            context.startActivity(Intent.createChooser(intent, "Share your keeper"))
                            studioStatus = "ready to share"
                        }
                    },
                    status = studioStatus,
                )
            }

            composable("you") {
                YouScreen(
                    state = youState,
                    onName = you::setName,
                    onTheme = you::setThemeMode,
                    onAccent = you::setAccent,
                    onReduceMotion = you::setReduceMotion,
                    onPace = you::setPace,
                    onToggleInterest = you::toggleInterest,
                    onToggleQuoteTheme = you::toggleQuoteTheme,
                    onExport = you::exportTo,
                    onReset = {
                        you.reset()
                        nav.popBackStack("today", inclusive = false)
                    },
                )
            }
        }

        if (showBar) {
            KairosNavBar(
                current = route,
                onSelect = { dest ->
                    nav.navigate(dest.route) {
                        popUpTo("today") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding(),
            )
        }
    }
}

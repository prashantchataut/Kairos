package com.kairos.app.ui.nav

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.kairos.app.ui.components.Icons
import com.kairos.app.ui.feature.capsule.CapsuleHome
import com.kairos.app.ui.feature.capsule.ComposeLetter
import com.kairos.app.ui.feature.capsule.LetterScreen
import com.kairos.app.ui.feature.detail.QuoteScreen
import com.kairos.app.ui.feature.detail.WordScreen
import com.kairos.app.ui.feature.learn.LearnScreen
import com.kairos.app.ui.feature.onboarding.OnboardingScreen
import com.kairos.app.ui.feature.practice.PracticeScreen
import com.kairos.app.ui.feature.studio.StudioScreen
import com.kairos.app.ui.feature.today.TodayScreen
import com.kairos.app.ui.feature.you.CuratorScreen
import com.kairos.app.ui.feature.you.InterestsScreen
import com.kairos.app.ui.feature.you.YouScreen
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.Motion
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import kotlinx.serialization.Serializable

object Route {
    @Serializable data object Welcome
    @Serializable data object Home
    @Serializable data class Word(val id: String)
    @Serializable data class Quote(val id: String)
    @Serializable data class Book(val id: String)
    @Serializable data object Library
    @Serializable data object Play
    @Serializable data object Practice
    @Serializable data class Studio(val kind: String, val id: String)
    @Serializable data object Compose
    @Serializable data class Letter(val id: Long)
    @Serializable data object Curator
    @Serializable data object Interests
}

private val enter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = { fadeIn(Motion.fade(300)) + slideInVertically(Motion.offset) { it / 14 } }
private val exit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = { fadeOut(Motion.fade(220)) + scaleOut(targetScale = 0.98f) }
private val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = { fadeIn(Motion.fade(300)) + scaleIn(initialScale = 0.98f) }
private val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = { fadeOut(Motion.fade(200)) + slideOutVertically(Motion.offset) { it / 14 } }

private inline fun <reified T : Any> NavGraphBuilder.screen(noinline content: @Composable (NavBackStackEntry) -> Unit) =
    composable<T>(enterTransition = enter, exitTransition = exit, popEnterTransition = popEnter, popExitTransition = popExit) { e ->
        CompositionLocalProvider(LocalAnim provides this) { content(e) }
    }

@Composable
fun KairosNav(onboarded: Boolean, deepLink: String?, onDeepLinkHandled: () -> Unit) {
    val nav = rememberNavController()
    val start: Any = remember { if (onboarded) Route.Home else Route.Welcome }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(deepLink, onboarded) {
        if (!onboarded || deepLink == null) return@LaunchedEffect
        deepLink.removePrefix("capsule/").toLongOrNull()?.let { nav.navigate(Route.Letter(it)) }
        onDeepLinkHandled()
    }
    SharedTransitionLayout {
        CompositionLocalProvider(LocalShared provides this) {
            NavHost(nav, start, Modifier.fillMaxSize().background(K.colors.canvas)) {
                screen<Route.Welcome> { OnboardingScreen(onDone = { nav.navigate(Route.Home) { popUpTo(Route.Welcome) { inclusive = true } } }) }
                screen<Route.Home> {
                    Home(
                        tab = tab, onTab = { tab = it },
                        openWord = { nav.navigate(Route.Word(it)) },
                        openQuote = { nav.navigate(Route.Quote(it)) },
                        openStudio = { k, id -> nav.navigate(Route.Studio(k, id)) },
                        openPractice = { nav.navigate(Route.Practice) },
                        openCompose = { nav.navigate(Route.Compose) },
                        openLetter = { nav.navigate(Route.Letter(it)) },
                        openCurator = { nav.navigate(Route.Curator) },
                        openInterests = { nav.navigate(Route.Interests) },
                        openBook = { nav.navigate(Route.Book(it)) },
                        openLibrary = { nav.navigate(Route.Library) },
                        openPlay = { nav.navigate(Route.Play) },
                    )
                }
                screen<Route.Word> { e -> val id = e.toRoute<Route.Word>().id; WordScreen(id, { nav.popBackStack() }, { nav.navigate(Route.Studio("word", id)) }, { nav.navigate(Route.Practice) }) }
                screen<Route.Quote> { e -> val id = e.toRoute<Route.Quote>().id; QuoteScreen(id, { nav.popBackStack() }) { nav.navigate(Route.Studio("quote", id)) } }
                screen<Route.Library> {
                    Box(Modifier.fillMaxSize()) {
                        LearnScreen({ nav.navigate(Route.Word(it)) }, { nav.navigate(Route.Quote(it)) }, { nav.navigate(Route.Practice) }, { nav.navigate(Route.Curator) }, { nav.navigate(Route.Book(it)) })
                        com.kairos.app.ui.feature.detail.TopBar({ nav.popBackStack() }, onSky = true, modifier = Modifier.align(Alignment.TopEnd).width(80.dp))
                    }
                }
                screen<Route.Play> { com.kairos.app.ui.feature.play.PlayScreen { nav.popBackStack() } }
                screen<Route.Book> { e -> com.kairos.app.ui.feature.detail.BookScreen(e.toRoute<Route.Book>().id) { nav.popBackStack() } }
                screen<Route.Practice> { PracticeScreen { nav.popBackStack() } }
                screen<Route.Studio> { e -> val r = e.toRoute<Route.Studio>(); StudioScreen(r.kind, r.id) { nav.popBackStack() } }
                screen<Route.Compose> { ComposeLetter({ nav.popBackStack() }, { nav.popBackStack() }) }
                screen<Route.Letter> { e -> LetterScreen(e.toRoute<Route.Letter>().id, { nav.popBackStack() }) { nav.navigate(Route.Compose) { popUpTo<Route.Home>() } } }
                screen<Route.Curator> { CuratorScreen { nav.popBackStack() } }
                screen<Route.Interests> { InterestsScreen { nav.popBackStack() } }
            }
        }
    }
}

@Composable
private fun Home(
    tab: Int, onTab: (Int) -> Unit,
    openWord: (String) -> Unit, openQuote: (String) -> Unit, openStudio: (String, String) -> Unit, openPractice: () -> Unit,
    openCompose: () -> Unit, openLetter: (Long) -> Unit, openCurator: () -> Unit, openInterests: () -> Unit, openBook: (String) -> Unit,
    openLibrary: () -> Unit, openPlay: () -> Unit,
) {
    val haze = remember { HazeState() }
    var sheet by remember { mutableStateOf(false) }
    BackHandler(tab != 0) { onTab(0) }
    Box(Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalHaze provides haze) {
            AnimatedContent(
                tab, Modifier.fillMaxSize().haze(haze),
                transitionSpec = { (fadeIn(Motion.fade(260)) + scaleIn(initialScale = 0.99f)) togetherWith fadeOut(Motion.fade(160)) },
                label = "tabs",
            ) { t ->
                when (t) {
                    0 -> TodayScreen(openWord, openQuote, openStudio, openPractice, { onTab(2) }, openCurator, { onTab(3) }, openBook, openPlay)
                    1 -> com.kairos.app.ui.feature.feed.FeedScreen(openStudio, openBook, openLibrary, openWord)
                    2 -> CapsuleHome(openCompose, openLetter)
                    else -> YouScreen(openCurator, openInterests)
                }
            }
            BottomBar(tab, onTab, { sheet = true }, sheet, Modifier.align(Alignment.BottomCenter), night = tab == 2 || tab == 1)
        }
    }
    if (sheet) QuickSheet(
        listOf(
            QuickAction(Icons.Edit, "Write to future you", "A letter, a photo or a voice note", com.kairos.app.ui.theme.Tint.Lavender, openCompose),
            QuickAction(Icons.Cards, "Play", "Random cards from your words and thoughts", com.kairos.app.ui.theme.Tint.Mint, openPlay),
            QuickAction(Icons.Search, "Your library", "Words, thoughts and books you've met", com.kairos.app.ui.theme.Tint.Sky, openLibrary),
            QuickAction(Icons.Smile, "Check in", "How are you, really?", com.kairos.app.ui.theme.Tint.Butter, { onTab(0) }),
        ),
    ) { sheet = false }
}

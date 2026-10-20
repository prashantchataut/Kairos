package com.kairos.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.kairos.app.ui.screens.capsule.CapsuleComposeScreen
import com.kairos.app.ui.screens.capsule.CapsuleDetailScreen
import com.kairos.app.ui.screens.home.HomeScreen
import com.kairos.app.ui.screens.onboarding.OnboardingScreen
import com.kairos.app.ui.screens.practice.PracticeScreen
import com.kairos.app.ui.screens.settings.InterestsScreen
import com.kairos.app.ui.screens.studio.StudioScreen
import com.kairos.app.ui.screens.words.QuoteDetailScreen
import com.kairos.app.ui.screens.words.WordDetailScreen
import com.kairos.app.ui.theme.Motion
import kotlinx.serialization.Serializable

object Route {
    @Serializable data object Onboarding
    @Serializable data class Home(val tab: Int = 0)
    @Serializable data class Word(val id: String, val origin: String = "list")
    @Serializable data class Quote(val id: String)
    @Serializable data object Practice
    /** [kind] is "word" or "quote". */
    @Serializable data class Studio(val kind: String, val id: String)
    @Serializable data object CapsuleCompose
    @Serializable data class Capsule(val id: Long)
    @Serializable data object Interests
}

private val enterUp: AnimatedContentTransitionScope<NavBackStackEntry>.() -> androidx.compose.animation.EnterTransition = {
    fadeIn(Motion.fade()) + slideInVertically(Motion.glideOffset) { it / 10 } + scaleIn(Motion.settle(), initialScale = 0.98f)
}
private val exitDown: AnimatedContentTransitionScope<NavBackStackEntry>.() -> androidx.compose.animation.ExitTransition = {
    fadeOut(Motion.fade(Motion.Quick)) + slideOutVertically(Motion.glideOffset) { it / 12 }
}
private val stay: AnimatedContentTransitionScope<NavBackStackEntry>.() -> androidx.compose.animation.ExitTransition = {
    fadeOut(Motion.fade(Motion.Medium)) + scaleOut(Motion.settle(), targetScale = 0.97f)
}
private val returnIn: AnimatedContentTransitionScope<NavBackStackEntry>.() -> androidx.compose.animation.EnterTransition = {
    fadeIn(Motion.fade(Motion.Medium)) + scaleIn(Motion.settle(), initialScale = 0.97f)
}

private inline fun <reified T : Any> NavGraphBuilder.screen(
    noinline content: @Composable (NavBackStackEntry) -> Unit,
) = composable<T>(
    enterTransition = enterUp, exitTransition = stay, popEnterTransition = returnIn, popExitTransition = exitDown,
) { entry ->
    CompositionLocalProvider(LocalNavAnimScope provides this) { content(entry) }
}

@Composable
fun KairosNavHost(onboarded: Boolean, deepLink: String?, onDeepLinkHandled: () -> Unit) {
    val nav = rememberNavController()
    LaunchedEffect(deepLink, onboarded) {
        if (!onboarded || deepLink == null) return@LaunchedEffect
        deepLink.removePrefix("capsule/").toLongOrNull()?.let { nav.navigate(Route.Capsule(it)) }
        onDeepLinkHandled()
    }
    // Fixed at first composition: completing onboarding navigates instead of swapping the graph.
    val start: Any = remember { if (onboarded) Route.Home() else Route.Onboarding }
    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedScope provides this) {
            NavHost(nav, startDestination = start) {
                screen<Route.Onboarding> {
                    OnboardingScreen(onDone = {
                        nav.navigate(Route.Home()) { popUpTo(Route.Onboarding) { inclusive = true } }
                    })
                }
                screen<Route.Home> { entry ->
                    HomeScreen(
                        initialTab = entry.toRoute<Route.Home>().tab,
                        openWord = { id, origin -> nav.navigate(Route.Word(id, origin)) },
                        openQuote = { nav.navigate(Route.Quote(it)) },
                        openPractice = { nav.navigate(Route.Practice) },
                        openStudio = { kind, id -> nav.navigate(Route.Studio(kind, id)) },
                        openCompose = { nav.navigate(Route.CapsuleCompose) },
                        openCapsule = { nav.navigate(Route.Capsule(it)) },
                        openInterests = { nav.navigate(Route.Interests) },
                    )
                }
                screen<Route.Word> { entry ->
                    val r = entry.toRoute<Route.Word>()
                    WordDetailScreen(
                        id = r.id, origin = r.origin, onBack = { nav.popBackStack() },
                        onShare = { nav.navigate(Route.Studio("word", r.id)) },
                        onPractice = { nav.navigate(Route.Practice) },
                    )
                }
                screen<Route.Quote> { entry ->
                    val id = entry.toRoute<Route.Quote>().id
                    QuoteDetailScreen(id = id, onBack = { nav.popBackStack() }, onShare = { nav.navigate(Route.Studio("quote", id)) })
                }
                screen<Route.Practice> { PracticeScreen(onBack = { nav.popBackStack() }) }
                screen<Route.Studio> { entry ->
                    val r = entry.toRoute<Route.Studio>()
                    StudioScreen(kind = r.kind, id = r.id, onBack = { nav.popBackStack() })
                }
                screen<Route.CapsuleCompose> { CapsuleComposeScreen(onBack = { nav.popBackStack() }, onSealed = { nav.popBackStack() }) }
                screen<Route.Capsule> { entry ->
                    CapsuleDetailScreen(
                        id = entry.toRoute<Route.Capsule>().id,
                        onBack = { nav.popBackStack() },
                        onWriteAnother = { nav.navigate(Route.CapsuleCompose) { popUpTo<Route.Home>() } },
                    )
                }
                screen<Route.Interests> { InterestsScreen(onBack = { nav.popBackStack() }) }
            }
        }
    }
}

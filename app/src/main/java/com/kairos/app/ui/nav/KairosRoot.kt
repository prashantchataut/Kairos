package com.kairos.app.ui.nav

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kairos.app.container
import com.kairos.app.data.prefs.Settings
import com.kairos.app.engine.daily.Mood
import com.kairos.app.ui.capsule.CapsuleComposeScreen
import com.kairos.app.ui.capsule.CapsuleOpenScreen
import com.kairos.app.ui.capsule.CapsulesScreen
import com.kairos.app.ui.glass.LocalHazeState
import com.kairos.app.ui.glass.WatercolorBackground
import com.kairos.app.ui.learn.LearnScreen
import com.kairos.app.ui.learn.LookupScreen
import com.kairos.app.ui.learn.PracticeScreen
import com.kairos.app.ui.learn.QuotesScreen
import com.kairos.app.ui.learn.WordDetailScreen
import com.kairos.app.ui.onboarding.OnboardingScreen
import com.kairos.app.ui.play.GameDetailScreen
import com.kairos.app.ui.play.OriginalGameScreen
import com.kairos.app.ui.play.PlayScreen
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.today.BreatheScreen
import com.kairos.app.ui.today.KindnessScreen
import com.kairos.app.ui.today.TodayScreen
import com.kairos.app.ui.you.PondexScreen
import com.kairos.app.ui.you.SettingsScreen
import com.kairos.app.ui.you.YouScreen
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

object Routes {
    const val ONBOARDING = "onboarding"
    const val WORD = "word/{id}"
    const val PRACTICE = "practice?focus={focus}"
    const val LOOKUP = "lookup"
    const val QUOTES = "quotes"
    const val CAPSULE_NEW = "capsule/new"
    const val CAPSULE = "capsule/{id}"
    const val GAME = "game/{id}"
    const val ORIGINAL = "original/{id}"
    const val PONDEX = "pondex"
    const val SETTINGS = "settings"
    const val BREATHE = "breathe"
    const val KINDNESS = "kindness"

    fun word(id: String) = "word/${android.net.Uri.encode(id)}"
    fun practice(focus: List<String> = emptyList()) = if (focus.isEmpty()) "practice" else "practice?focus=${android.net.Uri.encode(focus.joinToString(","))}"
    fun capsule(id: String) = "capsule/$id"
    fun game(id: String) = "game/$id"
    fun original(id: String) = "original/$id"
}

/** Thin wrapper so screens navigate by intent, not by NavController details. */
class Navigator(private val nav: NavHostController) {
    fun open(route: String) = nav.navigate(route) { launchSingleTop = true }
    fun back() { if (!nav.popBackStack()) Unit }
    fun tab(tab: Tab) = nav.navigate(tab.route) {
        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
    fun finishOnboarding() = nav.navigate(Tab.Today.route) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
}

val LocalNavigator = staticCompositionLocalOf<Navigator> { error("No navigator") }

private val tabRoutes = Tab.entries.map { it.route }.toSet()

private fun AnimatedContentTransitionScope<NavBackStackEntry>.enter(): EnterTransition {
    val toTab = targetState.destination.route in tabRoutes
    val fromTab = initialState.destination.route in tabRoutes
    return if (toTab && fromTab) {
        fadeIn(tween(260, delayMillis = 60)) + scaleIn(tween(320), initialScale = 0.985f)
    } else {
        fadeIn(tween(280)) + slideInVertically(tween(380, easing = com.kairos.app.ui.theme.Motion.Drawer)) { it / 14 }
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.exit(): ExitTransition =
    fadeOut(tween(160)) + scaleOut(tween(220), targetScale = 0.985f)

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popExit(): ExitTransition =
    fadeOut(tween(200)) + slideOutVertically(tween(260)) { it / 18 }

@Composable
fun KairosRoot(settings: Settings, pendingRoute: String?, onRouteHandled: () -> Unit) {
    val haze = remember { HazeState() }
    val nav = rememberNavController()
    val navigator = remember(nav) { Navigator(nav) }
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val tab = Tab.entries.firstOrNull { it.route == route }
    val container = LocalContext.current.container
    val mood by container.moods.observeToday().collectAsState(initial = null)
    val colors = Kairos.colors

    LaunchedEffect(pendingRoute, settings.onboarded) {
        if (pendingRoute != null && settings.onboarded) {
            runCatching { nav.navigate(pendingRoute) { launchSingleTop = true } }
            onRouteHandled()
        }
    }

    val washes: List<Color> = when {
        route == null || route == Routes.ONBOARDING -> listOf(colors.sky, colors.rose, colors.mint, colors.lilac, colors.butter)
        route.startsWith("capsule") -> listOf(colors.lilac, colors.rose, colors.sky, colors.peach, colors.lilac)
        route == Tab.Play.route || route.startsWith("game") || route.startsWith("original") -> listOf(colors.mint, colors.butter, colors.sky, colors.peach, colors.mint)
        route == Tab.Learn.route || route.startsWith("word") || route.startsWith("practice") || route == Routes.QUOTES || route == Routes.LOOKUP ->
            listOf(colors.sky, colors.lilac, colors.mint, colors.sky, colors.butter)
        route == Tab.You.route || route == Routes.PONDEX -> listOf(colors.peach, colors.mint, colors.rose, colors.sky, colors.butter)
        else -> when (mood) {
            Mood.Stormy -> listOf(colors.lilac, colors.sky, colors.lilac, colors.sky, colors.mint)
            Mood.Cloudy -> listOf(colors.sky, colors.lilac, colors.mint, colors.sky, colors.peach)
            Mood.Sunny -> listOf(colors.butter, colors.peach, colors.mint, colors.sky, colors.butter)
            Mood.Glowing -> listOf(colors.peach, colors.rose, colors.butter, colors.lilac, colors.butter)
            else -> colors.washes.take(5)
        }
    }

    CompositionLocalProvider(LocalHazeState provides haze, LocalNavigator provides navigator) {
        Box(Modifier.fillMaxSize()) {
            WatercolorBackground(
                Modifier.fillMaxSize().hazeSource(haze, zIndex = 0f),
                washes = washes,
                animate = !settings.calmBackground,
            )
            NavHost(
                navController = nav,
                startDestination = if (settings.onboarded) Tab.Today.route else Routes.ONBOARDING,
                modifier = Modifier.fillMaxSize().hazeSource(haze, zIndex = 1f),
                enterTransition = { enter() },
                exitTransition = { exit() },
                popEnterTransition = { fadeIn(tween(240)) + scaleIn(tween(300), initialScale = 0.985f) },
                popExitTransition = { popExit() },
            ) {
                composable(Routes.ONBOARDING) { OnboardingScreen() }
                composable(Tab.Today.route) { TodayScreen() }
                composable(Tab.Learn.route) { LearnScreen() }
                composable(Tab.Capsule.route) { CapsulesScreen() }
                composable(Tab.Play.route) { PlayScreen() }
                composable(Tab.You.route) { YouScreen() }
                composable(Routes.WORD, listOf(navArgument("id") { type = NavType.StringType })) {
                    WordDetailScreen(it.arguments?.getString("id").orEmpty())
                }
                composable(Routes.PRACTICE, listOf(navArgument("focus") { type = NavType.StringType; nullable = true; defaultValue = null })) {
                    PracticeScreen(it.arguments?.getString("focus")?.split(",")?.filter { s -> s.isNotBlank() })
                }
                composable(Routes.LOOKUP) { LookupScreen() }
                composable(Routes.QUOTES) { QuotesScreen() }
                composable(Routes.CAPSULE_NEW) { CapsuleComposeScreen() }
                composable(Routes.CAPSULE, listOf(navArgument("id") { type = NavType.StringType })) {
                    CapsuleOpenScreen(it.arguments?.getString("id").orEmpty())
                }
                composable(Routes.GAME, listOf(navArgument("id") { type = NavType.StringType })) {
                    GameDetailScreen(it.arguments?.getString("id").orEmpty())
                }
                composable(Routes.ORIGINAL, listOf(navArgument("id") { type = NavType.StringType })) {
                    OriginalGameScreen(it.arguments?.getString("id").orEmpty())
                }
                composable(Routes.PONDEX) { PondexScreen() }
                composable(Routes.SETTINGS) { SettingsScreen() }
                composable(Routes.BREATHE) { BreatheScreen() }
                composable(Routes.KINDNESS) { KindnessScreen() }
            }

            AnimatedVisibility(
                visible = tab != null,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(tween(420, easing = com.kairos.app.ui.theme.Motion.Drawer)) { it * 2 } + fadeIn(tween(200)),
                exit = slideOutVertically(tween(240)) { it * 2 } + fadeOut(tween(160)),
            ) {
                val ready by container.capsules.observeAll().collectAsState(initial = emptyList())
                val now = System.currentTimeMillis()
                LiquidTabBar(
                    selected = tab ?: Tab.Today,
                    onSelect = navigator::tab,
                    badges = mapOf(Tab.Capsule to ready.any { it.openedAt == null && it.unlockAt <= now }),
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                        .widthIn(max = 520.dp),
                )
            }
        }
    }
}

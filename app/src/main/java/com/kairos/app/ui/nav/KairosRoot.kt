package com.kairos.app.ui.nav

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kairos.app.data.prefs.Settings
import com.kairos.app.ui.kit.LiquidNavBar
import com.kairos.app.ui.kit.LocalHazeState
import com.kairos.app.ui.kit.NavItem
import com.kairos.app.ui.kit.WatercolorBackdrop
import com.kairos.app.ui.screens.capsule.CapsuleComposeScreen
import com.kairos.app.ui.screens.capsule.CapsuleListScreen
import com.kairos.app.ui.screens.capsule.CapsuleOpenScreen
import com.kairos.app.ui.screens.games.MemoryPondScreen
import com.kairos.app.ui.screens.games.PipRaindropsScreen
import com.kairos.app.ui.screens.games.WordBloomScreen
import com.kairos.app.ui.screens.learn.LearnScreen
import com.kairos.app.ui.screens.learn.PracticeScreen
import com.kairos.app.ui.screens.learn.WordDetailScreen
import com.kairos.app.ui.screens.onboarding.OnboardingScreen
import com.kairos.app.ui.screens.play.GameDetailScreen
import com.kairos.app.ui.screens.play.PlayScreen
import com.kairos.app.ui.screens.play.WebGameScreen
import com.kairos.app.ui.screens.settings.SettingsScreen
import com.kairos.app.ui.screens.today.TodayScreen
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Motion
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

object Routes {
    const val ONBOARDING = "onboarding"
    const val TODAY = "today"
    const val LEARN = "learn"
    const val CAPSULE = "capsule"
    const val PLAY = "play"
    const val SETTINGS = "settings"
    const val PRACTICE = "practice"
    const val CAPSULE_NEW = "capsule/new"
    fun word(id: String) = "word/$id"
    fun capsule(id: Long) = "capsule/open/$id"
    fun game(id: String) = "game/$id"
    fun playGame(id: String) = "play/$id"
    fun web(id: String) = "web/$id"
}

private val tabs = listOf(
    NavItem(Routes.TODAY, "Today", Icons.Outlined.WbSunny, Icons.Rounded.WbSunny),
    NavItem(Routes.LEARN, "Learn", Icons.Outlined.AutoStories, Icons.Rounded.AutoStories),
    NavItem(Routes.CAPSULE, "Capsule", Icons.Outlined.Mail, Icons.Rounded.Mail),
    NavItem(Routes.PLAY, "Play", Icons.Outlined.SportsEsports, Icons.Rounded.SportsEsports),
)

/** Which watercolor arrangement sits behind each route — tabs each get their own palette. */
private fun backdropVariant(route: String?): Int = when {
    route == null -> 0
    route == Routes.TODAY || route == Routes.ONBOARDING -> 0
    route == Routes.LEARN || route.startsWith("word") || route == Routes.PRACTICE -> 1
    route.startsWith("capsule") -> 2
    route.startsWith("play") || route.startsWith("game") || route.startsWith("web") -> 3
    else -> 4
}

@Composable
fun KairosRoot(settings: Settings, pendingRoute: String?, onRouteConsumed: () -> Unit) {
    val nav = rememberNavController()
    val haze = remember { HazeState() }
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val tabIndex = tabs.indexOfFirst { it.route == route }

    LaunchedEffect(pendingRoute, settings.onboarded) {
        if (pendingRoute != null && settings.onboarded) {
            nav.navigate(pendingRoute)
            onRouteConsumed()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().hazeSource(haze)) {
            WatercolorBackdrop(variant = backdropVariant(route))
            KairosNavHost(nav, start = if (settings.onboarded) Routes.TODAY else Routes.ONBOARDING, settings = settings)
        }
        CompositionLocalProvider(LocalHazeState provides haze) {
            AnimatedVisibility(
                visible = tabIndex >= 0,
                enter = fadeIn(Motion.enter(400)) + slideInVertically(Motion.enter(500)) { it / 2 },
                exit = fadeOut(Motion.exit()) + slideOutVertically(Motion.exit()) { it / 2 },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .widthIn(max = 480.dp),
            ) {
                LiquidNavBar(
                    items = tabs,
                    selectedIndex = tabIndex.coerceAtLeast(0),
                    onSelect = { i ->
                        val target = tabs[i].route
                        if (target != route) {
                            nav.navigate(target) {
                                popUpTo(Routes.TODAY) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun KairosNavHost(nav: NavHostController, start: String, settings: Settings) {
    val reduce = Kairos.reduceMotion
    val tabRoutes = tabs.map { it.route }.toSet()
    NavHost(
        navController = nav,
        startDestination = start,
        enterTransition = {
            when {
                reduce -> fadeIn(tween(150))
                initialState.destination.route in tabRoutes && targetState.destination.route in tabRoutes ->
                    fadeIn(Motion.enter(420, 60)) + scaleIn(Motion.enter(420, 60), initialScale = 0.97f)
                else -> fadeIn(Motion.enter(380)) + slideInHorizontally(Motion.enter(460)) { it / 6 }
            }
        },
        exitTransition = {
            when {
                reduce -> fadeOut(tween(150))
                initialState.destination.route in tabRoutes && targetState.destination.route in tabRoutes ->
                    fadeOut(Motion.exit(140))
                else -> fadeOut(Motion.exit(200)) + scaleOut(Motion.exit(220), targetScale = 0.97f)
            }
        },
        popEnterTransition = {
            if (reduce) fadeIn(tween(150))
            else fadeIn(Motion.enter(380)) + scaleIn(Motion.enter(420), initialScale = 0.97f)
        },
        popExitTransition = {
            if (reduce) fadeOut(tween(150))
            else fadeOut(Motion.exit(180)) + slideOutHorizontally(Motion.exit(220)) { it / 6 }
        },
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(onDone = {
                nav.navigate(Routes.TODAY) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
            })
        }
        composable(Routes.TODAY) {
            TodayScreen(
                settings = settings,
                onOpenWord = { nav.navigate(Routes.word(it)) },
                onOpenCapsule = { nav.navigate(Routes.capsule(it)) },
                onNewCapsule = { nav.navigate(Routes.CAPSULE_NEW) },
                onPractice = { nav.navigate(Routes.PRACTICE) },
                onSettings = { nav.navigate(Routes.SETTINGS) },
                onPlay = { nav.navigate(Routes.playGame(it)) },
            )
        }
        composable(Routes.LEARN) {
            LearnScreen(
                onOpenWord = { nav.navigate(Routes.word(it)) },
                onPractice = { nav.navigate(Routes.PRACTICE) },
            )
        }
        composable(Routes.CAPSULE) {
            CapsuleListScreen(
                onNew = { nav.navigate(Routes.CAPSULE_NEW) },
                onOpen = { nav.navigate(Routes.capsule(it)) },
            )
        }
        composable(Routes.PLAY) {
            PlayScreen(onOpenGame = { nav.navigate(Routes.game(it)) }, onPlay = { nav.navigate(Routes.playGame(it)) })
        }
        composable(Routes.SETTINGS) { SettingsScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.PRACTICE) { PracticeScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.CAPSULE_NEW) {
            CapsuleComposeScreen(onBack = { nav.popBackStack() }, onSealed = { nav.popBackStack() })
        }
        composable("word/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) {
            WordDetailScreen(id = it.arguments?.getString("id") ?: "", onBack = { nav.popBackStack() })
        }
        composable("capsule/open/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) {
            CapsuleOpenScreen(id = it.arguments?.getLong("id") ?: 0, onBack = { nav.popBackStack() })
        }
        composable("game/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) {
            GameDetailScreen(
                id = it.arguments?.getString("id") ?: "",
                onBack = { nav.popBackStack() },
                onPlay = { gameId -> nav.navigate(Routes.playGame(gameId)) },
                onPlayWeb = { gameId -> nav.navigate(Routes.web(gameId)) },
            )
        }
        composable("web/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) {
            WebGameScreen(id = it.arguments?.getString("id") ?: "", onBack = { nav.popBackStack() })
        }
        composable("play/word-bloom") { WordBloomScreen(onBack = { nav.popBackStack() }) }
        composable("play/pip-raindrops") { PipRaindropsScreen(onBack = { nav.popBackStack() }) }
        composable("play/memory-pond") { MemoryPondScreen(onBack = { nav.popBackStack() }) }
    }
}

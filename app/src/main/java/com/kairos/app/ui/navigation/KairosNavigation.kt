package com.kairos.app.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kairos.app.ui.screens.home.FocusedTodayScreen
import com.kairos.app.ui.screens.idiom.IdiomDetailScreen
import com.kairos.app.ui.screens.journal.JournalDetailScreen
import com.kairos.app.ui.screens.journal.JournalHistoryScreen
import com.kairos.app.ui.screens.journal.FocusedReflectScreen
import com.kairos.app.ui.screens.journal.NewJournalEntryScreen
import com.kairos.app.ui.screens.onboarding.OnboardingScreen
import com.kairos.app.ui.screens.profile.AchievementsCollectionScreen
import com.kairos.app.ui.screens.profile.BannerSelectionScreen
import com.kairos.app.ui.screens.profile.EditProfileScreen
import com.kairos.app.ui.screens.profile.FocusedProfileScreen
import com.kairos.app.ui.screens.profile.SettingsScreen
import com.kairos.app.ui.screens.setup.InterestsSetupScreen
import com.kairos.app.ui.screens.quotes.FocusedLibraryScreen
import com.kairos.app.ui.screens.quotes.WisdomTab
import com.kairos.app.ui.screens.vocabulary.VocabularyDetailScreen
import com.kairos.app.ui.screens.vocabulary.FocusedLearnScreen
import com.kairos.app.ui.screens.vocabulary.VocabularySessionScreen

/**
 * Kairos Navigation - Screen Routes & Navigation Graph
 *
 * Defines all navigation destinations and transitions for the app.
 * Features:
 * - Smooth, polished screen transitions
 * - Proper navigation state management
 * - Type-safe route definitions
 */

// =============================================================================
// ANIMATION CONFIGURATION
// =============================================================================

// Custom easing for smooth, natural-feeling transitions
private val EaseOutQuart = CubicBezierEasing(0.25f, 1f, 0.5f, 1f)
private val EaseInQuart = CubicBezierEasing(0.5f, 0f, 0.75f, 0f)

// Animation durations (in milliseconds)
private const val TRANSITION_DURATION = 350
private const val FADE_DURATION = 250

// Slide offset for horizontal transitions
private const val SLIDE_OFFSET_FRACTION = 0.15f

// =============================================================================
// SCREEN ROUTE DEFINITIONS
// =============================================================================

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")

    data object Home : Screen("home")

    data object JournalList : Screen("journal")

    data object JournalHistory : Screen("journal/history")

    data object NewJournalEntry : Screen("journal/new?prefill={prefill}") {
        fun createRoute(prefilledContent: String? = null): String {
            return if (prefilledContent.isNullOrEmpty()) {
                "journal/new"
            } else {
                "journal/new?prefill=${java.net.URLEncoder.encode(prefilledContent, "UTF-8")}"
            }
        }
    }

    data object JournalDetail : Screen("journal/{entryId}") {
        fun createRoute(entryId: Long) = "journal/$entryId"
    }

    data object Profile : Screen("profile")

    data object EditProfile : Screen("profile/edit")

    data object BannerSelection : Screen("profile/banner")

    data object AchievementsCollection : Screen("profile/achievements")

    data object Settings : Screen("settings")

    data object VocabularyList : Screen("vocabulary")

    data object VocabularyDetail : Screen("vocabulary/{wordId}") {
        fun createRoute(wordId: Long) = "vocabulary/$wordId"
    }

    data object Quotes : Screen("quotes/{tab}") {
        fun createRoute(tab: String = "quotes") = "quotes/$tab"
    }

    data object IdiomDetail : Screen("idiom/{idiomId}") {
        fun createRoute(idiomId: Long) = "idiom/$idiomId"
    }

    data object VocabularySession : Screen("vocabulary/session?mode={mode}") {
        fun createRoute(mode: com.kairos.app.ui.screens.vocabulary.SessionMode): String =
            "vocabulary/session?mode=${'$'}{mode.name}"
    }

    data object InterestsSetup : Screen("interests_setup")
}

// =============================================================================
// NAVIGATION HOST
// =============================================================================

/**
 * Main navigation host with smooth transitions between screens.
 */
@Composable
fun KairosNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        // Enter transition - new screen sliding in from right
        enterTransition = {
            fadeIn(
                animationSpec = tween(
                    durationMillis = FADE_DURATION,
                    easing = EaseOutQuart
                )
            ) + slideInHorizontally(
                initialOffsetX = { (it * SLIDE_OFFSET_FRACTION).toInt() },
                animationSpec = tween(
                    durationMillis = TRANSITION_DURATION,
                    easing = EaseOutQuart
                )
            )
        },
        // Exit transition - current screen fading out while staying mostly in place
        exitTransition = {
            fadeOut(
                animationSpec = tween(
                    durationMillis = FADE_DURATION,
                    easing = EaseInQuart
                )
            ) + slideOutHorizontally(
                targetOffsetX = { -(it * SLIDE_OFFSET_FRACTION * 0.3f).toInt() },
                animationSpec = tween(
                    durationMillis = TRANSITION_DURATION,
                    easing = EaseInQuart
                )
            )
        },
        // Pop enter transition - returning screen sliding in from left
        popEnterTransition = {
            fadeIn(
                animationSpec = tween(
                    durationMillis = FADE_DURATION,
                    easing = EaseOutQuart
                )
            ) + slideInHorizontally(
                initialOffsetX = { -(it * SLIDE_OFFSET_FRACTION * 0.3f).toInt() },
                animationSpec = tween(
                    durationMillis = TRANSITION_DURATION,
                    easing = EaseOutQuart
                )
            )
        },
        // Pop exit transition - current screen sliding out to right
        popExitTransition = {
            fadeOut(
                animationSpec = tween(
                    durationMillis = FADE_DURATION,
                    easing = EaseInQuart
                )
            ) + slideOutHorizontally(
                targetOffsetX = { (it * SLIDE_OFFSET_FRACTION).toInt() },
                animationSpec = tween(
                    durationMillis = TRANSITION_DURATION,
                    easing = EaseInQuart
                )
            )
        }
    ) {
        // =====================================================================
        // ONBOARDING
        // =====================================================================
        composable(
            route = Screen.Onboarding.route,
            // Special fade-only transition for onboarding
            enterTransition = { fadeIn(tween(400)) },
            exitTransition = { fadeOut(tween(300)) }
        ) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            FocusedTodayScreen(
                onNavigateToVocabulary = {
                    navController.navigate(Screen.VocabularyList.route)
                },
                onNavigateToQuotes = {
                    navController.navigate(Screen.Quotes.createRoute("quotes"))
                },
                onNavigateToJournal = {
                    navController.navigate(Screen.JournalList.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToNotificationSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.JournalList.route) {
            FocusedReflectScreen(
                onNavigateToNewEntry = {
                    navController.navigate(Screen.NewJournalEntry.createRoute())
                },
                onNavigateToDetail = { entryId ->
                    navController.navigate(Screen.JournalDetail.createRoute(entryId))
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.JournalHistory.route)
                }
            )
        }

        composable(Screen.JournalHistory.route) {
            JournalHistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { entryId ->
                    navController.navigate(Screen.JournalDetail.createRoute(entryId))
                }
            )
        }

        composable(
            route = Screen.NewJournalEntry.route,
            arguments = listOf(navArgument("prefill") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }),
            // Slide up transition for creation screens
            enterTransition = {
                fadeIn(tween(FADE_DURATION)) + slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(TRANSITION_DURATION, easing = EaseOutQuart)
                )
            },
            exitTransition = {
                fadeOut(tween(FADE_DURATION))
            },
            popExitTransition = {
                fadeOut(tween(FADE_DURATION)) + slideOutVertically(
                    targetOffsetY = { it / 4 },
                    animationSpec = tween(TRANSITION_DURATION, easing = EaseInQuart)
                )
            }
        ) { backStackEntry ->
            val prefill = backStackEntry.arguments?.getString("prefill")?.let {
                try { java.net.URLDecoder.decode(it, "UTF-8") } catch (_: Exception) { null }
            }
            NewJournalEntryScreen(
                onNavigateBack = { navController.popBackStack() },
                onEntrySaved = { navController.popBackStack() },
                prefilledContent = prefill
            )
        }

        composable(
            route = Screen.JournalDetail.route,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: return@composable
            JournalDetailScreen(
                entryId = entryId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            FocusedProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToEditProfile = {
                    navController.navigate(Screen.EditProfile.route)
                },
                onNavigateToAchievements = {
                    navController.navigate(Screen.AchievementsCollection.route)
                },
                onNavigateToCosmetics = {
                    navController.navigate(Screen.BannerSelection.route)
                }
            )
        }

        composable(
            route = Screen.EditProfile.route,
            // Slide up for edit screens
            enterTransition = {
                fadeIn(tween(FADE_DURATION)) + slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(TRANSITION_DURATION, easing = EaseOutQuart)
                )
            },
            popExitTransition = {
                fadeOut(tween(FADE_DURATION)) + slideOutVertically(
                    targetOffsetY = { it / 4 },
                    animationSpec = tween(TRANSITION_DURATION, easing = EaseInQuart)
                )
            }
        ) {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBannerSelection = {
                    navController.navigate(Screen.BannerSelection.route)
                }
            )
        }

        composable(Screen.BannerSelection.route) {
            BannerSelectionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AchievementsCollection.route) {
            AchievementsCollectionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToInterests = {
                    navController.navigate(Screen.InterestsSetup.route)
                }
            )
        }

        composable(Screen.VocabularyList.route) {
            FocusedLearnScreen(
                onNavigateToDetail = { wordId ->
                    navController.navigate(Screen.VocabularyDetail.createRoute(wordId))
                },
                onNavigateToSession = { mode ->
                    navController.navigate(Screen.VocabularySession.createRoute(mode))
                }
            )
        }

        composable(
            route = Screen.VocabularyDetail.route,
            arguments = listOf(navArgument("wordId") { type = NavType.LongType })
        ) { backStackEntry ->
            val wordId = backStackEntry.arguments?.getLong("wordId") ?: return@composable
            VocabularyDetailScreen(
                wordId = wordId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Quotes.route,
            arguments = listOf(navArgument("tab") {
                type = NavType.StringType
                defaultValue = "quotes"
            })
        ) { backStackEntry ->
            val tabName = backStackEntry.arguments?.getString("tab") ?: "quotes"
            val initialTab = when (tabName.lowercase()) {
                "proverbs" -> WisdomTab.PROVERBS
                "idioms" -> WisdomTab.IDIOMS
                "phrases" -> WisdomTab.PHRASES
                else -> WisdomTab.QUOTES
            }
            FocusedLibraryScreen(
                initialTab = initialTab
            )
        }

        composable(
            route = Screen.IdiomDetail.route,
            arguments = listOf(navArgument("idiomId") { type = NavType.LongType })
        ) { backStackEntry ->
            val idiomId = backStackEntry.arguments?.getLong("idiomId") ?: return@composable
            IdiomDetailScreen(
                idiomId = idiomId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.VocabularySession.route,
            arguments = listOf(navArgument("mode") { type = NavType.StringType; defaultValue = "MIXED" })
        ) {
            val modeName = it.arguments?.getString("mode") ?: "MIXED"
            val initialMode = runCatching {
                com.kairos.app.ui.screens.vocabulary.SessionMode.valueOf(modeName)
            }.getOrDefault(com.kairos.app.ui.screens.vocabulary.SessionMode.MIXED)
            VocabularySessionScreen(
                initialMode = initialMode,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.InterestsSetup.route) {
            InterestsSetupScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }


    }
}

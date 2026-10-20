package com.kairos.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.kairos.app.R
import com.kairos.app.ui.icons.KairosIcons

/**
 * Focused top-level navigation.
 *
 * Advanced and experimental features still have routes, but they are no longer
 * presented as equally important destinations. This keeps the primary product
 * loop understandable: see today's moment, learn, reflect, and revisit saved content.
 */
sealed class BottomNavItem(
    val route: String,
    val destinationRoute: String = route,
    @StringRes val labelResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @StringRes val contentDescriptionResId: Int = labelResId
) {
    data object Today : BottomNavItem(
        route = Screen.Home.route,
        labelResId = R.string.nav_today,
        selectedIcon = KairosIcons.Home,
        unselectedIcon = KairosIcons.Outlined.Home
    )

    data object Learn : BottomNavItem(
        route = Screen.VocabularyList.route,
        labelResId = R.string.nav_learn,
        selectedIcon = KairosIcons.School,
        unselectedIcon = KairosIcons.Outlined.School
    )

    data object Reflect : BottomNavItem(
        route = Screen.JournalList.route,
        labelResId = R.string.nav_reflect,
        selectedIcon = KairosIcons.Book,
        unselectedIcon = KairosIcons.Outlined.Book
    )

    data object Library : BottomNavItem(
        route = Screen.Quotes.createRoute("quotes"),
        destinationRoute = Screen.Quotes.route,
        labelResId = R.string.nav_library,
        selectedIcon = KairosIcons.AutoStories,
        unselectedIcon = KairosIcons.Outlined.AutoStories
    )

    data object Capsules : BottomNavItem(
        route = Screen.Capsules.route,
        labelResId = R.string.nav_capsules,
        selectedIcon = KairosIcons.Email,
        unselectedIcon = KairosIcons.Email
    )

    data object Play : BottomNavItem(
        route = Screen.Games.route,
        labelResId = R.string.nav_play,
        selectedIcon = KairosIcons.SportsEsports,
        unselectedIcon = KairosIcons.SportsEsports
    )

    companion object {
        val items: List<BottomNavItem> by lazy { listOf(Today, Learn, Capsules, Reflect, Play) }
        val bottomBarRoutes: Set<String> by lazy { items.mapTo(mutableSetOf()) { it.destinationRoute } }
    }
}

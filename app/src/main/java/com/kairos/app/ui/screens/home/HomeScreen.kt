package com.kairos.app.ui.screens.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kairos.app.ui.components.LiquidNavBar
import com.kairos.app.ui.components.NavItem
import com.kairos.app.ui.icons.KairosIcons
import com.kairos.app.ui.screens.capsule.CapsuleHomeScreen
import com.kairos.app.ui.screens.me.MeScreen
import com.kairos.app.ui.screens.today.TodayScreen
import com.kairos.app.ui.screens.words.LibraryScreen
import com.kairos.app.ui.theme.Motion

private val Tabs = listOf(
    NavItem("Today", KairosIcons.Today),
    NavItem("Library", KairosIcons.Words),
    NavItem("Capsule", KairosIcons.Capsule),
    NavItem("Me", KairosIcons.Me),
)

@Composable
fun HomeScreen(
    initialTab: Int,
    openWord: (String, String) -> Unit,
    openQuote: (String) -> Unit,
    openPractice: () -> Unit,
    openStudio: (String, String) -> Unit,
    openCompose: () -> Unit,
    openCapsule: (Long) -> Unit,
    openInterests: () -> Unit,
) {
    var tab by rememberSaveable { mutableIntStateOf(initialTab) }
    BackHandler(enabled = tab != 0) { tab = 0 }
    Box(Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = tab,
            transitionSpec = {
                (fadeIn(Motion.fade()) + scaleIn(Motion.settle(), initialScale = 0.985f)) togetherWith fadeOut(Motion.fade(Motion.Quick))
            },
            label = "tabs",
        ) { t ->
            when (t) {
                0 -> TodayScreen(
                    openWord = { openWord(it, "today") }, openQuote = openQuote, openStudio = openStudio,
                    openPractice = openPractice, openCompose = openCompose, openCapsuleTab = { tab = 2 },
                )
                1 -> LibraryScreen(openWord = { openWord(it, "list") }, openQuote = openQuote, openPractice = openPractice)
                2 -> CapsuleHomeScreen(openCompose = openCompose, openCapsule = openCapsule)
                else -> MeScreen(openInterests = openInterests, openWord = { openWord(it, "list") }, openQuote = openQuote)
            }
        }
        LiquidNavBar(
            items = Tabs,
            selected = tab,
            onSelect = { tab = it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 18.dp, end = 18.dp, bottom = 12.dp),
        )
    }
}

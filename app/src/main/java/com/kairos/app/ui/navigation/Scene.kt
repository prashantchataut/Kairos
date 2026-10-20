package com.kairos.app.ui.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import com.kairos.app.ui.theme.Wash

/** Which washes the living background paints. Each screen sets its own; the paper re-wets between them. */
@Stable
class SceneController {
    var washes by mutableStateOf(listOf(Wash.Cerulean, Wash.Lilac, Wash.Peach, Wash.Mint, Wash.Butter))
}

val LocalScene = staticCompositionLocalOf { SceneController() }

@Composable
fun Scene(vararg washes: Wash) {
    val scene = LocalScene.current
    val list = washes.toList()
    LaunchedEffect(list) { scene.washes = list }
}

val LocalSharedScope = staticCompositionLocalOf<SharedTransitionScope?> { null }
val LocalNavAnimScope = staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

/** Shared-bounds morph between screens (e.g. a word card growing into its detail page). */
fun Modifier.sharedElement(key: String): Modifier = composed {
    val shared = LocalSharedScope.current
    val anim = LocalNavAnimScope.current
    if (shared == null || anim == null) this
    else with(shared) {
        this@composed.sharedBounds(
            rememberSharedContentState(key),
            animatedVisibilityScope = anim,
            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
        )
    }
}

fun Modifier.sharedText(key: String): Modifier = composed {
    val shared = LocalSharedScope.current
    val anim = LocalNavAnimScope.current
    if (shared == null || anim == null) this
    else with(shared) {
        this@composed.sharedBounds(
            rememberSharedContentState(key),
            animatedVisibilityScope = anim,
            resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(),
        )
    }
}

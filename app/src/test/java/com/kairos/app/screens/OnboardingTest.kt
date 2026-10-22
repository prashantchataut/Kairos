package com.kairos.app.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.kairos.app.data.prefs.ThemeMode
import com.kairos.app.ui.feature.onboarding.OnboardingScreen
import com.kairos.app.ui.theme.KairosTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Every onboarding step at rest, and the wake-up animation frame by frame. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h860dp-xxhdpi")
class OnboardingTest {
    @get:Rule val compose = createComposeRule()

    private fun step(n: Int) {
        compose.setContent { KairosTheme(ThemeMode.Light, calmMotion = true) { OnboardingScreen({}, startStep = n) } }
        repeat(6) { compose.mainClock.advanceTimeBy(400); compose.waitForIdle() }
        compose.onRoot().captureRoboImage("screenshots/onboarding/step_$n.png")
    }

    @Test fun s0() = step(0)
    @Test fun s1() = step(1)
    @Test fun s2() = step(2)
    @Test fun s3() = step(3)
    @Test fun s4() = step(4)
    @Test fun s5() = step(5)
    @Test fun s6() = step(6)
    @Test fun s7() = step(7)

    @Test fun wakeFrames() {
        compose.mainClock.autoAdvance = false
        compose.setContent { KairosTheme(ThemeMode.Light, calmMotion = false) { OnboardingScreen({}) } }
        var t = 0L
        listOf(0L, 450, 800, 1150, 1320, 1420, 1560, 1750, 2000, 2400).forEach { at ->
            compose.mainClock.advanceTimeBy(at - t); t = at
            compose.onRoot().captureRoboImage("screenshots/onboarding/wake_%04d.png".format(at))
        }
    }

    @Test fun giftsFrames() {
        compose.mainClock.autoAdvance = false
        compose.setContent { KairosTheme(ThemeMode.Light, calmMotion = false) { OnboardingScreen({}, startStep = 1) } }
        var t = 0L
        listOf(60L, 180, 300, 450, 700, 1200).forEach { at ->
            compose.mainClock.advanceTimeBy(at - t); t = at
            compose.onRoot().captureRoboImage("screenshots/onboarding/gifts_%04d.png".format(at))
        }
    }

    @Test fun finaleFrames() {
        compose.mainClock.autoAdvance = false
        compose.setContent { KairosTheme(ThemeMode.Light, calmMotion = false) { OnboardingScreen({}, startStep = 7) } }
        var t = 0L
        listOf(300L, 900, 1500, 2100, 2700, 3300, 3700, 4400).forEach { at ->
            compose.mainClock.advanceTimeBy(at - t); t = at
            compose.onRoot().captureRoboImage("screenshots/onboarding/finale_%04d.png".format(at))
        }
    }
}

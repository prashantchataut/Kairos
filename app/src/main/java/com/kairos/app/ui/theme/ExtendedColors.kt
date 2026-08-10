package com.kairos.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Kairos Design System - Extended Theme Colors
 *
 * CompositionLocals for feature-specific color systems that extend
 * beyond Material 3's color scheme. Each feature domain gets its own
 * color palette accessible via CompositionLocal, enabling:
 *
 * - Feature-specific theming without polluting the main color scheme
 * - Easy dark mode support via composition overrides
 * - Testability through explicit dependency injection
 * - Clean separation of concerns between feature color systems
 *
 * Usage:
 *   val havenColors = LocalHavenColors.current
 *   Box(color = havenColors.bubble)
 */

// =============================================================================
// HAVEN COLORS - Therapeutic chat feature
// =============================================================================

data class HavenColorPalette(
    val background: Color,
    val bubble: Color,
    val userBubble: Color,
    val text: Color,
    val accentRose: Color,
    val accentGold: Color
)

val LocalHavenColors = staticCompositionLocalOf {
    HavenColorPalette(
        background = HavenBackgroundLight,
        bubble = HavenBubbleLight,
        userBubble = HavenUserBubbleLight,
        text = HavenTextLight,
        accentRose = HavenAccentRose,
        accentGold = HavenAccentGold
    )
}

// =============================================================================
// STREAK COLORS - Gamification streak system
// =============================================================================

data class StreakColorPalette(
    val fire: Color,
    val warm: Color,
    val hot: Color,
    val week: Color,
    val month: Color,
    val quarter: Color,
    val glow: Color,
    val ember: Color,
    val inferno: Color,
    val blazing: Color,
    val cold: Color
)

val LocalStreakColors = staticCompositionLocalOf {
    StreakColorPalette(
        fire = StreakFire,
        warm = StreakWarm,
        hot = StreakHot,
        week = StreakWeek,
        month = StreakMonth,
        quarter = StreakQuarter,
        glow = StreakGlow,
        ember = StreakEmber,
        inferno = StreakInferno,
        blazing = StreakBlazing,
        cold = StreakCold
    )
}

// =============================================================================
// MOOD COLORS - Mood tracking system
// =============================================================================

data class MoodColorPalette(
    val happy: Color,
    val calm: Color,
    val anxious: Color,
    val sad: Color,
    val motivated: Color,
    val grateful: Color,
    val confused: Color,
    val excited: Color,
    val energetic: Color,
    val inspired: Color,
    val nostalgic: Color
)

val LocalMoodColors = staticCompositionLocalOf {
    MoodColorPalette(
        happy = MoodHappy,
        calm = MoodCalm,
        anxious = MoodAnxious,
        sad = MoodSad,
        motivated = MoodMotivated,
        grateful = MoodGrateful,
        confused = MoodConfused,
        excited = MoodExcited,
        energetic = MoodEnergetic,
        inspired = MoodInspired,
        nostalgic = MoodNostalgic
    )
}

// =============================================================================
// DARK THEME OVERRIDES
// =============================================================================

val LightHavenColors = HavenColorPalette(
    background = HavenBackgroundLight,
    bubble = HavenBubbleLight,
    userBubble = HavenUserBubbleLight,
    text = HavenTextLight,
    accentRose = HavenAccentRose,
    accentGold = HavenAccentGold
)

val LightStreakColors = StreakColorPalette(
    fire = StreakFire,
    warm = StreakWarm,
    hot = StreakHot,
    week = StreakWeek,
    month = StreakMonth,
    quarter = StreakQuarter,
    glow = StreakGlow,
    ember = StreakEmber,
    inferno = StreakInferno,
    blazing = StreakBlazing,
    cold = StreakCold
)

val LightMoodColors = MoodColorPalette(
    happy = MoodHappy,
    calm = MoodCalm,
    anxious = MoodAnxious,
    sad = MoodSad,
    motivated = MoodMotivated,
    grateful = MoodGrateful,
    confused = MoodConfused,
    excited = MoodExcited,
    energetic = MoodEnergetic,
    inspired = MoodInspired,
    nostalgic = MoodNostalgic
)

val DarkHavenColors = HavenColorPalette(
    background = HavenBackgroundDark,
    bubble = HavenBubbleDark,
    userBubble = HavenUserBubbleDark,
    text = HavenTextDark,
    accentRose = HavenAccentRose,
    accentGold = HavenAccentGold
)

// Dark variants: slightly brighter, tuned for ink-navy surfaces so they keep
// adequate contrast and legibility in dark mode.
val DarkStreakColors = StreakColorPalette(
    fire = StreakFire,
    warm = StreakWarm,
    hot = StreakHot,
    week = StreakWeek,
    month = StreakMonth,
    quarter = StreakQuarter,
    glow = Color(0xFFFFE0A3),
    ember = Color(0xFFFFB74D),
    inferno = Color(0xFFE64A19),
    blazing = Color(0xFFFF6E40),
    cold = Color(0xFF90CAF9)
)

val DarkMoodColors = MoodColorPalette(
    happy = Color(0xFFF2C14D),
    calm = Color(0xFF6FC3E8),
    anxious = Color(0xFFF0A06E),
    sad = Color(0xFF93A5C9),
    motivated = Color(0xFFF5B04B),
    grateful = Color(0xFFA3D39C),
    confused = Color(0xFFBCAED6),
    excited = Color(0xFFF08A7A),
    energetic = Color(0xFFF0A24A),
    inspired = Color(0xFF96A6EF),
    nostalgic = Color(0xFFD3A9BF)
)
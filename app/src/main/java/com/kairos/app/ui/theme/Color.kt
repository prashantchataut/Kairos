package com.kairos.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Kairos color system — "Moment Blue".
 *
 * A vivid royal-blue brand anchor with periwinkle, soft cyan, and mint
 * supporting tones. Light mode is pearl with a blue tint; dark mode is
 * charcoal ink-navy with luminous blue highlights. All legacy alias names are
 * preserved for binary/source compatibility.
 */

// =============================================================================
// BRAND COLORS - Core Identity
// =============================================================================

val KairosMineralIndigo = Color(0xFF2E5BFF)   // Royal/electric blue — the brand anchor
val KairosClay = Color(0xFFE86A5E)            // Warm coral — emotional accent
val KairosVerdigris = Color(0xFF2E9E6B)       // Mint — success/progress
val KairosPeriwinkle = Color(0xFF8FA6FF)      // Periwinkle — dark-mode primary
val KairosSoftClay = Color(0xFFE8947F)        // Soft coral for dark secondary
val KairosSeaGlass = Color(0xFF7BD9A5)        // Pale mint — dark success

val KairosIndigoContainerLight = Color(0xFFDDE5FF)   // Blue wash
val KairosOnIndigoContainerLight = Color(0xFF0A2A75) // Deep navy on wash
val KairosClayContainerLight = Color(0xFFFFE0DC)     // Coral wash
val KairosOnClayContainerLight = Color(0xFF6E241C)
val KairosVerdigrisContainerLight = Color(0xFFD5F5E2) // Mint wash
val KairosOnVerdigrisContainerLight = Color(0xFF0F3B28)

val KairosIndigoContainerDark = Color(0xFF1B2F6E)     // Night blue wash
val KairosOnIndigoContainerDark = Color(0xFFDDE5FF)
val KairosClayContainerDark = Color(0xFF6E2E26)
val KairosOnClayContainerDark = Color(0xFFFFE0DC)
val KairosVerdigrisContainerDark = Color(0xFF0F3B28)
val KairosOnVerdigrisContainerDark = Color(0xFF7BD9A5)

val KairosForestGreen = KairosMineralIndigo
val KairosWarmAmber = KairosClay

val KairosPrimary = KairosForestGreen
val KairosSecondary = KairosWarmAmber

// =============================================================================
// LIGHT THEME COLORS — Pearl with a blue tint
// =============================================================================

val KairosBackgroundLight = Color(0xFFF5F7FC)          // Pearl blue-tinted ground
val KairosSurfaceLight = Color(0xFFFBFCFE)             // Pearl surface
val KairosSurfaceVariantLight = Color(0xFFEDF1F9)      // Pale blue-gray
val KairosSurfaceContainerLight = Color(0xFFE7ECF7)    // Blue-tinted container

val KairosTextPrimaryLight = Color(0xFF101828)         // Ink navy
val KairosTextSecondaryLight = Color(0xFF5A6478)       // Soft slate
val KairosTextTertiaryLight = Color(0xFF65708A)        // Faint slate (4.6:1 on pearl)
val KairosTextOnPrimaryLight = Color(0xFFFFFFFF)       // White on blue
val KairosTextOnAccentLight = Color(0xFFFFFFFF)

val KairosOutlineLight = Color(0xFFD8DFEC)             // Hairline
val KairosDividerLight = Color(0xFFE3E8F2)             // Softer hairline

// =============================================================================
// DARK THEME COLORS — Charcoal ink-navy
// =============================================================================

val KairosBackgroundDark = Color(0xFF0B0E15)             // Ink navy ground
val KairosSurfaceDark = Color(0xFF11161F)               // Elevated card
val KairosSurfaceVariantDark = Color(0xFF1A2130)        // Soft surface
val KairosSurfaceContainerDark = Color(0xFF1E2636)      // Container

val KairosTextPrimaryDark = Color(0xFFE9EDF6)            // Night ink
val KairosTextSecondaryDark = Color(0xFF9AA5BE)           // Night soft
val KairosTextTertiaryDark = Color(0xFF7C87A3)           // Night faint (5.4:1 on ink)
val KairosTextOnPrimaryDark = Color(0xFF0A1E54)

val KairosOutlineDark = Color(0xFF2A3345)                // Night hairline
val KairosDividerDark = Color(0xFF232C3D)                // Night soft hairline

// =============================================================================
// SEMANTIC COLORS
// =============================================================================

val KairosError = Color(0xFFD64545)                    // Clear red
val KairosSuccess = KairosVerdigris                    // Mint
val KairosWarning = Color(0xFFE8960C)                  // Amber
val KairosInfo = Color(0xFF2E7CF6)                     // Info blue

val KairosOnError = Color(0xFFFFFFFF)
val KairosOnSuccess = Color(0xFFFFFFFF)
val KairosOnWarning = Color(0xFF000000)
val KairosOnInfo = Color(0xFFFFFFFF)

// Containers (Light)
val KairosErrorContainer = Color(0xFFFFE0DE)
val KairosSuccessContainer = Color(0xFFD5F5E2)
val KairosWarningContainer = Color(0xFFFFF1D6)
val KairosInfoContainer = Color(0xFFDCEBFF)

// Containers (Dark)
val KairosErrorContainerDark = Color(0xFF5C2522)
val KairosSuccessContainerDark = Color(0xFF0F3B28)
val KairosWarningContainerDark = Color(0xFF4A3208)
val KairosInfoContainerDark = Color(0xFF0F3A70)

// =============================================================================
// LEGACY / COMPATIBILITY COLORS (Mapped to New System)
// =============================================================================

val KairosAccentGreen = KairosForestGreen
val KairosAccentGreenLight = Color(0xFF5B7FFF)
val KairosAccentGreenDark = Color(0xFF1B3FBF)
val KairosDeepBlue = Color(0xFF0F2C8F)                   // Deep navy — base of the hero-card gradient

val KairosAccent = KairosPrimary
val KairosGreen = KairosPrimary
val KairosAccentBlue = KairosInfo

// Surfaces
val KairosSurface = KairosSurfaceLight
val KairosBackground = KairosBackgroundLight
val KairosOnPrimary = KairosTextOnPrimaryLight
val KairosOnSecondary = Color(0xFF000000)
val KairosPrimaryContainer = KairosIndigoContainerLight
val KairosTertiary = KairosTextSecondaryLight
val KairosOnTertiary = Color(0xFFFFFFFF)
val KairosTertiaryContainer = KairosSurfaceVariantLight
val KairosTextSecondary = KairosTextSecondaryLight

val KairosPrimaryDark = KairosPeriwinkle
val KairosOnPrimaryDark = Color(0xFF0A1E54)
val KairosPrimaryContainerDark = KairosIndigoContainerDark
val KairosSecondaryDark = KairosSoftClay
val KairosOnSecondaryDark = Color(0xFF000000)
val KairosSecondaryContainerDark = KairosClayContainerDark
val KairosTertiaryDark = KairosTextSecondaryDark
val KairosOnTertiaryDark = Color(0xFF000000)
val KairosTertiaryContainerDark = KairosSurfaceVariantDark



// Moods — distinct but harmonized with the blue world
val MoodHappy = Color(0xFFF2B33D)
val MoodCalm = Color(0xFF4FB3D9)
val MoodAnxious = Color(0xFFE88B5A)
val MoodSad = Color(0xFF7D8FB3)
val MoodMotivated = Color(0xFFF2A02E)
val MoodGrateful = Color(0xFF8FC48A)
val MoodConfused = Color(0xFFA99BC8)
val MoodExcited = Color(0xFFE86A5E)
val MoodEnergetic = Color(0xFFE8932E)
val MoodInspired = Color(0xFF7C8FE0)
val MoodNostalgic = Color(0xFFC79BB0)

// Haven — Warm Reddish-Cream Palette (slightly warmer for coziness)
val HavenBackgroundLight = Color(0xFFFFF8F4)              // Warmer cream
val HavenBubbleLight = Color(0xFFF5DDD0)                 // Soft blush/rose
val HavenUserBubbleLight = Color(0xFFFFF0EB)              // Lighter cream user
val HavenTextLight = Color(0xFF2D2424)                   // Warm dark text
val HavenBackgroundDark = Color(0xFF1A1214)              // Deep warm dark
val HavenBubbleDark = Color(0xFF3D2A2A)                   // Dark rose bubble
val HavenUserBubbleDark = Color(0xFF2A1E1E)               // Dark user bubble
val HavenTextDark = Color(0xFFF0EAE2)                    // Warm light text
val HavenAccentRose = Color(0xFFD4736B)                   // Muted dusty rose
val HavenAccentGold = Color(0xFFD4A574)                   // Warm caramel gold

// Scrim
val Scrim = Color(0x52000000)

// Leaderboard / Gamification
val LeaderboardGold = Color(0xFFFFD700)
val LeaderboardSilver = Color(0xFFC0C0C0)
val LeaderboardBronze = Color(0xFFCD7F32)
val LeaderboardGoldLight = Color(0xFFFFE57F)
val LeaderboardGoldDark = Color(0xFFC7A500)
val LeaderboardSilverLight = Color(0xFFE0E0E0)
val LeaderboardSilverDark = Color(0xFF9E9E9E)
val LeaderboardBronzeLight = Color(0xFFFFCCBC)
val LeaderboardBronzeDark = Color(0xFF8D6E63)

val GoldTier = LeaderboardGold
val SilverTier = LeaderboardSilver
val BronzeTier = LeaderboardBronze
val PlatinumTier = Color(0xFFE1F5FE)

// Streak
val StreakColor = Color(0xFFE65100)
val StreakFire = Color(0xFFE65100)
val StreakWarm = Color(0xFFFF9800)
val StreakHot = Color(0xFFFF5722)
val StreakWeek = Color(0xFFFFA726)
val StreakMonth = Color(0xFFFF7043)
val StreakQuarter = Color(0xFFE64A19)
val StreakGlow = Color(0xFFFFD180)
val StreakEmber = Color(0xFFFFAB40)
val StreakInferno = Color(0xFFBF360C)
val StreakBlazing = Color(0xFFFF3D00)
val StreakCold = Color(0xFF90CAF9)

// Milestones
val StreakWeekMilestone = StreakWeek
val StreakMonthMilestone = StreakMonth
val StreakMilestone7 = StreakWeekMilestone
val StreakMilestone30 = StreakMonthMilestone
val StreakMilestone100 = StreakQuarter
val StreakMilestone365 = Color(0xFFD84315)

// Support
val SupportBoost = KairosSuccess
val SupportRespect = KairosInfo
val SupportEncourage = KairosWarmAmber

// Notifications
val NotificationAchievement = Color(0xFF9C27B0)
val NotificationCelebration = Color(0xFFFFEB3B)
val NotificationMotivation = Color(0xFFFF9800)
val NotificationPrimary = KairosPrimary
val NotificationReminder = KairosInfo
val NotificationStreak = StreakFire
val NotificationSuccess = KairosSuccess

// Gradients
object KairosGradients {
    val primaryGradient = listOf(KairosAccentGreenLight, KairosAccentGreen, KairosAccentGreenDark)
    val goldGradient = listOf(LeaderboardGoldLight, LeaderboardGold, LeaderboardGoldDark)
    val streakNotificationGradient = listOf(StreakWarm, StreakHot)
    val celebrationGradient = listOf(NotificationCelebration, NotificationAchievement)
    val achievementGradient = listOf(NotificationAchievement, KairosPrimary)
    val motivationGradient = listOf(NotificationMotivation, StreakWarm)
    val oceanGradient = listOf(KairosInfo, MoodCalm)
    val growthGradient = listOf(KairosSuccess, KairosAccentGreen)
    val serenityGradient = listOf(MoodCalm, MoodGrateful)
    val goldBanner = goldGradient
    val silverBanner = listOf(LeaderboardSilverLight, LeaderboardSilver, LeaderboardSilverDark)
    val bronzeBanner = listOf(LeaderboardBronzeLight, LeaderboardBronze, LeaderboardBronzeDark)

    // Gradient Aliases
    val streakGradient = streakNotificationGradient
}

// Activity Pulse
val ActivityPulseBackground = Color(0xFFE0F2F1)
val ActivityPulseBackgroundLight = Color(0xFFE0F2F1)

// Journal History
val JournalHistoryAccent = KairosPrimary
val JournalHistoryCardLight = KairosSurfaceLight
val JournalHistoryCardDark = KairosSurfaceDark
val JournalHistoryTextPrimaryLight = KairosTextPrimaryLight
val JournalHistoryTextPrimaryDark = KairosTextPrimaryDark
val JournalHistoryTextSecondaryLight = KairosTextSecondaryLight
val JournalHistoryTextSecondaryDark = KairosTextSecondaryDark
val JournalHistoryDividerLight = KairosDividerLight
val JournalHistoryDividerDark = KairosDividerDark

// Rarity
val RarityCommon = Color(0xFF9E9E9E)
val RarityUncommon = Color(0xFF66BB6A)
val RarityRare = Color(0xFF42A5F5)
val RarityEpic = Color(0xFFAB47BC)
val RarityLegendary = Color(0xFFFFD700)
val RarityMythic = Color(0xFFFF1744)

// Achievements
val AchievementUnlocked = KairosSuccess

// Premium
val KairosPremiumViolet = Color(0xFF6C5CE7)
val KairosPremiumVioletContainer = Color(0xFFE2DFFF)
val KairosPremiumVioletDark = Color(0xFF4F46B3)
val KairosPremiumVioletLight = Color(0xFF9D8FFF)

// Time Capsule
val TimeCapsuleBackgroundLight = KairosBackgroundLight
val TimeCapsuleBackgroundDark = KairosBackgroundDark
val TimeCapsuleTitleTextLight = KairosTextPrimaryLight
val TimeCapsuleTitleTextDark = KairosTextPrimaryDark
val TimeCapsuleDiscardTextLight = KairosTextSecondaryLight
val TimeCapsuleDiscardTextDark = KairosTextSecondaryDark
val TimeCapsulePlaceholderLight = KairosTextSecondaryLight
val TimeCapsulePlaceholderDark = KairosTextSecondaryDark
val TimeCapsuleActiveTextLight = KairosTextPrimaryLight
val TimeCapsuleActiveTextDark = KairosTextPrimaryDark
val TimeCapsuleMultimediaIconLight = KairosTextSecondaryLight
val TimeCapsuleMultimediaIconDark = KairosTextSecondaryDark
val TimeCapsuleAttachTextLight = KairosTextSecondaryLight
val TimeCapsuleAttachTextDark = KairosTextSecondaryDark
val TimeCapsuleDividerLight = KairosDividerLight
val TimeCapsuleDividerDark = KairosDividerDark
val TimeCapsuleSectionTitleLight = KairosTextPrimaryLight
val TimeCapsuleSectionTitleDark = KairosTextPrimaryDark
val TimeCapsuleInactiveTagBgLight = KairosSurfaceVariantLight
val TimeCapsuleInactiveTagBgDark = KairosSurfaceVariantDark
val TimeCapsuleInactiveTagTextLight = KairosTextSecondaryLight
val TimeCapsuleInactiveTagTextDark = KairosTextSecondaryDark
val TimeCapsuleButtonTextLight = Color.White
val TimeCapsuleButtonTextDark = Color.White
val TimeCapsuleAccent = KairosPrimary
val TimeCapsuleIconLight = KairosTextSecondaryLight
val TimeCapsuleIconDark = KairosTextSecondaryDark
val TimeCapsuleTabContainerLight = KairosSurfaceVariantLight
val TimeCapsuleTabContainerDark = KairosSurfaceVariantDark
val TimeCapsuleActiveTabTextLight = KairosTextPrimaryLight
val TimeCapsuleActiveTabTextDark = KairosTextPrimaryDark
val TimeCapsuleEmptyCircleBgLight = KairosSurfaceVariantLight
val TimeCapsuleEmptyCircleBgDark = KairosSurfaceVariantDark
val TimeCapsuleDashedCircleLight = KairosDividerLight
val TimeCapsuleDashedCircleDark = KairosDividerDark

// Daily Wisdom
val WordOfDayColor = Color(0xFFFFC107)
val IdiomPurple = Color(0xFF9C27B0)
val ProverbTeal = Color(0xFF009688)
val SeedGold = Color(0xFFFFD740)
val WisdomPerspective = Color(0xFF7E57C2)

// Challenges
val ChallengeActive = KairosSuccess
val ChallengeCompleted = LeaderboardGold

// Skills
val ClaritySkillColor = Color(0xFF29B6F6)
val DisciplineSkillColor = Color(0xFFAB47BC)
val CourageSkillColor = Color(0xFFFF7043)

// Misc
val KairosOutline = KairosOutlineLight
val KairosOutlineVariant = KairosDividerLight
val KairosOutlineVariantDark = KairosDividerDark
val KairosSurfaceElevated = KairosSurfaceLight
val KairosSurfaceDim = KairosSurfaceVariantLight
val KairosSurfaceElevatedDark = KairosSurfaceDark
val KairosSurfaceDimDark = KairosSurfaceVariantDark
val KairosInverseSurface = Color(0xFF303030)
val KairosInverseOnSurface = Color(0xFFF5F5F5)
val KairosInversePrimary = Color(0xFF81C784)
val BloomReady = KairosForestGreen
val BloomGrowing = Color(0xFF8BC34A)
val SeedDormant = Color(0xFFBDBDBD)
val InteractiveHoverLight = Color(0x0A000000)
val InteractivePressedLight = Color(0x1F000000)
val InteractiveHoverDark = Color(0x0AFFFFFF)
val InteractivePressedDark = Color(0x1FFFFFFF)
val InteractiveFocus = KairosForestGreen.copy(alpha = 0.5f)
val WrappedPurple1 = Color(0xFF6B5CE7)
val WrappedPurple2 = Color(0xFF8B7EF0)
val WrappedPink1 = Color(0xFFE91E63)
val WrappedPink2 = Color(0xFFF06292)
val SuccessGreen = KairosSuccess
val ErrorRed = KairosError
val WarningAmber = KairosWarning
val InfoBlue = KairosInfo
val ProfileAvatarRing = KairosForestGreen
val FutureCategoryGoal = KairosForestGreen
val FutureCategoryMotivation = KairosWarmAmber
val FutureMessageArrived = Color(0xFFFFD700)
val JournalAccentGreen = KairosForestGreen
val XpBarFill = KairosAccentGreen
val XpBarGlow = KairosAccentGreen.copy(alpha = 0.5f)
val LevelUpGlow = KairosAccentGreen

// Time Capsule Colors
val TimeCapsuleTextPrimaryLight = Color(0xFF1A1C1E)
val TimeCapsuleTextSecondaryLight = Color(0xFF42474E)
val TimeCapsuleTextPrimaryDark = Color(0xFFE2E2E6)
val TimeCapsuleTextSecondaryDark = Color(0xFFC4C7C5)

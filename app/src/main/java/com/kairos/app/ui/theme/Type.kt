package com.kairos.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.kairos.app.R

/**
 * Kairos Design System - Typography (Paper & Ink)
 *
 * A dual-family typography system using:
 * - Bundled Lora (regular/italic/medium/semibold/bold/bold-italic) for
 *   display, headline, and reflective content — the serif voice of the book.
 * - Native Android sans-serif for interface elements
 *
 * Design Principles:
 * - Serif for display/headline and literary content, sans for UI chrome
 * - Tight tracking on display sizes, generous line heights for reading
 * - WCAG AA contrast compliance for all text sizes
 */

/**
 * Bundled Lora family — the display and reflective voice of Kairos.
 * R.font resources ship with the app (see res/font/lora_*.ttf).
 */
val LoraFamily: FontFamily = FontFamily(
    Font(R.font.lora_regular, FontWeight.Normal),
    Font(R.font.lora_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.lora_medium, FontWeight.Medium),
    Font(R.font.lora_semibold, FontWeight.SemiBold),
    Font(R.font.lora_bold, FontWeight.Bold),
    Font(R.font.lora_bold_italic, FontWeight.Bold, FontStyle.Italic)
)

/** Serif accent family used throughout reflective surfaces. */
val SerifFamily: FontFamily = LoraFamily

/** Native Android sans-serif for interface text and dense controls. */
val KairosSansFamily: FontFamily = FontFamily.SansSerif

/** Compatibility alias for legacy UI styles. */
val PoppinsFamily: FontFamily = KairosSansFamily

/**
 * Main Typography configuration following Material Design 3 guidelines
 * with the native Android sans-serif family for reliable rendering.
 */
val KairosTypography = Typography(
    // ==========================================================================
    // DISPLAY STYLES - Bold modern sans (the Moment Blue display voice)
    // ==========================================================================
    displayLarge = TextStyle(
        fontFamily = KairosSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 46.sp,
        letterSpacing = (-0.6).sp
    ),
    displayMedium = TextStyle(
        fontFamily = KairosSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.4).sp
    ),
    displaySmall = TextStyle(
        fontFamily = KairosSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.2).sp
    ),

    // ==========================================================================
    // HEADLINE STYLES - Bold sans section headers
    // ==========================================================================
    headlineLarge = TextStyle(
        fontFamily = KairosSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.2).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = KairosSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.1).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = KairosSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.sp
    ),

    // ==========================================================================
    // TITLE STYLES - For card titles, list items, and UI element headers
    // ==========================================================================
    titleLarge = TextStyle(
        fontFamily = KairosSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = KairosSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = KairosSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // ==========================================================================
    // BODY STYLES - For main content and readable text blocks
    // ==========================================================================
    bodyLarge = TextStyle(
        fontFamily = KairosSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.3.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = KairosSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.2.sp
    ),
    bodySmall = TextStyle(
        fontFamily = KairosSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.3.sp
    ),

    // ==========================================================================
    // LABEL STYLES - For buttons, chips, tabs, and small UI elements
    // ==========================================================================
    labelLarge = TextStyle(
        fontFamily = KairosSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = KairosSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelSmall = TextStyle(
        fontFamily = KairosSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )
)

// =============================================================================
// EXTENDED TYPOGRAPHY - Custom text styles for special use cases
// Serif for reflective content, native sans-serif for UI
// =============================================================================

/**
 * Quote style - For displaying wisdom quotes and inspirational text
 */
val QuoteTextStyle = TextStyle(
    fontFamily = SerifFamily,
    fontWeight = FontWeight.Light,
    fontSize = 18.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.2.sp,
    fontStyle = FontStyle.Italic
)

/**
 * Number display - For stats, counters, and numerical highlights
 */
val NumberDisplayStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    letterSpacing = (-0.5).sp
)

/**
 * Caption style - For timestamps, metadata, and secondary information
 */
val CaptionTextStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.4.sp
)

/**
 * Button text - Emphasized for interactive elements
 * Note: Uses Regular weight as per design spec (not bold)
 */
val ButtonTextStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Normal,  // Regular weight per design spec
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.5.sp
)

/**
 * Overline style - For category labels and small headers
 */
val OverlineTextStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 10.sp,
    lineHeight = 14.sp,
    letterSpacing = 1.5.sp
)

// =============================================================================
// WISDOM TYPOGRAPHY - Serif accent for reflective content
// Primary quotes use serif; attribution and metadata stay sans-serif
// =============================================================================

/**
 * Hero wisdom quote - For Buddha's Thought and featured wisdom cards
 */
val WisdomHeroStyle = TextStyle(
    fontFamily = SerifFamily,
    fontWeight = FontWeight.Light,
    fontSize = 24.sp,
    lineHeight = 34.sp,
    letterSpacing = 0.2.sp,
    fontStyle = FontStyle.Italic
)

/**
 * Large wisdom quote - For quote of the day and prominent wisdom
 */
val WisdomLargeStyle = TextStyle(
    fontFamily = SerifFamily,
    fontWeight = FontWeight.Light,
    fontSize = 20.sp,
    lineHeight = 30.sp,
    letterSpacing = 0.15.sp,
    fontStyle = FontStyle.Italic
)

/**
 * Medium wisdom text - For proverbs, idioms, and journal prompts
 */
val WisdomMediumStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 18.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.1.sp
)

/**
 * Small wisdom text - For secondary wisdom content and attributions
 */
val WisdomSmallStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.1.sp
)

/**
 * Wisdom attribution - For author names and sources
 */
val WisdomAttributionStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.5.sp
)

/**
 * Wisdom caption - For small wisdom-related metadata
 */
val WisdomCaptionStyle = TextStyle(
    fontFamily = SerifFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.4.sp,
    fontStyle = FontStyle.Italic
)

/**
 * Journal prompt style - For reflective questions and prompts
 */
val JournalPromptStyle = TextStyle(
    fontFamily = SerifFamily,
    fontWeight = FontWeight.Light,
    fontSize = 18.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.1.sp,
    fontStyle = FontStyle.Italic
)

/**
 * Stoic maxim style - For concise philosophical statements
 */
val StoicMaximStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.3.sp
)

// =============================================================================
// GAMIFICATION TYPOGRAPHY - Stats, Badges, Streaks, XP
// =============================================================================

/**
 * Stat number style - For large stat displays (total entries, words written)
 */
val StatNumberStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    letterSpacing = (-0.5).sp
)

/**
 * Large stat number - For hero stats and major milestones
 */
val StatNumberLargeStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 48.sp,
    lineHeight = 56.sp,
    letterSpacing = (-0.5).sp
)

/**
 * Badge title style - For achievement and badge names
 */
val BadgeTitleStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.5.sp
)

/**
 * Badge description style - For achievement descriptions
 */
val BadgeDescriptionStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.25.sp
)

/**
 * Streak counter style - For displaying streak numbers prominently
 */
val StreakCounterStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 36.sp,
    lineHeight = 44.sp,
    letterSpacing = (-0.5).sp
)

/**
 * Streak label style - For "days" or "streak" labels
 */
val StreakLabelStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp
)

/**
 * XP text style - For experience points display
 */
val XpTextStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp
)

/**
 * Level text style - For level display
 */
val LevelTextStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 20.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp
)

/**
 * Leaderboard rank style - For position numbers on leaderboard
 */
val LeaderboardRankStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 24.sp,
    lineHeight = 28.sp,
    letterSpacing = (-0.5).sp
)

/**
 * Leaderboard name style - For user names on leaderboard
 */
val LeaderboardNameStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp
)

/**
 * Leaderboard points style - For point displays
 */
val LeaderboardPointsStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp
)

/**
 * Progress percentage style - For showing progress percentages
 */
val ProgressPercentageStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.25.sp
)

// =============================================================================
// NOTIFICATION TYPOGRAPHY - For push notifications and in-app messages
// =============================================================================

/**
 * Notification title style - Bold and attention-grabbing
 */
val NotificationTitleStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.1.sp
)

/**
 * Notification body style - Clear and readable
 */
val NotificationBodyStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.2.sp
)

// =============================================================================
// CARD TYPOGRAPHY - For various card types
// =============================================================================

/**
 * Card title style - For card headers
 */
val CardTitleStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp
)

/**
 * Card subtitle style - For card secondary headers
 */
val CardSubtitleStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp
)

/**
 * Card body style - For card content
 */
val CardBodyStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.2.sp
)

/**
 * Card caption style - For timestamps and metadata on cards
 */
val CardCaptionStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.4.sp
)

// =============================================================================
// ONBOARDING SPECIFIC TYPOGRAPHY
// =============================================================================

/**
 * Onboarding title - Large, bold, attention-grabbing
 */
val OnboardingTitleStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 36.sp,
    letterSpacing = (-0.25).sp
)

/**
 * Onboarding subtitle - Secondary explanation text
 */
val OnboardingSubtitleStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.1.sp
)

/**
 * Onboarding CTA button text
 */
val OnboardingButtonStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Normal,  // Regular weight per design spec
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp
)

// =============================================================================
// TIME CAPSULE SPECIFIC TYPOGRAPHY
// =============================================================================

/**
 * Time capsule section header
 */
val TimeCapsuleSectionStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 1.0.sp
)

/**
 * Time capsule tag text
 */
val TimeCapsuleTagStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp
)

// =============================================================================
// PROFILE SPECIFIC TYPOGRAPHY
// =============================================================================

/**
 * Profile display name
 */
val ProfileNameStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 24.sp,
    lineHeight = 32.sp,
    letterSpacing = 0.sp
)

/**
 * Profile title/badge text
 */
val ProfileTitleStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.25.sp
)

// =============================================================================
// HOME SCREEN SPECIFIC TYPOGRAPHY
// =============================================================================

/**
 * Home greeting text
 */
val HomeGreetingStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 24.sp,
    lineHeight = 32.sp,
    letterSpacing = 0.sp
)

/**
 * Quick action tile label
 */
val QuickActionLabelStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.25.sp
)

// =============================================================================
// STATS SCREEN SPECIFIC TYPOGRAPHY
// =============================================================================

/**
 * Stats hero number (very large)
 */
val StatsHeroNumberStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 56.sp,
    lineHeight = 64.sp,
    letterSpacing = (-1.0).sp
)

/**
 * Stats label
 */
val StatsLabelStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp
)

// =============================================================================
// MONTHLY LETTER TYPOGRAPHY - Beautiful, letter-like reading experience
// =============================================================================

/**
 * Letter greeting - warm, personal opening
 */
val LetterGreetingStyle = TextStyle(
    fontFamily = LoraFamily,
    fontWeight = FontWeight.Light,
    fontSize = 20.sp,
    lineHeight = 32.sp,
    letterSpacing = 0.1.sp,
    fontStyle = FontStyle.Italic
)

/**
 * Letter body text - comfortable reading for letter content
 */
val LetterBodyStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.2.sp
)

/**
 * Letter section header - distinguishes different parts of the letter
 */
val LetterSectionHeaderStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 1.0.sp
)

/**
 * Letter highlight quote - for featured quotes from user's entries
 */
val LetterQuoteStyle = TextStyle(
    fontFamily = LoraFamily,
    fontWeight = FontWeight.Light,
    fontSize = 18.sp,
    lineHeight = 30.sp,
    letterSpacing = 0.15.sp,
    fontStyle = FontStyle.Italic
)

/**
 * Letter closing - warm, personal sign-off
 */
val LetterClosingStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.2.sp
)

/**
 * Letter metadata - dates, stats presented elegantly
 */
val LetterMetadataStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.4.sp
)

// =============================================================================
// HAVEN TYPOGRAPHY - "Anti-AI" Handwritten Feel
// =============================================================================

/**
 * Haven Message Style - Serif font for organic, human feel
 * Uses default Serif as a fallback for a handwritten aesthetic
 */
val HavenMessageStyle = TextStyle(
    fontFamily = LoraFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.2.sp
)

/**
 * Haven User Input Style - Clean Sans-Serif for clarity
 */
val HavenInputStyle = TextStyle(
    fontFamily = KairosSansFamily,    // Keep user input clean and modern
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.1.sp
)

/**
 * Letter title - month and year display
 */
val LetterTitleStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 24.sp,
    lineHeight = 32.sp,
    letterSpacing = 0.sp
)

/**
 * Letter stat number - for presenting numbers in letter
 */
val LetterStatNumberStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 36.sp,
    letterSpacing = (-0.5).sp
)

/**
 * Letter stat label - for stat descriptions in letter
 */
val LetterStatLabelStyle = TextStyle(
    fontFamily = KairosSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.3.sp
)


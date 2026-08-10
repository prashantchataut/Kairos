package com.kairos.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Kairos Design System - Premium Design Tokens
 *
 * A comprehensive token system ensuring visual consistency across the entire application.
 * These tokens represent the foundational design decisions that create
 * a cohesive, premium user experience.
 *
 * Usage:
 * ```kotlin
 * Box(
 *     modifier = Modifier
 *         .padding(KairosTokens.Spacing.lg)
 *         .clip(RoundedCornerShape(KairosTokens.Radius.md))
 *         .shadow(KairosTokens.Elevation.sm)
 * )
 * ```
 */
object KairosTokens {

    // =========================================================================
    // SPACING SYSTEM - Based on 4dp grid
    // =========================================================================

    /**
     * Spacing tokens for margins, paddings, and gaps.
     * Built on a 4dp base unit for consistent rhythm.
     */
    object Spacing {
        val xxs = 2.dp      // Micro spacing for dense elements
        val xs = 4.dp       // Extra small for tight gaps
        val sm = 8.dp       // Small padding/margin
        val md = 12.dp      // Medium - comfortable spacing
        val lg = 16.dp      // Large - standard content padding
        val xl = 20.dp      // Extra large
        val xxl = 24.dp     // Section spacing
        val xxxl = 32.dp    // Major section breaks
        val huge = 48.dp    // Hero section spacing
        val massive = 64.dp // Full-screen hero elements

        // Semantic spacing
        val cardPadding = lg
        val listItemSpacing = sm
        val sectionSpacing = xxl
        val screenPadding = lg
        val buttonContentPadding = md
        val iconTextGap = sm
        val chipContentPadding = sm
    }

    // =========================================================================
    // CORNER RADIUS SYSTEM
    // =========================================================================

    /**
     * Corner radius tokens for consistent rounded corners.
     * Follows Material 3 shape scale with premium refinements.
     */
    object Radius {
        val xs = 4.dp       // Subtle rounding
        val sm = 8.dp       // Small components (chips, badges)
        val md = 12.dp      // Medium cards, inputs
        val lg = 16.dp      // Standard cards
        val xl = 20.dp      // Prominent cards
        val xxl = 24.dp     // Bottom sheets, dialogs
        val full = 100.dp   // Pills, avatars, FABs

        // Semantic radius
        val card = lg
        val button = sm
        val input = sm
        val chip = full
        val badge = full
        val dialog = xxl
        val bottomSheet = xxl
        val avatar = full
    }

    // =========================================================================
    // ELEVATION SYSTEM
    // =========================================================================

    /**
     * Elevation tokens for shadow depth.
     * Uses subtle shadows for a refined, premium appearance.
     */
    object Elevation {
        val none = 0.dp     // Flat elements
        val xs = 1.dp       // Subtle lift
        val sm = 2.dp       // Standard cards
        val md = 4.dp       // Elevated cards, FABs
        val lg = 8.dp       // Modals, dialogs
        val xl = 12.dp      // Prominent overlays
        val xxl = 16.dp     // Top-level surfaces

        // Semantic elevation
        val card = sm
        val cardElevated = md
        val fab = md
        val dialog = lg
        val bottomSheet = xl
        val dropdown = md
        val tooltip = sm
    }

    // =========================================================================
    // TOUCH TARGETS - Accessibility Compliant
    // =========================================================================

    /**
     * Touch target sizes ensuring accessibility compliance.
     * Follows WCAG 2.1 Level AA guidelines.
     */
    object Touch {
        val minimum = 48.dp      // WCAG minimum touch target
        val comfortable = 56.dp  // Comfortable tapping
        val spacious = 64.dp     // Hero action buttons

        // Component-specific targets
        val iconButton = minimum
        val listItem = comfortable
        val bottomNavItem = comfortable
        val fab = comfortable
    }

    // =========================================================================
    // ICON SIZE SYSTEM
    // =========================================================================

    /**
     * Standard icon sizes for visual hierarchy.
     */
    object IconSize {
        val xs = 16.dp      // Inline icons, badges
        val sm = 20.dp      // Small icons
        val md = 24.dp      // Default Material icon size
        val lg = 32.dp      // Prominent icons
        val xl = 48.dp      // Hero icons, achievements
        val xxl = 64.dp     // Featured icons
        val hero = 80.dp    // Empty state, onboarding

        // Semantic sizes
        val navigation = md
        val button = md
        val listItem = md
        val achievement = xl
        val emptyState = hero
    }

    // =========================================================================
    // ANIMATION DURATIONS
    // =========================================================================

    /**
     * Animation duration values in milliseconds.
     * Carefully tuned for smooth, premium feel at 60fps.
     */
    object Animation {
        const val instant = 50       // Immediate feedback
        const val fast = 150         // Quick micro-interactions
        const val normal = 300       // Standard transitions
        const val slow = 500         // Noticeable transitions
        const val verySlow = 800     // Dramatic reveals
        const val dramatic = 1200    // Celebration animations

        // Semantic durations
        const val buttonPress = fast
        const val cardHover = fast
        const val pageTransition = normal
        const val modalAppear = normal
        const val listStagger = 50           // Per-item delay
        const val achievementReveal = slow
        const val celebrationBurst = dramatic
        const val breathingCycle = 4000      // Meditation breathing
        const val glowPulse = 2000           // Ambient glow effect
    }

    // =========================================================================
    // CONTENT WIDTH CONSTRAINTS
    // =========================================================================

    /**
     * Maximum content widths for optimal readability.
     */
    object ContentWidth {
        val card = 160.dp           // Compact cards
        val cardWide = 200.dp       // Wide cards
        val dialogNarrow = 280.dp   // Narrow dialogs
        val dialog = 340.dp         // Standard dialogs
        val dialogWide = 400.dp     // Wide dialogs
        val formInput = 320.dp      // Form fields
        val readableText = 600.dp   // Optimal reading width
    }

    // =========================================================================
    // OPACITY VALUES
    // =========================================================================

    /**
     * Opacity values for various states and overlays.
     */
    object Opacity {
        const val disabled = 0.38f  // Disabled state
        const val medium = 0.60f    // Secondary content
        const val high = 0.87f      // Primary content
        const val full = 1f         // Full visibility
        const val overlay = 0.5f    // Modal overlays
        const val scrim = 0.32f     // Background scrim
        const val subtle = 0.12f    // Subtle highlights
        const val ghost = 0.08f     // Ghost elements
    }

    // =========================================================================
    // TYPOGRAPHY METRICS
    // =========================================================================

    /**
     * Extended typography metrics beyond Material spec.
     */
    object Typography {
        val displayLargeSize = 57.sp
        val displayMediumSize = 45.sp
        val displaySmallSize = 36.sp
        val headlineLargeSize = 32.sp
        val headlineMediumSize = 28.sp
        val headlineSmallSize = 24.sp
        val titleLargeSize = 22.sp
        val titleMediumSize = 16.sp
        val titleSmallSize = 14.sp
        val bodyLargeSize = 16.sp
        val bodyMediumSize = 14.sp
        val bodySmallSize = 12.sp
        val labelLargeSize = 14.sp
        val labelMediumSize = 12.sp
        val labelSmallSize = 11.sp

        // Custom sizes for Kairos
        val streakCountSize = 32.sp
        val achievementTitleSize = 18.sp
        val quoteTextSize = 20.sp
        val notificationTitleSize = 16.sp
        val notificationBodySize = 14.sp
    }

    // =========================================================================
    // NOTIFICATION DIMENSIONS
    // =========================================================================

    /**
     * Notification-specific dimensions for lively notifications.
     */
    object Notification {
        val minHeight = 72.dp
        val maxHeight = 200.dp
        val iconSize = 48.dp
        val avatarSize = 40.dp
        val padding = Spacing.lg
        val contentSpacing = Spacing.sm
        val actionButtonHeight = 36.dp
        val cornerRadius = Radius.xl
        val illustrationSize = 64.dp
        val progressBarHeight = 4.dp
    }

    // =========================================================================
    // STREAK COMPONENT DIMENSIONS
    // =========================================================================

    /**
     * Streak display specific dimensions.
     */
    object Streak {
        val badgeSizeSmall = 48.dp
        val badgeSizeMedium = 64.dp
        val badgeSizeLarge = 80.dp
        val flameIconSizeSmall = 20.dp
        val flameIconSizeMedium = 28.dp
        val flameIconSizeLarge = 40.dp
        val glowRadius = 24.dp
        val glowIntensity = 0.6f
    }

    // =========================================================================
    // ACHIEVEMENT DIMENSIONS
    // =========================================================================

    /**
     * Achievement card and badge dimensions.
     */
    object Achievement {
        val cardHeight = 100.dp
        val iconSize = 48.dp
        val progressHeight = 8.dp
        val badgeSize = 32.dp
        val rarityIndicatorSize = 12.dp
        val glowRadius = 20.dp
    }

    // =========================================================================
    // PROGRESS INDICATOR DIMENSIONS
    // =========================================================================

    /**
     * Progress indicator specific dimensions.
     */
    object Progress {
        val linearHeight = 8.dp
        val linearHeightSmall = 4.dp
        val linearHeightLarge = 12.dp
        val circularSize = 48.dp
        val circularSizeSmall = 32.dp
        val circularSizeLarge = 120.dp
        val circularStrokeWidth = 4.dp
        val circularStrokeWidthLarge = 12.dp
    }

    // =========================================================================
    // BORDER WIDTHS
    // =========================================================================

    /**
     * Border width tokens.
     */
    object Border {
        val thin = 1.dp
        val medium = 2.dp
        val thick = 3.dp
        val focus = 2.dp
        val selection = 2.dp
    }

    // =========================================================================
    // Z-INDEX / LAYER ORDER
    // =========================================================================

    /**
     * Logical z-index values for layer ordering.
     */
    object ZIndex {
        const val base = 0f
        const val elevated = 1f
        const val dropdown = 10f
        const val sticky = 100f
        const val modal = 1000f
        const val toast = 1100f
        const val tooltip = 1200f
    }

    // =========================================================================
    // SEMANTIC COLORS
    // =========================================================================

    /**
     * Semantic colors for consistent meaning across the app.
     * Migrated from KairosDesignTokens.SemanticColors.
     */
    object SemanticColors {
        val success = Color(0xFF2E8B3E)
        val successLight = Color(0xFF81C784)
        val successDark = Color(0xFF1B5E20)

        val warning = Color(0xFFFFA000)
        val warningLight = Color(0xFFFFCA28)
        val warningDark = Color(0xFFFF8F00)

        val error = Color(0xFFD32F2F)
        val errorLight = Color(0xFFEF5350)
        val errorDark = Color(0xFFB71C1C)

        val info = Color(0xFF1565C0)
        val infoLight = Color(0xFF64B5F6)
        val infoDark = Color(0xFF0D47A1)

        val streakPrimary = Color(0xFFFF6B6B)
        val streakSecondary = Color(0xFFFFAB76)
        val streakTertiary = Color(0xFFFFC371)

        val goldPrimary = Color(0xFFD4AF37)
        val goldSecondary = Color(0xFFF4D03F)
        val goldLight = Color(0xFFFFE5B4)
    }

    // =========================================================================
    // THEME-DRIVEN SEMANTIC COLORS (resolve against the active color scheme —
    // dark and light share this architecture)
    // =========================================================================
    object Scheme {
        val backgroundPrimary: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background
        val backgroundSecondary: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerLow
        val surfacePrimary: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surface
        val surfaceElevated: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerHigh
        val surfaceSelected: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primaryContainer
        val surfaceGlass: Color @Composable @ReadOnlyComposable get() = KairosTheme.glass.fill
        val surfaceGlassStrong: Color @Composable @ReadOnlyComposable get() = KairosTheme.glass.fillStrong

        val textPrimary: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurface
        val textSecondary: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant
        val textTertiary: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant
        val textInverse: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onPrimary

        val borderSubtle: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outlineVariant
        val borderStrong: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outline

        val accentPrimary: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary
        val accentPrimaryPressed: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.82f)
        val accentSecondary: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.tertiary
        val accentSuccess: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.tertiary
        val accentWarning: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.error
        val accentDestructive: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.error

        val onAccent: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onPrimary

        val iconXs: Dp = 16.dp
        val iconSm: Dp = 20.dp
        val iconMd: Dp = 24.dp
        val iconLg: Dp = 32.dp
    }
}

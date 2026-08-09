package com.kairos.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Kairos semantic design tokens — the single source of truth for screen styling.
 *
 * Screens must read colors through [KairosTokens] (which resolves against the
 * active Material color scheme) instead of scattering hard-coded values.
 * Dark and light modes share this architecture; only the scheme values differ.
 */
object KairosTokens {

    // ------------------------------------------------------------------ Color
    val backgroundPrimary: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background
    val backgroundSecondary: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerLow
    val surfacePrimary: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surface
    val surfaceElevated: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerHigh
    val surfaceSelected: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primaryContainer
    val surfaceGlass: Color @Composable @ReadOnlyComposable get() = KairosTheme.glass.fill
    val surfaceGlassStrong: Color @Composable @ReadOnlyComposable get() = KairosTheme.glass.fillStrong

    val textPrimary: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurface
    val textSecondary: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val textTertiary: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
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

    // ------------------------------------------------------------------ Space
    val spaceXxs: Dp = 4.dp
    val spaceXs: Dp = 8.dp
    val spaceSm: Dp = 12.dp
    val spaceMd: Dp = 16.dp
    val spaceLg: Dp = 24.dp
    val spaceXl: Dp = 32.dp
    val spacePage: Dp = 20.dp

    // ------------------------------------------------------------------ Radius
    val radiusControl: Dp = 14.dp
    val radiusCard: Dp = 24.dp
    val radiusFeature: Dp = 28.dp
    val radiusFloating: Dp = 32.dp

    // ------------------------------------------------------------------ Icon
    val iconXs: Dp = 16.dp
    val iconSm: Dp = 20.dp
    val iconMd: Dp = 24.dp
    val iconLg: Dp = 32.dp

    // ------------------------------------------------------------------ Motion
    const val motionInstantMs = 120
    const val motionStateMs = 240
    const val motionPageMs = 320
    const val motionAmbientMs = 1_400
}

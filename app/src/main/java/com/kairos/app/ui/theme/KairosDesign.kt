package com.kairos.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Focused design tokens for the Kairos product surface ("Moment Blue").
 *
 * The legacy token catalog is intentionally left intact while screens migrate.
 * New product-facing screens depend on this small semantic set instead of the
 * historical feature-specific color and dimension inventory.
 */
@Immutable
data class KairosGlassColors(
    val fill: Color,
    val fillStrong: Color,
    val border: Color,
    val highlight: Color,
    val shadow: Color,
    val coolWash: Color,
    val warmWash: Color,
    val success: Color
)

/**
 * Frosted glass tokens. The material is a translucent tonal fill with a bright
 * top edge, hairline border, soft tinted shadow, and gentle top sheen — the
 * object-like surfaces of the Moment Blue system.
 */
internal val LightKairosGlassColors = KairosGlassColors(
    fill = KairosSurfaceLight.copy(alpha = 0.86f),
    fillStrong = KairosSurfaceContainerLight.copy(alpha = 0.94f),
    border = KairosOutlineLight.copy(alpha = 0.85f),
    highlight = Color(0x59FFFFFF),
    shadow = Color(0x2E1B2A4A),
    coolWash = Color(0x1F2E5BFF),
    warmWash = Color(0x14E86A5E),
    success = KairosVerdigris
)

internal val DarkKairosGlassColors = KairosGlassColors(
    fill = KairosSurfaceDark.copy(alpha = 0.84f),
    fillStrong = KairosSurfaceContainerDark.copy(alpha = 0.92f),
    border = KairosOutlineDark.copy(alpha = 0.60f),
    highlight = Color(0x26FFFFFF),
    shadow = Color(0x59000000),
    coolWash = Color(0x1F2E5BFF),
    warmWash = Color(0x12E86A5E),
    success = KairosSeaGlass
)

internal val LocalKairosGlassColors = staticCompositionLocalOf { LightKairosGlassColors }

/**
 * Liquid glass tokens for floating navigation and elevated chrome.
 * Translucency carries the glass effect on every API level.
 */
@Immutable
data class KairosLiquidGlassColors(
    val fill: Color,
    val fillDeep: Color,
    val border: Color,
    val highlight: Color,
    val sheen: Color,
    val shadow: Color
)

internal val LightKairosLiquidGlassColors = KairosLiquidGlassColors(
    fill = KairosSurfaceLight.copy(alpha = 0.88f),
    fillDeep = KairosSurfaceLight.copy(alpha = 0.70f),
    border = KairosOutlineLight.copy(alpha = 0.90f),
    highlight = Color(0x66FFFFFF),
    sheen = Color(0x26FFFFFF),
    shadow = Color(0x401B2A4A)
)

internal val DarkKairosLiquidGlassColors = KairosLiquidGlassColors(
    fill = KairosSurfaceDark.copy(alpha = 0.86f),
    fillDeep = KairosSurfaceDark.copy(alpha = 0.70f),
    border = KairosOutlineDark.copy(alpha = 0.55f),
    highlight = Color(0x3DFFFFFF),
    sheen = Color(0x1AFFFFFF),
    shadow = Color(0x66000000)
)

internal val LocalKairosLiquidGlassColors = staticCompositionLocalOf { LightKairosLiquidGlassColors }

object KairosSpacing {
    val xxs: Dp = 4.dp
    val xs: Dp = 8.dp
    val sm: Dp = 12.dp
    val md: Dp = 16.dp
    val lg: Dp = 20.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
    val section: Dp = 40.dp
    val pageHorizontal: Dp = 20.dp
    val screen: Dp = pageHorizontal
    val tabletMaxWidth: Dp = 760.dp
}

object KairosRadius {
    val control: Dp = 14.dp
    val controlLarge: Dp = 18.dp
    val readingSurface: Dp = 24.dp
    val card: Dp = 26.dp
    val floating: Dp = 30.dp
    val navigation: Dp = 32.dp
}

object KairosElevation {
    val glass: Dp = 10.dp
    val floating: Dp = 18.dp
}

object KairosMotion {
    const val instant = 120
    const val quick = instant
    const val state = 220
    const val navigation = 300
}

object KairosTheme {
    val glass: KairosGlassColors
        @Composable
        @ReadOnlyComposable
        get() = LocalKairosGlassColors.current

    val liquidGlass: KairosLiquidGlassColors
        @Composable
        @ReadOnlyComposable
        get() = LocalKairosLiquidGlassColors.current
}

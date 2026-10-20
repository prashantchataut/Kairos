package com.kairos.app.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class KairosTheme(
    val colors: KairosColors,
    val type: KairosType = KairosType(),
    val motion: KairosMotion,
    val haptics: KairosHaptics,
)

val LocalKairos = staticCompositionLocalOf<KairosTheme> {
    error("KairosTheme missing — wrap the screen in KairosTheme { }")
}

object Kairos {
    val colors: KairosColors
        @Composable @ReadOnlyComposable get() = LocalKairos.current.colors
    val type: KairosType
        @Composable @ReadOnlyComposable get() = LocalKairos.current.type
    val motion: KairosMotion
        @Composable @ReadOnlyComposable get() = LocalKairos.current.motion
    val haptics: KairosHaptics
        @Composable @ReadOnlyComposable get() = LocalKairos.current.haptics
}

@Composable
fun KairosTheme(
    dark: Boolean,
    mood: Mood,
    hapticsEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = kairosColors(dark, mood)
    val type = KairosType()
    val motion = rememberMotion()
    val haptics = rememberHaptics(hapticsEnabled)

    val scheme = if (dark) {
        darkColorScheme(
            primary = colors.accent,
            onPrimary = colors.onAccent,
            background = colors.canvas,
            onBackground = colors.ink,
            surface = colors.surface,
            onSurface = colors.ink,
            surfaceVariant = colors.canvasTint,
            onSurfaceVariant = colors.inkSoft,
            outline = colors.hairline,
            error = colors.rose,
            onError = Color.White,
            secondary = colors.peach,
            tertiary = colors.mint,
        )
    } else {
        lightColorScheme(
            primary = colors.accent,
            onPrimary = colors.onAccent,
            background = colors.canvas,
            onBackground = colors.ink,
            surface = colors.surface,
            onSurface = colors.ink,
            surfaceVariant = colors.canvasTint,
            onSurfaceVariant = colors.inkSoft,
            outline = colors.hairline,
            error = colors.rose,
            onError = Color.White,
            secondary = colors.peach,
            tertiary = colors.mint,
        )
    }

    CompositionLocalProvider(LocalKairos provides KairosTheme(colors, type, motion, haptics)) {
        MaterialTheme(
            colorScheme = scheme,
            typography = materialType(type),
            content = content,
        )
    }
}

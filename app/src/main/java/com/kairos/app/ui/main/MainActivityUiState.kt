package com.kairos.app.ui.main

import com.kairos.app.ui.theme.ThemeMode

data class MainActivityUiState(
    val isLoading: Boolean = true,
    val startDestination: String? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val hapticFeedbackEnabled: Boolean = true,
    val dynamicColor: Boolean = false
)

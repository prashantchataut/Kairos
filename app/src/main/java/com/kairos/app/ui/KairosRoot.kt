package com.kairos.app.ui

import androidx.compose.runtime.Composable
import com.kairos.app.design.KairosTheme
import com.kairos.app.design.Mood

@Composable
fun KairosRoot() {
    KairosTheme(dark = false, mood = Mood.Sky) {
        androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier)
    }
}

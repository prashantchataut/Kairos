package com.kairos.app.di

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.EntryPointAccessors
import com.kairos.app.util.TextToSpeechManager

/**
 * Lightweight Hilt entry points for @Composable screens that need a singleton
 * dependency without adding a ViewModel layer for a single call.
 */
object KairosEntryPoints {

    @Composable
    fun textToSpeech(): TextToSpeechManager {
        val context = LocalContext.current.applicationContext
        return EntryPointAccessors.fromApplication(context, TextToSpeechEntryPoint::class.java)
            .textToSpeechManager()
    }

    @dagger.hilt.EntryPoint
    @dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
    interface TextToSpeechEntryPoint {
        fun textToSpeechManager(): TextToSpeechManager
    }
}

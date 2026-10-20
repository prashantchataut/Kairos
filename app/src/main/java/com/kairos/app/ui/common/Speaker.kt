package com.kairos.app.ui.common

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import java.util.Locale

/** Small wrapper so a screen can offer pronunciation without leaking an engine. */
class Speaker(private val tts: TextToSpeech?) {
    fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "kairos-word")
    }
}

@Composable
fun rememberSpeaker(context: Context): Speaker {
    val tts = remember { TextToSpeech(context) { } }
    DisposableEffect(tts) {
        tts.language = Locale.US
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }
    return remember(tts) { Speaker(tts) }
}

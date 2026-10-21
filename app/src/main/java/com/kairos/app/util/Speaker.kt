package com.kairos.app.util

import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/** Pronounces words with the device's text-to-speech voice. */
class Speaker internal constructor(internal val tts: TextToSpeech) {
    @Volatile internal var ready = false
    fun say(text: String) {
        if (!ready) return
        tts.setSpeechRate(0.85f)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, text.hashCode().toString())
    }
}

@Composable
fun rememberSpeaker(): Speaker {
    val context = LocalContext.current
    val holder = remember { arrayOfNulls<Speaker>(1) }
    val speaker = remember {
        lateinit var tts: TextToSpeech
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.US
                holder[0]?.ready = true
            }
        }
        Speaker(tts).also { holder[0] = it }
    }
    DisposableEffect(Unit) {
        onDispose { runCatching { speaker.tts.shutdown() } }
    }
    return speaker
}


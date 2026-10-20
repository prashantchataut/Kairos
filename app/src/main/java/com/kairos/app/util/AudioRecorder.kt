package com.kairos.app.util

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/** Records short voice notes for the time capsule as AAC in an .m4a container. */
class AudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var file: File? = null
    private var startedAt = 0L

    val isRecording get() = recorder != null

    fun start(): Boolean = runCatching {
        val dir = File(context.cacheDir, "voice").apply { mkdirs() }
        val out = File(dir, "voice-${System.currentTimeMillis()}.m4a")
        val r = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(context) else @Suppress("DEPRECATION") MediaRecorder()
        r.setAudioSource(MediaRecorder.AudioSource.MIC)
        r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        r.setAudioEncodingBitRate(96_000)
        r.setAudioSamplingRate(44_100)
        r.setOutputFile(out.absolutePath)
        r.prepare()
        r.start()
        recorder = r
        file = out
        startedAt = System.currentTimeMillis()
        true
    }.getOrElse { release(); false }

    /** Stops and returns the file plus its duration in milliseconds, or null if nothing usable was recorded. */
    fun stop(): Pair<File, Long>? {
        val r = recorder ?: return null
        val duration = System.currentTimeMillis() - startedAt
        val ok = runCatching { r.stop() }.isSuccess
        release()
        val f = file
        file = null
        return if (ok && f != null && duration > 600) f to duration else { f?.delete(); null }
    }

    fun release() {
        runCatching { recorder?.release() }
        recorder = null
    }
}

/** Plays one audio file at a time. */
class AudioPlayer {
    private var player: MediaPlayer? = null
    var playingPath: String? = null
        private set

    fun toggle(path: String, onDone: () -> Unit) {
        if (playingPath == path) { stop(); onDone(); return }
        stop()
        player = runCatching {
            MediaPlayer().apply {
                setDataSource(path)
                setOnCompletionListener { stop(); onDone() }
                prepare()
                start()
            }
        }.getOrNull()
        playingPath = if (player != null) path else null
    }

    fun stop() {
        runCatching { player?.release() }
        player = null
        playingPath = null
    }
}

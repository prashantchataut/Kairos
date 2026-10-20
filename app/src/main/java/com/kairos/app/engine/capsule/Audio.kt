package com.kairos.app.engine.capsule

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/** Voice notes for capsules: AAC in an M4A container, recorded to cache until sealed. */
class VoiceRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    var file: File? = null
        private set
    private var startedAt = 0L

    fun start(target: File): Boolean = runCatching {
        val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else @Suppress("DEPRECATION") MediaRecorder()
        r.setAudioSource(MediaRecorder.AudioSource.MIC)
        r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        r.setAudioEncodingBitRate(96_000)
        r.setAudioSamplingRate(44_100)
        r.setOutputFile(target.absolutePath)
        r.prepare()
        r.start()
        recorder = r
        file = target
        startedAt = System.currentTimeMillis()
        true
    }.getOrElse { release(); false }

    /** Returns the finished file and its duration, or null if nothing usable was captured. */
    fun stop(): Pair<File, Long>? {
        val r = recorder ?: return null
        val duration = System.currentTimeMillis() - startedAt
        val ok = runCatching { r.stop() }.isSuccess
        release()
        val f = file
        return if (ok && f != null && f.length() > 0) f to duration else null
    }

    fun amplitude(): Int = runCatching { recorder?.maxAmplitude ?: 0 }.getOrDefault(0)

    fun release() {
        runCatching { recorder?.release() }
        recorder = null
    }
}

class VoicePlayer {
    private var player: MediaPlayer? = null
    var playingPath: String? = null
        private set

    fun toggle(path: String, onDone: () -> Unit): Boolean {
        if (playingPath == path) {
            stop()
            return false
        }
        stop()
        return runCatching {
            player = MediaPlayer().apply {
                setDataSource(path)
                setOnCompletionListener { stop(); onDone() }
                prepare()
                start()
            }
            playingPath = path
            true
        }.getOrElse { stop(); false }
    }

    fun position(): Float {
        val p = player ?: return 0f
        return runCatching { if (p.duration > 0) p.currentPosition / p.duration.toFloat() else 0f }.getOrDefault(0f)
    }

    fun stop() {
        runCatching { player?.stop() }
        runCatching { player?.release() }
        player = null
        playingPath = null
    }
}

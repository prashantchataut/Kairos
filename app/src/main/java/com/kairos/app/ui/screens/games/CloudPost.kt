package com.kairos.app.ui.screens.games

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class CloudScene(val text: String, val choices: List<String>, val responses: List<String>)
data class CloudStory(val title: String, val opening: String, val scenes: List<CloudScene>, val ending: String)
data class CloudPostState(val story: CloudStory? = null, val busy: Boolean = true, val error: String? = null)

/** Data-only packages: checked before installation, never execute downloaded code. */
@HiltViewModel
class CloudPostViewModel @Inject constructor(@ApplicationContext private val context: Context) : ViewModel() {
    private val mutable = MutableStateFlow(CloudPostState())
    val state = mutable.asStateFlow()
    private val packageFile get() = File(context.filesDir, "games/cloud-post.json")
    init { load() }
    private fun load() = viewModelScope.launch {
        mutable.value = withContext(Dispatchers.IO) {
            if (!packageFile.exists()) CloudPostState(busy = false)
            else runCatching { CloudPostState(story = decode(packageFile.readBytes()), busy = false) }
                .getOrElse { CloudPostState(busy = false, error = "This game package needs to be downloaded again.") }
        }
    }
    fun download() {
        if (mutable.value.busy) return
        mutable.value = mutable.value.copy(busy = true, error = null)
        viewModelScope.launch {
            mutable.value = withContext(Dispatchers.IO) {
                runCatching {
                    val client = OkHttpClient.Builder().callTimeout(45, TimeUnit.SECONDS).followRedirects(false).build()
                    client.newCall(Request.Builder().url(URL).build()).execute().use { response ->
                        check(response.isSuccessful) { "Download failed" }
                        val body = requireNotNull(response.body)
                        val bytes = body.byteStream().use { input ->
                            val output = java.io.ByteArrayOutputStream()
                            val buffer = ByteArray(4096)
                            while (true) {
                                val count = input.read(buffer)
                                if (count < 0) break
                                check(output.size() + count <= 256 * 1024) { "Package too large" }
                                output.write(buffer, 0, count)
                            }
                            output.toByteArray()
                        }
                        val story = decode(bytes)
                        packageFile.parentFile?.mkdirs()
                        val staging = File(packageFile.parentFile, "cloud-post.tmp")
                        try {
                            staging.writeBytes(bytes)
                            check(staging.renameTo(packageFile))
                        } finally { staging.delete() }
                        CloudPostState(story = story, busy = false)
                    }
                }.getOrElse { CloudPostState(busy = false, error = "Cloud Post could not be downloaded. Check your connection and try again.") }
            }
        }
    }
    fun remove() {
        if (mutable.value.busy) return
        viewModelScope.launch {
            val removed = withContext(Dispatchers.IO) { !packageFile.exists() || packageFile.delete() }
            mutable.value = if (removed) CloudPostState(busy = false) else mutable.value.copy(error = "Could not remove this game. Try again.")
        }
    }
    private fun decode(bytes: ByteArray): CloudStory {
        check(MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) } == SHA256)
        val json = JSONObject(bytes.toString(Charsets.UTF_8))
        val scenes = json.getJSONArray("scenes")
        return CloudStory(json.getString("title"), json.getString("opening"), (0 until scenes.length()).map { index ->
            val scene = scenes.getJSONObject(index)
            val choices = scene.getJSONArray("choices"); val responses = scene.getJSONArray("responses")
            check(choices.length() == responses.length() && choices.length() in 2..4)
            CloudScene(scene.getString("text"), (0 until choices.length()).map { choices.getString(it) }, (0 until responses.length()).map { responses.getString(it) })
        }, json.getString("ending"))
    }
    companion object {
        // Pinned to the published game-package commit; updates require a new trusted digest.
        const val URL = "https://raw.githubusercontent.com/prashantchataut/Kairos/feat/painted-kairos-rebuild/games/cloud-post.json"
        const val SHA256 = "f55d8aa006f605f0583a8767fa6fd22ba798292ef9f1da5d0f1b31a514bf0a6a"
    }
}

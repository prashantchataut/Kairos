package com.kairos.app.ui.screens.play

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.kairos.app.domain.play.WordGarden
import com.kairos.app.domain.play.GameLibrary
import com.kairos.app.ui.components.painted.PocketGarden
import com.kairos.app.ui.animation.rememberKairosReducedMotion
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween

@Composable
fun PlayScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("word_garden", android.content.Context.MODE_PRIVATE) }
    var index by rememberSaveable { mutableIntStateOf(preferences.getInt("flowers", 0)) }
    var chosen by rememberSaveable { mutableStateOf<String?>(null) }
    val word = WordGarden.question(index)
    val correct = chosen != null && WordGarden.isCorrect(index, chosen!!)
    val uriHandler = LocalUriHandler.current
    var downloadMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val reduced = rememberKairosReducedMotion()
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextButton(onClick = onBack) { Text("Back to today") }
        Text("Play garden", style = MaterialTheme.typography.displayMedium)
        Text("A gentle adventure, one word at a time.", style = MaterialTheme.typography.bodyLarge)
        PocketGarden(Modifier.fillMaxWidth().height(180.dp), blooms = if (correct) (index % 9) + 1 else index % 9)
        Text("$index flowers grown", style = MaterialTheme.typography.labelLarge)
        AnimatedContent(index, transitionSpec = { fadeIn(tween(if (reduced) 0 else 240)) togetherWith fadeOut(tween(if (reduced) 0 else 140)) }, label = "garden-word") { number ->
            Text(WordGarden.question(number).word, style = MaterialTheme.typography.headlineLarge)
        }
        Text("Which meaning belongs to this word?")
        word.choices.forEach { answer ->
            OutlinedButton(onClick = { chosen = answer }, enabled = !correct, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                Text(answer)
            }
        }
        if (chosen != null) {
            Text(if (correct) "A new flower. Let it take its time." else "Not quite. Try another meaning; your garden can wait.", color = if (correct) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (correct) {
            Button(onClick = {
                index++
                preferences.edit().putInt("flowers", index).apply()
                chosen = null
            }, modifier = Modifier.fillMaxWidth()) { Text("Plant the next word") }
        }
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Text("Beyond the garden", style = MaterialTheme.typography.headlineSmall)
        GameLibrary.releases.forEach { release ->
            Text(release.title, style = MaterialTheme.typography.titleLarge)
            Text(release.description, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = { uriHandler.openUri(release.creatorUrl) }) { Text("Visit creator") }
            if (release.downloadUrl != null) {
                OutlinedButton(onClick = {
                    downloadMessage = runCatching {
                        val request = android.app.DownloadManager.Request(android.net.Uri.parse(release.downloadUrl))
                            .setTitle(release.title)
                            .setDescription("Official creator release")
                            .setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        if (android.os.Build.VERSION.SDK_INT >= 29) {
                            request.setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, release.filename)
                        } else {
                            request.setDestinationInExternalFilesDir(context, android.os.Environment.DIRECTORY_DOWNLOADS, release.filename)
                        }
                        val manager = context.getSystemService(android.content.Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
                        manager.enqueue(request)
                        "Download started. Find the APK and its status in your device's Downloads app."
                    }.getOrElse { "The download could not start. Visit the creator to download it directly." }
                }, modifier = Modifier.fillMaxWidth()) { Text("Download Android game") }
            }
        }
        downloadMessage?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        Text("Downloads stay on your device. Android asks separately before installing an APK. Pokémon fan games need their creator's instructions; Kairos includes no ROMs or emulator.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

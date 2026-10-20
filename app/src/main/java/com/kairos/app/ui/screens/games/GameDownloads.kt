package com.kairos.app.ui.screens.games

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Creator-provided packages remain files; Kairos never executes a downloaded APK or ROM. */
@Composable
fun GameDownloads() {
    val context = LocalContext.current
    val links = LocalUriHandler.current
    val downloads = remember { context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager }
    val prefs = remember { context.getSharedPreferences("game_downloads", Context.MODE_PRIVATE) }
    var url by rememberSaveable { mutableStateOf("") }
    var downloadId by remember { mutableLongStateOf(prefs.getLong("latest", -1)) }
    var status by remember { mutableStateOf("") }
    var ready by remember { mutableStateOf(false) }
    LaunchedEffect(downloadId) {
        ready = false
        if (downloadId < 0) return@LaunchedEffect
        while (true) {
            val result = withContext(Dispatchers.IO) {
                downloads.query(DownloadManager.Query().setFilterById(downloadId)).use { cursor ->
                    if (!cursor.moveToFirst()) return@use "Download is no longer available." to true
                    when (cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_SUCCESSFUL -> "Download ready" to true
                        DownloadManager.STATUS_FAILED -> "Download failed. Check the creator’s link and try again." to true
                        DownloadManager.STATUS_PAUSED -> "Download paused. Android will resume when a connection is available." to false
                        else -> {
                            val total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                            val bytes = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            (if (total > 0) "Downloading ${(bytes * 100 / total).coerceIn(0, 100)}%" else "Waiting for download…") to false
                        }
                    }
                }
            }
            status = result.first
            if (result.second) { ready = status == "Download ready"; break }
            delay(1500)
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Pokémon fan-game shelf", style = MaterialTheme.typography.headlineSmall)
        Text("Get patches from their creators. You may need your own compatible base game and an Android emulator.")
        OutlinedButton(onClick = { links.openUri("https://www.pokecommunity.com/threads/382178/") }, modifier = Modifier.fillMaxWidth()) { Text("Pokémon Unbound • Creator thread") }
        OutlinedButton(onClick = { links.openUri("https://www.pokecommunity.com/threads/326118/") }, modifier = Modifier.fillMaxWidth()) { Text("Pokémon Gaia • Creator thread") }
        OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("Creator’s direct HTTPS download link") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Button(onClick = {
            runCatching {
                val uri = Uri.parse(url.trim())
                require(uri.scheme == "https" && !uri.host.isNullOrBlank() && uri.userInfo == null)
                val name = uri.lastPathSegment?.replace(Regex("[^A-Za-z0-9._-]"), "_")?.take(100)?.takeIf { it.contains('.') } ?: "game-package.zip"
                val request = DownloadManager.Request(uri).setTitle(name)
                    .setDescription("Game package from ${uri.host}")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "${System.currentTimeMillis()}-$name")
                downloadId = downloads.enqueue(request)
                prefs.edit().putLong("latest", downloadId).apply()
                status = "Waiting for download…"
                url = ""
            }.onFailure { status = "Could not start the download. Use a direct HTTPS file link from the creator." }
        }, enabled = url.isNotBlank()) { Text("Download package") }
        if (status.isNotBlank()) Text(status, style = MaterialTheme.typography.bodySmall)
        if (ready) TextButton(onClick = {
            runCatching {
                val uri = requireNotNull(downloads.getUriForDownloadedFile(downloadId))
                context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, downloads.getMimeTypeForDownloadedFile(downloadId) ?: "application/octet-stream")
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                })
            }.onFailure { status = "No compatible app could open this package. Install a compatible patcher or emulator first." }
        }) { Text("Open downloaded package") }
    }
}

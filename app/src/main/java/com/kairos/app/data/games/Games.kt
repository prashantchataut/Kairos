package com.kairos.app.data.games

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.webkit.MimeTypeMap
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.kairos.app.KairosApp
import com.kairos.app.R
import com.kairos.app.data.content.ContentJson
import com.kairos.app.data.db.GameScore
import com.kairos.app.data.db.KairosDatabase
import com.kairos.app.data.db.ShelfEntry
import com.kairos.app.data.prefs.SettingsStore
import com.kairos.app.notify.Channels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

@Serializable
data class GameEntry(
    val id: String,
    val title: String,
    val author: String = "",
    val kind: String,
    val tagline: String = "",
    val description: String = "",
    val tags: List<String> = emptyList(),
    val accent: String = "sky",
    val sizeLabel: String? = null,
    val url: String? = null,
    val homepage: String? = null,
    val runner: String? = null,
    val github: String? = null,
    val assetPattern: String? = null,
    val steps: List<String> = emptyList(),
) {
    val isOriginal get() = kind == "original"
    val isPokemon get() = tags.any { it.startsWith("pok") }
}

@Serializable
data class Runner(val name: String, val packages: List<String> = emptyList(), val homepage: String? = null)

@Serializable
data class Catalog(
    val version: Int = 1,
    val updated: String? = null,
    val runners: Map<String, Runner> = emptyMap(),
    val games: List<GameEntry> = emptyList(),
)

sealed interface DownloadState {
    data object Idle : DownloadState
    data class Running(val done: Long, val total: Long) : DownloadState {
        val fraction get() = if (total > 0) done.toFloat() / total else -1f
    }
    data class Failed(val reason: String) : DownloadState
    data object Done : DownloadState
}

class GameRepository(
    private val context: Context,
    private val db: KairosDatabase,
    private val settings: SettingsStore,
    private val scope: CoroutineScope,
) {
    private val cacheFile = File(context.filesDir, "catalog-remote.json")
    private val _catalog = MutableStateFlow(Catalog())
    val catalog: StateFlow<Catalog> = _catalog
    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing

    val shelf: Flow<Map<String, ShelfEntry>> = db.games().observeShelf().map { list -> list.associateBy { it.gameId } }
    val scores: Flow<Map<String, GameScore>> = db.games().observeScores().map { list -> list.associateBy { it.gameId } }

    init {
        scope.launch(Dispatchers.IO) {
            val bundled = context.assets.open("games.json").bufferedReader().use { ContentJson.decodeFromString<Catalog>(it.readText()) }
            val cached = runCatching { ContentJson.decodeFromString<Catalog>(cacheFile.readText()) }.getOrNull()
            _catalog.value = merge(bundled, cached)
            refresh()
        }
    }

    private fun merge(base: Catalog, remote: Catalog?): Catalog {
        if (remote == null) return base
        val byId = LinkedHashMap<String, GameEntry>()
        base.games.forEach { byId[it.id] = it }
        remote.games.forEach { byId[it.id] = it }
        return Catalog(
            version = maxOf(base.version, remote.version),
            updated = remote.updated ?: base.updated,
            runners = base.runners + remote.runners,
            games = byId.values.toList(),
        )
    }

    /** Pulls the remote catalog. Failures are silent: the bundled catalog always works offline. */
    suspend fun refresh() = withContext(Dispatchers.IO) {
        val url = settings.current().catalogUrl
        if (url.isBlank()) return@withContext
        _refreshing.value = true
        try {
            val body = http.newCall(Request.Builder().url(url).build()).execute().use { r ->
                if (!r.isSuccessful) null else r.body?.string()
            } ?: return@withContext
            val remote = ContentJson.decodeFromString<Catalog>(body)
            cacheFile.writeText(body)
            val bundled = context.assets.open("games.json").bufferedReader().use { ContentJson.decodeFromString<Catalog>(it.readText()) }
            _catalog.value = merge(bundled, remote)
        } catch (_: Exception) {
        } finally {
            _refreshing.value = false
        }
    }

    fun game(id: String) = _catalog.value.games.firstOrNull { it.id == id }

    suspend fun addToShelf(id: String) {
        val existing = db.games().shelf(id)
        db.games().upsertShelf(existing ?: ShelfEntry(id, System.currentTimeMillis()))
    }

    suspend fun markPlayed(id: String) {
        val e = db.games().shelf(id) ?: ShelfEntry(id, System.currentTimeMillis())
        db.games().upsertShelf(e.copy(lastPlayedAt = System.currentTimeMillis()))
    }

    suspend fun removeFromShelf(id: String) = withContext(Dispatchers.IO) {
        db.games().shelf(id)?.filePath?.let { File(it).delete() }
        File(gamesDir(context), id).deleteRecursively()
        db.games().removeShelf(id)
    }

    suspend fun recordScore(id: String, score: Int): Boolean {
        val s = db.games().score(id) ?: GameScore(id)
        val best = score > s.best
        db.games().upsertScore(s.copy(best = maxOf(s.best, score), plays = s.plays + 1))
        return best
    }

    // ---------- downloads ----------
    fun startDownload(g: GameEntry) {
        val req = OneTimeWorkRequestBuilder<GameDownloadWorker>()
            .setInputData(workDataOf("id" to g.id, "url" to g.url, "github" to g.github, "pattern" to g.assetPattern, "title" to g.title))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork("dl-${g.id}", ExistingWorkPolicy.KEEP, req)
    }

    fun cancelDownload(id: String) = WorkManager.getInstance(context).cancelUniqueWork("dl-$id")

    fun downloadState(id: String): Flow<DownloadState> =
        WorkManager.getInstance(context).getWorkInfosForUniqueWorkFlow("dl-$id").map { infos ->
            val info = infos.lastOrNull() ?: return@map DownloadState.Idle
            when (info.state) {
                WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED ->
                    DownloadState.Running(info.progress.getLong("done", 0), info.progress.getLong("total", -1))
                WorkInfo.State.SUCCEEDED -> DownloadState.Done
                WorkInfo.State.FAILED -> DownloadState.Failed(info.outputData.getString("error") ?: "Download failed")
                WorkInfo.State.CANCELLED -> DownloadState.Idle
            }
        }

    // ---------- launching ----------
    fun runnerFor(g: GameEntry): Runner? = g.runner?.let { _catalog.value.runners[it] }

    fun installedRunnerPackage(g: GameEntry): String? = runnerFor(g)?.packages?.firstOrNull { pkg ->
        context.packageManager.getLaunchIntentForPackage(pkg) != null
    }

    fun launchRunner(g: GameEntry): Boolean {
        val pkg = installedRunnerPackage(g) ?: return false
        val intent = context.packageManager.getLaunchIntentForPackage(pkg) ?: return false
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        return true
    }

    fun openUrl(url: String) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    fun canInstallApks() = Build.VERSION.SDK_INT < 26 || context.packageManager.canRequestPackageInstalls()

    fun requestInstallPermission() {
        if (Build.VERSION.SDK_INT >= 26) {
            context.startActivity(
                Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }

    /** Installs a downloaded .apk, or offers the downloaded file to whatever can open it. */
    fun openDownloaded(path: String): Boolean {
        val file = File(path)
        if (!file.exists()) return false
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        val mime = if (file.extension.equals("apk", true)) "application/vnd.android.package-archive"
        else MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension.lowercase()) ?: "*/*"
        val intent = Intent(Intent.ACTION_VIEW).setDataAndType(uri, mime)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(if (mime == "application/vnd.android.package-archive") intent else Intent.createChooser(intent, "Open with").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            true
        } catch (_: ActivityNotFoundException) { false }
    }

    /** Copies a downloaded file into the public Downloads folder so launchers like JoiPlay can reach it. */
    suspend fun exportToDownloads(path: String): Boolean = withContext(Dispatchers.IO) {
        val file = File(path)
        if (!file.exists() || Build.VERSION.SDK_INT < 29) return@withContext false
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, file.name)
            put(MediaStore.Downloads.RELATIVE_PATH, "Download/Kairos")
        }
        val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return@withContext false
        context.contentResolver.openOutputStream(uri)?.use { out -> file.inputStream().use { it.copyTo(out) } }
        true
    }

    companion object {
        val http: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .followRedirects(true)
                .build()
        }

        fun gamesDir(context: Context): File =
            File(context.getExternalFilesDir(null) ?: context.filesDir, "games").apply { mkdirs() }

        /** Finds the newest release asset whose name matches [pattern] in a GitHub repo. */
        fun resolveGithubAsset(repo: String, pattern: String?): String? {
            val req = Request.Builder()
                .url("https://api.github.com/repos/$repo/releases/latest")
                .header("Accept", "application/vnd.github+json")
                .build()
            val body = http.newCall(req).execute().use { if (it.isSuccessful) it.body?.string() else null } ?: return null
            val regex = Regex(pattern ?: ".*")
            val assets = ContentJson.parseToJsonElement(body).jsonObject["assets"]?.jsonArray ?: return null
            return assets.map { it.jsonObject }.firstOrNull { a ->
                regex.containsMatchIn(a["name"]?.jsonPrimitive?.content ?: "")
            }?.get("browser_download_url")?.jsonPrimitive?.content
        }
    }
}

/** Streams a game file to app storage with progress; survives the app going to the background. */
class GameDownloadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val id = inputData.getString("id") ?: ""
    private val title = inputData.getString("title") ?: "Game"

    private fun foreground(done: Long, total: Long): ForegroundInfo {
        val pct = if (total > 0) ((done * 100) / total).toInt() else 0
        val n = NotificationCompat.Builder(applicationContext, Channels.DOWNLOADS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Downloading $title")
            .setContentText(if (total > 0) "$pct%" else "${done / 1_000_000} MB")
            .setProgress(100, pct, total <= 0)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(0, "Cancel", WorkManager.getInstance(applicationContext).createCancelPendingIntent(getId()))
            .build()
        return if (Build.VERSION.SDK_INT >= 29) ForegroundInfo(id.hashCode(), n, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        else ForegroundInfo(id.hashCode(), n)
    }

    override suspend fun getForegroundInfo(): ForegroundInfo = foreground(0, -1)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            runCatching { setForeground(foreground(0, -1)) }
            val url = inputData.getString("url")
                ?: inputData.getString("github")?.let { GameRepository.resolveGithubAsset(it, inputData.getString("pattern")) }
                ?: return@withContext Result.failure(workDataOf("error" to "No download link in the catalog"))

            val dir = File(GameRepository.gamesDir(applicationContext), id).apply { mkdirs() }
            val response = GameRepository.http.newCall(Request.Builder().url(url).build()).execute()
            if (!response.isSuccessful) {
                response.close()
                return@withContext Result.failure(workDataOf("error" to "Server said ${response.code}"))
            }
            val name = Uri.parse(response.request.url.toString()).lastPathSegment?.takeIf { it.contains('.') } ?: "$id.bin"
            val part = File(dir, "$name.part")
            val total = response.body?.contentLength() ?: -1
            var done = 0L
            var lastReport = 0L
            response.body!!.byteStream().use { input ->
                part.outputStream().use { out ->
                    val buf = ByteArray(64 * 1024)
                    while (true) {
                        if (isStopped) { part.delete(); return@withContext Result.failure() }
                        val n = input.read(buf)
                        if (n < 0) break
                        out.write(buf, 0, n)
                        done += n
                        if (done - lastReport > 512 * 1024) {
                            lastReport = done
                            setProgress(workDataOf("done" to done, "total" to total))
                            runCatching { setForeground(foreground(done, total)) }
                        }
                    }
                }
            }
            val file = File(dir, name)
            part.renameTo(file)
            val db = (applicationContext as KairosApp).container.db
            val existing = db.games().shelf(id)
            db.games().upsertShelf(
                (existing ?: ShelfEntry(id, System.currentTimeMillis())).copy(filePath = file.absolutePath, bytes = file.length()),
            )
            Result.success()
        } catch (e: Exception) {
            Result.failure(workDataOf("error" to (e.message ?: "Network hiccup — try again")))
        }
    }
}

package com.kairos.app.engine.games

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.kairos.app.KairosApp
import com.kairos.app.data.db.GameDao
import com.kairos.app.data.db.GameInstallEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

@Serializable
data class GameSource(
    /** "github" (latest release asset), "direct" (a URL), or "page" (the creator's page only). */
    val type: String,
    val repo: String? = null,
    val asset: String? = null,
    val url: String? = null,
)

@Serializable
data class BaseRom(val name: String, val crc32: String? = null)

@Serializable
data class CatalogGame(
    val id: String,
    val title: String,
    val creator: String,
    val platform: String,
    /** "rom", "patch", "apk", or "external". */
    val kind: String,
    val summary: String,
    val tags: List<String> = emptyList(),
    val hue: Int = 0,
    val page: String,
    val source: GameSource,
    val base: BaseRom? = null,
    val howToPlay: String = "",
    val status: String = "",
)

@Serializable
data class Catalog(val version: Int = 1, val updated: String = "", val games: List<CatalogGame> = emptyList())

enum class InstallState { None, Resolving, Downloading, Downloaded, Ready, Failed }

data class GameView(
    val game: CatalogGame,
    val install: GameInstallEntity?,
) {
    val state: InstallState get() = install?.state?.let { s -> InstallState.entries.firstOrNull { it.name == s } } ?: InstallState.None
}

/**
 * The fan-game shelf. The catalog ships with the app and refreshes from the repository, so new
 * games can be added without an app update. Downloads only ever come from the creators' own
 * release pages; ROM hacks arrive as patches and are applied to a ROM you already own.
 */
class GameRepository(
    private val context: Context,
    private val dao: GameDao,
    private val http: OkHttpClient,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val _catalog = MutableStateFlow(Catalog())
    val catalog: StateFlow<Catalog> = _catalog

    private val cacheFile get() = File(context.filesDir, "games/catalog.json")

    suspend fun load() = withContext(Dispatchers.IO) {
        val bundled = runCatching { json.decodeFromString<Catalog>(context.assets.open("games/catalog.json").bufferedReader().use { it.readText() }) }.getOrNull()
        val cached = runCatching { json.decodeFromString<Catalog>(cacheFile.readText()) }.getOrNull()
        _catalog.value = listOfNotNull(bundled, cached).maxByOrNull { it.version } ?: Catalog()
    }

    /** Pulls the latest shelf from the Kairos repository. Silent on failure — the bundled one still works. */
    suspend fun refresh(): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            http.newCall(Request.Builder().url(REMOTE_CATALOG).build()).execute().use { res ->
                if (!res.isSuccessful) return@use false
                val text = res.body?.string().orEmpty()
                val remote = json.decodeFromString<Catalog>(text)
                if (remote.games.isNotEmpty() && remote.version >= _catalog.value.version) {
                    cacheFile.parentFile?.mkdirs()
                    cacheFile.writeText(text)
                    _catalog.value = remote
                }
                true
            }
        }.getOrDefault(false)
    }

    fun observeInstalls(): Flow<Map<String, GameInstallEntity>> = dao.observeInstalls().map { list -> list.associateBy { it.gameId } }

    fun observeProgress(gameId: String): Flow<Float?> =
        WorkManager.getInstance(context).getWorkInfosForUniqueWorkFlow(workName(gameId)).map { infos ->
            infos.firstOrNull { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
                ?.progress?.getFloat(DownloadWorker.PROGRESS, -1f)?.takeIf { it >= 0f }
        }

    fun dir(gameId: String) = File(context.filesDir, "games/$gameId").apply { mkdirs() }

    fun download(game: CatalogGame) {
        val req = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(workDataOf(DownloadWorker.GAME_ID to game.id))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(workName(game.id), ExistingWorkPolicy.KEEP, req)
    }

    fun cancel(gameId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(gameId))
    }

    suspend fun remove(gameId: String) = withContext(Dispatchers.IO) {
        cancel(gameId)
        dir(gameId).deleteRecursively()
        dao.deleteInstall(gameId)
    }

    /** For "external" games: bring in the file you downloaded from the creator's page. */
    suspend fun import(game: CatalogGame, uri: Uri, displayName: String): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val target = File(dir(game.id), sanitize(displayName))
            context.contentResolver.openInputStream(uri)?.use { input -> target.outputStream().use { input.copyTo(it) } }
                ?: error("Couldn't read that file.")
            val playable = game.kind != "patch"
            dao.upsertInstall(
                GameInstallEntity(
                    game.id, if (playable) InstallState.Ready.name else InstallState.Downloaded.name, target.absolutePath, target.name,
                    "imported", target.length(), if (playable) target.absolutePath else null, null, System.currentTimeMillis(),
                ),
            )
            target
        }
    }

    /** Patches the downloaded ROM hack onto the user's own base ROM. */
    suspend fun patch(game: CatalogGame, baseRom: Uri): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val install = dao.install(game.id) ?: error("Download the patch first.")
            val patchFile = File(install.filePath ?: error("Download the patch first."))
            val base = context.contentResolver.openInputStream(baseRom)?.use { it.readBytes() } ?: error("Couldn't read your base ROM.")
            if (base.size > RomPatcher.MAX_BYTES) throw PatchException("That base ROM is too large.")
            val patched = try {
                RomPatcher.apply(base, patchFile.readBytes())
            } catch (e: PatchException) {
                throw e
            } catch (e: Exception) {
                throw PatchException("The patch couldn't be applied to this ROM. Make sure it's the exact base version the creator lists.")
            }
            val ext = when {
                game.platform.contains("Color", true) -> "gbc"
                game.platform.contains("Advance", true) -> "gba"
                game.platform.contains("Game Boy", true) -> "gb"
                game.platform.contains("DS", true) -> "nds"
                else -> "bin"
            }
            val out = File(dir(game.id), "${game.id}.$ext")
            out.writeBytes(patched)
            dao.upsertInstall(install.copy(state = InstallState.Ready.name, playablePath = out.absolutePath, message = null, updatedAt = System.currentTimeMillis()))
            out
        }
    }

    fun uriFor(file: File): Uri = FileProvider.getUriForFile(context, context.packageName + ".files", file)

    /** Hands the game to an installed emulator (or the package installer for Android ports). */
    fun launch(file: File): LaunchResult {
        val uri = uriFor(file)
        val isApk = file.name.endsWith(".apk", true)
        if (isApk && !context.packageManager.canRequestPackageInstalls()) {
            context.startActivity(
                Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + context.packageName)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
            return LaunchResult.NeedsPermission
        }
        val mime = if (isApk) "application/vnd.android.package-archive" else "application/octet-stream"
        val view = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(Intent.createChooser(view, "Play with…").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            LaunchResult.Launched
        } catch (e: ActivityNotFoundException) {
            LaunchResult.NoApp
        }
    }

    suspend fun export(file: File, target: Uri): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openOutputStream(target)?.use { out -> file.inputStream().use { it.copyTo(out) } } != null
        }.getOrDefault(false)
    }

    internal suspend fun resolve(game: CatalogGame): Pair<String, String> = withContext(Dispatchers.IO) {
        when (game.source.type) {
            "direct" -> {
                val url = game.source.url ?: error("No download link listed.")
                url to sanitize(url.substringAfterLast('/').substringBefore('?').ifBlank { game.id })
            }
            "github" -> {
                val repo = game.source.repo ?: error("No repository listed.")
                val req = Request.Builder().url("https://api.github.com/repos/$repo/releases/latest")
                    .header("Accept", "application/vnd.github+json").build()
                http.newCall(req).execute().use { res ->
                    if (!res.isSuccessful) error("The creator's release page didn't answer (${res.code}).")
                    val release = json.decodeFromString<GhRelease>(res.body?.string().orEmpty())
                    val pattern = Regex(game.source.asset ?: ".*", RegexOption.IGNORE_CASE)
                    val asset = release.assets.firstOrNull { pattern.matches(it.name) }
                        ?: error("The latest release (${release.tagName}) has no matching file yet.")
                    asset.url to sanitize(asset.name)
                }
            }
            else -> error("This game is only available from the creator's page.")
        }
    }

    internal fun sanitize(name: String) = name.replace(Regex("[^A-Za-z0-9._-]"), "_").take(80).ifBlank { "game.bin" }

    @Serializable
    internal data class GhRelease(@kotlinx.serialization.SerialName("tag_name") val tagName: String = "", val assets: List<GhAsset> = emptyList())

    @Serializable
    internal data class GhAsset(val name: String, @kotlinx.serialization.SerialName("browser_download_url") val url: String, val size: Long = 0)

    companion object {
        const val REMOTE_CATALOG = "https://raw.githubusercontent.com/prashantchataut/Kairos/main/catalog/games.json"
        fun workName(id: String) = "game-download-$id"
    }
}

enum class LaunchResult { Launched, NoApp, NeedsPermission }

class DownloadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val container = (applicationContext as KairosApp).container
        val repo = container.games
        val dao = container.db.games()
        val id = inputData.getString(GAME_ID) ?: return Result.failure()
        val game = repo.catalog.value.games.firstOrNull { it.id == id } ?: run { repo.load(); repo.catalog.value.games.firstOrNull { it.id == id } }
            ?: return Result.failure()

        suspend fun state(s: InstallState, msg: String? = null, file: File? = null, bytes: Long = 0, playable: String? = null) =
            dao.upsertInstall(GameInstallEntity(id, s.name, file?.absolutePath, file?.name, null, bytes, playable, msg, System.currentTimeMillis()))

        state(InstallState.Resolving)
        return try {
            val (url, name) = repo.resolve(game)
            state(InstallState.Downloading)
            val target = File(repo.dir(id), name)
            val part = File(target.path + ".part")
            withContext(Dispatchers.IO) {
                container.http.newCall(Request.Builder().url(url).build()).execute().use { res ->
                    if (!res.isSuccessful) error("Download failed (${res.code}).")
                    val body = res.body ?: error("Empty download.")
                    val total = body.contentLength()
                    var done = 0L
                    var lastReport = 0L
                    body.byteStream().use { input ->
                        part.outputStream().use { out ->
                            val buf = ByteArray(64 * 1024)
                            while (true) {
                                if (isStopped) error("Cancelled")
                                val n = input.read(buf)
                                if (n < 0) break
                                out.write(buf, 0, n)
                                done += n
                                if (total > 0 && done - lastReport > 128 * 1024) {
                                    lastReport = done
                                    setProgress(workDataOf(PROGRESS to done / total.toFloat()))
                                }
                            }
                        }
                    }
                }
                if (target.exists()) target.delete()
                part.renameTo(target)
            }
            val playable = game.kind == "rom" || game.kind == "apk"
            state(if (playable) InstallState.Ready else InstallState.Downloaded, file = target, bytes = target.length(), playable = if (playable) target.absolutePath else null)
            Result.success()
        } catch (e: Exception) {
            if (isStopped) dao.deleteInstall(id) else state(InstallState.Failed, e.message ?: "Something went wrong.")
            Result.failure()
        }
    }

    companion object {
        const val GAME_ID = "gameId"
        const val PROGRESS = "progress"
    }
}

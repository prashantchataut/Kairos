package com.kairos.app.ui.play

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.kairos.app.AppContainer
import com.kairos.app.data.db.GameScoreEntity
import com.kairos.app.engine.games.CatalogGame
import com.kairos.app.engine.games.GameView
import com.kairos.app.engine.games.InstallState
import com.kairos.app.engine.games.LaunchResult
import com.kairos.app.ui.art.CritterHues
import com.kairos.app.ui.art.darken
import com.kairos.app.ui.art.drawDrop
import com.kairos.app.ui.art.drawSparkle
import com.kairos.app.ui.art.lighten
import com.kairos.app.ui.components.DetailTopBar
import com.kairos.app.ui.components.EmptyState
import com.kairos.app.ui.components.Eyebrow
import com.kairos.app.ui.components.PillLabel
import com.kairos.app.ui.components.ProgressRing
import com.kairos.app.ui.components.ScreenHeader
import com.kairos.app.ui.components.SectionTitle
import com.kairos.app.ui.components.enter
import com.kairos.app.ui.components.kairosViewModel
import com.kairos.app.ui.components.screenPadding
import com.kairos.app.ui.glass.ButtonTone
import com.kairos.app.ui.glass.GlassCard
import com.kairos.app.ui.glass.GlassIconButton
import com.kairos.app.ui.glass.GlassWeight
import com.kairos.app.ui.glass.KButton
import com.kairos.app.ui.glass.KChip
import com.kairos.app.ui.nav.LocalNavigator
import com.kairos.app.ui.nav.Routes
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Radii
import com.kairos.app.ui.theme.Space
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.abs
import kotlin.math.sin

/** Kairos Originals — built for this app, playable instantly, no download. */
enum class Original(val id: String, val title: String, val tagline: String, val blurb: String) {
    DropletSpell("droplet-spell", "Droplet Spell", "Spell from the pond", "Letters float up as droplets. Read the clue, tap them in order, and grow your garden while you play."),
    LanternDrift("lantern-drift", "Lantern Drift", "A quiet night flight", "Hold to rise, let go to drift. Guide a paper lantern through the reeds and gather fireflies."),
    BloomMatch("bloom-match", "Bloom Match", "Pair words with meanings", "Flip watercolor cards to match each word with its meaning. Fewer flips, brighter blooms."),
    ;

    companion object {
        fun of(id: String) = entries.firstOrNull { it.id == id }
    }
}

enum class ShelfFilter(val label: String) { All("All"), Ready("On this phone"), Patch("ROM patches"), Page("From the creator") }

class PlayViewModel(private val c: AppContainer) : ViewModel() {
    val games = combine(c.games.catalog, c.games.observeInstalls()) { cat, installs ->
        cat.games.map { GameView(it, installs[it.id]) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val scores = c.db.games().observeScores().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _refreshing = kotlinx.coroutines.flow.MutableStateFlow(false)
    val refreshing = _refreshing

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _refreshing.value = true
        c.games.refresh()
        _refreshing.value = false
    }
}

@Composable
fun PlayScreen() {
    val vm = kairosViewModel { PlayViewModel(it) }
    val games by vm.games.collectAsStateWithLifecycle()
    val scores by vm.scores.collectAsStateWithLifecycle()
    val refreshing by vm.refreshing.collectAsStateWithLifecycle()
    val nav = LocalNavigator.current
    var filter by remember { mutableStateOf(ShelfFilter.All) }
    val colors = Kairos.colors

    val shown = games.filter {
        when (filter) {
            ShelfFilter.All -> true
            ShelfFilter.Ready -> it.state == InstallState.Ready || it.state == InstallState.Downloaded
            ShelfFilter.Patch -> it.game.kind == "patch"
            ShelfFilter.Page -> it.game.source.type == "page"
        }
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = screenPadding(bottom = 0.dp)) {
        item {
            ScreenHeader("Play something gentle", "Arcade", trailing = {
                GlassIconButton(Icons.Rounded.Refresh, if (refreshing) "Refreshing shelf" else "Refresh the shelf", { vm.refresh() })
            })
        }
        item { SectionTitle("Kairos Originals") }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Space.md), contentPadding = PaddingValues(end = Space.screen)) {
                items(Original.entries) { o ->
                    OriginalCard(o, scores.firstOrNull { it.gameId == o.id }, Modifier.enter(o.ordinal)) { nav.open(Routes.original(o.id)) }
                }
            }
        }
        item {
            SectionTitle("Fan game shelf")
            Text(
                "Community-made Pokémon fan games and ROM hacks, fetched only from their creators' official pages. ROM hacks arrive as patches — you bring a game you own, Kairos patches it on your phone.",
                style = Kairos.type.bodyRegular, color = colors.inkSoft,
            )
            Spacer(Modifier.height(Space.md))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                ShelfFilter.entries.forEach { f -> KChip(f.label, f == filter, { filter = f }) }
            }
            Spacer(Modifier.height(Space.md))
        }
        if (shown.isEmpty()) {
            item { EmptyState(null, "Nothing here yet", if (filter == ShelfFilter.Ready) "Games you download or import will live here." else "Pull the shelf fresh with the refresh button.") }
        }
        items(shown, key = { it.game.id }) { g ->
            ShelfCard(g, Modifier.padding(bottom = Space.md).animateItem()) { nav.open(Routes.game(g.game.id)) }
        }
        item {
            Text(
                "Kairos isn't affiliated with Nintendo, Game Freak, Creatures or The Pokémon Company. Fan projects belong to their creators — please support them on their pages.",
                style = Kairos.type.caption.copy(letterSpacing = 0.sp), color = colors.inkFaint,
                modifier = Modifier.padding(top = Space.md, bottom = 140.dp),
            )
        }
    }
}

@Composable
private fun OriginalCard(o: Original, score: GameScoreEntity?, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = Kairos.colors
    GlassCard(modifier.width(236.dp), onClick = onClick, blur = true, weight = GlassWeight.Frosted, shape = Radii.sheet, contentPadding = PaddingValues(10.dp)) {
        Column {
            OriginalCover(o, Modifier.fillMaxWidth().aspectRatio(1.1f).clip(Radii.card))
            Column(Modifier.padding(horizontal = 8.dp, vertical = 10.dp)) {
                Text(o.title, style = Kairos.type.title.copy(fontSize = 22.sp), color = colors.ink)
                Text(o.tagline, style = Kairos.type.bodyRegular, color = colors.inkSoft)
                Spacer(Modifier.height(Space.sm))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.PlayArrow, null, tint = colors.pond, modifier = Modifier.size(18.dp))
                    Text(
                        if (score != null && score.plays > 0) "Best ${score.best} · ${score.plays} plays" else "Play now",
                        style = Kairos.type.label, color = colors.pond,
                    )
                }
            }
        }
    }
}

/** Painted covers for the originals; each is a tiny looping scene. */
@Composable
fun OriginalCover(o: Original, modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "cover")
    val phase by t.animateFloat(0f, 6.283f, infiniteRepeatable(tween(6000, easing = LinearEasing)), label = "p")
    val reduced = Kairos.reducedMotion
    Canvas(modifier) {
        val p = if (reduced) 1f else phase
        when (o) {
            Original.DropletSpell -> {
                drawRect(Brush.verticalGradient(listOf(Color(0xFFCFE3FF), Color(0xFFB9EAD8))))
                val letters = "WORDS"
                letters.forEachIndexed { i, _ ->
                    val x = size.width * (0.16f + i * 0.17f)
                    val y = size.height * (0.5f + 0.12f * sin(p + i * 1.1f))
                    drawCircle(Brush.radialGradient(listOf(Color.White, Color(0xFF9CC4FF)), center = Offset(x - 8f, y - 8f), radius = size.width * 0.1f), size.width * 0.075f, Offset(x, y))
                    drawCircle(Color.White.copy(alpha = 0.8f), size.width * 0.015f, Offset(x - size.width * 0.025f, y - size.width * 0.025f))
                }
                drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color(0x553B6FEA)), startY = size.height * 0.75f))
            }
            Original.LanternDrift -> {
                drawRect(Brush.verticalGradient(listOf(Color(0xFF243A72), Color(0xFF5B4E8F), Color(0xFFE7A98F))))
                repeat(14) { i ->
                    val x = (i * 73 % 100) / 100f * size.width
                    val y = (i * 37 % 60) / 100f * size.height
                    drawCircle(Color(0xFFFFF3B0).copy(alpha = 0.4f + 0.4f * sin(p * 2 + i)), 2.5f, Offset(x, y))
                }
                val ly = size.height * (0.42f + 0.05f * sin(p))
                drawCircle(Brush.radialGradient(listOf(Color(0x99FFD27A), Color.Transparent), center = Offset(size.width * 0.45f, ly), radius = size.width * 0.3f), size.width * 0.3f, Offset(size.width * 0.45f, ly))
                drawRoundRect(Brush.verticalGradient(listOf(Color(0xFFFFE08A), Color(0xFFF2994A))), topLeft = Offset(size.width * 0.38f, ly - size.width * 0.09f), size = Size(size.width * 0.14f, size.width * 0.18f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f))
                drawHills(Color(0xFF2E3E6E), 0.78f, p * 0.3f)
                drawHills(Color(0xFF1C2A52), 0.88f, p * 0.6f)
            }
            Original.BloomMatch -> {
                drawRect(Brush.verticalGradient(listOf(Color(0xFFFFE4EC), Color(0xFFFFF1CF))))
                val cw = size.width * 0.26f
                val ch = cw * 1.3f
                listOf(-8f, 4f, 12f).forEachIndexed { i, rot ->
                    val cx = size.width * (0.3f + i * 0.2f)
                    val cy = size.height * 0.52f
                    rotate(rot + 3 * sin(p + i), Offset(cx, cy)) {
                        drawRoundRect(Color.White, topLeft = Offset(cx - cw / 2, cy - ch / 2), size = Size(cw, ch), cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f))
                        val petal = listOf(Color(0xFFFF9FB5), Color(0xFF9FD8FF), Color(0xFFB7E59B))[i]
                        repeat(5) { k ->
                            val a = k / 5f * 6.283f
                            drawCircle(petal, cw * 0.12f, Offset(cx + kotlin.math.cos(a) * cw * 0.15f, cy + kotlin.math.sin(a) * cw * 0.15f))
                        }
                        drawCircle(Color(0xFFFFD45E), cw * 0.09f, Offset(cx, cy))
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawHills(color: Color, top: Float, shift: Float) {
    val path = Path().apply {
        moveTo(0f, size.height)
        var x = 0f
        while (x <= size.width) {
            lineTo(x, size.height * top + sin(x / size.width * 9f + shift) * size.height * 0.04f)
            x += 6f
        }
        lineTo(size.width, size.height)
        close()
    }
    drawPath(path, color)
}

/** A painted, trademark-free cover: a watercolor landscape keyed to the game's hue. */
@Composable
fun GameCover(game: CatalogGame, modifier: Modifier = Modifier) {
    val base = CritterHues[abs(game.hue) % CritterHues.size]
    val seed = game.id.hashCode()
    Canvas(modifier) {
        drawRect(Brush.verticalGradient(listOf(base.lighten(0.45f), base.lighten(0.15f))))
        drawCircle(Color.White.copy(alpha = 0.7f), size.width * 0.12f, Offset(size.width * (0.2f + (abs(seed) % 50) / 100f), size.height * 0.28f))
        repeat(3) { i ->
            val c = base.darken(0.08f + i * 0.12f)
            val path = Path().apply {
                moveTo(0f, size.height)
                var x = 0f
                while (x <= size.width) {
                    val y = size.height * (0.55f + i * 0.13f) + sin(x / size.width * (4f + i * 2) + seed % 7 + i) * size.height * 0.06f
                    lineTo(x, y)
                    x += 6f
                }
                lineTo(size.width, size.height)
                close()
            }
            drawPath(path, c.copy(alpha = 0.85f))
        }
        drawSparkle(Offset(size.width * 0.78f, size.height * 0.22f), size.width * 0.07f, Color.White.copy(alpha = 0.9f))
        drawDrop(Offset(size.width * 0.86f, size.height * 0.4f), size.width * 0.03f, Color.White.copy(alpha = 0.6f))
    }
}

@Composable
private fun ShelfCard(g: GameView, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = Kairos.colors
    GlassCard(modifier.fillMaxWidth(), onClick = onClick, contentPadding = PaddingValues(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(84.dp).clip(Radii.card)) {
                GameCover(g.game, Modifier.fillMaxSize())
                Text(g.game.title.take(1), style = Kairos.type.display.copy(fontSize = 34.sp), color = Color.White.copy(alpha = 0.92f), modifier = Modifier.align(Alignment.Center))
            }
            Spacer(Modifier.width(Space.md))
            Column(Modifier.weight(1f)) {
                Text(g.game.title, style = Kairos.type.heading.copy(fontSize = 17.sp), color = colors.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${g.game.creator} · ${g.game.platform}", style = Kairos.type.bodyRegular.copy(fontSize = 13.sp), color = colors.inkSoft, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PillLabel(kindLabel(g.game.kind))
                    if (g.state == InstallState.Ready) PillLabel("Ready", color = colors.success)
                }
            }
            StateGlyph(g)
        }
    }
}

fun kindLabel(kind: String) = when (kind) {
    "patch" -> "Patch"
    "rom" -> "Download"
    "apk" -> "Android"
    else -> "Creator's page"
}

@Composable
private fun StateGlyph(g: GameView) {
    val colors = Kairos.colors
    Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
        when (g.state) {
            InstallState.Downloading, InstallState.Resolving -> ProgressRing(0.3f, Modifier.size(32.dp), stroke = 4.dp)
            InstallState.Ready -> Icon(Icons.Rounded.CheckCircle, "Ready to play", tint = colors.success)
            InstallState.Failed -> Icon(Icons.Rounded.Close, "Download failed", tint = colors.danger)
            else -> Icon(if (g.game.source.type == "page") Icons.AutoMirrored.Rounded.OpenInNew else Icons.Rounded.Download, null, tint = colors.inkSoft)
        }
    }
}

class GameViewModel(private val c: AppContainer, val id: String) : ViewModel() {
    val view = combine(c.games.catalog, c.games.observeInstalls()) { cat, installs ->
        cat.games.firstOrNull { it.id == id }?.let { GameView(it, installs[it.id]) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val progress = c.games.observeProgress(id).stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), null)
    private val _message = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val message = _message
    private val _busy = kotlinx.coroutines.flow.MutableStateFlow(false)
    val busy = _busy

    fun download() = view.value?.let { c.games.download(it.game) }
    fun cancel() = c.games.cancel(id)
    fun remove() = viewModelScope.launch { c.games.remove(id); _message.value = "Removed from this phone." }

    fun patch(uri: Uri) = viewModelScope.launch {
        val g = view.value?.game ?: return@launch
        _busy.value = true
        _message.value = "Patching…"
        c.games.patch(g, uri)
            .onSuccess { _message.value = "Patched! Ready to play." }
            .onFailure { _message.value = it.message ?: "Patching didn't work." }
        _busy.value = false
    }

    fun import(uri: Uri, name: String) = viewModelScope.launch {
        val g = view.value?.game ?: return@launch
        c.games.import(g, uri, name)
            .onSuccess { _message.value = "Imported. It lives in your arcade now." }
            .onFailure { _message.value = it.message }
    }

    fun play() {
        val path = view.value?.install?.playablePath ?: return
        _message.value = when (c.games.launch(File(path))) {
            LaunchResult.Launched -> null
            LaunchResult.NoApp -> "No emulator found. Install one (see “How to play”), or save the file to your phone."
            LaunchResult.NeedsPermission -> "Allow Kairos to install apps, then tap Play again."
        }
    }

    fun export(target: Uri) = viewModelScope.launch {
        val path = view.value?.install?.playablePath ?: view.value?.install?.filePath ?: return@launch
        _message.value = if (c.games.export(File(path), target)) "Saved to your phone." else "Couldn't save there."
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameDetailScreen(id: String) {
    val vm = kairosViewModel(key = "game-$id") { GameViewModel(it, id) }
    val view by vm.view.collectAsStateWithLifecycle()
    val progress by vm.progress.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    val nav = LocalNavigator.current
    val context = LocalContext.current
    val colors = Kairos.colors
    val pickBase = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let(vm::patch) }
    val pickImport = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        var name = "game.bin"
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c -> if (c.moveToFirst()) c.getString(0)?.let { name = it } }
        vm.import(uri, name)
    }
    val exportTo = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri -> uri?.let(vm::export) }

    val v = view
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = Space.huge)) {
        item { DetailTopBar(onBack = nav::back) }
        if (v == null) {
            item { EmptyState(null, "Game not found", "The shelf may have changed. Try refreshing it.") }
            return@LazyColumn
        }
        val g = v.game
        item {
            Column(Modifier.padding(horizontal = Space.screen)) {
                Box(Modifier.fillMaxWidth().aspectRatio(1.7f).clip(Radii.sheet)) {
                    GameCover(g, Modifier.fillMaxSize())
                    Column(Modifier.align(Alignment.BottomStart).padding(Space.lg)) {
                        Eyebrow(g.platform, color = Color.White.copy(alpha = 0.9f))
                        Text(g.title, style = Kairos.type.display, color = Color.White)
                    }
                }
                Spacer(Modifier.height(Space.lg))
                Text("by ${g.creator}", style = Kairos.type.label, color = colors.inkSoft)
                Spacer(Modifier.height(Space.sm))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    PillLabel(kindLabel(g.kind))
                    g.tags.forEach { PillLabel(it) }
                    if (g.status.isNotBlank()) PillLabel(g.status)
                }
                Spacer(Modifier.height(Space.lg))
                Text(g.summary, style = Kairos.type.bodyRegular.copy(fontSize = 17.sp, lineHeight = 26.sp), color = colors.ink)

                SectionTitle("Get it")
                GlassCard(Modifier.fillMaxWidth(), blur = true, weight = GlassWeight.Frosted) {
                    Column(verticalArrangement = Arrangement.spacedBy(Space.md)) {
                        val downloading = v.state == InstallState.Downloading || v.state == InstallState.Resolving
                        when {
                            g.source.type == "page" -> {
                                Step(1, "Download it from ${g.creator}'s official page", done = v.install != null)
                                KButton("Open the creator's page", { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(g.page))) }, icon = Icons.AutoMirrored.Rounded.OpenInNew, tone = ButtonTone.Glass, modifier = Modifier.fillMaxWidth())
                                Step(2, "Bring the file into your arcade", done = v.state == InstallState.Ready || v.state == InstallState.Downloaded)
                                KButton("Import downloaded file", { pickImport.launch(arrayOf("*/*")) }, icon = Icons.Rounded.FileOpen, tone = ButtonTone.Glass, modifier = Modifier.fillMaxWidth())
                            }
                            else -> {
                                Step(1, if (g.kind == "patch") "Download the patch from ${g.creator}'s releases" else "Download from ${g.creator}'s releases", done = v.state == InstallState.Downloaded || v.state == InstallState.Ready)
                                if (downloading) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        ProgressRing(progress ?: 0.05f, Modifier.size(40.dp), stroke = 5.dp)
                                        Spacer(Modifier.width(Space.md))
                                        Text(progress?.let { "Downloading ${(it * 100).toInt()}%" } ?: "Finding the latest release…", style = Kairos.type.body, color = colors.ink, modifier = Modifier.weight(1f))
                                        GlassIconButton(Icons.Rounded.Close, "Cancel download", vm::cancel, blur = false)
                                    }
                                } else if (v.state == InstallState.None || v.state == InstallState.Failed) {
                                    KButton("Download", { vm.download() }, icon = Icons.Rounded.Download, modifier = Modifier.fillMaxWidth())
                                    if (v.state == InstallState.Failed) Text(v.install?.message ?: "Something went wrong.", style = Kairos.type.bodyRegular, color = colors.danger)
                                }
                            }
                        }
                        if (g.kind == "patch") {
                            Step(if (g.source.type == "page") 3 else 2, "Choose your own ${g.base?.name ?: "base ROM"}", done = v.state == InstallState.Ready)
                            if (v.state == InstallState.Downloaded) {
                                KButton(if (busy) "Patching…" else "Choose base ROM and patch", { pickBase.launch(arrayOf("*/*")) }, icon = Icons.Rounded.Build, enabled = !busy, modifier = Modifier.fillMaxWidth())
                            }
                        }
                        AnimatedVisibility(v.state == InstallState.Ready) {
                            Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                                KButton("Play", vm::play, icon = Icons.Rounded.PlayArrow, modifier = Modifier.fillMaxWidth())
                                Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                                    KButton("Save to phone", {
                                        val name = File(v.install?.playablePath ?: v.install?.filePath ?: "game").name
                                        exportTo.launch(name)
                                    }, icon = Icons.Rounded.SaveAlt, tone = ButtonTone.Glass, modifier = Modifier.weight(1f))
                                    KButton("Remove", vm::remove, tone = ButtonTone.Glass, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                        message?.let { Text(it, style = Kairos.type.body, color = colors.pond) }
                    }
                }
                if (g.howToPlay.isNotBlank()) {
                    SectionTitle("How to play")
                    Text(g.howToPlay, style = Kairos.type.bodyRegular, color = colors.ink)
                }
                SectionTitle("Support the creator")
                KButton("Visit official page", { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(g.page))) }, icon = Icons.AutoMirrored.Rounded.OpenInNew, tone = ButtonTone.Glass)
                Spacer(Modifier.height(Space.lg))
                Text(
                    "Downloads come straight from the creator's own release. Patching happens on your phone, with a game you own. Nothing is uploaded.",
                    style = Kairos.type.caption.copy(letterSpacing = 0.sp), color = colors.inkFaint,
                )
            }
        }
    }
}

@Composable
private fun Step(n: Int, text: String, done: Boolean) {
    val colors = Kairos.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(28.dp).background(if (done) colors.success else colors.ink.copy(alpha = 0.08f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (done) Icon(Icons.Rounded.CheckCircle, null, tint = Color.White, modifier = Modifier.size(18.dp))
            else Text("$n", style = Kairos.type.label, color = colors.ink)
        }
        Spacer(Modifier.width(Space.md))
        Text(text, style = Kairos.type.body, color = colors.ink)
    }
}

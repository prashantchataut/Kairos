package com.kairos.app.ui.you

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.kairos.app.core.design.GlassCard
import com.kairos.app.core.design.GlassPill
import com.kairos.app.core.design.KairosAccents
import com.kairos.app.core.design.KairosMark
import com.kairos.app.core.design.KairosTheme
import com.kairos.app.core.design.KairosType
import com.kairos.app.core.design.MarkTile
import com.kairos.app.core.design.staggerIn
import com.kairos.app.data.catalog.QuoteCatalog
import com.kairos.app.data.catalog.VocabCatalog
import com.kairos.app.ui.common.Page
import com.kairos.app.ui.common.PageTitle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun YouScreen(
    state: YouUi,
    onName: (String) -> Unit,
    onTheme: (String) -> Unit,
    onAccent: (String) -> Unit,
    onReduceMotion: (Boolean) -> Unit,
    onPace: (Int) -> Unit,
    onToggleInterest: (String) -> Unit,
    onToggleQuoteTheme: (String) -> Unit,
    onExport: (File) -> Unit,
    onReset: () -> Unit,
) {
    val colors = KairosTheme.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var editName by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }
    var draftName by remember(state.profile.name) { mutableStateOf(state.profile.name) }
    var exportNote by remember { mutableStateOf<String?>(null) }

    Page {
        item {
            Row(
                modifier = Modifier.padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MarkTile(size = 58.dp)
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "you",
                        style = KairosType.eyebrow,
                        color = colors.inkFaint,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = state.profile.displayName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = colors.ink,
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Rounded.Edit,
                            contentDescription = "Edit name",
                            tint = colors.inkFaint,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable {
                                    draftName = state.profile.name
                                    editName = true
                                },
                        )
                    }
                    Text(
                        text = if (state.profile.joinedOn.isBlank()) "a local life, no cloud" else "here since ${state.profile.joinedOn}",
                        style = KairosType.handSmall,
                        color = colors.inkSoft,
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.staggerIn(0)) {
                StatCard("words owned", "${state.stats.wordsOwned}", "of ${state.stats.wordsMet} met", Modifier.weight(1f))
                StatCard("reviews", "${state.stats.reviews}", "all time", Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.staggerIn(1)) {
                StatCard("day thread", "${state.streak}", "best ${state.stats.bestStreak}", Modifier.weight(1f))
                StatCard("capsules", "${state.stats.capsules}", "${state.sealedCapsules} sealed", Modifier.weight(1f))
            }
        }

        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggerIn(2),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
            ) {
                Column {
                    Text("APPEARANCE", style = KairosType.eyebrow, color = colors.inkFaint)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("system" to "System", "light" to "Light", "dark" to "Dark").forEach { (id, label) ->
                            GlassPill(
                                selected = state.profile.themeMode == id,
                                onClick = { onTheme(id) },
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (state.profile.themeMode == id) colors.paper else colors.inkSoft,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Text("PIGMENT", style = KairosType.eyebrow, color = colors.inkFaint)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        KairosAccents.names.forEach { name ->
                            val accent = KairosAccents.of(name, colors.dark)
                            val selected = state.profile.accent == name
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(if (selected) 42.dp else 34.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(accent.apricot, accent.blossom, accent.periwinkle),
                                            ),
                                        )
                                        .border(
                                            width = if (selected) 2.dp else 1.dp,
                                            color = if (selected) colors.ink else colors.glassStroke,
                                            shape = CircleShape,
                                        )
                                        .clickable { onAccent(name) },
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = name,
                                    style = KairosType.handSmall.copy(fontSize = 14.sp),
                                    color = if (selected) colors.ink else colors.inkFaint,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Calm motion",
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.ink,
                            )
                            Text(
                                text = "stop drifting washes and breathing marks",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.inkSoft,
                            )
                        }
                        Switch(
                            checked = state.profile.reduceMotion,
                            onCheckedChange = onReduceMotion,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.paper,
                                checkedTrackColor = colors.ink,
                            ),
                        )
                    }
                }
            }
        }

        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggerIn(3),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
            ) {
                Column {
                    Text("WHAT WE GROW", style = KairosType.eyebrow, color = colors.inkFaint)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "words are chosen from these shelves",
                        style = KairosType.handSmall,
                        color = colors.inkFaint,
                    )
                    Spacer(Modifier.height(10.dp))
                    ChipCloud(
                        items = VocabCatalog.themes,
                        selected = state.profile.interests,
                        onClick = onToggleInterest,
                    )
                    Spacer(Modifier.height(18.dp))
                    Text("WHAT WE READ", style = KairosType.eyebrow, color = colors.inkFaint)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "the daily thought leans this way",
                        style = KairosType.handSmall,
                        color = colors.inkFaint,
                    )
                    Spacer(Modifier.height(10.dp))
                    ChipCloud(
                        items = QuoteCatalog.themes,
                        selected = state.profile.quoteThemes,
                        onClick = onToggleQuoteTheme,
                    )
                    Spacer(Modifier.height(18.dp))
                    Text("A SITTING IS", style = KairosType.eyebrow, color = colors.inkFaint)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(3, 5, 8, 12).forEach { pace ->
                            GlassPill(
                                selected = state.profile.dailyPace == pace,
                                onClick = { onPace(pace) },
                            ) {
                                Text(
                                    text = "$pace cards",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (state.profile.dailyPace == pace) colors.paper else colors.inkSoft,
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggerIn(4),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
            ) {
                Column {
                    Text("YOUR DATA", style = KairosType.eyebrow, color = colors.inkFaint)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Everything lives on this phone. Export is a single readable JSON file; nothing is uploaded.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkSoft,
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SmallAction(
                            icon = Icons.Rounded.FileDownload,
                            label = "Export",
                            onClick = {
                                scope.launch {
                                    val dir = File(context.cacheDir, "exports").apply { mkdirs() }
                                    val file = File(dir, "kairos-export.json")
                                    withContext(Dispatchers.IO) { onExport(file) }
                                    runCatching {
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file,
                                        )
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/json"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(
                                            Intent.createChooser(intent, "Export Kairos"),
                                        )
                                        exportNote = "export ready"
                                    }.onFailure { exportNote = "couldn't export" }
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )
                        SmallAction(
                            icon = Icons.Rounded.RestartAlt,
                            label = "Start over",
                            onClick = { confirmReset = true },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (exportNote != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = exportNote!!,
                            style = KairosType.handSmall,
                            color = colors.inkFaint,
                        )
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    KairosMark(modifier = Modifier.size(18.dp), tint = colors.inkFaint)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Kairos 2.0",
                        style = KairosType.eyebrow,
                        color = colors.inkFaint,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "for the days you want to remember",
                    style = KairosType.handSmall,
                    color = colors.inkFaint,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (editName) {
        AlertDialog(
            onDismissRequest = { editName = false },
            title = { Text("What should we call you?") },
            text = {
                OutlinedTextField(
                    value = draftName,
                    onValueChange = { draftName = it },
                    singleLine = true,
                    placeholder = { Text("your name") },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onName(draftName)
                        editName = false
                    },
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editName = false }) { Text("Cancel") } },
        )
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Start over?") },
            text = { Text("This erases your words, letters, moods and settings on this phone. It cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onReset()
                        confirmReset = false
                    },
                ) { Text("Erase everything") }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Keep it") } },
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, note: String, modifier: Modifier = Modifier) {
    GlassCard(
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
    ) {
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                color = KairosTheme.colors.ink,
            )
            Text(
                text = label,
                style = KairosType.eyebrow.copy(fontSize = 9.sp),
                color = KairosTheme.colors.inkFaint,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = note,
                style = KairosType.handSmall.copy(fontSize = 15.sp),
                color = KairosTheme.colors.inkSoft,
            )
        }
    }
}

@Composable
private fun ChipCloud(
    items: List<Pair<String, String>>,
    selected: Set<String>,
    onClick: (String) -> Unit,
) {
    val colors = KairosTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                row.forEach { (id, label) ->
                    GlassPill(
                        selected = id in selected,
                        onClick = { onClick(id) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (id in selected) colors.paper else colors.inkSoft,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SmallAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = KairosTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.paperDeep.copy(alpha = 0.65f))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = colors.inkSoft, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(7.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colors.ink,
        )
    }
}

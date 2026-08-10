package com.kairos.app.ui.screens.journal

import android.Manifest
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.domain.model.Mood
import com.kairos.app.ui.animation.KairosReveal
import com.kairos.app.ui.components.kairos.KairosAppBackground
import com.kairos.app.ui.components.kairos.KairosGlassSurface
import com.kairos.app.ui.components.kairos.KairosIconButton
import com.kairos.app.ui.components.kairos.KairosReadingSurface
import com.kairos.app.ui.icons.KairosIcons
import com.kairos.app.ui.security.KairosSecureScreenEffect
import com.kairos.app.ui.theme.KairosRadius
import com.kairos.app.ui.theme.KairosSpacing
import com.kairos.app.ui.theme.SerifFamily
import com.kairos.app.ui.theme.color
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A calm writing room. The interface deliberately keeps AI and rewards outside
 * the writing surface so the user can think before the application reacts.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewJournalEntryScreen(
    onNavigateBack: () -> Unit,
    onEntrySaved: () -> Unit,
    prefilledContent: String? = null,
    viewModel: NewJournalEntryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val contentFocus = remember { FocusRequester() }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> -> viewModel.addPhotos(uris.map(Uri::toString)) }
    val videoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> -> viewModel.addVideos(uris.map(Uri::toString)) }
    val audioPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.startRecording()
        else scope.launch { snackbar.showSnackbar("Microphone permission is needed for a voice note.") }
    }

    LaunchedEffect(Unit) {
        if (prefilledContent.isNullOrBlank()) {
            viewModel.restoreDraft()
        }
    }
    LaunchedEffect(prefilledContent) {
        if (!prefilledContent.isNullOrBlank() && state.content.isBlank()) {
            viewModel.updateContent(prefilledContent)
        }
    }
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbar.showSnackbar(it)
            viewModel.clearError()
        }
    }
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            delay(220)
            onEntrySaved()
        }
    }

    KairosSecureScreenEffect()

    val requestBack = {
        if (viewModel.handleBackNavigation()) onNavigateBack()
    }
    BackHandler(onBack = requestBack)

    KairosAppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            JournalEditorHeader(
                isSaving = state.isSaving,
                canSave = state.content.isNotBlank() && !state.isSaving,
                onBack = requestBack,
                onSave = viewModel::saveEntry
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = KairosSpacing.screen)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(KairosSpacing.md)
            ) {
                KairosReveal(visible = true, delayMillis = 30) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "How is this moment landing?",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MoodRibbon(
                            selected = state.selectedMood,
                            onSelected = viewModel::updateMood
                        )
                    }
                }

                AnimatedVisibility(
                    visible = state.selectedTemplate != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    state.selectedTemplate?.let { template ->
                        KairosGlassSurface(
                            modifier = Modifier.fillMaxWidth(),
                            strong = true,
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(template.icon, contentDescription = null, tint = template.color)
                                Column(Modifier.weight(1f)) {
                                    Text(template.name, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        template.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                TextButton(onClick = viewModel::clearTemplate) { Text("Clear") }
                            }
                        }
                    }
                }

                KairosReveal(visible = true, delayMillis = 80) {
                    KairosReadingSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(420.dp),
                        accent = state.selectedMood.color,
                        contentPadding = PaddingValues(22.dp)
                    ) {
                        Column(Modifier.fillMaxSize()) {
                            BasicTextField(
                                value = state.title,
                                onValueChange = viewModel::updateTitle,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.headlineSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                                decorationBox = { inner ->
                                    Box {
                                        if (state.title.isBlank()) {
                                            Text(
                                                "A title, if one appears",
                                                style = MaterialTheme.typography.headlineSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.48f)
                                            )
                                        }
                                        inner()
                                    }
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 18.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            )
                            BasicTextField(
                                value = state.content,
                                onValueChange = viewModel::updateContent,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .focusRequester(contentFocus),
                                textStyle = TextStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontFamily = SerifFamily,
                                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                                ),
                                cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                                decorationBox = { inner ->
                                    Box {
                                        if (state.content.isBlank()) {
                                            Text(
                                                state.selectedTemplate?.placeholderText
                                                    ?: "Write what is true before trying to make it useful…",
                                                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = SerifFamily),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.52f)
                                            )
                                        }
                                        inner()
                                    }
                                }
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    state.validationHint ?: "Private on this device",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "${state.wordCount} words",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                LinearProgressIndicator(
                    progress = { state.contentCompletionProgress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(CircleShape),
                    color = state.selectedMood.color,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )

                JournalToolDock(
                    isRecording = state.isRecording,
                    photoCount = state.attachedPhotos.size,
                    videoCount = state.attachedVideos.size,
                    hasVoice = state.voiceRecordingUri != null,
                    onTemplate = viewModel::toggleTemplateSelector,
                    onPhoto = { photoPicker.launch("image/*") },
                    onVideo = { videoPicker.launch("video/*") },
                    onVoice = {
                        if (state.isRecording) viewModel.stopRecording()
                        else audioPermission.launch(Manifest.permission.RECORD_AUDIO)
                    }
                )

                AnimatedVisibility(state.attachedPhotos.isNotEmpty() || state.attachedVideos.isNotEmpty() || state.voiceRecordingUri != null) {
                    AttachmentSummary(
                        photoCount = state.attachedPhotos.size,
                        videoCount = state.attachedVideos.size,
                        voiceDuration = state.voiceRecordingDuration,
                        onClearVoice = viewModel::removeVoiceRecording
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
        )
    }

    if (state.showTemplateSelector) {
        ModalBottomSheet(onDismissRequest = viewModel::toggleTemplateSelector) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = KairosSpacing.screen)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Choose a writing frame", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "Templates are scaffolding, not homework. Edit or erase every prompt.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                state.availableTemplates.forEach { template ->
                    Surface(
                        onClick = { viewModel.selectTemplate(template) },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                Modifier
                                    .size(44.dp)
                                    .background(template.color.copy(alpha = 0.15f), RoundedCornerShape(15.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(template.icon, contentDescription = null, tint = template.color)
                            }
                            Column(Modifier.weight(1f)) {
                                Text(template.name, fontWeight = FontWeight.SemiBold)
                                Text(template.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.showDiscardDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideDiscardDialog,
            title = { Text("Leave this reflection?") },
            text = { Text("Your unsaved words and attachments will be discarded.") },
            confirmButton = {
                TextButton(onClick = onNavigateBack) { Text("Discard") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideDiscardDialog) { Text("Keep writing") }
            }
        )
    }

    if (state.showTranscriptionChoice) {
        AlertDialog(
            onDismissRequest = { viewModel.onTranscriptionChoiceSelected(TranscriptionChoice.LATER) },
            title = { Text("Add a live transcript?") },
            text = { Text("Your recording is attached. Dictate the same thought once more to add searchable text using Android speech recognition.") },
            confirmButton = {
                TextButton(onClick = { viewModel.onTranscriptionChoiceSelected(TranscriptionChoice.NOW) }) { Text("Dictate now") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { viewModel.onTranscriptionChoiceSelected(TranscriptionChoice.LATER) }) { Text("Later") }
                    TextButton(onClick = { viewModel.onTranscriptionChoiceSelected(TranscriptionChoice.NEVER) }) { Text("No thanks") }
                }
            }
        )
    }
}

@Composable
private fun JournalEditorHeader(
    isSaving: Boolean,
    canSave: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = KairosSpacing.screen, vertical = KairosSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        KairosIconButton(KairosIcons.Close, "Close editor", onBack)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Reflection", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("No audience. No performance.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Button(
            onClick = onSave,
            enabled = canSave,
            shape = RoundedCornerShape(17.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.height(48.dp)
        ) {
            AnimatedContent(isSaving, label = "journal_save") { saving ->
                if (saving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text("Save", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun MoodRibbon(selected: Mood, onSelected: (Mood) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Mood.entries.forEach { mood ->
            val active = mood == selected
            Surface(
                onClick = { onSelected(mood) },
                shape = RoundedCornerShape(18.dp),
                color = if (active) mood.color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceContainerLow,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (active) mood.color.copy(alpha = 0.72f) else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.semantics {
                    role = Role.RadioButton
                    this.selected = active
                    contentDescription = mood.displayName
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(mood.emoji)
                    Text(mood.displayName, style = MaterialTheme.typography.labelMedium, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun JournalToolDock(
    isRecording: Boolean,
    photoCount: Int,
    videoCount: Int,
    hasVoice: Boolean,
    onTemplate: () -> Unit,
    onPhoto: () -> Unit,
    onVideo: () -> Unit,
    onVoice: () -> Unit
) {
    KairosGlassSurface(
        modifier = Modifier.fillMaxWidth(),
        strong = true,
        shape = RoundedCornerShape(KairosRadius.floating),
        contentPadding = PaddingValues(8.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ToolButton(KairosIcons.AutoStories, "Template", null, onTemplate)
            ToolButton(KairosIcons.Image, "Photo", photoCount.takeIf { it > 0 }?.toString(), onPhoto)
            ToolButton(KairosIcons.PlayCircle, "Video", videoCount.takeIf { it > 0 }?.toString(), onVideo)
            ToolButton(
                if (isRecording) KairosIcons.Stop else KairosIcons.Mic,
                if (isRecording) "Stop" else "Voice",
                if (hasVoice) "1" else null,
                onVoice,
                active = isRecording
            )
        }
    }
}

@Composable
private fun ToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    count: String?,
    onClick: () -> Unit,
    active: Boolean = false
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box {
            Icon(icon, contentDescription = label, tint = if (active) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
            if (count != null) {
                Text(
                    count,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(horizontal = 4.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AttachmentSummary(
    photoCount: Int,
    videoCount: Int,
    voiceDuration: Long,
    onClearVoice: () -> Unit
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (photoCount > 0) AttachmentPill("$photoCount photo${if (photoCount == 1) "" else "s"}", KairosIcons.Image)
        if (videoCount > 0) AttachmentPill("$videoCount video${if (videoCount == 1) "" else "s"}", KairosIcons.PlayCircle)
        if (voiceDuration > 0) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                onClick = onClearVoice
            ) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(KairosIcons.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("Voice note · ${voiceDuration / 1000}s", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.width(7.dp))
                    Icon(KairosIcons.Close, contentDescription = "Remove voice note", modifier = Modifier.size(15.dp))
                }
            }
        }
    }
}

@Composable
private fun AttachmentPill(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(7.dp))
            Text(text, style = MaterialTheme.typography.labelMedium)
        }
    }
}

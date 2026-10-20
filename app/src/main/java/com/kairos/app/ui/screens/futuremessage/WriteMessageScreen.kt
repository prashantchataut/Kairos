package com.kairos.app.ui.screens.futuremessage

import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.ui.animation.KairosDurations
import com.kairos.app.ui.animation.KairosEasing
import com.kairos.app.ui.animation.KairosReveal
import com.kairos.app.ui.animation.rememberKairosReducedMotion
import com.kairos.app.ui.components.kairos.KairosAppBackground
import com.kairos.app.ui.components.kairos.KairosGlassSurface
import com.kairos.app.ui.components.kairos.KairosIconButton
import com.kairos.app.ui.components.kairos.KairosPrimaryButton
import com.kairos.app.ui.components.kairos.KairosReadingSurface
import com.kairos.app.ui.icons.KairosIcons
import com.kairos.app.ui.security.KairosSecureScreenEffect
import com.kairos.app.ui.theme.KairosRadius
import com.kairos.app.ui.theme.KairosSpacing
import com.kairos.app.ui.theme.SerifFamily
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * A private writing room for a future letter.
 *
 * The screen intentionally treats scheduling, intention and attachments as
 * secondary tools. The writing itself remains the visual centre of gravity.
 */
@Composable
fun WriteMessageScreen(
    onNavigateBack: () -> Unit,
    onMessageSaved: () -> Unit,
    viewModel: WriteMessageViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val reduceMotion = rememberKairosReducedMotion()

    val attachmentContext = LocalContext.current
    fun retainAttachments(uris: List<Uri>, photos: Boolean) {
        val retained = uris.filter { uri ->
            runCatching {
                attachmentContext.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }.isSuccess
        }.map(Uri::toString)
        if (photos) viewModel.addPhotos(retained) else viewModel.addVideos(retained)
        if (retained.size != uris.size) viewModel.attachmentAccessFailed()
    }
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> -> retainAttachments(uris, true) }
    val videoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> -> retainAttachments(uris, false) }
    val microphonePermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.startRecording()
    }

    KairosSecureScreenEffect()

    val requestBack = {
        if (viewModel.handleBackNavigation()) onNavigateBack()
    }
    BackHandler(onBack = requestBack)

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            delay(if (reduceMotion) 100 else 620)
            onMessageSaved()
        }
    }

    KairosAppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            FutureLetterHeader(
                isSaving = state.isSaving,
                canSave = state.canSave,
                onBack = requestBack,
                onSeal = viewModel::saveMessage
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = KairosSpacing.screen)
                    .padding(bottom = 132.dp),
                verticalArrangement = Arrangement.spacedBy(KairosSpacing.lg)
            ) {
                KairosReveal(visible = true, delayMillis = 20) {
                    FutureHorizon(
                        selectedPreset = state.selectedPreset,
                        deliveryDate = state.deliveryDate,
                        onPresetSelected = viewModel::selectDatePreset
                    )
                }

                KairosReveal(visible = true, delayMillis = 70) {
                    LetterIntentionPicker(
                        selected = state.selectedCategory,
                        onSelected = viewModel::updateCategory
                    )
                }

                KairosReveal(visible = true, delayMillis = 120) {
                    FutureLetterEditor(
                        title = state.title,
                        content = state.content,
                        onTitleChanged = viewModel::updateTitle,
                        onContentChanged = viewModel::updateContent
                    )
                }

                KairosReveal(visible = true, delayMillis = 170) {
                    LetterAttachments(
                        state = state,
                        onPickPhotos = { photoPicker.launch(arrayOf("image/*")) },
                        onPickVideos = { videoPicker.launch(arrayOf("video/*")) },
                        onRecord = {
                            if (state.isRecording) viewModel.stopRecording()
                            else microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        onPlayVoice = viewModel::toggleVoicePlayback,
                        onRemoveVoice = viewModel::removeVoiceRecording
                    )
                }

                Text(
                    text = "Future letters are stored privately on this device. Their contents stay sealed in the list until the chosen date.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        KairosGlassSurface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = KairosSpacing.screen, vertical = KairosSpacing.md),
            strong = true,
            shape = RoundedCornerShape(KairosRadius.floating),
            contentPadding = PaddingValues(8.dp)
        ) {
            KairosPrimaryButton(
                text = if (state.isSaving) "Sealing…" else "Seal until ${shortDate(state.deliveryDate)}",
                onClick = viewModel::saveMessage,
                enabled = state.canSave && !state.isSaving,
                icon = if (state.isSaving) null else KairosIcons.Lock,
                modifier = Modifier.fillMaxWidth()
            )
        }

        AnimatedVisibility(
            visible = state.isSaving || state.isSaved,
            enter = fadeIn(tween(KairosDurations.State)),
            exit = fadeOut(tween(KairosDurations.Micro))
        ) {
            SealingLetterOverlay(saved = state.isSaved)
        }
    }

    if (state.showDatePicker) {
        FutureDatePicker(
            initialDate = state.deliveryDate,
            onDismiss = viewModel::hideDatePicker,
            onConfirm = viewModel::selectCustomDate
        )
    }

    if (state.showDiscardDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideDiscardDialog,
            title = { Text("Discard this letter?") },
            text = { Text("The draft and its attachments have not been saved.") },
            confirmButton = {
                TextButton(onClick = onNavigateBack) { Text("Discard") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideDiscardDialog) { Text("Keep writing") }
            }
        )
    }

    state.error?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text("Letter not sealed") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = viewModel::clearError) { Text("Return to draft") }
            }
        )
    }
}

@Composable
private fun FutureLetterHeader(
    isSaving: Boolean,
    canSave: Boolean,
    onBack: () -> Unit,
    onSeal: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = KairosSpacing.screen, vertical = KairosSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        KairosIconButton(KairosIcons.Close, "Close future letter", onBack)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Future letter", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "A private note across time",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        KairosIconButton(
            icon = if (isSaving) KairosIcons.HourglassEmpty else KairosIcons.Check,
            contentDescription = "Seal future letter",
            onClick = onSeal,
            selected = canSave && !isSaving
        )
    }
}

@Composable
private fun FutureHorizon(
    selectedPreset: DatePreset,
    deliveryDate: Long,
    onPresetSelected: (DatePreset) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("When should this arrive?", style = MaterialTheme.typography.titleLarge, fontFamily = SerifFamily)
                Text(
                    longDate(deliveryDate),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                relativeDate(deliveryDate),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DatePreset.entries.forEach { preset ->
                val selected = preset == selectedPreset
                Surface(
                    onClick = { onPresetSelected(preset) },
                    shape = RoundedCornerShape(18.dp),
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    border = BorderStroke(1.dp, if (selected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.semantics {
                        this.selected = selected
                        role = Role.RadioButton
                    }
                ) {
                    Text(
                        preset.label,
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 11.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun LetterIntentionPicker(
    selected: MessageCategory,
    onSelected: (MessageCategory) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("What kind of letter is this?", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            MessageCategory.entries.forEach { category ->
                val isSelected = category == selected
                val color = categoryColor(category)
                KairosGlassSurface(
                    modifier = Modifier
                        .semantics {
                            this.selected = isSelected
                            role = Role.RadioButton
                        },
                    strong = isSelected,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = if (category.ordinal % 2 == 0) 6.dp else 20.dp),
                    onClick = { onSelected(category) },
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 11.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(8.dp).background(color, CircleShape))
                        Text(
                            category.displayName,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FutureLetterEditor(
    title: String,
    content: String,
    onTitleChanged: (String) -> Unit,
    onContentChanged: (String) -> Unit
) {
    KairosReadingSurface(
        modifier = Modifier.fillMaxWidth(),
        accent = categoryColor(MessageCategory.GENERAL),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            BasicTextField(
                value = title,
                onValueChange = { if (it.length <= 90) onTitleChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Future letter title" },
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = SerifFamily,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                ),
                singleLine = true,
                decorationBox = { inner ->
                    if (title.isBlank()) {
                        Text(
                            "A title, promise, or question",
                            style = MaterialTheme.typography.headlineSmall,
                            fontFamily = SerifFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                        )
                    }
                    inner()
                }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))

            BasicTextField(
                value = content,
                onValueChange = { if (it.length <= 12_000) onContentChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .semantics { contentDescription = "Future letter body" },
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = SerifFamily,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                ),
                decorationBox = { inner ->
                    if (content.isBlank()) {
                        Text(
                            "Write what is true now—not what sounds impressive. What do you hope changes? What deserves to remain?",
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = SerifFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f)
                        )
                    }
                    inner()
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${wordCount(content)} words",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${content.length}/12,000",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LetterAttachments(
    state: WriteMessageUiState,
    onPickPhotos: () -> Unit,
    onPickVideos: () -> Unit,
    onRecord: () -> Unit,
    onPlayVoice: () -> Unit,
    onRemoveVoice: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
        Text("Add a trace of this moment", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AttachmentTool(
                modifier = Modifier.weight(1f),
                icon = KairosIcons.Image,
                label = if (state.attachedPhotos.isEmpty()) "Photo" else "${state.attachedPhotos.size} photos",
                onClick = onPickPhotos
            )
            AttachmentTool(
                modifier = Modifier.weight(1f),
                icon = KairosIcons.PlayCircle,
                label = if (state.attachedVideos.isEmpty()) "Video" else "${state.attachedVideos.size} videos",
                onClick = onPickVideos
            )
            AttachmentTool(
                modifier = Modifier.weight(1f),
                icon = if (state.isRecording) KairosIcons.Stop else KairosIcons.Mic,
                label = if (state.isRecording) formatDuration(state.recordingTimeElapsed) else "Voice",
                selected = state.isRecording,
                onClick = onRecord
            )
        }

        AnimatedVisibility(visible = state.voiceRecordingUri != null) {
            KairosGlassSurface(
                modifier = Modifier.fillMaxWidth(),
                strong = true,
                shape = RoundedCornerShape(22.dp),
                contentPadding = PaddingValues(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KairosIconButton(
                        icon = if (state.isPlayingVoice) KairosIcons.Pause else KairosIcons.PlayArrow,
                        contentDescription = if (state.isPlayingVoice) "Pause voice note" else "Play voice note",
                        onClick = onPlayVoice
                    )
                    Column(Modifier.weight(1f)) {
                        Text("Voice note", fontWeight = FontWeight.SemiBold)
                        Text(
                            formatDuration(state.voiceRecordingDuration),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    KairosIconButton(KairosIcons.Delete, "Remove voice note", onRemoveVoice)
                }
            }
        }
    }
}

@Composable
private fun AttachmentTool(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    KairosGlassSurface(
        modifier = modifier,
        strong = selected,
        shape = RoundedCornerShape(20.dp),
        onClick = onClick,
        contentPadding = PaddingValues(vertical = 13.dp, horizontal = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(21.dp), tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    }
}

@Composable
private fun FutureDatePicker(
    initialDate: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val state = androidx.compose.material3.rememberDatePickerState(initialSelectedDateMillis = initialDate)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let(onConfirm)
                },
                enabled = (state.selectedDateMillis ?: 0L) > System.currentTimeMillis()
            ) { Text("Use date") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    ) {
        DatePicker(state = state, title = { Text("Choose when this letter arrives", modifier = Modifier.padding(24.dp)) })
    }
}

@Composable
private fun SealingLetterOverlay(saved: Boolean) {
    val reduceMotion = rememberKairosReducedMotion()
    val scale by animateFloatAsState(
        targetValue = if (saved) 0.82f else 1f,
        animationSpec = tween(if (reduceMotion) 1 else 520, easing = KairosEasing.EaseInQuart),
        label = "letter_seal_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (saved) 0.72f else 1f,
        animationSpec = tween(if (reduceMotion) 1 else 480),
        label = "letter_seal_alpha"
    )
    val scheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.scrim.copy(alpha = 0.58f)),
        contentAlignment = Alignment.Center
    ) {
        KairosGlassSurface(
            modifier = Modifier
                .padding(36.dp)
                .fillMaxWidth()
                .scale(scale)
                .alpha(alpha),
            strong = true,
            shape = RoundedCornerShape(topStart = 38.dp, topEnd = 38.dp, bottomStart = 38.dp, bottomEnd = 12.dp),
            contentPadding = PaddingValues(28.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                SealingGlyph(saved = saved, modifier = Modifier.size(116.dp))
                Text(
                    if (saved) "Letter sealed" else "Sealing your words",
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = SerifFamily,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    if (saved) "It will stay quiet until its date." else "Keeping the writing intact…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                if (!saved) CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        }
    }
}

@Composable
private fun SealingGlyph(saved: Boolean, modifier: Modifier = Modifier) {
    val progress by animateFloatAsState(
        targetValue = if (saved) 1f else 0.35f,
        animationSpec = tween(520, easing = KairosEasing.EaseOutExpo),
        label = "sealing_glyph_progress"
    )
    val scheme = MaterialTheme.colorScheme
    Canvas(modifier.semantics { contentDescription = if (saved) "Letter sealed" else "Sealing letter" }) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val envelopeWidth = size.width * 0.62f
        val envelopeHeight = size.height * 0.42f
        val left = center.x - envelopeWidth / 2f
        val right = center.x + envelopeWidth / 2f
        val top = center.y - envelopeHeight / 2f
        val bottom = center.y + envelopeHeight / 2f
        val stroke = 2.dp.toPx()

        drawCircle(scheme.primary.copy(alpha = 0.10f + progress * 0.08f), size.minDimension * 0.46f)
        drawCircle(scheme.primary.copy(alpha = 0.25f), size.minDimension * (0.31f + progress * 0.05f), style = Stroke(stroke))

        val envelope = Path().apply {
            moveTo(left, top)
            lineTo(right, top)
            lineTo(right, bottom)
            lineTo(left, bottom)
            close()
        }
        drawPath(envelope, scheme.onSurface.copy(alpha = 0.9f), style = Stroke(stroke))
        drawLine(scheme.onSurface.copy(alpha = 0.9f), Offset(left, top), Offset(center.x, center.y + envelopeHeight * 0.08f), stroke)
        drawLine(scheme.onSurface.copy(alpha = 0.9f), Offset(right, top), Offset(center.x, center.y + envelopeHeight * 0.08f), stroke)

        if (saved) {
            drawCircle(scheme.primary, radius = 12.dp.toPx(), center = Offset(center.x, bottom + 2.dp.toPx()))
            drawLine(scheme.onPrimary, Offset(center.x - 5.dp.toPx(), bottom + 2.dp.toPx()), Offset(center.x - 1.dp.toPx(), bottom + 6.dp.toPx()), stroke)
            drawLine(scheme.onPrimary, Offset(center.x - 1.dp.toPx(), bottom + 6.dp.toPx()), Offset(center.x + 6.dp.toPx(), bottom - 3.dp.toPx()), stroke)
        }
    }
}

enum class DatePreset(val label: String) {
    ONE_WEEK("1 week"),
    ONE_MONTH("1 month"),
    SIX_MONTHS("6 months"),
    ONE_YEAR("1 year"),
    CUSTOM("Choose date")
}

enum class MessageCategory(val displayName: String) {
    GENERAL("A snapshot"),
    GOAL("A direction"),
    PROMISE("A promise"),
    MOTIVATION("Encouragement")
}

private fun categoryColor(category: MessageCategory): Color = when (category) {
    MessageCategory.GENERAL -> Color(0xFF5577B8)
    MessageCategory.GOAL -> Color(0xFF7C70D9)
    MessageCategory.PROMISE -> Color(0xFF3E9B85)
    MessageCategory.MOTIVATION -> Color(0xFFD8755C)
}

private fun longDate(timestamp: Long): String =
    SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date(timestamp))

private fun shortDate(timestamp: Long): String =
    SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))

private fun relativeDate(timestamp: Long): String {
    val days = TimeUnit.MILLISECONDS.toDays((timestamp - System.currentTimeMillis()).coerceAtLeast(0L))
    return when {
        days == 0L -> "today"
        days == 1L -> "tomorrow"
        days < 30 -> "in $days days"
        days < 365 -> "in ${days / 30} months"
        else -> "in ${days / 365} years"
    }
}

private fun wordCount(text: String): Int =
    text.trim().split(Regex("\\s+")).count { it.isNotBlank() }

private fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}

package com.kairos.app.ui.capsule

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kairos.app.core.design.BackButton
import com.kairos.app.core.design.GlassCard
import com.kairos.app.core.design.GlassIconButton
import com.kairos.app.core.design.GlassPill
import com.kairos.app.core.design.InkButton
import com.kairos.app.core.design.Kai
import com.kairos.app.core.design.KaiMood
import com.kairos.app.core.design.KairosShape
import com.kairos.app.core.design.KairosTheme
import com.kairos.app.core.design.KairosType
import com.kairos.app.core.design.MarkTile
import com.kairos.app.core.util.KairosDates
import com.kairos.app.data.Attachment
import com.kairos.app.data.Capsule
import com.kairos.app.domain.capsule.CapsuleEngine
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapsuleComposerScreen(
    state: CapsulesUi,
    onBack: () -> Unit,
    onTitle: (String) -> Unit,
    onBody: (String) -> Unit,
    onMood: (String?) -> Unit,
    onUnlock: (Long) -> Unit,
    onAttach: (Uri) -> Unit,
    onRemove: (Attachment) -> Unit,
    onSeal: () -> Unit,
    onDismissSealed: () -> Unit,
    onOpenVault: () -> Unit,
    onStartDraft: () -> Unit,
) {
    val colors = KairosTheme.colors
    val draft = state.draft
    if (draft == null) {
        androidx.compose.runtime.LaunchedEffect(Unit) { onStartDraft() }
        return
    }
    val context = androidx.compose.ui.platform.LocalContext.current

    var showPicker by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri -> uri?.let(onAttach) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 22.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BackButton(onClick = onBack)
                Spacer(Modifier.weight(1f))
                Text(
                    text = "a letter for later",
                    style = KairosType.handSmall,
                    color = colors.inkFaint,
                )
            }

            Spacer(Modifier.height(26.dp))
            Text(
                text = "TO FUTURE YOU",
                style = KairosType.eyebrow,
                color = colors.inkFaint,
            )
            Spacer(Modifier.height(10.dp))

            Box {
                if (draft.title.isEmpty()) {
                    Text(
                        text = "give it a name",
                        style = MaterialTheme.typography.displaySmall,
                        color = colors.inkFaint.copy(alpha = 0.55f),
                    )
                }
                BasicTextField(
                    value = draft.title,
                    onValueChange = onTitle,
                    textStyle = TextStyle(
                        fontFamily = com.kairos.app.core.design.KairosDisplay,
                        fontSize = 36.sp,
                        lineHeight = 40.sp,
                        color = colors.ink,
                    ),
                    cursorBrush = SolidColor(colors.apricot),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(KairosShape.card)
                    .background(colors.glassFillStrong)
                    .padding(20.dp),
            ) {
                if (draft.body.isEmpty()) {
                    Text(
                        text = "Tell them what today was like. What you're hoping for. What you don't want to forget…",
                        style = KairosType.quote.copy(fontSize = 20.sp, lineHeight = 29.sp),
                        color = colors.inkFaint.copy(alpha = 0.7f),
                    )
                }
                BasicTextField(
                    value = draft.body,
                    onValueChange = onBody,
                    textStyle = TextStyle(
                        fontFamily = com.kairos.app.core.design.KairosDisplay,
                        fontSize = 20.sp,
                        lineHeight = 29.sp,
                        color = colors.ink,
                    ),
                    cursorBrush = SolidColor(colors.apricot),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                )
            }

            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("TODAY FEELS", style = KairosType.eyebrow, color = colors.inkFaint)
                if (draft.mood != null) {
                    Text(
                        text = "clear",
                        style = KairosType.handSmall,
                        color = colors.inkFaint,
                        modifier = Modifier.clickable { onMood(null) },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                CapsuleEngine.moods.take(3).forEach { mood ->
                    GlassPill(selected = draft.mood == mood, onClick = { onMood(mood) }) {
                        Text(
                            text = mood,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (draft.mood == mood) colors.paper else colors.inkSoft,
                        )
                    }
                }
            }
            Spacer(Modifier.height(7.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                CapsuleEngine.moods.drop(3).forEach { mood ->
                    GlassPill(selected = draft.mood == mood, onClick = { onMood(mood) }) {
                        Text(
                            text = mood,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (draft.mood == mood) colors.paper else colors.inkSoft,
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("OPENS ON", style = KairosType.eyebrow, color = colors.inkFaint)
                Spacer(Modifier.weight(1f))
                Text(
                    text = KairosDates.monthDayYear(
                        Instant.ofEpochMilli(draft.unlockAt).atZone(ZoneId.systemDefault()).toLocalDate(),
                    ),
                    style = KairosType.handSmall,
                    color = colors.ink,
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                state.presets.take(3).forEach { preset ->
                    GlassPill(selected = draft.unlockAt == preset.at, onClick = { onUnlock(preset.at) }) {
                        Text(
                            text = preset.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (draft.unlockAt == preset.at) colors.paper else colors.inkSoft,
                        )
                    }
                }
            }
            Spacer(Modifier.height(7.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                state.presets.drop(3).forEach { preset ->
                    GlassPill(selected = draft.unlockAt == preset.at, onClick = { onUnlock(preset.at) }) {
                        Text(
                            text = preset.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (draft.unlockAt == preset.at) colors.paper else colors.inkSoft,
                        )
                    }
                }
                GlassPill(onClick = { showPicker = true }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            tint = colors.inkSoft,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = "pick",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.inkSoft,
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("A SMALL KEEPSAKE", style = KairosType.eyebrow, color = colors.inkFaint)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                draft.attachments.forEach { attachment ->
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(RoundedCornerShape(16.dp)),
                    ) {
                        AsyncImage(
                            model = File(context.filesDir, attachment.relativePath),
                            contentDescription = attachment.displayName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.45f))
                                .clickable { onRemove(attachment) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "Remove",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp),
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.glassFill)
                        .clickable {
                            picker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.AddPhotoAlternate,
                            contentDescription = "Add a photo",
                            tint = colors.inkSoft,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (state.busy) "adding…" else "photo",
                            style = KairosType.handSmall.copy(fontSize = 14.sp),
                            color = colors.inkFaint,
                        )
                    }
                }
            }

            if (state.error != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = state.error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(Modifier.height(26.dp))
            InkButton(
                text = "Seal it",
                onClick = onSeal,
                modifier = Modifier.fillMaxWidth(),
                loading = state.busy,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "once sealed, it can't be edited. that's the whole point.",
                style = KairosType.handSmall,
                color = colors.inkFaint,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(120.dp))
        }

        if (state.sealed != null) {
            SealedCelebration(
                capsule = state.sealed,
                onKeep = onDismissSealed,
                onVault = onOpenVault,
            )
        }
    }

    if (showPicker) {
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = draft.unlockAt,
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dateState.selectedDateMillis?.let { millis ->
                            val date = LocalDate.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.UTC)
                            onUnlock(date.atTime(LocalTime.of(9, 0)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        }
                        showPicker = false
                    },
                ) { Text("Set the date") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = dateState)
        }
    }
}

@Composable
private fun SealedCelebration(capsule: Capsule, onKeep: () -> Unit, onVault: () -> Unit) {
    val colors = KairosTheme.colors
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 220f),
        label = "seal",
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.scrim)
            .clickable(enabled = false) { },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(30.dp)
                .graphicsLayer {
                    scaleX = 0.9f + 0.1f * scale
                    scaleY = 0.9f + 0.1f * scale
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(contentAlignment = Alignment.Center) {
                MarkTile(size = 96.dp, animated = true)
                Kai(
                    modifier = Modifier
                        .size(74.dp)
                        .padding(start = 74.dp, top = 60.dp),
                    mood = KaiMood.Proud,
                )
            }
            Spacer(Modifier.height(26.dp))
            Text(
                text = "Sealed.",
                style = MaterialTheme.typography.displayMedium,
                color = Color(0xFFFFF6E8),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "It will find you on ${KairosDates.monthDayYear(
                    Instant.ofEpochMilli(capsule.unlockAt).atZone(ZoneId.systemDefault()).toLocalDate(),
                )}.",
                style = KairosType.handSmall,
                color = Color(0xFFD9C7B6),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(28.dp))
            InkButton(
                text = "Back to the vault",
                onClick = onVault,
                modifier = Modifier.fillMaxWidth(),
                fill = Color(0xFFFFF6E8),
                contentColor = Color(0xFF241C3B),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "keep writing",
                style = KairosType.handSmall,
                color = Color(0xFFD9C7B6),
                modifier = Modifier.clickable(onClick = onKeep),
            )
        }
    }
}

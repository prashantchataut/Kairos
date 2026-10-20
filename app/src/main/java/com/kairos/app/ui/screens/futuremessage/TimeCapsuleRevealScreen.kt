package com.kairos.app.ui.screens.futuremessage

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.data.local.entity.FutureMessageEntity
import com.kairos.app.ui.animation.KairosDurations
import com.kairos.app.ui.animation.KairosEasing
import com.kairos.app.ui.animation.KairosReveal
import com.kairos.app.ui.animation.rememberKairosReducedMotion
import com.kairos.app.ui.components.kairos.KairosAppBackground
import com.kairos.app.ui.components.kairos.KairosEmptyState
import com.kairos.app.ui.components.kairos.KairosGlassSurface
import com.kairos.app.ui.components.kairos.KairosIconButton
import com.kairos.app.ui.components.kairos.KairosPrimaryButton
import com.kairos.app.ui.components.kairos.KairosReadingSurface
import com.kairos.app.ui.components.kairos.KairosSecondaryButton
import com.kairos.app.ui.icons.KairosIcons
import com.kairos.app.ui.security.KairosSecureScreenEffect
import com.kairos.app.ui.theme.KairosRadius
import com.kairos.app.ui.theme.KairosSpacing
import com.kairos.app.ui.theme.SerifFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Opens a future letter without exposing its content before the user chooses to
 * reveal it. The transition is ceremonial but short, skippable, and reduced-
 * motion aware.
 */
@Composable
fun TimeCapsuleRevealScreen(
    messageId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToJournal: (String) -> Unit,
    onNavigateToReply: () -> Unit = {},
    viewModel: TimeCapsuleRevealViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val reduceMotion = rememberKairosReducedMotion()

    KairosSecureScreenEffect()
    LaunchedEffect(messageId) { viewModel.loadMessage(messageId) }

    KairosAppBackground {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = KairosSpacing.screen, vertical = KairosSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                KairosIconButton(KairosIcons.ArrowBack, "Back", onNavigateBack)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("A letter arrived", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        state.timeAgoText.takeIf { it.isNotBlank() }?.let { "written $it ago" } ?: "from your past self",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                KairosIconButton(
                    icon = if (state.isFavorite) KairosIcons.Favorite else KairosIcons.FavoriteBorder,
                    contentDescription = if (state.isFavorite) "Remove from favorites" else "Save to favorites",
                    onClick = viewModel::toggleFavorite,
                    selected = state.isFavorite
                )
            }

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.error != null -> KairosEmptyState(
                    icon = KairosIcons.ErrorOutline,
                    title = "This letter is unavailable",
                    body = state.error ?: "Kairos could not open the letter.",
                    actionLabel = "Try again",
                    onAction = { viewModel.retry(messageId) }
                )
                state.message != null -> AnimatedContent(
                    targetState = state.hasBeenRevealed,
                    modifier = Modifier.weight(1f),
                    transitionSpec = {
                        if (reduceMotion) {
                            fadeIn(tween(KairosDurations.Micro)) togetherWith fadeOut(tween(KairosDurations.Micro))
                        } else {
                            (fadeIn(tween(KairosDurations.Page)) + scaleIn(initialScale = 0.97f, animationSpec = tween(KairosDurations.Page, easing = KairosEasing.EaseOutExpo))) togetherWith
                                (fadeOut(tween(KairosDurations.State)) + scaleOut(targetScale = 1.03f, animationSpec = tween(KairosDurations.State)))
                        }
                    },
                    label = "future_letter_reveal"
                ) { revealed ->
                    if (revealed) {
                        RevealedLetter(
                            message = state.message!!,
                            timeAgo = state.timeAgoText,
                            hasReply = state.hasReply,
                            onReply = onNavigateToReply,
                            onJournal = {
                                onNavigateToJournal(
                                    buildString {
                                        appendLine("# A letter from my past self")
                                        appendLine()
                                        appendLine("> ${state.message!!.content.replace("\n", "\n> ")}")
                                        appendLine()
                                        appendLine("## How it lands now")
                                        appendLine()
                                    }
                                )
                            }
                        )
                    } else {
                        ClosedLetter(
                            message = state.message!!,
                            timeAgo = state.timeAgoText,
                            onReveal = viewModel::reveal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClosedLetter(
    message: FutureMessageEntity,
    timeAgo: String,
    onReveal: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = KairosSpacing.screen, vertical = KairosSpacing.section),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        KairosReveal(visible = true, delayMillis = 50) {
            SealedEnvelope(
                accent = futureCategoryColor(message.category),
                modifier = Modifier
                    .size(230.dp)
                    .semantics {
                        contentDescription = "Sealed future letter. Double tap to open."
                        role = Role.Button
                    }
                    .clickable(onClick = onReveal)
            )
        }
        Spacer(Modifier.height(30.dp))
        Text(
            message.title.ifBlank { "A letter to you" },
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = SerifFamily,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "You wrote this ${timeAgo.ifBlank { "some time" }} ago. Open it when you are ready—not because the app demands it.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.86f)
        )
        Spacer(Modifier.height(28.dp))
        KairosPrimaryButton(
            text = "Open the letter",
            onClick = onReveal,
            icon = KairosIcons.LockOpen,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun RevealedLetter(
    message: FutureMessageEntity,
    timeAgo: String,
    hasReply: Boolean,
    onReply: () -> Unit,
    onJournal: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = KairosSpacing.screen)
            .padding(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(KairosSpacing.lg)
    ) {
        KairosReveal(visible = true, delayMillis = 20) {
            LetterProvenance(message = message, timeAgo = timeAgo)
        }
        KairosReveal(visible = true, delayMillis = 80) {
            KairosReadingSurface(
                modifier = Modifier.fillMaxWidth(),
                accent = futureCategoryColor(message.category),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 28.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Text(
                        message.title.ifBlank { "A letter from your past" },
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = SerifFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        message.content,
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = SerifFamily,
                        lineHeight = MaterialTheme.typography.headlineSmall.lineHeight
                    )
                    Text(
                        "— You, ${formatDate(message.createdAt)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = SerifFamily,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (message.attachedPhotos.isNotBlank() || message.attachedVideos.isNotBlank() || message.voiceRecordingUri != null) {
            KairosReveal(visible = true, delayMillis = 130) {
                AttachmentMemory(message)
            }
        }

        KairosReveal(visible = true, delayMillis = 170) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    if (hasReply) "You have already answered this letter." else "What would you tell the person who wrote this?",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = SerifFamily
                )
                Text(
                    "Replying creates a private conversation across time. It is optional, and it earns progress only when you actually write.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        KairosGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            strong = true,
            shape = RoundedCornerShape(KairosRadius.floating),
            contentPadding = PaddingValues(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                KairosPrimaryButton(
                    text = if (hasReply) "Read my reply" else "Reply to my past self",
                    onClick = onReply,
                    icon = KairosIcons.Send,
                    modifier = Modifier.fillMaxWidth()
                )
                KairosSecondaryButton(
                    text = "Begin a journal reflection",
                    onClick = onJournal,
                    icon = KairosIcons.Edit,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun LetterProvenance(message: FutureMessageEntity, timeAgo: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(17.dp),
            color = futureCategoryColor(message.category).copy(alpha = 0.14f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(KairosIcons.History, contentDescription = null, tint = futureCategoryColor(message.category))
            }
        }
        Column(Modifier.weight(1f)) {
            Text("Written ${timeAgo.ifBlank { "in the past" }} ago", fontWeight = FontWeight.SemiBold)
            Text(
                "Arrived ${formatDate(message.deliveryDate)} · ${message.category.replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AttachmentMemory(message: FutureMessageEntity) {
    val context = LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }
    fun openAttachment(value: String, mime: String) {
        runCatching {
            val original = Uri.parse(value)
            val uri = if (original.scheme == "file") {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(requireNotNull(original.path)))
            } else original
            val intent = Intent(Intent.ACTION_VIEW).setDataAndType(uri, mime)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(intent)
        }.onFailure { error = "This attachment could not open. It may have been moved, or this device needs an app that can play it." }
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Memories attached", style = MaterialTheme.typography.titleMedium)
        message.attachedPhotos.split(',').filter(String::isNotBlank).forEachIndexed { index, uri ->
            coil.compose.AsyncImage(model = uri, contentDescription = "Photo ${index + 1} attached to this letter", modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(24.dp)).clickable { openAttachment(uri, "image/*") })
            TextButton(onClick = { openAttachment(uri, "image/*") }) { Text("Open photo ${index + 1}") }
        }
        message.attachedVideos.split(',').filter(String::isNotBlank).forEachIndexed { index, uri ->
            TextButton(onClick = { openAttachment(uri, "video/*") }) { Text("Play video ${index + 1}") }
        }
        message.voiceRecordingUri?.let { uri ->
            TextButton(onClick = { openAttachment(uri, "audio/*") }) { Text("Listen to your voice note") }
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
    }
}

@Composable
private fun SealedEnvelope(accent: Color, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Canvas(modifier) {
        val centre = Offset(size.width / 2f, size.height / 2f)
        val width = size.width * 0.74f
        val height = size.height * 0.48f
        val left = centre.x - width / 2f
        val right = centre.x + width / 2f
        val top = centre.y - height / 2f
        val bottom = centre.y + height / 2f
        val stroke = 3.dp.toPx()

        drawCircle(accent.copy(alpha = 0.08f), radius = size.minDimension * 0.48f)
        drawCircle(accent.copy(alpha = 0.18f), radius = size.minDimension * 0.38f, style = Stroke(width = 1.5.dp.toPx()))

        val body = Path().apply {
            moveTo(left, top)
            lineTo(right, top)
            lineTo(right, bottom)
            lineTo(left, bottom)
            close()
        }
        drawPath(body, scheme.onSurface.copy(alpha = 0.9f), style = Stroke(stroke))
        drawLine(scheme.onSurface.copy(alpha = 0.9f), Offset(left, top), Offset(centre.x, centre.y + 8.dp.toPx()), stroke)
        drawLine(scheme.onSurface.copy(alpha = 0.9f), Offset(right, top), Offset(centre.x, centre.y + 8.dp.toPx()), stroke)
        drawLine(scheme.onSurface.copy(alpha = 0.42f), Offset(left, bottom), Offset(centre.x, centre.y), 1.5.dp.toPx())
        drawLine(scheme.onSurface.copy(alpha = 0.42f), Offset(right, bottom), Offset(centre.x, centre.y), 1.5.dp.toPx())

        drawCircle(accent, radius = 18.dp.toPx(), center = Offset(centre.x, centre.y + 8.dp.toPx()))
        drawCircle(scheme.surface.copy(alpha = 0.75f), radius = 6.dp.toPx(), center = Offset(centre.x, centre.y + 8.dp.toPx()))
    }
}

private fun futureCategoryColor(category: String): Color = when {
    category.contains("goal", ignoreCase = true) -> Color(0xFF7C70D9)
    category.contains("promise", ignoreCase = true) -> Color(0xFF3E9B85)
    category.contains("motivation", ignoreCase = true) -> Color(0xFFD8755C)
    else -> Color(0xFF5577B8)
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(timestamp))

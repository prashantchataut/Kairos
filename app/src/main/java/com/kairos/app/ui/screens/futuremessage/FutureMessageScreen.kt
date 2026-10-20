package com.kairos.app.ui.screens.futuremessage

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.data.local.entity.FutureMessageEntity
import com.kairos.app.ui.animation.rememberKairosReducedMotion
import com.kairos.app.ui.components.kairos.KairosAppBackground
import com.kairos.app.ui.components.kairos.KairosEmptyState
import com.kairos.app.ui.components.kairos.KairosGlassSurface
import com.kairos.app.ui.components.kairos.KairosIconButton
import com.kairos.app.ui.components.kairos.KairosPrimaryButton
import com.kairos.app.ui.components.kairos.KairosSegmentedControl
import com.kairos.app.ui.icons.KairosIcons
import com.kairos.app.ui.theme.KairosRadius
import com.kairos.app.ui.theme.KairosSpacing
import com.kairos.app.ui.theme.SerifFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun FutureMessageListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWrite: () -> Unit,
    onNavigateToMessage: (Long) -> Unit = {},
    viewModel: FutureMessageViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(if (state.unreadCount > 0) 0 else 1) }

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
                    Text("Time capsule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Something lovely to come back to",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                KairosIconButton(KairosIcons.Add, "Write a future letter", onNavigateToWrite)
            }

            KairosSegmentedControl(
                items = listOf(
                    "Arrived${if (state.unreadCount > 0) " · ${state.unreadCount}" else ""}",
                    "Sealed · ${state.pendingMessages.size}"
                ),
                selectedIndex = selectedTab,
                onSelected = { selectedTab = it },
                modifier = Modifier.padding(horizontal = KairosSpacing.screen)
            )

            AnimatedContent(
                targetState = selectedTab,
                modifier = Modifier.weight(1f),
                label = "future_letter_tabs"
            ) { tab ->
                when {
                    state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    state.error != null -> KairosEmptyState(
                        icon = KairosIcons.ErrorOutline,
                        title = "Letters unavailable",
                        body = state.error ?: "Future letters could not be loaded.",
                        actionLabel = "Try again",
                        onAction = viewModel::retry
                    )
                    tab == 0 -> ArrivedLetters(
                        messages = state.deliveredMessages,
                        onOpen = { message ->
                            onNavigateToMessage(message.id)
                        },
                        onWrite = onNavigateToWrite
                    )
                    else -> SealedLetters(
                        messages = state.pendingMessages,
                        onWrite = onNavigateToWrite
                    )
                }
            }
        }

        KairosGlassSurface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = KairosSpacing.screen, vertical = KairosSpacing.lg),
            strong = true,
            shape = RoundedCornerShape(KairosRadius.floating),
            contentPadding = PaddingValues(8.dp)
        ) {
            KairosPrimaryButton(
                text = "Write to future me",
                onClick = onNavigateToWrite,
                icon = KairosIcons.Edit,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ArrivedLetters(
    messages: List<FutureMessageEntity>,
    onOpen: (FutureMessageEntity) -> Unit,
    onWrite: () -> Unit
) {
    if (messages.isEmpty()) {
        KairosEmptyState(
            icon = KairosIcons.Mail,
            title = "Nothing has arrived yet",
            body = "Sealed letters stay quiet until the date you chose. Kairos will not tease their contents early.",
            actionLabel = "Write a letter",
            onAction = onWrite
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = KairosSpacing.screen,
            end = KairosSpacing.screen,
            top = KairosSpacing.lg,
            bottom = 112.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Messages from an earlier version of you",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = SerifFamily,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        items(messages, key = { it.id }) { message ->
            ArrivedLetterCard(message, onClick = { onOpen(message) })
        }
    }
}

@Composable
private fun ArrivedLetterCard(message: FutureMessageEntity, onClick: () -> Unit) {
    val unread = !message.isRead
    val accent = categoryColor(message.category)
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            if (unread) 1.5.dp else 1.dp,
            if (unread) accent.copy(alpha = 0.72f) else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Box(
                Modifier
                    .size(54.dp)
                    .background(accent.copy(alpha = 0.15f), RoundedCornerShape(19.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(KairosIcons.Mail, contentDescription = null, tint = accent)
                if (unread) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .size(10.dp)
                            .background(accent, CircleShape)
                    )
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    message.title.ifBlank { "A letter from your past" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    if (unread) "Ready to open" else message.content.replace('\n', ' '),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (unread) 1 else 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Written ${formatLetterDate(message.createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }
            Icon(KairosIcons.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SealedLetters(messages: List<FutureMessageEntity>, onWrite: () -> Unit) {
    if (messages.isEmpty()) {
        KairosEmptyState(
            icon = KairosIcons.Schedule,
            title = "No letters are waiting",
            body = "Write a promise, prediction, question, or honest snapshot for a date that matters.",
            actionLabel = "Seal a letter",
            onAction = onWrite
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = KairosSpacing.screen,
            end = KairosSpacing.screen,
            top = KairosSpacing.lg,
            bottom = 112.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SealedOverview(messages) }
        items(messages, key = { it.id }) { message ->
            SealedLetterCard(message)
        }
    }
}

@Composable
private fun SealedOverview(messages: List<FutureMessageEntity>) {
    val nearest = messages.minByOrNull { it.deliveryDate }
    KairosGlassSurface(
        modifier = Modifier.fillMaxWidth(),
        strong = true,
        shape = RoundedCornerShape(30.dp),
        contentPadding = PaddingValues(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SealedOrbit(Modifier.size(72.dp))
            Column {
                Text("${messages.size} sealed letter${if (messages.size == 1) "" else "s"}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    nearest?.let { "Next arrival ${relativeDelivery(it.deliveryDate)}" } ?: "Waiting for their dates",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SealedLetterCard(message: FutureMessageEntity) {
    val accent = categoryColor(message.category)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp, bottomStart = 26.dp, bottomEnd = 8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                Modifier
                    .size(48.dp)
                    .background(accent.copy(alpha = 0.14f), RoundedCornerShape(17.dp)),
                contentAlignment = Alignment.Center
            ) { Icon(KairosIcons.Lock, contentDescription = null, tint = accent) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(message.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "Sealed until ${formatLetterDate(message.deliveryDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    relativeDelivery(message.deliveryDate),
                    style = MaterialTheme.typography.labelSmall,
                    color = accent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}


@Composable
private fun rememberSealedOrbitPhase(enabled: Boolean): Float {
    if (!enabled) return 0.15f
    val transition = rememberInfiniteTransition(label = "sealed_orbit")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(7000, easing = FastOutSlowInEasing)),
        label = "sealed_orbit_phase"
    )
    return phase
}

@Composable
private fun SealedOrbit(modifier: Modifier = Modifier) {
    val reducedMotion = rememberKairosReducedMotion()
    val phase = rememberSealedOrbitPhase(enabled = !reducedMotion)
    val scheme = MaterialTheme.colorScheme
    Canvas(modifier.semantics { contentDescription = "Sealed letters waiting" }) {
        val center = Offset(size.width / 2, size.height / 2)
        drawCircle(scheme.primary.copy(alpha = 0.12f), radius = size.minDimension * 0.44f, center = center)
        drawCircle(scheme.primary.copy(alpha = 0.35f), radius = size.minDimension * 0.34f, center = center, style = Stroke(width = 2.dp.toPx()))
        val angle = (phase * Math.PI * 2).toFloat()
        val orbit = size.minDimension * 0.34f
        drawCircle(
            scheme.primary,
            radius = 4.dp.toPx(),
            center = Offset(center.x + kotlin.math.cos(angle) * orbit, center.y + kotlin.math.sin(angle) * orbit)
        )
        val envelope = Path().apply {
            moveTo(center.x - 13.dp.toPx(), center.y - 8.dp.toPx())
            lineTo(center.x + 13.dp.toPx(), center.y - 8.dp.toPx())
            lineTo(center.x + 13.dp.toPx(), center.y + 9.dp.toPx())
            lineTo(center.x - 13.dp.toPx(), center.y + 9.dp.toPx())
            close()
        }
        drawPath(envelope, scheme.onSurface.copy(alpha = 0.85f), style = Stroke(width = 2.dp.toPx()))
        drawLine(
            scheme.onSurface.copy(alpha = 0.85f),
            Offset(center.x - 13.dp.toPx(), center.y - 8.dp.toPx()),
            Offset(center.x, center.y + 1.dp.toPx()),
            2.dp.toPx()
        )
        drawLine(
            scheme.onSurface.copy(alpha = 0.85f),
            Offset(center.x + 13.dp.toPx(), center.y - 8.dp.toPx()),
            Offset(center.x, center.y + 1.dp.toPx()),
            2.dp.toPx()
        )
    }
}

private fun categoryColor(category: String): Color = when (category.lowercase(Locale.getDefault())) {
    "goal" -> Color(0xFF7C70D9)
    "promise" -> Color(0xFF3E9B85)
    "motivation" -> Color(0xFFD8755C)
    else -> Color(0xFF5577B8)
}

private fun formatLetterDate(timestamp: Long): String =
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestamp))

private fun relativeDelivery(timestamp: Long): String {
    val days = TimeUnit.MILLISECONDS.toDays((timestamp - System.currentTimeMillis()).coerceAtLeast(0L))
    return when {
        days == 0L -> "arrives today"
        days == 1L -> "arrives tomorrow"
        days < 30 -> "arrives in $days days"
        days < 365 -> "arrives in ${days / 30} months"
        else -> "arrives in ${days / 365} years"
    }
}

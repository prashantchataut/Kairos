package com.kairos.app.ui.capsule

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Markunread
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kairos.app.core.design.BackButton
import com.kairos.app.core.design.GlassCard
import com.kairos.app.core.design.GlassIconButton
import com.kairos.app.core.design.InkButton
import com.kairos.app.core.design.Kai
import com.kairos.app.core.design.KaiMood
import com.kairos.app.core.design.KairosMark
import com.kairos.app.core.design.KairosShape
import com.kairos.app.core.design.KairosTheme
import com.kairos.app.core.design.KairosType
import com.kairos.app.core.design.Tag
import com.kairos.app.core.design.WatercolorBloom
import com.kairos.app.core.util.KairosDates
import com.kairos.app.data.Capsule
import com.kairos.app.domain.capsule.CapsuleEngine
import kotlinx.coroutines.delay
import java.io.File
import java.time.Instant
import java.time.ZoneId

@Composable
fun CapsuleDetailScreen(
    capsule: Capsule,
    onBack: () -> Unit,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = KairosTheme.colors
    val context = LocalContext.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(capsule.id) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }

    val canOpen = CapsuleEngine.canOpen(capsule, now)
    val opened = capsule.openedAt != null
    val zone = ZoneId.systemDefault()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
            GlassIconButton(
                icon = Icons.Rounded.DeleteOutline,
                onClick = { confirmDelete = true },
                contentDescription = "Delete capsule",
            )
        }

        Spacer(Modifier.height(24.dp))

        AnimatedContent(
            targetState = opened,
            transitionSpec = {
                (fadeIn(tween(420)) + scaleIn(tween(520), initialScale = 0.96f))
                    .togetherWith(fadeOut(tween(200)))
            },
            label = "letter",
        ) { isOpen ->
            if (!isOpen) {
                LockedLetter(
                    capsule = capsule,
                    now = now,
                    canOpen = canOpen,
                    onOpen = onOpen,
                )
            } else {
                OpenedLetter(capsule = capsule, now = now)
            }
        }

        Spacer(Modifier.height(40.dp))
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Let this one go?") },
            text = { Text("The letter and anything inside it will be deleted. This can't be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        onDelete()
                    },
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Keep it") }
            },
        )
    }
}

@Composable
private fun LockedLetter(capsule: Capsule, now: Long, canOpen: Boolean, onOpen: () -> Unit) {
    val colors = KairosTheme.colors
    val countdown = CapsuleEngine.countdown(capsule, now)
    val zone = ZoneId.systemDefault()

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(KairosShape.large)
                .background(
                    Brush.linearGradient(listOf(Color(0xFF1C1733), Color(0xFF3B2753))),
                )
                .padding(vertical = 34.dp, horizontal = 22.dp),
        ) {
            androidx.compose.foundation.Canvas(Modifier.matchParentSize()) {
                val random = kotlin.random.Random(capsule.id.hashCode() * 7)
                repeat(50) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f + random.nextFloat() * 0.22f),
                        radius = 0.7f + random.nextFloat() * 1.6f,
                        center = androidx.compose.ui.geometry.Offset(
                            random.nextFloat() * size.width,
                            random.nextFloat() * size.height,
                        ),
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFB98C)),
                        contentAlignment = Alignment.Center,
                    ) {
                        KairosMark(modifier = Modifier.size(48.dp), tint = Color(0xFFFFE3C2))
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2A2140)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = Color(0xFFFFD9B0),
                            modifier = Modifier.size(15.dp),
                        )
                    }
                }
                Spacer(Modifier.height(22.dp))
                Text(
                    text = capsule.title.ifBlank { "an untitled letter" },
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFFFFF4E4),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "sealed ${KairosDates.monthDayYear(Instant.ofEpochMilli(capsule.createdAt).atZone(zone).toLocalDate())}",
                    style = KairosType.handSmall,
                    color = Color(0xFFB4A2CF),
                )

                Spacer(Modifier.height(26.dp))
                if (canOpen) {
                    InkButton(
                        text = "Break the seal",
                        onClick = onOpen,
                        modifier = Modifier.fillMaxWidth(),
                        fill = Color(0xFFFFF4E4),
                        contentColor = Color(0xFF241C3B),
                        icon = Icons.Rounded.Markunread,
                    )
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CountUnit(countdown.days, "days")
                        CountUnit(countdown.hours, "hrs")
                        CountUnit(countdown.minutes, "min")
                        CountUnit(countdown.seconds, "sec")
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "patience is part of the gift",
                        style = KairosType.handSmall,
                        color = Color(0xFFB4A2CF),
                    )
                }
            }
        }

        if (capsule.mood != null) {
            Spacer(Modifier.height(16.dp))
            Tag(text = "felt ${capsule.mood}", tint = colors.inkSoft)
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = "You can't peek — that's the rule that makes it worth opening.",
            style = KairosType.handSmall,
            color = colors.inkFaint,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
    }
}

@Composable
private fun CountUnit(value: Long, label: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x1AFFFFFF))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value.toString().padStart(2, '0'),
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFFFFF4E4),
        )
        Text(
            text = label,
            style = KairosType.eyebrow.copy(fontSize = 8.sp),
            color = Color(0xFFB4A2CF),
        )
    }
}

@Composable
private fun OpenedLetter(capsule: Capsule, now: Long) {
    val colors = KairosTheme.colors
    val context = LocalContext.current
    val zone = ZoneId.systemDefault()
    val paper = if (colors.dark) Color(0xFF1E1A29) else Color(0xFFFDF8EE)
    val ink = if (colors.dark) Color(0xFFF3EBDD) else Color(0xFF2A2333)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(KairosShape.large)
            .background(paper)
            .padding(24.dp),
    ) {
        WatercolorBloom(
            modifier = Modifier.size(220.dp).align(Alignment.TopEnd),
            color = colors.apricot,
            seed = capsule.id.hashCode(),
            alpha = 0.22f,
        )
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                KairosMark(modifier = Modifier.size(18.dp), tint = colors.apricot)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "A LETTER FROM ${KairosDates.monthDayYear(Instant.ofEpochMilli(capsule.createdAt).atZone(zone).toLocalDate()).uppercase()}",
                    style = KairosType.eyebrow.copy(fontSize = 9.sp),
                    color = colors.inkFaint,
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = capsule.title.ifBlank { "an untitled letter" },
                style = MaterialTheme.typography.displaySmall,
                color = ink,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = capsule.body,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = com.kairos.app.core.design.KairosDisplay,
                    fontSize = 20.sp,
                    lineHeight = 31.sp,
                ),
                color = ink.copy(alpha = 0.92f),
            )

            if (capsule.attachments.isNotEmpty()) {
                Spacer(Modifier.height(22.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    capsule.attachments.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            row.forEach { attachment ->
                                AsyncImage(
                                    model = File(context.filesDir, attachment.relativePath),
                                    contentDescription = attachment.displayName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(150.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                )
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (capsule.mood != null) Tag(text = "felt ${capsule.mood}")
                Spacer(Modifier.weight(1f))
                Text(
                    text = "opened ${KairosDates.relativeDay(
                        Instant.ofEpochMilli(capsule.openedAt ?: now).atZone(zone).toLocalDate(),
                        Instant.ofEpochMilli(now).atZone(zone).toLocalDate(),
                    )}",
                    style = KairosType.handSmall,
                    color = colors.inkFaint,
                )
            }
        }
    }

    Spacer(Modifier.height(20.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Kai(modifier = Modifier.size(64.dp), mood = KaiMood.Happy)
        Spacer(Modifier.width(12.dp))
        Text(
            text = "Different person, same handwriting. Write another one?",
            style = KairosType.handSmall,
            color = colors.inkSoft,
            modifier = Modifier.weight(1f),
        )
    }
}

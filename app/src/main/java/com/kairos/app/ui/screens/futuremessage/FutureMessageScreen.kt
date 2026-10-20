package com.kairos.app.ui.screens.futuremessage

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.ui.components.kairos.KairosGlassSurface
import com.kairos.app.ui.theme.SerifFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FutureMessageListScreen(onNavigateBack: () -> Unit, onNavigateToWrite: () -> Unit, onNavigateToMessage: (Long) -> Unit = {}, viewModel: FutureMessageViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var waiting by rememberSaveable { mutableStateOf(true) }
    val messages = if (waiting) state.pendingMessages else state.deliveredMessages
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(Modifier.widthIn(max = 720.dp).fillMaxSize().safeDrawingPadding(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = onNavigateBack) { Text("Back") }
                    TextButton(onClick = onNavigateToWrite) { Text("Write a letter") }
                }
                Text("Dear future me,", style = MaterialTheme.typography.displayMedium, fontFamily = SerifFamily)
                Text("A piece of now. A gift for later.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp))
            }
            item { CapsuleEnvelope(Modifier.fillMaxWidth().height(160.dp)) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilterChip(selected = waiting, onClick = { waiting = true }, label = { Text("Sealed (${state.pendingMessages.size})") }, modifier = Modifier.heightIn(min = 48.dp))
                    FilterChip(selected = !waiting, onClick = { waiting = false }, label = { Text("Arrived (${state.deliveredMessages.size})") }, modifier = Modifier.heightIn(min = 48.dp))
                }
                if (state.unreadCount > 0) Text("${state.unreadCount} letters ready to open", style = MaterialTheme.typography.labelLarge)
            }
            if (state.isLoading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            state.error?.let { error -> item { Text(error, color = MaterialTheme.colorScheme.error); TextButton(onClick = viewModel::retry) { Text("Try again") } } }
            if (!state.isLoading && messages.isEmpty()) item {
                Text(if (waiting) "What would you like your future self to remember?" else "Your letters will arrive here when their dates come.", style = MaterialTheme.typography.headlineSmall, fontFamily = SerifFamily)
                Text("Letters stay on this device. Keep important original files; uninstalling Kairos removes local memories.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp))
                Button(onClick = onNavigateToWrite) { Text("Seal my first letter") }
            }
            items(messages, key = { it.id }) { message ->
                KairosGlassSurface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(topStart = 12.dp, topEnd = 28.dp, bottomStart = 28.dp, bottomEnd = 28.dp), onClick = if (waiting) null else ({ onNavigateToMessage(message.id) }), contentPadding = PaddingValues(24.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date(message.deliveryDate)), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text(message.title, style = MaterialTheme.typography.headlineSmall, fontFamily = SerifFamily)
                        Text(if (waiting) "Sealed until your chosen day" else if (!message.isRead) "Your past self left something for you" else message.content.take(100), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (!waiting) TextButton(onClick = { onNavigateToMessage(message.id) }) { Text(if (message.isRead) "Read again" else "Open my letter") }
                    }
                }
            }
            item { Text("Custom dates arrive at 9 a.m. in your device's timezone. Reminders may arrive later when Android limits background work; your letter still opens on its date.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun CapsuleEnvelope(modifier: Modifier) {
    val scheme = MaterialTheme.colorScheme
    Canvas(modifier) {
        val w = size.width; val h = size.height
        drawOval(scheme.primaryContainer.copy(alpha = .5f), Offset(w*.1f, h*.1f), Size(w*.8f,h*.9f))
        val body = Path().apply { moveTo(w*.24f,h*.28f); lineTo(w*.76f,h*.22f); lineTo(w*.78f,h*.78f); lineTo(w*.25f,h*.82f); close() }
        drawPath(body, scheme.surface)
        drawPath(body, scheme.primary.copy(alpha=.25f), style=Stroke(2.dp.toPx()))
        val flap = Path().apply { moveTo(w*.24f,h*.28f); quadraticBezierTo(w*.48f,h*.80f,w*.76f,h*.22f) }
        drawPath(flap,scheme.primary.copy(alpha=.4f), style=Stroke(2.dp.toPx()))
        drawCircle(scheme.secondary, h*.11f, Offset(w*.51f,h*.56f))
        drawCircle(scheme.surface.copy(alpha=.8f), h*.035f, Offset(w*.51f,h*.56f))
    }
}

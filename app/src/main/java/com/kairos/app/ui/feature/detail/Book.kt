package com.kairos.app.ui.feature.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.engine.Topics
import com.kairos.app.ui.art.Kai
import com.kairos.app.ui.art.KaiLooks
import com.kairos.app.ui.components.BookCover
import com.kairos.app.ui.components.Card
import com.kairos.app.ui.components.Icons
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.SecondaryButton
import com.kairos.app.ui.theme.Candy
import com.kairos.app.ui.theme.Gap
import com.kairos.app.ui.theme.K
import com.kairos.app.util.appContainer
import kotlinx.coroutines.launch

/** One book's micro-summary: the cover, what it is, the key ideas, a line, and why it's for you. */
@Composable
fun BookScreen(id: String, onBack: () -> Unit) {
    val c = appContainer()
    val book by c.kairos.observeBook(id).collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()
    LaunchedEffect(id) { c.kairos.markBookRead(id) }
    val b = book ?: return
    val p = K.colors
    Column(Modifier.fillMaxSize().background(p.canvas).verticalScroll(rememberScrollState()).navigationBarsPadding().padding(bottom = Gap.huge)) {
        Box(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(p.skyTop, p.skyMid, p.canvas)))) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TopBar(onBack, onSky = true)
                Spacer(Modifier.height(Gap.md))
                BookCover(b.title, b.author, b.id, Modifier.size(width = 170.dp, height = 248.dp).graphicsLayer { rotationZ = -3f }.shadow(24.dp, RoundedCornerShape(6.dp)))
                Spacer(Modifier.height(Gap.xl))
                Text(b.title, style = K.type.shout.copy(fontSize = 32.sp, lineHeight = 34.sp), color = p.ink, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = Gap.xl))
                Spacer(Modifier.height(Gap.xs))
                Text(b.author + if (b.year > 0) ", ${b.year}" else "", style = K.type.label, color = p.inkSoft)
                Spacer(Modifier.height(Gap.sm))
                Text("${b.minutes} minute read · ${Topics.bookLabel(b.category)}", style = K.type.caption.copy(fontWeight = FontWeight.SemiBold), color = p.accent,
                    modifier = Modifier.clip(CircleShape).background(p.accentSoft).padding(horizontal = 12.dp, vertical = 5.dp))
                Spacer(Modifier.height(Gap.xl))
            }
        }
        Column(Modifier.padding(horizontal = Gap.gutter)) {
            Text(b.summary, style = K.type.body.copy(fontSize = 18.sp, lineHeight = 28.sp), color = p.ink)
            Spacer(Modifier.height(Gap.xl))
            Card(Modifier.fillMaxWidth()) {
                Text("What stays with you", style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = p.accent)
                Spacer(Modifier.height(Gap.md))
                b.ideaList.forEach { idea ->
                    Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.Top) {
                        Box(Modifier.padding(top = 7.dp).size(8.dp).clip(CircleShape).background(Candy.Coral))
                        Spacer(Modifier.width(Gap.md))
                        Text(idea, style = K.type.body, color = p.ink)
                    }
                }
            }
            if (b.line.isNotBlank()) {
                Spacer(Modifier.height(Gap.xl))
                Text("“${b.line}”", style = K.type.quote.copy(fontSize = 26.sp, lineHeight = 33.sp), color = p.ink, modifier = Modifier.padding(horizontal = Gap.sm))
            }
            if (b.hook.isNotBlank()) {
                Spacer(Modifier.height(Gap.xl))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Kai(KaiLooks.Curator.copy(gaze = Offset(0.6f, 0f)), size = 44.dp, shadow = false)
                    Spacer(Modifier.width(Gap.md))
                    Text(b.hook, style = K.type.label, color = p.inkSoft)
                }
            }
            Spacer(Modifier.height(Gap.xl))
            Row(horizontalArrangement = Arrangement.spacedBy(Gap.sm)) {
                PrimaryButton(if (b.saved) "On your shelf" else "Keep on my shelf", { scope.launch { c.kairos.toggleBookSaved(b.id) } }, Modifier.weight(1f), icon = if (b.saved) Icons.HeartOn else Icons.Heart, dark = true)
                SecondaryButton("Not for me", { scope.launch { c.kairos.setBookTaste(b.id, -1); onBack() } })
            }
        }
    }
}

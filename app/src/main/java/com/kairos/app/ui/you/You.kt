package com.kairos.app.ui.you

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.kairos.app.AppContainer
import com.kairos.app.BuildConfig
import com.kairos.app.data.db.MoodEntity
import com.kairos.app.engine.PondState
import com.kairos.app.engine.daily.Mood
import com.kairos.app.engine.learn.Srs
import com.kairos.app.engine.learn.Stage
import com.kairos.app.engine.todayEpoch
import com.kairos.app.ui.art.CritterView
import com.kairos.app.ui.art.Kiri
import com.kairos.app.ui.art.moodColor
import com.kairos.app.ui.components.DewBadge
import com.kairos.app.ui.components.Eyebrow
import com.kairos.app.ui.components.ScreenHeader
import com.kairos.app.ui.components.SectionTitle
import com.kairos.app.ui.components.enter
import com.kairos.app.ui.components.kairosViewModel
import com.kairos.app.ui.components.screenPadding
import com.kairos.app.ui.glass.GlassCard
import com.kairos.app.ui.glass.GlassIconButton
import com.kairos.app.ui.glass.GlassWeight
import com.kairos.app.ui.glass.liquidGlass
import com.kairos.app.ui.nav.LocalNavigator
import com.kairos.app.ui.nav.Routes
import com.kairos.app.ui.nav.Tab
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Radii
import com.kairos.app.ui.theme.Space
import com.kairos.app.ui.today.MoodFace
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data class YouUi(
    val name: String = "",
    val pond: PondState = PondState(0, emptyList()),
    val moods: List<MoodEntity> = emptyList(),
    val planted: Int = 0,
    val rooted: Int = 0,
    val sealed: Int = 0,
    val opened: Int = 0,
    val kind: Int = 0,
    val savedWords: Int = 0,
    val keptQuotes: Int = 0,
)

class YouViewModel(c: AppContainer) : ViewModel() {
    val state = combine(
        combine(c.settings.settings, c.pond.observe(), c.moods.observeWeek()) { s, p, m -> Triple(s, p, m) },
        c.db.words().observeAll(),
        c.capsules.observeAll(),
        c.kindness.observeTotal(),
        c.db.quotes().observeAll(),
    ) { (s, p, m), words, caps, kind, quotes ->
        YouUi(
            name = s.name, pond = p, moods = m,
            planted = words.count { it.inDeck }, rooted = words.count { Srs.stage(it) == Stage.Rooted },
            sealed = caps.size, opened = caps.count { it.openedAt != null }, kind = kind,
            savedWords = words.count { it.saved }, keptQuotes = quotes.count { it.saved },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), YouUi())
}

@Composable
fun YouScreen() {
    val vm = kairosViewModel { YouViewModel(it) }
    val ui by vm.state.collectAsStateWithLifecycle()
    val nav = LocalNavigator.current
    val colors = Kairos.colors

    LazyColumn(Modifier.fillMaxSize(), contentPadding = screenPadding()) {
        item {
            ScreenHeader(if (ui.name.isBlank()) "Your corner" else "Hello, ${ui.name}", "You", trailing = {
                GlassIconButton(Icons.Rounded.Settings, "Settings", { nav.open(Routes.SETTINGS) })
            })
        }
        item {
            val buddy = ui.pond.buddy
            GlassCard(Modifier.fillMaxWidth().enter(0), blur = true, weight = GlassWeight.Frosted, shape = Radii.sheet, onClick = { nav.open(Routes.PONDEX) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (buddy != null) CritterView(buddy.genome, size = 120.dp, name = buddy.entity.name) else Kiri(null, size = 110.dp)
                    Spacer(Modifier.width(Space.md))
                    Column(Modifier.weight(1f)) {
                        Eyebrow(if (buddy != null) "Your buddy · ${buddy.genome.rarity.label}" else "Your pond")
                        Text(buddy?.entity?.name ?: "No buddy yet", style = Kairos.type.title, color = colors.ink)
                        Text(buddy?.let { "${it.entity.name} ${it.genome.trait}." } ?: "Collect dew to hatch your first Puddling.", style = Kairos.type.bodyRegular, color = colors.inkSoft)
                        Spacer(Modifier.height(Space.sm))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DewBadge(ui.pond.dew)
                            Spacer(Modifier.width(Space.sm))
                            Text("${ui.pond.critters.size} in the pond", style = Kairos.type.label, color = colors.pond)
                        }
                    }
                }
            }
        }
        item {
            SectionTitle("This week's inner weather")
            WeatherWeek(ui.moods)
        }
        item {
            SectionTitle("Quiet evidence")
            Row(horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                Stat("${ui.planted}", "words planted", "${ui.rooted} rooted for good", colors.sky, Modifier.weight(1f))
                Stat("${ui.sealed}", "letters sent", "${ui.opened} opened", colors.lilac, Modifier.weight(1f))
            }
            Spacer(Modifier.height(Space.md))
            Row(horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                Stat("${ui.kind}", "kind things", "done for yourself", colors.mint, Modifier.weight(1f))
                Stat("${ui.pond.dew}", "dew gathered", "since you arrived", colors.peach, Modifier.weight(1f))
            }
        }
        item {
            SectionTitle("Things you kept")
            LinkRow(Icons.Rounded.Spa, "Saved words", "${ui.savedWords} words you love") { nav.tab(Tab.Learn) }
            Spacer(Modifier.height(Space.sm))
            LinkRow(Icons.Rounded.FormatQuote, "Kept thoughts", "${ui.keptQuotes} quotes that landed") { nav.open(Routes.QUOTES) }
        }
        item {
            Text(
                "Kairos ${BuildConfig.VERSION_NAME} · everything stays on this phone.",
                style = Kairos.type.caption.copy(letterSpacing = 0.sp), color = colors.inkFaint, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = Space.xxl),
            )
        }
    }
}

@Composable
private fun WeatherWeek(moods: List<MoodEntity>) {
    val colors = Kairos.colors
    val today = todayEpoch()
    GlassCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            (6 downTo 0).forEach { back ->
                val day = today - back
                val m = moods.lastOrNull { it.epochDay == day }?.mood?.let(Mood::of)
                val date = LocalDate.ofEpochDay(day)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()), style = Kairos.type.caption, color = if (back == 0) colors.ink else colors.inkSoft)
                    Spacer(Modifier.height(6.dp))
                    Box(
                        Modifier.size(38.dp)
                            .then(if (m != null) Modifier.liquidGlass(CircleShape, tint = moodColor(m).copy(alpha = 0.9f)) else Modifier.background(colors.ink.copy(alpha = 0.06f), CircleShape))
                            .semantics { contentDescription = "${date.dayOfWeek}: ${m?.label ?: "no check-in"}" },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (m != null) MoodFace(m, Modifier.size(22.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun Stat(value: String, label: String, sub: String, wash: Color, modifier: Modifier = Modifier) {
    val colors = Kairos.colors
    GlassCard(modifier) {
        Column {
            Box(Modifier.size(10.dp).background(wash, CircleShape))
            Spacer(Modifier.height(Space.sm))
            Text(value, style = Kairos.type.display.copy(fontSize = 34.sp, lineHeight = 38.sp), color = colors.ink)
            Text(label, style = Kairos.type.label, color = colors.ink)
            Text(sub, style = Kairos.type.caption.copy(letterSpacing = 0.sp), color = colors.inkSoft)
        }
    }
}

@Composable
fun LinkRow(icon: ImageVector, title: String, sub: String, onClick: () -> Unit) {
    val colors = Kairos.colors
    GlassCard(Modifier.fillMaxWidth(), onClick = onClick, contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).background(colors.sky.copy(alpha = 0.7f), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = colors.ink) }
            Spacer(Modifier.width(Space.md))
            Column(Modifier.weight(1f)) {
                Text(title, style = Kairos.type.label, color = colors.ink)
                Text(sub, style = Kairos.type.bodyRegular.copy(fontSize = 14.sp), color = colors.inkSoft)
            }
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = colors.inkSoft)
        }
    }
}

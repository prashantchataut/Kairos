package com.kairos.app.ui.screens.words

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.data.content.Taxonomy
import com.kairos.app.data.db.QuoteEntity
import com.kairos.app.data.db.WordEntity
import com.kairos.app.ui.components.EmptyState
import com.kairos.app.ui.components.HeartButton
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.ScreenTitle
import com.kairos.app.ui.components.Segmented
import com.kairos.app.ui.components.SelectChip
import com.kairos.app.ui.components.squishClickable
import com.kairos.app.ui.glass.GlassPanel
import com.kairos.app.ui.glass.liquidGlass
import com.kairos.app.ui.icons.KairosIcons
import com.kairos.app.ui.mascot.DropMascot
import com.kairos.app.ui.mascot.Expression
import com.kairos.app.ui.navigation.Scene
import com.kairos.app.ui.navigation.sharedText
import com.kairos.app.ui.paint.WashSpot
import com.kairos.app.ui.screens.today.categoryWash
import com.kairos.app.ui.screens.today.themeWash
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Radii
import com.kairos.app.ui.theme.Space
import com.kairos.app.ui.theme.Wash
import com.kairos.app.util.appViewModel

@Composable
fun LibraryScreen(openWord: (String) -> Unit, openQuote: (String) -> Unit, openPractice: () -> Unit) {
    val vm = appViewModel { LibraryViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    val filter by vm.filter.collectAsStateWithLifecycle()
    val savedOnly by vm.quotesSavedOnly.collectAsStateWithLifecycle()
    val theme by vm.theme.collectAsStateWithLifecycle()
    var section by rememberSaveable { mutableIntStateOf(0) }
    Scene(Wash.Mint, Wash.Cerulean, Wash.Butter, Wash.Lilac, Wash.Peach)

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = Space.gutter, end = Space.gutter, bottom = 130.dp),
        horizontalArrangement = Arrangement.spacedBy(Space.md),
        verticalItemSpacing = Space.md,
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            Column(Modifier.windowInsetsPadding(WindowInsets.statusBars).padding(top = Space.xl)) {
                ScreenTitle("Library", subtitle = "${state.totalWords} words to grow into, and thoughts worth keeping.")
                Spacer(Modifier.height(Space.lg))
                Segmented(listOf("Words", "Quotes"), section, { section = it }, Modifier.fillMaxWidth())
                Spacer(Modifier.height(Space.md))
                SearchField(query, { vm.query.value = it }, if (section == 0) "Search words or meanings" else "Search quotes or authors")
                Spacer(Modifier.height(Space.md))
            }
        }
        if (section == 0) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Column {
                    PracticeBanner(state.due, state.savedWords, state.mastered, openPractice)
                    Spacer(Modifier.height(Space.md))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                        items(WordFilter.entries.toList()) { f ->
                            SelectChip(f.label, f == filter, { vm.filter.value = f })
                        }
                    }
                }
            }
            if (state.words.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    EmptyState(
                        title = if (filter == WordFilter.Saved) "No saved words yet" else "Nothing matches",
                        body = if (filter == WordFilter.Saved) "Tap the heart on a word you love. Saved words come back in practice." else "Try a different search.",
                    )
                }
            }
            items(state.words, key = { it.id }) { w ->
                WordTile(w, onClick = { openWord(w.id) }, onSave = { vm.toggleWord(w.id) }, modifier = Modifier.animateItem())
            }
        } else {
            item(span = StaggeredGridItemSpan.FullLine) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                    item { SelectChip("Saved", savedOnly, { vm.quotesSavedOnly.value = !savedOnly }, leading = { Icon(KairosIcons.HeartFilled, null, Modifier.size(14.dp), tint = if (savedOnly) Kairos.colors.paper else Kairos.colors.danger) }) }
                    items(Taxonomy.quoteThemes) { t ->
                        SelectChip(t.label, theme == t.key, { vm.theme.value = if (theme == t.key) null else t.key })
                    }
                }
            }
            if (state.quotes.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    EmptyState("No quotes here yet", if (savedOnly) "Save a thought from Today and it lands here." else "Try another theme or search.")
                }
            }
            items(state.quotes, key = { it.id }) { q ->
                QuoteTile(q, onClick = { openQuote(q.id) }, onSave = { vm.toggleQuote(q.id) }, modifier = Modifier.animateItem())
            }
        }
    }
}

@Composable
fun SearchField(value: String, onChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier) {
    val palette = Kairos.colors
    Row(
        modifier
            .fillMaxWidth()
            .height(50.dp)
            .liquidGlass(CircleShape, blur = false, elevation = 0.dp, strong = true)
            .padding(horizontal = Space.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(KairosIcons.Search, null, tint = palette.inkSoft, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(Space.md))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text(hint, style = Kairos.type.body, color = palette.inkFaint)
            BasicTextField(
                value, onChange, singleLine = true,
                textStyle = Kairos.type.body.copy(color = palette.ink),
                cursorBrush = SolidColor(palette.accent),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (value.isNotEmpty()) {
            Icon(KairosIcons.Close, "Clear search", tint = palette.inkSoft, modifier = Modifier.size(20.dp).squishClickable { onChange("") })
        }
    }
}

@Composable
private fun PracticeBanner(due: Int, saved: Int, mastered: Int, onPractice: () -> Unit) {
    val palette = Kairos.colors
    GlassPanel(Modifier.fillMaxWidth(), blur = true) {
        Row(Modifier.padding(Space.lg), verticalAlignment = Alignment.CenterVertically) {
            DropMascot(size = 64.dp, wash = Wash.Mint, expression = if (due > 0) Expression.Wow else Expression.Happy, interactive = false)
            Spacer(Modifier.width(Space.md))
            Column(Modifier.weight(1f)) {
                Text(
                    if (due > 0) "$due ${if (due == 1) "word is" else "words are"} ready to review" else "Practice keeps words",
                    style = Kairos.type.title, color = palette.ink,
                )
                Text("$saved saved, $mastered remembered for good", style = Kairos.type.caption, color = palette.inkSoft)
                Spacer(Modifier.height(Space.sm))
                PrimaryButton(if (due > 0) "Start review" else "Practise", onPractice, icon = KairosIcons.Cards)
            }
        }
    }
}

/** Word tiles vary in height with their content, giving the board its Pinterest rhythm. */
@Composable
private fun WordTile(w: WordEntity, onClick: () -> Unit, onSave: () -> Unit, modifier: Modifier = Modifier) {
    val palette = Kairos.colors
    val wash = categoryWash(w.category).color(palette.isDark)
    Box(
        modifier
            .fillMaxWidth()
            .squishClickable(pressedScale = 0.96f, haptic = false, onClick = onClick)
            .liquidGlass(Radii.Card, blur = false, elevation = 6.dp, tint = palette.card)
    ) {
        WashSpot(wash, w.id.hashCode(), Modifier.size(150.dp).align(Alignment.TopEnd).offset(x = 50.dp, y = (-50).dp))
        Column(Modifier.padding(Space.lg)) {
            Text(Taxonomy.wordLabel(w.category), style = Kairos.type.caption, color = palette.inkFaint)
            Spacer(Modifier.height(Space.xs))
            Text(
                w.word,
                style = Kairos.type.display.copy(fontSize = Kairos.type.title.fontSize * 1.2f, lineHeight = Kairos.type.title.lineHeight * 1.2f),
                color = palette.ink,
                modifier = Modifier.sharedText("word-${w.id}-list"),
            )
            Text(w.partOfSpeech, style = Kairos.type.caption.copy(fontStyle = FontStyle.Italic), color = palette.inkSoft)
            Spacer(Modifier.height(Space.sm))
            Text(
                w.definition, style = Kairos.type.label.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
                color = palette.inkSoft, maxLines = if (w.word.length % 3 == 0) 5 else 3, overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(Space.sm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (w.mastered) LearnedDot("Remembered", palette.success)
                else if (w.inPractice) LearnedDot("Learning", palette.accent)
                Spacer(Modifier.weight(1f))
                HeartButton(w.saved, onSave, size = 38.dp, glass = false)
            }
        }
    }
}

@Composable
private fun LearnedDot(label: String, color: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text(label, style = Kairos.type.caption, color = color)
    }
}

@Composable
private fun QuoteTile(q: QuoteEntity, onClick: () -> Unit, onSave: () -> Unit, modifier: Modifier = Modifier) {
    val palette = Kairos.colors
    val wash = themeWash(q.theme).color(palette.isDark)
    Box(
        modifier
            .fillMaxWidth()
            .squishClickable(pressedScale = 0.96f, haptic = false, onClick = onClick)
            .liquidGlass(Radii.Card, blur = false, elevation = 6.dp, tint = palette.card)
    ) {
        WashSpot(wash, q.id.hashCode(), Modifier.size(140.dp).align(Alignment.BottomStart).offset(x = (-50).dp, y = 50.dp))
        Column(Modifier.padding(Space.lg)) {
            Icon(KairosIcons.Quote, null, tint = wash, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(Space.sm))
            Text(q.text, style = Kairos.type.quote.copy(fontSize = Kairos.type.body.fontSize * 1.05f, lineHeight = Kairos.type.body.lineHeight * 1.05f), color = palette.ink)
            Spacer(Modifier.height(Space.sm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(q.author, style = Kairos.type.caption, color = palette.inkSoft, modifier = Modifier.weight(1f), maxLines = 2)
                HeartButton(q.saved, onSave, size = 38.dp, glass = false)
            }
        }
    }
}

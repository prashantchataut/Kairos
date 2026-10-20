package com.kairos.app.ui.garden

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.data.local.entity.*
import com.kairos.app.domain.futuremessage.CapsulePolicy
import com.kairos.app.ui.animation.rememberKairosReducedMotion
import com.kairos.app.ui.theme.SerifFamily
import com.kairos.app.util.ShareManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val Page = 24.dp
private val Shape = RoundedCornerShape(28.dp)
private val DateLabel = DateTimeFormatter.ofPattern("d MMM yyyy")

@Composable
fun GardenExperience(onOpenTools: () -> Unit, vm: GardenViewModel = hiltViewModel()) {
    GardenTheme {
        val setupComplete by vm.setupComplete.collectAsStateWithLifecycle()
        val settingUp by vm.settingUp.collectAsStateWithLifecycle()
        val words by vm.words.collectAsStateWithLifecycle()
        val quotes by vm.thoughts.collectAsStateWithLifecycle()
        val letters by vm.letters.collectAsStateWithLifecycle()
        val plan by vm.plan.collectAsStateWithLifecycle()
        val error by vm.error.collectAsStateWithLifecycle()
        val saving by vm.saving.collectAsStateWithLifecycle()
        var tab by rememberSaveable { mutableIntStateOf(0) }
        var composing by rememberSaveable { mutableStateOf(false) }
        var openedId by rememberSaveable { mutableStateOf<Long?>(null) }
        var printId by rememberSaveable { mutableStateOf<Long?>(null) }
        var showThoughts by rememberSaveable { mutableStateOf(false) }
        var practice by rememberSaveable { mutableStateOf(false) }
        val context = LocalContext.current
        val lifecycle = LocalLifecycleOwner.current
        DisposableEffect(lifecycle) {
            val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) vm.refresh() }
            lifecycle.lifecycle.addObserver(observer)
            onDispose { lifecycle.lifecycle.removeObserver(observer) }
        }
        var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
        LaunchedEffect(Unit) {
            while (true) { delay(30_000); now = System.currentTimeMillis(); vm.refresh() }
        }
        val reduced = rememberKairosReducedMotion()
        val snackbar = remember { SnackbarHostState() }
        LaunchedEffect(error) { error?.let { snackbar.showSnackbar(it); vm.clearError() } }
        BackHandler(enabled = tab != 0 || composing || openedId != null || printId != null || practice || showThoughts) {
            when { showThoughts -> showThoughts = false; printId != null -> printId = null; openedId != null -> openedId = null; practice -> practice = false; !composing -> tab = 0 }
        }
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                if (setupComplete == true && !composing && openedId == null && printId == null && !practice && !showThoughts) {
                    Surface(modifier = Modifier.navigationBarsPadding().padding(horizontal=24.dp, vertical=12.dp),
                        shape=RoundedCornerShape(32.dp), color=MaterialTheme.colorScheme.surface.copy(alpha=.94f), tonalElevation=3.dp, shadowElevation=8.dp) {
                        Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement=Arrangement.SpaceEvenly) {
                            listOf("Today" to Icons.Outlined.WbSunny,"Words" to Icons.Outlined.AutoStories,"Capsules" to Icons.Outlined.MailOutline,"Your space" to Icons.Outlined.Spa).forEachIndexed { i, (label, icon) ->
                                NavigationBarItem(selected=tab==i,onClick={tab=i},icon={Icon(icon,null)},label={Text(label,style=MaterialTheme.typography.labelSmall)},
                                    colors=NavigationBarItemDefaults.colors(indicatorColor=MaterialTheme.colorScheme.primaryContainer))
                            }
                        }
                    }
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding).statusBarsPadding(), contentAlignment=Alignment.TopCenter) {
                val selectedWord = words.firstOrNull { it.id == printId }
                val opened = letters.firstOrNull { it.id == openedId }
                when {
                    setupComplete == null -> CircularProgressIndicator(Modifier.padding(48.dp))
                    setupComplete == false -> GardenSetup(settingUp, vm::finishSetup)
                    selectedWord != null -> WordPrintStudio(selectedWord, { printId=null })
                    composing -> CapsuleComposer(vm, { composing=false })
                    opened != null && CapsulePolicy.canOpen(opened.deliveryDate,System.currentTimeMillis()) -> CapsuleLetter(opened,{openedId=null})
                    showThoughts -> ThoughtGarden(quotes, vm::saveThought, { showThoughts=false })
                    practice -> RecallGarden(words, saving, vm::review, {practice=false})
                    else -> AnimatedContent(targetState=tab, transitionSpec={
                        (fadeIn(tween(if(reduced) 0 else 220)) togetherWith fadeOut(tween(if(reduced) 0 else 120))).using(SizeTransform(clip=false))
                    },label="garden-destination") { destination ->
                        when(destination) {
                            0 -> GardenToday(
                                word=plan?.word?.item?.let { daily -> words.firstOrNull { it.id==daily.id } },
                                quote=plan?.quote?.item?.let { daily -> quotes.firstOrNull { it.id==daily.id } },
                                onWord={tab=1},onCapsule={composing=true},onPrint={printId=it.id},
                                onSaveWord=vm::saveWord,onSaveQuote=vm::saveThought,onRetry={vm.refresh()},saving=saving
                            )
                            1 -> WordGarden(words,saving,vm::saveWord,{printId=it.id},{practice=true})
                            2 -> CapsuleGarden(letters,now,{composing=true}) { vm.open(it); openedId=it.id }
                            else -> GardenSpace(onOpenTools,{composing=true},{tab=1},{showThoughts=true})
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GardenHeader(kicker: String, title: String, subtitle: String, back: (() -> Unit)? = null) {
    Column(Modifier.fillMaxWidth().padding(top=16.dp,bottom=8.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically) {
            if(back!=null) IconButton(onClick=back) { Icon(Icons.Outlined.ArrowBack,"Back") } else SeedMark(Modifier.size(36.dp))
            Text(kicker,style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.primary,modifier=Modifier.padding(start=8.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(title,fontFamily=SerifFamily,fontSize=36.sp,lineHeight=42.sp,modifier=Modifier.semantics { heading() })
        Spacer(Modifier.height(8.dp))
        Text(subtitle,style=MaterialTheme.typography.bodyLarge,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun GardenToday(word: VocabularyEntity?,quote: QuoteEntity?,onWord:()->Unit,onCapsule:()->Unit,onPrint:(VocabularyEntity)->Unit,onSaveWord:(VocabularyEntity)->Unit,onSaveQuote:(QuoteEntity)->Unit,onRetry:()->Unit,saving:Boolean) {
    LazyColumn(Modifier.widthIn(max=720.dp).fillMaxSize(),contentPadding=PaddingValues(Page),verticalArrangement=Arrangement.spacedBy(24.dp)) {
        item { GardenHeader("KAIROS  /  ${LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, d MMM"))}","Make room for\nsomething good.","One word. One thought. A moment that's yours.") }
        item {
            Surface(shape=Shape,color=MaterialTheme.colorScheme.primaryContainer) {
                Column(Modifier.fillMaxWidth().padding(24.dp)) {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("TODAY'S LITTLE DISCOVERY",style=MaterialTheme.typography.labelSmall)
                            Spacer(Modifier.height(20.dp))
                            Text(word?.word ?: "A word is taking root",fontFamily=SerifFamily,fontSize=32.sp,lineHeight=38.sp)
                            word?.let { Text("${it.partOfSpeech}  ${it.pronunciation}",style=MaterialTheme.typography.bodySmall,modifier=Modifier.padding(top=8.dp)) }
                        }
                        GardenPainting(Modifier.size(112.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(word?.definition ?: "Your offline library is getting ready. Tap below to try again.",style=MaterialTheme.typography.bodyLarge)
                    word?.exampleSentence?.takeIf { it.isNotBlank() }?.let { Text(it,fontFamily=SerifFamily,fontStyle=FontStyle.Italic,modifier=Modifier.padding(top=16.dp)) }
                    Spacer(Modifier.height(20.dp))
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        FilledTonalButton(onClick=if(word==null) onRetry else onWord) { Text(if(word==null) "Try again" else "Explore words") }
                        Spacer(Modifier.weight(1f))
                        if(word!=null) {
                            IconButton(onClick={onSaveWord(word)},enabled=!saving) { Icon(if(word.isFavorite) Icons.Outlined.BookmarkAdded else Icons.Outlined.BookmarkBorder,if(word.isFavorite) "Unsave word" else "Save word") }
                            IconButton(onClick={onPrint(word)}) { Icon(Icons.Outlined.IosShare,"Create Word Print") }
                        }
                    }
                }
            }
        }
        item {
            Column(Modifier.padding(vertical=8.dp)) {
                Text("A THOUGHT TO CARRY",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(16.dp))
                Text(quote?.let { "“${it.content}”" } ?: "Your next thought is on its way.",fontFamily=SerifFamily,fontSize=24.sp,lineHeight=34.sp)
                Row(verticalAlignment=Alignment.CenterVertically) {
                    Text(quote?.let { "— ${it.author}" }.orEmpty(),Modifier.weight(1f).padding(top=16.dp),style=MaterialTheme.typography.bodyMedium)
                    if(quote!=null) IconButton(onClick={onSaveQuote(quote)}) { Icon(if(quote.isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,"Save or unsave thought") }
                }
            }
        }
        item { Surface(modifier=Modifier.gardenPress(onCapsule),shape=Shape,color=MaterialTheme.colorScheme.secondaryContainer) {
            Row(Modifier.padding(20.dp),verticalAlignment=Alignment.CenterVertically) {
                GardenPainting(Modifier.size(96.dp),letter=true)
                Column(Modifier.weight(1f).padding(start=12.dp)) { Text("Hello, future me.",fontFamily=SerifFamily,fontSize=24.sp);Text("Leave a little piece of today for later.",modifier=Modifier.padding(top=8.dp)) }
                Icon(Icons.Outlined.ArrowOutward,null)
            }
        } }
        item { Text("Growing slowly still counts.",fontFamily=SerifFamily,fontStyle=FontStyle.Italic,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(bottom=16.dp)) }
    }
}

@Composable
private fun WordGarden(words:List<VocabularyEntity>,saving:Boolean,onSave:(VocabularyEntity)->Unit,onPrint:(VocabularyEntity)->Unit,onPractice:()->Unit) {
    var query by rememberSaveable { mutableStateOf("") };var savedOnly by rememberSaveable { mutableStateOf(false) }
    val filtered=remember(words,query,savedOnly) { words.filter { (!savedOnly||it.isFavorite) && (it.word.contains(query,true)||it.definition.contains(query,true)) && !it.origin.startsWith("kairos:family:") } }
    LazyColumn(Modifier.widthIn(max=720.dp).fillMaxSize(),contentPadding=PaddingValues(Page),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        item { GardenHeader("THE WORD GARDEN","Find your next\nfavorite word.","Collect a word. Use it. Make it yours.") }
        item { OutlinedTextField(query,{query=it},label={Text("Search words and meanings")},leadingIcon={Icon(Icons.Outlined.Search,null)},singleLine=true,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp)) }
        item { Row(horizontalArrangement=Arrangement.spacedBy(12.dp),verticalAlignment=Alignment.CenterVertically) { FilterChip(selected=savedOnly,onClick={savedOnly=!savedOnly},label={Text("Saved words")});OutlinedButton(onClick=onPractice,enabled=words.isNotEmpty()) { Icon(Icons.Outlined.Psychology,null);Text(" Recall",modifier=Modifier.padding(start=4.dp)) } } }
        if(filtered.isEmpty()) item { Text(if(query.isNotEmpty()) "No words found. Try another spelling." else if(savedOnly) "Save a word that speaks to you. It will grow here." else "The offline library is preparing. Come back in a moment.",modifier=Modifier.padding(vertical=32.dp)) }
        items(filtered,key={it.id}) { word ->
            Surface(shape=Shape,color=MaterialTheme.colorScheme.surface) {
                Column(Modifier.fillMaxWidth().padding(24.dp)) {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Text(word.word,fontFamily=SerifFamily,fontSize=28.sp,modifier=Modifier.weight(1f))
                        IconButton(onClick={onSave(word)},enabled=!saving) { Icon(if(word.isFavorite) Icons.Outlined.BookmarkAdded else Icons.Outlined.BookmarkBorder,"Save or unsave ${word.word}") }
                    }
                    Text("${word.partOfSpeech}  ${word.pronunciation}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(word.definition,modifier=Modifier.padding(top=16.dp),style=MaterialTheme.typography.bodyLarge)
                    if(word.exampleSentence.isNotBlank()) Text(word.exampleSentence,fontFamily=SerifFamily,fontStyle=FontStyle.Italic,modifier=Modifier.padding(top=12.dp))
                    TextButton(onClick={onPrint(word)},modifier=Modifier.align(Alignment.End)) { Icon(Icons.Outlined.IosShare,null);Text(" Make a Word Print") }
                }
            }
        }
    }
}

@Composable
private fun CapsuleGarden(letters:List<FutureMessageEntity>,now:Long,onCompose:()->Unit,onOpen:(FutureMessageEntity)->Unit) {
    LazyColumn(Modifier.widthIn(max=720.dp).fillMaxSize(),contentPadding=PaddingValues(Page),verticalArrangement=Arrangement.spacedBy(20.dp)) {
        item { GardenHeader("THE TIME CAPSULE","A little now.\nA lovely later.","Letters and keepsakes for the person you're becoming.") }
        item { GardenPainting(Modifier.fillMaxWidth().height(180.dp),letter=true) }
        item { Button(onClick=onCompose,modifier=Modifier.fillMaxWidth().heightIn(min=52.dp)) { Icon(Icons.Outlined.Edit,null);Text(" Write to future me",modifier=Modifier.padding(start=8.dp)) } }
        item { Text("Kept privately on this device. Uninstalling removes local letters and keepsakes.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant) }
        if(letters.isEmpty()) item { Text("Your first letter starts with today.",fontFamily=SerifFamily,fontSize=24.sp,modifier=Modifier.padding(vertical=24.dp)) }
        items(letters,key={it.id}) { letter ->
            val ready=CapsulePolicy.canOpen(letter.deliveryDate,now)
            Surface(onClick={if(ready)onOpen(letter)},enabled=ready,shape=Shape,color=if(ready)MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface) {
                Row(Modifier.fillMaxWidth().padding(24.dp),verticalAlignment=Alignment.CenterVertically) {
                    Icon(if(ready) Icons.Outlined.MarkEmailRead else Icons.Outlined.Lock,null,modifier=Modifier.size(28.dp))
                    Column(Modifier.weight(1f).padding(horizontal=16.dp)) {
                        Text(letter.title,fontFamily=SerifFamily,fontSize=20.sp)
                        Text((if(ready) "Ready to open · " else "Sealed until ")+Instant.ofEpochMilli(letter.deliveryDate).atZone(ZoneId.systemDefault()).toLocalDate().format(DateLabel),style=MaterialTheme.typography.bodySmall,modifier=Modifier.padding(top=8.dp))
                    }
                    if(ready) Icon(Icons.Outlined.ArrowForward,null)
                }
            }
        }
    }
}

@Composable
private fun CapsuleComposer(vm:GardenViewModel,onClose:()->Unit) {
    var title by rememberSaveable { mutableStateOf("") };var body by rememberSaveable { mutableStateOf("") }
    var dateText by rememberSaveable { mutableStateOf(LocalDate.now().plusMonths(1).toString()) }
    var discard by remember { mutableStateOf(false) }
    val files by vm.draftFiles.collectAsStateWithLifecycle();val copying by vm.copying.collectAsStateWithLifecycle()
    val sealing by vm.sealing.collectAsStateWithLifecycle();val sealed by vm.sealed.collectAsStateWithLifecycle()
    val context=LocalContext.current
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { vm.addAttachments(it) }
    val close:()->Unit={if(body.isNotBlank()||title.isNotBlank()||files.isNotEmpty())discard=true else onClose()}
    BackHandler { if(!sealing&&!copying) close() }
    if(discard) AlertDialog(onDismissRequest={discard=false},title={Text("Leave this letter?")},text={Text("Your unsaved writing and attachments will be discarded.")},confirmButton={TextButton(onClick={vm.discardDraft();onClose()}) { Text("Discard draft") }},dismissButton={TextButton(onClick={discard=false}) { Text("Keep writing") }})
    if(sealed) {
        BackHandler { vm.consumeSealed(); onClose() }
        Column(Modifier.fillMaxSize().padding(32.dp),verticalArrangement=Arrangement.Center,horizontalAlignment=Alignment.CenterHorizontally) {
            GardenPainting(Modifier.size(220.dp),letter=true)
            Text("A moment, safely kept.",fontFamily=SerifFamily,fontSize=32.sp,lineHeight=40.sp)
            Text("Your letter opens on ${LocalDate.parse(dateText).format(DateLabel)}.",modifier=Modifier.padding(vertical=24.dp))
            Button(onClick={vm.consumeSealed();onClose()}) { Text("Back to capsules") }
        };return
    }
    LazyColumn(Modifier.widthIn(max=720.dp).fillMaxSize().imePadding(),contentPadding=PaddingValues(Page),verticalArrangement=Arrangement.spacedBy(20.dp)) {
        item { GardenHeader("A LETTER THROUGH TIME","Dear future me,","What would you love to remember about right now?",{if(!sealing&&!copying)close()}) }
        item { OutlinedTextField(title,{title=it},label={Text("Give your letter a name")},enabled=!sealing,modifier=Modifier.fillMaxWidth(),shape=Shape) }
        item { OutlinedTextField(body,{body=it},label={Text("Your letter")},placeholder={Text("Today felt like…\nOne thing I hope you remember…")},enabled=!sealing,modifier=Modifier.fillMaxWidth().heightIn(min=240.dp),shape=Shape) }
        item {
            Column {
                Text("OPEN ON",style=MaterialTheme.typography.labelSmall)
                TextButton(onClick={
                    val d=LocalDate.parse(dateText)
                    DatePickerDialog(context,{_,y,m,day->dateText=LocalDate.of(y,m+1,day).toString()},d.year,d.monthValue-1,d.dayOfMonth).apply { datePicker.minDate=System.currentTimeMillis()+86400000L;show() }
                },enabled=!sealing) { Icon(Icons.Outlined.CalendarToday,null);Text(LocalDate.parse(dateText).format(DateLabel),modifier=Modifier.padding(start=12.dp)) }
                Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    listOf("1 week" to LocalDate.now().plusWeeks(1),"1 month" to LocalDate.now().plusMonths(1),"1 year" to LocalDate.now().plusYears(1)).forEach { (label,d) ->
                        FilterChip(selected=dateText==d.toString(),onClick={dateText=d.toString()},enabled=!sealing,label={Text(label)})
                    }
                }
            }
        }
        item { OutlinedButton(onClick={picker.launch(arrayOf("image/*","video/*","audio/*","application/pdf"))},enabled=!copying&&!sealing&&files.size<6,modifier=Modifier.fillMaxWidth()) { Icon(Icons.Outlined.AttachFile,null);Text(if(copying) " Keeping your attachment…" else " Add a keepsake (${files.size}/6)") } }
        items(files,key={it}) { file -> Row(verticalAlignment=Alignment.CenterVertically) { Icon(Icons.Outlined.InsertDriveFile,null);Text("Kept attachment ${files.indexOf(file)+1}",Modifier.weight(1f).padding(12.dp));IconButton(onClick={vm.removeAttachment(file)},enabled=!sealing) { Icon(Icons.Outlined.Close,"Remove attachment") } } }
        item { Text("Up to six files, 25 MB each. Your letter stays sealed until its date; notifications depend on your device settings.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant) }
        item { Button(onClick={vm.seal(title,body,LocalDate.parse(dateText))},enabled=!sealing&&!copying&&body.isNotBlank(),modifier=Modifier.fillMaxWidth().heightIn(min=56.dp)) { if(sealing) CircularProgressIndicator(Modifier.size(20.dp),strokeWidth=2.dp) else Icon(Icons.Outlined.Lock,null);Text(if(sealing) " Sealing…" else " Seal my capsule") } }
    }
}

@Composable
private fun CapsuleLetter(letter:FutureMessageEntity,onBack:()->Unit) {
    val context=LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }
    LazyColumn(Modifier.widthIn(max=720.dp).fillMaxSize(),contentPadding=PaddingValues(Page),verticalArrangement=Arrangement.spacedBy(24.dp)) {
        item { GardenHeader("A MOMENT RETURNED",letter.title,"Written ${Instant.ofEpochMilli(letter.createdAt).atZone(ZoneId.systemDefault()).toLocalDate().format(DateLabel)}",onBack) }
        item { Text(letter.content,fontFamily=SerifFamily,fontSize=20.sp,lineHeight=32.sp) }
        val files=(letter.attachedPhotos.split(',')+letter.attachedVideos.split(',')+listOfNotNull(letter.voiceRecordingUri)).filter { it.isNotBlank() }
        items(files) { uri ->
            if(context.contentResolver.getType(Uri.parse(uri))?.startsWith("image/")==true) coil.compose.AsyncImage(model=uri,contentDescription="A photo kept with your letter",modifier=Modifier.fillMaxWidth().heightIn(max=400.dp).clip(Shape))
            OutlinedButton(onClick={
                runCatching {
                    val parsed=Uri.parse(uri)
                    context.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(parsed,context.contentResolver.getType(parsed)?:"*/*").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
                }.onFailure { error="No app could open this keepsake. Install a compatible viewer and try again." }
            }) { Icon(Icons.Outlined.AttachFile,null);Text(" Open keepsake") }
        }
        error?.let { item { Text(it,color=MaterialTheme.colorScheme.error) } }
        item { Text("Look how far you've come.",fontFamily=SerifFamily,fontStyle=FontStyle.Italic,modifier=Modifier.padding(vertical=24.dp)) }
    }
}

@Composable
private fun WordPrintStudio(word:VocabularyEntity,onBack:()->Unit) {
    var paper by rememberSaveable { mutableIntStateOf(0) };val style=WordPrintRenderer.Paper.entries[paper]
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) };var exporting by remember { mutableStateOf(false) };var failure by remember { mutableStateOf<String?>(null) }
    val context=LocalContext.current;val scope=rememberCoroutineScope()
    LaunchedEffect(word.id,paper) { bitmap=withContext(Dispatchers.Default) { WordPrintRenderer.render(word.word,word.definition,word.pronunciation,style) } }
    LazyColumn(Modifier.widthIn(max=600.dp).fillMaxSize(),contentPadding=PaddingValues(Page),verticalArrangement=Arrangement.spacedBy(20.dp)) {
        item { GardenHeader("KAIROS WORD PRINTS","A word worth keeping.","A pocket-sized piece of the word garden.",onBack) }
        item { bitmap?.let { Image(it.asImageBitmap(),contentDescription="${style.label} Word Print preview for ${word.word}",modifier=Modifier.fillMaxWidth().aspectRatio(.8f).clip(Shape)) } }
        item { Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) { WordPrintRenderer.Paper.entries.forEachIndexed { i,p -> FilterChip(selected=paper==i,onClick={paper=i},label={Text(p.label)}) } } }
        failure?.let { item { Text(it,color=MaterialTheme.colorScheme.error) } }
        item { Button(onClick={
            exporting=true
            scope.launch {
                val ok=withContext(Dispatchers.IO) { ShareManager(context.applicationContext).shareWordPrint(word.word,word.definition,word.pronunciation,style) }
                exporting=false;if(!ok)failure="Could not share this print. Please try again."
            }
        },enabled=!exporting,modifier=Modifier.fillMaxWidth().heightIn(min=52.dp)) { Icon(Icons.Outlined.IosShare,null);Text(if(exporting) " Preparing print…" else " Share Word Print") } }
    }
}

@Composable
private fun RecallGarden(words:List<VocabularyEntity>,saving:Boolean,onGrade:(VocabularyEntity,Boolean,()->Unit)->Unit,onBack:()->Unit) {
    var index by rememberSaveable { mutableIntStateOf(0) };var revealed by rememberSaveable { mutableStateOf(false) }
    val queueIds = remember { com.kairos.app.domain.learning.GardenRecallQueue.select(words.map {
        com.kairos.app.domain.learning.GardenRecallQueue.Seed(it.id,it.isFavorite,it.isLearned,it.nextReviewAt)
    },System.currentTimeMillis()) }
    val word=words.firstOrNull { it.id == queueIds.getOrNull(index) }
    Column(Modifier.widthIn(max=720.dp).fillMaxSize().verticalScroll(rememberScrollState()).padding(Page),verticalArrangement=Arrangement.spacedBy(24.dp)) {
        GardenHeader("A LITTLE RECALL",if(word==null) "Room to grow." else "Let it come back.",if(word==null) "You've reached the end of this little practice." else "${index+1} of ${queueIds.size} · What does this word mean?",onBack)
        if(word!=null) {
            GardenPainting(Modifier.align(Alignment.CenterHorizontally).size(160.dp))
            Text(word.word,fontFamily=SerifFamily,fontSize=36.sp,lineHeight=44.sp)
            if(revealed) {
                Text(word.definition,style=MaterialTheme.typography.bodyLarge)
                if(word.exampleSentence.isNotBlank()) Text(word.exampleSentence,fontFamily=SerifFamily,fontStyle=FontStyle.Italic)
                Button(onClick={onGrade(word,true) { index++;revealed=false }},enabled=!saving,modifier=Modifier.fillMaxWidth()) { Text("I remembered") }
                OutlinedButton(onClick={onGrade(word,false) { index++;revealed=false }},enabled=!saving,modifier=Modifier.fillMaxWidth()) { Text("Let's meet again") }
            } else Button(onClick={revealed=true},modifier=Modifier.fillMaxWidth()) { Text("Reveal meaning") }
        } else Button(onClick=onBack) { Text("Back to the garden") }
    }
}

@Composable
private fun GardenSpace(onTools:()->Unit,onCapsule:()->Unit,onWords:()->Unit,onThoughts:()->Unit) {
    LazyColumn(Modifier.widthIn(max=720.dp).fillMaxSize(),contentPadding=PaddingValues(Page),verticalArrangement=Arrangement.spacedBy(24.dp)) {
        item { GardenHeader("YOUR OWN LITTLE SPACE","A softer kind\nof progress.","There is no perfect pace. Just a little more you.") }
        item { GardenPainting(Modifier.fillMaxWidth().height(220.dp)) }
        item { SpaceLink("A thought to carry","Quotes, sources and your saved thoughts",Icons.Outlined.FormatQuote,onThoughts) }
        item { SpaceLink("Keep a thought","Journal, reflections and your existing tools",Icons.Outlined.EditNote,onTools) }
        item { SpaceLink("Send a little hope forward","Write a time capsule",Icons.Outlined.MailOutline,onCapsule) }
        item { SpaceLink("Grow your vocabulary","Explore words and practice recall",Icons.Outlined.LocalFlorist,onWords) }
        item { Text("Kairos is a place to return to, not another thing to keep up with.",fontFamily=SerifFamily,fontSize=24.sp,lineHeight=34.sp,modifier=Modifier.padding(vertical=24.dp)) }
    }
}

@Composable
private fun SpaceLink(title:String,body:String,icon:ImageVector,onClick:()->Unit) {
    Surface(modifier=Modifier.gardenPress(onClick),shape=Shape,color=MaterialTheme.colorScheme.surface) {
        Row(Modifier.fillMaxWidth().padding(24.dp),verticalAlignment=Alignment.CenterVertically) {
            Icon(icon,null,modifier=Modifier.size(28.dp));Column(Modifier.weight(1f).padding(horizontal=16.dp)) { Text(title,style=MaterialTheme.typography.titleMedium);Text(body,style=MaterialTheme.typography.bodySmall,modifier=Modifier.padding(top=8.dp)) };Icon(Icons.Outlined.ChevronRight,null)
        }
    }
}

@Composable
private fun ThoughtGarden(quotes: List<QuoteEntity>, onSave: (QuoteEntity) -> Unit, onBack: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var saved by rememberSaveable { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val visible = remember(quotes, query, saved) { quotes.filter { (!saved || it.isFavorite) && (it.content.contains(query,true) || it.author.contains(query,true)) } }
    LazyColumn(Modifier.widthIn(max=720.dp).fillMaxSize(),contentPadding=PaddingValues(Page),verticalArrangement=Arrangement.spacedBy(24.dp)) {
        item { GardenHeader("THE THOUGHT COLLECTION","A few words\nto come back to.","Keep the thoughts that meet you where you are.",onBack) }
        item { OutlinedTextField(query,{query=it},label={Text("Search thoughts and authors")},modifier=Modifier.fillMaxWidth(),singleLine=true,shape=Shape) }
        item { FilterChip(selected=saved,onClick={saved=!saved},label={Text("Saved thoughts")}) }
        if(failed) item { Text("Could not open sharing. Please try again.",color=MaterialTheme.colorScheme.error) }
        if(visible.isEmpty()) item { Text(if(saved) "A thought you save will be waiting here." else "No matching thoughts. Try another search.") }
        items(visible,key={it.id}) { quote ->
            Surface(shape=Shape,color=MaterialTheme.colorScheme.surface) {
                Column(Modifier.fillMaxWidth().padding(24.dp)) {
                    Text("“${quote.content}”",fontFamily=SerifFamily,fontSize=24.sp,lineHeight=34.sp)
                    Text("— ${quote.author}",modifier=Modifier.padding(top=20.dp))
                    if(quote.source.isNotBlank()) Text(quote.source,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(top=8.dp))
                    if(quote.reflectionPrompt.isNotBlank()) Text(quote.reflectionPrompt,fontStyle=FontStyle.Italic,modifier=Modifier.padding(top=16.dp))
                    Row(Modifier.align(Alignment.End)) {
                        IconButton(onClick={onSave(quote)}) { Icon(if(quote.isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,"Save or unsave thought") }
                        IconButton(onClick={scope.launch { failed = !withContext(Dispatchers.IO) { ShareManager(context.applicationContext).shareQuote(quote.content,quote.author,false) } }}) { Icon(Icons.Outlined.IosShare,"Share thought") }
                    }
                }
            }
        }
    }
}

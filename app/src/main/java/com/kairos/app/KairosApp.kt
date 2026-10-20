package com.kairos.app

import android.app.Application
import android.content.Context
import com.kairos.app.data.db.KairosDatabase
import com.kairos.app.data.prefs.SettingsStore
import com.kairos.app.engine.KindnessService
import com.kairos.app.engine.LearnService
import com.kairos.app.engine.MoodService
import com.kairos.app.engine.PondService
import com.kairos.app.engine.QuoteService
import com.kairos.app.engine.TodayService
import com.kairos.app.engine.capsule.CapsuleRepository
import com.kairos.app.engine.content.ContentRepository
import com.kairos.app.engine.games.GameRepository
import com.kairos.app.engine.learn.DictionaryClient
import com.kairos.app.engine.learn.Speaker
import com.kairos.app.engine.notify.Notify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/** Hand-wired dependencies. Small app, explicit graph, no annotation processing. */
class AppContainer(context: Context) {
    val app: Context = context.applicationContext
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val db: KairosDatabase = KairosDatabase.build(app)
    val settings = SettingsStore(app)
    val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .addInterceptor { chain -> chain.proceed(chain.request().newBuilder().header("User-Agent", "Kairos-Android/${BuildConfig.VERSION_NAME}").build()) }
        .build()
    val content = ContentRepository(app)
    val pond = PondService(db)
    val today = TodayService(content, db, settings)
    val learn = LearnService(content, db, pond)
    val quotes = QuoteService(content, db, pond)
    val moods = MoodService(db, today, pond)
    val kindness = KindnessService(content, db, pond)
    val capsules = CapsuleRepository(app, db)
    val games = GameRepository(app, db.games(), http)
    val dictionary = DictionaryClient(http)
    val speaker by lazy { Speaker(app) }
}

class KairosApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        Notify.createChannels(this)
        container.scope.launch {
            container.games.load()
            runCatching { container.capsules.rescheduleAll() }
        }
        container.scope.launch { container.settings.settings.collect { container.speaker.enabled = it.ttsOn } }
    }
}

val Context.container: AppContainer get() = (applicationContext as KairosApp).container

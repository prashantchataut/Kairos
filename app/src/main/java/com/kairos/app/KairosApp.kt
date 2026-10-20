package com.kairos.app

import android.app.Application
import android.content.Context
import com.kairos.app.data.LearnRepository
import com.kairos.app.data.capsule.CapsuleRepository
import com.kairos.app.data.content.ContentRepository
import com.kairos.app.data.db.KairosDatabase
import com.kairos.app.data.games.GameRepository
import com.kairos.app.data.prefs.SettingsStore
import com.kairos.app.notify.CapsuleScheduler
import com.kairos.app.notify.Channels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Manual dependency graph — small app, no DI framework needed. */
class AppContainer(context: Context) {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val db = KairosDatabase.build(context)
    val settings = SettingsStore(context)
    val content = ContentRepository(context)
    val learn = LearnRepository(db, content, settings)
    val capsules = CapsuleRepository(context, db, CapsuleScheduler(context))
    val games = GameRepository(context, db, settings, scope)
}

class KairosApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        Channels.create(this)
        container.scope.launch { runCatching { container.capsules.rescheduleAll() } }
    }
}

val Context.container: AppContainer get() = (applicationContext as KairosApp).container

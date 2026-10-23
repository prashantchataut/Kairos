package com.kairos.app

import android.app.Application
import com.kairos.app.data.ai.OpenRouter
import com.kairos.app.data.ai.SecureKeyStore
import com.kairos.app.data.db.KairosDatabase
import com.kairos.app.data.prefs.UserPrefs
import com.kairos.app.data.repo.CapsuleRepository
import com.kairos.app.data.repo.KairosRepository
import com.kairos.app.work.Notifier
import com.kairos.app.work.RefillWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Hand-wired dependencies. Small app, no DI framework needed. */
class AppContainer(app: Application, val db: KairosDatabase = KairosDatabase.create(app)) {
    val prefs = UserPrefs(app)
    val keys = SecureKeyStore(app)
    val ai = OpenRouter(keys)
    val local = com.kairos.app.data.ai.LocalModel()
    val writer = com.kairos.app.data.ai.Writer(keys, local) { prefs.current().aiModel }
    val kairos = KairosRepository(db, prefs, keys, writer)
    val capsules = CapsuleRepository(app, db, kairos)
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val appContext: android.content.Context = app
}

open class KairosApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = createContainer()
        Notifier.createChannels(this)
        container.appScope.launch { container.capsules.rescheduleAll() }
        container.appScope.launch { container.keys.setOnDevice(container.local.refresh() == com.kairos.app.data.ai.LocalStatus.Ready) }
        RefillWorker.enqueue(this)
        RefillWorker.schedulePeriodic(this)
    }

    protected open fun createContainer() = AppContainer(this)
}

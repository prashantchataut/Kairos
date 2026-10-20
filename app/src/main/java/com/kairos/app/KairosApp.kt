package com.kairos.app

import android.app.Application
import com.kairos.app.data.content.ContentLoader
import com.kairos.app.data.db.KairosDatabase
import com.kairos.app.data.prefs.UserPrefs
import com.kairos.app.data.repo.CapsuleRepository
import com.kairos.app.data.repo.KairosRepository
import com.kairos.app.work.Notifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Hand-wired dependencies. Small app, no DI framework needed. */
class AppContainer(app: Application) {
    val db = KairosDatabase.create(app)
    val prefs = UserPrefs(app)
    val content = ContentLoader(app)
    val kairos = KairosRepository(db, prefs, content)
    val capsules = CapsuleRepository(app, db, kairos)
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}

class KairosApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        Notifier.createChannels(this)
        container.appScope.launch {
            container.kairos.syncContent()
            container.capsules.rescheduleAll()
        }
    }
}

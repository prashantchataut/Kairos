package com.kairos.app

import android.content.Context
import com.kairos.app.core.util.Clock
import com.kairos.app.core.util.SystemClock
import com.kairos.app.data.AttachmentStore
import com.kairos.app.data.KairosRepository
import com.kairos.app.data.StateStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File

/**
 * Hand-rolled dependency graph. The app has six collaborators, so a container is clearer
 * than a framework: one place to see what exists, one place to swap a clock in tests.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val clock: Clock = SystemClock()
    val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val store: StateStore = StateStore(File(appContext.filesDir, "kairos"))
    val repository: KairosRepository = KairosRepository(store, applicationScope)
    val attachments: AttachmentStore = AttachmentStore(appContext)

    fun flush() = repository.flush()
}

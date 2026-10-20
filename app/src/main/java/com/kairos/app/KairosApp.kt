package com.kairos.app

import android.app.Application
import android.content.Context
import com.kairos.app.data.AppGraph

class KairosApp : Application() {
    lateinit var graph: AppGraph
        private set

    override fun onCreate() {
        super.onCreate()
        graph = AppGraph(this)
        instance = this
    }

    companion object {
        lateinit var instance: KairosApp
            private set

        fun graph(context: Context): AppGraph =
            (context.applicationContext as KairosApp).graph
    }
}

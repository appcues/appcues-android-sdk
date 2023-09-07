package com.appcues.monitor

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.appcues.analytics.SdkMetrics

// new monitor that observes the entire application lifecycle
internal object ApplicationMonitor : DefaultLifecycleObserver {

    interface Listener {

        fun onApplicationStopped()
    }

    private val listeners: HashSet<Listener> = hashSetOf()

    fun initialize() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    fun subscribe(listener: Listener) {
        listeners.add(listener)
    }

    fun unsubscribe(listener: Listener) {
        listeners.remove(listener)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)

        SdkMetrics.clear()

        listeners.forEach { it.onApplicationStopped() }
    }
}

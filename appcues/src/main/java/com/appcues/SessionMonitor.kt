package com.appcues

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.appcues.analytics.AnalyticsEvent
import com.appcues.analytics.AnalyticsTracker
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit

internal class SessionMonitor(
    private val storage: Storage,
    config: AppcuesConfig,
    override val scope: Scope,
) : LifecycleObserver, KoinScopeComponent {

    private var _sessionId: UUID? = null
    val sessionId: UUID?
        get() = _sessionId

    val isActive: Boolean
        get() = _sessionId != null

    private var applicationBackgrounded: Date? = null
    private val sessionTimeout: Int = config.sessionTimeout

    // lazy prop inject here to avoid circular dependency
    private val analyticsTracker by inject<AnalyticsTracker>()

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    fun start() {
        if (storage.userId.isEmpty()) return
        _sessionId = UUID.randomUUID()
        analyticsTracker.track(AnalyticsEvent.SessionStarted, null, true)
    }

    fun reset() {
        analyticsTracker.track(AnalyticsEvent.SessionReset, null, false)
        _sessionId = null
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val backgroundTime = applicationBackgrounded?.time
        if (_sessionId == null || backgroundTime == null) return

        val elapsed = TimeUnit.MILLISECONDS.toSeconds(Date().time - backgroundTime)
        applicationBackgrounded = null

        if (elapsed >= sessionTimeout) {
            analyticsTracker.track(AnalyticsEvent.SessionStarted, null, true)
        } else {
            analyticsTracker.track(AnalyticsEvent.SessionResumed, null, false)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        if (_sessionId == null) return
        applicationBackgrounded = Date()
        analyticsTracker.track(AnalyticsEvent.SessionSuspended, null, false)

        // ensure any pending in-memory analytics get processed asap
        analyticsTracker.flushAsync()
    }
}

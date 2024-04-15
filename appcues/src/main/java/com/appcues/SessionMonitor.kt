package com.appcues

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.appcues.analytics.AnalyticsTracker
import com.appcues.analytics.SdkMetrics
import com.appcues.di.component.AppcuesComponent
import com.appcues.di.component.inject
import com.appcues.di.scope.AppcuesScope
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit

internal class SessionMonitor(
    override val scope: AppcuesScope,
) : AppcuesComponent, DefaultLifecycleObserver {

    // lazy prop inject here to avoid circular dependency
    private val analyticsTracker by inject<AnalyticsTracker>()
    private val storage by inject<Storage>()
    private val config by inject<AppcuesConfig>()
    private val appcuesCoroutineScope by inject<AppcuesCoroutineScope>()

    private var _sessionId: UUID? = null
    val sessionId: UUID?
        get() = _sessionId

    private val isExpired: Boolean
        get() = lastActivityAt?.let {
            TimeUnit.MILLISECONDS.toSeconds(Date().time - it.time) >= sessionTimeout
        } ?: false

    private var lastActivityAt: Date? = null
    private val sessionTimeout: Int = config.sessionTimeout

    init {
        appcuesCoroutineScope.launch {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this@SessionMonitor)
        }
    }

    fun hasSession(): Boolean {
        val isValid = sessionId != null && isExpired.not()

        if (isValid) updateLastActivity()

        return isValid
    }

    fun startNewSession(): UUID? {
        if (storage.userId.isEmpty()) return null

        // during the start of a new session, check if there is a new pushToken to set.
        if (Appcues.pushToken != null && Appcues.pushToken != storage.pushToken) {
            storage.pushToken = Appcues.pushToken
            Appcues.pushToken = null
        }

        return UUID.randomUUID().also {
            _sessionId = it
            updateLastActivity()
        }
    }

    private fun updateLastActivity() {
        lastActivityAt = Date()
    }

    fun reset() {
        _sessionId = null
        lastActivityAt = null
    }

    override fun onStop(owner: LifecycleOwner) {
        // ensure any pending in-memory analytics get processed asap
        analyticsTracker.flushPendingActivity()

        // clear out any pending metrics upon backgrounding
        SdkMetrics.clear()
    }
}

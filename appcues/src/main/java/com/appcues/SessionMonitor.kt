package com.appcues

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.appcues.analytics.AnalyticsTracker
import com.appcues.analytics.SdkMetrics
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit

internal class SessionMonitor(
    override val scope: Scope,
) : KoinScopeComponent, DefaultLifecycleObserver {

    // lazy prop inject here to avoid circular dependency
    private val analyticsTracker by inject<AnalyticsTracker>()
    private val storage by inject<Storage>()
    private val config by inject<AppcuesConfig>()
    private val appcuesCoroutineScope by inject<AppcuesCoroutineScope>()

    private var _sessionId: UUID? = null
    val sessionId: UUID?
        get() = _sessionId

    val isExpired: Boolean
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

    fun startNewSession(): UUID? {
        if (storage.userId.isEmpty()) return null

        return UUID.randomUUID().also {
            _sessionId = it
            updateLastActivity()
        }
    }

    fun updateLastActivity() {
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

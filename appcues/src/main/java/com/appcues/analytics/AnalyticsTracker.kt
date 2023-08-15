package com.appcues.analytics

import com.appcues.AnalyticType
import com.appcues.AnalyticType.EVENT
import com.appcues.AnalyticType.GROUP
import com.appcues.AnalyticType.IDENTIFY
import com.appcues.AnalyticType.SCREEN
import com.appcues.AppcuesCoroutineScope
import com.appcues.SessionMonitor
import com.appcues.data.remote.appcues.request.ActivityRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

internal class AnalyticsTracker(
    private val appcuesCoroutineScope: AppcuesCoroutineScope,
    private val activityBuilder: ActivityRequestBuilder,
    private val sessionMonitor: SessionMonitor,
    private val analyticsQueueProcessor: AnalyticsQueueProcessor,
) {

    private val _analyticsFlow = MutableSharedFlow<TrackingData>(1)
    val analyticsFlow: SharedFlow<TrackingData>
        get() = _analyticsFlow

    fun identify(properties: Map<String, Any>? = null, interactive: Boolean = true) {
        if (!checkSession()) return

        activityBuilder.identify(properties).let {
            updateAnalyticsFlow(IDENTIFY, false, it)

            if (interactive) {
                analyticsQueueProcessor.flushThenSend(it)
            } else {
                analyticsQueueProcessor.queue(it)
            }
        }
    }

    fun track(name: String, properties: Map<String, Any>? = null, interactive: Boolean = true, isInternal: Boolean = false) {
        if (!checkSession()) return

        activityBuilder.track(name, properties).let { activityRequest ->
            updateAnalyticsFlow(EVENT, isInternal, activityRequest)

            if (interactive) {
                analyticsQueueProcessor.queueThenFlush(activityRequest)
            } else {
                analyticsQueueProcessor.queue(activityRequest)
            }
        }
    }

    fun screen(title: String, properties: Map<String, Any>? = null, isInternal: Boolean = false) {
        if (!checkSession()) return

        activityBuilder.screen(title, properties?.toMutableMap()).let { activityRequest ->
            updateAnalyticsFlow(SCREEN, isInternal, activityRequest)
            analyticsQueueProcessor.queueThenFlush(activityRequest)
        }
    }

    fun group(properties: Map<String, Any>? = null) {
        if (!checkSession()) return

        activityBuilder.group(properties).let {
            updateAnalyticsFlow(GROUP, false, it)
            analyticsQueueProcessor.flushThenSend(it)
        }
    }

    // to be called when any pending activity should immediately be flushed to cache, and network if possible
    // i.e. app going to background / being killed
    fun flushPendingActivity() {
        analyticsQueueProcessor.flushAsync()
    }

    // return true if we have a valid session and can track analytics, false if not.
    private fun checkSession(): Boolean {

        if (sessionMonitor.sessionId == null || sessionMonitor.isExpired) {

            // if no existing session, or expired, start a new one
            if (sessionMonitor.startNewSession()) {
                // immediately track the session_started analytic, before any
                // subsequent analytics for the session are queued
                val activityRequest = activityBuilder.track(AnalyticsEvent.SessionStarted.eventName, null)
                updateAnalyticsFlow(EVENT, true, activityRequest)
                analyticsQueueProcessor.queueThenFlush(activityRequest)
            } else {
                // this means no session could be started (no user) and we cannot track
                // anything - return false to caller
                return false
            }
        } else {
            // we have a valid session, update its last activity timestamp to push out the timeout
            sessionMonitor.updateLastActivity()
        }

        return true
    }

    private fun updateAnalyticsFlow(type: AnalyticType, isInternal: Boolean, activity: ActivityRequest) {
        appcuesCoroutineScope.launch {
            _analyticsFlow.emit(TrackingData(type, isInternal, activity))
        }
    }
}

package com.appcues.analytics

import com.appcues.AnalyticType
import com.appcues.AnalyticType.EVENT
import com.appcues.AnalyticType.GROUP
import com.appcues.AnalyticType.IDENTIFY
import com.appcues.AnalyticType.SCREEN
import com.appcues.SessionMonitor
import com.appcues.data.remote.appcues.request.ActivityRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.util.UUID

internal class AnalyticsTracker(
    private val activityBuilder: ActivityRequestBuilder,
    private val sessionMonitor: SessionMonitor,
    private val analyticsQueueProcessor: AnalyticsQueueProcessor,
) {

    private val _analyticsFlow = MutableSharedFlow<TrackingData>(
        replay = 1,
        extraBufferCapacity = Int.MAX_VALUE
    )
    val analyticsFlow: SharedFlow<TrackingData>
        get() = _analyticsFlow

    fun identify(properties: Map<String, Any>? = null, interactive: Boolean = true) {
        val sessionId = getSession() ?: return

        activityBuilder.identify(sessionId, properties).let {
            updateAnalyticsFlow(IDENTIFY, false, it)

            if (interactive) {
                // use true for waitForBatch - since a new identify() should merge with any
                // immediately subsequent analytics - to prevent stale group info on qualification
                analyticsQueueProcessor.flushThenSend(it, true)
            } else {
                analyticsQueueProcessor.queue(it)
            }
        }
    }

    fun track(name: String, properties: Map<String, Any>? = null, interactive: Boolean = true, isInternal: Boolean = false) {
        val sessionId = getSession() ?: return

        activityBuilder.track(sessionId, name, properties).let { activityRequest ->
            updateAnalyticsFlow(EVENT, isInternal, activityRequest)

            if (interactive) {
                analyticsQueueProcessor.queueThenFlush(activityRequest)
            } else {
                analyticsQueueProcessor.queue(activityRequest)
            }
        }
    }

    fun screen(title: String, properties: Map<String, Any>? = null, isInternal: Boolean = false) {
        val sessionId = getSession() ?: return

        activityBuilder.screen(sessionId, title, properties?.toMutableMap()).let { activityRequest ->
            updateAnalyticsFlow(SCREEN, isInternal, activityRequest)
            analyticsQueueProcessor.queueThenFlush(activityRequest)
        }
    }

    fun group(properties: Map<String, Any>? = null) {
        val sessionId = getSession() ?: return

        activityBuilder.group(sessionId, properties).let {
            updateAnalyticsFlow(GROUP, false, it)
            analyticsQueueProcessor.flushThenSend(it)
        }
    }

    // to be called when any pending activity should immediately be flushed to cache, and network if possible
    // i.e. app going to background / being killed
    fun flushPendingActivity() {
        analyticsQueueProcessor.flushAsync()
    }

    // returns valid session id (existing or creating new) or null if unable to start one.
    private fun getSession(): UUID? {
        if (!sessionMonitor.hasSession()) {
            // if no existing session, or expired, start a new one
            val sessionId = sessionMonitor.startNewSession() ?: return null
            // immediately track the session_started analytic, before any
            // subsequent analytics for the session are queued
            val activityRequest = activityBuilder.track(sessionId, AnalyticsEvent.SessionStarted.eventName, null)
            updateAnalyticsFlow(EVENT, true, activityRequest)
            // use true for waitForBatch, as a session_started event created due to a new user identify() should
            // merge and send together in a single activity payload
            analyticsQueueProcessor.flushThenSend(activityRequest, true)
        }

        return sessionMonitor.sessionId
    }

    private fun updateAnalyticsFlow(type: AnalyticType, isInternal: Boolean, activity: ActivityRequest) {
        _analyticsFlow.tryEmit(TrackingData(type, isInternal, activity))
    }
}

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
        if (!sessionMonitor.checkSession("unable to track user")) return

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
        if (!sessionMonitor.checkSession("unable to track event")) return

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
        if (!sessionMonitor.checkSession("unable to track screen")) return

        activityBuilder.screen(title, properties?.toMutableMap()).let { activityRequest ->
            updateAnalyticsFlow(SCREEN, isInternal, activityRequest)
            analyticsQueueProcessor.queueThenFlush(activityRequest)
        }
    }

    fun group(properties: Map<String, Any>? = null) {
        if (!sessionMonitor.checkSession("unable to track group")) return

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

    private fun updateAnalyticsFlow(type: AnalyticType, isInternal: Boolean, activity: ActivityRequest) {
        appcuesCoroutineScope.launch {
            _analyticsFlow.emit(TrackingData(type, isInternal, activity))
        }
    }
}

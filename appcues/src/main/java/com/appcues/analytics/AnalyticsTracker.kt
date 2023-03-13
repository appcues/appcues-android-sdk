package com.appcues.analytics

import com.appcues.AnalyticType
import com.appcues.AnalyticType.EVENT
import com.appcues.AnalyticType.GROUP
import com.appcues.AnalyticType.IDENTIFY
import com.appcues.AnalyticType.SCREEN
import com.appcues.AppcuesCoroutineScope
import com.appcues.data.remote.appcues.request.ActivityRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

internal class AnalyticsTracker(
    private val appcuesCoroutineScope: AppcuesCoroutineScope,
    private val activityBuilder: ActivityRequestBuilder,
    private val experienceLifecycleTracker: ExperienceLifecycleTracker,
    private val analyticsPolicy: AnalyticsPolicy,
    private val analyticsQueueProcessor: AnalyticsQueueProcessor,
) {

    private val _analyticsFlow = MutableSharedFlow<TrackingData>(1)
    val analyticsFlow: SharedFlow<TrackingData>
        get() = _analyticsFlow

    init {
        appcuesCoroutineScope.launch {
            experienceLifecycleTracker.start()
        }
    }

    fun identify(properties: Map<String, Any>? = null, interactive: Boolean = true) {
        if (!analyticsPolicy.canIdentify()) return

        activityBuilder.identify(properties).let {
            updateAnalyticsFlow(IDENTIFY, false, it)

            if (interactive) {
                analyticsQueueProcessor.flushThenSend(it)
            } else {
                analyticsQueueProcessor.queue(it)
            }
        }
    }

    // convenience helper for internal events
    fun track(event: AnalyticsEvent, properties: Map<String, Any>? = null, interactive: Boolean = true) {
        track(event.eventName, properties, interactive, true)
    }

    fun track(name: String, properties: Map<String, Any>? = null, interactive: Boolean = true, isInternal: Boolean = false) {
        if (!analyticsPolicy.canTrackEvent()) return

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
        if (!analyticsPolicy.canTrackScreen(title)) return

        activityBuilder.screen(title, properties?.toMutableMap()).let { activityRequest ->
            updateAnalyticsFlow(SCREEN, isInternal, activityRequest)
            analyticsQueueProcessor.queueThenFlush(activityRequest)
        }
    }

    fun group(properties: Map<String, Any>? = null) {
        if (!analyticsPolicy.canTrackGroup()) return

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

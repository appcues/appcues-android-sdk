package com.appcues.analytics

import com.appcues.AppcuesCoroutineScope
import com.appcues.data.remote.request.ActivityRequest
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

    private val _analyticsFlow = MutableSharedFlow<ActivityRequest>(1)
    val analyticsFlow: SharedFlow<ActivityRequest>
        get() = _analyticsFlow

    init {
        appcuesCoroutineScope.launch {
            experienceLifecycleTracker.start()
        }
    }

    fun identify(properties: Map<String, Any>? = null) {
        if (!analyticsPolicy.canIdentify()) return

        activityBuilder.identify(properties).let {
            updateAnalyticsFlow(it)

            analyticsQueueProcessor.flushThenSend(it)
        }
    }

    // convenience helper for internal events
    fun track(event: AnalyticsEvent, properties: Map<String, Any>? = null, interactive: Boolean = true) {
        track(event.eventName, properties, interactive)
    }

    fun track(name: String, properties: Map<String, Any>? = null, interactive: Boolean = true) {
        if (!analyticsPolicy.canTrackEvent()) return

        activityBuilder.track(name, properties).let {
            updateAnalyticsFlow(it)

            if (interactive) {
                analyticsQueueProcessor.queueThenFlush(it)
            } else {
                analyticsQueueProcessor.queue(it)
            }
        }
    }

    fun screen(title: String, properties: Map<String, Any>? = null) {
        if (!analyticsPolicy.canTrackScreen(title)) return

        activityBuilder.screen(title, properties?.toMutableMap()).let {
            updateAnalyticsFlow(it)

            analyticsQueueProcessor.queueThenFlush(it)
        }
    }

    fun group(properties: Map<String, Any>? = null) {
        if (!analyticsPolicy.canTrackGroup()) return

        activityBuilder.group(properties).let {
            updateAnalyticsFlow(it)

            analyticsQueueProcessor.flushThenSend(it)
        }
    }

    // to be called when any pending activity should immediately be flushed to cache, and network if possible
    // i.e. app going to background / being killed
    fun flushPendingActivity() {
        analyticsQueueProcessor.flushAsync()
    }

    private fun updateAnalyticsFlow(activity: ActivityRequest) {
        appcuesCoroutineScope.launch {
            _analyticsFlow.emit(activity)
        }
    }
}

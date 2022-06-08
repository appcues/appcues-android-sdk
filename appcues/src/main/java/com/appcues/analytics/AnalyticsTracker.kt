package com.appcues.analytics

import com.appcues.AppcuesCoroutineScope
import com.appcues.data.AppcuesRepository
import com.appcues.data.remote.request.ActivityRequest
import com.appcues.ui.ExperienceRenderer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

internal class AnalyticsTracker(
    private val appcuesCoroutineScope: AppcuesCoroutineScope,
    private val repository: AppcuesRepository,
    private val experienceRenderer: ExperienceRenderer,
    private val activityBuilder: ActivityRequestBuilder,
    private val experienceLifecycleTracker: ExperienceLifecycleTracker,
    private val analyticsPolicy: AnalyticsPolicy,
    private val analyticsQueueManager: AnalyticsQueueManager,
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

            analyticsQueueManager.flushThenSend(it, ::send)
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
                analyticsQueueManager.queueThenFlush(it, ::send)
            } else {
                analyticsQueueManager.queue(it, ::send)
            }
        }
    }

    fun screen(title: String, properties: Map<String, Any>? = null) {
        if (!analyticsPolicy.canTrackScreen(title)) return

        activityBuilder.screen(title, properties?.toMutableMap()).let {
            updateAnalyticsFlow(it)

            analyticsQueueManager.queueThenFlush(it, ::send)
        }
    }

    fun group(properties: Map<String, Any>? = null) {
        if (!analyticsPolicy.canTrackGroup()) return

        activityBuilder.group(properties).let {
            updateAnalyticsFlow(it)

            analyticsQueueManager.flushThenSend(it, ::send)
        }
    }

    // to be called when any pending activity should immediately be flushed to cache, and network if possible
    // i.e. app going to background / being killed
    fun flushPendingActivity() {
        analyticsQueueManager.flushAsync(::send)
    }

    private fun updateAnalyticsFlow(activity: ActivityRequest) {
        appcuesCoroutineScope.launch {
            _analyticsFlow.emit(activity)
        }
    }

    private fun send(activity: ActivityRequest) {
        appcuesCoroutineScope.launch {
            // this will respond with qualified experiences, if applicable
            repository.trackActivity(activity).also {
                // we will try to show an experience from this list
                experienceRenderer.show(it)
            }
        }
    }
}

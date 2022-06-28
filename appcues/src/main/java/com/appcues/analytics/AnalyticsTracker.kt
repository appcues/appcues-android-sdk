package com.appcues.analytics

import com.appcues.AppcuesConfig
import com.appcues.AppcuesCoroutineScope
import com.appcues.analytics.AnalyticsListener.AnalyticType.EVENT
import com.appcues.analytics.AnalyticsListener.AnalyticType.GROUP
import com.appcues.analytics.AnalyticsListener.AnalyticType.IDENTIFY
import com.appcues.analytics.AnalyticsListener.AnalyticType.SCREEN
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
    private val config: AppcuesConfig,
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

            config.analyticsListener?.trackedAnalytic(IDENTIFY, it.userId, it.profileUpdate, false)
        }
    }

    // convenience helper for internal events
    fun track(event: AnalyticsEvent, properties: Map<String, Any>? = null, interactive: Boolean = true) {
        track(event.eventName, properties, interactive, true)
    }

    fun track(name: String, properties: Map<String, Any>? = null, interactive: Boolean = true, isInternal: Boolean = false) {
        if (!analyticsPolicy.canTrackEvent()) return

        activityBuilder.track(name, properties).let { activityRequest ->
            updateAnalyticsFlow(activityRequest)

            if (interactive) {
                analyticsQueueProcessor.queueThenFlush(activityRequest)
            } else {
                analyticsQueueProcessor.queue(activityRequest)
            }

            activityRequest.events?.forEach {
                config.analyticsListener?.trackedAnalytic(EVENT, it.name, it.attributes, isInternal)
            }
        }
    }

    fun screen(title: String, properties: Map<String, Any>? = null, isInternal: Boolean = false) {
        if (!analyticsPolicy.canTrackScreen(title)) return

        activityBuilder.screen(title, properties?.toMutableMap()).let { activityRequest ->
            updateAnalyticsFlow(activityRequest)

            analyticsQueueProcessor.queueThenFlush(activityRequest)

            activityRequest.events?.forEach {
                config.analyticsListener?.trackedAnalytic(SCREEN, it.name, it.attributes, isInternal)
            }
        }
    }

    fun group(properties: Map<String, Any>? = null) {
        if (!analyticsPolicy.canTrackGroup()) return

        activityBuilder.group(properties).let {
            updateAnalyticsFlow(it)

            analyticsQueueProcessor.flushThenSend(it)

            config.analyticsListener?.trackedAnalytic(GROUP, it.groupId, it.groupUpdate, false)
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

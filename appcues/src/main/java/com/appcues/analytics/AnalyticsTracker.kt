package com.appcues.analytics

import com.appcues.AnalyticType
import com.appcues.AnalyticType.EVENT
import com.appcues.AnalyticType.GROUP
import com.appcues.AnalyticType.IDENTIFY
import com.appcues.AnalyticType.SCREEN
import com.appcues.AppcuesCoroutineScope
import com.appcues.analytics.AnalyticsQueue.QueueProcessor
import com.appcues.data.AppcuesRepository
import com.appcues.data.model.Experience
import com.appcues.data.remote.appcues.request.ActivityRequest
import com.appcues.ui.ExperienceRenderer
import com.appcues.util.appcuesFormatted
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

internal class AnalyticsTracker(
    private val appcuesCoroutineScope: AppcuesCoroutineScope,
    private val activityBuilder: ActivityRequestBuilder,
    private val analyticsPolicy: AnalyticsPolicy,
    private val analyticsQueue: AnalyticsQueue,
    private val repository: AppcuesRepository,
    private val experienceRenderer: ExperienceRenderer,
) : QueueProcessor {

    private val _analyticsFlow = MutableSharedFlow<TrackingData>(1)
    val analyticsFlow: SharedFlow<TrackingData>
        get() = _analyticsFlow

    init {
        analyticsQueue.setProcessor(this)
    }

    fun identify(properties: Map<String, Any>? = null, interactive: Boolean = true) {
        if (!analyticsPolicy.canIdentify()) return

        activityBuilder.identify(properties).let {
            updateAnalyticsFlow(IDENTIFY, false, it)

            if (interactive) {
                analyticsQueue.flushThenSend(it)
            } else {
                analyticsQueue.queue(it)
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
                analyticsQueue.queueThenFlush(activityRequest)
            } else {
                analyticsQueue.queue(activityRequest)
            }
        }
    }

    fun screen(title: String, properties: Map<String, Any>? = null, isInternal: Boolean = false) {
        if (!analyticsPolicy.canTrackScreen()) return

        activityBuilder.screen(title, properties?.toMutableMap()).let { activityRequest ->
            updateAnalyticsFlow(SCREEN, isInternal, activityRequest)
            analyticsQueue.queueThenFlush(activityRequest)
        }
    }

    fun group(properties: Map<String, Any>? = null) {
        if (!analyticsPolicy.canTrackGroup()) return

        activityBuilder.group(properties).let {
            updateAnalyticsFlow(GROUP, false, it)
            analyticsQueue.flushThenSend(it)
        }
    }

    // to be called when any pending activity should immediately be flushed to cache, and network if possible
    // i.e. app going to background / being killed
    fun flushPendingActivity() {
        analyticsQueue.flushAsync()
    }

    private fun updateAnalyticsFlow(type: AnalyticType, isInternal: Boolean, activity: ActivityRequest) {
        appcuesCoroutineScope.launch {
            _analyticsFlow.emit(TrackingData(type, isInternal, activity))
        }
    }

    override fun process(activity: ActivityRequest) {
        appcuesCoroutineScope.launch {
            // start tracking metrics for this request
            SdkMetrics.tracked(activity.requestId, activity.timestamp)
            // this will respond with qualified experiences, if applicable
            repository.trackActivity(activity).showFirstOr {
                // no experiences to track metrics
                SdkMetrics.remove(activity.requestId)
            }
        }
    }

    private suspend fun List<Experience>.showFirstOr(orBlock: () -> Unit) {
        if (isEmpty()) {
            orBlock()
            return
        }

        // get first experience on the list
        with(first()) {
            // track possible experiment
            trackExperiment()
            // get out of the loop if experience shows
            if (experienceRenderer.show(this)) return
        }

        // remove "first" and call this recursively until we show one experience or the list is empty
        dropFirst().showFirstOr(orBlock)
    }

    private fun <T> List<T>.dropFirst() = drop(1)

    private fun Experience.trackExperiment() {
        experiment?.run {
            track(
                event = AnalyticsEvent.ExperimentEntered,
                properties = mapOf(
                    "experimentId" to id.appcuesFormatted(),
                    "experimentGroup" to group,
                    "experimentExperienceId" to experienceId.appcuesFormatted(),
                    "experimentGoalId" to goalId,
                    "experimentContentType" to contentType,
                ),
                interactive = false
            )
        }
    }
}

package com.appcues.analytics

import com.appcues.AppcuesCoroutineScope
import com.appcues.SessionMonitor
import com.appcues.data.AppcuesRepository
import com.appcues.data.remote.request.ActivityRequest
import com.appcues.data.remote.request.EventRequest
import com.appcues.ui.ExperienceRenderer
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule

internal class AnalyticsTracker(
    private val appcuesCoroutineScope: AppcuesCoroutineScope,
    private val repository: AppcuesRepository,
    private val experienceRenderer: ExperienceRenderer,
    private val sessionMonitor: SessionMonitor,
    private val activityBuilder: ActivityRequestBuilder,
    experienceLifecycleTracker: ExperienceLifecycleTracker,
) {
    companion object {
        const val FLUSH_AFTER_MILLISECONDS: Long = 10000
    }

    private var pendingActivity = mutableListOf<ActivityRequest>()
    private var flushTask: TimerTask? = null

    init {
        appcuesCoroutineScope.launch {
            experienceLifecycleTracker.start()
        }
    }

    fun identify(properties: HashMap<String, Any>? = null) {
        flushThenSend(activityBuilder.identify(properties))
    }

    // convenience helper for internal events
    fun track(event: AnalyticsEvent, properties: HashMap<String, Any>? = null, sync: Boolean = true) {
        track(event.eventName, properties, sync)
    }

    fun track(name: String, properties: HashMap<String, Any>? = null, sync: Boolean = true) {
        val activity = activityBuilder.track(name, properties)
        if (sync) {
            queueThenFlush(activity)
        } else {
            queue(activity)
        }
    }

    fun screen(title: String, properties: HashMap<String, Any>? = null) {
        queueThenFlush(activityBuilder.screen(title, properties))
    }

    fun group(properties: HashMap<String, Any>? = null) {
        flushThenSend(activityBuilder.group(properties))
    }

    private fun queueThenFlush(activity: ActivityRequest) {
        synchronized(this) {
            flushTask?.cancel()
            pendingActivity.add(activity)
            flushPendingActivity(true)
        }
    }

    private fun flushThenSend(activity: ActivityRequest) {
        synchronized(this) {
            flushTask?.cancel()
            flushPendingActivity(false)
            flush(activity, true)
        }
    }

    private fun queue(activity: ActivityRequest) {
        synchronized(this) {
            pendingActivity.add(activity)
            if (flushTask == null) {
                flushTask = Timer().schedule(FLUSH_AFTER_MILLISECONDS) {
                    synchronized(this@AnalyticsTracker) {
                        flushPendingActivity(false)
                    }
                }
            }
        }
    }

    private fun flushPendingActivity(sync: Boolean) {
        flushTask = null
        val activity = pendingActivity.merge()
        pendingActivity = mutableListOf()
        if (activity != null) {
            flush(activity, sync)
        }
    }

    private fun flush(activity: ActivityRequest, sync: Boolean) {
        if (!sessionMonitor.isActive) return

        appcuesCoroutineScope.launch {
            // this will respond with qualified experiences, if applicable
            val experiences = repository.trackActivity(activity, sync)

            if (sync && experiences.isNotEmpty()) {
                // note: by default we just show the first experience, but will need to revisit and allow
                // for showing secondary qualified experience if the first fails to load for some reason
                experienceRenderer.show(experiences.first())
            }
        }
    }
}

internal fun List<ActivityRequest>.merge(): ActivityRequest? {
    if (isEmpty()) return null
    val activity = first()
    val events = mutableListOf<EventRequest>()
    forEach { item ->
        item.events?.let {
            events.addAll(it)
        }
    }
    return activity.copy(events = events)
}

package com.appcues.analytics

import com.appcues.AppcuesCoroutineScope
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
    private val activityBuilder: ActivityRequestBuilder,
    private val experienceLifecycleTracker: ExperienceLifecycleTracker,
    private val analyticsPolicy: AnalyticsPolicy,
) {
    companion object {
        const val FLUSH_AFTER_MILLISECONDS: Long = 10000
    }

    private val pendingActivity = mutableListOf<ActivityRequest>()
    private var flushTask: TimerTask? = null

    init {
        appcuesCoroutineScope.launch {
            experienceLifecycleTracker.start()
        }
    }

    fun identify(properties: HashMap<String, Any>? = null) {
        if (!analyticsPolicy.canIdentify()) return
        flushThenSend(activityBuilder.identify(properties))
    }

    // convenience helper for internal events
    fun track(event: AnalyticsEvent, properties: HashMap<String, Any>? = null, interactive: Boolean = true) {
        track(event.eventName, properties, interactive)
    }

    fun track(name: String, properties: HashMap<String, Any>? = null, interactive: Boolean = true) {
        if (!analyticsPolicy.canTrackEvent()) return
        val activity = activityBuilder.track(name, properties)
        if (interactive) {
            queueThenFlush(activity)
        } else {
            queue(activity)
        }
    }

    fun screen(title: String, properties: HashMap<String, Any>? = null) {
        if (!analyticsPolicy.canTrackScreen(title)) return
        queueThenFlush(activityBuilder.screen(title, properties))
    }

    fun group(properties: HashMap<String, Any>? = null) {
        if (!analyticsPolicy.canTrackGroup()) return
        flushThenSend(activityBuilder.group(properties))
    }

    // to be called when any pending activity should immediately be flushed to cache, and network if possible
    // i.e. app going to background / being killed
    fun flushAsync() {
        synchronized(this) {
            flushPendingActivity()
        }
    }

    private fun queueThenFlush(activity: ActivityRequest) {
        synchronized(this) {
            flushTask?.cancel()
            pendingActivity.add(activity)
            flushPendingActivity()
        }
    }

    private fun flushThenSend(activity: ActivityRequest) {
        synchronized(this) {
            flushTask?.cancel()
            flushPendingActivity()
            flush(activity)
        }
    }

    private fun queue(activity: ActivityRequest) {
        synchronized(this) {
            pendingActivity.add(activity)
            if (flushTask == null) {
                flushTask = Timer().schedule(FLUSH_AFTER_MILLISECONDS) {
                    synchronized(this@AnalyticsTracker) {
                        flushPendingActivity()
                    }
                }
            }
        }
    }

    private fun flushPendingActivity() {
        flushTask = null
        val activity = pendingActivity.merge()
        pendingActivity.clear()
        if (activity != null) {
            flush(activity)
        }
    }

    private fun flush(activity: ActivityRequest) {
        appcuesCoroutineScope.launch {
            // this will respond with qualified experiences, if applicable
            val experiences = repository.trackActivity(activity)
            experienceRenderer.show(experiences)
        }
    }
}

internal fun List<ActivityRequest>.merge(): ActivityRequest? {
    if (isEmpty()) return null
    val activity = first()
    val events = mutableListOf<EventRequest>()
    val profileUpdate = hashMapOf<String, Any>()
    forEach { item ->
        // merge events
        item.events?.let {
            events.addAll(it)
        }
        // merge auto properties in profile update
        if (item.profileUpdate != null) {
            profileUpdate.putAll(item.profileUpdate)
        }
    }
    return activity.copy(events = events, profileUpdate = profileUpdate)
}

package com.appcues.analytics

import androidx.annotation.VisibleForTesting
import com.appcues.AppcuesCoroutineScope
import com.appcues.data.AppcuesRepository
import com.appcues.data.remote.request.ActivityRequest
import com.appcues.data.remote.request.EventRequest
import com.appcues.ui.ExperienceRenderer
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule

internal class AnalyticsQueueProcessor(
    private val appcuesCoroutineScope: AppcuesCoroutineScope,
    private val repository: AppcuesRepository,
    private val experienceRenderer: ExperienceRenderer,
    private val analyticsQueueScheduler: QueueScheduler,
) {

    interface QueueScheduler {

        fun schedule(block: () -> Unit)
        fun cancel()
    }

    private val pendingActivity = mutableListOf<ActivityRequest>()

    @VisibleForTesting
    fun queueForTesting(activity: ActivityRequest) {
        pendingActivity.add(activity)
    }

    fun queue(activity: ActivityRequest) {
        synchronized(this) {
            // add activity to pending activities
            pendingActivity.add(activity)
            // and schedule the flush task
            analyticsQueueScheduler.schedule {
                synchronized(this) {
                    flushPendingActivity()
                }
            }
        }
    }

    fun queueThenFlush(activity: ActivityRequest) {
        synchronized(this) {
            analyticsQueueScheduler.cancel()
            // add new activity to pending activities and try to send all as a merged activity request
            pendingActivity.add(activity)
            flushPendingActivity()
        }
    }

    fun flushThenSend(activity: ActivityRequest) {
        synchronized(this) {
            analyticsQueueScheduler.cancel()
            // send all pending activities as a merged activity request
            flushPendingActivity()
            // then send another one for our newer activity
            send(activity)
        }
    }

    fun flushAsync() {
        // to be called when any pending activity should immediately be flushed to cache, and network if possible
        // i.e. app going to background / being killed
        synchronized(this) {
            flushPendingActivity()
        }
    }

    private fun flushPendingActivity() {
        pendingActivity.merge()
            .let {
                if (it != null) send(it)
            }.also {
                pendingActivity.clear()
            }
    }

    private fun List<ActivityRequest>.merge(): ActivityRequest? {
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

    private fun send(activity: ActivityRequest) {
        appcuesCoroutineScope.launch {
            // this will respond with qualified experiences, if applicable
            repository.trackActivity(activity).also {
                // we will try to show an experience from this list
                experienceRenderer.show(it)
            }
        }
    }

    class AnalyticsQueueScheduler : QueueScheduler {

        private var debounceTimer: TimerTask? = null

        override fun schedule(block: () -> Unit) {
            // if null, then schedule
            if (debounceTimer == null) {
                debounceTimer = Timer().schedule(delay = 10000L) {
                    // set to know to allow new Timer
                    debounceTimer = null
                    // run callback block
                    block()
                }
            }
        }

        override fun cancel() {
            debounceTimer?.cancel()
            debounceTimer = null
        }
    }
}

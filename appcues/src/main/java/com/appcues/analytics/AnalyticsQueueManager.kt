package com.appcues.analytics

import com.appcues.data.remote.request.ActivityRequest
import com.appcues.data.remote.request.EventRequest
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule

internal class AnalyticsQueueManager {

    companion object {

        const val FLUSH_AFTER_MILLISECONDS: Long = 10000
    }

    private val pendingActivity = mutableListOf<ActivityRequest>()
    private var flushTask: TimerTask? = null

    fun queue(activity: ActivityRequest, flush: (ActivityRequest) -> Unit) {
        // add activity to pending activities and re-schedule the flush task
        synchronized(this) {
            pendingActivity.add(activity)
            if (flushTask == null) {
                flushTask = Timer().schedule(FLUSH_AFTER_MILLISECONDS) {
                    synchronized(this@AnalyticsQueueManager) {
                        flushPendingActivity(flush)
                    }
                }
            }
        }
    }

    fun queueThenFlush(activity: ActivityRequest, flush: (ActivityRequest) -> Unit) {
        // add new activity to pending activities and try to send all
        // as a merged activity request
        synchronized(this) {
            flushTask?.cancel()
            pendingActivity.add(activity)
            flushPendingActivity(flush)
        }
    }

    fun flushThenSend(activity: ActivityRequest, flush: (ActivityRequest) -> Unit) {
        // send all pending activities as a merged activity request
        // then send another one for our newer activity
        // (useful when we don't want to merge the properties)
        synchronized(this) {
            flushTask?.cancel()
            flushPendingActivity(flush)
            flush(activity)
        }
    }

    fun flushAsync(flush: (ActivityRequest) -> Unit) {
        // to be called when any pending activity should immediately be flushed to cache, and network if possible
        // i.e. app going to background / being killed
        synchronized(this) {
            flushPendingActivity(flush)
        }
    }

    private fun flushPendingActivity(flush: (ActivityRequest) -> Unit) {
        flushTask = null

        pendingActivity.merge()
            .let {
                if (it != null) flush(it)
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
}

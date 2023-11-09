package com.appcues.analytics

import androidx.annotation.VisibleForTesting
import com.appcues.AppcuesCoroutineScope
import com.appcues.data.AppcuesRepository
import com.appcues.data.remote.appcues.request.ActivityRequest
import com.appcues.data.remote.appcues.request.EventRequest
import com.appcues.ui.ExperienceRenderer
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule

internal class AnalyticsQueueProcessor(
    private val appcuesCoroutineScope: AppcuesCoroutineScope,
    private val repository: AppcuesRepository,
    private val experienceRenderer: ExperienceRenderer,
    // this is the background analytics processing queue - 10 sec batch
    private val backgroundQueueScheduler: QueueScheduler,
    // this is the immediate processing queue - 50ms batch to group identify with immediate subsequent updates
    private val priorityQueueScheduler: QueueScheduler,
) {

    interface QueueScheduler {
        fun schedule(delay: Long = 10000L, block: () -> Unit)
        fun cancel()
    }

    // holds background analytics (flow events) to batch and send in 10 sec intervals
    private val backgroundQueue = mutableListOf<ActivityRequest>()

    // holds items to be immediately processed, but allowed to group with very near additional updates (50ms)
    private val priorityQueue = mutableListOf<ActivityRequest>()

    @VisibleForTesting
    fun queueForTesting(activity: ActivityRequest) {
        backgroundQueue.add(activity)
    }

    // used by non-interactive tracking - flow events - allowing for 10 sec batching
    fun queue(activity: ActivityRequest) {
        synchronized(this) {
            // add activity to pending activities
            backgroundQueue.add(activity)
            // and schedule the flush task
            backgroundQueueScheduler.schedule {
                synchronized(this) {
                    flushBackgroundActivity()
                }
            }
        }
    }

    // used by normal interactive screen/track calls
    // add this item to any waiting in the background queue, and flush immediately,
    // preempting any 10 sec wait for other items
    fun queueThenFlush(activity: ActivityRequest) {
        synchronized(this) {
            backgroundQueueScheduler.cancel()
            // add new activity to pending activities and try to send all as a merged activity request
            backgroundQueue.add(activity)
            flushBackgroundActivity(activity.timestamp)
        }
    }

    // used by identify, group, and session_started event
    // flush any items in the background queue, then send this item immediately*
    // existing background items are flushed first, as they may pertain to a previous user/session
    //
    // *the `waitForBatch` param determines whether a 50ms timer will start to allow
    // a slight delay before sending this item to allow any immediately subsequent items
    // to batch with it before sending - common case may be grouping an identify(id) with a group(id)
    // that immediately follows
    fun flushThenSend(activity: ActivityRequest, waitForBatch: Boolean = false) {
        synchronized(this) {
            backgroundQueueScheduler.cancel()
            // send all pending activities as a merged activity request
            flushBackgroundActivity()
            // then send another one for our newer activity
            sendWithPriorityQueue(activity, waitForBatch, activity.timestamp)
        }
    }

    // used on session reset or app backgrounding to immediately empty anything still in the queue first
    fun flushAsync() {
        // to be called when any pending activity should immediately be flushed to cache, and network if possible
        // i.e. app going to background / being killed
        synchronized(this) {
            // this will move into priority queue, if exists
            flushBackgroundActivity()
            // flush priority queue, if exists
            flushPriorityActivity()
        }
    }

    // send anything in the background analytics queue, merging into a priority queue, if exists
    private fun flushBackgroundActivity(time: Date? = null) {
        backgroundQueue.merge()
            .let {
                if (it != null) {
                    // sending through the priority queue here - if there
                    // is anything batching at 50ms delay (i.e. a new identify)
                    // then these items will get merged in. Typically, this
                    // is not the case, and they will be sent immediately.
                    // `startQueue` is false, as this call will never start a new
                    // 50ms delay, only merge with an existing one, if exists.
                    sendWithPriorityQueue(it, false, time)
                }
            }.also {
                backgroundQueue.clear()
            }
    }

    // send the priority queue
    private fun flushPriorityActivity() {
        priorityQueue.merge()
            .let {
                if (it != null) send(it, it.timestamp)
            }.also {
                priorityQueue.clear()
            }
    }

    // this function handles the optional creation of a 50ms buffer to allow for priority items
    // to be batched together, when calls are very near in succession.
    // 1. only start the queue if requested with `startQueue` - only identify or session_start will start a queue
    // 2. if no queue exists, and not starting a queue - send immediately with no delay
    // 3. if a queue exists and it contains items from a different user - flush immediately and then process this item
    // 4. otherwise, add this item to the queue and start it (if not already)
    private fun sendWithPriorityQueue(activity: ActivityRequest, startQueue: Boolean, time: Date?) {
        // if there is no queue in flight, just send it immediate - hopefully common case
        if (!startQueue && priorityQueue.isEmpty()) {
            send(activity, time)
            return
        }
        // if the queue items are for a different user
        if (priorityQueue.any { it.userId != activity.userId }) {
            // flush immediately...
            priorityQueueScheduler.cancel()
            flushPriorityActivity()
            // note: we know that background items were already flushed before getting here
            // with a new user identified, in flushThenSend.
            //
            // then try again..
            sendWithPriorityQueue(activity, startQueue, time)
            return
        }
        // add activity to priority queue
        priorityQueue.add(activity)
        // and schedule the flush task (if not already scheduled)
        priorityQueueScheduler.schedule(delay = 50L) {
            synchronized(this) {
                flushPriorityActivity()
            }
        }
    }

    // make the network call to send the activity, and process the result
    private fun send(activity: ActivityRequest, time: Date?) {
        SdkMetrics.tracked(activity.requestId, time)
        appcuesCoroutineScope.launch {
            // this will respond with qualified experiences, if applicable
            repository.trackActivity(activity).also {
                it?.let { qualificationResult ->
                    // we will try to show experience from this list
                    experienceRenderer.show(qualificationResult)

                    if (qualificationResult.experiences.isEmpty()) {
                        // we know we are not rendering any experiences, so no metrics needed
                        // can proactively clear this request out
                        SdkMetrics.remove(activity.requestId)
                    }
                }
            }
        }
    }

    class AnalyticsQueueScheduler : QueueScheduler {

        private var debounceTimer: TimerTask? = null

        override fun schedule(delay: Long, block: () -> Unit) {
            // if null, then schedule
            if (debounceTimer == null) {
                debounceTimer = Timer().schedule(delay = delay) {
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

private fun List<ActivityRequest>.merge(): ActivityRequest? {
    if (isEmpty()) return null
    val activity = first()
    val events = mutableListOf<EventRequest>()
    val profileUpdate = hashMapOf<String, Any>()
    val groupUpdate = hashMapOf<String, Any>()
    var groupId: String? = null
    forEach { item ->
        // merge events
        item.events?.let {
            events.addAll(it)
        }
        // merge auto properties in profile update
        if (item.profileUpdate != null) {
            profileUpdate.putAll(item.profileUpdate)
        }
        // since a group update might get merged in with a new user identify,
        // we need to take the latest groupId and merge group props as well
        groupId = item.groupId
        if (item.groupUpdate != null) {
            groupUpdate.putAll(item.groupUpdate)
        }
    }
    return activity.copy(
        groupId = groupId,
        events = events.ifEmpty { null },
        profileUpdate = profileUpdate.ifEmpty { null },
        groupUpdate = groupUpdate.ifEmpty { null },
    )
}

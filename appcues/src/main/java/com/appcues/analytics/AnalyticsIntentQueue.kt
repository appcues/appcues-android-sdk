package com.appcues.analytics

import androidx.annotation.VisibleForTesting
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule

internal class AnalyticsIntentQueue(private val scheduler: QueueScheduler) {

    interface IntentProcessor {

        fun process(intents: List<AnalyticsIntent>)
    }

    // Analytics is also the one that process intents when we send it back
    private lateinit var processor: IntentProcessor

    fun setProcessor(processor: IntentProcessor) {
        this.processor = processor
    }

    interface QueueScheduler {

        fun schedule(block: () -> Unit)
        fun cancel()
    }

    private val queue = mutableListOf<AnalyticsIntent>()

    @VisibleForTesting
    fun queueForTesting(intent: AnalyticsIntent) {
        queue.add(intent)
    }

    fun queue(intent: AnalyticsIntent) {
        synchronized(this) {
            // add activity to pending activities
            queue.add(intent)
            // and schedule the flush task
            scheduler.schedule {
                synchronized(this) {
                    flushQueue()
                }
            }
        }
    }

    fun queueThenFlush(intent: AnalyticsIntent) {
        synchronized(this) {
            scheduler.cancel()
            // add new intent to queue and flush all
            queue.add(intent)

            flushQueue()
        }
    }

    fun flushAndProcess(intent: AnalyticsIntent) {
        synchronized(this) {
            scheduler.cancel()
            // send all pending activities as a merged activity request
            flushQueue()
            // then send another one for our newer activity
            processor.process(listOf(intent))
        }
    }

    fun flushAsync() {
        // to be called when any pending activity should immediately be flushed to cache, and network if possible
        // i.e. app going to background / being killed
        synchronized(this) {
            flushQueue()
        }
    }

    private fun flushQueue() {
        processor.process(queue)

        queue.clear()
    }

    class DefaultQueueScheduler(private val delay: Long = 10000L) : QueueScheduler {

        private var debounceTimer: TimerTask? = null

        override fun schedule(block: () -> Unit) {
            // if null, then schedule
            if (debounceTimer == null) {
                debounceTimer = Timer().schedule(delay) {
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

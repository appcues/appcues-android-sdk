package com.appcues.analytics

import androidx.annotation.VisibleForTesting
import com.appcues.analytics.AnalyticsIntentQueue.QueueAction.FLUSH_THEN_PROCESS
import com.appcues.analytics.AnalyticsIntentQueue.QueueAction.QUEUE
import com.appcues.analytics.AnalyticsIntentQueue.QueueAction.QUEUE_THEN_FLUSH
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule

internal class AnalyticsIntentQueue(private val scheduler: QueueScheduler) {

    enum class QueueAction {
        QUEUE, QUEUE_THEN_FLUSH, FLUSH_THEN_PROCESS
    }

    interface IntentProcessor {

        // process given intent list, usually the queue is cleared after this is called
        fun process(intents: List<AnalyticsIntent>)
    }

    // Analytics is also the one that process intents when we send it back
    private lateinit var processor: IntentProcessor

    fun setProcessor(processor: IntentProcessor) {
        this.processor = processor
    }

    // default implementation uses a timer, which is hard to test, so keeping this abstraction layer
    // makes it easier to unit test this class
    interface QueueScheduler {

        fun schedule(block: () -> Unit)
        fun cancel()
    }

    private val queue = mutableListOf<AnalyticsIntent>()

    @VisibleForTesting
    fun queueForTesting(intent: AnalyticsIntent) {
        queue.add(intent)
    }

    fun queue(intent: AnalyticsIntent, action: QueueAction) {
        when (action) {
            QUEUE -> queue(intent)
            QUEUE_THEN_FLUSH -> queueThenFlush(intent)
            FLUSH_THEN_PROCESS -> flushThenProcess(intent)
        }
    }

    fun flush() {
        // to be called when any pending activity should immediately be flushed to cache, and network if possible
        // i.e. app going to background / being killed
        synchronized(this) {
            processAndClear()
        }
    }

    private fun queue(intent: AnalyticsIntent) {
        synchronized(this) {
            // add activity to pending activities
            queue.add(intent)
            // and schedule the flush task
            scheduler.schedule {
                synchronized(this) {
                    processAndClear()
                }
            }
        }
    }

    private fun queueThenFlush(intent: AnalyticsIntent) {
        synchronized(this) {
            scheduler.cancel()
            // add new intent to queue and flush all
            queue.add(intent)

            processAndClear()
        }
    }

    private fun flushThenProcess(intent: AnalyticsIntent) {
        synchronized(this) {
            scheduler.cancel()
            // send all pending activities as a merged activity request
            processAndClear()
            // then send another one for our newer activity
            processor.process(listOf(intent))
        }
    }

    private fun processAndClear() {
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

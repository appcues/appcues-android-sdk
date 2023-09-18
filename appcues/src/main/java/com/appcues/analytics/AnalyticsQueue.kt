package com.appcues.analytics

import androidx.annotation.VisibleForTesting
import com.appcues.analytics.AnalyticsQueue.QueueAction.FLUSH_THEN_PROCESS
import com.appcues.analytics.AnalyticsQueue.QueueAction.QUEUE
import com.appcues.analytics.AnalyticsQueue.QueueAction.QUEUE_THEN_FLUSH
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule

internal class AnalyticsQueue(private val scheduler: QueueScheduler) {

    enum class QueueAction {
        QUEUE, QUEUE_THEN_FLUSH, FLUSH_THEN_PROCESS
    }

    interface QueueProcessor {

        // process given intent list, usually the queue is cleared after this is called
        fun process(items: List<AnalyticActivity>)
    }

    // Analytics is also the one that process intents when we send it back
    private lateinit var processor: QueueProcessor

    fun setProcessor(processor: QueueProcessor) {
        this.processor = processor
    }

    // default implementation uses a timer, which is hard to test, so keeping this abstraction layer
    // makes it easier to unit test this class
    interface QueueScheduler {

        fun schedule(block: () -> Unit)
        fun cancel()
    }

    private val queue = mutableListOf<AnalyticActivity>()

    @VisibleForTesting
    fun enqueueForTesting(item: AnalyticActivity) {
        queue.add(item)
    }

    fun enqueue(item: AnalyticActivity, action: QueueAction) {
        when (action) {
            QUEUE -> enqueue(item)
            QUEUE_THEN_FLUSH -> enqueueThenFlush(item)
            FLUSH_THEN_PROCESS -> flushThenProcess(item)
        }
    }

    fun flush() {
        // to be called when any pending activity should immediately be flushed to cache, and network if possible
        // i.e. app going to background / being killed
        synchronized(this) {
            processAndClear()
        }
    }

    private fun enqueue(item: AnalyticActivity) {
        synchronized(this) {
            // add activity to pending activities
            queue.add(item)
            // and schedule the flush task
            scheduler.schedule {
                synchronized(this) {
                    processAndClear()
                }
            }
        }
    }

    private fun enqueueThenFlush(item: AnalyticActivity) {
        synchronized(this) {
            scheduler.cancel()
            // add new intent to queue and flush all
            queue.add(item)

            processAndClear()
        }
    }

    private fun flushThenProcess(item: AnalyticActivity) {
        synchronized(this) {
            scheduler.cancel()
            // send all pending activities as a merged activity request
            processAndClear()
            // then send another one for our newer activity
            processor.process(listOf(item))
        }
    }

    private fun processAndClear() {
        // copy of queue before clearing the original reference
        processor.process(queue.toList())

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

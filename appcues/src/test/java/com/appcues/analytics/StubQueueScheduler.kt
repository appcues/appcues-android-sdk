package com.appcues.analytics

import com.appcues.analytics.AnalyticsQueueProcessor.QueueScheduler
import io.mockk.mockk

internal class StubQueueScheduler : QueueScheduler {

    val mockkScheduler: QueueScheduler = mockk(relaxed = true)

    // used to guard against setting up queue handling more than once for deferred
    // execution tests
    private var pendingBlock: (() -> Unit)? = null

    // can be set true by tests to have fine control over when the queue completion
    // block is processed. By default, queue items are processed immediately
    var deferCompletion = false

    override fun schedule(delay: Long, block: () -> Unit) {
        mockkScheduler.schedule(delay, block)

        if (deferCompletion) {
            pendingBlock = block
        } else {
            block()
        }
    }

    // used to allow tests to control exactly when the queue should process - simulating
    // the timer expiry
    fun processQueue() {
        pendingBlock?.invoke()
        pendingBlock = null
    }

    override fun cancel() {
        mockkScheduler.cancel()
    }
}

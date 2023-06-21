package com.appcues.analytics

import com.appcues.analytics.AnalyticsQueue.QueueScheduler
import io.mockk.mockk

internal class StubQueueScheduler : QueueScheduler {

    val mockkScheduler: QueueScheduler = mockk(relaxed = true)

    override fun schedule(block: () -> Unit) {
        mockkScheduler.schedule(block)
        block()
    }

    override fun cancel() {
        mockkScheduler.cancel()
    }
}

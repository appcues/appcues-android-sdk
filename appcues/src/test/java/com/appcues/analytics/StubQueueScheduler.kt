package com.appcues.analytics

import com.appcues.analytics.AnalyticsQueueProcessor.QueueScheduler
import io.mockk.mockk

internal class StubQueueScheduler : QueueScheduler {

    val mockkScheduler: QueueScheduler = mockk(relaxed = true)

    override fun schedule(delay: Long, block: () -> Unit) {
        mockkScheduler.schedule(delay, block)
        block()
    }

    override fun cancel() {
        mockkScheduler.cancel()
    }
}

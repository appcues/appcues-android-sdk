package com.appcues.analytics

import com.appcues.analytics.AnalyticsQueue.QueueProcessor
import com.appcues.data.remote.appcues.request.ActivityRequest
import com.appcues.data.remote.appcues.request.EventRequest
import com.appcues.rules.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class AnalyticsQueueTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val mockkEvent: EventRequest = mockk()
    private val mockkActivity: ActivityRequest =
        ActivityRequest(userId = "222", accountId = "111", events = listOf(mockkEvent))

    private val queueProcessor = mockk<QueueProcessor>(relaxed = true)
    private val queueScheduler = StubQueueScheduler()

    private lateinit var analyticsQueue: AnalyticsQueue

    @Before
    fun setup() {
        analyticsQueue = AnalyticsQueue(queueScheduler).apply { setProcessor(queueProcessor) }
    }

    @Test
    fun `queue SHOULD schedule and call processor`() {
        // when
        analyticsQueue.queue(mockkActivity)
        // then
        verify { queueScheduler.mockkScheduler.schedule(any()) }
        verify { queueProcessor.process(any()) }
    }

    @Test
    fun `queueThenFlush SHOULD cancel scheduler and call processor`() {
        // given
        val activitySlot = slot<ActivityRequest>()
        // when
        analyticsQueue.queueThenFlush(mockkActivity)
        // then
        verify { queueScheduler.mockkScheduler.cancel() }
        verify { queueProcessor.process(any()) }
    }

    @Test
    fun `flushThenSend SHOULD cancel scheduler and call processor for pendingActivities AND for new activity`() {
        // given
        val queuedActivitySlot = slot<ActivityRequest>()
        val queuedEvent: EventRequest = mockk()
        val queuedActivity = ActivityRequest(userId = "222", accountId = "111", events = listOf(queuedEvent))
        val activitySlot = slot<ActivityRequest>()
        analyticsQueue.queueForTesting(queuedActivity)
        // when
        analyticsQueue.flushThenSend(mockkActivity)
        // then verify sequence of events
        verifySequence {
            queueScheduler.mockkScheduler.cancel()
            queueProcessor.process(capture(queuedActivitySlot))
            queueProcessor.process(capture(activitySlot))
        }
        // check that correct events where captured by mockk
        assertThat(queuedActivitySlot.captured.events).hasSize(1)
        assertThat(queuedActivitySlot.captured.events!![0]).isEqualTo(queuedEvent)
        assertThat(activitySlot.captured.events).hasSize(1)
        assertThat(activitySlot.captured.events!![0]).isEqualTo(mockkEvent)
    }

    @Test
    fun `flushAsync SHOULD call processor for pendingActivities`() {
        // given
        val queuedActivitySlot = slot<ActivityRequest>()
        val queuedActivity = ActivityRequest(userId = "222", accountId = "111", events = listOf())
        analyticsQueue.queueForTesting(queuedActivity)
        // when
        analyticsQueue.flushAsync()
        // then
        verify { queueProcessor.process(capture(queuedActivitySlot)) }
    }
}

package com.appcues.analytics

import com.appcues.AppcuesCoroutineScope
import com.appcues.data.AppcuesRepository
import com.appcues.data.model.ExperienceTrigger.Qualification
import com.appcues.data.model.QualificationResult
import com.appcues.data.remote.appcues.request.ActivityRequest
import com.appcues.data.remote.appcues.request.EventRequest
import com.appcues.logging.Logcues
import com.appcues.rules.MainDispatcherRule
import com.appcues.ui.ExperienceRenderer
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

internal class AnalyticsQueueProcessorTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val mockkEvent: EventRequest = mockk()
    private val mockkActivity: ActivityRequest =
        ActivityRequest(userId = "222", accountId = "111", appId = "appId", sessionId = UUID.randomUUID(), events = listOf(mockkEvent))
    private val mockkQualificationResult: QualificationResult =
        QualificationResult(Qualification("screen_view"), arrayListOf(mockk()))

    private val coroutineScope = AppcuesCoroutineScope(Logcues())
    private val experienceRenderer: ExperienceRenderer = mockk()
    private val repository: AppcuesRepository = mockk<AppcuesRepository>().apply {
        coEvery { trackActivity(any()) } returns mockkQualificationResult
    }

    private val backgroundQueueScheduler = StubQueueScheduler()
    private val priorityQueueScheduler = StubQueueScheduler()

    private lateinit var analyticsQueueProcessor: AnalyticsQueueProcessor

    @Before
    fun setup() {
        analyticsQueueProcessor = AnalyticsQueueProcessor(
            appcuesCoroutineScope = coroutineScope,
            experienceRenderer = experienceRenderer,
            repository = repository,
            backgroundQueueScheduler = backgroundQueueScheduler,
            priorityQueueScheduler = priorityQueueScheduler,
        )
    }

    @After
    fun tearDown() {
        // reset any usage of queue completion handler for next test
        backgroundQueueScheduler.deferCompletion = false
        priorityQueueScheduler.deferCompletion = false
    }

    @Test
    fun `queue SHOULD schedule and call repository on callback`() {
        // given
        val activitySlot = slot<ActivityRequest>()
        // when
        analyticsQueueProcessor.queue(mockkActivity)
        // then
        verify { backgroundQueueScheduler.mockkScheduler.schedule(any(), any()) }
        coVerify { repository.trackActivity(capture(activitySlot)) }
        coVerify { experienceRenderer.show(mockkQualificationResult) }
        assertThat(activitySlot.captured.events).hasSize(1)
        assertThat(activitySlot.captured.events!![0]).isEqualTo(mockkEvent)
    }

    @Test
    fun `queueThenFlush SHOULD cancel scheduler and call repository`() {
        // given
        val activitySlot = slot<ActivityRequest>()
        // when
        analyticsQueueProcessor.queueThenFlush(mockkActivity)
        // then
        verify { backgroundQueueScheduler.mockkScheduler.cancel() }
        coVerify { repository.trackActivity(capture(activitySlot)) }
        coVerify { experienceRenderer.show(mockkQualificationResult) }
    }

    @Test
    fun `flushThenSend SHOULD cancel scheduler and call repository for pendingActivities AND for new activity`() {
        // given
        val queuedActivitySlot = slot<ActivityRequest>()
        val queuedEvent: EventRequest = mockk()
        val queuedActivity = ActivityRequest(
            userId = "222",
            appId = "appId",
            accountId = "111",
            sessionId = UUID.randomUUID(),
            events = listOf(queuedEvent)
        )
        val activitySlot = slot<ActivityRequest>()
        analyticsQueueProcessor.queueForTesting(queuedActivity)
        // when
        analyticsQueueProcessor.flushThenSend(mockkActivity)
        // then verify sequence of events
        coVerifySequence {
            backgroundQueueScheduler.mockkScheduler.cancel()
            repository.trackActivity(capture(queuedActivitySlot))
            experienceRenderer.show(mockkQualificationResult)
            repository.trackActivity(capture(activitySlot))
            experienceRenderer.show(mockkQualificationResult)
        }

        // check that correct events where captured by mockk
        assertThat(queuedActivitySlot.captured.events).hasSize(1)
        assertThat(queuedActivitySlot.captured.events!![0]).isEqualTo(queuedEvent)
        assertThat(activitySlot.captured.events).hasSize(1)
        assertThat(activitySlot.captured.events!![0]).isEqualTo(mockkEvent)
    }

    @Test
    fun `flushAsync SHOULD call repository for pendingActivities`() {
        // given
        val queuedActivitySlot = slot<ActivityRequest>()
        val queuedEvent: EventRequest = mockk()
        val queuedActivity =
            ActivityRequest(userId = "222", accountId = "111", appId = "appId", sessionId = UUID.randomUUID(), events = listOf(queuedEvent))
        analyticsQueueProcessor.queueForTesting(queuedActivity)
        // when
        analyticsQueueProcessor.flushAsync()
        // then
        coVerifySequence {
            repository.trackActivity(capture(queuedActivitySlot))
            experienceRenderer.show(mockkQualificationResult)
        }
    }

    @Test
    fun `flushThenSend SHOULD batch activity WHEN waitForBatch is true AND a second Activity is sent immediately after`() {
        // given
        val identifyActivity = ActivityRequest(
            userId = "user-1",
            appId = "appId",
            accountId = "00000",
            sessionId = UUID.randomUUID(),
            profileUpdate = mutableMapOf("userProp" to 1)
        )
        val groupActivity = ActivityRequest(
            userId = "user-1",
            appId = "appId",
            accountId = "00000",
            sessionId = UUID.randomUUID(),
            groupId = "group-id",
            groupUpdate = mutableMapOf("groupProp" to 2)
        )

        val mergedActivitySlot = slot<ActivityRequest>()
        priorityQueueScheduler.deferCompletion = true
        // when
        analyticsQueueProcessor.flushThenSend(identifyActivity, true)
        analyticsQueueProcessor.flushThenSend(groupActivity, false)
        priorityQueueScheduler.processQueue() // simulates priority queue completion
        // then verify sequence of events
        coVerifySequence {
            repository.trackActivity(capture(mergedActivitySlot))
            experienceRenderer.show(mockkQualificationResult)
        }

        // check that correct events where captured by mockk
        assertThat(mergedActivitySlot.captured.groupId).isEqualTo("group-id")
        assertThat(mergedActivitySlot.captured.profileUpdate!!["userProp"]).isEqualTo(1)
        assertThat(mergedActivitySlot.captured.groupUpdate!!["groupProp"]).isEqualTo(2)
    }

    @Test
    fun `flushThenSend SHOULD NOT batch activity WHEN waitForBatch is true AND second Activity is for different user ID`() {
        // given
        val identifyActivity1 = ActivityRequest(
            userId = "user-1",
            appId = "appId",
            accountId = "00000",
            sessionId = UUID.randomUUID(),
            profileUpdate = mutableMapOf("userProp" to 1)
        )
        val identifyActivity2 = ActivityRequest(
            userId = "user-2",
            appId = "appId",
            accountId = "00000",
            sessionId = UUID.randomUUID(),
            profileUpdate = mutableMapOf("groupProp" to 2)
        )

        val identify1ActivitySlot = slot<ActivityRequest>()
        val identify2ActivitySlot = slot<ActivityRequest>()
        priorityQueueScheduler.deferCompletion = true
        // when
        analyticsQueueProcessor.flushThenSend(identifyActivity1, true)
        analyticsQueueProcessor.flushThenSend(identifyActivity2, true)
        priorityQueueScheduler.processQueue() // simulates priority queue completion
        // then verify sequence of events
        coVerifySequence {
            backgroundQueueScheduler.mockkScheduler.cancel() // start processing 1st item
            priorityQueueScheduler.mockkScheduler.schedule(any(), any())
            backgroundQueueScheduler.mockkScheduler.cancel() // start processing 2nd item
            priorityQueueScheduler.mockkScheduler.cancel() // priority queue is flushed due to user change
            repository.trackActivity(capture(identify1ActivitySlot))
            experienceRenderer.show(mockkQualificationResult)
            priorityQueueScheduler.mockkScheduler.schedule(any(), any()) // now queue 2nd item
            repository.trackActivity(capture(identify2ActivitySlot))
            experienceRenderer.show(mockkQualificationResult)
        }

        // check that correct events where captured by mockk
        assertThat(identify1ActivitySlot.captured.userId).isEqualTo("user-1")
        assertThat(identify2ActivitySlot.captured.userId).isEqualTo("user-2")
    }

    @Test
    fun `flushThenSend SHOULD batch activity WHEN waitForBatch is true AND and queueThenFlush is called immediately after`() {
        // given
        val identifyActivity = ActivityRequest(
            userId = "user-1",
            appId = "appId",
            accountId = "00000",
            sessionId = UUID.randomUUID(),
            profileUpdate = mutableMapOf("userProp" to 1)
        )
        val eventActivity = ActivityRequest(
            userId = "user-1",
            appId = "appId",
            accountId = "00000",
            sessionId = UUID.randomUUID(),
            events = listOf(mockkEvent)
        )

        val mergedActivitySlot = slot<ActivityRequest>()
        priorityQueueScheduler.deferCompletion = true
        // when
        analyticsQueueProcessor.flushThenSend(identifyActivity, true)
        analyticsQueueProcessor.queueThenFlush(eventActivity)
        priorityQueueScheduler.processQueue() // simulates priority queue completion
        // then verify sequence of events
        coVerifySequence {
            repository.trackActivity(capture(mergedActivitySlot))
            experienceRenderer.show(mockkQualificationResult)
        }

        // check that correct events where captured by mockk
        assertThat(mergedActivitySlot.captured.profileUpdate!!["userProp"]).isEqualTo(1)
        assertThat(mergedActivitySlot.captured.events!![0]).isEqualTo(mockkEvent)
    }

    @Test
    fun `flushThenSend SHOULD NOT batch activity WHEN waitForBatch is true AND and queueThenFlush is called after queue is processed`() {
        // given
        val identifyActivity = ActivityRequest(
            userId = "user-1",
            appId = "appId",
            accountId = "00000",
            sessionId = UUID.randomUUID(),
            profileUpdate = mutableMapOf("userProp" to 1)
        )
        val eventActivity = ActivityRequest(
            userId = "user-1",
            appId = "appId",
            accountId = "00000",
            sessionId = UUID.randomUUID(),
            events = listOf(mockkEvent)
        )

        val activitySlot1 = slot<ActivityRequest>()
        val activitySlot2 = slot<ActivityRequest>()
        priorityQueueScheduler.deferCompletion = true
        // when
        analyticsQueueProcessor.flushThenSend(identifyActivity, true)
        priorityQueueScheduler.processQueue() // simulates priority queue completion BEFORE 2nd activity
        analyticsQueueProcessor.queueThenFlush(eventActivity)
        // then verify sequence of events
        coVerifySequence {
            repository.trackActivity(capture(activitySlot1))
            repository.trackActivity(capture(activitySlot2))
        }

        // check that correct events where captured by mockk
        assertThat(activitySlot1.captured.profileUpdate!!["userProp"]).isEqualTo(1)
        assertThat(activitySlot2.captured.events!![0]).isEqualTo(mockkEvent)
    }
}

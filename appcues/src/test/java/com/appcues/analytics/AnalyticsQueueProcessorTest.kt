package com.appcues.analytics

import com.appcues.AppcuesCoroutineScope
import com.appcues.LoggingLevel.NONE
import com.appcues.data.AppcuesRepository
import com.appcues.data.model.Experience
import com.appcues.data.remote.request.ActivityRequest
import com.appcues.data.remote.request.EventRequest
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AnalyticsQueueProcessorTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val mockkEvent: EventRequest = mockk()
    private val mockkActivity: ActivityRequest = ActivityRequest(userId = "222", accountId = "111", events = listOf(mockkEvent))
    private val mockkExperiences: List<Experience> = arrayListOf(mockk())

    private val coroutineScope = AppcuesCoroutineScope(Logcues(NONE))
    private val experienceRenderer: ExperienceRenderer = mockk()
    private val repository: AppcuesRepository = mockk<AppcuesRepository>().apply {
        coEvery { trackActivity(any()) } returns mockkExperiences
    }

    private val queueScheduler = StubQueueScheduler()

    private lateinit var analyticsQueueProcessor: AnalyticsQueueProcessor

    @Before
    fun setup() {
        analyticsQueueProcessor = AnalyticsQueueProcessor(
            appcuesCoroutineScope = coroutineScope,
            experienceRenderer = experienceRenderer,
            repository = repository,
            analyticsQueueScheduler = queueScheduler,
        )
    }

    @Test
    fun `queue SHOULD schedule and call repository on callback`() {
        // given
        val activitySlot = slot<ActivityRequest>()
        // when
        analyticsQueueProcessor.queue(mockkActivity)
        // then
        verify { queueScheduler.mockkScheduler.schedule(any()) }
        coVerify { repository.trackActivity(capture(activitySlot)) }
        coVerify { experienceRenderer.show(mockkExperiences) }
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
        verify { queueScheduler.mockkScheduler.cancel() }
        coVerify { repository.trackActivity(capture(activitySlot)) }
        coVerify { experienceRenderer.show(mockkExperiences) }
    }

    @Test
    fun `flushThenSend SHOULD cancel scheduler and call repository for pendingActivities AND for new activity`() {
        // given
        val queuedActivitySlot = slot<ActivityRequest>()
        val queuedEvent: EventRequest = mockk()
        val queuedActivity = ActivityRequest(userId = "222", accountId = "111", events = listOf(queuedEvent))
        val activitySlot = slot<ActivityRequest>()
        analyticsQueueProcessor.queueForTesting(queuedActivity)
        // when
        analyticsQueueProcessor.flushThenSend(mockkActivity)
        // then verify sequence of events
        coVerifySequence {
            queueScheduler.mockkScheduler.cancel()
            repository.trackActivity(capture(queuedActivitySlot))
            experienceRenderer.show(mockkExperiences)
            repository.trackActivity(capture(activitySlot))
            experienceRenderer.show(mockkExperiences)
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
        val queuedActivity = ActivityRequest(userId = "222", accountId = "111", events = listOf(queuedEvent))
        analyticsQueueProcessor.queueForTesting(queuedActivity)
        // when
        analyticsQueueProcessor.flushAsync()
        // then
        coVerifySequence {
            repository.trackActivity(capture(queuedActivitySlot))
            experienceRenderer.show(mockkExperiences)
        }
    }
}

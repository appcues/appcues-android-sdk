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
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.launch
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AnalyticsTrackerTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val coroutineScope = AppcuesCoroutineScope(Logcues(NONE))
    private val repository: AppcuesRepository = mockk()
    private val experienceRenderer: ExperienceRenderer = mockk()
    private val activityBuilder: ActivityRequestBuilder = mockk()
    private val experienceLifecycleTracker: ExperienceLifecycleTracker = mockk(relaxed = true)
    private val analyticsPolicy: AnalyticsPolicy = mockk()

    private val analyticsFlowUpdates: ArrayList<ActivityRequest> = arrayListOf()

    private lateinit var analyticsTracker: AnalyticsTracker

    @Before
    fun setup() {
        analyticsTracker = AnalyticsTracker(
            appcuesCoroutineScope = coroutineScope,
            repository = repository,
            experienceRenderer = experienceRenderer,
            activityBuilder = activityBuilder,
            experienceLifecycleTracker = experienceLifecycleTracker,
            analyticsPolicy = analyticsPolicy,
        )

        coroutineScope.launch {
            analyticsTracker.analyticsFlow.collect {
                analyticsFlowUpdates.add(it)
            }
        }
    }

    @Test
    fun `init SHOULD trigger experienceLifecycleTracker start`() {
        // then
        coVerify { experienceLifecycleTracker.start() }
    }

    @Test
    fun `identify SHOULD update analyticsFlow`() {
        // given
        every { analyticsPolicy.canIdentify() } returns true
        every { activityBuilder.identify(any()) } returns mockk()
        // when
        analyticsTracker.identify()
        // then
        assertThat(analyticsFlowUpdates).hasSize(1)
    }

    @Test
    fun `identify SHOULD call repository AND show experience`() {
        // given
        val activityRequest: ActivityRequest = mockk()
        val experienceList: List<Experience> = listOf(mockk())
        every { analyticsPolicy.canIdentify() } returns true
        every { activityBuilder.identify(any()) } returns activityRequest
        coEvery { repository.trackActivity(activityRequest) } returns experienceList
        // when
        analyticsTracker.identify()
        // then
        coVerify { repository.trackActivity(activityRequest) }
        coVerify { experienceRenderer.show(experienceList) }
    }

    @Test
    fun `identify SHOULD not call repository WHEN canIdentify is false`() {
        // given
        every { analyticsPolicy.canIdentify() } returns false
        // when
        analyticsTracker.identify()
        // then
        coVerify { repository wasNot Called }
    }

    @Test
    fun `identify SHOULD call repository for enqueued activities before identify activity`() {
        // given
        every { analyticsPolicy.canIdentify() } returns true
        every { analyticsPolicy.canTrackEvent() } returns true
        // enqueue event
        val enqueuedEvent: EventRequest = mockk(relaxed = true)
        every { activityBuilder.track(any(), any()) } returns ActivityRequest(userId = "", accountId = "", events = listOf(enqueuedEvent))
        analyticsTracker.track("event1", interactive = false)
        val identifyActivity: ActivityRequest = mockk()
        every { activityBuilder.identify(any()) } returns identifyActivity
        // when
        analyticsTracker.identify()
        // then
        val activityRequestSlot = slot<ActivityRequest>()
        coVerifySequence {
            repository.trackActivity(capture(activityRequestSlot))
            repository.trackActivity(identifyActivity)
        }
        with(activityRequestSlot.captured) {
            assertThat(events).hasSize(1)
            assertThat(events!![0]).isEqualTo(enqueuedEvent)
        }
    }
}

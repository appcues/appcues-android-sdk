package com.appcues.analytics

import com.appcues.AppcuesCoroutineScope
import com.appcues.LoggingLevel.NONE
import com.appcues.data.AppcuesRepository
import com.appcues.data.model.Experience
import com.appcues.data.remote.request.ActivityRequest
import com.appcues.logging.Logcues
import com.appcues.rules.MainDispatcherRule
import com.appcues.ui.ExperienceRenderer
import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
    private val analyticsQueueManager: AnalyticsQueueManager = mockk(relaxed = true)

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
            analyticsQueueManager = analyticsQueueManager,
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
    fun `identify SHOULD do nothing WHEN canIdentify is false`() {
        // given
        every { analyticsPolicy.canIdentify() } returns false
        // when
        analyticsTracker.identify()
        // then
        verify { analyticsQueueManager wasNot Called }
    }

    @Test
    fun `identify SHOULD update analyticsFlow AND flushThenSend`() {
        // given
        every { analyticsPolicy.canIdentify() } returns true
        val activity: ActivityRequest = mockk()
        every { activityBuilder.identify(any()) } returns activity
        // when
        analyticsTracker.identify()
        // then
        assertThat(analyticsFlowUpdates).hasSize(1)
        verify { analyticsQueueManager.flushThenSend(activity, any()) }
    }

    @Test
    fun `track SHOULD do nothing WHEN canTrackEvent is false`() {
        // given
        every { analyticsPolicy.canTrackEvent() } returns false
        // when
        analyticsTracker.track("event1")
        // then
        verify { analyticsQueueManager wasNot Called }
    }

    @Test
    fun `track SHOULD update analyticsFlow AND queueThenFlush WHEN interactive is true`() {
        // given
        every { analyticsPolicy.canTrackEvent() } returns true
        val activity: ActivityRequest = mockk()
        every { activityBuilder.track(any()) } returns activity
        // when
        analyticsTracker.track("event1", interactive = true)
        // then
        assertThat(analyticsFlowUpdates).hasSize(1)
        verify { analyticsQueueManager.queueThenFlush(activity, any()) }
    }

    @Test
    fun `track SHOULD update analyticsFlow AND queue WHEN interactive is false`() {
        // given
        every { analyticsPolicy.canTrackEvent() } returns true
        val activity: ActivityRequest = mockk()
        every { activityBuilder.track(any()) } returns activity
        // when
        analyticsTracker.track("event1", interactive = false)
        // then
        assertThat(analyticsFlowUpdates).hasSize(1)
        verify { analyticsQueueManager.queue(activity, any()) }
    }

    @Test
    fun `screen SHOULD do nothing WHEN canTrackScreen is false`() {
        // given
        every { analyticsPolicy.canTrackScreen("title") } returns false
        // when
        analyticsTracker.screen("title")
        // then
        verify { analyticsQueueManager wasNot Called }
    }

    @Test
    fun `screen SHOULD update analyticsFlow AND queueThenFlush`() {
        // given
        every { analyticsPolicy.canTrackScreen("title") } returns true
        val activity: ActivityRequest = mockk()
        every { activityBuilder.screen(any()) } returns activity
        // when
        analyticsTracker.screen("title")
        // then
        assertThat(analyticsFlowUpdates).hasSize(1)
        verify { analyticsQueueManager.queueThenFlush(activity, any()) }
    }

    @Test
    fun `group SHOULD do nothing WHEN canTrackGroup is false`() {
        // given
        every { analyticsPolicy.canTrackGroup() } returns false
        // when
        analyticsTracker.group()
        // then
        verify { analyticsQueueManager wasNot Called }
    }

    @Test
    fun `group SHOULD update analyticsFlow AND flushThenSend`() {
        // given
        every { analyticsPolicy.canTrackGroup() } returns true
        val activity: ActivityRequest = mockk()
        every { activityBuilder.group() } returns activity
        // when
        analyticsTracker.group()
        // then
        assertThat(analyticsFlowUpdates).hasSize(1)
        verify { analyticsQueueManager.flushThenSend(activity, any()) }
    }

    @Test
    fun `flushPendingActivity SHOULD flushAsync`() {
        // when
        analyticsTracker.flushPendingActivity()
        // then
        verify { analyticsQueueManager.flushAsync(any()) }
    }

    @Test
    fun `send SHOULD be called on callbacks`() {
        // given
        val activity = mockk<ActivityRequest>()
        every { analyticsQueueManager.flushAsync(any()) } answers {
            val function = firstArg() as ((ActivityRequest) -> Unit)
            function(activity)
        }
        val listExperience = listOf<Experience>(mockk())
        coEvery { repository.trackActivity(activity) } returns listExperience
        // when
        analyticsTracker.flushPendingActivity()
        // then
        coVerify { repository.trackActivity(activity) }
        coVerify { experienceRenderer.show(listExperience) }
    }
}

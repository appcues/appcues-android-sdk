package com.appcues.analytics

import com.appcues.AnalyticType
import com.appcues.AppcuesCoroutineScope
import com.appcues.LoggingLevel.NONE
import com.appcues.analytics.AnalyticsEvent.SessionStarted
import com.appcues.data.AppcuesRepository
import com.appcues.data.remote.appcues.request.ActivityRequest
import com.appcues.logging.Logcues
import com.appcues.rules.MainDispatcherRule
import com.appcues.ui.ExperienceRenderer
import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.launch
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class AnalyticsTrackerTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val coroutineScope = AppcuesCoroutineScope(Logcues(NONE))
    private val activityBuilder: ActivityRequestBuilder = mockk()
    private val analyticsPolicy: AnalyticsPolicy = mockk()
    private val analyticsQueue: AnalyticsQueue = mockk(relaxed = true)
    private val repository: AppcuesRepository = mockk(relaxed = true)
    private val experienceRenderer: ExperienceRenderer = mockk(relaxed = true)

    private val analyticsFlowUpdates: ArrayList<TrackingData> = arrayListOf()

    private lateinit var analyticsTracker: AnalyticsTracker

    @Before
    fun setup() {

        analyticsTracker = AnalyticsTracker(
            appcuesCoroutineScope = coroutineScope,
            activityBuilder = activityBuilder,
            analyticsPolicy = analyticsPolicy,
            analyticsQueue = analyticsQueue,
            repository = repository,
            experienceRenderer = experienceRenderer,
        )

        coroutineScope.launch {
            analyticsTracker.analyticsFlow.collect {
                analyticsFlowUpdates.add(it)
            }
        }
    }

    @Test
    fun `identify SHOULD do nothing WHEN canIdentify is false`() {
        // given
        every { analyticsPolicy.canIdentify() } returns false
        // when
        analyticsTracker.identify()
        // then
        verify { activityBuilder wasNot Called }
    }

    @Test
    fun `identify SHOULD update analyticsFlow AND flushThenSend`() {
        // given
        every { analyticsPolicy.canIdentify() } returns true
        val activity: ActivityRequest = mockk(relaxed = true)
        every { activityBuilder.identify(any()) } returns activity
        // when
        analyticsTracker.identify()
        // then
        assertThat(analyticsFlowUpdates).hasSize(1)
        verify { analyticsQueue.flushThenSend(activity) }
        assertThat(analyticsFlowUpdates.first().type).isEqualTo(AnalyticType.IDENTIFY)
        assertThat(analyticsFlowUpdates.first().isInternal).isFalse()
        assertThat(analyticsFlowUpdates.first().request).isEqualTo(activity)
    }

    @Test
    fun `track SHOULD do nothing WHEN canTrackEvent is false`() {
        // given
        every { analyticsPolicy.canTrackEvent() } returns false
        // when
        analyticsTracker.track("event1")
        // then
        verify { activityBuilder wasNot Called }
    }

    @Test
    fun `track SHOULD update analyticsFlow AND queueThenFlush WHEN interactive is true`() {
        // given
        every { analyticsPolicy.canTrackEvent() } returns true
        val activity: ActivityRequest = mockk(relaxed = true)
        every { activityBuilder.track(any()) } returns activity
        // when
        analyticsTracker.track("event1", interactive = true)
        // then
        assertThat(analyticsFlowUpdates).hasSize(1)
        verify { analyticsQueue.queueThenFlush(activity) }
        assertThat(analyticsFlowUpdates.first().type).isEqualTo(AnalyticType.EVENT)
        assertThat(analyticsFlowUpdates.first().isInternal).isFalse()
        assertThat(analyticsFlowUpdates.first().request).isEqualTo(activity)
    }

    @Test
    fun `track SHOULD update analyticsFlow AND queue WHEN interactive is false`() {
        // given
        every { analyticsPolicy.canTrackEvent() } returns true
        val activity: ActivityRequest = mockk(relaxed = true)
        every { activityBuilder.track(any()) } returns activity
        // when
        analyticsTracker.track("event1", interactive = false)
        // then
        assertThat(analyticsFlowUpdates).hasSize(1)
        verify { analyticsQueue.queue(activity) }
        assertThat(analyticsFlowUpdates.first().type).isEqualTo(AnalyticType.EVENT)
        assertThat(analyticsFlowUpdates.first().isInternal).isFalse()
        assertThat(analyticsFlowUpdates.first().request).isEqualTo(activity)
    }

    @Test
    fun `screen SHOULD do nothing WHEN canTrackScreen is false`() {
        // given
        every { analyticsPolicy.canTrackScreen() } returns false
        // when
        analyticsTracker.screen("title")
        // then
        verify { activityBuilder wasNot Called }
    }

    @Test
    fun `screen SHOULD update analyticsFlow AND queueThenFlush`() {
        // given
        every { analyticsPolicy.canTrackScreen() } returns true
        val activity: ActivityRequest = mockk(relaxed = true)
        every { activityBuilder.screen(any()) } returns activity
        // when
        analyticsTracker.screen("title")
        // then
        assertThat(analyticsFlowUpdates).hasSize(1)
        verify { analyticsQueue.queueThenFlush(activity) }
        assertThat(analyticsFlowUpdates.first().type).isEqualTo(AnalyticType.SCREEN)
        assertThat(analyticsFlowUpdates.first().isInternal).isFalse()
        assertThat(analyticsFlowUpdates.first().request).isEqualTo(activity)
    }

    @Test
    fun `group SHOULD do nothing WHEN canTrackGroup is false`() {
        // given
        every { analyticsPolicy.canTrackGroup() } returns false
        // when
        analyticsTracker.group()
        // then
        verify { activityBuilder wasNot Called }
    }

    @Test
    fun `group SHOULD update analyticsFlow AND flushThenSend`() {
        // given
        every { analyticsPolicy.canTrackGroup() } returns true
        val activity: ActivityRequest = mockk(relaxed = true)
        every { activityBuilder.group() } returns activity
        // when
        analyticsTracker.group()
        // then
        assertThat(analyticsFlowUpdates).hasSize(1)
        verify { analyticsQueue.flushThenSend(activity) }
        assertThat(analyticsFlowUpdates.first().type).isEqualTo(AnalyticType.GROUP)
        assertThat(analyticsFlowUpdates.first().isInternal).isFalse()
        assertThat(analyticsFlowUpdates.first().request).isEqualTo(activity)
    }

    @Test
    fun `flushPendingActivity SHOULD flushAsync`() {
        // when
        analyticsTracker.flushPendingActivity()
        // then
        verify { analyticsQueue.flushAsync() }
    }

    @Test
    fun `track internal event SHOULD update analyticsFlow with internal event`() {
        // given
        every { analyticsPolicy.canTrackEvent() } returns true
        val activity: ActivityRequest = mockk(relaxed = true)
        every { activityBuilder.track(any(), any()) } returns activity
        // when
        analyticsTracker.track(SessionStarted)
        // then
        assertThat(analyticsFlowUpdates.first().isInternal).isTrue()
        assertThat(analyticsFlowUpdates.first().type).isEqualTo(AnalyticType.EVENT)
        assertThat(analyticsFlowUpdates.first().request).isEqualTo(activity)
    }
}

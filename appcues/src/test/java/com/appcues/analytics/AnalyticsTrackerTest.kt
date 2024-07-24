package com.appcues.analytics

import com.appcues.AnalyticType
import com.appcues.AppcuesCoroutineScope
import com.appcues.SessionMonitor
import com.appcues.data.remote.appcues.request.ActivityRequest
import com.appcues.logging.Logcues
import com.appcues.rules.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.launch
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

internal class AnalyticsTrackerTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val coroutineScope = AppcuesCoroutineScope(Logcues())
    private val activityBuilder: ActivityRequestBuilder = mockk()
    private val sessionMonitor: SessionMonitor = mockk(relaxed = true)
    private val analyticsQueueProcessor: AnalyticsQueueProcessor = mockk(relaxed = true)

    private val analyticsFlowUpdates: ArrayList<TrackingData> = arrayListOf()

    private lateinit var analyticsTracker: AnalyticsTracker

    @Before
    fun setup() {
        analyticsTracker = AnalyticsTracker(
            appcuesCoroutineScope = coroutineScope,
            activityBuilder = activityBuilder,
            sessionMonitor = sessionMonitor,
            analyticsQueueProcessor = analyticsQueueProcessor,
        )

        coroutineScope.launch {
            analyticsTracker.analyticsFlow.collect {
                analyticsFlowUpdates.add(it)
            }
        }
    }

    @Test
    fun `identify SHOULD do nothing WHEN no active session available`() {
        // given
        every { sessionMonitor.sessionId } returns null
        every { sessionMonitor.startNewSession() } returns null
        // when
        analyticsTracker.identify()
        // then
        verify { analyticsQueueProcessor wasNot Called }
    }

    @Test
    fun `identify SHOULD update analyticsFlow AND flushThenSend`() {
        // given
        val sessionId = UUID.randomUUID()
        every { sessionMonitor.sessionId } returns sessionId
        every { sessionMonitor.hasSession() } returns true
        val activity: ActivityRequest = mockk(relaxed = true)
        every { activityBuilder.identify(any()) } returns activity
        // when
        analyticsTracker.identify()
        // then
        verify { sessionMonitor.hasSession() }
        assertThat(analyticsFlowUpdates).hasSize(1)
        verify { analyticsQueueProcessor.flushThenSend(activity, true) }
        assertThat(analyticsFlowUpdates.first().type).isEqualTo(AnalyticType.IDENTIFY)
        assertThat(analyticsFlowUpdates.first().isInternal).isFalse()
        assertThat(analyticsFlowUpdates.first().request).isEqualTo(activity)
    }

    @Test
    fun `track SHOULD do nothing WHEN no active session available`() {
        // given
        every { sessionMonitor.sessionId } returns null
        every { sessionMonitor.startNewSession() } returns null
        // when
        analyticsTracker.track("event1")
        // then
        verify { analyticsQueueProcessor wasNot Called }
    }

    @Test
    fun `track SHOULD update analyticsFlow AND queueThenFlush WHEN interactive is true`() {
        // given
        val sessionId = UUID.randomUUID()
        every { sessionMonitor.sessionId } returns sessionId
        every { sessionMonitor.hasSession() } returns true
        val activity: ActivityRequest = mockk(relaxed = true)
        every { activityBuilder.track(sessionId, any()) } returns activity
        // when
        analyticsTracker.track("event1", interactive = true)
        // then
        verify { sessionMonitor.hasSession() }
        assertThat(analyticsFlowUpdates).hasSize(1)
        verify { analyticsQueueProcessor.queueThenFlush(activity) }
        assertThat(analyticsFlowUpdates.first().type).isEqualTo(AnalyticType.EVENT)
        assertThat(analyticsFlowUpdates.first().isInternal).isFalse()
        assertThat(analyticsFlowUpdates.first().request).isEqualTo(activity)
    }

    @Test
    fun `track SHOULD update analyticsFlow AND queue WHEN interactive is false`() {
        // given
        val sessionId = UUID.randomUUID()
        every { sessionMonitor.sessionId } returns sessionId
        every { sessionMonitor.hasSession() } returns true
        val activity: ActivityRequest = mockk(relaxed = true)
        every { activityBuilder.track(sessionId, any()) } returns activity
        // when
        analyticsTracker.track("event1", interactive = false)
        // then
        verify { sessionMonitor.hasSession() }
        assertThat(analyticsFlowUpdates).hasSize(1)
        verify { analyticsQueueProcessor.queue(activity) }
        assertThat(analyticsFlowUpdates.first().type).isEqualTo(AnalyticType.EVENT)
        assertThat(analyticsFlowUpdates.first().isInternal).isFalse()
        assertThat(analyticsFlowUpdates.first().request).isEqualTo(activity)
    }

    @Test
    fun `screen SHOULD do nothing WHEN no active session available`() {
        // given
        every { sessionMonitor.sessionId } returns null
        every { sessionMonitor.startNewSession() } returns null
        // when
        analyticsTracker.screen("title")
        // then
        verify { analyticsQueueProcessor wasNot Called }
    }

    @Test
    fun `screen SHOULD update analyticsFlow AND queueThenFlush`() {
        // given
        val sessionId = UUID.randomUUID()
        every { sessionMonitor.sessionId } returns sessionId
        every { sessionMonitor.hasSession() } returns true
        val activity: ActivityRequest = mockk(relaxed = true)
        every { activityBuilder.screen(sessionId, any()) } returns activity
        // when
        analyticsTracker.screen("title")
        // then
        verify { sessionMonitor.hasSession() }
        assertThat(analyticsFlowUpdates).hasSize(1)
        verify { analyticsQueueProcessor.queueThenFlush(activity) }
        assertThat(analyticsFlowUpdates.first().type).isEqualTo(AnalyticType.SCREEN)
        assertThat(analyticsFlowUpdates.first().isInternal).isFalse()
        assertThat(analyticsFlowUpdates.first().request).isEqualTo(activity)
    }

    @Test
    fun `group SHOULD do nothing WHEN no active session available`() {
        // given
        every { sessionMonitor.sessionId } returns null
        every { sessionMonitor.startNewSession() } returns null
        // when
        analyticsTracker.group()
        // then
        verify { analyticsQueueProcessor wasNot Called }
    }

    @Test
    fun `group SHOULD update analyticsFlow AND flushThenSend`() {
        // given
        val sessionId = UUID.randomUUID()
        every { sessionMonitor.sessionId } returns sessionId
        every { sessionMonitor.hasSession() } returns true
        val activity: ActivityRequest = mockk(relaxed = true)
        every { activityBuilder.group(sessionId) } returns activity
        // when
        analyticsTracker.group()
        // then
        verify { sessionMonitor.hasSession() }
        assertThat(analyticsFlowUpdates).hasSize(1)
        verify { analyticsQueueProcessor.flushThenSend(activity) }
        assertThat(analyticsFlowUpdates.first().type).isEqualTo(AnalyticType.GROUP)
        assertThat(analyticsFlowUpdates.first().isInternal).isFalse()
        assertThat(analyticsFlowUpdates.first().request).isEqualTo(activity)
    }

    @Test
    fun `flushPendingActivity SHOULD flushAsync`() {
        // when
        analyticsTracker.flushPendingActivity()
        // then
        verify { analyticsQueueProcessor.flushAsync() }
    }
}

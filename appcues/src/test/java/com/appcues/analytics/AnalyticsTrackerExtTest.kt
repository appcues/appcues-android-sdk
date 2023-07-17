package com.appcues.analytics

import com.appcues.AnalyticType
import com.appcues.AppcuesCoroutineScope
import com.appcues.LoggingLevel.NONE
import com.appcues.SessionMonitor
import com.appcues.analytics.AnalyticsEvent.SessionStarted
import com.appcues.data.model.Experiment
import com.appcues.data.remote.appcues.request.ActivityRequest
import com.appcues.logging.Logcues
import com.appcues.rules.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.launch
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

internal class AnalyticsTrackerExtTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val coroutineScope = AppcuesCoroutineScope(Logcues(NONE))
    private val activityBuilder: ActivityRequestBuilder = mockk()
    private val sessionMonitor: SessionMonitor = mockk()
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
    fun `track internal event SHOULD update analyticsFlow with internal event`() {
        // given
        every { sessionMonitor.checkSession(any()) } returns true
        val activity: ActivityRequest = mockk(relaxed = true)
        every { activityBuilder.track(any(), any()) } returns activity
        // when
        analyticsTracker.track(SessionStarted)
        // then
        assertThat(analyticsFlowUpdates.first().isInternal).isTrue()
        assertThat(analyticsFlowUpdates.first().type).isEqualTo(AnalyticType.EVENT)
        assertThat(analyticsFlowUpdates.first().request).isEqualTo(activity)
    }

    @Test
    fun `track experiment SHOULD call track with correct properties set in the map`() {
        // GIVEN
        val activity: ActivityRequest = mockk(relaxed = true)
        every { activityBuilder.track(any(), any()) } returns activity
        every { sessionMonitor.checkSession(any()) } returns true
        val experiment = Experiment(
            id = UUID.fromString("06f9bf87-1921-4919-be55-429b278bf578"),
            group = "control",
            experienceId = UUID.fromString("d84c9d01-aa27-4cbb-b832-ee03720e04fc"),
            goalId = "my-goal",
            contentType = "my-content-type"
        )
        // WHEN
        analyticsTracker.track(experiment)
        // THEN
        verify {
            analyticsTracker.track(
                AnalyticsEvent.ExperimentEntered,
                mapOf(
                    "experimentId" to "06f9bf87-1921-4919-be55-429b278bf578",
                    "experimentGroup" to "control",
                    "experimentExperienceId" to "d84c9d01-aa27-4cbb-b832-ee03720e04fc",
                    "experimentGoalId" to "my-goal",
                    "experimentContentType" to "my-content-type",
                ),
                false
            )
        }
    }
}

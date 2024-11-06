package com.appcues.action.appcues

import com.appcues.AppcuesScopeTest
import com.appcues.analytics.AnalyticsTracker
import com.appcues.di.component.get
import com.appcues.rules.TestScopeRule
import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

internal class TrackEventActionTest : AppcuesScopeTest {

    @get:Rule
    override val scopeRule = TestScopeRule()

    @Test
    fun `track event SHOULD have expected type name`() {
        assertThat(TrackEventAction.TYPE).isEqualTo("@appcues/track")
    }

    @Test
    fun `track event SHOULD trigger Appcues track with event`() = runTest {
        // GIVEN
        val event = "track_event"
        val analyticsTracker: AnalyticsTracker = get()
        val action = TrackEventAction(mapOf("eventName" to event), analyticsTracker)

        // WHEN
        action.execute()

        // THEN
        coVerify { analyticsTracker.track(event) }
    }

    @Test
    fun `track event SHOULD trigger Appcues track with event and attributes`() = runTest {
        // GIVEN
        val event = "track_event"
        val analyticsTracker: AnalyticsTracker = get()
        val action = TrackEventAction(mapOf("eventName" to event, "attributes" to mapOf("_builderButtonClick" to true)), analyticsTracker)

        // WHEN
        action.execute()

        // THEN
        coVerify { analyticsTracker.track(event, mapOf("_builderButtonClick" to true)) }
    }

    @Test
    fun `track event SHOULD NOT trigger Appcues track WHEN no eventName is in config`() = runTest {
        // GIVEN
        val analyticsTracker: AnalyticsTracker = get()
        val action = TrackEventAction(mapOf(), analyticsTracker)

        // WHEN
        action.execute()

        // THEN
        coVerify { analyticsTracker wasNot Called }
    }
}

package com.appcues.action.appcues

import com.appcues.Appcues
import com.appcues.AppcuesScopeTest
import com.appcues.rules.KoinScopeRule
import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.coVerify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.koin.core.component.get

@OptIn(ExperimentalCoroutinesApi::class)
internal class TrackEventActionTest : AppcuesScopeTest {

    @get:Rule
    override val koinTestRule = KoinScopeRule()

    @Test
    fun `tracke event SHOULD have expected type name`() {
        assertThat(TrackEventAction.TYPE).isEqualTo("@appcues/track")
    }

    @Test
    fun `track event SHOULD trigger Appcues track with event`() = runTest {
        // GIVEN
        val event = "track_event"
        val appcues: Appcues = get()
        val action = TrackEventAction(mapOf("eventName" to event))

        // WHEN
        action.execute(appcues)

        // THEN
        coVerify { appcues.track(event) }
    }

    @Test
    fun `track event SHOULD NOT trigger Appcues track WHEN no eventName is in config`() = runTest {
        // GIVEN
        val appcues: Appcues = get()
        val action = TrackEventAction(mapOf())

        // WHEN
        action.execute(appcues)

        // THEN
        coVerify { appcues wasNot Called }
    }
}

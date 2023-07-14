package com.appcues

import com.appcues.analytics.AnalyticsEvent.SessionReset
import com.appcues.analytics.AnalyticsEvent.SessionResumed
import com.appcues.analytics.AnalyticsEvent.SessionStarted
import com.appcues.analytics.AnalyticsEvent.SessionSuspended
import com.appcues.analytics.AnalyticsTracker
import com.appcues.analytics.track
import com.appcues.logging.Logcues
import com.appcues.rules.KoinScopeRule
import com.appcues.rules.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.koin.core.component.get

@OptIn(ExperimentalCoroutinesApi::class)
internal class SessionMonitorTest : AppcuesScopeTest {

    @get:Rule
    override val koinTestRule = KoinScopeRule()

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun `start SHOULD NOT track a session start WHEN userID is empty`() {
        // GIVEN
        val tracker: AnalyticsTracker = get()
        val sessionMonitor = SessionMonitor(scope)

        // WHEN
        sessionMonitor.start()

        // THEN
        verify { tracker wasNot Called }
        assertThat(sessionMonitor.isActive).isFalse()
    }

    @Test
    fun `start SHOULD track a session start WHEN userID is not empty`() {
        // GIVEN
        val storage: Storage = get()
        val tracker: AnalyticsTracker = get()
        val sessionMonitor = SessionMonitor(scope)
        storage.userId = "userId"

        // WHEN
        sessionMonitor.start()

        // THEN
        verify(exactly = 1) { tracker.track(SessionStarted, any(), true) }
        assertThat(sessionMonitor.sessionId).isNotNull()
        assertThat(sessionMonitor.isActive).isTrue()
    }

    @Test
    fun `reset SHOULD track a session reset`() {
        // GIVEN
        val tracker: AnalyticsTracker = get()
        val sessionMonitor = SessionMonitor(scope)

        // WHEN
        sessionMonitor.reset()

        // THEN
        verify(exactly = 1) { tracker.track(SessionReset, any(), true) }
        assertThat(sessionMonitor.sessionId).isNull()
        assertThat(sessionMonitor.isActive).isFalse()
    }

    @Test
    fun `onStart SHOULD not track an event WHEN onStop was not previously called`() {
        // GIVEN
        val tracker: AnalyticsTracker = get()
        val sessionMonitor = SessionMonitor(scope)
        val storage: Storage = get()
        storage.userId = "userId"
        sessionMonitor.start()

        // WHEN
        sessionMonitor.onStart(mockk())

        // THEN
        verify(exactly = 1) { tracker.track(SessionStarted, any(), any()) } // only the initial start
        verify(exactly = 0) { tracker.track(SessionResumed, any(), any()) }
    }

    @Test
    fun `onStart SHOULD not track an event WHEN no current session exists`() {
        // GIVEN
        val tracker: AnalyticsTracker = get()
        val sessionMonitor = SessionMonitor(scope)

        // WHEN
        sessionMonitor.onStart(mockk())

        // THEN
        verify(exactly = 0) { tracker.track(SessionStarted, any(), any()) }
        verify(exactly = 0) { tracker.track(SessionResumed, any(), any()) }
    }

    @Test
    fun `onStop SHOULD track session suspended WHEN an active session exists`() {
        // GIVEN
        val tracker: AnalyticsTracker = get()
        val sessionMonitor = SessionMonitor(scope)
        val storage: Storage = get()
        storage.userId = "userId"
        sessionMonitor.start()

        // WHEN
        sessionMonitor.onStop(mockk())

        // THEN
        verify(exactly = 1) { tracker.track(SessionSuspended, any(), any()) }
        verify(exactly = 1) { tracker.flushPendingActivity() }
    }

    @Test
    fun `onStop SHOULD NOT track session suspended WHEN no active session exists`() {
        // GIVEN
        val tracker: AnalyticsTracker = get()
        val sessionMonitor = SessionMonitor(scope)

        // WHEN
        sessionMonitor.onStop(mockk())

        // THEN
        verify { tracker wasNot Called }
    }

    @Test
    fun `onStart SHOULD track session resumed WHEN session timeout not expired`() {
        // GIVEN
        val tracker: AnalyticsTracker = get()
        val sessionMonitor = SessionMonitor(scope)
        val storage: Storage = get()
        storage.userId = "userId"
        sessionMonitor.start()
        sessionMonitor.onStop(mockk())

        // WHEN
        sessionMonitor.onStart(mockk())

        // THEN
        verify(exactly = 1) { tracker.track(SessionResumed, any(), any()) }
    }

    @Test
    fun `onStart SHOULD track session started WHEN session timeout has expired`() {
        // GIVEN
        val tracker: AnalyticsTracker = get()
        val storage: Storage = get()
        storage.userId = "userId"
        val config: AppcuesConfig = get()
        config.sessionTimeout = -1
        val sessionMonitor = SessionMonitor(scope)
        sessionMonitor.start()
        sessionMonitor.onStop(mockk())

        // WHEN
        sessionMonitor.onStart(mockk())

        // THEN
        verify(exactly = 2) { tracker.track(SessionStarted, any(), any()) }
        verify(exactly = 0) { tracker.track(SessionResumed, any(), any()) }
    }

    @Test
    fun `checkSession SHOULD log and return false WHEN there is no active session`() {
        // GIVEN
        val logcues: Logcues = get()
        val sessionMonitor = SessionMonitor(scope)

        // WHEN
        val result = sessionMonitor.checkSession("test message")

        // THEN
        verify { logcues.info(any()) }
        assertThat(result).isFalse()
    }

    @Test
    fun `checkSession SHOULD return true WHEN there is an active session`() {
        // GIVEN
        val sessionMonitor = SessionMonitor(scope)
        val storage: Storage = get()
        storage.userId = "userId"
        sessionMonitor.start()

        // WHEN
        val result = sessionMonitor.checkSession("test message")

        // THEN
        assertThat(result).isTrue()
    }
}

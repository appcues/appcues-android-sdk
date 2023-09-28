package com.appcues

import com.appcues.di.component.get
import com.appcues.rules.MainDispatcherRule
import com.appcues.rules.TestScopeRule
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

internal class SessionMonitorTest : AppcuesScopeTest {

    @get:Rule
    override val scopeRule = TestScopeRule()

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun `startNewSession SHOULD NOT create a session WHEN userID is empty`() {
        // GIVEN
        val sessionMonitor = SessionMonitor(scope)

        // WHEN
        sessionMonitor.startNewSession()

        // THEN
        assertThat(sessionMonitor.sessionId).isNull()
    }

    @Test
    fun `start SHOULD create a session WHEN userID is not empty`() {
        // GIVEN
        val storage: Storage = get()
        val sessionMonitor = SessionMonitor(scope)
        storage.userId = "userId"

        // WHEN
        sessionMonitor.startNewSession()

        // THEN
        assertThat(sessionMonitor.sessionId).isNotNull()
    }

    @Test
    fun `reset SHOULD clear the session`() {
        // GIVEN
        val sessionMonitor = SessionMonitor(scope)

        // WHEN
        sessionMonitor.reset()

        // THEN
        assertThat(sessionMonitor.sessionId).isNull()
    }
}

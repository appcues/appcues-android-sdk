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

internal class UpdateProfileActionTest : AppcuesScopeTest {

    @get:Rule
    override val scopeRule = TestScopeRule()

    @Test
    fun `update profile SHOULD have expected type name`() {
        assertThat(UpdateProfileAction.TYPE).isEqualTo("@appcues/update-profile")
    }

    @Test
    fun `update profile SHOULD trigger Appcues identify with properties`() = runTest {
        // GIVEN
        val analyticsTracker: AnalyticsTracker = get()
        val properties = mapOf("prop1" to 2, "prop2" to "ok")
        val action = UpdateProfileAction(properties, analyticsTracker)

        // WHEN
        action.execute()

        // THEN
        coVerify { analyticsTracker.identify(properties) }
    }

    @Test
    fun `update profile SHOULD NOT trigger Appcues identify WHEN no props are in config`() = runTest {
        // GIVEN
        val analyticsTracker: AnalyticsTracker = get()
        val action = UpdateProfileAction(null, analyticsTracker)

        // WHEN
        action.execute()

        // THEN
        coVerify { analyticsTracker wasNot Called }
    }
}

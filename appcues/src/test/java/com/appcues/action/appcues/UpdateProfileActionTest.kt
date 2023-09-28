package com.appcues.action.appcues

import com.appcues.Appcues
import com.appcues.AppcuesScopeTest
import com.appcues.Storage
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
        val appcues: Appcues = get()
        val storage: Storage = get()
        val userId = "test-user"
        storage.userId = userId
        val properties = mapOf("prop1" to 2, "prop2" to "ok")
        val action = UpdateProfileAction(properties, storage, appcues)

        // WHEN
        action.execute()

        // THEN
        coVerify { appcues.identify(userId, properties) }
    }

    @Test
    fun `update profile SHOULD NOT trigger Appcues identify WHEN no props are in config`() = runTest {
        // GIVEN
        val appcues: Appcues = get()
        val action = UpdateProfileAction(null, get(), appcues)

        // WHEN
        action.execute()

        // THEN
        coVerify { appcues wasNot Called }
    }
}

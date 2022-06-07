package com.appcues.action.appcues

import com.appcues.Appcues
import com.appcues.AppcuesScopeTest
import com.appcues.rules.KoinScopeRule
import com.google.common.truth.Truth
import io.mockk.Called
import io.mockk.coVerify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.koin.core.component.get
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
internal class LaunchExperienceActionTest : AppcuesScopeTest {

    @get:Rule
    override val koinTestRule = KoinScopeRule()

    @Test
    fun `launch experience SHOULD have expected type name`() {
        Truth.assertThat(LaunchExperienceAction.TYPE).isEqualTo("@appcues/launch-experience")
    }

    @Test
    fun `launch experience SHOULD trigger Appcues show with experience ID`() = runTest {
        // GIVEN
        val experienceId: String = UUID.randomUUID().toString()
        val appcues: Appcues = get()
        val action = LaunchExperienceAction(mapOf("experienceID" to experienceId))

        // WHEN
        action.execute(appcues)

        // THEN
        coVerify { appcues.show(experienceId) }
    }

    @Test
    fun `launch experience SHOULD NOT trigger Appcues show WHEN no experience ID is in config`() = runTest {
        // GIVEN
        val appcues: Appcues = get()
        val action = LaunchExperienceAction(mapOf())

        // WHEN
        action.execute(appcues)

        // THEN
        coVerify { appcues wasNot Called }
    }
}

package com.appcues.action.appcues

import com.appcues.AppcuesScopeTest
import com.appcues.data.model.ExperienceTrigger
import com.appcues.mocks.mockExperience
import com.appcues.rules.KoinScopeRule
import com.appcues.statemachine.State.RenderingStep
import com.appcues.ui.ExperienceRenderer
import com.google.common.truth.Truth
import io.mockk.Called
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
    fun `launch experience SHOULD call ExperienceRenderer show with experience ID`() = runTest {
        // GIVEN
        val experienceId = UUID.randomUUID()
        val experienceIdString = experienceId.toString()
        val currentExperience = mockExperience()
        val experienceRenderer = mockk<ExperienceRenderer>(relaxed = true) {
            every { getState() } returns RenderingStep(currentExperience, 0, true)
        }
        val action = LaunchExperienceAction(mapOf("experienceID" to experienceIdString), experienceRenderer)

        // WHEN
        action.execute()

        // THEN
        coVerify { experienceRenderer.show(experienceIdString, ExperienceTrigger.LaunchExperienceAction(currentExperience.id)) }
    }

    @Test
    fun `launch experience SHOULD NOT call ExperienceRenderer show WHEN no experience ID is in config`() = runTest {
        // GIVEN
        val experienceRenderer: ExperienceRenderer = get()
        val action = LaunchExperienceAction(mapOf(), experienceRenderer)

        // WHEN
        action.execute()

        // THEN
        coVerify { experienceRenderer wasNot Called }
    }
}

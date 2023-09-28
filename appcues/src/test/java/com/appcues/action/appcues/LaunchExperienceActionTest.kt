package com.appcues.action.appcues

import com.appcues.AppcuesScopeTest
import com.appcues.data.model.ExperienceTrigger
import com.appcues.data.model.RenderContext
import com.appcues.di.component.get
import com.appcues.mocks.mockExperience
import com.appcues.rules.TestScopeRule
import com.appcues.statemachine.State.RenderingStep
import com.appcues.ui.ExperienceRenderer
import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.util.UUID

internal class LaunchExperienceActionTest : AppcuesScopeTest {

    @get:Rule
    override val scopeRule = TestScopeRule()

    @Test
    fun `launch experience SHOULD have expected type name`() {
        assertThat(LaunchExperienceAction.TYPE).isEqualTo("@appcues/launch-experience")
    }

    @Test
    fun `category SHOULD be internal`() {
        // GIVEN
        val experienceRenderer: ExperienceRenderer = mockk(relaxed = true)
        val action = LaunchExperienceAction(mapOf(), RenderContext.Modal, experienceRenderer)
        // THEN
        assertThat(action.category).isEqualTo("internal")
    }

    @Test
    fun `destination SHOULD match experienceId`() {
        // GIVEN
        val experienceRenderer: ExperienceRenderer = mockk(relaxed = true)
        val action = LaunchExperienceAction(mapOf("experienceID" to "1234"), RenderContext.Modal, experienceRenderer)
        // THEN
        assertThat(action.destination).isEqualTo("1234")
    }

    @Test
    fun `destination SHOULD be empty string when experienceId is not present`() {
        // GIVEN
        val experienceRenderer: ExperienceRenderer = mockk(relaxed = true)
        val action = LaunchExperienceAction(mapOf(), RenderContext.Modal, experienceRenderer)
        // THEN
        assertThat(action.destination).isEmpty()
    }

    @Test
    fun `LaunchExperienceAction custom constructor`() {
        // GIVEN
        val experienceRenderer: ExperienceRenderer = mockk(relaxed = true)
        val completedExperienceId = "1234"
        val launchExperienceId = "5678"
        val action = LaunchExperienceAction(
            renderContext = RenderContext.Modal,
            completedExperienceId = completedExperienceId,
            launchExperienceId = launchExperienceId,
            experienceRenderer = experienceRenderer
        )

        // THEN
        assertThat(action.config).containsEntry("completedExperienceID", completedExperienceId)
        assertThat(action.config).containsEntry("experienceID", launchExperienceId)
    }

    @Test
    fun `launch experience SHOULD call ExperienceRenderer show with experience ID`() = runTest {
        // GIVEN
        val experienceId = UUID.randomUUID()
        val experienceIdString = experienceId.toString()
        val currentExperience = mockExperience()
        val experienceRenderer: ExperienceRenderer = mockk(relaxed = true) {
            every { this@mockk.getState(RenderContext.Modal) } returns RenderingStep(currentExperience, 0, true)
        }
        val config = mapOf("experienceID" to experienceIdString)
        val action = LaunchExperienceAction(config, RenderContext.Modal, experienceRenderer)

        // WHEN
        action.execute()

        // THEN
        coVerify { experienceRenderer.show(experienceIdString, ExperienceTrigger.LaunchExperienceAction(currentExperience.id)) }
    }

    @Test
    fun `launch experience SHOULD call ExperienceRenderer show with null WHEN no state`() = runTest {
        // GIVEN
        val experienceId = UUID.randomUUID()
        val experienceIdString = experienceId.toString()
        val experienceRenderer: ExperienceRenderer = mockk(relaxed = true) {
            every { this@mockk.getState(RenderContext.Modal) } returns null
        }
        val config = mapOf("experienceID" to experienceIdString)
        val action = LaunchExperienceAction(config, RenderContext.Modal, experienceRenderer)

        // WHEN
        action.execute()

        // THEN
        coVerify { experienceRenderer.show(experienceIdString, ExperienceTrigger.LaunchExperienceAction(null)) }
    }

    @Test
    fun `launch experience SHOULD call ExperienceRenderer show with completedExperienceId when present`() = runTest {
        // GIVEN
        // GIVEN
        val experienceRenderer: ExperienceRenderer = mockk(relaxed = true)
        val completedExperienceId = UUID.randomUUID()
        val launchExperienceId = UUID.randomUUID()
        val action = LaunchExperienceAction(
            renderContext = RenderContext.Modal,
            completedExperienceId = completedExperienceId.toString(),
            launchExperienceId = launchExperienceId.toString(),
            experienceRenderer = experienceRenderer
        )

        // WHEN
        action.execute()

        // THEN
        coVerify {
            experienceRenderer.show(
                launchExperienceId.toString(),
                ExperienceTrigger.ExperienceCompletionAction(completedExperienceId)
            )
        }
    }

    @Test
    fun `launch experience SHOULD NOT call ExperienceRenderer show WHEN no experience ID is in config`() = runTest {
        // GIVEN
        val experienceRenderer: ExperienceRenderer = get()
        val action = LaunchExperienceAction(mapOf(), RenderContext.Modal, experienceRenderer)

        // WHEN
        action.execute()

        // THEN
        coVerify { experienceRenderer wasNot Called }
    }
}

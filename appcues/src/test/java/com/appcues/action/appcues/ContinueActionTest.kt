package com.appcues.action.appcues

import com.appcues.AppcuesScopeTest
import com.appcues.data.model.RenderContext
import com.appcues.rules.KoinScopeRule
import com.appcues.statemachine.StepReference.StepId
import com.appcues.statemachine.StepReference.StepIndex
import com.appcues.statemachine.StepReference.StepOffset
import com.appcues.ui.ExperienceRenderer
import com.google.common.truth.Truth.assertThat
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.util.UUID

internal class ContinueActionTest : AppcuesScopeTest {

    @get:Rule
    override val koinTestRule = KoinScopeRule()

    @Test
    fun `continue SHOULD have expected type name`() {
        assertThat(ContinueAction.TYPE).isEqualTo("@appcues/continue")
    }

    @Test
    fun `category SHOULD be internal`() {
        // GIVEN
        val experienceRenderer: ExperienceRenderer = mockk(relaxed = true)
        val action = ContinueAction(mapOf(), RenderContext.Modal, experienceRenderer)
        // THEN
        assertThat(action.category).isEqualTo("internal")
    }

    @Test
    fun `destination SHOULD match expected when stepReference is StepIndex`() {
        // GIVEN
        val experienceRenderer: ExperienceRenderer = mockk(relaxed = true)
        val action = ContinueAction(mapOf("index" to 1234), RenderContext.Modal, experienceRenderer)
        // THEN
        assertThat(action.destination).isEqualTo("#1234")
    }

    @Test
    fun `destination SHOULD match expected when stepReference is StepOffset with positive value`() {
        // GIVEN
        val experienceRenderer: ExperienceRenderer = mockk(relaxed = true)
        val action = ContinueAction(mapOf("offset" to 3), RenderContext.Modal, experienceRenderer)
        // THEN
        assertThat(action.destination).isEqualTo("+3")
    }

    @Test
    fun `destination SHOULD match expected when stepReference is StepOffset with negative value`() {
        // GIVEN
        val experienceRenderer: ExperienceRenderer = mockk(relaxed = true)
        val action = ContinueAction(mapOf("offset" to -3), RenderContext.Modal, experienceRenderer)
        // THEN
        assertThat(action.destination).isEqualTo("-3")
    }

    @Test
    fun `destination SHOULD match expected when stepReference is StepId`() {
        // GIVEN
        val experienceRenderer: ExperienceRenderer = mockk(relaxed = true)
        val stepId = UUID.randomUUID()
        val action = ContinueAction(mapOf("stepID" to stepId.toString()), RenderContext.Modal, experienceRenderer)
        // THEN
        assertThat(action.destination).isEqualTo(stepId.toString())
    }

    @Test
    fun `continue SHOULD trigger StateMachine StartStep with index WHEN config has index`() = runTest {
        // GIVEN
        val experienceRenderer: ExperienceRenderer = mockk(relaxed = true)
        val action = ContinueAction(mapOf("index" to 1), RenderContext.Modal, experienceRenderer)

        // WHEN
        action.execute()

        // THEN
        coVerify { experienceRenderer.show(RenderContext.Modal, StepIndex(1)) }
    }

    @Test
    fun `continue SHOULD trigger StateMachine StartStep with offset WHEN config has offset`() = runTest {
        // GIVEN
        val experienceRenderer: ExperienceRenderer = mockk(relaxed = true)
        val action = ContinueAction(mapOf("offset" to -1), RenderContext.Modal, experienceRenderer)

        // WHEN
        action.execute()

        // THEN
        coVerify { experienceRenderer.show(RenderContext.Modal, StepOffset(-1)) }
    }

    @Test
    fun `continue SHOULD trigger StateMachine StartStep with step id WHEN config has step id`() = runTest {
        // GIVEN
        val stepId = UUID.randomUUID()
        val experienceRenderer: ExperienceRenderer = mockk(relaxed = true)
        val action = ContinueAction(mapOf("stepID" to stepId.toString()), RenderContext.Modal, experienceRenderer)

        // WHEN
        action.execute()

        // THEN
        coVerify { experienceRenderer.show(RenderContext.Modal, StepId(stepId)) }
    }

    @Test
    fun `continue SHOULD trigger StateMachine StartStep with offset 1 by default`() = runTest {
        // GIVEN
        val experienceRenderer: ExperienceRenderer = mockk(relaxed = true)
        val action = ContinueAction(mapOf(), RenderContext.Modal, experienceRenderer)

        // WHEN
        action.execute()

        // THEN
        coVerify { experienceRenderer.show(RenderContext.Modal, StepOffset(1)) }
    }
}

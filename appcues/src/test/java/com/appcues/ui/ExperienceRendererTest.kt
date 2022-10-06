package com.appcues.ui

import com.appcues.mocks.mockExperience
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.State.RenderingStep
import com.appcues.statemachine.StateMachine
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExperienceRendererTest {

    @Test
    fun `dismissCurrentExperience SHOULD mark complete WHEN current state is on last step`() = runTest {
        // GIVEN
        val experience = mockExperience()
        val state = RenderingStep(experience, 3, false)
        val stateMachine: StateMachine = mockk(relaxed = true) {
            every { this@mockk.state } answers { state }
        }

        val experienceRenderer = ExperienceRenderer(mockk(), stateMachine, mockk(), mockk())

        // WHEN
        experienceRenderer.dismissCurrentExperience(markComplete = false, destroyed = false)

        // THEN
        coVerify { stateMachine.handleAction(EndExperience(markComplete = true, destroyed = false)) }
    }

    @Test
    fun `dismissCurrentExperience SHOULD NOT mark complete WHEN current state is not on last step`() = runTest {
        // GIVEN
        val experience = mockExperience()
        val state = RenderingStep(experience, 2, false)
        val stateMachine: StateMachine = mockk(relaxed = true) {
            every { this@mockk.state } answers { state }
        }

        val experienceRenderer = ExperienceRenderer(mockk(), stateMachine, mockk(), mockk())

        // WHEN
        experienceRenderer.dismissCurrentExperience(markComplete = false, destroyed = false)

        // THEN
        coVerify { stateMachine.handleAction(EndExperience(markComplete = false, destroyed = false)) }
    }
}

package com.appcues.ui.sm.owner

import com.appcues.statemachine.Action.Reset
import com.appcues.statemachine.StateMachine
import com.appcues.statemachine.states.IdlingState
import com.appcues.statemachine.states.RenderingStepState
import com.appcues.ui.ModalStateMachineOwner
import com.appcues.ui.StateMachineOwning
import com.google.common.truth.Truth.assertThat
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class ModalStateMachineOwnerTest {

    private val stateMachine = mockk<StateMachine>(relaxed = true)

    private val owner = ModalStateMachineOwner(stateMachine)

    @Test
    fun `owner SHOULD extend from StateMachineOwning`() {
        assertThat(owner).isInstanceOf(StateMachineOwning::class.java)
    }

    @Test
    fun `reset SHOULD call frame reset and stateMachine stop`() = runTest {
        // WHEN
        owner.reset()
        // THEN
        coVerifySequence {
            stateMachine.stop(true)
        }
    }

    @Test
    fun `onConfigurationChanged SHOULD send action Reset WHEN state is RenderingStep`() = runTest {
        // GIVEN
        every { stateMachine.state } returns mockk<RenderingStepState>(relaxed = true)
        // WHEN
        owner.onConfigurationChanged()
        // THEN
        coVerifySequence {
            stateMachine.state

            stateMachine.handleAction(Reset)
        }
    }

    @Test
    fun `onConfigurationChanged SHOULD not send action Reset WHEN state is not RenderingStep`() = runTest {
        // GIVEN
        every { stateMachine.state } returns mockk<IdlingState>(relaxed = true)
        // WHEN
        owner.onConfigurationChanged()
        // THEN
        coVerifySequence {
            stateMachine.state
        }
    }
}

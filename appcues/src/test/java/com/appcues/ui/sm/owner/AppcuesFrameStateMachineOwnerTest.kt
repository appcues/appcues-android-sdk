package com.appcues.ui.sm.owner

import com.appcues.AppcuesFrameView
import com.appcues.data.model.RenderContext.Embed
import com.appcues.statemachine.StateMachine
import com.appcues.ui.AppcuesFrameStateMachineOwner
import com.appcues.ui.StateMachineOwning
import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.coVerifySequence
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class AppcuesFrameStateMachineOwnerTest {

    private val frame = mockk<AppcuesFrameView>(relaxed = true)

    private val stateMachine = mockk<StateMachine>(relaxed = true)

    private val owner = AppcuesFrameStateMachineOwner(frame, Embed("frame1"), stateMachine)

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
            frame.reset()
            stateMachine.stop(false)
        }
    }

    @Test
    fun `reset SHOULD not CALL frame reset if WeakReference is null`() = runTest {
        // GIVEN
        owner.simulateGarbageCollector()
        // WHEN
        owner.reset()
        // THEN
        verify { frame wasNot Called }
    }
}

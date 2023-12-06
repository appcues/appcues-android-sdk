package com.appcues.ui.sm.owner

import com.appcues.AppcuesFrameView
import com.appcues.data.model.RenderContext
import com.appcues.data.model.RenderContext.Embed
import com.appcues.data.model.RenderContext.Modal
import com.appcues.statemachine.StateMachine
import com.appcues.ui.AppcuesFrameStateMachineOwner
import com.appcues.ui.StateMachineDirectory
import com.appcues.ui.StateMachineOwning
import com.google.common.truth.Truth.assertThat
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class StateMachineDirectoryTest {

    private val ownerDirectory = StateMachineDirectory()

    @Test
    fun `getOwner SHOULD return null`() {
        // WHEN
        val owner = ownerDirectory.getOwner(Modal)
        // THEN
        assertThat(owner).isNull()
    }

    @Test
    fun `getOwner(renderContext) SHOULD return reference set in setOwner`() {
        // GIVEN
        val renderContext = Modal
        val owner = fakeOwner(renderContext)
        ownerDirectory.setOwner(owner)
        // WHEN
        val result = ownerDirectory.getOwner(Modal)
        // THEN
        assertThat(result).isEqualTo(owner)
    }

    @Test
    fun `getOwner(frameView) SHOULD return reference set in setOwner`() {
        // GIVEN
        val frameView = mockk<AppcuesFrameView>()
        val renderContext = Embed("frame1")
        val owner = fakeFrameOwner(frameView, renderContext)
        ownerDirectory.setOwner(owner)
        // WHEN
        val result = ownerDirectory.getOwner(frameView)
        // THEN
        assertThat(result).isEqualTo(owner)
    }

    @Test
    fun `getOwner(frameView) SHOULD return null WHEN frameView reference is not the same`() {
        // GIVEN
        val frameView = mockk<AppcuesFrameView>()
        val frameView2 = mockk<AppcuesFrameView>()
        val renderContext = Embed("frame1")
        val owner = fakeFrameOwner(frameView, renderContext)
        ownerDirectory.setOwner(owner)
        // WHEN
        val result = ownerDirectory.getOwner(frameView2)
        // THEN
        assertThat(result).isNull()
    }

    @Test
    fun `resetAll SHOULD call reset for all owners`() = runTest {
        // GIVEN
        val owner1 = fakeFrameOwner(mockk(relaxed = true), Embed("frame1"))
        val owner2 = fakeFrameOwner(mockk(relaxed = true), Embed("frame2"))
        val owner3 = fakeFrameOwner(mockk(relaxed = true), Embed("frame3"))
        ownerDirectory.setOwner(owner1)
        ownerDirectory.setOwner(owner2)
        ownerDirectory.setOwner(owner3)
        // WHEN
        ownerDirectory.resetAll()
        // THEN
        coVerify {
            owner1.reset()
            owner2.reset()
            owner3.reset()
        }
    }

    @Test
    fun `cleanup SHOULD remove owners WITH null WeakReference`() = runTest {
        // GIVEN
        val owner1 = fakeFrameOwner(mockk(relaxed = true), Embed("frame1"))
        val owner2 = fakeFrameOwner(mockk(relaxed = true), Embed("frame2"))
        val owner3 = fakeFrameOwner(mockk(relaxed = true), Embed("frame3"))
        ownerDirectory.setOwner(owner1)
        ownerDirectory.setOwner(owner2)
        ownerDirectory.setOwner(owner3)
        // WHEN
        owner1.simulateGarbageCollector()
        owner2.simulateGarbageCollector()
        ownerDirectory.onScreenChange()
        // THEN
        assertThat(ownerDirectory.getOwner(Embed("frame1"))).isNull()
        assertThat(ownerDirectory.getOwner(Embed("frame2"))).isNull()
        assertThat(ownerDirectory.getOwner(Embed("frame3"))).isEqualTo(owner3)
    }

    private fun fakeOwner(renderContext: RenderContext): StateMachineOwning {
        return object : StateMachineOwning {
            override val renderContext: RenderContext
                get() = renderContext
            override val stateMachine: StateMachine
                get() = mockk()

            override suspend fun reset() = Unit
        }
    }

    private fun fakeFrameOwner(frame: AppcuesFrameView, renderContext: Embed): AppcuesFrameStateMachineOwner {
        return AppcuesFrameStateMachineOwner(frame, renderContext, mockk(relaxed = true))
    }
}

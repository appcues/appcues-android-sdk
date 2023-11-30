package com.appcues.ui.presentation

import android.view.View
import android.view.ViewGroup
import com.appcues.AppcuesFrameView
import com.appcues.data.model.RenderContext
import com.appcues.ui.AppcuesFrameStateMachineOwner
import com.appcues.ui.StateMachineDirectory
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import org.junit.Test

internal class EmbedViewPresenterTest {

    private val renderContext = RenderContext.Embed("frame1")
    private val ownerDirectory = mockk<StateMachineDirectory>(relaxed = true)

    private val presenter = EmbedViewPresenter(mockk(relaxed = true), renderContext, ownerDirectory)

    @Test
    fun `presenter SHOULD not handle back press`() {
        assertThat(presenter.shouldHandleBack).isFalse()
    }

    @Test
    fun `setupView should set frameVisibility to true and return ComposeView`() {
        // GIVEN
        val frameView = mockk<AppcuesFrameView>(relaxed = true)
        val owner = mockk<AppcuesFrameStateMachineOwner>(relaxed = true) {
            every { frame } returns frameView
        }
        every { ownerDirectory.getOwner(renderContext) } returns owner
        // WHEN
        with(presenter) { with(mockk<ViewGroup>()) { setupView(mockk()) } }
        // THEN
        verifySequence {
            frameView.visibility = View.VISIBLE
            frameView.composeView
        }
    }

    @Test
    fun `removeView should set frameVisibility to true and return ComposeView`() {
        // GIVEN
        val frameView = mockk<AppcuesFrameView>(relaxed = true)
        val owner = mockk<AppcuesFrameStateMachineOwner>(relaxed = true) {
            every { frame } returns frameView
        }
        every { ownerDirectory.getOwner(renderContext) } returns owner
        // WHEN
        with(presenter) { with(mockk<ViewGroup>()) { removeView() } }
        // THEN
        verifySequence {
            frameView.visibility = View.GONE
            frameView.reset()
        }
    }
}

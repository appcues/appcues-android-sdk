package com.appcues.ui.presentation

import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.Test

internal class OverlayViewPresenterTest {

    val presenter = OverlayViewPresenter(mockk(relaxed = true), mockk(relaxed = true))

    @Test
    fun `presenter SHOULD handle back press`() {
        assertThat(presenter.shouldHandleBack).isTrue()
    }
}

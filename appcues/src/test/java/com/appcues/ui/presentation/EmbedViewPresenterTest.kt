package com.appcues.ui.presentation

import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.Test

internal class EmbedViewPresenterTest {

    val presenter = EmbedViewPresenter(mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true))

    @Test
    fun `presenter SHOULD not handle back press`() {
        assertThat(presenter.shouldHandleBack).isFalse()
    }
}

package com.appcues.debugger.screencapture

import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class ElementSelectorTest {
    @Test
    fun `empty selector properties SHOULD NOT create a valid selector`() {
        // given
        val selector = AndroidViewSelector(
            mapOf(
                AndroidViewSelector.SELECTOR_APPCUES_ID to "",
                AndroidViewSelector.SELECTOR_TAG to "",
                AndroidViewSelector.SELECTOR_CONTENT_DESCRIPTION to "",
                AndroidViewSelector.SELECTOR_RESOURCE_NAME to ""
            )
        )
        // then
        assertThat(selector.isValid).isFalse()
        assertThat(selector.toMap()).isEmpty()
    }
}

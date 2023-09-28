package com.appcues.action.appcues

import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class RequestReviewActionTest {

    @Test
    fun `link SHOULD have expected type name`() {
        assertThat(RequestReviewAction.TYPE).isEqualTo("@appcues/request-review")
    }

    // execute is not easily tested because of getIntent
}

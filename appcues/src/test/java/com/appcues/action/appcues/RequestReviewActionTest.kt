package com.appcues.action.appcues

import com.appcues.AppcuesScopeTest
import com.appcues.rules.KoinScopeRule
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

internal class RequestReviewActionTest : AppcuesScopeTest {

    @get:Rule
    override val koinTestRule = KoinScopeRule()

    @Test
    fun `link SHOULD have expected type name`() {
        assertThat(RequestReviewAction.TYPE).isEqualTo("@appcues/request-review")
    }

    // execute is not easily tested because of getIntent
}
